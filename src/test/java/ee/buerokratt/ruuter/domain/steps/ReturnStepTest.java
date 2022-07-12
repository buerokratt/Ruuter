package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.steps.http.HttpStepResult;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
    }

    @Test
    void execute_shouldAssignReturnValue() {
        String expectedResult = "VALUE 1";
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue(expectedResult);
        }};

        when(ci.getProperties()).thenReturn(properties);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(ci);

        verify(ci, times(1)).setReturnValue(expectedResult);
    }

    @Test
    void execute_shouldCallScriptingHelperWhenScriptFound() {
        String expectedResult = "VALUE 2";
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue("${value}");
        }};

        when(ci.getProperties()).thenReturn(properties);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(ci);

        verify(ci, times(1)).setReturnValue(expectedResult);
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
        returnStep.execute(ci);

        verify(scriptingHelper, times(1)).evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap());
        verify(ci, times(1)).setReturnHeaders(expectedResult);
    }

    @Test
    void execute_shouldAddDefaultHttpStatusCodeDefinedInApplicationYmlFile() {
        HttpStepResult expectedResult = new HttpStepResult() {{
            setRequest(null);
            setResponse(new HttpQueryResponse() {{
                setStatus(HttpStatus.OK.value());
            }});
        }};
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue("${value}");
        }};
        ApplicationProperties.FinalResponse finalResponse = new ApplicationProperties.FinalResponse() {{
            setHttpStatusCode(HttpStatus.ACCEPTED.value());
        }};

        when(ci.getProperties()).thenReturn(properties);
        when(ci.getReturnValue()).thenReturn(expectedResult);
        when(properties.getFinalResponse()).thenReturn(finalResponse);
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(ci);

        HttpStepResult resultWithDefaultStatusCode = (HttpStepResult) ci.getReturnValue();
        assertEquals(HttpStatus.ACCEPTED.value(), resultWithDefaultStatusCode.getResponse().getStatus());
    }

    @Test
    void execute_shouldNotAddDefaultHttpStatusCodeWhenItIsNull() {
        HttpStepResult expectedResult = new HttpStepResult() {{
            setRequest(null);
            setResponse(new HttpQueryResponse() {{
                setStatus(HttpStatus.OK.value());
            }});
        }};
        ReturnStep returnStep = new ReturnStep() {{
            setReturnValue("${value}");
        }};
        ApplicationProperties.FinalResponse finalResponse = new ApplicationProperties.FinalResponse() {{
            setHttpStatusCode(null);
        }};

        when(ci.getProperties()).thenReturn(properties);
        when(ci.getReturnValue()).thenReturn(expectedResult);
        when(properties.getFinalResponse()).thenReturn(finalResponse);
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(ci);

        HttpStepResult resultWithDefaultStatusCode = (HttpStepResult) ci.getReturnValue();
        assertEquals(HttpStatus.OK.value(), resultWithDefaultStatusCode.getResponse().getStatus());
    }
}
