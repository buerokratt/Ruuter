package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AssignStepTest {
    private final ScriptingHelper scriptingHelper = Mockito.mock(ScriptingHelper.class);

    @Test
    void execute_shouldAssignVariableToContext() {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, new HashMap<>(), new HashMap<>(), new HashMap<>());
        String expectedResult = "VALUE";
        AssignStep<String> assignStep = new AssignStep<>() {{
            setAssign(new HashMap<>() {{
                put("key", expectedResult);
            }});
        }};

        when(scriptingHelper.containsScript(anyString())).thenReturn(false);
        assignStep.execute(instance);

        assertEquals(expectedResult, instance.getContext().get("key"));
    }

    @Test
    void execute_shouldCallScriptingHelperWhenScriptFound() {
        String expectedResult = "EVALUATED";
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, new HashMap<>(), new HashMap<>(), new HashMap<>());
        AssignStep<String> assignStep = new AssignStep<>() {{
            setAssign(new HashMap<>() {{
                put("key", "${value}");
            }});
        }};

        when(scriptingHelper.containsScript(anyString())).thenReturn(true);
        when(scriptingHelper.evaluateScripts(anyString(), anyMap())).thenReturn(expectedResult);
        assignStep.execute(instance);

        assertEquals(expectedResult, instance.getContext().get("key"));
    }
}
