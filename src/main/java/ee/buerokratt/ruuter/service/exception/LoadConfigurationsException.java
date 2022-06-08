package ee.buerokratt.ruuter.service.exception;

public class LoadConfigurationsException extends IllegalArgumentException {
    public LoadConfigurationsException(Throwable err) {
        super("Unable to load configurations", err);
    }
}
