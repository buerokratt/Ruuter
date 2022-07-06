package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.net.http.HttpResponse;
import java.util.Map;

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

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        HttpResponse<String> response = getHttpRequestResponse(ci);
        JsonNode responseBody = response.body().isEmpty() ? null : ci.getMappingHelper().convertStringToNode(response.body());
        HttpQueryResponse httpQueryResponse = new HttpQueryResponse(responseBody, response.headers().map(), response.statusCode(), MDC.get("spanId"));
        ci.getContext().put(resultName, new HttpStepResult(args, httpQueryResponse));
        if (!ci.getProperties().getHttpCodesAllowList().isEmpty() && !ci.getProperties().getHttpCodesAllowList().contains(response.statusCode())) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void handleFailedResult(ConfigurationInstance ci) {
        super.handleFailedResult(ci);
        ApplicationProperties.DefaultAction defaultAction = ci.getProperties().getDefaultAction();
        if (defaultAction != null && defaultAction.getService() != null) {
            HttpQueryResponse response = ((HttpStepResult) ci.getContext().get(resultName)).getResponse();
            Map<String, Object> body = defaultAction.getBody();
            body.put("statusCode", response.getStatus().toString());
            body.put("responseBody", ci.getMappingHelper().convertObjectToString(response.getBody()));
            body.put("failedRequestId", MDC.get("spanId"));
            ci.getConfigurationService().execute(defaultAction.getService(), defaultAction.getBody(), defaultAction.getQuery(), ci.getRequestOrigin());
        }
    }

    @Override
    protected void logStep(Long elapsedTime, ConfigurationInstance ci) {
        ApplicationProperties properties = ci.getProperties();
        Integer responseStatus = ((HttpStepResult) ci.getContext().get(resultName)).getResponse().getStatus();
        JsonNode responseNode = ((HttpStepResult) ci.getContext().get(resultName)).getResponse().getBody();
        String responseContent = responseNode != null && properties.getLogging().getDisplayResponseContent() ? responseNode.toString() : "-";
        String requestContent = args.getBody() != null && properties.getLogging().getDisplayRequestContent() ? args.getBody().toString() : "-";
        LoggingUtils.logStep(log, this, ci.getRequestOrigin(), elapsedTime, args.getUrl(), requestContent, responseContent, String.valueOf(responseStatus));
    }

    protected abstract HttpResponse<String> getHttpRequestResponse(ConfigurationInstance ci);
}
