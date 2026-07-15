package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.StepTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * DeclarationStep only exposes @Getter (no setters) and AllowList is a non-static inner class, so
 * this test lives in the same package to set fields directly - matching how OpenApiBuilderTest (a
 * different package) instead had to build these via real YAML deserialization.
 */
class DeclarationStepTest extends StepTestBase {

    @Test
    void getType_shouldReturnDeclare() {
        assertEquals("declare", new DeclarationStep().getType());
    }

    @Test
    void execute_shouldBeNoOp() {
        DeclarationStep step = new DeclarationStep();
        step.setName("declaration");

        assertDoesNotThrow(() -> step.execute(di));
    }

    @Test
    void getAllowedBody_shouldReturnNull_whenNoAllowlistSet() {
        DeclarationStep step = new DeclarationStep();

        assertNull(step.getAllowedBody());
    }

    @Test
    void getAllowedBody_shouldDeriveFieldNamesFromAllowlist() {
        DeclarationStep step = new DeclarationStep();
        DeclarationStep.AllowList allowlist = step.new AllowList();
        allowlist.body = List.of(
            new DslField("name", "string", "the resource name"),
            new DslField("age", "integer", "the resource age"));
        step.allowlist = allowlist;

        assertEquals(List.of("name", "age"), step.getAllowedBody());
    }

    @Test
    void getAllowedBody_shouldReturnExplicitlySetValue_withoutConsultingAllowlist() {
        DeclarationStep step = new DeclarationStep();
        step.allowedBody = List.of("already-resolved-field");
        DeclarationStep.AllowList allowlist = step.new AllowList();
        allowlist.body = List.of(new DslField("should-not-be-used", "string", null));
        step.allowlist = allowlist;

        assertEquals(List.of("already-resolved-field"), step.getAllowedBody());
    }

    @Test
    void getAllowedHeader_shouldDeriveFieldNamesFromAllowlist() {
        DeclarationStep step = new DeclarationStep();
        DeclarationStep.AllowList allowlist = step.new AllowList();
        allowlist.header = List.of(new DslField("Authorization", "string", "auth header"));
        step.allowlist = allowlist;

        assertEquals(List.of("Authorization"), step.getAllowedHeader());
    }

    @Test
    void getAllowedParams_shouldDeriveFieldNamesFromAllowlist() {
        DeclarationStep step = new DeclarationStep();
        DeclarationStep.AllowList allowlist = step.new AllowList();
        allowlist.params = List.of(new DslField("id", "string", "the resource id"));
        step.allowlist = allowlist;

        assertEquals(List.of("id"), step.getAllowedParams());
    }
}
