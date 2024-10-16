package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.domain.Logging;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.util.LoggingUtils;
import io.netty.channel.ConnectTimeoutException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;


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
    @JsonSubTypes.Type(value = HttpPutStep.class, name = "http.put"),
    @JsonSubTypes.Type(value = HttpDeleteStep.class, name = "http.delete"),
})
@NoArgsConstructor
public abstract class HttpStep extends DslStep {
    @JsonAlias({"result"})
    protected String resultName;
    protected HttpQueryArgs args;
    protected String call;
    protected DefaultHttpDsl localHttpExceptionDsl;
    protected Logging logging;

    protected Integer limit;

    @JsonAlias("error")
    protected String onErrorStep;

    @Override
    protected void executeStepAction(DslInstance di) {
        args.checkUrl(di);
        ResponseEntity<Object> response;
        try {
             response = getRequestResponse(di);
        } catch (WebClientRequestException | WebClientResponseException wcre) {
            di.setErrorMessage("Webclient error: "+wcre.getMessage());
            if (wcre.getRootCause() instanceof ConnectTimeoutException)
                di.setErrorStatus(HttpStatus.REQUEST_TIMEOUT);
            else
                di.setErrorStatus(HttpStatus.BAD_REQUEST);
            throw wcre;
        }
        di.getContext().put(resultName, new HttpStepResult(args, response, MDC.get("spanId")));

        if (!isAllowedHttpStatusCode(di, response.getStatusCodeValue())) {
            if (getOnErrorStep() != null) {
                setNextStepName(getOnErrorStep());
            }
            else {
                di.setErrorStatus(HttpStatus.valueOf(response.getStatusCodeValue()));
                di.setErrorMessage("HTTP returned with non-OK");
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void handleFailedResult(DslInstance di) {
        super.handleFailedResult(di);
        HttpStepResult stepResult = (HttpStepResult) di.getContext().get(                   resultName);
        if (stepResult != null && !isAllowedHttpStatusCode(di, stepResult.getResponse().getStatusCodeValue())) {
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
        String responseContent = responseBody != null && displayResponseContent(properties) ? responseBody : "-";
        String requestContent = args.getBody() != null && displayRequestContent(properties) ? args.getBody().toString() : "-";
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

    private boolean displayResponseContent(ApplicationProperties properties) {
        if (logging != null && Boolean.TRUE.equals(logging.getDisplayResponseContent())) {
            return true;
        }

        if (properties.getLogging() != null &&
            (logging == null || logging.getDisplayResponseContent() == null) &&
            Boolean.TRUE.equals(properties.getLogging().getDisplayResponseContent())) {
            return true;
        }

        return false;
    }

    private boolean displayRequestContent(ApplicationProperties properties) {
        if (logging != null && Boolean.TRUE.equals(logging.getDisplayRequestContent())) {
            return true;
        }

        if (properties.getLogging() != null &&
            (logging == null || logging.getDisplayRequestContent() == null) &&
            Boolean.TRUE.equals(properties.getLogging().getDisplayRequestContent())) {
            return true;
        }

        return false;
    }

    protected abstract ResponseEntity<Object> getRequestResponse(DslInstance di);
}
