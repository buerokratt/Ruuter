package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseStepTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WireMockTest
class HttpGetStepTest extends BaseStepTest {

    @Test
    void execute_shouldQueryEndpointAndStoreResponse(WireMockRuntimeInfo wmRuntimeInfo) {
        HashMap<String, Object> testContext = new HashMap<>();
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wmRuntimeInfo.getHttpPort()));
        }};
        HttpStep expectedGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};

        when(ci.getContext()).thenReturn(testContext);
        stubFor(get("/endpoint?some_val=Hello+World&another_val=123").willReturn(ok()));
        expectedGetStep.execute(ci);

        assertEquals(200, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldPassDefinedHeadersToRequest(WireMockRuntimeInfo wmRuntimeInfo) {
        HashMap<String, Object> testContext = new HashMap<>();
        String headerValue = "Some custom header value";
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wmRuntimeInfo.getHttpPort()));
            setHeaders(new HashMap<>(){{
                put("X-Custom-Header", headerValue);
            }});
        }};
        HttpStep expectedGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};

        when(ci.getContext()).thenReturn(testContext);
        stubFor(get("/endpoint?some_val=Hello+World&another_val=123").willReturn(ok()));
        expectedGetStep.execute(ci);

        verify(getRequestedFor(urlEqualTo("/endpoint?some_val=Hello+World&another_val=123"))
            .withHeader("X-Custom-Header", equalTo("Some custom header value")));
    }
}
