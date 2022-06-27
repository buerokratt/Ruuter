package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseTest;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.MappingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WireMockTest
class HttpPostStepTest extends BaseTest {
    @Mock
    private MappingHelper mappingHelper;

    @Test
    void execute_shouldSendPostRequestAndStoreResponse(WireMockRuntimeInfo wireMockRuntimeInfo) {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, applicationProperties, new HashMap<>(), new HashMap<>(), new HashMap<>(), mappingHelper, "", tracer);
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};

        HttpStep expectedPostStep = new HttpPostStep() {{
           setName("post_message");
           setArgs(expectedPostArgs);
           setResultName("the_response");
        }};

        when(mappingHelper.convertObjectToString(expectedPostArgs.getBody())).thenReturn("""
            {
              "some_val" : "Hello World",
              "another_val" : 123
            }""");
        stubFor(post("/endpoint").willReturn(ok()));
        expectedPostStep.execute(instance);

        assertEquals(200, ((HttpStepResult) instance.getContext().get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldPassDefinedHeadersToRequest(WireMockRuntimeInfo wireMockRuntimeInfo) {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, applicationProperties, new HashMap<>(), new HashMap<>(), new HashMap<>(), mappingHelper, "", tracer);
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setHeaders(new HashMap<>(){{
                put("Cache-Control", "no-cache");
            }});
        }};
        HttpStep expectedPostStep = new HttpGetStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};

        expectedPostStep.execute(instance);
        verify(postRequestedFor(urlEqualTo("/endpoint"))
            .withHeader("Cache-Control", equalTo("no-cache")));
    }
}
