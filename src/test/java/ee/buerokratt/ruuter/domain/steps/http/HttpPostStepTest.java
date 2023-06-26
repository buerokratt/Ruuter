package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.Logging;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

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
    private ApplicationProperties.HttpPost httpPost;

    @Mock
    private Logging logging;

    private HttpQueryArgs postArgs;
    private HttpStep postStep;
    private Map<String, Object> testContext;
    private ResponseEntity<Object> httpResponse;

    @BeforeEach
    protected void mockDependencies() {
        when(di.getContext()).thenReturn(testContext);
        when(di.getProperties()).thenReturn(properties);
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(properties.getHttpPost()).thenReturn(httpPost);
    }

    @BeforeEach
    protected void initializeObjects(WireMockRuntimeInfo wireMockRuntimeInfo) {
        postArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};
        postStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(postArgs);
            setResultName("the_response");
        }};
        testContext = new HashMap<>();
        httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);
    }

    @Test
    void execute_shouldSendPostRequestAndStoreResponse() {
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), new HashMap<>(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(postArgs.getBody(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(postArgs.getBody());
        when(httpPost.getHeaders()).thenReturn(new HashMap<>());
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(properties.getLogging()).thenReturn(logging);
        when(logging.getDisplayRequestContent()).thenReturn(true);
        postStep.execute(di);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());
    }

    @Test
    void execute_shouldAddDefaultHeadersDefinedInSettingsFileToRequest() {
        postArgs.setHeaders(new HashMap<>() {{
            put("header1", "value1");
        }});
        ApplicationProperties.HttpPost httpPost = new ApplicationProperties.HttpPost() {{
            setHeaders(new HashMap<>() {{
                put("header2", "value2");
            }});
        }};

        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), new HashMap<>(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(postArgs.getBody(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(postArgs.getBody());
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(properties.getLogging()).thenReturn(logging);
        when(logging.getDisplayRequestContent()).thenReturn(true);
        postStep.execute(di);

        assertEquals("value2", postStep.getArgs().getHeaders().get("header2"));
    }

    @Test
    void execute_shouldThrowErrorWhenUrlIsInvalid(WireMockRuntimeInfo wireMockRuntimeInfo) {
        postStep.getArgs().setUrl("http://notFounUrl:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));

        when(scriptingHelper.evaluateScripts(postArgs.getBody(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(postArgs.getBody());
        when(properties.getStopInCaseOfException()).thenReturn(true);
        doCallRealMethod().when(httpHelper).doPost(postArgs.getUrl(), postArgs.getBody(), postArgs.getQuery(), new HashMap<>());

        assertThrows(IllegalArgumentException.class, () -> postStep.execute(di));
    }

    @Test
    void execute_shouldFollowPlaintextWhenPlaintextContentType(WireMockRuntimeInfo wireMockRuntimeInfo){
        postArgs.setContentType("plaintext");
        postArgs.setPlaintext("plaintextTest");
        when(httpHelper.doPostPlaintext(postArgs.getUrl(), postArgs.getBody(), new HashMap<>(), new HashMap<>(), postArgs.getPlaintext())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(postArgs.getBody(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(postArgs.getBody());
        when(httpPost.getHeaders()).thenReturn(new HashMap<>());
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(properties.getLogging()).thenReturn(logging);
        when(logging.getDisplayRequestContent()).thenReturn(true);
        postStep.execute(di);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());

    }
}
