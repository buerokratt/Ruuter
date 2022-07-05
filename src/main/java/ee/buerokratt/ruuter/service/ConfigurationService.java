package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.helper.*;
import ee.buerokratt.ruuter.helper.exception.LoadConfigurationsException;
import ee.buerokratt.ruuter.util.FileUtils;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.ResponseEntity;
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
    private final MappingHelper mappingHelper;
    private final HttpHelper httpHelper;
    private final ExternalForwardingHelper externalForwardingHelper;

    private final Map<String, Map<String, ConfigurationStep>> configurations;

    public ConfigurationService(ApplicationProperties properties, ConfigurationMappingHelper configurationMappingHelper, ScriptingHelper scriptingHelper, Tracer tracer, MappingHelper mappingHelper, HttpHelper httpHelper, ExternalForwardingHelper externalForwardingHelper) {
        this.configurationMappingHelper = configurationMappingHelper;
        this.properties = properties;
        this.scriptingHelper = scriptingHelper;
        this.configurations = getConfigurations(properties.getConfigPath());
        this.tracer = tracer;
        this.mappingHelper = mappingHelper;
        this.httpHelper = httpHelper;
        this.externalForwardingHelper = externalForwardingHelper;
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

    public Object execute(String configuration, Map<String, Object> requestBody, Map<String, Object> requestParams, String requestOrigin) {
        Map<String, ConfigurationStep> steps = configurations.get(configuration);
        LoggingUtils.logIncomingRequest(log, configuration, requestOrigin);

        ConfigurationInstance configurationInstance = new ConfigurationInstance(this, scriptingHelper, properties, steps, requestBody, requestParams, mappingHelper, requestOrigin, tracer, httpHelper);
        if (allowedToExecuteConfiguration(requestBody, requestParams)) {
            configurationInstance.execute(configuration);
        }

        LoggingUtils.logRequestProcessed(log, configuration, requestOrigin);
        return configurationInstance.getReturnValue();
    }

    private boolean allowedToExecuteConfiguration(Map<String, Object> requestBody, Map<String, Object> requestParams) {
        if (externalForwardingHelper.shouldForwardRequest()) {
            ResponseEntity<Object> stringHttpResponse = externalForwardingHelper.forwardRequest(requestBody, requestParams);
            return externalForwardingHelper.isAllowedForwardingResponse(stringHttpResponse.getStatusCodeValue());
        }
        return true;
    }
}
