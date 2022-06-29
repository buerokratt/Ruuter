package ee.buerokratt.ruuter.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ScriptingHelper {
    public static final String OBJECT_REGEX = "([a-zA-Z0-9_. \"]+\\.[a-zA-Z0-9_. \"]+)";
    public static final String SCRIPT_REGEX = "(\\$\\{[^}]+})";
    private final MappingHelper mappingHelper;

    private final ScriptEngine engine;

    public boolean containsScript(String s) {
        return Pattern.compile(SCRIPT_REGEX, Pattern.MULTILINE).matcher(s).find();
    }

    public Map<String, Object> setupEvalContext(Map<String, Object> context, Map<String, String> requestBody, Map<String, String> requestParams) {
        Map<String, Object> incoming = new HashMap<>();
        if (requestParams != null) {
            incoming.put("params", new HashMap<>(requestParams));
        }
        if (requestBody != null) {
            incoming.put("body", new HashMap<>(requestBody));
        }
        HashMap<String, Object> evalContext = new HashMap<>(context);
        evalContext.put("incoming", incoming);
        return evalContext;
    }

    public Object evaluateScripts(String toEval, Map<String, Object> evalContext) {
        Bindings bindings = createBindingsWithContext(evalContext);

        List<String> nonScriptSlices = Arrays.stream(toEval.split(SCRIPT_REGEX)).toList();
        List<Object> evaluatedScripts = Pattern.compile(SCRIPT_REGEX, Pattern.MULTILINE).matcher(toEval).results()
            .map(matchResult -> matchResult.group(0))
            .map(scriptToExecute -> setupObjectsInScript(removeScriptWrapper(scriptToExecute), bindings, evalContext))
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
        Bindings bindings = engine.createBindings();
        bindings.putAll(evalContext);
        return bindings;
    }

    private String setupObjectsInScript(String scriptToExecute, Bindings bindings, Map<String, Object> evalContext) {
        List<String> scriptSlices = Arrays.stream(scriptToExecute.split(OBJECT_REGEX)).toList();
        List<String> parsedObjects = new LinkedList<>(Pattern.compile(OBJECT_REGEX, Pattern.MULTILINE).matcher(scriptToExecute).results()
            .map(objectMatch -> objectMatch.group(0).trim())
            .map(possibleObject -> mapParseableValueToScriptAndBindings(bindings, evalContext, possibleObject))
            .toList());
        if (scriptSlices.isEmpty()) {
            return parsedObjects.get(0);
        }
        return scriptSlices.stream()
            .map(scriptSlice -> parsedObjects.isEmpty() ? scriptSlice : scriptSlice + parsedObjects.remove(0))
            .reduce("", (s, s2) -> s + s2);
    }

    private String mapParseableValueToScriptAndBindings(Bindings bindings, Map<String, Object> evalContext, String possibleObject) {
        if (possibleObject.contains("\"")) {
            return possibleObject;
        }
        String objectName = possibleObject.substring(0, possibleObject.indexOf('.', 0));
        bindings.put(objectName, mappingHelper.convertObjectToString(evalContext.get(objectName)));
        return "JSON.parse(" + possibleObject.replaceFirst("\\.", ").");
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
}
