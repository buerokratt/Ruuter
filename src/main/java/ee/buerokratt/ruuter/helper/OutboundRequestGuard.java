package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.exception.SsrfGuardException;
import ee.buerokratt.ruuter.util.Cidr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Blocks outbound HTTP requests (issued via {@link HttpHelper}) that resolve to internal/private
 * networks, so DSL-defined services cannot be used to reach infrastructure such as the Kubernetes
 * API or cloud metadata endpoints (SSRF).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundRequestGuard {

    private static final List<String> DEFAULT_BLOCKED_NETWORKS = List.of(
        "127.0.0.0/8",     // loopback
        "10.0.0.0/8",      // RFC1918
        "172.16.0.0/12",   // RFC1918
        "192.168.0.0/16",  // RFC1918
        "169.254.0.0/16",  // link-local, covers the 169.254.169.254 cloud metadata endpoint
        "100.64.0.0/10",   // carrier-grade NAT, used by some cloud metadata proxies
        "::1/128",         // loopback
        "fc00::/7",        // unique local addresses
        "fe80::/10"        // link-local
    );

    private final ApplicationProperties properties;

    public void assertAllowed(String rawUrl) {
        ApplicationProperties.OutboundRequests config = properties.getOutboundRequests();
        if (config != null && Boolean.FALSE.equals(config.getEnabled())) {
            return;
        }

        String host = URI.create(rawUrl).getHost();
        if (host == null) {
            throw new SsrfGuardException("Request URL has no host: " + rawUrl);
        }

        if (isExplicitlyAllowed(host, config)) {
            return;
        }

        List<Cidr> blockedNetworks = blockedNetworks(config);
        for (InetAddress address : resolve(host)) {
            if (blockedNetworks.stream().anyMatch(network -> network.contains(address))) {
                log.warn("Blocked outbound request to '{}' ({}): target resolves to a restricted network", host, address.getHostAddress());
                throw new SsrfGuardException("Target host resolves to a restricted network: " + host);
            }
        }
    }

    private boolean isExplicitlyAllowed(String host, ApplicationProperties.OutboundRequests config) {
        return config != null && config.getAllowedHosts() != null
            && config.getAllowedHosts().stream().anyMatch(host::equalsIgnoreCase);
    }

    private List<Cidr> blockedNetworks(ApplicationProperties.OutboundRequests config) {
        List<String> networks = new ArrayList<>(DEFAULT_BLOCKED_NETWORKS);
        if (config != null && config.getBlockedNetworks() != null) {
            networks.addAll(config.getBlockedNetworks());
        }
        return networks.stream().map(Cidr::parse).toList();
    }

    private InetAddress[] resolve(String host) {
        try {
            return InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new SsrfGuardException("Could not resolve host: " + host, e);
        }
    }
}
