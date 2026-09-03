package ee.buerokratt.ruuter;

import ee.buerokratt.ruuter.domain.DslInstance;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class StepTestBase {

    @Mock
    protected Tracer tracer;

    @Mock
    protected SpanBuilder spanBuilder;

    @Mock
    protected Span span;

    @Mock
    protected DslInstance di;

    @BeforeEach
    protected void mockTracer() {
        // lenient: some subclasses mix trivial getter-only tests (which never call step.execute(),
        // so never touch the tracer at all) with real execution tests in the same class - strict
        // stubbing would flag this setup as unused for the former.
        lenient().when(di.getTracer()).thenReturn(tracer);
        lenient().when(tracer.spanBuilder(nullable(String.class))).thenReturn(spanBuilder);
        lenient().when(spanBuilder.startSpan()).thenReturn(span);
    }
}
