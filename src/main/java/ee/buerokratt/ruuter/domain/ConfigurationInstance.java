package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Tracer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@RequiredArgsConstructor
public class ConfigurationInstance {
    private final ScriptingHelper scriptingHelper;
    private final ApplicationProperties properties;
    private final Map<String, ConfigurationStep> steps;
    private final Map<String, String> requestBody;
    private final Map<String, String> requestParams;
    private final HashMap<String, Object> context = new HashMap<>();
    private final String requestOrigin;
    private final Tracer tracer;
    private Object returnValue;

    public void execute(String configurationName) {
        List<String> stepNames = steps.keySet().stream().toList();
        try {
            LoggingUtils.logIncomingRequest(log, configurationName, requestOrigin);
            executeStep(stepNames.get(0), stepNames);
            LoggingUtils.logRequestProcessed(log, configurationName, requestOrigin);
        } catch (Exception e) {
            LoggingUtils.logRequestError(log, configurationName, requestOrigin, e);
        }
    }

    private void executeStep(String stepName, List<String> configurationNames) {
        ConfigurationStep stepToExecute = steps.get(stepName);
        stepToExecute.execute(this);
        executeNextStep(stepToExecute, configurationNames);
    }

    private void executeNextStep(ConfigurationStep previousStep, List<String> configurationNames) {
        if (previousStep.getNextStepName() == null) {
            int nextStepIndex = configurationNames.indexOf(previousStep.getName()) + 1;
            if (nextStepIndex >= configurationNames.size()) {
                return;
            }
            executeStep(configurationNames.get(nextStepIndex), configurationNames);
        } else {
            executeStep(previousStep.getNextStepName(), configurationNames);
        }
    }
}
