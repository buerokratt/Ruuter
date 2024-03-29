package ee.buerokratt.ruuter.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort = 8090)
@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/domain", "application.finalResponse.dslWithoutResponseHttpStatusCode=500", "application.maxStepRecursions=4"})
class DslInstanceIT extends BaseIntegrationTest {
    public static final String EXPECTED_RESULT = "expected_result";

    @Test
    void execute_shouldReturnNullWhenExceptionEncounteredAndReturnStatusCodeDefinedInSettings() {
        client.get()
            .uri("/incorrect-next-value")
            .exchange().expectStatus().is5xxServerError()
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
    void execute_shouldSkipStepsWhereSkipIsTrue() {
        client.get()
            .uri("/skip-true")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void execute_shouldNotSkipStepsWhereSkipIsFalse() {
        client.get()
            .uri("/skip-false")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void execute_shouldGetAndReturnValue() {
        String result = "expected_result";
        stubFor(get("/endpoint").willReturn(ok().withBody("\"%s\"".formatted(result)).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        client.get()
            .uri("/get-and-return")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$..request.url")
            .isEqualTo("http://localhost:8090/endpoint")
            .jsonPath("$..response.body")
            .isEqualTo(result)
            .jsonPath("$..response.statusCodeValue")
            .isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void execute_shouldCallTemplate() {
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("element1", "Byrokratt");
        postBody.put("element3", "- 4 More Years");

        client.post()
            .uri("/call-template?element2=2021")
            .bodyValue(postBody)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("Byrokratt v2.0 since 2021 - 4 More Years");
    }

    @Test
    void execute_shouldCallPostWithMappedBody() throws JsonProcessingException {
        String expectedMappedValue = "expected mapped value";
        HashMap<String, String> expectedPostBody = new HashMap<>();
        expectedPostBody.put("mappedValue", expectedMappedValue);

        stubFor(post("/endpoint").withRequestBody(equalToJson(new ObjectMapper().writeValueAsString(expectedPostBody)))
            .willReturn(ok().withBody("\"%s\"".formatted(expectedMappedValue)).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        client.get()
            .uri("/post-mapped-value")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$..response.body")
            .isEqualTo(expectedMappedValue);
    }

    @Test
    void execute_shouldExecuteStepFourTimesBecauseGlobalLimitOverridesStepSpecificWhenStepSpecificIsBigger() {
        client.get()
            .uri("/max-recursions-bigger")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("testtesttesttest");
    }

    @Test
    void execute_shouldExecuteStepTwoTimesBecauseStepSpecificOverridesGlobalLimitWhenStepSpecificIsSmaller() {
        client.get()
            .uri("/max-recursions-smaller")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("testtest");
    }

    @Test
    void execute_shouldExecuteTwoStepsFourTimesWhenMaxRecursionsIsDefinedAsFourInGlobalLevel() {
        client.get()
            .uri("/global-max-recursions")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step2step3step2step3step2step3");
    }

    @Test
    void execute_shouldExecuteEachStepTheAmountOfTimesDefinedForEachStep() {
        client.get()
            .uri("/step-specific-max-recursions")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step2step3step3");
    }

    @Test
    void execute_shouldExecuteEachStepTheAmountOfTimesDefinedForEachStepAndNextStepNameIsNotNecessary() {
        client.get()
            .uri("/max-recursions-without-next-step-name")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step4step2step3step4step2step3step3");
    }

    @Test
    void execute_shouldExecuteDslWithMultipleLoops() {
        client.get()
            .uri("/max-recursions-multiple-loops")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step2step3step3step3step4step5step4step5step5step6step7step8step6step7step8step6step8step6");
    }
}
