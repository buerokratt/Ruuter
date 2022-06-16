package ee.buerokratt.ruuter.helper.exception;

import static java.lang.String.format;

public class InvalidConfigurationStepException extends IllegalArgumentException {
    public InvalidConfigurationStepException(String step, Throwable err) {
        super(format("Unable to load invalid step: %s", step), err);
    }
}
