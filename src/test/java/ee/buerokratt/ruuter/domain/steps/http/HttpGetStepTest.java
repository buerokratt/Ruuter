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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WireMockTest
class HttpGetStepTest extends StepTestBase {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private ConfigurationService configurationService;

    private HttpQueryArgs getArgs;

    private HttpStep getStep;

    private Map<String, Map<String, Object>> evaluatedParameters;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
        when(ci.getProperties()).thenReturn(applicationProperties);
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
        evaluatedParameters = new HashMap<>() {{
            put("query", getArgs.getQuery());
        }};
    }

    @Test
    void execute_shouldQueryEndpointAndStoreResponse() {
        Map<String, Object> testContext = new HashMap<>();
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(ci.getContext()).thenReturn(testContext);
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateRequestParameters(ci, null, getArgs.getQuery(), new HashMap<>())).thenReturn(evaluatedParameters);
        getStep.execute(ci);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());
        assertEquals(httpResponse.getBody(), ((HttpStepResult) testContext.get("the_response")).getResponse().getBody());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenRequestIsInvalidAndStopInCaseOfExceptionIsTrue() {
        DefaultHttpService defaultHttpService = Mockito.spy(new DefaultHttpService() {{
            setService("default-action");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }});
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(ci.getContext()).thenReturn(new HashMap<>());
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateRequestParameters(ci, null, getArgs.getQuery(), new HashMap<>())).thenReturn(evaluatedParameters);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        when(applicationProperties.getDefaultServiceInCaseOfException()).thenReturn(defaultHttpService);
        getStep.execute(ci);

        verify(configurationService, times(1)).execute("default-action", "POST", null, null, new HashMap<>(), null);
    }

    @Test
    void execute_shouldExecuteStepSpecificDefaultActionWhenRequestIsInvalidAndStopInCaseOfExceptionIsTrue() {
        DefaultHttpService defaultHttpService2 = Mockito.spy(new DefaultHttpService() {{
            setService("default-action2");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }});
        getStep.setLocalHttpExceptionService(defaultHttpService2);
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(ci.getContext()).thenReturn(new HashMap<>());
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateRequestParameters(ci, null, getArgs.getQuery(), new HashMap<>())).thenReturn(evaluatedParameters);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        getStep.execute(ci);

        verify(configurationService, times(1)).execute("default-action2", "POST", null, null, new HashMap<>(), null);
    }

    @Test
    void execute_shouldNotExecuteDefaultActionWhenRequestIsInvalidButDefaultActionIsNotDefined() {
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(ci.getContext()).thenReturn(new HashMap<>());
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateRequestParameters(ci, null, getArgs.getQuery(), new HashMap<>())).thenReturn(evaluatedParameters);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        getStep.execute(ci);

        verify(configurationService, times(0)).execute(anyString(), anyString(), anyMap(), anyMap(), anyMap(), anyString());
    }
}
