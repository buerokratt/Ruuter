package ee.buerokratt.ruuter.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Minimal IPv4/IPv6 CIDR block, used to test whether a resolved address falls inside a restricted network.
 */
public class Cidr {

    private final byte[] network;
    private final int prefixLength;

    private Cidr(byte[] network, int prefixLength) {
        this.network = network;
        this.prefixLength = prefixLength;
    }

    public static Cidr parse(String cidr) {
        String[] parts = cidr.trim().split("/", 2);
        try {
            InetAddress address = InetAddress.getByName(parts[0]);
            int maxPrefix = address.getAddress().length * 8;
            int prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : maxPrefix;
            if (prefixLength < 0 || prefixLength > maxPrefix) {
                throw new IllegalArgumentException("Invalid CIDR prefix length: " + cidr);
            }
            return new Cidr(address.getAddress(), prefixLength);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid CIDR: " + cidr, e);
        }
    }

    public boolean contains(InetAddress address) {
        byte[] candidate = address.getAddress();
        if (candidate.length != network.length) {
            return false;
        }

        int fullBytes = prefixLength / 8;
        for (int i = 0; i < fullBytes; i++) {
            if (candidate[i] != network[i]) {
                return false;
            }
        }

        int remainingBits = prefixLength % 8;
        if (remainingBits > 0) {
            int mask = 0xFF << (8 - remainingBits);
            if ((candidate[fullBytes] & mask) != (network[fullBytes] & mask)) {
                return false;
            }
        }

        return true;
    }
}
