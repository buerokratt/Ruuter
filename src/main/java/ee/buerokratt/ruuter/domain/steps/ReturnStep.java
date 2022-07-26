package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
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
public class ReturnStep extends DslStep {
    @JsonAlias({"return"})
    private String returnValue;
    private Map<String, Object> headers = new LinkedHashMap<>();
    private Integer status;

    @Override
    protected void executeStepAction(DslInstance di) {
        di.setReturnHeaders(formatHeaders(di));
        di.setReturnStatus(status);
        di.setReturnValue(di.getScriptingHelper().evaluateScripts(returnValue, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders()));
    }

    @Override
    public String getType() {
        return "return";
    }

    private Map<String, String> formatHeaders(DslInstance di) {
        Map<String, Object> evaluatedMap = di.getScriptingHelper().evaluateScripts(headers, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
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
