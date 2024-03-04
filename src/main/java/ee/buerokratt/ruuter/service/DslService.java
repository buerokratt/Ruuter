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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static ee.buerokratt.ruuter.util.FileUtils.getFolderPath;
import static ee.buerokratt.ruuter.util.LoggingUtils.*;
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

    private Map<String, Map<String, Map<String, Map<String, DslStep>>>> dsls;

    private Map<String, Map<String, Map<String, Map<String, DslStep>>>> guards;

    public static final String UNSUPPORTED_FILETYPE_ERROR_MESSAGE = "Unsupported filetype";

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

    public Map<String, Map<String, Map<String, Map<String, DslStep>>>> getDsls(String configPath) {
        Map<String, Map<String, Map<String, Map<String, DslStep>>>> _dsls =
            Arrays.stream(Objects.requireNonNull(new File(configPath).listFiles(File::isDirectory)))
                .collect(toMap(File::getName, f -> getDslsForProject(configPath+"/" + f.getName()+"/")));
        return _dsls;
    }

    public Map<String, Map<String, Map<String, DslStep>>> getDslsForProject(String projectPath) {
        Map<String, Map<String, Map<String, DslStep>>> _dsls =
             Arrays.stream(Objects.requireNonNull(new File(projectPath).listFiles(File::isDirectory)))
                   .collect(toMap(File::getName, this::extractDSL));
        log.debug("Loaded DSLs: " + mapDeepToString(_dsls));
        return _dsls;
    }
    private Map<String, Map<String, DslStep>> extractDSL(File directory) {
        try (Stream<Path> paths = Files.walk(getFolderPath(directory.toString()))
            .filter(path -> !FileUtils.isGuard(path))
            .filter(path -> {
                if (!FileUtils.isFiletype(path, properties.getDsl().getAllowedFiletypes()))
                    throw new IllegalArgumentException(UNSUPPORTED_FILETYPE_ERROR_MESSAGE + " " + path.toString().substring(path.toString().lastIndexOf('.')) + " (" + path + ")");
                return true;
            }).filter(path -> FileUtils.isFiletype(path, properties.getDsl().getProcessedFiletypes()))) {
            return paths
                .filter(Files::isRegularFile)
                .collect(toMap(FileUtils::getFileNameWithPathWithoutSuffix, dslMappingHelper::getDslSteps));
        } catch (Exception e) {
            throw new LoadDslsException(e);
        }
    }

    public Map<String, Map<String, Map<String, Map<String, DslStep>>>> getGuards(String configPath) {
        Map<String, Map<String, Map<String, Map<String, DslStep>>>> _dsls =
            Arrays.stream(Objects.requireNonNull(new File(configPath).listFiles(File::isDirectory)))
                .collect(toMap(File::getName, f -> getGuardsForProject(configPath+"/" + f.getName()+"/")));
        log.debug("Loaded Guards: " + mapDeepToString(_dsls));
        return _dsls;
    }

    public Map<String, Map<String, Map<String, DslStep>>> getGuardsForProject(String projectPath) {
        Map<String, Map<String, Map<String, DslStep>>> _dsls =
            Arrays.stream(Objects.requireNonNull(new File(projectPath).listFiles(File::isDirectory)))
                .collect(toMap(File::getName, this::extractGuard));
        return _dsls;
    }

    private Map<String, Map<String, DslStep>> extractGuard(File directory) {
        try (Stream<Path> paths = Files.walk(getFolderPath(directory.toString()))
            .filter(path -> FileUtils.isGuard(path))) {
            return paths
                .filter(Files::isRegularFile)
                .collect(toMap(FileUtils::getGuardWithPath, dslMappingHelper::getDslSteps));
        } catch (Exception e) {
            throw new LoadDslsException(e);
        }
    }

    public DslInstance execute(String dsl, String requestType, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String requestOrigin) {
        return execute(dsl, requestType, requestBody, requestQuery, requestHeaders, requestOrigin, this.getClass().getName());
    }

    public DslInstance execute(String dsl, String requestType, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String requestOrigin, String contentType) {
        String project = dsl.substring(0, dsl.indexOf('/'));
        dsl = dsl.substring(dsl.indexOf('/')+1);
        return execute(project, dsl, requestType, requestBody, requestQuery,requestHeaders, requestOrigin, contentType);
    }
    public DslInstance execute(String project, String dsl, String requestType, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String requestOrigin, String contentType) {

        log.info("Loading DSL: "+ dsl + " from project: " + project);
        Map<String, DslStep> dslSteps = dsls.containsKey(project) ?
            dsls.get(project)
            .get(requestType.toUpperCase())
            .get(dsl) :
            null;
        log.debug("DSL: "+ dslSteps);

        DslInstance di = new DslInstance(dsl, requestType.toUpperCase(), dslSteps, requestBody, requestQuery, requestHeaders, requestOrigin, this, properties, scriptingHelper, mappingHelper, httpHelper, tracer, openSearchSender);

        if (di.getSteps() != null) {
            LoggingUtils.logInfo(log, "Request received for DSL: %s".formatted(dsl), requestOrigin, INCOMING_REQUEST);

            if ( !allowedToExecuteDSLFrom(di, requestOrigin, requestHeaders.get("referer"))) {
                LoggingUtils.logError(log, "Internal DSL not allowed: %s".formatted(dsl), requestOrigin, INCOMING_RESPONSE);
                return di;
            };

            DslInstance guard = new DslInstance(dsl, requestType.toUpperCase(), getGuard(project, requestType.toUpperCase(), dsl), requestBody, requestBody, requestHeaders, requestOrigin, this, properties, scriptingHelper, mappingHelper, httpHelper, tracer, openSearchSender);
            if (guard != null && guard.getSteps() != null) {
                LoggingUtils.logInfo(log, "Executing guard for DSL: %s".formatted(dsl), requestOrigin, INCOMING_REQUEST);
                guard.execute();

                // In case the guard does not specifically return a status code or throw an exception, it
                // should be considered as HTTP OK.
                if (guard.getReturnStatus() != null)
                    guard.setReturnStatus(HttpStatus.OK.value());

                if (guard.getReturnStatus() != HttpStatus.OK.value()) {
                    LoggingUtils.logError(log, "Guard failed for DSL: %s".formatted(dsl), requestOrigin, INCOMING_RESPONSE);
                    return guard;
                }
            }

            if (allowedToExecuteDsl(dsl, requestBody, requestQuery, requestHeaders, contentType, di)) {
                di.execute();
            }
            LoggingUtils.logInfo(log, "Request processed for DSL: %s".formatted(dsl), requestOrigin, INCOMING_RESPONSE);
        } else {
            LoggingUtils.logError(log, "Received request for non existent DSL: %s".formatted(dsl), requestOrigin, INCOMING_REQUEST);
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
        return ipAllowed && urlAllowed;
    }

    private Map<String, DslStep> getGuard(String project, String method, String dslPath) {
        if (dslPath.length()<=1)
            return null;
        String path = dslPath.contains("/") ? dslPath.substring(0, dslPath.lastIndexOf('/')) : "";
        return guards.get(project).get(method).containsKey(path) ? guards.get(project).get(method).get(path) : getGuard(project, method, path);
    }
}
