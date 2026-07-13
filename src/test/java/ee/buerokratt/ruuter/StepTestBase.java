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
import static org.mockito.Mockito.when;

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
        when(di.getTracer()).thenReturn(tracer);
        when(tracer.spanBuilder(nullable(String.class))).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
    }
}
