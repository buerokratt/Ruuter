package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/controller"})
class ConfigurationControllerIT extends BaseIntegrationTest {

    @Test
    void getRoute_shouldGet() {
        client.get()
            .uri("/test-call")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value");
    }

    @Test
    void getRoute_shouldPost() {
        client.post()
            .uri("/test-call")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value");
    }

    @Test
    void getRoute_shouldGetMethodNotAllowedErrorWhenInvalidMethodType() {
        client.put()
            .uri("/test-call")
            .exchange().expectStatus().isEqualTo(405);
    }

}
