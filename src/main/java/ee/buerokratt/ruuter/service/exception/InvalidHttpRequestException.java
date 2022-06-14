package ee.buerokratt.ruuter.service.exception;

public class InvalidHttpRequestException extends IllegalArgumentException {
    public InvalidHttpRequestException(Throwable err) {
        super("Invalid http request", err);
    }
}
