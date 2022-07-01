package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseStepTest;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.function.BiPredicate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@WireMockTest
class HttpGetStepTest extends BaseStepTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private BiPredicate<String, String> biPredicate;

    private HttpHeaders httpHeaders;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getProperties()).thenReturn(applicationProperties);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        httpHeaders = HttpHeaders.of(new HashMap<>(), biPredicate);
    }

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
        when(ci.getHttpHelper().makeHttpGetRequest(expectedGetArgs, ci)).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        stubFor(get("/endpoint?some_val=Hello+World&another_val=123").willReturn(ok()));
        expectedGetStep.execute(ci);

        assertEquals(200, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldThrowErrorWhenRequestFailsAndStopProcessingUnRespondingServiceIsTrue() {
        String getWrongRequestUrl = "http://localhost:randomPort/endpoint";
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setUrl(getWrongRequestUrl);
        }};
        HttpStep expectedGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};

        when(ci.getHttpHelper().makeHttpGetRequest(expectedGetArgs, ci)).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(applicationProperties.isStopProcessingUnRespondingService()).thenReturn(true);
        stubFor(get("/endpoint").willReturn(ok()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> expectedGetStep.execute(ci));
        assertEquals("Error executing: %s".formatted(expectedGetStep.getName()), exception.getMessage());
    }

}
