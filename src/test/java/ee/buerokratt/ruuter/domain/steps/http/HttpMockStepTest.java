package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.util.MappingUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class HttpMockStepTest {
    private final ScriptingHelper scriptingHelper = Mockito.mock(ScriptingHelper.class);

    @Test
    void execute_shouldStoreResponse() {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, new HashMap<>(), new HashMap<>(), new HashMap<>());
        String resultName = "result";
        HashMap<String, Object> mockStepResponse = new HashMap<>() {{
            put("key", "value");
        }};
        HttpMockArgs httpMockArgs = new HttpMockArgs() {{
            setResponse(mockStepResponse);
            setRequest(new HashMap<>() {{
                put("key2", "value2");
            }});
        }};
        HttpMockStep httpMockStep = new HttpMockStep() {{
            setCall("mockStep");
            setArgs(httpMockArgs);
            setResultName(resultName);
        }};
        HttpStepResult expectedResult = new HttpStepResult() {{
            setRequest(httpMockArgs);
            setResponse(new HttpQueryResponse(MappingUtils.convertMapToNode(mockStepResponse), null, 200));
        }};

        when(scriptingHelper.containsScript(anyString())).thenReturn(false);
        httpMockStep.execute(instance);

        assertEquals(expectedResult, instance.getContext().get(resultName));
    }
}
