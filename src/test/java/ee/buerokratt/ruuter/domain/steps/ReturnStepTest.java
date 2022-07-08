package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReturnStepTest extends StepTestBase {

    @Mock
    private ScriptingHelper scriptingHelper;

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

        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        returnStep.execute(ci);

        verify(ci, times(1)).setReturnValue(expectedResult);
    }
}
