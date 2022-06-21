package ee.buerokratt.ruuter;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BaseTest {

    @Mock
    protected Tracer tracer;

    @Mock
    protected Span span;

    @Mock
    protected ScriptingHelper scriptingHelper;

    @Mock
    protected ApplicationProperties applicationProperties;

    @BeforeEach
    protected void mockTracer() {
        when(tracer.nextSpan()).thenReturn(span);
        when(span.name(any())).thenReturn(span);
    }

}
