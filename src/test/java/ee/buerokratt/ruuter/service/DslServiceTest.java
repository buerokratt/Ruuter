package ee.buerokratt.ruuter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.Dsl;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.domain.steps.ReturnStep;
import ee.buerokratt.ruuter.helper.DslMappingHelper;
import ee.buerokratt.ruuter.helper.ExternalForwardingHelper;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.exception.DSLExecutionException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DslService's constructor walks the config directory for real (getDsls/getGuards), so an empty
 * @TempDir satisfies that without needing real DSL fixtures. For execute()'s dispatch logic, the
 * private dsls/guards maps are populated directly via reflection with hand-built Dsl/DslStep trees
 * rather than real files - FileUtils.getFileNameWithPathWithoutSuffix() derives its map keys from
 * the file's *absolute* path depth, which a @TempDir fixture can't predictably reproduce.
 */
class DslServiceTest {

    private ApplicationProperties properties;
    private ScriptingHelper scriptingHelper;
    private ExternalForwardingHelper externalForwardingHelper;
    private DslService dslService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        properties = new ApplicationProperties();
        properties.setConfigPath(tempDir.toString());
        properties.setIncomingRequests(new ApplicationProperties.IncomingRequests());
        ApplicationProperties.InternalRequests internalRequests = new ApplicationProperties.InternalRequests();
        // real ArrayLists, not List.of(...) - Spring's YAML-bound config lists are ArrayLists too,
        // and unlike List.of(...) they tolerate a null argument to contains() (a missing referer
        // header is a completely normal, common case, not an edge case worth NPEing on)
        internalRequests.setAllowedIPs(new ArrayList<>(List.of("1.2.3.4")));
        internalRequests.setAllowedURLs(new ArrayList<>(List.of("http://allowed")));
        properties.setInternalRequests(internalRequests);

        scriptingHelper = mock(ScriptingHelper.class);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(scriptingHelper.evaluateScripts(anyString(), anyMap(), anyMap(), anyMap(), anyMap()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        MappingHelper mappingHelper = new MappingHelper(new ObjectMapper());
        HttpHelper httpHelper = mock(HttpHelper.class);
        Tracer tracer = mock(Tracer.class);
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        Span span = mock(Span.class);
        when(tracer.spanBuilder(nullable(String.class))).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);

        externalForwardingHelper = mock(ExternalForwardingHelper.class);

        dslService = new DslService(properties, mock(DslMappingHelper.class), scriptingHelper, tracer,
            mappingHelper, httpHelper, externalForwardingHelper, mock(OpenSearchSender.class));
    }

    @SuppressWarnings("unchecked")
    private void injectDsls(Map<String, Map<String, Map<String, Dsl>>> dsls) throws Exception {
        Field field = DslService.class.getDeclaredField("dsls");
        field.setAccessible(true);
        field.set(dslService, dsls);
    }

    @SuppressWarnings("unchecked")
    private void injectGuards(Map<String, Map<String, Map<String, Dsl>>> guards) throws Exception {
        Field field = DslService.class.getDeclaredField("guards");
        field.setAccessible(true);
        field.set(dslService, guards);
    }

    private Dsl returnStepDsl(String returnValue, Integer status) {
        ReturnStep step = new ReturnStep();
        step.setName("return_step");
        step.setReturnValue(returnValue);
        step.setStatus(status);
        Map<String, DslStep> steps = new LinkedHashMap<>();
        steps.put("return_step", step);
        return new Dsl(steps);
    }

    @Test
    void filterFields_shouldReturnSameMap_whenAllowedFieldsIsNull() {
        Map<String, Object> requestFields = new HashMap<>();
        requestFields.put("a", 1);

        Map<String, Object> result = dslService.filterFields(requestFields, null);

        assertSame(requestFields, result);
    }

    @Test
    void filterFields_shouldReturnNull_whenRequestFieldsIsNullButAllowedFieldsIsNot() {
        Map<String, Object> result = dslService.filterFields(null, List.of("a"));

        assertNull(result);
    }

    @Test
    void filterFields_shouldKeepOnlyAllowedFields() {
        Map<String, Object> requestFields = new HashMap<>();
        requestFields.put("a", 1);
        requestFields.put("b", 2);
        requestFields.put("c", 3);

        Map<String, Object> result = dslService.filterFields(requestFields, List.of("a", "c"));

        assertEquals(Map.of("a", 1, "c", 3), result);
    }

    @Test
    void filterFields_shouldExcludeFieldsWithNullValues() {
        Map<String, Object> requestFields = new HashMap<>();
        requestFields.put("a", null);
        requestFields.put("b", 2);

        Map<String, Object> result = dslService.filterFields(requestFields, List.of("a", "b"));

        assertEquals(Map.of("b", 2), result);
    }

    @Test
    void checkFields_shouldNotThrow_whenRequestedFieldsIsNull() {
        assertDoesNotThrow(() -> dslService.checkFields(new HashMap<>(), null));
    }

    @Test
    void checkFields_shouldNotThrow_whenAllRequestedFieldsArePresent() {
        Map<String, Object> requestFields = new HashMap<>();
        requestFields.put("a", 1);

        assertDoesNotThrow(() -> dslService.checkFields(requestFields, List.of("a")));
    }

