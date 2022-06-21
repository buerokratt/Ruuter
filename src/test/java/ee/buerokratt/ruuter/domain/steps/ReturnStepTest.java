package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.BaseTest;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ReturnStepTest extends BaseTest {

    @Test
    void execute_shouldAssignReturnValue() {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, applicationProperties, new HashMap<>(), new HashMap<>(), new HashMap<>(), "", tracer);
        String expectedResult = "VALUE";
        ReturnStep assignStep = new ReturnStep() {{
            setReturnValue(expectedResult);
        }};

        when(scriptingHelper.containsScript(anyString())).thenReturn(false);
        assignStep.execute(instance);

        assertEquals(expectedResult, instance.getReturnValue());
    }

    @Test
    void execute_shouldCallScriptingHelperWhenScriptFound() {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, applicationProperties, new HashMap<>(), new HashMap<>(), new HashMap<>(), "", tracer);
        String expectedResult = "VALUE";
        ReturnStep assignStep = new ReturnStep() {{
            setReturnValue("${value}");
        }};

        when(scriptingHelper.containsScript(anyString())).thenReturn(true);
        when(scriptingHelper.evaluateScripts(anyString(), anyMap())).thenReturn(expectedResult);
        assignStep.execute(instance);

        assertEquals(expectedResult, instance.getReturnValue());
    }
}
