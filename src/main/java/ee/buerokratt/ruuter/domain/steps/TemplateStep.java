package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
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
public class TemplateStep extends DslStep {
    @JsonAlias({"template"})
    private String templateToCall;
    @JsonAlias({"result"})
    private String resultName;
    private String requestType;
    private Map<String, Object> body;
    private Map<String, Object> params;

    @Override
    protected void executeStepAction(DslInstance di) {
        ScriptingHelper scriptingHelper = di.getScriptingHelper();
        Map<String, Object> templateBody = scriptingHelper.evaluateScripts(body, di.getContext(), di.getRequestBody(), di.getRequestParams());
        Map<String, Object> templateParams = scriptingHelper.evaluateScripts(params, di.getContext(), di.getRequestBody(), di.getRequestParams());

        DslInstance templateInstance = di.getDslService().execute(templateToCall, requestType, templateBody, templateParams, di.getRequestOrigin());
        di.getContext().put(resultName, templateInstance.getReturnValue());
    }

    @Override
    public String getType() {
        return "template";
    }
}
