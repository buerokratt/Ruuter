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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static ee.buerokratt.ruuter.util.FileUtils.getFolderPath;
import static ee.buerokratt.ruuter.util.LoggingUtils.INCOMING_REQUEST;
import static ee.buerokratt.ruuter.util.LoggingUtils.INCOMING_RESPONSE;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class ConfigurationService {
    private final ApplicationProperties properties;
    private final ConfigurationMappingHelper configurationMappingHelper;
    private final ExternalForwardingHelper externalForwardingHelper;
    private final ScriptingHelper scriptingHelper;
    private final MappingHelper mappingHelper;
    private final HttpHelper httpHelper;
    private final Tracer tracer;

    private final Map<String, Map<String, Map<String, ConfigurationStep>>> configurations;

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

    public ConfigurationInstance execute(String configuration, String requestType, Map<String, Object> requestBody, Map<String, Object> requestParams, String requestOrigin) {
        ConfigurationInstance ci = new ConfigurationInstance(configuration, configurations.get(requestType.toUpperCase()).get(configuration), requestBody, requestParams, requestOrigin, this, properties, scriptingHelper, mappingHelper, httpHelper, tracer);

        if (ci.getSteps() != null) {
            LoggingUtils.logInfo(log, "Request received for configuration: %s".formatted(configuration), requestOrigin, INCOMING_REQUEST);
            if (allowedToExecuteConfiguration(requestBody, requestParams)) {
                ci.execute();
            }
            LoggingUtils.logInfo(log, "Request processed for configuration: %s".formatted(configuration), requestOrigin, INCOMING_RESPONSE);
        } else {
            LoggingUtils.logError(log, "Received request for non existent configuration: %s".formatted(configuration), requestOrigin, INCOMING_REQUEST);
        }

        return ci;
    }

    private boolean allowedToExecuteConfiguration(Map<String, Object> requestBody, Map<String, Object> requestParams) {
        if (externalForwardingHelper.shouldForwardRequest()) {
            ResponseEntity<Object> response = externalForwardingHelper.forwardRequest(requestBody, requestParams);
            return externalForwardingHelper.isAllowedForwardingResponse(response.getStatusCodeValue());
        }
        return true;
    }
}
