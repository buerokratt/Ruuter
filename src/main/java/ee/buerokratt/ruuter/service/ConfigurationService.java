package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.ConfigurationMappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.helper.exception.LoadConfigurationsException;
import ee.buerokratt.ruuter.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static ee.buerokratt.ruuter.util.FileUtils.getFolderPath;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class ConfigurationService {
    private final ConfigurationMappingHelper configurationMappingHelper;
    private final ApplicationProperties properties;
    private final ScriptingHelper scriptingHelper;
    private final Tracer tracer;

    private final Map<String, Map<String, ConfigurationStep>> configurations;

    public ConfigurationService(ApplicationProperties properties, ConfigurationMappingHelper configurationMappingHelper, ScriptingHelper scriptingHelper, Tracer tracer) {
        this.configurationMappingHelper = configurationMappingHelper;
        this.properties = properties;
        this.scriptingHelper = scriptingHelper;
        this.configurations = getConfigurations(properties.getConfigPath());
        this.tracer = tracer;
    }

    public Map<String, Map<String, ConfigurationStep>> getConfigurations(String configPath) {
        try (Stream<Path> paths = Files.walk(getFolderPath(configPath))) {
            return paths
                .filter(Files::isRegularFile)
                .collect(toMap(FileUtils::getFileNameWithoutSuffix, configurationMappingHelper::getConfigurationSteps));
        } catch (Exception e) {
            throw new LoadConfigurationsException(e);
        }
    }

    public Object execute(String configuration, Map<String, String> requestBody, Map<String, String> requestParams, String requestOrigin) {
        Map<String, ConfigurationStep> steps = configurations.get(configuration);
        ConfigurationInstance configurationInstance = new ConfigurationInstance(scriptingHelper, properties, steps, requestBody, requestParams, requestOrigin, tracer);
        configurationInstance.execute(configuration);
        return configurationInstance.getReturnValue();
    }
}
