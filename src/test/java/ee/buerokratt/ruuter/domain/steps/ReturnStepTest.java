package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.exception.DSLExecutionException;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReturnStepTest extends StepTestBase {

    @Mock
    private ScriptingHelper scriptingHelper;

    private final ApplicationProperties properties = new ApplicationProperties();
    private final MappingHelper mappingHelper = new MappingHelper(new ObjectMapper());

    @BeforeEach
    protected void mockScriptingHelper() {
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getProperties()).thenReturn(properties);
        when(di.getContext()).thenReturn(new HashMap<>());
        when(di.getRequestBody()).thenReturn(new HashMap<>());
        when(di.getRequestQuery()).thenReturn(new HashMap<>());
        when(di.getRequestHeaders()).thenReturn(new HashMap<>());
    }

    @Test
    void execute_shouldAssignReturnValue() throws StepExecutionException, DSLExecutionException {
        String expectedResult = "VALUE 1";
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue(expectedResult);
        }};

        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(di);

        verify(di, times(1)).setReturnValue(expectedResult);
    }

    @Test
    void execute_shouldCallScriptingHelperWhenScriptFound() throws StepExecutionException, DSLExecutionException {
        String expectedResult = "VALUE 2";
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue("${value}");
        }};

        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(di);

        verify(di, times(1)).setReturnValue(expectedResult);
    }

    @Test
    void execute_shouldAssignFormattedHeaders() throws StepExecutionException, DSLExecutionException {
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> cookieHeader = new LinkedHashMap<>() {{
            put("cookieName", "headerName");
            put("Domain", "localhost");
            put("Secure", true);
            put("HttpOnly", false);
            put("stringBoolean", "false");
            put("Max-Age", 300);
            put("Expires", "2022-08-08T10:08:39.159Z");
        }};
        headers.put("Set-Cookie", cookieHeader);
        ReturnStep returnStep = new ReturnStep() {{
            setHeaders(headers);
        }};
        // ReturnStep.addDefaultCookies fills in a "Path=/" default for any Set-Cookie header that
        // doesn't specify one - this test's cookie map doesn't, so the default is expected here too.
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("Set-Cookie", "cookieName=headerName; Domain=localhost; Secure; stringBoolean=false; Max-Age=300; Expires=2022-08-08T10:08:39.159Z; Path=/; ");

        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(headers);
        returnStep.execute(di);

        verify(scriptingHelper, times(1)).evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap());
        verify(di, times(1)).setReturnHeaders(expectedResult);
    }
}
