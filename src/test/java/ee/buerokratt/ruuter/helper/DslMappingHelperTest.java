package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class DslMappingHelperTest {

    /*
    private final String configPath = System.getProperty("user.dir") + "/src/test/resources/dsl/mapping/helper";

    private DslMappingHelper helper;

    @BeforeEach
    protected void initializeObjects() {
        ObjectMapper mapper = new YAMLMapper();
        helper = new DslMappingHelper(mapper);
    }

    @Test
    void getDslSteps_shouldReturnDslWithStepsWithAllPossibleStepTypes() {
        Map<String, DslStep> dslSteps = helper.getDslSteps(Path.of(configPath + "/GET/all-possible-step-types.yml"));

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
    void getDslSteps_shouldThrowExceptionWhenPathIsNotYmlFile() {
        Path fullPath = Path.of(configPath + "/GET/get-and-return.json");
        String expectedErrorMessage = "Encountered error, when loading DSL: " + fullPath + ". " +
            DslMappingHelper.DSL_NOT_YML_FILE_ERROR_MESSAGE;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> helper.getDslSteps(fullPath));
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void getDslSteps_shouldThrowExceptionWhenFileContainsInvalidStepType() {
        Path fullPath = Path.of(configPath + "/POST/invalid-step-type.yml");
        String expectedErrorMessage = "Encountered error, when loading DSL: " + fullPath + ". Unable to load invalid step: invalidStep. Error message: " + DslMappingHelper.INVALID_STEP_ERROR_MESSAGE;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> helper.getDslSteps(fullPath));
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void getDslSteps_shouldThrowExceptionWhenStepHasInvalidParameter() {
        Path fullPath = Path.of(configPath + "/POST/invalid-step-parameter.yml");
        String expectedErrorMessage = "Encountered error, when loading DSL: " + fullPath + ". Unable to load invalid step: invalidStep. Error message: ";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> helper.getDslSteps(fullPath));
        assertTrue(exception.getMessage().contains(expectedErrorMessage));
    }

     */
}
