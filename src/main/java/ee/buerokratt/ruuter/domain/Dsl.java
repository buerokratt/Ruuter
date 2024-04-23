package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.domain.steps.DeclarationStep;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.helper.MappingHelper;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Data
public class Dsl {
    private final Map<String, DslStep> steps;

    private DeclarationStep declaration;

    public Dsl(Map<String, DslStep> steps) {
        this.steps = steps;
        steps.entrySet().stream()
            .filter(step -> "declare".equals(step.getKey())).findFirst()
            .ifPresent(step -> this.declaration = (DeclarationStep) step.getValue());
    }

    public Map<String, DslStep> steps() {
        return steps;
    }

    public DslStep step(String stepName) {
        return steps.get(stepName);
    }
}
