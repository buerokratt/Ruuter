package ee.buerokratt.ruuter.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeartBeatInfo {
    private String appName;
    private String version;
    private long packagingTime;
    private long appStartTime;
    private long serverTime;
}
