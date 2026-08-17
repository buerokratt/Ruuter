package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpPostStepTest extends StepTestBase {

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private HttpHelper httpHelper;

    private final ApplicationProperties properties = new ApplicationProperties();
    private final MappingHelper mappingHelper = new MappingHelper(new ObjectMapper());
    private Map<String, Object> context;
    private HttpQueryArgs args;
    private HttpPostStep step;

    @BeforeEach
    void setUp() {
        properties.setHttpCodesAllowList(new ArrayList<>()); // empty = allow all statuses
        context = new HashMap<>();

        when(di.getContext()).thenReturn(context);
        when(di.getProperties()).thenReturn(properties);
        when(di.getHttpHelper()).thenReturn(httpHelper);
        when(di.getScriptingHelper()).thenReturn(scriptingHelper);
        when(di.getMappingHelper()).thenReturn(mappingHelper);
        when(di.getRequestBody()).thenReturn(new HashMap<>());
        when(di.getRequestQuery()).thenReturn(new HashMap<>());
        when(di.getRequestHeaders()).thenReturn(new HashMap<>());

        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        // only reached by HttpStep.logStep(), which only runs on the success path
        lenient().when(scriptingHelper.evaluateScripts(anyString(), eq(di)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        args = new HttpQueryArgs();
        args.setUrl("http://example.com/endpoint");
        step = new HttpPostStep();
        step.setName("post_message");
        step.setArgs(args);
        step.setResultName("the_response");
    }

    private void stubResponse(HttpStatus status, Object body) {
        when(httpHelper.doMethod(any(), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any()))
            .thenReturn(new ResponseEntity<>(body, status));
    }

    @Test
    void execute_shouldCallHttpHelperWithPostMethodAndEvaluatedUrl() throws StepExecutionException, DSLExecutionException {
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.POST), eq("http://example.com/endpoint"), anyMap(),
            any(), anyMap(), isNull(), isNull(), any(), eq(di), eq(false), eq(true), any());
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
    }

    @Test
    void execute_shouldPassEvaluatedBodyThrough() throws StepExecutionException, DSLExecutionException {
        args.setBody(new LinkedHashMap<>() {{
            put("key", "value");
        }});
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.POST), anyString(), anyMap(), eq(Map.of("key", "value")),
            anyMap(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any());
    }

    @Test
    void execute_shouldPassDynamicBodyFlagThrough() throws StepExecutionException, DSLExecutionException {
        args.setDynamicParameters(true);
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.POST), anyString(), anyMap(), any(), anyMap(),
            any(), any(), any(), eq(di), eq(true), anyBoolean(), any());
    }

    @Test
    void execute_shouldPassPlaintextValue_whenContentTypeIsPlaintext() throws StepExecutionException, DSLExecutionException {
        args.setContentType("plaintext");
        args.setPlaintext("raw text body");
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.POST), anyString(), anyMap(), any(), anyMap(),
            eq("plaintext"), eq("raw text body"), any(), eq(di), anyBoolean(), anyBoolean(), any());
    }

    @Test
    void execute_shouldNotPassPlaintextValue_whenContentTypeIsNotPlaintext() throws StepExecutionException, DSLExecutionException {
        args.setPlaintext("should be ignored");
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.POST), anyString(), anyMap(), any(), anyMap(),
            isNull(), isNull(), any(), eq(di), anyBoolean(), anyBoolean(), any());
    }

    @Test
    void execute_shouldMergeGlobalDefaultHeaders_whenConfigured() throws StepExecutionException, DSLExecutionException {
        // args.headers must be a LinkedHashMap - HttpQueryArgs.addHeaders() routes it through
        // mapPossibleScriptedObject, which only recognizes LinkedHashMap (matching how Jackson
        // deserializes YAML) as "already resolved"; a plain HashMap comes back as an immutable
        // Map.of(), and putIfAbsent on that throws UnsupportedOperationException.
        args.setHeaders(new LinkedHashMap<>());
        ApplicationProperties.HttpPost httpPostDefaults = new ApplicationProperties.HttpPost();
        httpPostDefaults.setHeaders(new LinkedHashMap<>() {{
            put("X-Default-Header", "default-value");
        }});
        properties.setHttpPost(httpPostDefaults);
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        assertEquals("default-value", ((Map<?, ?>) step.getArgs().getHeaders()).get("X-Default-Header"));
    }

    @Test
    void execute_shouldNotAttemptHeaderMerge_whenNoGlobalDefaultsConfigured() throws StepExecutionException, DSLExecutionException {
        // ApplicationProperties.HttpPost.headers defaults to null - this must not NPE
        stubResponse(HttpStatus.OK, "body");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.POST), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any());
    }
}
