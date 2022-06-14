package ee.buerokratt.ruuter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.buerokratt.ruuter.domain.steps.AssignStep;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpStep;
import ee.buerokratt.ruuter.domain.steps.ReturnStep;
import ee.buerokratt.ruuter.service.exception.InvalidConfigurationException;
import ee.buerokratt.ruuter.service.exception.InvalidConfigurationStepException;
import ee.buerokratt.ruuter.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationMappingService {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Map<String, ConfigurationStep> getConfigurationSteps(File file) {
        try {
            if (FileUtils.isYmlFile(file)) {
                Map<String, JsonNode> nodeMap = mapper.readValue(file, new TypeReference<>() {});
                return convertNodeMapToStepMap(nodeMap);
            } else {
                throw new IllegalArgumentException("Config not yml file");
            }
        } catch (Exception e) {
            throw new InvalidConfigurationException(file.getName(), e);
        }
    }

    private Map<String, ConfigurationStep> convertNodeMapToStepMap(Map<String, JsonNode> stepNodes) {
        return stepNodes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> {
            JsonNode jsonNode = o.getValue();
            try {
                ConfigurationStep step = convertJsonNodeToConfigurationStep(jsonNode);
                step.setName(o.getKey());
                return step;
            } catch (Exception e) {
                throw new InvalidConfigurationStepException(o.getKey(), e);
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
