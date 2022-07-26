package ee.buerokratt.ruuter.helper.exception;

import static java.lang.String.format;

public class InvalidDslStepException extends IllegalArgumentException {
    public InvalidDslStepException(String step, String message, Throwable err) {
        super(format("Unable to load invalid step: %s. Error message: %s", step, message), err);
    }
}
