package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AssignStep<T> extends ConfigurationStep {
    private HashMap<String, T> assign;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        assign.forEach((k, v) -> ci.getContext().put(k, ci.getScriptingHelper().evaluateScripts(v, ci.getContext(), ci.getRequestBody(), ci.getRequestParams())));
    }

    @Override
    public String getType() {
        return "assign";
    }
}
