package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseStepTest;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.service.ConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WireMockTest
class HttpGetStepTest extends BaseStepTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private BiPredicate<String, String> biPredicate;

    @Mock
    private ApplicationProperties.DefaultAction defaultAction;

    @Mock
    private ConfigurationService configurationService;

    private HttpHeaders httpHeaders;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getProperties()).thenReturn(applicationProperties);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        httpHeaders = HttpHeaders.of(new HashMap<>(), biPredicate);
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

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().makeHttpGetRequest(expectedGetArgs)).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        expectedGetStep.execute(ci);

        assertEquals(200, ((HttpStepResult) testContext.get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenRequestIsInvalidAndStopInCaseOfExceptionIsTrue(WireMockRuntimeInfo wireMockRuntimeInfo) {
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

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().makeHttpGetRequest(expectedGetArgs)).thenReturn(httpResponse);
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(applicationProperties.getDefaultAction()).thenReturn(defaultAction);
        when(defaultAction.getService()).thenReturn("default-action");
        when(defaultAction.getBody()).thenReturn(new HashMap<>());
        when(defaultAction.getQuery()).thenReturn(new HashMap<>());
        when(ci.getRequestOrigin()).thenReturn("");
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        expectedGetStep.execute(ci);

        verify(configurationService, times(1)).execute(eq("default-action"), anyMap(), anyMap(), anyString());
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

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().makeHttpGetRequest(expectedGetArgs)).thenReturn(httpResponse);
        when(applicationProperties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        expectedGetStep.execute(ci);

        verify(configurationService, times(0)).execute(anyString(), anyMap(), anyMap(), anyString());
    }

}
