package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.RuuterResponse;
import ee.buerokratt.ruuter.service.DslService;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ee.buerokratt.ruuter.util.LoggingUtils.INCOMING_REQUEST;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "**")
public class DslController {
    private final DslService dslService;
    private final ApplicationProperties properties;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> queryDslMultipart(@RequestBody(required = true) MultipartFile[] file,
                                           @RequestParam(required = false) Map<String, Object> requestQuery,
                                           @RequestHeader(required = false) Map<String, String> requestHeaders,
                                           HttpServletRequest request) {
        Map<String, Object> body = Arrays.stream(file).collect(Collectors.toMap(
            f -> f.getOriginalFilename(),
            f -> {
                try {
                    return new String(f.getBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        return queryDsl(body, requestQuery, requestHeaders, request);
    }

    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Object> queryDslFormdata(@RequestBody(required = true) MultiValueMap<String, Object> requestBody,
                                           @RequestParam(required = false) Map<String, Object> requestQuery,
                                           @RequestHeader(required = false) Map<String, String> requestHeaders,
                                           HttpServletRequest request) {
        return queryDsl(requestBody.toSingleValueMap(), requestQuery,requestHeaders, request);
    }

    @RequestMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> queryDsl(@RequestBody(required = false) Map<String, Object> requestBody,
                                                             @RequestParam(required = false) Map<String, Object> requestQuery,
                                                             @RequestHeader(required = false) Map<String, String> requestHeaders,
                                                             HttpServletRequest request) {
        String dsl = request.getRequestURI();
        // Remove '/' from beginning to read DSLs properly
        if (dsl.startsWith("/")) dsl = dsl.substring(1);
        if (!properties.getIncomingRequests().getAllowedMethodTypes().contains(request.getMethod())) {
            String errorMsg = "Request received with invalid method type %s for DSL: %s".formatted(request.getMethod(), dsl);
            LoggingUtils.logError(log, errorMsg, request.getRemoteAddr(), INCOMING_REQUEST);
            return status(HttpStatus.METHOD_NOT_ALLOWED).body(new RuuterResponse());
        }

        DslInstance di = dslService.execute(dsl, request.getMethod(), requestBody, requestQuery, requestHeaders, request.getRemoteAddr());

        Object returnObj;
        if (di.isReturnWithWrapper()) returnObj = new RuuterResponse(di.getReturnValue());
        else returnObj = di.getReturnValue();

        if (di.getProperties().getLogging().getMeaningfulErrors() && di.getErrorMessage() != null) {
            String errorMsg = "DSL %s caught error: %s";
            LoggingUtils.logError(log, errorMsg.formatted(dsl, di.getErrorMessage()), request.getRemoteAddr(), INCOMING_REQUEST);

            if (di.getReturnValue() == null) {
                returnObj = new HashMap<>() {{ put("error", di.getErrorMessage()); }};
             }
            di.setReturnStatus(di.getErrorStatus().value());
        }

        return status(di.getReturnStatus() == null ? getReturnStatus(di.getReturnValue()) : HttpStatus.valueOf(di.getReturnStatus()))
            .headers(httpHeaders -> di.getReturnHeaders().forEach(httpHeaders::add))
            .body(returnObj);
    }

    @PostMapping(consumes = "text/*")
    public ResponseEntity<Object> queryDslText(@RequestBody(required = false) String requestBody,
                                           @RequestParam(required = false) Map<String, Object> requestQuery,
                                           @RequestHeader(required = false) Map<String, String> requestHeaders,
                                           HttpServletRequest request) {
        String subType = request.getContentType().substring(5);
        log.info("queryDSL for media type text/"+ subType);
        return queryDsl(
            requestBody != null ? Map.of(subType, requestBody) : Map.of(),
            requestQuery,requestHeaders, request);
    }

    private HttpStatus getReturnStatus(Object response) {
        Integer dslWithResponseStatusCode = properties.getFinalResponse().getDslWithResponseHttpStatusCode();
        Integer dslWithoutResponseStatusCode = properties.getFinalResponse().getDslWithoutResponseHttpStatusCode();
        Integer finalResponseStatusCode = response == null ? dslWithoutResponseStatusCode : dslWithResponseStatusCode;
        return finalResponseStatusCode != null ? HttpStatus.valueOf(finalResponseStatusCode) : HttpStatus.OK;
    }
}
