package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import ee.buerokratt.ruuter.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(properties = { "application.config-path=${user.dir}/src/test/resources/services" })
class ConfigurationServiceTest extends BaseIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Value("${application.config-path}")
    private String configPath;

    @Test
    void getConfigurations_shouldReturnConfigurationsMap() {
        HttpStepArgs<Object> expectedGetArgs = new HttpStepArgs<>() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};
        HttpStepArgs<Object> expectedPostArgs = new HttpStepArgs<>() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};
        HttpStep<Object> expectedGetStep = new HttpStep<>() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResult("the_response");
        }};
        HttpStep<Object> expectedPostStep = new HttpStep<>() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResult("the_message");
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

        Map<String, Map<String, ConfigurationStep>> configurations = configurationService.getConfigurations(configPath);
        List<String> stepNames = new ArrayList<>(configurations.get("get-message").keySet());

        assertEquals(1, configurations.size());
        assertEquals("get_message", stepNames.get(0));
        assertEquals("post_message", stepNames.get(1));
        assertEquals("assign_value", stepNames.get(2));
        assertEquals("return_value", stepNames.get(3));
        assertEquals(expectedGetStep, configurations.get("get-message").get("get_message"));
        assertEquals(expectedPostStep, configurations.get("get-message").get("post_message"));
        assertEquals(expectedAssignStep, configurations.get("get-message").get("assign_value"));
        assertEquals(expectedReturnStep, configurations.get("get-message").get("return_value"));
    }
}
