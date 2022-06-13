package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ScriptingHelper {
    public static final String OBJECT_REGEX = "([a-zA-Z0-9_. \"]+\\.[a-zA-Z0-9_. \"]+)";
    public static final String SCRIPT_REGEX = "(\\$\\{[^}]+})";

    private final ScriptEngine engine;
    private final ObjectMapper mapper;

    public boolean containsScript(String s) {
        return Pattern.compile(SCRIPT_REGEX, Pattern.MULTILINE).matcher(s).find();
    }

    public Object evaluateScripts(String toEval, Map<String, Object> evalContext) {
        Bindings bindings = createBindingsWithContext(evalContext);

        List<String> nonScriptSlices = Arrays.stream(toEval.split(SCRIPT_REGEX)).toList();
        List<Object> evaluatedScripts = Pattern.compile(SCRIPT_REGEX, Pattern.MULTILINE).matcher(toEval).results()
            .map(matchResult -> matchResult.group(0))
            .map(scriptToExecute -> wrapObjectsInScriptWithJsonParse(removeScriptWrapper(scriptToExecute)))
            .map(evaluableScript -> evaluate(bindings, evaluableScript))
            .collect(toList());

        if (nonScriptSlices.isEmpty()) {
            return evaluatedScripts.size() == 1 ? evaluatedScripts.get(0) : evaluatedScripts.stream().reduce("", (o, o2) -> o + o2.toString());
        }
        return nonScriptSlices.stream()
            .map(nonScriptSlice -> evaluatedScripts.isEmpty() ? nonScriptSlice : nonScriptSlice + evaluatedScripts.remove(0))
            .reduce("", (s, s2) -> s + s2);
    }

    private Bindings createBindingsWithContext(Map<String, Object> evalContext) {
        Bindings bindings;
        bindings = engine.createBindings();
        evalContext.forEach((key, value) -> {
            if (isKnownType(value)) {
                bindings.put(key, value);
            } else {
                bindings.put(key, convertObjectToJson(value));
            }
        });
        return bindings;
    }

    private String wrapObjectsInScriptWithJsonParse(String scriptToExecute) {
        List<String> scriptSlices = Arrays.stream(scriptToExecute.split(OBJECT_REGEX)).toList();
        List<String> parsedObjects = Pattern.compile(OBJECT_REGEX, Pattern.MULTILINE).matcher(scriptToExecute).results()
            .map(objectMatch -> objectMatch.group(0).trim())
            .map(possibleObject -> possibleObject.contains("\"") ? possibleObject : "JSON.parse(" + possibleObject.replaceFirst("\\.", ")."))
            .toList();
        if (scriptSlices.isEmpty()) {
            return parsedObjects.get(0);
        }
        return scriptSlices.stream()
            .map(scriptSlice -> parsedObjects.isEmpty() ? scriptSlice : scriptSlice + parsedObjects.remove(0))
            .reduce("", (s, s2) -> s + s2);
    }

    private Object evaluate(Bindings bindings, String evaluableString) {
        try {
            return engine.eval(evaluableString, bindings);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private String removeScriptWrapper(String s) {
        return s.substring(2, s.length() - 1);
    }

    private boolean isKnownType(Object o) {
        return o instanceof String || o instanceof Integer || o instanceof Boolean || o instanceof Long ||
            o instanceof Double || o instanceof Short || o instanceof Float || o instanceof Character;
    }

    private String convertObjectToJson(Object o) {
        try {
            return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

}
