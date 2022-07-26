package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AssignStep<T> extends DslStep {
    private Map<String, T> assign;

    @Override
    protected void executeStepAction(DslInstance di) {
        assign.forEach((k, v) -> di.getContext().put(k, di.getScriptingHelper().evaluateScripts(v, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders())));
    }

    @Override
    public String getType() {
        return "assign";
    }
}
