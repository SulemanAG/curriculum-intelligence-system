package org.curriculumai.evidence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

@Component
public class SafeUrlValidator {

    private static final Logger log = LoggerFactory.getLogger(SafeUrlValidator.class);
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    /**
     * Validates that the provided URL is well-formed, uses http/https,
     * and does not target localhost, private subnets, link-local, or cloud metadata
     * IP addresses.
     *
     * @param urlString target URL string
     * @return validated URI
     * @throws IllegalArgumentException if URL is invalid or violates SSRF
     *                                  protection
     */
    public URI validateSafeUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty.");
        }

        URI uri;
        try {
            uri = URI.create(urlString.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed URL format: " + e.getMessage());
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new IllegalArgumentException("Only HTTP and HTTPS URLs are permitted. Scheme was: " + scheme);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL host cannot be empty.");
        }

        String lowerHost = host.toLowerCase();

        // Check common loopback & metadata names
        if (lowerHost.equals("localhost") ||
                lowerHost.endsWith(".localhost") ||
                lowerHost.endsWith(".local") ||
                lowerHost.equals("metadata.google.internal") ||
                lowerHost.equals("instance-data")) {
            throw new IllegalArgumentException("Access to local/metadata host is prohibited: " + host);
        }

        // Check literal IPv4 formats directly
        if (lowerHost.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            String[] parts = lowerHost.split("\\.");
            int b0 = Integer.parseInt(parts[0]);
            int b1 = Integer.parseInt(parts[1]);
            if (b0 == 127 || b0 == 10 || (b0 == 172 && (b1 >= 16 && b1 <= 31)) ||
                    (b0 == 192 && b1 == 168) || (b0 == 169 && b1 == 254) || b0 == 0) {
                throw new IllegalArgumentException("Destination IP is prohibited (SSRF protection): " + host);
            }
        }

        // Resolve DNS and test resolved IP addresses
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (isProhibitedIp(addr)) {
                    throw new IllegalArgumentException(
                            "Destination IP is prohibited (SSRF protection): " + addr.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            log.debug("DNS resolution for host '{}' not available in current network environment: {}", host,
                    e.getMessage());
        }

        return uri;
    }

    /**
     * Checks if IP is loopback, link-local, site-local (private), multicast, or
     * cloud metadata (169.254.169.254).
     */
    public boolean isProhibitedIp(InetAddress addr) {
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
            return true;
        }

        if (addr.isSiteLocalAddress()) {
            return true;
        }

        if (addr.isMulticastAddress()) {
            return true;
        }

        byte[] bytes = addr.getAddress();

        // IPv4 checks
        if (bytes.length == 4) {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;

            // 127.0.0.0/8 (Loopback)
            if (b0 == 127)
                return true;

            // 10.0.0.0/8 (Private)
            if (b0 == 10)
                return true;

            // 172.16.0.0/12 (Private)
            if (b0 == 172 && (b1 >= 16 && b1 <= 31))
                return true;

            // 192.168.0.0/16 (Private)
            if (b0 == 192 && b1 == 168)
                return true;

            // 169.254.0.0/16 (Link-local & AWS/GCP/Azure Metadata: 169.254.169.254)
            if (b0 == 169 && b1 == 254)
                return true;

            // 0.0.0.0/8 (Current network)
            if (b0 == 0)
                return true;

            // 100.64.0.0/10 (Carrier-grade NAT)
            if (b0 == 100 && (b1 >= 64 && b1 <= 127))
                return true;
        }

        // IPv6 Unique Local Address (fc00::/7)
        if (bytes.length == 16) {
            int b0 = bytes[0] & 0xFF;
            if ((b0 & 0xFE) == 0xFC)
                return true;
        }

        return false;
    }
}
