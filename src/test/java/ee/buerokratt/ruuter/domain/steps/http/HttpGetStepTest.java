package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.DslService;
import ee.buerokratt.ruuter.service.exception.DSLExecutionException;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpGetStepTest extends StepTestBase {

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private DslService dslService;

    private final ApplicationProperties properties = new ApplicationProperties();
    private final MappingHelper mappingHelper = new MappingHelper(new ObjectMapper());
    private Map<String, Object> context;
    private HttpQueryArgs args;
    private HttpGetStep step;

    @BeforeEach
    void setUp() {
        properties.setHttpCodesAllowList(new ArrayList<>()); // empty = allow all statuses
        context = new HashMap<>();

        when(di.getContext()).thenReturn(context);
        when(di.getProperties()).thenReturn(properties);
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        lenient().when(di.getDslService()).thenReturn(dslService); // only exercised by the default-DSL-on-exception tests
        when(di.getRequestBody()).thenReturn(new HashMap<>());
        when(di.getRequestQuery()).thenReturn(new HashMap<>());
        when(di.getRequestHeaders()).thenReturn(new HashMap<>());

        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        // only reached by HttpStep.logStep(), which only runs on the success path - not exercised
        // by the "throws before reaching logStep" scenario
        lenient().when(scriptingHelper.evaluateScripts(anyString(), eq(di)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        args = new HttpQueryArgs();
        args.setUrl("http://example.com/endpoint");
        step = new HttpGetStep();
        step.setName("get_message");
        step.setArgs(args);
        step.setResultName("the_response");
    }

    private void stubResponse(HttpStatus status, Object body) {
        when(httpHelper.doMethod(any(), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any()))
            .thenReturn(new ResponseEntity<>(body, status));
    }

    @Test
    void execute_shouldCallHttpHelperWithGetMethodAndEvaluatedUrl() throws StepExecutionException, DSLExecutionException {
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.GET), eq("http://example.com/endpoint"), anyMap(),
            isNull(), anyMap(), isNull(), isNull(), any(), eq(di), eq(false), eq(true), any());
    }

    @Test
    void execute_shouldStoreResponseInContextAsHttpStepResult() throws StepExecutionException, DSLExecutionException {
        ResponseEntity<Object> response = new ResponseEntity<>("body", HttpStatus.OK);
        when(httpHelper.doMethod(any(), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any()))
            .thenReturn(response);

        step.execute(di);

        HttpStepResult result = (HttpStepResult) context.get("the_response");
        assertNotNull(result);
        assertEquals(response, result.getResponse());
        assertEquals(args, result.getRequest());
    }

    @Test
    void execute_shouldPassEvaluatedQueryParamsThrough() throws StepExecutionException, DSLExecutionException {
        // mapPossibleScriptedObject only recognizes LinkedHashMap (matching how Jackson deserializes
        // a YAML mapping) as "already resolved" data - a plain HashMap here would silently be
        // treated as empty.
        args.setQuery(new LinkedHashMap<>() {{
            put("q", "value");
        }});
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.GET), anyString(), eq(Map.of("q", "value")),
            isNull(), anyMap(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any());
    }

    @Test
    void execute_shouldGoToErrorStep_whenResponseStatusNotAllowedAndOnErrorStepIsSet() throws StepExecutionException, DSLExecutionException {
        properties.setHttpCodesAllowList(new ArrayList<>(List.of(200)));
        step.setOnErrorStep("fallback_step");
        stubResponse(HttpStatus.INTERNAL_SERVER_ERROR, "boom");

        step.execute(di);

        verify(di).setGotoStep("fallback_step");
    }

    @Test
    void execute_shouldThrow_whenResponseStatusNotAllowedAndNoOnErrorStep() {
        properties.setHttpCodesAllowList(new ArrayList<>(List.of(200)));
        stubResponse(HttpStatus.NOT_FOUND, "boom");

        assertThrows(StepExecutionException.class, () -> step.execute(di));

        // HttpStep.executeStepAction sets the actual upstream status first, but DslStep.execute's
        // generic catch-all then unconditionally overwrites it with INTERNAL_SERVER_ERROR - the
        // more specific status set here never actually survives to the final DslInstance state.
        verify(di).setErrorStatus(HttpStatus.NOT_FOUND);
        verify(di).setErrorStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
