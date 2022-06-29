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

    public final void execute(ConfigurationInstance configurationInstance) {
        Span newSpan = configurationInstance.getTracer().nextSpan().name(name);
        long startTime = System.currentTimeMillis();

        try (Tracer.SpanInScope ws = configurationInstance.getTracer().withSpan(newSpan.start())) {
            if (!Boolean.TRUE.equals(skip)) {
                executeStepAction(configurationInstance);
            }
            logStep(System.currentTimeMillis() - startTime, configurationInstance);
        } catch (Exception e) {
            LoggingUtils.logStepError(log, getType(), configurationInstance.getRequestOrigin(), name);
            if (configurationInstance.getProperties().isStopProcessingUnRespondingService()) {
                throw new StepExecutionException(name, e);
            }
        } finally {
            newSpan.end();
        }
    }

    public abstract String getType();

    protected abstract void executeStepAction(ConfigurationInstance configurationInstance);

    protected void logStep(Long elapsedTime, ConfigurationInstance configurationInstance) {
        LoggingUtils.logStep(log, this, configurationInstance.getRequestOrigin(), elapsedTime, "-", "-", "-", "-");
    }
}
