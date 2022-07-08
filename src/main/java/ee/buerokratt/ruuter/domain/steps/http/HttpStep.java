package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "call")
@JsonSubTypes({
    @JsonSubTypes.Type(value = HttpGetStep.class, name = "http.get"),
    @JsonSubTypes.Type(value = HttpPostStep.class, name = "http.post"),
})
@NoArgsConstructor
public abstract class HttpStep extends ConfigurationStep {
    @JsonAlias({"result"})
    protected String resultName;
    protected HttpQueryArgs args;
    protected String call;
    protected HttpDefaultService httpDefaultService;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        ResponseEntity<Object> response = getRequestResponse(ci);
        HttpQueryResponse httpQueryResponse = new HttpQueryResponse(response.getBody(), response.getHeaders(), response.getStatusCodeValue(), MDC.get("spanId"));
        ci.getContext().put(resultName, new HttpStepResult(args, httpQueryResponse));

        if (!isAllowedHttpStatusCode(ci, response)) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isAllowedHttpStatusCode(ConfigurationInstance ci, ResponseEntity<Object> response) {
        return ci.getProperties().getHttpCodesAllowList().isEmpty() || ci.getProperties().getHttpCodesAllowList().contains(response.getStatusCodeValue());
    }

    @Override
    public void handleFailedResult(ConfigurationInstance ci) {
        super.handleFailedResult(ci);
        if (!ci.getProperties().getHttpCodesAllowList().contains(((HttpStepResult) ci.getContext().get(resultName)).getResponse().getStatus())) {
            HttpDefaultService propertiesHttpDefaultService = ci.getProperties().getDefaultServiceInCaseOfException();
            if (httpDefaultService != null && httpDefaultService.getService() != null) {
                httpDefaultService.executeHttpDefaultAction(ci, resultName);
            } else if (propertiesHttpDefaultService != null && propertiesHttpDefaultService.getService() != null) {
                propertiesHttpDefaultService.executeHttpDefaultAction(ci, resultName);
            }
        }
    }

    @Override
    protected void logStep(Long elapsedTime, ConfigurationInstance ci) {
        ApplicationProperties properties = ci.getProperties();
        MappingHelper mappingHelper = ci.getMappingHelper();
        Integer responseStatus = ((HttpStepResult) ci.getContext().get(resultName)).getResponse().getStatus();
        String responseBody = mappingHelper.convertObjectToString(((HttpStepResult) ci.getContext().get(resultName)).getResponse().getBody());
        String responseContent = responseBody != null && properties.getLogging().getDisplayResponseContent() ? responseBody : "-";
        String requestContent = args.getBody() != null && properties.getLogging().getDisplayRequestContent() ? args.getBody().toString() : "-";
        LoggingUtils.logStep(log, this, ci.getRequestOrigin(), elapsedTime, args.getUrl(), requestContent, responseContent, String.valueOf(responseStatus));
    }

    protected abstract ResponseEntity<Object> getRequestResponse(ConfigurationInstance ci);
}
