package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
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
public class TemplateStep extends DslStep {
    @JsonAlias({"template"})
    private String templateToCall;
    @JsonAlias({"result"})
    private String resultName;
    private String requestType;
    private Map<String, Object> body = new HashMap<>();
    private Map<String, Object> query = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();

    @Override
    protected void executeStepAction(DslInstance di) {
        ScriptingHelper scriptingHelper = di.getScriptingHelper();
        Map<String, Object> evaluatedBody = scriptingHelper.evaluateScripts(body, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedQuery = scriptingHelper.evaluateScripts(query, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = scriptingHelper.evaluateScripts(headers, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, String> mappedHeaders = di.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);
        DslInstance templateInstance = di.getDslService().execute(templateToCall, requestType, evaluatedBody, evaluatedQuery, mappedHeaders, di.getRequestOrigin());
        di.getContext().put(resultName, templateInstance.getReturnValue());
    }

    @Override
    public String getType() {
        return "template";
    }
}
