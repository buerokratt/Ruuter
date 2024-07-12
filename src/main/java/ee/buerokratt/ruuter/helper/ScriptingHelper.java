package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.helper.exception.ScriptEvaluationException;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class ScriptingHelper {
    public static final String OBJECT_REGEX = "([a-zA-Z0-9_. \"]+\\.[a-zA-Z0-9_. \"]+)";
    public static final String SCRIPT_REGEX = "(\\$\\{[^}]+})";
    public static final String SCRIPT_LINE_REGEX = "(\\$=.+=$)";

    private final MappingHelper mappingHelper;
    private final ScriptEngine engine;

    private Pattern scriptPattern = Pattern.compile(SCRIPT_REGEX);
    private Pattern linePattern = Pattern.compile(SCRIPT_LINE_REGEX);

    public boolean containsScript(String s) {
        return scriptPattern.matcher(s).find() ||
            linePattern.matcher(s).find();
    }

    public Map<String, Object> evaluateScripts(Map<String, Object> map, DslInstance di) {
        return evaluateScripts(map, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
    }

    public Map<String, Object> evaluateScripts(Map<String, Object> map, Map<String, Object> context, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders) {
        return map == null || map.isEmpty() ? map : map.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, objectEntry -> evaluateScripts(objectEntry.getValue(), context, requestBody, requestQuery, requestHeaders), (x, y) -> y, LinkedHashMap::new));
    }

    public Object evaluateScripts(Object toEval, DslInstance di) {
        return evaluateScripts(toEval, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
    }

    public Object evaluateScripts(Object toEval, Map<String, Object> context, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders) {
        return replaceVariables(toEval, context, requestBody, requestQuery, requestHeaders);
    }

    private Object replaceVariables(Object toEval, Map<String, Object> context, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders ) {
        // Null evaluation: value does not contain any scriptable element or is empty
        if (toEval == null || !containsScript(toEval.toString())) {
            return toEval;
        }

        // Simple evaluation: value is one variable only
        if (toEval.toString().matches(SCRIPT_REGEX)) {
            return evaluateSimple(toEval, context, requestBody, requestQuery, requestHeaders, scriptPattern);
        }

        if (toEval.toString().matches(SCRIPT_LINE_REGEX)) {
            return evaluateSimple(toEval, context, requestBody, requestQuery, requestHeaders, linePattern);
        }


        // Recursive evaluation: value is of complex type (List/Map)
        if (toEval instanceof List)
            return ((List)toEval).stream().map( obj -> replaceVariables(obj, context, requestBody, requestQuery, requestHeaders)).collect(toList());
        if (toEval instanceof Map)
            return ((HashMap<String, Object>)toEval).entrySet().stream()
                .collect(toMap(Map.Entry::getKey,entry -> replaceVariables(entry.getValue(), context, requestBody, requestQuery, requestHeaders)));

        // Complex evaluation: value is a string formula
            return evaluateComplex(toEval, context, requestBody, requestQuery, requestHeaders, SCRIPT_REGEX, scriptPattern);
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
        if (evalContext.get(objectName) != null) {
            String value = mappingHelper.convertObjectToString(evalContext.get(objectName));
            bindings.put(objectName, value);
        }

        if (bindings.keySet().contains(objectName)) {
            return "JSON.parse(" + possibleObject.replaceFirst("\\.", ").");
        }
        return possibleObject;
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

    public Object evaluateSimple(Object toEval, Map<String, Object> context,
                                 Map<String, Object> requestBody,
                                 Map<String, Object> requestQuery,
                                 Map<String, String> requestHeaders,
                                 Pattern pattern) {
        List<Object> evaluatedScripts = getEvaluatedScripts(toEval, context, requestBody, requestQuery, requestHeaders, pattern);
        return evaluatedScripts.size() == 1 ? evaluatedScripts.get(0) : evaluatedScripts.stream().reduce("", (o, o2) -> o + o2.toString());
    }

    public Object evaluateComplex(Object toEval, Map<String, Object> context,
                                  Map<String, Object> requestBody,
                                  Map<String, Object> requestQuery,
                                  Map<String, String> requestHeaders,
                                  String script, Pattern pattern) {
        List<Object> evaluatedScripts = getEvaluatedScripts(toEval, context, requestBody, requestQuery, requestHeaders, pattern);
        List<String> nonScriptSlices = Arrays.stream(toEval.toString().split(script)).toList();

        if (nonScriptSlices.isEmpty()) {
            return evaluatedScripts.size() == 1 ? evaluatedScripts.get(0) : evaluatedScripts.stream().reduce("", (o, o2) -> o + o2.toString());
        }
        return nonScriptSlices.stream()
            .map(nonScriptSlice -> evaluatedScripts.isEmpty() ? nonScriptSlice : nonScriptSlice + evaluatedScripts.remove(0))
            .reduce("", (s, s2) -> s + s2);
    }

    private List<Object> getEvaluatedScripts(Object toEval,
                                             Map<String, Object> context,
                                             Map<String, Object> requestBody,
                                             Map<String, Object> requestQuery,
                                             Map<String, String> requestHeaders,
                                             Pattern pattern) {
        Map<String, Object> evalContext = setupEvalContext(context, requestBody, requestQuery, requestHeaders);
        Bindings bindings = createBindingsWithContext(evalContext);

        List<Object> evaluatedScripts = pattern.matcher(toEval.toString()).results()
            .map(matchResult -> matchResult.group(0))
            .map(scriptToExecute -> setupObjectsInScript(removeScriptWrapper(scriptToExecute), bindings, evalContext))
            .map(evaluableScript -> filterEmptyOptional(bindings, evaluableScript))
            .collect(toList());
        return evaluatedScripts;
    }

}
