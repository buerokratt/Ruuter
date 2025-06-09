package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.PackageVersionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ee.buerokratt.ruuter.configuration.PackageInfoConfiguration;
import ee.buerokratt.ruuter.domain.HeartBeatInfo;

@Slf4j
@Service
public class HeartBeatService {

    private final ServerInfoService serverInfoService;

    private final PackageInfoConfiguration packageInfoConfiguration;

    private final PackageVersionConfiguration packageVersionConfiguration;

    private String version;

    public HeartBeatService(ServerInfoService serverInfoService,
                            PackageInfoConfiguration packageInfoConfiguration,
                            PackageVersionConfiguration packageVersionConfiguration) {
        this.serverInfoService = serverInfoService;
        this.packageInfoConfiguration = packageInfoConfiguration;
        this.packageVersionConfiguration = packageVersionConfiguration;
    }

    private String getVersion() {
        if (version == null)
            version =   "v" +
                packageVersionConfiguration.getMajor() + "." +
                packageVersionConfiguration.getMinor() + "." +
                packageVersionConfiguration.getPatch();
        return version;
    }


    public HeartBeatInfo getData() {
        return HeartBeatInfo.builder()
            .appName(packageInfoConfiguration.getAppName())
            .packagingTime(packageVersionConfiguration.getBuildTime())
            .version(getVersion())
            .appStartTime(serverInfoService.getStartupTime())
            .serverTime(serverInfoService.getServerTime())
            .build();
    }

}
