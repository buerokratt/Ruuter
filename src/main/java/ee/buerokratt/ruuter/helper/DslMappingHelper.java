package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.Dsl;
import ee.buerokratt.ruuter.domain.steps.*;
import ee.buerokratt.ruuter.domain.steps.http.HttpMockStep;
import ee.buerokratt.ruuter.domain.steps.conditional.SwitchStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpStep;
import ee.buerokratt.ruuter.helper.exception.InvalidDslException;
import ee.buerokratt.ruuter.helper.exception.InvalidDslStepException;
import ee.buerokratt.ruuter.service.OpenSearchSender;
import ee.buerokratt.ruuter.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class DslMappingHelper {
    public ApplicationProperties properties;
    private final ObjectMapper mapper;

    public static final String DSL_NOT_YML_FILE_ERROR_MESSAGE = "DSL is not yml file.";
    public static final String INVALID_STEP_ERROR_MESSAGE = "Invalid step type.";

    private Properties dslParameters;

    private OpenSearchSender openSearchSender;

    public DslMappingHelper(@Qualifier("ymlMapper") ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private void logEvent(String dslName, String dslMethod, String level, StackTraceElement[] stackTrace) {
/*        openSearchSender.log(
            new OpenSearchSender.RuuterEvent(
                level,
                dslName,
                dslMethod,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                stackTrace
            ));*/
    }

    public Dsl getDslSteps(Path path) {
        try {
            if (FileUtils.isFiletype(path, properties.getDsl().getProcessedFiletypes())
               || FileUtils.isGuard(path)) {
                Map<String, JsonNode> nodeMap = mapper.readValue(path.toFile(), new TypeReference<>() {});
                for (String key : nodeMap.keySet()) {
                    JsonNode node = nodeMap.get(key);
                    nodeMap.replace(key, node, mapper.readTree(this.replaceDslParametersWithValues(node.toString())));
                }
                return convertNodeMapToStepMap(nodeMap);
            } else {
                throw new IllegalArgumentException(DSL_NOT_YML_FILE_ERROR_MESSAGE);
            }
        } catch (Exception e) {
            String pathname = path.toString();
            logEvent(pathname.substring(1, pathname.indexOf('/', 1)),
                pathname.substring(pathname.indexOf('/', 1)),
                "STARTUP",
                e.getStackTrace());
            throw new InvalidDslException(path.toString(), e.getMessage(), e);
        }
    }

    private Dsl convertNodeMapToStepMap(Map<String, JsonNode> stepNodes) {
        return new Dsl(stepNodes.entrySet().stream().collect(toMap(Map.Entry::getKey, map -> {
            try {
                DslStep step = convertJsonNodeToDslStep(map.getValue());
                step.setName(map.getKey());
                return step;
            } catch (Exception e) {
                throw new InvalidDslStepException(map.getKey(), e.getMessage(), e);
            }
        }, (x, y) -> y, LinkedHashMap::new)));
    }

    private DslStep convertJsonNodeToDslStep(JsonNode jsonNode) throws JsonProcessingException {

        if (jsonNode.get("call") != null) {
            if ("declare".equals(jsonNode.get("call").asText())) {
                return mapper.treeToValue(jsonNode, DeclarationStep.class);
            }

            if (jsonNode.get("call").asText().equals("reflect.mock")) {
                return mapper.treeToValue(jsonNode, HttpMockStep.class);
            }
            return mapper.treeToValue(jsonNode, HttpStep.class);
        }

        if (jsonNode.get("template") != null) {
            return mapper.treeToValue(jsonNode, TemplateStep.class);
        }
        if (jsonNode.get("assign") != null) {
            return mapper.treeToValue(jsonNode, AssignStep.class);
        }
        if (jsonNode.get("return") != null) {
            return mapper.treeToValue(jsonNode, ReturnStep.class);
        }
        if (jsonNode.get("switch") != null) {
            return mapper.treeToValue(jsonNode, SwitchStep.class);
        }

        throw new IllegalArgumentException(INVALID_STEP_ERROR_MESSAGE);
    }

    /**
     * Replace DSL file parameters with values.
     * DSL parameters are formatted like "[#PARAMETER]"
     * and will be replaced in bulk by regular expression.
     *
     * @param input String to modify
     * @return Updated string
     */
    private String replaceDslParametersWithValues(String input) {
        if (dslParameters == null || dslParameters.isEmpty()) {
            initDSLParameters();
        }

        Matcher parameterMatcher = Pattern.compile("\\[#.*?\\]").matcher(input);

        if (!parameterMatcher.find())
            return input;

        String replaced = parameterMatcher
            .replaceAll(match ->
                dslParameters.containsKey(unwrapParameter(match.group(0))) ?
                    dslParameters.getProperty(unwrapParameter(match.group(0))) :
                    match.group(0)
                );

        return replaced;
    }

    public void initDSLParameters() {
        try {
            dslParameters = new Properties();
            dslParameters.load(new FileInputStream("/app/constants.ini"));
        } catch (IOException e) {
            log.warn("constants.ini not found or not accessible");
            throw new RuntimeException(e);
        }
    }

    private String unwrapParameter(String parameter) {
        return parameter.substring(2, parameter.length()-1);
    }
}
