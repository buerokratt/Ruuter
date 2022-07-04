package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.ConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class TemplateStepTest extends StepTestBase {

    @Mock
    ConfigurationService configurationService;

    @Mock
    ScriptingHelper scriptingHelper;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
    }

    @Test
    void execute_shouldCallTemplateWithNoInputAndStoreResult() {
        HashMap<String, Object> testContext = new HashMap<>();
        String resultName = "result-name";
        String templateToCall = "call-me";
        String requestOrigin = "origin";
        String expectedResult = "VALUE 1";
        TemplateStep templateStep = new TemplateStep() {{
            setTemplateToCall(templateToCall);
            setResultName(resultName);
        }};

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getRequestOrigin()).thenReturn(requestOrigin);
        when(configurationService.execute(templateToCall, "", null, new HashMap<>(), requestOrigin)).thenReturn(expectedResult);
        templateStep.execute(ci);

        assertEquals(expectedResult, ci.getContext().get(resultName));
    }
}
