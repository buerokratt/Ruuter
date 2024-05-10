package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.helper.exception.ScriptEvaluationException;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.opentelemetry.api.trace.Span;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DslStep {
    @JsonAlias({"step"})
    private String name;
    @JsonAlias({"next"})
    private String nextStepName;
    private Boolean skip;
    private Long sleep;
    private Integer maxRecursions;
    @JsonAlias({"reloadDsls"})
    private boolean reloadDsl = false;

    public final void execute(DslInstance di) throws StepExecutionException {
        Span newSpan = di.getTracer().spanBuilder(name).startSpan();
        long startTime = System.currentTimeMillis();

        try {
            if (sleep != null) {
                Thread.sleep(sleep);
            }
            if (!Boolean.TRUE.equals(skip)) {
                executeStepAction(di);
            }
            logStep(System.currentTimeMillis() - startTime, di);
        } catch (ScriptEvaluationException see) {
            handleFailedResult(di);
            di.setErrorMessage("ScriptingException: " + see.getMessage());
            di.setErrorStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            throw new StepExecutionException(name, see);
        } catch (Exception e) {
            handleFailedResult(di);
            di.setErrorMessage(e.getMessage());
            di.setErrorStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            throw new StepExecutionException(name, e);
        } finally {
            newSpan.end();
        }
    }

    protected void handleFailedResult(DslInstance di) {
        LoggingUtils.logError(log, "Error: %s".formatted(name), di.getRequestOrigin(), getType());

        if (di.getProperties().getLogging().getMeaningfulErrors() != null &&
            di.getProperties().getLogging().getMeaningfulErrors() &&
            di.getErrorMessage() != null) {
            LoggingUtils.logError(log, "Error: %s, message: ".formatted(name, di.getErrorMessage()), di.getRequestOrigin(), getType());
        }
    }

    protected void logStep(Long elapsedTime, DslInstance di) {
        LoggingUtils.logStep(log, this, di.getRequestOrigin(), elapsedTime, "-", "-", "-", "-");
    }

    protected abstract void executeStepAction(DslInstance di);

    public abstract String getType();
}
