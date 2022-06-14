package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class ConfigurationInstance {
    private final ScriptingHelper scriptingHelper;
    private final Map<String, ConfigurationStep> steps;
    private final Map<String, String> requestBody;
    private final Map<String, String> requestParams;
    private final HashMap<String, Object> context = new HashMap<>();
    private Object returnValue;

    public void execute() {
        steps.forEach((s, step) -> step.execute(this));
    }
}
