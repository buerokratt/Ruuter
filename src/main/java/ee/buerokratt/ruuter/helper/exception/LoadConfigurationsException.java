package ee.buerokratt.ruuter.helper.exception;

public class LoadConfigurationsException extends IllegalArgumentException {
    public LoadConfigurationsException(Throwable err) {
        super("Unable to load configurations", err);
    }
}
