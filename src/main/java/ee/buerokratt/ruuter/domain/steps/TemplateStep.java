package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
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
public class TemplateStep extends ConfigurationStep {
    @JsonAlias({"template"})
    private String templateToCall;
    @JsonAlias({"result"})
    private String resultName;
    private String requestType;
    private Map<String, Object> body;
    private Map<String, Object> query;
    private Map<String, Object> headers = new HashMap<>();

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        Map<String, Object> evaluatedBody = ci.getScriptingHelper().evaluateScripts(body, ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, Object> evaluatedQuery = ci.getScriptingHelper().evaluateScripts(query, ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = ci.getScriptingHelper().evaluateScripts(headers, ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, String> mappedHeaders = ci.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);
        ConfigurationInstance templateInstance = ci.getConfigurationService().execute(templateToCall, requestType, evaluatedBody, evaluatedQuery, mappedHeaders, ci.getRequestOrigin());
        ci.getContext().put(resultName, templateInstance.getReturnValue());
    }

    @Override
    public String getType() {
        return "template";
    }
}
