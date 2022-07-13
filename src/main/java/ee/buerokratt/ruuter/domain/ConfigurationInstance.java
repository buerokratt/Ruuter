package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.ConfigurationService;
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
    private final Map<String, ConfigurationStep> steps;
    private final Map<String, Object> requestBody;
    private final Map<String, Object> requestParams;
    private final String requestOrigin;
    private final ConfigurationService configurationService;
    private final ApplicationProperties properties;
    private final ScriptingHelper scriptingHelper;
    private final MappingHelper mappingHelper;
    private final HttpHelper httpHelper;
    private final Tracer tracer;
    private final Map<String, Object> context = new HashMap<>();

    private Object returnValue;
    private Integer returnStatus;
    private Map<String, String> returnHeaders = new HashMap<>();

    public void execute(String configurationName) {
        List<String> stepNames = steps.keySet().stream().toList();
        try {
            executeStep(stepNames.get(0), stepNames);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error executing configuration: %s".formatted(configurationName), requestOrigin, "", e);
            setReturnValue(null);
        }
    }

    private void executeStep(String stepName, List<String> stepNames) {
        ConfigurationStep stepToExecute = steps.get(stepName);
        stepToExecute.execute(this);
        executeNextStep(stepToExecute, stepNames);
    }

    private void executeNextStep(ConfigurationStep previousStep, List<String> stepNames) {
        if (previousStep.getNextStepName() == null) {
            int nextStepIndex = stepNames.indexOf(previousStep.getName()) + 1;
            if (nextStepIndex >= stepNames.size()) {
                return;
            }
            executeStep(stepNames.get(nextStepIndex), stepNames);
        } else if (!previousStep.getNextStepName().equals("end")) {
            executeStep(previousStep.getNextStepName(), stepNames);
        }
    }
}
