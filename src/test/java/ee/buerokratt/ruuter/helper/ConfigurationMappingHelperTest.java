package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@WireMockTest
class ConfigurationMappingHelperTest {

    private final String configPath = "C:/Users/ewert.ubaleht/IdeaProjects/ruuter3.0/Ruuter/src/test/resources/dsl/mapping/helper";

    private ConfigurationMappingHelper helper;

    @BeforeEach
    protected void initializeObjects() {
        ObjectMapper mapper = new YAMLMapper();
        helper = new ConfigurationMappingHelper(mapper);
    }

    @Test
    void getConfigurationSteps_shouldReturnConfigurationWithStepsWithAllPossibleStepTypes() {
        Map<String, ConfigurationStep> dslSteps = helper.getConfigurationSteps(Path.of(configPath + "/GET/all-possible-step-types.yml"));

        assertEquals(7, dslSteps.size());
        assertTrue(dslSteps.containsKey("get_step"));
        assertTrue(dslSteps.containsKey("post_step"));
        assertTrue(dslSteps.containsKey("conditional_step"));
        assertTrue(dslSteps.containsKey("mock_step"));
        assertTrue(dslSteps.containsKey("assign_step"));
        assertTrue(dslSteps.containsKey("template_step"));
        assertTrue(dslSteps.containsKey("return_step"));
    }

    @Test
    void getConfigurations_shouldThrowExceptionWhenPathIsNotYmlFile() {
        Path fullPath = Path.of(configPath + "/GET/get-and-return.json");
        String expectedErrorMessage = "Encountered error, when loading DSL: C:\\Users\\ewert.ubaleht\\IdeaProjects\\ruuter3.0\\Ruuter\\src\\test\\resources\\dsl\\mapping\\helper\\GET\\get-and-return.json. " +
            ConfigurationMappingHelper.DSL_NOT_YML_FILE_ERROR_MESSAGE;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> helper.getConfigurationSteps(fullPath));
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void getConfigurations_shouldThrowExceptionWhenFileContainsInvalidStepType() {
        Path fullPath = Path.of(configPath + "/POST/invalid-step-type.yml");
        String expectedErrorMessage = "Encountered error, when loading DSL: C:\\Users\\ewert.ubaleht\\IdeaProjects\\ruuter3.0\\Ruuter\\src\\test\\resources\\dsl\\mapping\\helper\\POST\\invalid-step-type.yml" +
            ". Unable to load invalid step: invalidStep. Error message: " + ConfigurationMappingHelper.INVALID_STEP_ERROR_MESSAGE;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> helper.getConfigurationSteps(fullPath));
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void getConfigurations_shouldThrowExceptionWhenStepHasInvalidParameter() {
        Path fullPath = Path.of(configPath + "/POST/invalid-step-parameter.yml");
        String expectedErrorMessage = "Encountered error, when loading DSL: C:\\Users\\ewert.ubaleht\\IdeaProjects\\ruuter3.0\\Ruuter\\src\\test\\resources\\dsl\\mapping\\helper\\POST\\invalid-step-parameter.yml" +
            ". Unable to load invalid step: invalidStep. Error message: ";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> helper.getConfigurationSteps(fullPath));
        assertTrue(exception.getMessage().contains(expectedErrorMessage));
    }
}
