package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.domain.RuuterResponse;
import ee.buerokratt.ruuter.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @RequestMapping(value = "/{configuration}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<RuuterResponse> queryConfiguration(@PathVariable("configuration") String code,
                                                             @RequestBody(required = false) Map<String, String> requestBody,
                                                             @RequestParam(required = false) Map<String, String> requestParams) {
        return ok(new RuuterResponse(configurationService.executeConfiguration(code, requestBody, requestParams)));
    }
}
