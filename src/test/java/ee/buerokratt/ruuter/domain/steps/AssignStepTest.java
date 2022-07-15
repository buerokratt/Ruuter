package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AssignStepTest extends StepTestBase {

    @Mock
    private ScriptingHelper scriptingHelper;

    @BeforeEach
    protected void mockScriptingHelper() {
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
    }

    @Test
    void execute_shouldAssignVariableToContext() {
        HashMap<String, Object> testContext = new HashMap<>();
        String expectedResult = "VALUE";
        AssignStep<String> assignStep = new AssignStep<>() {{
            setAssign(new HashMap<>() {{
                put("key", expectedResult);
            }});
        }};

        when(di.getContext()).thenReturn(testContext);
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        assignStep.execute(di);

        assertEquals(expectedResult, di.getContext().get("key"));
    }

    @Test
    void execute_shouldCallScriptingHelperWhenScriptFound() {
        HashMap<String, Object> testContext = new HashMap<>();
        String expectedResult = "EVALUATED";
        AssignStep<String> assignStep = new AssignStep<>() {{
            setAssign(new HashMap<>() {{
                put("key", "${value}");
            }});
        }};

        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
        when(di.getContext()).thenReturn(testContext);
        assignStep.execute(di);

        assertEquals(expectedResult, di.getContext().get("key"));
    }
}
