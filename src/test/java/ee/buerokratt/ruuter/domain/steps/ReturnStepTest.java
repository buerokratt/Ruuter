package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
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

    @Mock
    private ApplicationProperties properties;

    @BeforeEach
    protected void mockScriptingHelper() {
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
    }

    @Test
    void execute_shouldAssignReturnValue() {
        String expectedResult = "VALUE 1";
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue(expectedResult);
        }};

        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(di);

        verify(di, times(1)).setReturnValue(expectedResult);
    }

    @Test
    void execute_shouldCallScriptingHelperWhenScriptFound() {
        String expectedResult = "VALUE 2";
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue("${value}");
        }};

        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(di);

        verify(di, times(1)).setReturnValue(expectedResult);
    }

    @Test
    void execute_shouldAssignFormattedHeaders() {
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
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("Set-Cookie", "cookieName=headerName; Domain=localhost; Secure; stringBoolean=false; Max-Age=300; Expires=2022-08-08T10:08:39.159Z; ");

        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(headers);
        returnStep.execute(di);

        verify(scriptingHelper, times(1)).evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap());
        verify(di, times(1)).setReturnHeaders(expectedResult);
    }
}
