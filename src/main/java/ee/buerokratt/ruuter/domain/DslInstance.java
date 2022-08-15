package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.DslService;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Tracer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Data
@RequiredArgsConstructor
public class DslInstance {
    private final Map<String, DslStep> steps;
    private final Map<String, Object> requestBody;
    private final Map<String, Object> requestQuery;
    private final Map<String, String> requestHeaders;
    private final String requestOrigin;
    private final DslService dslService;
    private final ApplicationProperties properties;
    private final ScriptingHelper scriptingHelper;
    private final MappingHelper mappingHelper;
    private final HttpHelper httpHelper;
    private final Tracer tracer;
    private final Map<String, Object> context = new HashMap<>();

    private Object returnValue;
    private Integer returnStatus;
    private int currentLoopMaxRecursions = 1;
    private Map<String, String> returnHeaders = new HashMap<>();

    public void execute(String dslName) {
        addGlobalIncomingHeadersToRequestHeaders();
        List<String> stepNames = steps.keySet().stream().toList();
        try {
            executeStep(stepNames.get(0), stepNames);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error executing DSL: %s".formatted(dslName), requestOrigin, "", e);
            setReturnValue(null);
        }
        setAllStepsCurrentRecursionsToZero();
    }

    private void executeStep(String stepName, List<String> stepNames) {
        DslStep stepToExecute = steps.get(stepName);
        if (!Objects.equals(stepToExecute.getCurrentRecursions(), getStepMaxRecursions(stepToExecute))) {
            stepToExecute.execute(this);
            Integer maxRecursions = getStepMaxRecursions(stepToExecute);
            if (stepToExecute.getCurrentRecursions() > 1 && maxRecursions != null && maxRecursions > currentLoopMaxRecursions) {
                setCurrentLoopMaxRecursions(maxRecursions);
            }
        }
        findNextStepToExecute(stepToExecute, stepNames);
    }

    private void findNextStepToExecute(DslStep previousStep, List<String> stepNames) {
        if (previousStep.getNextStepName() == null) {
            int nextStepIndex = stepNames.indexOf(previousStep.getName()) + 1;
            if (nextStepIndex >= stepNames.size()) {
                return;
            }
            DslStep nextStep = steps.get(stepNames.get(nextStepIndex));
            executeNextStep(nextStep, stepNames);
        } else if (!previousStep.getNextStepName().equals("end")) {
            DslStep nextStep = steps.get(previousStep.getNextStepName());
            executeNextStep(nextStep, stepNames);
        }
    }

    private void executeNextStep(DslStep nextStep, List<String> stepNames) {
        if (Objects.equals(nextStep.getCurrentRecursions(), currentLoopMaxRecursions)) {
            int nextStepIndex = stepNames.indexOf(nextStep.getName());
            executeNextStepOutsideLoop(nextStepIndex, stepNames);
        } else {
            executeStep(nextStep.getName(), stepNames);
        }
    }

    private void executeNextStepOutsideLoop(int nextStepIndex, List<String> stepNames) {
        for (int i = nextStepIndex; i < stepNames.size(); i++) {
            DslStep nextStep = steps.get(stepNames.get(i));
            if (!Objects.equals(nextStep.getCurrentRecursions(), getStepMaxRecursions(nextStep))) {
                setCurrentLoopMaxRecursions(1);
                executeStep(nextStep.getName(), stepNames);
                break;
            }
        }
    }

    private Integer getStepMaxRecursions(DslStep step) {
        Integer globalMaxStepRecursions = properties.getMaxStepRecursions();
        Integer stepSpecificMaxRecursions = step.getMaxRecursions();
        if (globalMaxStepRecursions == null) {
            return stepSpecificMaxRecursions;
        }
        return stepSpecificMaxRecursions != null && stepSpecificMaxRecursions < globalMaxStepRecursions ? stepSpecificMaxRecursions : globalMaxStepRecursions;
    }

    private void addGlobalIncomingHeadersToRequestHeaders() {
        Map<String, Object> evaluatedHeaders = scriptingHelper.evaluateScripts(properties.getIncomingRequests().getHeaders(), context, requestBody, requestQuery, requestHeaders);
        requestHeaders.putAll(mappingHelper.convertMapObjectValuesToString(evaluatedHeaders));
    }

    private void setAllStepsCurrentRecursionsToZero() {
        steps.forEach((k, v) -> v.setCurrentRecursions(0));
    }
}
