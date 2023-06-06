package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.helper.*;
import ee.buerokratt.ruuter.helper.exception.LoadDslsException;
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
public class DslService {
    private final ApplicationProperties properties;
    private final DslMappingHelper dslMappingHelper;
    private final ExternalForwardingHelper externalForwardingHelper;
    private final ScriptingHelper scriptingHelper;
    private final MappingHelper mappingHelper;
    private final HttpHelper httpHelper;
    private final Tracer tracer;

    private Map<String, Map<String, Map<String, DslStep>>> dsls;

    public static final String UNSUPPORTED_FILETYPE_ERROR_MESSAGE = "Unsupported filetype";

    public DslService(ApplicationProperties properties, DslMappingHelper dslMappingHelper, ScriptingHelper scriptingHelper, Tracer tracer, MappingHelper mappingHelper, HttpHelper httpHelper, ExternalForwardingHelper externalForwardingHelper) {
        this.dslMappingHelper = dslMappingHelper;
        this.properties = properties;
        this.scriptingHelper = scriptingHelper;
        this.dsls = getDsls(properties.getConfigPath());
        this.tracer = tracer;
        this.mappingHelper = mappingHelper;
        this.httpHelper = httpHelper;
        this.externalForwardingHelper = externalForwardingHelper;
    }

    public void reloadDsls() {
        this.dsls = getDsls(properties.getConfigPath());
    }

    public Map<String, Map<String, Map<String, DslStep>>> getDsls(String configPath) {
        return Arrays.stream(Objects.requireNonNull(new File(configPath).listFiles(File::isDirectory))).collect(toMap(File::getName, directory -> {
            try (Stream<Path> paths = Files.walk(getFolderPath(directory.toString())).filter(path -> {
                if (!FileUtils.isAllowedFiletype(path, properties.getDsl().getAllowedFiletypes()))
                    throw new IllegalArgumentException(UNSUPPORTED_FILETYPE_ERROR_MESSAGE+" "+path.toString().substring(path.toString().lastIndexOf('.'))+" ("+path+")");
                return true;
            }).filter(FileUtils::isYmlFile)) {
                return paths
                    .filter(Files::isRegularFile)
                    .collect(toMap(FileUtils::getFileNameWithPathWithoutSuffix, dslMappingHelper::getDslSteps));
            } catch (Exception e) {
                throw new LoadDslsException(e);
            }
        }));
    }

    public DslInstance execute(String dsl, String requestType, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String requestOrigin) {
        return execute(dsl, requestType, requestBody, requestQuery, requestHeaders, requestOrigin, this.getClass().getName());
    }
    public DslInstance execute(String dsl, String requestType, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String requestOrigin, String contentType) {
        DslInstance di = new DslInstance(dsl, dsls.get(requestType.toUpperCase()).get(dsl), requestBody, requestQuery, requestHeaders, requestOrigin, this, properties, scriptingHelper, mappingHelper, httpHelper, tracer);

        if (di.getSteps() != null) {
            LoggingUtils.logInfo(log, "Request received for DSL: %s".formatted(dsl), requestOrigin, INCOMING_REQUEST);
            if (allowedToExecuteDsl(requestBody, requestQuery, requestHeaders, contentType)) {
                di.execute();
            }
            LoggingUtils.logInfo(log, "Request processed for DSL: %s".formatted(dsl), requestOrigin, INCOMING_RESPONSE);
        } else {
            LoggingUtils.logError(log, "Received request for non existent DSL: %s".formatted(dsl), requestOrigin, INCOMING_REQUEST);
        }

        return di;
    }

    private boolean allowedToExecuteDsl(Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String contentType) {
        if (externalForwardingHelper.shouldForwardRequest()) {
            ResponseEntity<Object> response = externalForwardingHelper.forwardRequest(requestBody, requestQuery, requestHeaders, contentType);
            return externalForwardingHelper.isAllowedForwardingResponse(response.getStatusCodeValue());
        }
        return true;
    }
}
