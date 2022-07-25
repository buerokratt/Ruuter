package ee.buerokratt.ruuter.helper.exception;

import static java.lang.String.*;

public class InvalidConfigurationException extends IllegalArgumentException {
    public InvalidConfigurationException(String configuration, String message, Throwable err) {
        super(format("Encountered error, when loading DSL: %s. %s", configuration, message), err);
    }
}
