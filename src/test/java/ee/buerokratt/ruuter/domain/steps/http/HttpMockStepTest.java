package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.helper.MappingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

class HttpMockStepTest extends StepTestBase {

    @Mock
    private MappingHelper mappingHelper;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
    }

    @Test
    void execute_shouldStoreResponse() {
        HashMap<String, Object> testContext = new HashMap<>();
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
            setResponse(new HttpQueryResponse(new ObjectMapper().convertValue(mockStepResponse, JsonNode.class), null, 200));
        }};

        when(ci.getContext()).thenReturn(testContext);
        when(mappingHelper.convertMapToNode(anyMap())).thenReturn(new ObjectMapper().convertValue(mockStepRequest.getBody(), JsonNode.class));
        httpMockStep.execute(ci);

        assertEquals(expectedResult, ci.getContext().get(resultName));
    }
}
