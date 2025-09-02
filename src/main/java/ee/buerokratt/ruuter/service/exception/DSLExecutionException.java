package ee.buerokratt.ruuter.service.exception;

import ee.buerokratt.ruuter.helper.exception.ScriptEvaluationException;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Slf4j
@Getter
public class DSLExecutionException extends Exception {

    public static final String ERR_UNKNOWN = "E_unknown";
    public static final String ERR_VALUE_NULL = "E_null";
    private static final String ERR_VALUE_SCRIPTING = "E_script";
    private static final String ERR_NETWORK = "E_network";

    String dslName;
    String stepName;
    String causeCode;
    String message;

    public DSLExecutionException(String stepName, String dslName, Throwable err) {
        this.stepName = stepName;
        this.dslName = dslName;

        this.message = err.getMessage();
        if (err instanceof NullPointerException) {
            this.causeCode = ERR_VALUE_NULL;
        } if (err instanceof ScriptEvaluationException){
            this.causeCode = ERR_VALUE_SCRIPTING;
        } if (err instanceof WebClientRequestException) {
            this.causeCode = ERR_NETWORK;
        } else
            this.causeCode = ERR_UNKNOWN;
    }

    public ErrorObject getErrorObject() {
        return new ErrorObject(this.dslName, this.stepName, this.causeCode, this.message);
    }

    @Value
    public static class ErrorObject {
        String dslName;
        String stepName;
        String causeCode;
        String message;
    }
}
