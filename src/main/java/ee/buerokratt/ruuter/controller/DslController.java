package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.RuuterResponse;
import ee.buerokratt.ruuter.service.DslService;
import ee.buerokratt.ruuter.util.LoggingUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static ee.buerokratt.ruuter.util.LoggingUtils.INCOMING_REQUEST;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("**")
public class DslController {
    private final DslService dslService;
    private final ApplicationProperties properties;

    @RequestMapping()
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

        return status(di.getReturnStatus() == null ? getReturnStatus(di.getReturnValue()) : HttpStatus.valueOf(di.getReturnStatus()))
            .headers(httpHeaders -> di.getReturnHeaders().forEach(httpHeaders::add))
            .body(returnObj);
    }

    private HttpStatus getReturnStatus(Object response) {
        Integer dslWithResponseStatusCode = properties.getFinalResponse().getDslWithResponseHttpStatusCode();
        Integer dslWithoutResponseStatusCode = properties.getFinalResponse().getDslWithoutResponseHttpStatusCode();
        Integer finalResponseStatusCode = response == null ? dslWithoutResponseStatusCode : dslWithResponseStatusCode;
        return finalResponseStatusCode != null ? HttpStatus.valueOf(finalResponseStatusCode) : HttpStatus.OK;
    }
}
