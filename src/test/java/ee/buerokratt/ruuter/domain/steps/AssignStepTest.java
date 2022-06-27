package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.BaseTest;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.MappingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AssignStepTest extends BaseTest {

    @Mock
    private MappingHelper mappingHelper;

    @Test
    void execute_shouldAssignVariableToContext() {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, applicationProperties, new HashMap<>(), new HashMap<>(), new HashMap<>(), mappingHelper, "", tracer, true);
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
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, applicationProperties, new HashMap<>(), new HashMap<>(), new HashMap<>(), mappingHelper, "", tracer, true);
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
