package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
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
    private Map<String, Object> query;
    private Map<String, Object> headers;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        Map<String, Map<String, Object>> evaluatedParameters = ci.getScriptingHelper().evaluateRequestParameters(ci, body, query, headers);
        ConfigurationInstance templateInstance = ci.getConfigurationService().execute(templateToCall, requestType, evaluatedParameters.get("body"), evaluatedParameters.get("query"), ci.getMappingHelper().convertMapObjectValuesToString(evaluatedParameters.get("headers")), ci.getRequestOrigin());
        ci.getContext().put(resultName, templateInstance.getReturnValue());
    }

    @Override
    public String getType() {
        return "template";
    }
}
