package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.DslService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.mockito.Mockito;

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
    private DslService dslService;

    @Mock
    private ApplicationProperties.HttpPost httpPost;

    @BeforeEach
    protected void mockDependencies() {
        when(di.getProperties()).thenReturn(properties);
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
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
            setHeaders(new HashMap<>());
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(di.getContext()).thenReturn(testContext);
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(httpPost.getHeaders()).thenReturn(new HashMap<>());
        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedPostArgs.getBody());

        expectedPostStep.execute(di);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenResponseCodeIsNotInWhitelist(WireMockRuntimeInfo wireMockRuntimeInfo) {
        DefaultHttpDsl defaultHttpDsl = Mockito.spy(new DefaultHttpDsl() {{
            setDsl("default-action");
            setRequestType("POST");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }});
        HashMap<String, Object> testContext = new HashMap<>();
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setHeaders(new HashMap<>());
        }};
        HttpStep failingPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        when(di.getDslService()).thenReturn(dslService);
        when(di.getContext()).thenReturn(testContext);
        when(di.getRequestOrigin()).thenReturn("");
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(expectedPostArgs.getBody());
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        when(properties.getDefaultDslInCaseOfException()).thenReturn(defaultHttpDsl);

        failingPostStep.execute(di);

        verify(dslService, times(1)).execute(eq("default-action"), anyString(), anyMap(), anyMap(), anyString());
    }

    @Test
    void execute_shouldAddDefaultHeadersDefinedInSettingsFileToRequest(WireMockRuntimeInfo wireMockRuntimeInfo) {
        HashMap<String, Object> testContext = new HashMap<>();
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
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(di.getContext()).thenReturn(testContext);
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(scriptingHelper.evaluateScripts(any(), anyMap(), anyMap(), anyMap())).thenReturn(null);
        when(httpHelper.doPost(expectedPostArgs.getUrl(), expectedPostArgs.getBody(), expectedPostArgs.getQuery(), expectedPostArgs.getHeaders())).thenReturn(httpResponse);
        expectedPostStep.execute(di);

        assertEquals("value2", expectedPostStep.getArgs().getHeaders().get("header2"));
    }
}
