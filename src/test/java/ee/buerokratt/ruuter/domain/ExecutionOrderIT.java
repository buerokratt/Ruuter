package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=/dsl-domain"})
class ExecutionOrderIT extends BaseIntegrationTest {
    public static final String EXPECTED_RESULT = "expected_result";

    @Test
    void shouldExecuteInOrder() {
        client.get()
            .uri("/test/execute-in-order")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void shouldExecuteInOrderUntilEnd() {
        client.get()
            .uri("/test/execute-in-order-until-end")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void shouldExecuteAccordingToNextStep() {
        client.post()
            .uri("/test/execute-by-next")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void shouldExecuteAccordingToNextStepUntilEnd() {
        client.post()
            .uri("/test/execute-by-next-until-end")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }
}
