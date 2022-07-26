package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.domain.steps.AssignStep;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.domain.steps.ReturnStep;
import ee.buerokratt.ruuter.domain.steps.TemplateStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpMockStep;
import ee.buerokratt.ruuter.domain.steps.conditional.SwitchStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpStep;
import ee.buerokratt.ruuter.helper.exception.InvalidDslException;
import ee.buerokratt.ruuter.helper.exception.InvalidDslStepException;
import ee.buerokratt.ruuter.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class DslMappingHelper {
    private final ObjectMapper mapper;

    public static final String DSL_NOT_YML_FILE_ERROR_MESSAGE = "DSL is not yml file.";
    public static final String INVALID_STEP_ERROR_MESSAGE = "Invalid step type.";

    public DslMappingHelper(@Qualifier("ymlMapper") ObjectMapper mapper) {
        this.mapper = mapper;
    }


    public Map<String, DslStep> getDslSteps(Path path) {
        try {
            if (FileUtils.isYmlFile(path)) {
                Map<String, JsonNode> nodeMap = mapper.readValue(path.toFile(), new TypeReference<>() {});
                return convertNodeMapToStepMap(nodeMap);
            } else {
                throw new IllegalArgumentException(DSL_NOT_YML_FILE_ERROR_MESSAGE);
            }
        } catch (Exception e) {
            throw new InvalidDslException(path.toString(), e.getMessage(), e);
        }
    }

    private Map<String, DslStep> convertNodeMapToStepMap(Map<String, JsonNode> stepNodes) {
        return stepNodes.entrySet().stream().collect(toMap(Map.Entry::getKey, map -> {
            try {
                DslStep step = convertJsonNodeToDslStep(map.getValue());
                step.setName(map.getKey());
                return step;
            } catch (Exception e) {
                throw new InvalidDslStepException(map.getKey(), e.getMessage(), e);
            }
        }, (x, y) -> y, LinkedHashMap::new));
    }

    private DslStep convertJsonNodeToDslStep(JsonNode jsonNode) throws JsonProcessingException {
        if (jsonNode.get("call") != null) {
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
}
