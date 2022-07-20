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
import org.mockito.Mockito;
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
    private ConfigurationService configurationService;

    @Mock
    private ApplicationProperties.HttpPost httpPost;

    private HashMap<String, Object> testContext;
    private HttpQueryArgs expectedPostArgs;
    private HttpStep expectedPostStep;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getContext()).thenReturn(testContext);
        when(ci.getProperties()).thenReturn(properties);
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
    }

    @BeforeEach
    protected void initializeObjects(WireMockRuntimeInfo wireMockRuntimeInfo) {
        testContext = new HashMap<>();
        expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setHeaders(new HashMap<>());
        }};
        expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};
    }

    @Test
    void execute_shouldSendPostRequestAndStoreResponse() {
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedPostArgs.getBody());
        when(httpPost.getHeaders()).thenReturn(new HashMap<>());
        when(properties.getHttpPost()).thenReturn(httpPost);

        expectedPostStep.execute(ci);

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
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedPostArgs.getBody());
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        when(properties.getDefaultServiceInCaseOfException()).thenReturn(defaultHttpService);

        expectedPostStep.execute(ci);

        verify(configurationService, times(1)).execute(eq("default-action"), anyString(), anyMap(), anyMap(), eq(null));
    }

    @Test
    void execute_shouldAddDefaultHeadersDefinedInSettingsFileToRequest() {
        expectedPostArgs.setHeaders(new HashMap<>() {{
            put("header1", "value1");
        }});
        ApplicationProperties.HttpPost httpPost = new ApplicationProperties.HttpPost() {{
            setHeaders(new HashMap<>() {{
                put("header2", "value2");
            }});
        }};
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(expectedPostArgs.getBody(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(expectedPostArgs.getBody());
        when(properties.getHttpPost()).thenReturn(httpPost);
        expectedPostStep.execute(ci);

        assertEquals("value2", expectedPostStep.getArgs().getHeaders().get("header2"));
    }
}
