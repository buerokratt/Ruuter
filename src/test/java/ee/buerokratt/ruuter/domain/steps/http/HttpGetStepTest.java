package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.service.DslService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private DslService dslService;

    @BeforeEach
    protected void mockDependencies() {
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getProperties()).thenReturn(applicationProperties);
    }

    @Test
    void execute_shouldQueryEndpointAndStoreResponse(WireMockRuntimeInfo wireMockRuntimeInfo) {
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
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.OK);

        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getContext()).thenReturn(testContext);
        when(httpHelper.doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        expectedGetStep.execute(di);

        assertEquals(HttpStatus.OK, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatusCode());
        assertEquals(httpResponse.getBody(), ((HttpStepResult) testContext.get("the_response")).getResponse().getBody());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenRequestIsInvalidAndStopInCaseOfExceptionIsTrue(WireMockRuntimeInfo wireMockRuntimeInfo) {
        DefaultHttpDsl defaultHttpDsl = Mockito.spy(new DefaultHttpDsl() {{
            setDsl("default-action");
            setRequestType("POST");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }});
        HashMap<String, Object> testContext = new HashMap<>();
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};
        HttpStep failingGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(httpHelper.doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        when(di.getDslService()).thenReturn(dslService);
        when(di.getContext()).thenReturn(testContext);
        when(di.getRequestOrigin()).thenReturn("");
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        when(applicationProperties.getDefaultDslInCaseOfException()).thenReturn(defaultHttpDsl);
        failingGetStep.execute(di);

        verify(dslService, times(1)).execute(eq("default-action"), anyString(), anyMap(), anyMap(), anyString());
    }

    @Test
    void execute_shouldExecuteStepSpecificDefaultActionWhenRequestIsInvalidAndStopInCaseOfExceptionIsTrue(WireMockRuntimeInfo wireMockRuntimeInfo) {
        DefaultHttpDsl defaultHttpDsl2 = Mockito.spy(new DefaultHttpDsl() {{
            setDsl("default-action2");
            setRequestType("POST");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }});
        HashMap<String, Object> testContext = new HashMap<>();
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};
        HttpStep failingGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
            setLocalHttpExceptionDsl(defaultHttpDsl2);
        }};
        ResponseEntity<Object> httpResponse = new ResponseEntity<>("body", null, HttpStatus.CREATED);

        when(httpHelper.doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        when(di.getDslService()).thenReturn(dslService);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getContext()).thenReturn(testContext);
        when(di.getRequestOrigin()).thenReturn("");
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        failingGetStep.execute(di);

        verify(dslService, times(1)).execute(eq("default-action2"), eq("POST"), anyMap(), anyMap(), anyString());
    }

    @Test
    void execute_shouldNotExecuteDefaultActionWhenRequestIsInvalidButDefaultActionIsNotDefined(WireMockRuntimeInfo wireMockRuntimeInfo) {
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

        when(di.getContext()).thenReturn(testContext);
        when(di.getHttpHelper().doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});
        expectedGetStep.execute(di);

        verify(dslService, times(0)).execute(anyString(), anyString(), anyMap(), anyMap(), anyString());
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

        when(di.getContext()).thenReturn(testContext);
        when(di.getProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getStopInCaseOfException()).thenReturn(true);
        when(di.getHttpHelper().doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(HttpStatus.OK.value());}});

        assertThrows(IllegalArgumentException.class, () -> expectedGetStep.execute(di));
    }
}
