package ee.buerokratt.ruuter.configuration;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/controller", "application.finalResponse.httpStatusCode=201"})
class FinalResponseStatusCodeIT extends BaseIntegrationTest {

    @Test
    void queryConfiguration_shouldSetFinalResponseStatusCodeToRuuterResponse() {
        client.post()
            .uri("/test-call")
            .exchange().expectStatus().isEqualTo(HttpStatus.CREATED)
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value_post");
    }
}
