package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/domain"})
class ExecutionOrderIT extends BaseIntegrationTest {

    public static final String EXPECTED_RESULT = "expected_result";

    @Test
    void shouldExecuteInOrder() {
        client.get()
            .uri("/execute-in-order")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void shouldExecuteInOrderUntilEnd() {
        client.get()
            .uri("/execute-in-order-until-end")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void shouldExecuteAccordingToNextStep() {
        client.post()
            .uri("/execute-by-next")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void shouldExecuteAccordingToNextStepUntilEnd() {
        client.post()
            .uri("/execute-by-next-until-end")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }
}
