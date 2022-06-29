package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AssignStep<T> extends ConfigurationStep {
    private HashMap<String, T> assign;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        assign.forEach((k, v) -> {
            ScriptingHelper scriptingHelper = ci.getScriptingHelper();
            if (v instanceof String && scriptingHelper.containsScript(v.toString())) {
                Map<String, Object> evalContext = scriptingHelper.setupEvalContext(ci.getContext(), ci.getRequestBody(), ci.getRequestParams());
                ci.getContext().put(k, scriptingHelper.evaluateScripts(v.toString(), evalContext));
            } else {
                ci.getContext().put(k, v);
            }
        });
    }

    @Override
    public String getType() {
        return "assign";
    }
}
