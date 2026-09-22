package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.PackageInfoConfiguration;
import ee.buerokratt.ruuter.configuration.PackageVersionConfiguration;
import ee.buerokratt.ruuter.domain.HeartBeatInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeartBeatServiceTest {

    private PackageVersionConfiguration packageVersionConfiguration;
    private HeartBeatService heartBeatService;

    @BeforeEach
    void setUp() {
        ServerInfoService serverInfoService = mock(ServerInfoService.class);
        PackageInfoConfiguration packageInfoConfiguration = mock(PackageInfoConfiguration.class);
        packageVersionConfiguration = mock(PackageVersionConfiguration.class);
        // getData() unboxes this directly into HeartBeatInfo's primitive `long packagingTime` field -
        // an unstubbed (null) Long here would NPE on every test, regardless of what's under test.
        when(packageVersionConfiguration.getBuildTime()).thenReturn(123L);

        heartBeatService = new HeartBeatService(serverInfoService, packageInfoConfiguration, packageVersionConfiguration);
    }

    @Test
    void getData_shouldDecodeChangelog_whenValidBase64IsSet() {
        String encoded = Base64.getEncoder().encodeToString("### v1.2.3\n- did a thing".getBytes());
        when(packageVersionConfiguration.getChangelogBase64()).thenReturn(encoded);

        HeartBeatInfo result = heartBeatService.getData();

        assertEquals("### v1.2.3\n- did a thing", result.getChangelog());
    }

    // CHANGELOG is optional in .env (local/non-CI runs don't set it), and CI writes it via base64
    // -w0 which can't itself produce a blank value - either way it must not blow up getData().
    static Stream<String> blankOrMissingChangelogValues() {
        return Stream.of(null, "", "   ");
    }

    @ParameterizedTest
    @MethodSource("blankOrMissingChangelogValues")
    void getData_shouldReturnEmptyChangelog_whenNotSet(String rawValue) {
        when(packageVersionConfiguration.getChangelogBase64()).thenReturn(rawValue);

        HeartBeatInfo result = heartBeatService.getData();

        assertEquals("", result.getChangelog());
    }

    @Test
    void getData_shouldReturnEmptyChangelog_whenValueIsNotValidBase64() {
        when(packageVersionConfiguration.getChangelogBase64()).thenReturn("not-valid-base64!!!");

        HeartBeatInfo result = heartBeatService.getData();

        assertEquals("", result.getChangelog());
    }
}
