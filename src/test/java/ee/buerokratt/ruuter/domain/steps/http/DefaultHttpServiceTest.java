package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.DslService;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers HttpStep.handleFailedResult()'s default-DSL-on-exception logic specifically: when a step's
 * response status isn't in the allowlist, a step-local defaultDsl (if set) takes precedence over the
 * global application.defaultDslInCaseOfException config; if neither is set, no fallback DSL runs.
 * Request-building itself (URL/query/body/header evaluation) is HttpGetStepTest/HttpPostStepTest's
 * responsibility - this only exercises the failure/fallback path.
 */
class DefaultHttpServiceTest extends StepTestBase {

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private DslService dslService;

    private final ApplicationProperties properties = new ApplicationProperties();
    private final MappingHelper mappingHelper = new MappingHelper(new ObjectMapper());
    private HttpGetStep step;

    @BeforeEach
    void setUp() {
        properties.setHttpCodesAllowList(new ArrayList<>(List.of(200))); // only 200 is allowed

        when(di.getContext()).thenReturn(new HashMap<>());
        when(di.getProperties()).thenReturn(properties);
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        lenient().when(di.getDslService()).thenReturn(dslService); // only used when a default DSL actually fires
        when(di.getRequestBody()).thenReturn(new HashMap<>());
        when(di.getRequestQuery()).thenReturn(new HashMap<>());
        when(di.getRequestHeaders()).thenReturn(new HashMap<>());

        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(scriptingHelper.evaluateScripts(anyString(), eq(di)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        HttpQueryArgs args = new HttpQueryArgs();
        args.setUrl("http://example.com/endpoint");
        step = new HttpGetStep();
        step.setName("get_message");
        step.setArgs(args);
        step.setResultName("the_response");

        lenient().when(httpHelper.doMethod(any(), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any()))
            .thenReturn(new ResponseEntity<>("boom", HttpStatus.INTERNAL_SERVER_ERROR)); // overridden by the "status allowed" test
    }

    @Test
    void execute_shouldExecuteGlobalDefaultDsl_whenResponseCodeNotAllowedAndNoLocalOverride() throws Exception {
        var globalDefault = new DefaultHttpDsl();
        globalDefault.setDsl("global-fallback");
        globalDefault.setRequestType("POST");
        properties.setDefaultDslInCaseOfException(globalDefault);

        assertThrows(StepExecutionException.class, () -> step.execute(di));

        verify(dslService).execute(eq("global-fallback"), eq("POST"), anyMap(), anyMap(), anyMap(), any());
    }

    @Test
    void execute_shouldExecuteLocalDefaultDsl_insteadOfGlobal_whenBothAreConfigured() throws Exception {
        var globalDefault = new DefaultHttpDsl();
        globalDefault.setDsl("global-fallback");
        globalDefault.setRequestType("POST");
        properties.setDefaultDslInCaseOfException(globalDefault);

        var localDefault = new DefaultHttpDsl();
        localDefault.setDsl("local-fallback");
        localDefault.setRequestType("POST");
        step.setLocalHttpExceptionDsl(localDefault);

        assertThrows(StepExecutionException.class, () -> step.execute(di));

        verify(dslService).execute(eq("local-fallback"), eq("POST"), anyMap(), anyMap(), anyMap(), any());
        verify(dslService, never()).execute(eq("global-fallback"), anyString(), anyMap(), anyMap(), anyMap(), any());
    }

    @Test
    void execute_shouldNotExecuteAnyDefaultDsl_whenNeitherLocalNorGlobalConfigured() throws Exception {
        assertThrows(StepExecutionException.class, () -> step.execute(di));

        verify(dslService, never()).execute(anyString(), anyString(), anyMap(), anyMap(), anyMap(), any());
    }

    @Test
    void execute_shouldNotExecuteDefaultDsl_whenResponseStatusIsAllowed() throws Exception {
        var globalDefault = new DefaultHttpDsl();
        globalDefault.setDsl("global-fallback");
        properties.setDefaultDslInCaseOfException(globalDefault);
        when(httpHelper.doMethod(any(), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any()))
            .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        step.execute(di);

        verify(dslService, never()).execute(anyString(), anyString(), anyMap(), anyMap(), anyMap(), any());
    }
}
