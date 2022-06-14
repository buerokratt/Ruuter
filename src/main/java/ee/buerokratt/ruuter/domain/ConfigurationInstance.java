package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class ConfigurationInstance {
    private Map<String, ConfigurationStep> steps;
    private Map<String, String> requestBody;
    private Map<String, String> requestParams;
    private HashMap<String, Object> context;
    private Object returnValue;

    public ConfigurationInstance(Map<String, ConfigurationStep> steps, Map<String, String> requestBody, Map<String, String> requestParams) {
        this.steps = steps;
        this.requestBody = requestBody;
        this.requestParams = requestParams;
        this.context = new HashMap<>();
    }

    public void execute() {
        List<String> configurationNames = steps.keySet().stream().toList();
        try {
            executeStep(configurationNames.get(0), configurationNames);
        } catch (Exception e) {
            log.error("encountered error when executing configurationInstance", e);
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
        } else {
            executeStep(previousStep.getNextStepName(), configurationNames);
        }
    }
}
