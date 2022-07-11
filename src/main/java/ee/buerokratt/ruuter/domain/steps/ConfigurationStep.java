package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
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
public abstract class ConfigurationStep {
    @JsonAlias({"step"})
    private String name;
    @JsonAlias({"next"})
    private String nextStepName;
    private Boolean skip;
    private Long sleep;

    public final void execute(ConfigurationInstance ci) {
        Span newSpan = ci.getTracer().nextSpan().name(name);
        long startTime = System.currentTimeMillis();

        try (Tracer.SpanInScope ws = ci.getTracer().withSpan(newSpan.start())) {
            if (sleep != null) {
                Thread.sleep(sleep);
            }
            if (!Boolean.TRUE.equals(skip)) {
                executeStepAction(ci);
            }
            logStep(System.currentTimeMillis() - startTime, ci);
        }  catch (Exception e) {
            handleFailedResult(ci);
            if (ci.getProperties().getStopInCaseOfException() != null && ci.getProperties().getStopInCaseOfException()) {
                throw new StepExecutionException(name, e);
            }
        } finally {
            newSpan.end();
        }
    }

    protected void handleFailedResult(ConfigurationInstance ci) {
        LoggingUtils.logStepError(log, getType(), ci.getRequestOrigin(), name);
    }

    public abstract String getType();

    protected abstract void executeStepAction(ConfigurationInstance configurationInstance);

    protected void logStep(Long elapsedTime, ConfigurationInstance configurationInstance) {
        LoggingUtils.logStep(log, this, configurationInstance.getRequestOrigin(), elapsedTime, "-", "-", "-", "-");
    }
}
