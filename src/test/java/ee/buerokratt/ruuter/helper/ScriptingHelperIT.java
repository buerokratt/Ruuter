package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

@TestPropertySource(properties = {"application.config-path=/dsl-helper"})
class ScriptingHelperIT extends BaseIntegrationTest {

    @Test
    void shouldConcatenateStrings() {
        client.get()
            .uri("/test/string-concatenation")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("Bürokratt www.kratid.ee");
    }

    @Test
    void shouldConcatStringAndVariable() {
        client.post()
            .uri("/test/concat-string-and-variable")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("PNOEE-1234567890");
    }

    @Test
    void shouldCalculateWithIntegers() {
        client.get()
            .uri("/test/integer-addition")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(4);
    }

    @Test
    void shouldEvaluateBooleanValue() {
        client.get()
            .uri("/test/boolean-or-evaluation")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(true);
    }

    @Test
    void shouldEvaluateBooleanValue2() {
        client.get()
            .uri("/test/boolean-and-evaluation")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(false);
    }

    @Test
    void shouldBeAbleToUseIncomingResponseBodyAndParams() {
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("element", "123");

        client.post()
            .uri("/test/return-incoming?element=321")
            .bodyValue(postBody)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("123321 and 321123");
    }

    @Test
    void shouldNotEvaluatePostBodyScripts() {
        String expectedString = "eval(print(\"bye\"))";
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("script1", expectedString);
        String expectedValue = "%s & \";%s;\" & %s\";%s;\" & %s & \";%s;\" & %s\";%s;\"".replace("%s", expectedString);

        client.post()
            .uri("/test/return-script-strings?script2=\";" + expectedString + ";\"")
            .bodyValue(postBody)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(expectedValue);
    }

    @Test
    void evaluateScripts_shouldRespond_whenOptionalQueryParamFilled() {
        String optionalString = "Hello";

        client.get()
              .uri("/test/get-optional?optional_parameter=" + optionalString)
              .exchange().expectStatus().isOk()
              .expectBody()
              .jsonPath("$.response")
              .isEqualTo(optionalString);
    }

    @Test
    void evaluateScripts_shouldRespond_whenOptionalQueryParamNotFilled() {
        client.get()
              .uri("/test/get-optional")
              .exchange().expectStatus().isOk()
              .expectBody()
              .jsonPath("$.response")
              .isEqualTo("");
    }

    @Test
    void evaluateScripts_shouldRespond_whenOptionalBodyParamFilled() {
        String required = "Hello";
        String optional = " and good morning!";
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("somethingRequired", required);
        postBody.put("optional_something", optional);

        client.post()
            .uri("/test/post-with-optional")
            .bodyValue(postBody)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(required + optional);
    }

    @Test
    void evaluateScripts_shouldRespond_whenOptionalBodyParamNotFilled() {
        String required = "Hello";
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("somethingRequired", required);

        client.post()
              .uri("/test/post-with-optional")
              .bodyValue(postBody)
              .exchange().expectStatus().isOk()
              .expectBody()
              .jsonPath("$.response")
              .isEqualTo(required);
    }

    @Test
    void evaluateScripts_shouldThrow_whenRequiredParamNotFilled() throws NullPointerException {
        HashMap<String, String> postBody = new HashMap<>();

        client.post()
              .uri("/test/post-with-optional")
              .bodyValue(postBody)
              .exchange();
    }
}
