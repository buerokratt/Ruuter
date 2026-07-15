package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.exception.DSLExecutionException;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogStepTest extends StepTestBase {

    @Mock
    private ScriptingHelper scriptingHelper;

    private Map<String, Object> context;
    private Map<String, Object> requestBody;
    private Map<String, Object> requestQuery;
    private Map<String, String> requestHeaders;

    @BeforeEach
    void setUp() {
        context = new HashMap<>();
        requestBody = new HashMap<>();
        requestQuery = new HashMap<>();
        requestHeaders = new HashMap<>();

        lenient().when(di.getContext()).thenReturn(context);
        lenient().when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        lenient().when(di.getRequestBody()).thenReturn(requestBody);
        lenient().when(di.getRequestQuery()).thenReturn(requestQuery);
        lenient().when(di.getRequestHeaders()).thenReturn(requestHeaders);

        lenient().when(scriptingHelper.evaluateScripts(eq("some message"), eq(context), eq(requestBody), eq(requestQuery), eq(requestHeaders)))
            .thenReturn("evaluated message");
    }

    @Test
    void getType_shouldReturnLog() {
        assertEquals("log", new LogStep().getType());
    }

    @Test
    void execute_shouldEvaluateMessageThroughScriptingHelper() throws StepExecutionException, DSLExecutionException {
        LogStep step = new LogStep();
        step.setName("log_step");
        step.setMessage("some message");

        step.execute(di);

        verify(scriptingHelper).evaluateScripts("some message", context, requestBody, requestQuery, requestHeaders);
    }
}
