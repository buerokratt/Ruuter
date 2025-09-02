package ee.buerokratt.ruuter.service.exception;

import lombok.Getter;

@Getter
public class StepExecutionException extends Exception {
    String stepName;
    String message;
    public StepExecutionException(String stepName, Throwable err) {
        super("Error executing: %s".formatted(stepName), err);
        this.stepName = stepName;
        this.message = err.getMessage();
    }
}
