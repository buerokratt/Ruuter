package ee.buerokratt.ruuter.service.exception;

import static java.lang.String.format;

public class InvalidConfigurationDirectoryException extends IllegalArgumentException {
    public InvalidConfigurationDirectoryException(String path, Throwable err) {
        super(format("Failed to resolve directory: %s", path), err);
    }
}
