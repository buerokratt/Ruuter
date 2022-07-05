package ee.buerokratt.ruuter.helper.exception;

public class ScriptEvaluationException extends RuntimeException {
    public ScriptEvaluationException(String script, Throwable err) {
        super("Error executing script: %s".formatted(script), err);
    }
}
