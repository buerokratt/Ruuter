package ee.buerokratt.ruuter.helper.exception;

import static java.lang.String.format;

public class InvalidHttpMethodTypeException extends IllegalArgumentException {
    public InvalidHttpMethodTypeException(String methodType) { super(format("Incorrect method type: %s", methodType)); }
}
