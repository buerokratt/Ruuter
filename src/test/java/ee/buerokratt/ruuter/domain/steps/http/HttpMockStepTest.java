package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.StepTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class HttpMockStepTest extends StepTestBase {

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
            setResponse(new ResponseEntity<>(mockStepResponse, null, HttpStatus.OK));
        }};

        when(ci.getContext()).thenReturn(testContext);
        httpMockStep.execute(ci);

        assertEquals(expectedResult, ci.getContext().get(resultName));
    }
}
