package ee.buerokratt.ruuter.helper.exception;

public class SsrfGuardException extends RuntimeException {
    public SsrfGuardException(String message) {
        super(message);
    }

    public SsrfGuardException(String message, Throwable cause) {
        super(message, cause);
    }
}
