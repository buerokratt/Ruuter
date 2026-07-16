package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.exception.SsrfGuardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutboundRequestGuardTest {

    private ApplicationProperties properties;
    private OutboundRequestGuard guard;

    @BeforeEach
    void setUp() {
        properties = new ApplicationProperties();
        guard = new OutboundRequestGuard(properties);
    }

    @Test
    void assertAllowed_shouldBlockLoopbackAddressByDefault() {
        assertThrows(SsrfGuardException.class, () -> guard.assertAllowed("http://127.0.0.1:8080/secret"));
    }

    @Test
    void assertAllowed_shouldBlockLinkLocalMetadataAddressByDefault() {
        assertThrows(SsrfGuardException.class, () -> guard.assertAllowed("http://169.254.169.254/latest/meta-data"));
    }

    @Test
    void assertAllowed_shouldBlockRfc1918AddressByDefault() {
        assertThrows(SsrfGuardException.class, () -> guard.assertAllowed("http://10.0.0.5/api"));
    }

    @Test
    void assertAllowed_shouldAllowPublicAddress() {
        assertDoesNotThrow(() -> guard.assertAllowed("http://8.8.8.8/api"));
    }

    @Test
    void assertAllowed_shouldThrowWhenUrlHasNoHost() {
        assertThrows(SsrfGuardException.class, () -> guard.assertAllowed("/relative/path"));
    }

    @Test
    void assertAllowed_shouldThrowWhenHostDoesNotResolve() {
        assertThrows(SsrfGuardException.class, () -> guard.assertAllowed("http://definitely-not-a-real-host.invalid/x"));
    }

    @Test
    void assertAllowed_shouldSkipAllChecksWhenDisabled() {
        ApplicationProperties.OutboundRequests config = new ApplicationProperties.OutboundRequests();
        config.setEnabled(false);
        properties.setOutboundRequests(config);

        assertDoesNotThrow(() -> guard.assertAllowed("http://127.0.0.1/secret"));
    }

    @Test
    void assertAllowed_shouldAllowExplicitlyAllowedHostEvenIfItWouldOtherwiseResolveToPrivateRange() {
        ApplicationProperties.OutboundRequests config = new ApplicationProperties.OutboundRequests();
        config.setAllowedHosts(List.of("127.0.0.1"));
        properties.setOutboundRequests(config);

        assertDoesNotThrow(() -> guard.assertAllowed("http://127.0.0.1/secret"));
    }

    @Test
    void assertAllowed_shouldBlockAddressInAdditionalConfiguredNetwork() {
        ApplicationProperties.OutboundRequests config = new ApplicationProperties.OutboundRequests();
        config.setBlockedNetworks(List.of("8.8.8.0/24"));
        properties.setOutboundRequests(config);

        assertThrows(SsrfGuardException.class, () -> guard.assertAllowed("http://8.8.8.8/api"));
    }
}
