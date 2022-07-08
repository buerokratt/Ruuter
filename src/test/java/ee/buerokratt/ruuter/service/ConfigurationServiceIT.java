package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import ee.buerokratt.ruuter.domain.steps.*;
import ee.buerokratt.ruuter.domain.steps.http.HttpGetStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpPostStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpQueryArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = { "application.config-path=${user.dir}/src/test/resources/service" })
class ConfigurationServiceIT extends BaseIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Value("${application.config-path}")
    private String configPath;

    @Test
    void getConfigurations_shouldReturnConfigurationsMap() {
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};
        HttpStep expectedGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_message");
        }};
        AssignStep<Object> expectedAssignStep = new AssignStep<>() {{
            setName("assign_value");
            setAssign(new HashMap<>() {{
                put("stringValue", "Bürokratt");
                put("integerValue", 2021);
            }});
        }};
        ReturnStep expectedReturnStep = new ReturnStep() {{
            setName("return_value");
            setReturnValue("return_value");
        }};

        Map<String, Map<String, Map<String, ConfigurationStep>>> configurations = configurationService.getConfigurations(configPath);
        List<String> stepNames = new ArrayList<>(configurations.get("POST").get("test-conf").keySet());

        assertEquals("get_message", stepNames.get(0));
        assertEquals("post_message", stepNames.get(1));
        assertEquals("assign_value", stepNames.get(2));
        assertEquals("return_value", stepNames.get(3));
        assertEquals(expectedGetStep, configurations.get("POST").get("test-conf").get("get_message"));
        assertEquals(expectedPostStep, configurations.get("POST").get("test-conf").get("post_message"));
        assertEquals(expectedAssignStep, configurations.get("POST").get("test-conf").get("assign_value"));
        assertEquals(expectedReturnStep, configurations.get("POST").get("test-conf").get("return_value"));
    }

    @Test
    void getConfigurations_shouldAddDefinedHeadersToHttpSteps() {
        Map<String, Map<String, Map<String, ConfigurationStep>>> configurations = configurationService.getConfigurations(configPath);
        Map<String, ConfigurationStep> steps = new HashMap<>(configurations.get("POST").get("pass-headers-with-request"));

        HttpGetStep httpGetStep = (HttpGetStep) steps.get("get_message");
        HttpPostStep httpPostStep = (HttpPostStep) steps.get("post_message");

        HashMap<String, String> httpGetStepHeaders = new HashMap<>() {{
            put("Connection", "keep-alive");
        }};
        HashMap<String, String> httpPostStepHeaders = new HashMap<>() {{
            put("Cache-Control", "no-cache");
        }};

        assertEquals(httpGetStepHeaders, httpGetStep.getArgs().getHeaders());
        assertEquals(httpPostStepHeaders, httpPostStep.getArgs().getHeaders());
    }

    @Test
    void getConfigurations_shouldPutAllConfigurationsToPostAndGetKeys() {
        Map<String, Map<String, Map<String, ConfigurationStep>>> configurations = configurationService.getConfigurations(configPath);

        assertEquals(2, configurations.keySet().size());
        assertTrue(configurations.containsKey("POST"));
        assertTrue(configurations.containsKey("GET"));
    }
}
