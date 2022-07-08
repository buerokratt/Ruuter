package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
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
    private ConfigurationService configurationService;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getProperties()).thenReturn(applicationProperties);
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

        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(ci.getContext()).thenReturn(testContext);
        when(httpHelper.doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        expectedGetStep.execute(ci);

        assertEquals(200, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatus());
        assertEquals(httpResponse.getBody(), ((HttpStepResult) testContext.get("the_response")).getResponse().getBody());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenRequestIsInvalidAndStopInCaseOfExceptionIsTrue(WireMockRuntimeInfo wireMockRuntimeInfo) {
        HttpDefaultService httpDefaultService = Mockito.spy(new HttpDefaultService() {{
            setService("default-action");
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
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().makeHttpGetRequest(expectedGetArgs)).thenReturn(httpResponse);
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(applicationProperties.getDefaultServiceInCaseOfException()).thenReturn(httpDefaultService);
        doCallRealMethod().when(httpDefaultService).executeHttpDefaultAction(eq(ci), anyString());
        when(ci.getRequestOrigin()).thenReturn("");
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        failingGetStep.execute(ci);
        when(ci.getRequestOrigin()).thenReturn("");
        when(applicationProperties.getDefaultAction()).thenReturn(defaultAction);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        when(defaultAction.getService()).thenReturn("default-action");
        when(defaultAction.getBody()).thenReturn(new HashMap<>());
        when(defaultAction.getQuery()).thenReturn(new HashMap<>());

        expectedGetStep.execute(ci);

        verify(configurationService, times(1)).execute(eq("default-action"), anyMap(), anyMap(), anyString());
    }

    @Test
    void execute_shouldExecuteStepSpecificDefaultActionWhenRequestIsInvalidAndStopInCaseOfExceptionIsTrue(WireMockRuntimeInfo wireMockRuntimeInfo) {
        HttpDefaultService httpDefaultService2 = Mockito.spy(new HttpDefaultService() {{
            setService("default-action2");
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
            setHttpDefaultService(httpDefaultService2);
        }};

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().makeHttpGetRequest(expectedGetArgs)).thenReturn(httpResponse);
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(ci.getRequestOrigin()).thenReturn("");
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        failingGetStep.execute(ci);

        verify(configurationService, times(1)).execute(eq("default-action2"), anyMap(), anyMap(), anyString());
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

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().doGet(expectedGetArgs.getUrl(), expectedGetArgs.getQuery(), expectedGetArgs.getHeaders())).thenReturn(httpResponse);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        expectedGetStep.execute(ci);

        verify(configurationService, times(0)).execute(anyString(), anyMap(), anyMap(), anyString());
    }
}
