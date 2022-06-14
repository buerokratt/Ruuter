package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.domain.AssignStep;
import ee.buerokratt.ruuter.domain.ConfigurationStep;
import ee.buerokratt.ruuter.domain.HttpStep;
import ee.buerokratt.ruuter.domain.ReturnStep;
import ee.buerokratt.ruuter.helper.exception.InvalidConfigurationException;
import ee.buerokratt.ruuter.helper.exception.InvalidConfigurationStepException;
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
public class ConfigurationMappingHelper {
    private final ObjectMapper mapper;

    public ConfigurationMappingHelper(@Qualifier("ymlMapper") ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, ConfigurationStep> getConfigurationSteps(Path path) {
        try {
            if (FileUtils.isYmlFile(path)) {
                Map<String, JsonNode> nodeMap = mapper.readValue(path.toFile(), new TypeReference<>() {});
                return convertNodeMapToStepMap(nodeMap);
            } else {
                throw new IllegalArgumentException("Config not yml file");
            }
        } catch (Exception e) {
            throw new InvalidConfigurationException(path.toString(), e);
        }
    }

    private Map<String, ConfigurationStep> convertNodeMapToStepMap(Map<String, JsonNode> stepNodes) {
        return stepNodes.entrySet().stream().collect(toMap(Map.Entry::getKey, map -> {
            try {
                ConfigurationStep step = convertJsonNodeToConfigurationStep(map.getValue());
                step.setName(map.getKey());
                return step;
            } catch (Exception e) {
                throw new InvalidConfigurationStepException(map.getKey(), e);
            }
        }, (x, y) -> y, LinkedHashMap::new));
    }

    private ConfigurationStep convertJsonNodeToConfigurationStep(JsonNode jsonNode) throws JsonProcessingException {
        if (jsonNode.get("call") != null) {
            return mapper.treeToValue(jsonNode, HttpStep.class);
        }
        if (jsonNode.get("assign") != null) {
            return mapper.treeToValue(jsonNode, AssignStep.class);
        }
        if (jsonNode.get("return") != null) {
            return mapper.treeToValue(jsonNode, ReturnStep.class);
        }
        throw new IllegalArgumentException("No valid step recognised");
    }
}
