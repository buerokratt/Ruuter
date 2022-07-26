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
    private HashMap<String, Object> testContext;
    private String expectedResult;

    @BeforeEach
    protected void mockScriptingHelper() {
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getContext()).thenReturn(testContext);
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedResult);
    }

    @BeforeEach
    protected void initializeObjects() {
        testContext = new HashMap<>();
        expectedResult = "VALUE";
    }

    @Test
    void execute_shouldAssignVariableToContext() {
        AssignStep<String> assignStep = new AssignStep<>() {{
            setAssign(new HashMap<>() {{
                put("key", expectedResult);
            }});
        }};

        assignStep.execute(di);

        assertEquals(expectedResult, di.getContext().get("key"));
    }

    @Test
    void execute_shouldCallScriptingHelperWhenScriptFound() {
        AssignStep<String> assignStep = new AssignStep<>() {{
            setAssign(new HashMap<>() {{
                put("key", "${value}");
            }});
        }};

        assignStep.execute(di);

        assertEquals(expectedResult, di.getContext().get("key"));
    }
}
