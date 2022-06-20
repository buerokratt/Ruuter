package ee.buerokratt.ruuter;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BaseTest {
    @Mock
    protected final Tracer tracer = Mockito.mock(Tracer.class);

    @Mock
    protected final Span span = Mockito.mock(Span.class);

    @BeforeEach
    protected void mockTracer() {
        when(tracer.nextSpan()).thenReturn(span);
        when(span.name(any())).thenReturn(span);
    }
}
