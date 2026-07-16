package ee.buerokratt.ruuter.util;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CidrTest {

    @Test
    void contains_shouldReturnTrueForAddressInsideIpv4Block() throws UnknownHostException {
        Cidr cidr = Cidr.parse("10.0.0.0/8");
        assertTrue(cidr.contains(InetAddress.getByName("10.1.2.3")));
    }

    @Test
    void contains_shouldReturnFalseForAddressOutsideIpv4Block() throws UnknownHostException {
        Cidr cidr = Cidr.parse("10.0.0.0/8");
        assertFalse(cidr.contains(InetAddress.getByName("11.1.2.3")));
    }

    @Test
    void contains_shouldRespectNonByteAlignedPrefix() throws UnknownHostException {
        Cidr cidr = Cidr.parse("172.16.0.0/12");
        assertTrue(cidr.contains(InetAddress.getByName("172.31.255.255")));
        assertFalse(cidr.contains(InetAddress.getByName("172.32.0.0")));
    }

    @Test
    void contains_shouldMatchLinkLocalMetadataAddress() throws UnknownHostException {
        Cidr cidr = Cidr.parse("169.254.0.0/16");
        assertTrue(cidr.contains(InetAddress.getByName("169.254.169.254")));
    }

    @Test
    void contains_shouldReturnFalseForMismatchedAddressFamily() throws UnknownHostException {
        Cidr cidr = Cidr.parse("10.0.0.0/8");
        assertFalse(cidr.contains(InetAddress.getByName("::1")));
    }

    @Test
    void contains_shouldMatchIpv6Block() throws UnknownHostException {
        Cidr cidr = Cidr.parse("fc00::/7");
        assertTrue(cidr.contains(InetAddress.getByName("fd12:3456:789a:1::1")));
    }

    @Test
    void parse_shouldDefaultToHostPrefixWhenNoMaskGiven() throws UnknownHostException {
        Cidr cidr = Cidr.parse("127.0.0.1");
        assertTrue(cidr.contains(InetAddress.getByName("127.0.0.1")));
        assertFalse(cidr.contains(InetAddress.getByName("127.0.0.2")));
    }

    @Test
    void parse_shouldThrowForInvalidPrefixLength() {
        assertThrows(IllegalArgumentException.class, () -> Cidr.parse("10.0.0.0/33"));
    }
}
