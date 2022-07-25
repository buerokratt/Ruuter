package ee.buerokratt.ruuter.domain.steps.conditional;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class SwitchStep extends ConfigurationStep {
    @JsonAlias({"switch"})
    private List<Condition> conditions;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        ScriptingHelper scriptingHelper = ci.getScriptingHelper();
        Optional<Condition> correctStatement = conditions.stream()
            .filter(condition -> Boolean.TRUE.equals(scriptingHelper.evaluateScripts(condition.getConditionStatement(), ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders())))
            .findFirst();
        correctStatement.ifPresent(condition -> this.setNextStepName(condition.getNextStepName()));
    }

    @Override
    public String getType() {
        return "switch";
    }
}
