package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.core.JsonProcessingException;
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
class HttpPostStepTest extends BaseStepTest {

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private BiPredicate<String, String> biPredicate;

    private HttpHeaders httpHeaders;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(ci.getProperties()).thenReturn(properties);
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        httpHeaders = HttpHeaders.of(new HashMap<>(), biPredicate);
    }

    @Test
    void execute_shouldSendPostRequestAndStoreResponse(WireMockRuntimeInfo wireMockRuntimeInfo) {
        HashMap<String, Object> testContext = new HashMap<>();
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
        ApplicationProperties.Logging logging = new ApplicationProperties.Logging();
        logging.setDisplayRequestContent(false);

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().makeHttpPostRequest(expectedPostArgs, ci)).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        stubFor(post("/endpoint").willReturn(ok()));
        expectedPostStep.execute(ci);

        assertEquals(200, ((HttpStepResult) ci.getContext().get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldThrowErrorWhenRequestFailsAndStopProcessingUnRespondingStepsIsTrue() {
        String getWrongRequestUrl = "http://localhost:randomPort/endpoint";
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setUrl(getWrongRequestUrl);
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};

        when(ci.getHttpHelper().makeHttpPostRequest(expectedPostArgs, ci)).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(properties.isStopProcessingUnRespondingService()).thenReturn(true);
        stubFor(get("/endpoint").willReturn(ok()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> expectedPostStep.execute(ci));
        assertEquals("Error executing: %s".formatted(expectedPostStep.getName()), exception.getMessage());
    }

}
