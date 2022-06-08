package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import ee.buerokratt.ruuter.model.Args;
import ee.buerokratt.ruuter.model.Step;
import ee.buerokratt.ruuter.model.step.types.AssignStep;
import ee.buerokratt.ruuter.model.step.types.HttpStep;
import ee.buerokratt.ruuter.model.step.types.ReturnStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationServiceTest extends BaseIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Value("${application.config-path}")
    private String configPath;

    @Test
    void getFolder_shouldThrowWhenPathIsEmpty() {
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder(null));
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder(""));
    }

    @Test
    void getFolder_shouldThrowOnInvalidPath() {
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder("/fake/path"));
    }

    @Test
    void getFolder_shouldThrowWhenNoDirectory() {
        String path = ConfigurationServiceTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "ConfigurationServiceTest.java";
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder(path));
    }

    @Test
    void getFolder_shouldReturnFolder() {
        String path = ConfigurationServiceTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertEquals(ConfigurationService.getFolder(path), new File(path));
    }

    @Test
    void getConfigurations_shouldReturnConfigurationsMap() {
        Args<Object> getArgs = new Args<>() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};
        Args<Object> setArgs = new Args<>() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};

        Map<String, Map<String, Step>> configurations = configurationService.getConfigurations();
        List<String> stepNames = new ArrayList<>(configurations.get("get-message").keySet());

        assertEquals(1, configurations.size());
        assertEquals("get_message", stepNames.get(0));
        assertEquals("post_message", stepNames.get(1));
        assertEquals("assign_value", stepNames.get(2));
        assertEquals("return_value", stepNames.get(3));
        assertEquals(new HttpStep<>() {{
            setName("get_message");
            setCall("http.get");
            setArgs(getArgs);
            setResult("the_response");
        }}, configurations.get("get-message").get("get_message"));
        assertEquals(new HttpStep<>() {{
            setName("post_message");
            setCall("http.post");
            setArgs(setArgs);
            setResult("the_message");
        }}, configurations.get("get-message").get("post_message"));
        assertEquals(new AssignStep<>() {{
            setName("assign_value");
            setAssign(new HashMap<>() {{
                put("stringValue", "Bürokratt");
                put("integerValue", 2021);
            }});
        }}, configurations.get("get-message").get("assign_value"));
        assertEquals(new ReturnStep() {{
            setName("return_value");
            setReturnValue("${the_message.body}");
        }}, configurations.get("get-message").get("return_value"));
    }
}
