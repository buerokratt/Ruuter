package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.RuuterResponse;
import ee.buerokratt.ruuter.service.ConfigurationService;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static ee.buerokratt.ruuter.util.LoggingUtils.INCOMING_REQUEST;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;
    private final ApplicationProperties properties;

    @RequestMapping(value = "/{configuration}")
    public ResponseEntity<RuuterResponse> queryConfiguration(@PathVariable String configuration,
                                                             @RequestBody(required = false) Map<String, Object> requestBody,
                                                             @RequestParam(required = false) Map<String, Object> requestQuery,
                                                             @RequestHeader(required = false) Map<String, String> requestHeaders,
                                                             HttpServletRequest request) {
        if (!properties.getIncomingRequests().getAllowedMethodTypes().contains(request.getMethod())) {
            String errorMsg = "Request received with invalid method type %s for configuration: %s".formatted(request.getMethod(), configuration);
            LoggingUtils.logError(log, errorMsg, request.getRemoteAddr(), INCOMING_REQUEST);
            return status(HttpStatus.METHOD_NOT_ALLOWED).body(new RuuterResponse());
        }
        ConfigurationInstance ci = configurationService.execute(configuration, request.getMethod(),  requestBody, requestQuery, requestHeaders, request.getRemoteAddr());
        return status(ci.getReturnStatus() == null ? getReturnStatus() : HttpStatus.valueOf(ci.getReturnStatus()))
            .headers(httpHeaders -> ci.getReturnHeaders().forEach(httpHeaders::add))
            .body(new RuuterResponse(ci.getReturnValue()));
    }

    private HttpStatus getReturnStatus() {
        Integer finalResponseStatusCode = properties.getFinalResponse().getHttpStatusCode();
        return finalResponseStatusCode != null ? HttpStatus.valueOf(finalResponseStatusCode) : HttpStatus.OK;
    }
}
