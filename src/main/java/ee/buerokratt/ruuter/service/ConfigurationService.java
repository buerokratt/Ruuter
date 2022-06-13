package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.ConfigurationStep;
import ee.buerokratt.ruuter.helper.ConfigurationMappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.helper.exception.LoadConfigurationsException;
import ee.buerokratt.ruuter.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static ee.buerokratt.ruuter.util.FileUtils.getFolder;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class ConfigurationService {
    private final ConfigurationMappingHelper configurationMappingHelper;
    private final ScriptingHelper scriptingHelper;

    private final HashMap<String, Map<String, ConfigurationStep>> configurations;

    public ConfigurationService(ApplicationProperties properties, ConfigurationMappingHelper configurationMappingHelper, ScriptingHelper scriptingHelper) {
        this.configurationMappingHelper = configurationMappingHelper;
        this.configurations = new HashMap<>(getConfigurations(properties.getConfigPath()));
        this.scriptingHelper = scriptingHelper;
    }

    public Map<String, Map<String, ConfigurationStep>> getConfigurations(String configPath) {
        Path servicesAbsPath = Paths.get(getFolder(configPath).getAbsolutePath());
        try (Stream<Path> paths = Files.walk(servicesAbsPath).filter(Files::isRegularFile)) {
            return paths
                .map(Path::toFile)
                .collect(toMap(FileUtils::getFileNameWithoutYmlSuffix, configurationMappingHelper::getConfigurationSteps));
        } catch (Exception e) {
            throw new LoadConfigurationsException(e);
        }
    }

    public Object executeConfiguration(String configurationName, Map<String, String> requestBody, Map<String, String> requestParams) {
        ConfigurationInstance configurationInstance = new ConfigurationInstance(configurations.get(configurationName), requestBody, requestParams, scriptingHelper);
        configurationInstance.execute();
        return configurationInstance.getReturnValue();
    }
}
