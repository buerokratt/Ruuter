package ee.buerokratt.ruuter.domain;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort = 8090)
@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/domain"})
class ConfigurationInstanceIT extends BaseIntegrationTest {

    @Test
    void execute_shouldReturnNullWhenExceptionEncountered() {
        client.get()
            .uri("/incorrect-next-value")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEmpty();
    }

    @Test
    void execute_shouldAssignAndReturnMappedValue() {
        client.get()
            .uri("/assign-and-map")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("Bürokratt v2.0 since 2021");
    }

    @Test
    void execute_shouldGetAndReturnValue() {
        String result = "expected_result";
        stubFor(get("/endpoint").willReturn(ok().withBody("\"%s\"".formatted(result))));

        client.get()
            .uri("/get-and-return")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$..request.url")
            .isEqualTo("http://localhost:8090/endpoint")
            .jsonPath("$..response.body")
            .isEqualTo(result)
            .jsonPath("$..response.status")
            .isEqualTo(200);
    }

}
