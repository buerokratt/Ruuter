package ee.buerokratt.ruuter.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.model.ConfigurationModel;
import ee.buerokratt.ruuter.model.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class ConfigurationService {

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
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                List<Step> steps = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Step.class));
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

    public void addConfiguration(String configurationName, List<Step> steps) {
        HashMap<String, List<Step>> configurations = configurationModel.getConfigurations();
        configurations.put(configurationName, steps);
        configurationModel.setConfigurations(configurations);
    }

    public Map<String, List<Step>> getConfigurations() {
        return configurationModel.getConfigurations();
    }
}