    @Test
    void checkFields_shouldNotThrow_whenRequestedFieldsAreMissing() {
        assertDoesNotThrow(() -> dslService.checkFields(new HashMap<>(), List.of("missing")));
    }

    @Test
    void checkFields_shouldNotThrow_whenRequestFieldsIsNullButRequestedFieldsIsNot() {
        // DslService.execute() calls filterFields() (which can return null) then immediately passes
        // that result into checkFields() - if the original requestBody was null and the DSL declares
        // allowedBody fields, checkFields(null, [...]) is a real call pattern, not just a test artifact.
        // A null requestFields is treated as empty, so every requested field is reported as missing.
        assertDoesNotThrow(() -> dslService.checkFields(null, List.of("a")));
    }

    @Test
    void execute_shouldReturnNull_whenDslNameIsBlankAndNotFound() throws Exception {
        // an entirely unregistered "project" (dsls.get(project) == null) hits an unguarded debug-log
        // dereference at DslService.java:180 and throws NPE before ever reaching the blank-name check -
        // registering an empty method map for the project is what's needed to reach that check at all.
        injectDsls(Map.of("project", Map.of("GET", Map.of())));

        DslInstance result = dslService.execute("project", "", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "origin", "app");

        assertNull(result);
    }

    @Test
    void execute_shouldThrowMeaningfulException_whenProjectIsNotRegisteredAtAll() throws Exception {
        injectDsls(Map.of());

        DSLExecutionException exception = assertThrows(DSLExecutionException.class, () ->
            dslService.execute("unknown-project", "anything", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "origin", "app"));

        assertEquals("Unknown DSL project 'unknown-project' - check that this project directory exists under the configured DSL path",
            exception.getMessage());
    }

    @Test
    void execute_shouldThrowMeaningfulException_whenMethodIsNotRegisteredForAnExistingProject() throws Exception {
        injectDsls(Map.of("project", Map.of("POST", Map.of())));

        DSLExecutionException exception = assertThrows(DSLExecutionException.class, () ->
            dslService.execute("project", "anything", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "origin", "app"));

        assertEquals("No DSLs registered for method 'GET' in project 'project'", exception.getMessage());
    }

    @Test
    void execute_shouldRunResolvedDsl_afterExtractingTrailingPathParameter() throws Exception {
        Map<String, Dsl> methodDsls = Map.of("GET/users", returnStepDsl("found-user", null));
        injectDsls(Map.of("project", Map.of("GET", methodDsls)));

        Map<String, Object> requestQuery = new HashMap<>();
        DslInstance result = dslService.execute("project", "users/123", "GET", new HashMap<>(), requestQuery, new HashMap<>(), "origin", "app");

        assertEquals("found-user", result.getReturnValue());
        assertEquals(List.of("123"), requestQuery.get("pathParams"));
    }

    @Test
    void execute_shouldSkipMainDsl_whenGuardReturnsNonOkStatus() throws Exception {
        injectDsls(Map.of("project", Map.of("GET", Map.of("GET/secured", returnStepDsl("should-not-run", null)))));
        injectGuards(Map.of("project", Map.of("GET", Map.of("GET", returnStepDsl("denied", 403)))));

        DslInstance result = dslService.execute("project", "secured", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "origin", "app");

        assertEquals(403, result.getReturnStatus());
        assertEquals("denied", result.getReturnValue());
    }

    @Test
    void execute_shouldRunMainDsl_whenGuardReturnsOkStatus() throws Exception {
        injectDsls(Map.of("project", Map.of("GET", Map.of("GET/secured", returnStepDsl("main-ran", null)))));
        injectGuards(Map.of("project", Map.of("GET", Map.of("GET", returnStepDsl("allowed", 200)))));

        DslInstance result = dslService.execute("project", "secured", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "origin", "app");

        assertEquals("main-ran", result.getReturnValue());
    }

    @Test
    void execute_shouldSkipExecution_whenInternalDslCalledFromDisallowedOrigin() throws Exception {
        injectDsls(Map.of("project", Map.of("GET", Map.of("GET/internal-secret", returnStepDsl("internal-ran", null)))));

        DslInstance result = dslService.execute("project", "internal-secret", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "9.9.9.9", "app");

        assertNull(result.getReturnValue());
    }

    @Test
    void execute_shouldRunDsl_whenInternalDslCalledFromAllowedOrigin() throws Exception {
        injectDsls(Map.of("project", Map.of("GET", Map.of("GET/internal-secret", returnStepDsl("internal-ran", null)))));

        DslInstance result = dslService.execute("project", "internal-secret", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "1.2.3.4", "app");

        assertEquals("internal-ran", result.getReturnValue());
    }

    @Test
    void execute_shouldSkipExecution_whenExternalForwardingDisallowsTheResponse() throws Exception {
        injectDsls(Map.of("project", Map.of("GET", Map.of("GET/forwarded", returnStepDsl("should-not-run", null)))));
        when(externalForwardingHelper.shouldForwardRequest()).thenReturn(true);
        when(externalForwardingHelper.forwardRequest(any(), any(), any(), any(), any(), any()))
            .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        when(externalForwardingHelper.isAllowedForwardingResponse(anyInt())).thenReturn(false);

        DslInstance result = dslService.execute("project", "forwarded", "GET", new HashMap<>(), new HashMap<>(), new HashMap<>(), "origin", "app");

        assertNull(result.getReturnValue());
    }
}
