package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
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
    public void execute(ConfigurationInstance configurationInstance) {
        assign.forEach((k, v) -> {
            ScriptingHelper scriptingHelper = configurationInstance.getScriptingHelper();
            if (v instanceof String && scriptingHelper.containsScript(v.toString())) {
                configurationInstance.getContext().put(k, scriptingHelper.evaluateScripts(v.toString(), configurationInstance.getContext()));
            } else {
                configurationInstance.getContext().put(k, v);
            }
        });
        super.execute(configurationInstance);
    }
}
