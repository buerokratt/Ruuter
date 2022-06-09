package ee.buerokratt.ruuter.service.exception;

import static java.lang.String.*;

public class InvalidHttpRequestException extends IllegalArgumentException {
    public InvalidHttpRequestException(String step, Throwable err) {
        super(format("Invalid http request in step: %s", step), err);
    }
}
