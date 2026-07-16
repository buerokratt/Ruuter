package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.exception.DSLExecutionException;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HttpDeleteStep only overrides getType()/getMethod() - all request-building logic is inherited
 * from HttpPostStep (see HttpPostStepTest, currently non-functional, for that logic's own coverage).
 * This test focuses on what's actually specific to this class: the DELETE method is genuinely used
 * end-to-end when the step executes. Doesn't extend StepTestBase since two of these three tests
 * don't touch di/tracer at all, and MockitoExtension's strict stubs would flag that setup as unused.
 */
@ExtendWith(MockitoExtension.class)
class HttpDeleteStepTest {

    @Test
    void getType_shouldReturnHttpDelete() {
        assertEquals("http.delete", new HttpDeleteStep().getType());
    }

    @Test
    void getMethod_shouldReturnDelete() {
        assertEquals(HttpMethod.DELETE, new HttpDeleteStep().getMethod());
    }

    @Test
    void execute_shouldCallHttpHelperWithDeleteMethod() throws StepExecutionException, DSLExecutionException {
        DslInstance di = mock(DslInstance.class);
        Tracer tracer = mock(Tracer.class);
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        Span span = mock(Span.class);
        ScriptingHelper scriptingHelper = mock(ScriptingHelper.class);
        HttpHelper httpHelper = mock(HttpHelper.class);
        ApplicationProperties properties = new ApplicationProperties();
        properties.setHttpCodesAllowList(new ArrayList<>());
        MappingHelper mappingHelper = new MappingHelper(new ObjectMapper());

        when(di.getTracer()).thenReturn(tracer);
        when(tracer.spanBuilder(nullable(String.class))).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(di.getContext()).thenReturn(new HashMap<>());
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
        when(scriptingHelper.evaluateScripts(anyString(), eq(di)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(httpHelper.doMethod(any(), anyString(), any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any()))
            .thenReturn(new ResponseEntity<>("deleted", HttpStatus.OK));

        HttpQueryArgs args = new HttpQueryArgs();
        args.setUrl("http://example.com/resource/123");
        HttpDeleteStep step = new HttpDeleteStep();
        step.setName("delete_resource");
        step.setArgs(args);
        step.setResultName("the_response");

        step.execute(di);

        verify(httpHelper).doMethod(eq(HttpMethod.DELETE), eq("http://example.com/resource/123"),
            any(), any(), any(), any(), any(), any(), eq(di), anyBoolean(), anyBoolean(), any());
    }
}
