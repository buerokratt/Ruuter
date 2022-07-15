package ee.buerokratt.ruuter.helper.exception;

public class LoadDslsException extends IllegalArgumentException {
    public LoadDslsException(Throwable err) {
        super("Unable to load dsls", err);
    }
}
