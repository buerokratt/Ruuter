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
public class TemplateStep extends ConfigurationStep {
    @JsonAlias({"template"})
    private String templateToCall;
    @JsonAlias({"result"})
    private String resultName;
    private String requestType;
    private Map<String, Object> body;
    private Map<String, Object> params;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        ScriptingHelper scriptingHelper = ci.getScriptingHelper();
        Map<String, Object> templateBody = scriptingHelper.evaluateScripts(body, ci.getContext(), ci.getRequestBody(), ci.getRequestParams());
        Map<String, Object> templateParams = scriptingHelper.evaluateScripts(params, ci.getContext(), ci.getRequestBody(), ci.getRequestParams());

        ConfigurationInstance templateInstance = ci.getConfigurationService().execute(templateToCall, requestType, templateBody, templateParams, ci.getRequestOrigin());
        ci.getContext().put(resultName, templateInstance.getReturnValue());
    }

    @Override
    public String getType() {
        return "template";
    }
}
