package ee.buerokratt.ruuter.service.exception;

public class InvalidIncomingRequestMethodType extends IllegalArgumentException {
    public InvalidIncomingRequestMethodType(String methodType) { super("Method type %s is not allowed".formatted(methodType)); }
}
