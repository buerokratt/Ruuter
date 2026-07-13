package ee.buerokratt.ruuter.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The DSL fixtures under src/test/resources/domain hardcode http://localhost:8090/endpoint as their
 * upstream, so the mock server here binds to that same fixed port. Uses Reactor Netty's own
 * HttpServer instead of WireMock: this project's dependency graph force-resolves Jetty to 12.x,
 * which is binary-incompatible with the Jetty 9.4 server bundled in wiremock-jre8:2.35.1 (fails with
 * NoClassDefFoundError on org.eclipse.jetty.*). reactor-netty-http is already a transitive test
 * dependency (via spring-boot-starter-webflux) and is the same library HttpHelper's WebClient runs
 * on, so there's no protocol-compatibility risk.
 */
// outboundRequests.allowedHosts: OutboundRequestGuard blocks loopback addresses by default (SSRF
// protection), but these DSL fixtures intentionally target the mock server on localhost.
@TestPropertySource(properties = {"application.config-path=/dsl-domain", "application.finalResponse.dslWithoutResponseHttpStatusCode=500", "application.maxStepRecursions=4", "application.outboundRequests.allowedHosts=localhost"})
class DslInstanceIT extends BaseIntegrationTest {
    public static final String EXPECTED_RESULT = "expected_result";

    private record RecordedRequest(String method, String body) {
    }

    private DisposableServer mockServer;
    private final List<RecordedRequest> requests = new CopyOnWriteArrayList<>();
    private volatile int responseStatus;
    private volatile String responseBody;
    private volatile String responseContentType;

    @BeforeEach
    void startMockServer() {
        responseStatus = 200;
        responseBody = "";
        responseContentType = null;
        requests.clear();

        mockServer = HttpServer.create()
            .host("localhost")
            .port(8090)
            .handle((request, response) ->
                request.receive().aggregate().asString()
                    .defaultIfEmpty("")
                    .flatMap(body -> {
                        requests.add(new RecordedRequest(request.method().name(), body));
                        response.status(responseStatus);
                        if (responseContentType != null) {
                            response.header("Content-Type", responseContentType);
                        }
                        if (responseBody.isEmpty()) {
                            return response.send().then();
                        }
                        return response.sendString(Mono.just(responseBody)).then();
                    }))
            .bindNow();
    }

    @AfterEach
    void stopMockServer() {
        mockServer.disposeNow();
    }

    private void stub(String jsonBody) {
        responseBody = jsonBody;
        responseContentType = "application/json";
    }

    @Test
    void execute_shouldReturnNullWhenExceptionEncounteredAndReturnStatusCodeDefinedInSettings() {
        client.get()
            .uri("/test/incorrect-next-value")
            .exchange().expectStatus().is5xxServerError()
            .expectBody()
            .isEmpty();
    }

    @Test
    void execute_shouldAssignAndReturnMappedValue() {
        client.get()
            .uri("/test/assign-and-map")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("Bürokratt v2.0 since 2021");
    }

    @Test
    void execute_shouldSkipStepsWhereSkipIsTrue() {
        client.get()
            .uri("/test/skip-true")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void execute_shouldNotSkipStepsWhereSkipIsFalse() {
        client.get()
            .uri("/test/skip-false")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void execute_shouldGetAndReturnValue() {
        stub("\"%s\"".formatted(EXPECTED_RESULT));

        client.get()
            .uri("/test/get-and-return")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$..request.url")
            .isEqualTo("http://localhost:8090/endpoint")
            .jsonPath("$..response.body")
            .isEqualTo(EXPECTED_RESULT)
            .jsonPath("$..response.statusCodeValue")
            .isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void execute_shouldCallTemplate() {
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("element1", "Byrokratt");
        postBody.put("element3", "- 4 More Years");

        client.post()
            .uri("/test/call-template?element2=2021")
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
        stub("\"%s\"".formatted(expectedMappedValue));

        client.get()
            .uri("/test/post-mapped-value")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$..response.body")
            .isEqualTo(expectedMappedValue);

        assertEquals(1, requests.size());
        assertEquals("POST", requests.get(0).method());
        assertEquals(new ObjectMapper().writeValueAsString(expectedPostBody), requests.get(0).body());
    }

    @Test
    void execute_shouldExecuteStepFourTimesBecauseGlobalLimitOverridesStepSpecificWhenStepSpecificIsBigger() {
        client.get()
            .uri("/test/max-recursions-bigger")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("testtesttesttest");
    }

    @Test
    void execute_shouldExecuteStepTwoTimesBecauseStepSpecificOverridesGlobalLimitWhenStepSpecificIsSmaller() {
        client.get()
            .uri("/test/max-recursions-smaller")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("testtest");
    }

    @Test
    void execute_shouldExecuteTwoStepsFourTimesWhenMaxRecursionsIsDefinedAsFourInGlobalLevel() {
        client.get()
            .uri("/test/global-max-recursions")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step2step3step2step3step2step3");
    }

    @Test
    void execute_shouldExecuteEachStepTheAmountOfTimesDefinedForEachStep() {
        client.get()
            .uri("/test/step-specific-max-recursions")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step2step3step3");
    }

    @Test
    void execute_shouldExecuteEachStepTheAmountOfTimesDefinedForEachStepAndNextStepNameIsNotNecessary() {
        client.get()
            .uri("/test/max-recursions-without-next-step-name")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step4step2step3step4step2step3step3");
    }

    @Test
    void execute_shouldExecuteDslWithMultipleLoops() {
        client.get()
            .uri("/test/max-recursions-multiple-loops")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("step2step3step2step3step3step3step4step5step4step5step5step6step7step8step6step7step8step6step8step6");
    }
}
