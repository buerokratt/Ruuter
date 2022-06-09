package ee.buerokratt.ruuter.domain;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import ee.buerokratt.ruuter.service.ConfigurationService;
import ee.buerokratt.ruuter.service.exception.InvalidHttpRequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpStepTest extends BaseIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Value("${application.config-path}")
    private String configPath;

    private static HttpStep expectedGetStep;

    @BeforeAll
    public static void setup() {
        HttpStepArgs expectedArgs = new HttpStepArgs();
        expectedArgs.setUrl("https://example.com/endpoint");
        expectedArgs.setHeaders(new HashMap<>() {{
            put("Content-Type", "text/html");
        }});
        expectedGetStep = new HttpStep();
        expectedGetStep.setName("get_step");
        expectedGetStep.setResult("new_result");
        expectedGetStep.setCall("http.get");
        expectedGetStep.setArgs(expectedArgs);
    }

    @Test
    void execute_shouldThrowExceptionWhenInvalidCallParameter() {
        expectedGetStep.setCall("wrongCall");
        Map<String, Map<String, ConfigurationStep>> configurations = configurationService.getConfigurations(configPath);
        ConfigurationInstance configurationInstance = new ConfigurationInstance(configurations.get("get-message"), new HashMap<>(), new HashMap<>());

        Exception exception = assertThrows(InvalidHttpRequestException.class, () -> expectedGetStep.execute(configurationInstance));

        assertEquals(exception.getMessage(), format("Invalid http request in step: %s", expectedGetStep.getName()));
    }
}
