package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AssignStep<T> extends ConfigurationStep {
    private Map<String, T> assign;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        assign.forEach((k, v) -> ci.getContext().put(k, ci.getScriptingHelper().evaluateScripts(v, ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders())));
    }

    @Override
    public String getType() {
        return "assign";
    }
}
