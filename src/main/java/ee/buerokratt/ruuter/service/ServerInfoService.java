package ee.buerokratt.ruuter.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class ServerInfoService {

    @Getter
    private final long startupTime;

    public ServerInfoService() {
        this.startupTime = new Date().getTime();
    }

    public long getServerTime() {
        return new Date().getTime();
    }

}
