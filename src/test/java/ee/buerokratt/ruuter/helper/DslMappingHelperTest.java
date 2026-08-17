package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.Dsl;
import ee.buerokratt.ruuter.domain.steps.AssignStep;
import ee.buerokratt.ruuter.domain.steps.DeclarationStep;
import ee.buerokratt.ruuter.domain.steps.LogStep;
import ee.buerokratt.ruuter.domain.steps.ReturnStep;
import ee.buerokratt.ruuter.domain.steps.conditional.SwitchStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpGetStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpMockStep;
import ee.buerokratt.ruuter.helper.exception.InvalidDslException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * getDslSteps() reads a real YAML file from disk (via a real ymlMapper, matching
 * JacksonConfiguration's bean) and dispatches each top-level key to the right DslStep subclass.
 * "[#PARAM]" constant substitution is tested via reflection into the private dslParameters field
 * rather than the real /app/constants.ini path, which only exists in the deployed container.
 */
class DslMappingHelperTest {

    private DslMappingHelper dslMappingHelper;

    @BeforeEach
    void setUp() {
        ApplicationProperties properties = new ApplicationProperties();
        ApplicationProperties.DSL dsl = new ApplicationProperties.DSL();
        dsl.setProcessedFiletypes(List.of(".yml", ".yaml"));
        dsl.setAllowedFiletypes(List.of(".yml", ".yaml"));
        properties.setDsl(dsl);

        ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        dslMappingHelper = new DslMappingHelper(ymlMapper);
        dslMappingHelper.properties = properties;
    }

    private Path writeDsl(Path tempDir, String filename, String content) throws IOException {
        Path path = tempDir.resolve(filename);
        Files.writeString(path, content);
        return path;
    }

    private void setDslParameters(Properties properties) throws Exception {
        Field field = DslMappingHelper.class.getDeclaredField("dslParameters");
        field.setAccessible(true);
        field.set(dslMappingHelper, properties);
    }

    @Test
    void getDslSteps_shouldParseReturnStep(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "return-value.yml", """
            return_value:
              return: "hello"
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        ReturnStep step = assertInstanceOf(ReturnStep.class, dsl.step("return_value"));
        assertEquals("return_value", step.getName());
        assertEquals("hello", step.getReturnValue());
    }

    @Test
    void getDslSteps_shouldParseAssignStep(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "assign-value.yml", """
            assign_value:
              assign:
                stringValue: "Burokratt"
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertInstanceOf(AssignStep.class, dsl.step("assign_value"));
    }

    @Test
    void getDslSteps_shouldParseDeclarationStepAndExposeItOnTheDsl(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "declare.yml", """
            declaration:
              call: declare
            return_value:
              return: "hello"
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertInstanceOf(DeclarationStep.class, dsl.getDeclaration());
    }

    @Test
    void getDslSteps_shouldParseHttpGetStep(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "get-message.yml", """
            get_message:
              call: http.get
              args:
                url: https://example.com/endpoint
              result: the_response
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertInstanceOf(HttpGetStep.class, dsl.step("get_message"));
    }

    @Test
    void getDslSteps_shouldParseHttpMockStep(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "mock-step.yml", """
            mock_step:
              call: reflect.mock
              args:
                response:
                  key: value
              result: the_response
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertInstanceOf(HttpMockStep.class, dsl.step("mock_step"));
    }

    @Test
    void getDslSteps_shouldParseSwitchStep(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "switch-step.yml", """
            switch_step:
              switch:
                - condition: "${true}"
                  next: step_a
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertInstanceOf(SwitchStep.class, dsl.step("switch_step"));
    }

    @Test
    void getDslSteps_shouldParseLogStep(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "log-step.yml", """
            log_step:
              log: "some message"
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertInstanceOf(LogStep.class, dsl.step("log_step"));
    }

    @Test
    void getDslSteps_shouldThrow_whenFileExtensionIsNotAllowed(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "not-a-dsl.txt", "return_value:\n  return: \"hello\"\n");

        assertThrows(InvalidDslException.class, () -> dslMappingHelper.getDslSteps(path));
    }

    @Test
    void getDslSteps_shouldThrow_whenStepTypeIsUnrecognized(@TempDir Path tempDir) throws IOException {
        Path path = writeDsl(tempDir, "unknown-step.yml", """
            mystery_step:
              nonsense: true
            """);

        assertThrows(InvalidDslException.class, () -> dslMappingHelper.getDslSteps(path));
    }

    @Test
    void getDslSteps_shouldLeaveParameterPlaceholderLiteral_whenConstantsAreNotLoaded(@TempDir Path tempDir) throws Exception {
        setDslParameters(new Properties());

        Path path = writeDsl(tempDir, "with-parameter.yml", """
            return_value:
              return: "[#SOME_CONSTANT]"
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertEquals("[#SOME_CONSTANT]", ((ReturnStep) dsl.step("return_value")).getReturnValue());
    }

    @Test
    void getDslSteps_shouldSubstituteParameter_whenConstantsAreLoaded(@TempDir Path tempDir) throws Exception {
        Properties constants = new Properties();
        constants.setProperty("SOME_CONSTANT", "actual-value");
        setDslParameters(constants);

        Path path = writeDsl(tempDir, "with-parameter.yml", """
            return_value:
              return: "[#SOME_CONSTANT]"
            """);

        Dsl dsl = dslMappingHelper.getDslSteps(path);

        assertEquals("actual-value", ((ReturnStep) dsl.step("return_value")).getReturnValue());
    }
}
