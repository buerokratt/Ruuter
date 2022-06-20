package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.domain.RuuterResponse;
import ee.buerokratt.ruuter.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @RequestMapping(value = "/{configuration}", method = {GET, POST})
    public ResponseEntity<RuuterResponse> queryConfiguration(@PathVariable String configuration,
                                                             @RequestBody(required = false) Map<String, String> requestBody,
                                                             @RequestParam(required = false) Map<String, String> requestParams,
                                                             HttpServletRequest request) {
        return ok(new RuuterResponse(configurationService.execute(configuration, requestBody, requestParams, request.getRemoteAddr())));
    }
}
