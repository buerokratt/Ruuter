package ee.buerokratt.ruuter.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.Logging;
import ee.buerokratt.ruuter.domain.steps.http.HttpPostStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpQueryArgs;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.exception.DSLExecutionException;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Exercises HttpStep's displayRequestContent/displayResponseContent override logic
 * (step-level {@link Logging} settings take precedence over the global one when set).
 * HttpHelper is mocked, so no real HTTP server is needed for these.
 */
class LoggingUtilsTest extends StepTestBase {

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private ScriptingHelper scriptingHelper;

    private ApplicationProperties properties;
    private final MappingHelper mappingHelper = new MappingHelper(new ObjectMapper());

    private HttpQueryArgs postArgs;
    private HttpPostStep postStep;

    @BeforeEach
    void mockDependencies() {
        properties = new ApplicationProperties();
        properties.setHttpCodesAllowList(new ArrayList<>());

        when(di.getContext()).thenReturn(new HashMap<>());
        when(di.getProperties()).thenReturn(properties);
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getRequestBody()).thenReturn(new HashMap<>());
        when(di.getRequestQuery()).thenReturn(new HashMap<>());
        when(di.getRequestHeaders()).thenReturn(new HashMap<>());

        // none of these tests use ${...} templating, so scriptingHelper is a pure passthrough
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(scriptingHelper.evaluateScripts(anyString(), eq(di)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        postArgs = new HttpQueryArgs();
        postArgs.setUrl("http://example.com/endpoint");
        postArgs.setBody(new HashMap<>() {{
            put("some_val", "Hello World");
        }});
        postStep = new HttpPostStep();
        postStep.setName("post_message");
        postStep.setArgs(postArgs);
        postStep.setResultName("the_response");

        when(httpHelper.doMethod(any(), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any()))
            .thenReturn(new ResponseEntity<>(Map.of("key", "value"), HttpStatus.OK));
    }

    @Test
    void execute_shouldMaskBothContents_whenNoLoggingIsConfigured() throws StepExecutionException, DSLExecutionException {
        try (MockedStatic<LoggingUtils> loggingUtils = mockStatic(LoggingUtils.class)) {
            postStep.execute(di);

            loggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), any(), any(), eq("-"), eq("-"), any()), times(1));
        }
    }

    @Test
    void execute_shouldShowBothContents_whenGlobalSettingEnablesThem() throws StepExecutionException, DSLExecutionException {
        properties.setLogging(new Logging(true, true, false, false));

        try (MockedStatic<LoggingUtils> loggingUtils = mockStatic(LoggingUtils.class)) {
            postStep.execute(di);

            loggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), any(), any(),
                argThat(requestContent -> requestContent.contains("Hello World")),
                argThat(responseContent -> responseContent.contains("value")),
                any()), times(1));
        }
    }

    @Test
    void execute_shouldMaskBothContents_whenStepLevelSettingOverridesGlobalTrueToFalse() throws StepExecutionException, DSLExecutionException {
        properties.setLogging(new Logging(true, true, false, false));
        postStep.setLogging(new Logging(false, false, false, false));

        try (MockedStatic<LoggingUtils> loggingUtils = mockStatic(LoggingUtils.class)) {
            postStep.execute(di);

            loggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), any(), any(), eq("-"), eq("-"), any()), times(1));
        }
    }

    @Test
    void execute_shouldShowBothContents_whenStepLevelSettingOverridesGlobalFalseToTrue() throws StepExecutionException, DSLExecutionException {
        properties.setLogging(new Logging(false, false, false, false));
        postStep.setLogging(new Logging(true, true, false, false));

        try (MockedStatic<LoggingUtils> loggingUtils = mockStatic(LoggingUtils.class)) {
            postStep.execute(di);

            loggingUtils.verify(() -> LoggingUtils.logStep(any(), any(), any(), any(), any(),
                argThat(requestContent -> requestContent.contains("Hello World")),
                argThat(responseContent -> responseContent.contains("value")),
                any()), times(1));
        }
    }
}
