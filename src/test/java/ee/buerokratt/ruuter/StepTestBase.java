package ee.buerokratt.ruuter;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StepTestBase {

    @Mock
    protected Tracer tracer;

    @Mock
    protected Span span;

    @Mock
    protected ConfigurationInstance ci;

    @BeforeEach
    protected void mockTracer() {
        when(ci.getTracer()).thenReturn(tracer);
        when(tracer.nextSpan()).thenReturn(span);
        when(span.name(any())).thenReturn(span);
    }
}
