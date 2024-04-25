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
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WireMockTest
class DefaultHttpServiceTest extends StepTestBase {

/*
    @Mock
    private ApplicationProperties properties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private DslService dslService;

    @Mock
    private ApplicationProperties.HttpPost httpPost;

    private HttpQueryArgs getArgs;

    private HttpStep getStep;

    private HttpQueryArgs postArgs;

    private HttpStep postStep;

    @BeforeEach
    protected void mockDependencies() {
        when(di.getContext()).thenReturn(new HashMap<>());
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getProperties()).thenReturn(properties);
    }

    @BeforeEach
    protected void initializeObjects(WireMockRuntimeInfo wireMockRuntimeInfo) {
        getArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};
        getStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(getArgs);
            setResultName("the_response");
        }};
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
    }

    @Test
    void execute_getRequestShouldExecuteDefaultDslWhenResponseCodeIsNotInWhitelist() {
        DefaultHttpDsl defaultHttpDsl = Mockito.spy(new DefaultHttpDsl() {{
            setDsl("default-dsl");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
            setRequestType("POST");
        }});
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(di.getDslService()).thenReturn(dslService);
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        when(properties.getDefaultDslInCaseOfException()).thenReturn(defaultHttpDsl);
        getStep.execute(di);

        verify(dslService, times(1)).execute("default-dsl", "POST", new HashMap<>(), new HashMap<>(), new HashMap<>(), null);
    }

    @Test
    void execute_postRequestShouldExecuteDefaultDslWhenResponseCodeIsNotInWhitelist() {
        DefaultHttpDsl defaultHttpDsl = Mockito.spy(new DefaultHttpDsl() {{
            setDsl("default-dsl");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
            setRequestType("POST");
        }});
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(di.getDslService()).thenReturn(dslService);
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), new HashMap<>(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(postArgs.getBody(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(postArgs.getBody());
        when(properties.getHttpPost()).thenReturn(httpPost);
        when(properties.getDefaultDslInCaseOfException()).thenReturn(defaultHttpDsl);
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        postStep.execute(di);

        verify(dslService, times(1)).execute("default-dsl", "POST", new HashMap<>(), new HashMap<>(), new HashMap<>(), null);
    }

    @Test
    void execute_shouldExecuteStepSpecificDefaultDslWhenResponseCodeIsNotInWhitelist() {
        DefaultHttpDsl defaultHttpDsl2 = Mockito.spy(new DefaultHttpDsl() {{
            setDsl("default-dsl2");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
            setRequestType("POST");
        }});
        getStep.setLocalHttpExceptionDsl(defaultHttpDsl2);
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(di.getDslService()).thenReturn(dslService);
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        getStep.execute(di);

        verify(dslService, times(1)).execute("default-dsl2", "POST", new HashMap<>(), new HashMap<>(), new HashMap<>(), null);
    }

    @Test
    void execute_shouldNotExecuteDefaultDslWhenRequestIsInvalidButDefaultDslIsNotDefined() {
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        getStep.execute(di);

        verify(dslService, times(0)).execute(anyString(), anyString(), anyMap(), anyMap(), anyMap(), anyString());
    }

 */
}
