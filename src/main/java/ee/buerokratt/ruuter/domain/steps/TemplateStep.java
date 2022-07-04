package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class TemplateStep extends ConfigurationStep {
    @JsonAlias({"template"})
    private String templateToCall;
    @JsonAlias({"result"})
    private String resultName;
    private Map<String, Object> body;
    private Map<String, Object> params;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        ScriptingHelper scriptingHelper = ci.getScriptingHelper();
        Map<String, Object> templateBody = body != null ? body.entrySet().stream().collect(toEvaluatedMap(scriptingHelper, ci)) : null;
        Map<String, Object> templateParams = params != null ? params.entrySet().stream().collect(toEvaluatedMap(scriptingHelper, ci)) : new HashMap<>();

        ci.getContext().put(resultName,  ci.getConfigurationService().execute(templateToCall, templateBody, templateParams, ci.getRequestOrigin()));
    }

    private Collector<Map.Entry<String, Object>, ?, Map<String, Object>> toEvaluatedMap(ScriptingHelper scriptingHelper, ConfigurationInstance ci) {
        return toMap(Map.Entry::getKey, entry -> {
                if (scriptingHelper.containsScript(entry.getValue().toString())) {
                    Map<String, Object> evalContext = scriptingHelper.setupEvalContext(ci.getContext(), ci.getRequestBody(), ci.getRequestParams());
                    return scriptingHelper.evaluateScripts(entry.getValue().toString(), evalContext);
                }
                return entry.getValue();
            }
        );
    }

    @Override
    public String getType() {
        return "template";
    }
}
