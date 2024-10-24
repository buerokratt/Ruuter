package ee.buerokratt.ruuter.domain.steps.conditional;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.steps.DslStep;
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
public class SwitchStep extends DslStep {
    @JsonAlias({"switch"})
    private List<Condition> conditions;

    @JsonAlias({"next"})
    private String elseNextStepName;

    @Override
    protected void executeStepAction(DslInstance di) {
/*
        Optional<Condition> correctStatement =
            conditions.stream()
                .filter(condition -> condition.getConditionStatement().equals(di.getRequestQuery().get("metric")))
    .findFirst();

        System.out.println("METRIC: " + di.getRequestQuery().get("metric"));
*/
        ScriptingHelper scriptingHelper = di.getScriptingHelper();
        Optional<Condition> correctStatement = conditions.stream()
            .filter(condition -> Boolean.TRUE.equals(scriptingHelper.evaluateScripts(condition.getConditionStatement(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders())))
            .findFirst();

        correctStatement.ifPresentOrElse(condition -> this.setNextStepName(condition.getNextStepName()), () -> this.setNextStepName(elseNextStepName));
    }

    @Override
    public String getType() {
        return "switch";
    }
}
