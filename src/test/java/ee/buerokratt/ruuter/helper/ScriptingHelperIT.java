package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/helper"})
class ScriptingHelperIT extends BaseIntegrationTest {

    @Test
    void shouldConcatenateStrings() {
        client.get()
            .uri("/string-concatenation")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("Bürokratt www.kratid.ee");
    }

    @Test
    void shouldCalculateWithIntegers() {
        client.get()
            .uri("/integer-addition")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(4);
    }

    @Test
    void shouldEvaluateBooleanValue() {
        client.get()
            .uri("/boolean-or-evaluation")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(true);
    }

    @Test
    void shouldEvaluateBooleanValue2() {
        client.get()
            .uri("/boolean-and-evaluation")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(false);
    }
}
