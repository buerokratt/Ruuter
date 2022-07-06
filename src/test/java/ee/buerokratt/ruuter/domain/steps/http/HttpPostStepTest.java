package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.ConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WireMockTest
class HttpPostStepTest extends StepTestBase {

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private ApplicationProperties.DefaultAction defaultAction;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ApplicationProperties.Logging logging;

    @Mock
    private ApplicationProperties.HttpPost httpPost;

    private HttpHeaders httpHeaders;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getProperties()).thenReturn(properties);
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
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
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(ci.getContext()).thenReturn(testContext);
        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedPostArgs.getBody());
        when(properties.getLogging()).thenReturn(logging);
        when(logging.getDisplayRequestContent()).thenReturn(false);

        expectedPostStep.execute(ci);

        assertEquals(200, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenRequestIsInvalid(WireMockRuntimeInfo wireMockRuntimeInfo) {
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
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getRequestOrigin()).thenReturn("");
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedPostArgs.getBody());
        when(properties.getDefaultAction()).thenReturn(defaultAction);
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        when(defaultAction.getService()).thenReturn("default-action");
        when(defaultAction.getBody()).thenReturn(new HashMap<>());
        when(defaultAction.getQuery()).thenReturn(new HashMap<>());

        expectedPostStep.execute(ci);

        verify(configurationService, times(1)).execute(eq("default-action"), anyMap(), anyMap(), anyString());
    }

    @Test
    void execute_shouldAddDefaultHeadersDefinedInSettingsFileToRequest(WireMockRuntimeInfo wireMockRuntimeInfo) {
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setHeaders(new HashMap<>() {{
                put("header1", "value1");
            }});
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};
        ApplicationProperties.HttpPost httpPost = new ApplicationProperties.HttpPost() {{
            setHeaders(new HashMap<>() {{
                put("header2", "value2");
            }});
        }};

        when(properties.getHttpPost()).thenReturn(httpPost);
        expectedPostStep.execute(ci);

        assertEquals("value2", expectedPostStep.getArgs().getHeaders().get("header2"));
    }
}
