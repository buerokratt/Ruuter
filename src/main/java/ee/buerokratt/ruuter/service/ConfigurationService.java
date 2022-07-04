package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.ConfigurationMappingHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.helper.exception.LoadConfigurationsException;
import ee.buerokratt.ruuter.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
    private final MappingHelper mappingHelper;

    private final Map<String, Map<String, Map<String, ConfigurationStep>>> configurations;

    public ConfigurationService(ApplicationProperties properties, ConfigurationMappingHelper configurationMappingHelper, ScriptingHelper scriptingHelper, Tracer tracer, MappingHelper mappingHelper) {
        this.configurationMappingHelper = configurationMappingHelper;
        this.properties = properties;
        this.scriptingHelper = scriptingHelper;
        this.configurations = getConfigurations(properties.getConfigPath());
        this.tracer = tracer;
        this.mappingHelper = mappingHelper;
    }

    public Map<String, Map<String, Map<String, ConfigurationStep>>> getConfigurations(String configPath) {
        return Arrays.stream(Objects.requireNonNull(new File(configPath).listFiles(File::isDirectory))).collect(toMap(File::getName, directory -> {
            try (Stream<Path> paths = Files.walk(getFolderPath(directory.toString()))) {
                return paths
                    .filter(Files::isRegularFile)
                    .collect(toMap(FileUtils::getFileNameWithoutSuffix, configurationMappingHelper::getConfigurationSteps));
            } catch (Exception e) {
                throw new LoadConfigurationsException(e);
            }
        }));
    }

    public Object execute(String configuration, String requestType, Map<String, Object> requestBody, Map<String, Object> requestParams, String requestOrigin) {
        Map<String, ConfigurationStep> steps = configurations.get(requestType.toUpperCase()).get(configuration);
        ConfigurationInstance configurationInstance = new ConfigurationInstance(this, scriptingHelper, properties, steps, requestBody, requestParams, mappingHelper, requestOrigin, tracer);
        configurationInstance.execute(configuration);
        return configurationInstance.getReturnValue();
    }
}
