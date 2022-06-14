package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ReturnStep extends ConfigurationStep {
    @JsonAlias({"return"})
    private String returnValue;

    @Override
    public void execute(ConfigurationInstance configurationInstance) {
        ScriptingHelper scriptingHelper = configurationInstance.getScriptingHelper();

        if (Boolean.TRUE.equals(scriptingHelper.containsScript(returnValue))) {
            Object evaluatedValue = scriptingHelper.evaluateScripts(returnValue, configurationInstance.getContext());
            configurationInstance.setReturnValue(evaluatedValue);
        } else {
            configurationInstance.setReturnValue(returnValue);
        }

        super.execute(configurationInstance);
    }
}
