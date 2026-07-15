package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.service.exception.DSLExecutionException;
import ee.buerokratt.ruuter.service.exception.StepExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

class InlineStepTest extends StepTestBase {

    private final ApplicationProperties properties = new ApplicationProperties();
    private Map<String, Object> context;

    @BeforeEach
    void setUp() {
        context = new HashMap<>();
        // unused by the trivial getType() test, which never calls step.execute()
        lenient().when(di.getContext()).thenReturn(context);
        // only reached by the "extAuth" scenarios, not the "unrecognized routine" one
        lenient().when(di.getProperties()).thenReturn(properties);
    }

    @Test
    void getType_shouldReturnInline() {
        assertEquals("inline", new InlineStep().getType());
    }

    @Test
    void execute_shouldStoreTrue_whenExtAuthAndNameIsInAllowedList() throws StepExecutionException, DSLExecutionException {
        properties.setExternalAuthAllowed(List.of("trusted-service"));
        when(di.getName()).thenReturn("trusted-service");

        InlineStep step = new InlineStep();
        step.setName("check_auth");
        step.setInline("extAuth");
        step.setResultName("is_authorized");

        step.execute(di);

        assertEquals(true, context.get("is_authorized"));
    }

    @Test
    void execute_shouldStoreFalse_whenExtAuthAndNameIsNotInAllowedList() throws StepExecutionException, DSLExecutionException {
        properties.setExternalAuthAllowed(List.of("trusted-service"));
        when(di.getName()).thenReturn("untrusted-service");

        InlineStep step = new InlineStep();
        step.setName("check_auth");
        step.setInline("extAuth");
        step.setResultName("is_authorized");

        step.execute(di);

        assertEquals(false, context.get("is_authorized"));
    }

    @Test
    void execute_shouldStoreFalse_whenExtAuthAndAllowedListIsNotConfigured() throws StepExecutionException, DSLExecutionException {
        properties.setExternalAuthAllowed(null);
        when(di.getName()).thenReturn("any-service");

        InlineStep step = new InlineStep();
        step.setName("check_auth");
        step.setInline("extAuth");
        step.setResultName("is_authorized");

        step.execute(di);

        assertFalse((Boolean) context.get("is_authorized"));
    }

    @Test
    void execute_shouldStoreNull_whenInlineRoutineIsUnrecognized() throws StepExecutionException, DSLExecutionException {
        InlineStep step = new InlineStep();
        step.setName("mystery_routine");
        step.setInline("somethingElse");
        step.setResultName("the_result");

        step.execute(di);

        assertNull(context.get("the_result"));
        assertEquals(true, context.containsKey("the_result"));
    }
}
