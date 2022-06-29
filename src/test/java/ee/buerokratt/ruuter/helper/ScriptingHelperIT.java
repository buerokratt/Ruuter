package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

@TestPropertySource(properties = {"application.config-path=${user.dir}/src/test/resources/helper"})
class ScriptingHelperIT extends BaseIntegrationTest {

    @Test
    void shouldBeAbleToUseIncomingResponseBodyAndParams() {
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("element", "123");

        client.post()
            .uri("/return-incoming?element=321")
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
            .uri("/return-script-strings?script2=\";" + expectedString + ";\"")
            .bodyValue(postBody)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo(expectedValue);
    }

    @Test
    void shouldConcatStringAndVariable() {
        client.post()
            .uri("/concat-string-and-variable")
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .isEqualTo("PNOEE-1234567890");
    }

}
