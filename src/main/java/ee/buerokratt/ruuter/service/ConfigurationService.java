package ee.buerokratt.ruuter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.model.ConfigurationModel;
import ee.buerokratt.ruuter.model.Step;
import ee.buerokratt.ruuter.model.step.types.AssignStep;
import ee.buerokratt.ruuter.model.step.types.HttpStep;
import ee.buerokratt.ruuter.model.step.types.ReturnStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ConfigurationService {

    public static final String COULDNT_LOAD_CONFIGURATION_STEP = "Couldn't load configuration: %s, step: %s";
    private final ApplicationProperties properties;
    private final ConfigurationModel configurationModel = new ConfigurationModel();
    private Stream<Path> paths;

    public ConfigurationService(ApplicationProperties properties) {
        this.properties = properties;
        load();
    }

    public void load() {
        try {
            File configFolder = getFolder(properties.getConfigPath());
            paths = Files.walk(Paths.get(configFolder.getAbsolutePath())).filter(Files::isRegularFile);
            for (Path path : paths.toList()) {
                File file = path.toFile();
                if (file.isDirectory() || !file.getName().endsWith(".yml")) {
                    continue;
                }
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Map<String, JsonNode> nodeMap = mapper.readValue(file, new TypeReference<>() {
                });
                Map<String, Step> steps = nodeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> {
                    JsonNode jsonNode = o.getValue();
                    Step step = null;
                    try {
                        if (jsonNode.get("call") != null) {
                            step = mapper.treeToValue(jsonNode, HttpStep.class);
                        }
                        if (jsonNode.get("assign") != null) {
                            step = mapper.treeToValue(jsonNode, AssignStep.class);
                        }
                        if (jsonNode.get("return") != null) {
                            step = mapper.treeToValue(jsonNode, ReturnStep.class);
                        }
                        if (step == null) {
                            throw new IllegalStateException(String.format(COULDNT_LOAD_CONFIGURATION_STEP, file.getName(), o.getKey()));
                        }
                        step.setName(o.getKey());
                        return step;
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(String.format(COULDNT_LOAD_CONFIGURATION_STEP, file.getName(), o.getKey()));
                    }
                }, (x, y) -> y, LinkedHashMap::new));
                addConfiguration(file.getName().replace(".yml", ""), steps);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't load configuration");
        } finally {
            paths.close();
        }
    }

    protected static File getFolder(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                return file;
            }
        }
        throw new IllegalStateException("Failed to resolve configurations directory: %s".formatted(path));
    }

    public void addConfiguration(String configurationName, Map<String, Step> steps) {
        HashMap<String, Map<String, Step>> configurations = configurationModel.getConfigurations();
        configurations.put(configurationName, steps);
        configurationModel.setConfigurations(configurations);
    }

    public Map<String, Map<String, Step>> getConfigurations() {
        return configurationModel.getConfigurations();
    }
}
