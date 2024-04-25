package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.Dsl;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.helper.*;
import ee.buerokratt.ruuter.helper.exception.LoadDslsException;
import ee.buerokratt.ruuter.util.FileUtils;
import ee.buerokratt.ruuter.util.LoggingUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.core.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private final OpenSearchSender openSearchSender;

    private Map<String, Map<String, Dsl>> dsls;

    private Map<String, Map<String, Dsl>> guards;

    public static final String UNSUPPORTED_FILETYPE_ERROR_MESSAGE = "Unsupported filetype";


    private OpenApiBuilder openApiBuilder;
    private OpenAPI openAPI;

    public DslService(ApplicationProperties properties, DslMappingHelper dslMappingHelper, ScriptingHelper scriptingHelper,
                      Tracer tracer, MappingHelper mappingHelper, HttpHelper httpHelper,
                      ExternalForwardingHelper externalForwardingHelper, OpenSearchSender openSearchSender) {
        this.dslMappingHelper = dslMappingHelper;
        this.properties = properties;
        this.dslMappingHelper.properties = properties;
        this.scriptingHelper = scriptingHelper;
        this.dsls = getDsls(properties.getConfigPath());
        this.guards = getGuards(properties.getConfigPath());
        this.tracer = tracer;
        this.mappingHelper = mappingHelper;
        this.httpHelper = httpHelper;
        this.externalForwardingHelper = externalForwardingHelper;
        this.openSearchSender = openSearchSender;
    }

    public void reloadDsls() {
        this.dsls = getDsls(properties.getConfigPath());
        this.guards = getGuards(properties.getConfigPath());
    }

    public Map<String, Map<String, Dsl>> getDsls(String configPath) {
        openApiBuilder = new OpenApiBuilder("BYK", "1.0");

        Map<String, Map<String, Dsl>> _dsls =
               Arrays.stream(Objects.requireNonNull(new File(configPath).listFiles(File::isDirectory)))
                   .collect(toMap(File::getName, directory -> {
                       return getDslDirectory(directory);
                   }));
        log.info("Built OpenAPI spec: " + Yaml.pretty(getOpenAPISpec()));
        //writeSpecToFile();
        return _dsls;
    }

    private Map<String, Dsl> getDslDirectory(File directory) {
        try (Stream<Path> paths = Files.walk(getFolderPath(directory.toString()))
            .filter(path -> !FileUtils.isGuard(path))
            .filter(path -> {
            if (!FileUtils.isFiletype(path, properties.getDsl().getAllowedFiletypes()))
                throw new IllegalArgumentException(UNSUPPORTED_FILETYPE_ERROR_MESSAGE + " " + path.toString().substring(path.toString().lastIndexOf('.')) + " (" + path + ")");
            return true;
        }).filter(path -> FileUtils.isFiletype(path, properties.getDsl().getProcessedFiletypes()))) {
            return paths
                .filter(Files::isRegularFile)
                .collect(toMap(FileUtils::getFileNameWithPathWithoutSuffix, this::getDslFromPath));
        } catch (Exception e) {
            throw new LoadDslsException(e);
        }
    }

    private Dsl getDslFromPath(Path path) {
        Dsl dsl = dslMappingHelper.getDslSteps(path);
        if (dsl.getDeclaration() == null)
            log.warn("Found DSL without declaration: {}", path.toString());
        else {
            openApiBuilder.addService(dsl, FileUtils.getFileNameWithPathWithoutSuffix(path));
        }
        return dsl;
    }

    public Map<String, Map<String, Dsl>> getGuards(String configPath) {
        Map<String, Map<String, Dsl>> _dsls = Arrays.stream(Objects.requireNonNull(new File(configPath).listFiles(File::isDirectory))).collect(toMap(File::getName, directory -> {
            try (Stream<Path> paths = Files.walk(getFolderPath(directory.toString()))
                .filter(path -> FileUtils.isGuard(path))) {
                return paths
                    .filter(Files::isRegularFile)
                    .collect(toMap(FileUtils::getGuardWithPath, dslMappingHelper::getDslSteps));
            } catch (Exception e) {
                throw new LoadDslsException(e);
            }
        }));
        return _dsls;
    }

    public DslInstance execute(String dsl, String requestType, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String requestOrigin) {
        return execute(dsl, requestType, requestBody, requestQuery, requestHeaders, requestOrigin, this.getClass().getName());
    }
    public DslInstance execute(String dslName, String requestType, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String requestOrigin, String contentType) {

        Dsl dsl = dsls.get(requestType.toUpperCase()).get(dslName);
        Map<String, DslStep> steps = null;

        if (dsl != null) {
            steps = dsl.steps();
            log.debug("body before: {}", LoggingUtils.mapDeepToString(requestBody));

            if (dsl.getDeclaration() != null) {
                requestBody = filterFields(requestBody, dsl.getDeclaration().getAllowedBody());
                requestHeaders = filterFields(requestHeaders, dsl.getDeclaration().getAllowedHeader());
                requestQuery = filterFields(requestQuery, dsl.getDeclaration().getAllowedParams());
            }
            log.debug("body after: "+ LoggingUtils.mapDeepToString(requestBody));
        } else {
            log.info("Executing DSLv1 (without declare)");
            steps = null;
        }

        DslInstance di = new DslInstance(dslName,
            requestType.toUpperCase(),
            steps,
            requestBody,
            requestQuery,
            requestHeaders,
            requestOrigin,
            this,
            properties, scriptingHelper, mappingHelper, httpHelper, tracer, openSearchSender);

        if (steps != null) {
            LoggingUtils.logInfo(log, "Request received for DSL: %s".formatted(dslName), requestOrigin, INCOMING_REQUEST);

            if ( !allowedToExecuteDSLFrom(di, requestOrigin, requestHeaders.get("referer"))) {
                LoggingUtils.logError(log, "Internal DSL not allowed: %s (%s)".formatted(dslName, requestOrigin), requestOrigin, INCOMING_RESPONSE);
                return di;
            };

            DslInstance guard = new DslInstance(dslName, requestType.toUpperCase(),
                getGuard(requestType.toUpperCase(), dslName),
                requestBody,
                requestQuery,
                requestHeaders,
                requestOrigin,
                this,
                properties, scriptingHelper, mappingHelper, httpHelper, tracer, openSearchSender);

            if (guard != null && guard.getSteps() != null) {
                LoggingUtils.logInfo(log, "Executing guard for DSL: %s".formatted(dslName), requestOrigin, INCOMING_REQUEST);
                guard.execute();

                // In case the guard does not specifically return a status code or throw an exception, it
                // should be considered as HTTP OK.
                if (guard.getReturnStatus() != null)
                    guard.setReturnStatus(HttpStatus.OK.value());

                if (guard.getReturnStatus() != HttpStatus.OK.value()) {
                    LoggingUtils.logError(log, "Guard failed for DSL: %s (%s)".formatted(dslName, requestOrigin), requestOrigin, INCOMING_RESPONSE);
                    return guard;
                }
            }

            if (allowedToExecuteDsl(dslName, requestBody, requestQuery, requestHeaders, contentType, di)) {
                di.execute();
            }
            LoggingUtils.logInfo(log, "Request processed for DSL: %s".formatted(dslName), requestOrigin, INCOMING_RESPONSE);
        } else {
            LoggingUtils.logError(log, "Received request for non existent DSL: %s".formatted(dslName), requestOrigin, INCOMING_REQUEST);
        }

        return di;
    }

    private boolean allowedToExecuteDsl(String dsl, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String contentType, DslInstance di) {
        if (externalForwardingHelper.shouldForwardRequest()) {
            ResponseEntity<Object> response = externalForwardingHelper.forwardRequest(dsl, requestBody, requestQuery, requestHeaders, contentType, di);
            return externalForwardingHelper.isAllowedForwardingResponse(response.getStatusCodeValue());
        }
        return true;
    }

    private boolean allowedToExecuteDSLFrom(DslInstance dsl, String origin, String referer) {
        if (!dsl.isInternal())
            return true;
        boolean ipAllowed = properties.getInternalRequests().getAllowedIPs().contains(origin);
        boolean urlAllowed = properties.getInternalRequests().getAllowedURLs().contains(referer);
        return ipAllowed || urlAllowed;
    }

    private Map<String, DslStep> getGuard(String method, String dslPath) {
        if (dslPath.length()<=1)
            return null;
        String path = dslPath.contains("/") ? dslPath.substring(0, dslPath.lastIndexOf('/')) : "";
        return guards.get(method).containsKey(path) ? guards.get(method).get(path).steps() : getGuard(method, path);
    }

    <V> Map<String, V> filterFields(Map<String, V> requestFields, List<String> allowedFields) {
        return allowedFields == null ?
                        requestFields :
                        requestFields == null ?
                            null :
                            requestFields.entrySet().stream().filter(e -> allowedFields.contains(e.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public OpenAPI getOpenAPISpec() {
        if (openApiBuilder == null)
            throw new RuntimeException("OpenAPI spec not generated");
        if (openAPI == null)
            openAPI = openApiBuilder.build();
        return openAPI;
    }

    private void writeSpecToFile() {
        if (openApiBuilder == null)
            return;
        try {
            try (FileWriter specWriter = new FileWriter("byk_spec.yaml")) {
                specWriter.write(Yaml.pretty(getOpenAPISpec()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not write OpenAPI spec to file: ",e);
        }
    }

}
