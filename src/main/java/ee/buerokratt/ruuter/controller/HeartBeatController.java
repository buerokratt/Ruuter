package ee.buerokratt.ruuter.controller;

import ee.buerokratt.ruuter.domain.HeartBeatInfo;
import ee.buerokratt.ruuter.service.HeartBeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HeartBeatController {
    private final HeartBeatService heartBeatService;

    @GetMapping("/healthz")
    public HeartBeatInfo get() {
        return heartBeatService.getData();
    }
}
