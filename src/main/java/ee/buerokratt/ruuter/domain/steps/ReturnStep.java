package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ReturnStep extends ConfigurationStep {
    @JsonAlias({"return"})
    private String returnValue;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        ScriptingHelper scriptingHelper = ci.getScriptingHelper();

        if (Boolean.TRUE.equals(scriptingHelper.containsScript(returnValue))) {
            Map<String, Object> evalContext = scriptingHelper.setupEvalContext(ci.getContext(), ci.getRequestBody(), ci.getRequestParams());
            Object evaluatedValue = scriptingHelper.evaluateScripts(returnValue, evalContext);
            ci.setReturnValue(evaluatedValue);
        } else {
            ci.setReturnValue(returnValue);
        }
    }

    @Override
    public String getType() {
        return "return";
    }
}
