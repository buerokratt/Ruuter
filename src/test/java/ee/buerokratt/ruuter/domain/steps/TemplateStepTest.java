package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.DslService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class TemplateStepTest extends StepTestBase {

    @Mock
    private DslService dslService;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private DslInstance templateInstance;

    @Mock
    private MappingHelper mappingHelper;

    @BeforeEach
    protected void mockDependencies() {
        when(di.getDslService()).thenReturn(dslService);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
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

        when(di.getContext()).thenReturn(testContext);
        when(di.getRequestOrigin()).thenReturn(requestOrigin);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(templateInstance.getReturnValue()).thenReturn(expectedResult);
        when(dslService.execute(templateToCall, "POST", new HashMap<>(), new HashMap<>(), new HashMap<>(), requestOrigin)).thenReturn(templateInstance);
        templateStep.execute(di);

        assertEquals(expectedResult, di.getContext().get(resultName));
    }
}
