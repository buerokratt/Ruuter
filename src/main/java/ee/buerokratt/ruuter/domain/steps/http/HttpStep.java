package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.steps.DslStep;
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
public abstract class HttpStep extends DslStep {
    @JsonAlias({"result"})
    protected String resultName;
    protected HttpQueryArgs args;
    protected String call;
    protected DefaultHttpDsl localHttpExceptionDsl;

    @Override
    protected void executeStepAction(DslInstance di) {
        ResponseEntity<Object> response = getRequestResponse(di);
        di.getContext().put(resultName, new HttpStepResult(args, response, MDC.get("spanId")));

        if (!isAllowedHttpStatusCode(di, response.getStatusCodeValue())) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void handleFailedResult(DslInstance di) {
        super.handleFailedResult(di);
        if (!isAllowedHttpStatusCode(di, ((HttpStepResult) di.getContext().get(resultName)).getResponse().getStatusCodeValue())) {
            DefaultHttpDsl globalHttpExceptionDsl = di.getProperties().getDefaultDslInCaseOfException();
            if (localHttpExceptionDslExists()) {
                localHttpExceptionDsl.executeHttpDefaultDsl(di, resultName);
            } else if (globalHttpExceptionDslExists(globalHttpExceptionDsl)) {
                globalHttpExceptionDsl.executeHttpDefaultDsl(di, resultName);
            }
        }
    }

    @Override
    protected void logStep(Long elapsedTime, DslInstance di) {
        ApplicationProperties properties = di.getProperties();
        MappingHelper mappingHelper = di.getMappingHelper();
        Integer responseStatus = ((HttpStepResult) di.getContext().get(resultName)).getResponse().getStatusCodeValue();
        String responseBody = mappingHelper.convertObjectToString(((HttpStepResult) di.getContext().get(resultName)).getResponse().getBody());
        String responseContent = responseBody != null && properties.getLogging().getDisplayResponseContent() ? responseBody : "-";
        String requestContent = args.getBody() != null && properties.getLogging().getDisplayRequestContent() ? args.getBody().toString() : "-";
        LoggingUtils.logStep(log, this, di.getRequestOrigin(), elapsedTime, args.getUrl(), requestContent, responseContent, String.valueOf(responseStatus));
    }

    private boolean isAllowedHttpStatusCode(DslInstance di, Integer response) {
        return di.getProperties().getHttpCodesAllowList().isEmpty() || di.getProperties().getHttpCodesAllowList().contains(response);
    }

    private boolean localHttpExceptionDslExists() {
        return localHttpExceptionDsl != null && localHttpExceptionDsl.getDsl() != null;
    }

    private boolean globalHttpExceptionDslExists(DefaultHttpDsl globalHttpExceptionDsl) {
        return globalHttpExceptionDsl != null && globalHttpExceptionDsl.getDsl() != null;
    }

    protected abstract ResponseEntity<Object> getRequestResponse(DslInstance di);
}
