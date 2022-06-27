package ee.buerokratt.ruuter.service.exception;

public class StepExecutionException extends IllegalArgumentException {
    public StepExecutionException(String stepName, Throwable err) {
        super("Error executing: %s".formatted(stepName), err);
    }
}
