package ee.buerokratt.ruuter.helper.exception;

import static java.lang.String.*;

public class InvalidDslException extends IllegalArgumentException {
    public InvalidDslException(String dsl, Throwable err) {
        super(format("Encountered error, when loading dsl: %s", dsl), err);
    }
}
