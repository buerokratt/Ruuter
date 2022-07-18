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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WireMockTest
class HttpGetStepTest extends StepTestBase {

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
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
        when(ci.getProperties()).thenReturn(properties);
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
        testContext = new HashMap<>();
    }

    @Test
    void execute_shouldQueryEndpointAndStoreResponse() {
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(ci.getContext()).thenReturn(testContext);
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        getStep.execute(ci);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());
        assertEquals(httpResponse.getBody(), ((HttpStepResult) testContext.get("the_response")).getResponse().getBody());
    }

    @Test
    void execute_shouldThrowErrorWhenUrlIsInvalid(WireMockRuntimeInfo wireMockRuntimeInfo) {
        getStep.getArgs().setUrl("http://notFounUrl:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));

        when(ci.getContext()).thenReturn(testContext);
        when(scriptingHelper.evaluateScripts(getArgs.getQuery(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())).thenReturn(getArgs.getQuery());
        when(properties.getStopInCaseOfException()).thenReturn(true);
        doCallRealMethod().when(httpHelper).doGet(getArgs.getUrl(), getArgs.getQuery(), new HashMap<>());

        assertThrows(IllegalArgumentException.class, () -> getStep.execute(ci));
    }

    @Test
    void execute_shouldThrowIllegalArgumentExceptionWhenHttpStatusCodeIsNotinWhitelist(WireMockRuntimeInfo wireMockRuntimeInfo) {
        HashMap<String, Object> testContext = new HashMap<>();
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};
        HttpStep expectedGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getStopInCaseOfException()).thenReturn(true);
        when(ci.getHttpHelper().doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});

        assertThrows(IllegalArgumentException.class, () -> expectedGetStep.execute(ci));
    }
}
