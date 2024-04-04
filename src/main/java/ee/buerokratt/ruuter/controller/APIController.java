package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.domain.HeartBeatInfo;
import ee.buerokratt.ruuter.service.DslService;
import ee.buerokratt.ruuter.service.HeartBeatService;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestController
@RequiredArgsConstructor
public class APIController {
    private final DslService dslService;

    @GetMapping("/api")
    public ResponseEntity<Object> openApiSpec(){
        return status(200).contentType(MediaType.APPLICATION_JSON).body(Json.pretty(dslService.getOpenAPISpec()));
    }
}
