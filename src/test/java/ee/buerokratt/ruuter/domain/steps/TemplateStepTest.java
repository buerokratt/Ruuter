package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.MappingHelper;
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
    private ConfigurationService configurationService;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private ConfigurationInstance templateInstance;

    @Mock
    private MappingHelper mappingHelper;

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
            setRequestType("POST");
        }};

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getRequestOrigin()).thenReturn(requestOrigin);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(templateInstance.getReturnValue()).thenReturn(expectedResult);
        when(configurationService.execute(templateToCall, "POST", null, null, new HashMap<>(), requestOrigin)).thenReturn(templateInstance);
        templateStep.execute(ci);

        assertEquals(expectedResult, ci.getContext().get(resultName));
    }
}
