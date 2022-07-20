package ee.buerokratt.ruuter.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.Logging;
import ee.buerokratt.ruuter.domain.steps.http.HttpGetStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpPostStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpQueryArgs;
import ee.buerokratt.ruuter.domain.steps.http.HttpStep;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@WireMockTest
class LoggingUtilsTest extends StepTestBase {

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private ApplicationProperties.HttpPost httpPost;

    private HashMap<String, Object> testContext;
    private HttpQueryArgs postArgs;
    private HttpStep postStep;
    private HttpQueryArgs getArgs;
    private HttpStep getStep;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getContext()).thenReturn(testContext);
        when(ci.getProperties()).thenReturn(properties);
        when(ci.getHttpHelper()).thenReturn(httpHelper);
    }

    @BeforeEach
    protected void initializeObjects(WireMockRuntimeInfo wireMockRuntimeInfo) {
        testContext = new HashMap<>();
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
    }

    @Test
    void execute_shouldLogOutResponseContentWhenItIsTrueInSettings() {
        Logging globalLogging = new Logging(false, true);
        ResponseEntity<Object> httpResponse = new ResponseEntity<>(3, null, HttpStatus.OK);
        mappingHelper = new MappingHelper(new ObjectMapper());

        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doGet(getArgs.getUrl(), getArgs.getQuery(), getArgs.getHeaders())).thenReturn(httpResponse);
        when(properties.getLogging()).thenReturn(globalLogging);

        try (MockedStatic<LoggingUtils> mockedLoggingUtils = mockStatic(LoggingUtils.class)) {
            getStep.execute(ci);
            mockedLoggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), anyLong(), anyString(), eq("-"), eq(Objects.requireNonNull(httpResponse.getBody()).toString()), anyString()), times(1));
        }
    }

    @Test
    void execute_shouldLogOutResponseContentAndRequestContentWhenBothTrueInSettings() {
        Logging globalLogging = new Logging(true, true);
        ResponseEntity<Object> httpResponse = new ResponseEntity<>(3, null, HttpStatus.OK);
        mappingHelper = new MappingHelper(new ObjectMapper());

        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), postArgs.getQuery(), postArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(postArgs.getBody());
        when(properties.getLogging()).thenReturn(globalLogging);
        when(properties.getHttpPost()).thenReturn(httpPost);

        try(MockedStatic<LoggingUtils> mockedLoggingUtils = mockStatic(LoggingUtils.class)) {
            postStep.execute(ci);
            mockedLoggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), anyLong(), anyString(), eq(postArgs.getBody().toString()), eq(Objects.requireNonNull(httpResponse.getBody()).toString()), anyString()), times(1));
        }
    }

    @Test
    void execute_shouldNotLogResponseAndRequestContentWhenStepBasedValuesAreFalse() {
        Logging stepLogging = new Logging(false, false);
        postStep.setLogging(stepLogging);
        ResponseEntity<Object> httpResponse = new ResponseEntity<>(3, null, HttpStatus.OK);
        mappingHelper = new MappingHelper(new ObjectMapper());

        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), postArgs.getQuery(), postArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(postArgs.getBody());
        when(properties.getHttpPost()).thenReturn(httpPost);

        try(MockedStatic<LoggingUtils> mockedLoggingUtils = mockStatic(LoggingUtils.class)) {
            postStep.execute(ci);
            mockedLoggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), anyLong(), anyString(), eq("-"), eq("-"), anyString()), times(1));
        }
    }

    @Test
    void execute_shouldLogResponseAndRequestContentWhenStepBasedValuesAreTrue() {
        Logging stepLogging = new Logging(true, true);
        postStep.setLogging(stepLogging);
        ResponseEntity<Object> httpResponse = new ResponseEntity<>(3, null, HttpStatus.OK);
        mappingHelper = new MappingHelper(new ObjectMapper());

        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(httpHelper.doPost(postArgs.getUrl(), postArgs.getBody(), postArgs.getQuery(), postArgs.getHeaders())).thenReturn(httpResponse);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(postArgs.getBody());
        when(properties.getHttpPost()).thenReturn(httpPost);

        try(MockedStatic<LoggingUtils> mockedLoggingUtils = mockStatic(LoggingUtils.class)) {
            postStep.execute(ci);
            mockedLoggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), anyLong(), anyString(), eq(postArgs.getBody().toString()), eq(Objects.requireNonNull(httpResponse.getBody()).toString()), anyString()), times(1));
        }
    }
}
