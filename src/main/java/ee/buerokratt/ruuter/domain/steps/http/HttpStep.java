package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.service.ConfigurationService;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

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

    public boolean isValidStatusCode(ConfigurationInstance ci, ConfigurationService configurationService)  {
        ApplicationProperties properties = ci.getProperties();
        Integer responseStatus = ((HttpStepResult) ci.getContext().get(resultName)).getResponse().getStatus();
        boolean isValidStatusCode = properties.getHttpCodesAllowList().contains(responseStatus);
        if (!isValidStatusCode) {
            ApplicationProperties.DefaultAction defaultAction = properties.getDefaultAction();
            HttpQueryResponse response = ((HttpStepResult) ci.getContext().get(resultName)).getResponse();
            HashMap<String, String> body = defaultAction.getBody();
            body.put("statusCode", responseStatus.toString());
            body.put("responseBody", ci.getMappingHelper().convertObjectToString(response.getBody()));
            body.put("failedRequestId", response.getRequestId());
            configurationService.execute(defaultAction.getService(), defaultAction.getBody(), defaultAction.getQuery(), ci.getRequestOrigin());
        }
        return isValidStatusCode;
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
}
