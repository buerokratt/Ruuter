package ee.buerokratt.ruuter.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ConfigurationInstance {
    private Map<String, ConfigurationStep> steps;
    private Map<String, String> requestBody;
    private Map<String, String> requestParams;
    private HashMap<String, Object> context;
    private Object returnValue;

    public ConfigurationInstance(Map<String, ConfigurationStep> steps, Map<String, String> requestBody, Map<String, String> requestParams) {
        this.steps = steps;
        this.requestBody = requestBody;
        this.requestParams = requestParams;
        this.context = new HashMap<>();
    }

    public void execute() {
        steps.forEach((s, step) -> step.execute(this));
    }
}
