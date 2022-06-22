package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@RequiredArgsConstructor
public class ConfigurationInstance {
    private final ScriptingHelper scriptingHelper;
    private final Map<String, ConfigurationStep> steps;
    private final Map<String, String> requestBody;
    private final Map<String, String> requestParams;
    private final HashMap<String, Object> context = new HashMap<>();
    private Object returnValue;

    public void execute() {
        List<String> configurationNames = steps.keySet().stream().toList();
        try {
            executeStep(configurationNames.get(0), configurationNames);
        } catch (Exception e) {
            log.error("encountered error when executing configurationInstance", e);
            setReturnValue(null);
        }
    }

    private void executeStep(String stepName, List<String> configurationNames) {
        ConfigurationStep stepToExecute = steps.get(stepName);
        if (Boolean.TRUE.equals(stepToExecute.getSkip())) {
            log.info("Skipping step: %s".formatted(stepName));
        } else {
            stepToExecute.execute(this);
        }
        executeNextStep(stepToExecute, configurationNames);
    }

    private void executeNextStep(ConfigurationStep previousStep, List<String> configurationNames) {
        if (previousStep.getNextStepName() == null) {
            int nextStepIndex = configurationNames.indexOf(previousStep.getName()) + 1;
            if (nextStepIndex >= configurationNames.size()) {
                return;
            }
            executeStep(configurationNames.get(nextStepIndex), configurationNames);
        } else if (!previousStep.getNextStepName().equals("end")) {
            executeStep(previousStep.getNextStepName(), configurationNames);
        }
    }
}
