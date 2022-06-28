package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseStepTest;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.MappingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@WireMockTest
class HttpPostStepTest extends BaseStepTest {

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ApplicationProperties properties;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(ci.getProperties()).thenReturn(properties);
    }

    @Test
    void execute_shouldSendPostRequestAndStoreResponse(WireMockRuntimeInfo wireMockRuntimeInfo) throws JsonProcessingException {
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
        logging.setDisplayRequestContent(false);

        when(ci.getContext()).thenReturn(testContext);
        when(properties.getLogging()).thenReturn(logging);
        when(mappingHelper.convertObjectToString(anyMap())).thenReturn(new ObjectMapper().writeValueAsString(expectedPostArgs.getBody()));
        stubFor(post("/endpoint").willReturn(ok()));
        expectedPostStep.execute(ci);

        assertEquals(200, ((HttpStepResult) ci.getContext().get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldThrowErrorWhenRequestFailsAndStopProcessingUnRespondingStepsIsTrue() {
        String getWrongRequestUrl = "http://localhost:randomPort/endpoint";
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setUrl(getWrongRequestUrl);
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};

        when(properties.isStopProcessingUnRespondingSteps()).thenReturn(true);
        stubFor(get("/endpoint").willReturn(ok()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> expectedPostStep.execute(ci));
        assertEquals("Error executing: %s".formatted(expectedPostStep.getName()), exception.getMessage());
    }

}
