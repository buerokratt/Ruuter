package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class HttpMockStepTest {
    private final ScriptingHelper scriptingHelper = Mockito.mock(ScriptingHelper.class);
    private final MappingHelper mappingHelper = Mockito.mock(MappingHelper.class);

    @Test
    void execute_shouldStoreResponse() {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, new HashMap<>(), new HashMap<>(), new HashMap<>(), mappingHelper);
        String resultName = "result";
        HashMap<String, Object> mockStepResponse = new HashMap<>() {{
            put("key", "value");
        }};
        HttpQueryArgs mockStepRequest = new HttpQueryArgs() {{
           setBody(new HashMap<>() {{
               put("key", "value");
           }});
           setUrl("https://example.com/endpoint");
        }};
        HttpMockArgs httpMockArgs = new HttpMockArgs() {{
            setResponse(mockStepResponse);
            setRequest(mockStepRequest);
        }};
        HttpMockStep httpMockStep = new HttpMockStep() {{
            setCall("mockStep");
            setArgs(httpMockArgs);
            setResultName(resultName);
        }};
        HttpStepResult expectedResult = new HttpStepResult() {{
            setRequest(mockStepRequest);
            setResponse(new HttpQueryResponse(mappingHelper.convertMapToNode(mockStepResponse), null, 200));
        }};

        when(scriptingHelper.containsScript(anyString())).thenReturn(false);
        httpMockStep.execute(instance);

        assertEquals(expectedResult, instance.getContext().get(resultName));
    }
}
