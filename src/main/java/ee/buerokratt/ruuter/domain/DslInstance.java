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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Data
@RequiredArgsConstructor
public class DslInstance {
    private final String name;
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
    private Map<String, Integer> recursions;
    private boolean returnWithWrapper;

    public void execute() {
        addGlobalIncomingHeadersToRequestHeaders();
        List<String> stepNames = steps.keySet().stream().toList();
        recursions = stepNames.stream().collect(Collectors.toMap(Function.identity(), a -> 0));
        try {
            executeStep(stepNames.get(0), stepNames);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error executing DSL: %s".formatted(name), requestOrigin, "", e);
            clearReturnValues();
        }
    }

    private void executeStep(String stepName, List<String> stepNames) {
        DslStep stepToExecute = steps.get(stepName);
        if (!Objects.equals(recursions.get(stepName), getStepMaxRecursions(stepToExecute))) {
            stepToExecute.execute(this);
            recursions.computeIfPresent(stepName, (k, v) ->  v + 1);
            Integer maxRecursions = getStepMaxRecursions(stepToExecute);
            if (recursions.get(stepName) > 1 && maxRecursions != null && maxRecursions > currentLoopMaxRecursions) {
                setCurrentLoopMaxRecursions(maxRecursions);
            }
        }
        executeNextStep(stepToExecute, stepNames);
    }

    private void executeNextStep(DslStep previousStep, List<String> stepNames) {
        if (Boolean.TRUE.equals(previousStep.getSkip()) || previousStep.getNextStepName() == null) {
            int nextStepIndex = stepNames.indexOf(previousStep.getName()) + 1;
            if (nextStepIndex >= stepNames.size()) {
                return;
            }
            DslStep nextStep = steps.get(stepNames.get(nextStepIndex));
            executeNextStepWithoutMaxRecursionsExceeded(nextStep, stepNames);
        } else if (!previousStep.getNextStepName().equals("end")) {
            DslStep nextStep = steps.get(previousStep.getNextStepName());
            executeNextStepWithoutMaxRecursionsExceeded(nextStep, stepNames);
        }
    }

    private void executeNextStepWithoutMaxRecursionsExceeded(DslStep nextStep, List<String> stepNames) {
        if (Objects.equals(recursions.get(nextStep.getName()), currentLoopMaxRecursions)) {
            int nextStepIndex = stepNames.indexOf(nextStep.getName());
            executeNextStepOutsideRecursion(nextStepIndex, stepNames);
        } else {
            executeStep(nextStep.getName(), stepNames);
        }
    }

    private void executeNextStepOutsideRecursion(int nextStepIndex, List<String> stepNames) {
        for (int i = nextStepIndex; i < stepNames.size(); i++) {
            DslStep nextStep = steps.get(stepNames.get(i));
            if (!Objects.equals(recursions.get(nextStep.getName()), getStepMaxRecursions(nextStep))) {
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

    private void clearReturnValues() {
        setReturnValue(null);
        setReturnStatus(null);
        setReturnHeaders(new HashMap<>());
        setReturnWithWrapper(true);
    }

    private void addGlobalIncomingHeadersToRequestHeaders() {
        Map<String, Object> evaluatedHeaders = scriptingHelper.evaluateScripts(properties.getIncomingRequests().getHeaders(), context, requestBody, requestQuery, requestHeaders);
        requestHeaders.putAll(mappingHelper.convertMapObjectValuesToString(evaluatedHeaders));
    }
}
