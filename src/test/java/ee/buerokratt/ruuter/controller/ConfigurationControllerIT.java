package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/controller"})
class ConfigurationControllerIT extends BaseIntegrationTest {

    @Test
    void queryConfiguration_shouldGet() {
        client.get()
            .uri("/test-call")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value_get");
    }

    @Test
    void queryConfiguration_shouldPost() {
        client.post()
            .uri("/test-call")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value_post");
    }

    @Test
    void queryConfiguration_shouldGetMethodNotAllowedErrorWhenInvalidMethodType() {
        client.put()
            .uri("/test-call")
            .exchange().expectStatus().isEqualTo(405);
    }

    @Test
    void queryConfiguration_shouldSetHeadersToResponse() {
        client.get()
            .uri("/custom-headers")
            .exchange().expectStatus().isOk()
            .expectHeader().valueEquals("Set-Cookie", "cookieName=headerName; Domain=localhost; Secure; HttpOnly; SameSite=Strict; Max-Age=300; Expires=2022-08-08T10:08:39.159Z;")
            .expectHeader().valueEquals("custom-header", "custom-value");
    }
}
