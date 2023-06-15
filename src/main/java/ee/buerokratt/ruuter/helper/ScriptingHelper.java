package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.helper.exception.ScriptEvaluationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.*;

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

    public Map<String, Object> evaluateScripts(Map<String, Object> map, Map<String, Object> context, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders) {
        return map == null || map.isEmpty() ? map : map.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, objectEntry -> evaluateScripts(objectEntry.getValue(), context, requestBody, requestQuery, requestHeaders), (x, y) -> y, LinkedHashMap::new));
    }

    public Object evaluateScripts(Object toEval, Map<String, Object> context, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders) {
        if (toEval == null || !containsScript(toEval.toString())) {
            return toEval;
        }

        Map<String, Object> evalContext = setupEvalContext(context, requestBody, requestQuery, requestHeaders);
        Bindings bindings = createBindingsWithContext(evalContext);

        Object ret = replaceVariables(toEval, bindings, evalContext);
        return ret;
    }

    private Object replaceVariables(Object toEval, Bindings bindings, Map<String, Object> evalContext ) {
        if (toEval instanceof String) {
            String eval = (String) toEval;

            // Full variable match, assigned value can be any
            if (eval.matches(SCRIPT_REGEX)) {
                setupObjectsInScript(eval, bindings, evalContext);
                return filterEmptyOptional(bindings, eval.substring(2, eval.length() - 1));
            }

            // Partial variable match, assigned value can only be String
            List<MatchResult> founded = Pattern.compile(SCRIPT_REGEX).matcher(eval).results().collect(toList());
            for (MatchResult f : founded) {
                String found = f.group();
                setupObjectsInScript(found.substring(2, found.length() - 1), bindings, evalContext);
                eval = eval.replace(found, filterEmptyOptional(bindings, found.substring(2, found.length() - 1)).toString());
            };

            return eval;
        }
        if (toEval instanceof List)
            return ((List)toEval).stream().map( obj -> replaceVariables(obj, bindings, evalContext)).collect(toList());
        if (toEval instanceof Map)
            return ((HashMap<String, Object>)toEval).entrySet().stream()
                .collect(toMap(Map.Entry::getKey,entry -> replaceVariables(entry.getValue(), bindings,evalContext)));
        return null;
    }

    private Object filterEmptyOptional(Bindings bindings, String evaluableScript) {
        Object foundObject = evaluate(bindings, evaluableScript);
        boolean isOptional = evaluableScript.contains(".optional.") || evaluableScript.contains(".optional_");

        if (Objects.isNull(foundObject)) {
            return isOptional ? "" : null;
        }

        return foundObject;
    }

    private Map<String, Object> setupEvalContext(Map<String, Object> context, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders) {
        Map<String, Object> incoming = new HashMap<>();
        if (requestQuery != null) {
            incoming.put("params", new HashMap<>(requestQuery));
        }
        if (requestBody != null) {
            incoming.put("body", new HashMap<>(requestBody));
        }
        if (requestHeaders != null) {
            incoming.put("headers", new HashMap<>(requestHeaders));
        }
        HashMap<String, Object> evalContext = new HashMap<>(context);
        evalContext.put("incoming", incoming);
        return evalContext;
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
        String objectName = possibleObject.substring(0, possibleObject.indexOf('.'));
        bindings.put(objectName, mappingHelper.convertObjectToString(evalContext.get(objectName)));
        return "JSON.parse(" + possibleObject.replaceFirst("\\.", ").");
    }

    private Object evaluate(Bindings bindings, String evaluableString) {
        try {
            return engine.eval(evaluableString, bindings);
        } catch (Exception e) {
            throw new ScriptEvaluationException(evaluableString, e);
        }
    }

    private String removeScriptWrapper(String s) {
        return s.substring(2, s.length() - 1);
    }
}
