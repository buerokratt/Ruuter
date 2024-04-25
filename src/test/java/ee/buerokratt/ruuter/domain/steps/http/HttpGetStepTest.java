package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@WireMockTest
class HttpGetStepTest extends StepTestBase {

    /*
    @Mock
    private ApplicationProperties properties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ScriptingHelper scriptingHelper;

    private HttpQueryArgs getArgs;
    private HttpStep getStep;
    private Map<String, Object> testContext;

    @BeforeEach
    protected void mockDependencies() {
        when(di.getContext()).thenReturn(testContext);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getProperties()).thenReturn(properties);
    }

    @BeforeEach
    protected void initializeObjects(WireMockRuntimeInfo wireMockRuntimeInfo) {
        testContext = new HashMap<>();
        getArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setBody(null);
        }};
        getStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(getArgs);
            setResultName("the_response");
        }};
    }

    @Test
    void execute_shouldQueryEndpointAndStoreResponse() {
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(httpHelper.doMethod(HttpMethod.GET, getArgs.getUrl(), getArgs.getQuery(), null, new HashMap<>(), null, null)).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        getStep.execute(di);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());
        assertEquals(httpResponse.getBody(), ((HttpStepResult) testContext.get("the_response")).getResponse().getBody());
    }

    @Test
    void execute_shouldThrowIllegalArgumentExceptionWhenUrlIsInvalid(WireMockRuntimeInfo wireMockRuntimeInfo) {
        getStep.getArgs().setUrl("http://notFounUrl:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));

        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        when(properties.getStopInCaseOfException()).thenReturn(true);
        doCallRealMethod().when(httpHelper).doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>());

        assertThrows(IllegalArgumentException.class, () -> getStep.execute(di));
    }

    @Test
    void execute_shouldThrowIllegalArgumentExceptionWhenHttpStatusCodeIsNotinWhitelist() {
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(di.getHttpHelper().doMethod(HttpMethod.GET, getArgs.getUrl(), getArgs.getQuery(), null, new HashMap<>(), null, null)).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        when(properties.getStopInCaseOfException()).thenReturn(true);
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});

        assertThrows(IllegalArgumentException.class, () -> getStep.execute(di));
    }
    
     */
}
