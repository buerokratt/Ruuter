package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.PackageVersionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ee.buerokratt.ruuter.configuration.PackageInfoConfiguration;
import ee.buerokratt.ruuter.domain.HeartBeatInfo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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


    private String getChangelog() {
        String changelogBase64 = packageVersionConfiguration.getChangelogBase64();
        if (changelogBase64 == null || changelogBase64.isBlank())
            return "";

        try {
            return new String(Base64.getDecoder().decode(changelogBase64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.warn("Could not decode CHANGELOG from .env - expected base64: {}", e.getMessage());
            return "";
        }
    }

    public HeartBeatInfo getData() {
        return HeartBeatInfo.builder()
            .appName(packageInfoConfiguration.getAppName())
            .packagingTime(packageVersionConfiguration.getBuildTime())
            .version(getVersion())
            .appStartTime(serverInfoService.getStartupTime())
            .serverTime(serverInfoService.getServerTime())
            .changelog(getChangelog())
            .build();
    }

}
