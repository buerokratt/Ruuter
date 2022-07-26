package ee.buerokratt.ruuter.configuration;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/configuration", "application.finalResponse.dslWithResponseHttpStatusCode=201", "application.finalResponse.dslWithoutResponseHttpStatusCode=202"})
class FinalResponseStatusCodeIT extends BaseIntegrationTest {

    @Test
    void queryDsl_shouldSetFinalResponseStatusCodeToRuuterResponse() {
        client.post()
            .uri("/test-call")
            .exchange().expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value_post");
    }

    @Test
    void queryDsl_shouldSetFinalResponseStatusBasedOnDSLStatusValue() {
        client.get()
            .uri("/test-status")
            .exchange().expectStatus().isEqualTo(HttpStatus.ACCEPTED)
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value_get");
    }

    @Test
    void queryConfiguration_shouldSetFinalResponseStatusCodeWhenResponseIsMissing() {
        client.get()
            .uri("/without-return")
            .exchange().expectStatus().isAccepted()
            .expectBody()
            .jsonPath("$.response")
            .isEmpty();
    }
}
