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
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private ConfigurationService configurationService;

    @Mock
    private ApplicationProperties.HttpPost httpPost;

    @Mock
    private ApplicationProperties.Logging logging;

    private HttpQueryArgs postArgs;

    private HttpStep postStep;

    private Map<String, Map<String, Object>> evaluatedParameters;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getProperties()).thenReturn(properties);
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
    }

    @BeforeEach
    protected void initializeObjects(WireMockRuntimeInfo wireMockRuntimeInfo) {
        postArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setHeaders(new HashMap<>());
        }};
        postStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(postArgs);
            setResultName("the_response");
        }};
        evaluatedParameters = new HashMap<>() {{
            put("body", postArgs.getBody());
        }};
    }

    @Test
    void execute_shouldSendPostRequestAndStoreResponse() {
        Map<String, Object> testContext = new HashMap<>();
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), postArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateRequestParameters(ci, postArgs.getBody(), null, new HashMap<>())).thenReturn(evaluatedParameters);
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(properties.getLogging()).thenReturn(logging);
        when(httpPost.getHeaders()).thenReturn(new HashMap<>());
        postStep.execute(ci);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenResponseCodeIsNotInWhitelist() {
        DefaultHttpService defaultHttpService = Mockito.spy(new DefaultHttpService() {{
            setService("default-action");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }});
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(ci.getContext()).thenReturn(new HashMap<>());
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), postArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateRequestParameters(ci, postArgs.getBody(), null, new HashMap<>())).thenReturn(evaluatedParameters);
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(properties.getDefaultServiceInCaseOfException()).thenReturn(defaultHttpService);
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        postStep.execute(ci);

        verify(configurationService, times(1)).execute("default-action", "POST", null, null, new HashMap<>(), null);
    }

    @Test
    void execute_shouldAddDefaultHeadersDefinedInSettingsFileToRequest() {
        postArgs.setHeaders(new HashMap<>() {{
            put("header1", "value1");
        }});
        Map<String, Object> headers = new HashMap<>() {{
            put("header1", "value1");
            put("header2", "value2");
        }};
        ApplicationProperties.HttpPost httpPost = new ApplicationProperties.HttpPost() {{
            setHeaders(new HashMap<>() {{
                put("header2", "value2");
            }});
        }};
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(ci.getContext()).thenReturn(new HashMap<>());
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), postArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateRequestParameters(ci, postArgs.getBody(), null, headers)).thenReturn(evaluatedParameters);
        when(properties.getHttpPost()).thenReturn(httpPost);
        postStep.execute(ci);

        assertEquals("value2", postStep.getArgs().getHeaders().get("header2"));
    }
}
