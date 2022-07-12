package ee.buerokratt.ruuter.util;

import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class LoggingUtils {
    public static final String INCOMING_REQUEST = "incoming.request";
    public static final String STEP_TYPE = "stepType";
    public static final String REQUEST_AUTHOR_IP = "requestAuthorIp";
    public static final String REQUEST_TO = "requestTo";
    public static final String REQUEST_CONTENT = "requestContent";
    public static final String RESPONSE_CONTENT = "responseContent";
    public static final String RESPONSE_CODE = "responseCode";
    public static final String RESPONSE_IN = "responseInMs";
    public static final String INCOMING_RESPONSE = "incoming.response";

    private LoggingUtils() {
    }

    public static void logError(Logger log, String message, String requestOrigin, String stepType) {
        setLogContext(requestOrigin, stepType, "-", "-", "-", "-", "-");
        log.error(message);
        clearLogContext();
    }

    public static void logError(Logger log, String message, String requestOrigin, String stepType, Throwable e) {
        setLogContext(requestOrigin, stepType, "-", "-", "-", "-", "-");
        log.error(message, e);
        clearLogContext();
    }

    public static void logInfo(Logger log, String message, String requestOrigin, String stepType) {
        setLogContext(requestOrigin, stepType, "-", "-", "-", "-", "-");
        log.info(message);
        clearLogContext();
    }

    public static void logStep(Logger log, ConfigurationStep step, String requestAuthorIp, Long elapsedTime, String requestTo, String requestContent, String responseContent, String responseStatus) {
        String stepType = Boolean.TRUE.equals(step.getSkip()) ? "skip" : step.getType();
        setLogContext(requestAuthorIp, stepType, elapsedTime.toString(), requestTo, requestContent, responseContent, responseStatus);
        String message = "Executed: %s".formatted(step.getName());
        log.info(message);
        clearLogContext();
    }

    private static void clearLogContext() {
        setLogContext("", "", "-", "-", "-", "-", "-");
    }

    private static void setLogContext(String requestAuthorIp, String stepType, String responseIn, String requestTo, String requestContent, String responseContent, String responseCode) {
        MDC.put(REQUEST_AUTHOR_IP, requestAuthorIp);
        MDC.put(STEP_TYPE, stepType);
        MDC.put(RESPONSE_IN, responseIn);
        MDC.put(REQUEST_TO, requestTo);
        MDC.put(REQUEST_CONTENT, requestContent);
        MDC.put(RESPONSE_CONTENT, responseContent);
        MDC.put(RESPONSE_CODE, responseCode);
    }
}
