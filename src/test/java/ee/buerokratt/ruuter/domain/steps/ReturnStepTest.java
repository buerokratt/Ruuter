package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ReturnStepTest {
    private final ScriptingHelper scriptingHelper = Mockito.mock(ScriptingHelper.class);
    private final MappingHelper mappingHelper = Mockito.mock(MappingHelper.class);

    @Test
    void execute_shouldAssignReturnValue() {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, new HashMap<>(), new HashMap<>(), new HashMap<>(), mappingHelper);
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
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, new HashMap<>(), new HashMap<>(), new HashMap<>(), mappingHelper);
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
