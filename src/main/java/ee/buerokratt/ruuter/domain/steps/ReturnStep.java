package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
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
    @JsonAlias({"wrapper"})
    private boolean withWrapper = true;
    @JsonAlias({"return"})
    private String returnValue;
    private Map<String, Object> headers = new LinkedHashMap<>();
    private Integer status;

    @Override
    protected void executeStepAction(DslInstance di) {
        di.setReturnHeaders(formatHeaders(di));
        di.setReturnStatus(status);
        di.setReturnWithWrapper(withWrapper);
        di.setReturnValue(di.getScriptingHelper().evaluateScripts(returnValue, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders()));
        if (status != 200) {
            di.setErrorStatus(HttpStatus.valueOf(status));
            di.setErrorMessage(returnValue);
        }
    }

    @Override
    public String getType() {
        return "return";
    }

    private Map<String, String> formatHeaders(DslInstance di) {
        Map<String, Object> evaluatedMap = di.getScriptingHelper().evaluateScripts(headers, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        return evaluatedMap.entrySet().stream()
            .map(e -> addDefaultCookies(e, di))
            .collect(toMap(Entry::getKey, this::entryValueToHeaderString));
    }

    private void addToCookie(LinkedHashMap<String, Object> cookie, String key, Object value) {
        if (!cookie.containsKey(key))
            cookie.put(key, value);
    }
    private Map.Entry<String, Object> addDefaultCookies(Map.Entry<String, Object> entry, DslInstance di) {
        if ("Set-Cookie".equals(entry.getKey())) {
            LinkedHashMap<String, Object> cookie = new LinkedHashMap<>((HashMap<String, Object>) entry.getValue());

            addToCookie(cookie, "Path", "/");
            addToCookie(cookie, "HttpOnly", true);
            addToCookie(cookie, "Secure", true);
            addToCookie(cookie, "Max-Age", 28800);

            entry.setValue(cookie);
        }
        return entry;
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
