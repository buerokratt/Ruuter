package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ReturnStep extends ConfigurationStep {
    @JsonAlias({"return"})
    private String returnValue;
    private Map<String, Object> headers = new LinkedHashMap<>();
    private Integer status;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        ci.setReturnHeaders(formatHeaders(ci));
        ci.setReturnStatus(status);
        ci.setReturnValue(ci.getScriptingHelper().evaluateScripts(returnValue, ci.getContext(), ci.getRequestBody(), ci.getRequestParams()));
    }

    @Override
    public String getType() {
        return "return";
    }

    private Map<String, String> formatHeaders(ConfigurationInstance ci) {
        Map<String, Object> evaluatedMap = ci.getScriptingHelper().evaluateScripts(headers, ci.getContext(), ci.getRequestBody(), ci.getRequestParams());
        return evaluatedMap.entrySet().stream().collect(toMap(Entry::getKey, this::entryValueToHeaderString));
    }

    private String entryValueToHeaderString(Entry<String, Object> entry) {
        if (entry.getValue() instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                .map(this::entryToKeyValueString)
                .collect(joining());
        }
        return entry.getValue().toString();
    }

    private String entryToKeyValueString(Entry<?, ?> innerEntry) {
        if (innerEntry.getValue() instanceof Boolean bool) {
            return Boolean.TRUE.equals(bool) ? "%s; ".formatted(innerEntry.getKey()) : "";
        }
        return "%s=%s; ".formatted(innerEntry.getKey(), innerEntry.getValue());
    }
}
