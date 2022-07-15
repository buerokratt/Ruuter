package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

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

    public final void execute(DslInstance di) {
        Span newSpan = di.getTracer().nextSpan().name(name);
        long startTime = System.currentTimeMillis();

        try (Tracer.SpanInScope ws = di.getTracer().withSpan(newSpan.start())) {
            if (sleep != null) {
                Thread.sleep(sleep);
            }
            if (!Boolean.TRUE.equals(skip)) {
                executeStepAction(di);
            }
            logStep(System.currentTimeMillis() - startTime, di);
        } catch (Exception e) {
            handleFailedResult(di);
            if (di.getProperties().getStopInCaseOfException() != null && di.getProperties().getStopInCaseOfException()) {
                throw new StepExecutionException(name, e);
            }
        } finally {
            newSpan.end();
        }
    }

    protected void handleFailedResult(DslInstance di) {
        LoggingUtils.logError(log, "Error: %s".formatted(name), di.getRequestOrigin(), getType());
    }

    protected void logStep(Long elapsedTime, DslInstance di) {
        LoggingUtils.logStep(log, this, di.getRequestOrigin(), elapsedTime, "-", "-", "-", "-");
    }

    protected abstract void executeStepAction(DslInstance di);

    public abstract String getType();
}
