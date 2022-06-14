package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.service.exception.LoadConfigurationsException;
import ee.buerokratt.ruuter.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ee.buerokratt.ruuter.util.FileUtils.getFolder;

@Slf4j
@Service
public class ConfigurationService {
    private final HashMap<String, Map<String, ConfigurationStep>> configurations;

    private final ConfigurationMappingService configurationMappingService;

    public ConfigurationService(ConfigurationMappingService configurationMappingService, ApplicationProperties properties) {
        this.configurationMappingService = configurationMappingService;
        this.configurations = new HashMap<>(getConfigurations(properties.getConfigPath()));
    }

    public Map<String, Map<String, ConfigurationStep>> getConfigurations(String configPath) {
        Path servicesAbsPath = Paths.get(getFolder(configPath).getAbsolutePath());
        try (Stream<Path> paths = Files.walk(servicesAbsPath).filter(Files::isRegularFile)) {
            return paths
                .map(Path::toFile)
                .collect(Collectors.toMap(FileUtils::getFileNameWithoutYmlSuffix, configurationMappingService::getConfigurationSteps));
        } catch (Exception e) {
            throw new LoadConfigurationsException(e);
        }
    }

    public Object executeConfiguration(String configurationName, Map<String, String> requestBody, Map<String, String> requestParams) {
        ConfigurationInstance configurationInstance = new ConfigurationInstance(configurations.get(configurationName), requestBody, requestParams);
        configurationInstance.execute();
        return configurationInstance.getReturnValue();
    }
}
