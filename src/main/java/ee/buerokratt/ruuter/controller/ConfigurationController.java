package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.RuuterResponse;
import ee.buerokratt.ruuter.service.ConfigurationService;
import ee.buerokratt.ruuter.service.exception.InvalidIncomingRequestMethodType;
import ee.buerokratt.ruuter.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;
    private final ApplicationProperties properties;

    @RequestMapping(value = "/{configuration}")
    public ResponseEntity<RuuterResponse> queryConfiguration(@PathVariable String configuration,
                                                             @RequestBody(required = false) Map<String, String> requestBody,
                                                             @RequestParam(required = false) Map<String, String> requestParams,
                                                             HttpServletRequest request) {
        if (properties.getIncomingRequests().getAllowedMethodTypes().contains(request.getMethod())) {
            return ok(new RuuterResponse(configurationService.execute(configuration, requestBody, requestParams, request.getRemoteAddr())));
        }
        LoggingUtils.logIncorrectIncomingRequest(log, configuration, request.getRemoteAddr(), request.getMethod());
        throw new InvalidIncomingRequestMethodType(request.getMethod());
    }

}
