package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"application.config-path=/dsl-controller"})
class DslControllerIT extends BaseIntegrationTest {

    @Test
    void queryDsl_shouldGet() {
        client.get()
            .uri("/test/test-call")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value_get");
    }

    @Test
    void queryDsl_shouldPost() {
        client.post()
            .uri("/test/test-call")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("return_value_post");
    }

    @Test
    void queryDsl_shouldGetMethodNotAllowedErrorWhenInvalidMethodType() {
        client.put()
            .uri("/test/test-call")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().isEqualTo(405);
    }

    @Test
    void queryDsl_shouldSetHeadersToResponse() {
        client.get()
            .uri("/test/custom-headers")
            .exchange().expectStatus().isOk()
            // ReturnStep.addDefaultCookies fills in a "Path=/" default for any Set-Cookie header
            // that doesn't specify one - this fixture's cookie doesn't, so it's expected here too.
            .expectHeader().valueEquals("Set-Cookie", "cookieName=headerName; Domain=localhost; Secure; HttpOnly; SameSite=Strict; Max-Age=300; Expires=2022-08-08T10:08:39.159Z; Path=/;")
            .expectHeader().valueEquals("custom-header", "custom-value");
    }
}
