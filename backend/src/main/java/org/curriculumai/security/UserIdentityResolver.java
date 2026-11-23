package org.curriculumai.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Resolves user identity for data ownership and authorization boundaries.
 * 
 * ARCHITECTURE & SECURITY NOTICE:
 * 1. Development Mode: When running in development/test profiles, this component strictly requires
 *    a valid caller-supplied 'X-User-Id' header. Missing, null, or blank identity headers are explicitly rejected.
 * 2. Production Mode: In the 'prod' profile or when 'app.security.allow-header-identity' is false,
 *    caller-supplied headers are strictly ignored and rejected. Identity is derived exclusively from
 *    the authenticated SecurityContext principal.
 * 3. Startup Gate: The application strictly refuses to start if 'app.security.allow-header-identity'
 *    is enabled within the 'prod' profile, preventing accidental insecure deployments.
 */
@Component
public class UserIdentityResolver {

    private static final Logger log = LoggerFactory.getLogger(UserIdentityResolver.class);

    @Deprecated(forRemoval = true, since = "Phase 2.1.1")
    public static final String DEFAULT_DEV_FACULTY_ID = "usr-default-faculty";

    @Value("${app.security.allow-header-identity:true}")
    private boolean allowHeaderIdentity;

    private final Environment environment;

    public UserIdentityResolver(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateSecurityConfiguration() {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProd && allowHeaderIdentity) {
            throw new IllegalStateException(
                    "FATAL SECURITY CONFIGURATION: 'app.security.allow-header-identity' is enabled in the 'prod' profile. " +
                    "Caller-supplied identity headers (X-User-Id) are strictly forbidden in production. " +
                    "Startup aborted to prevent unauthorized access."
            );
        }
        if (allowHeaderIdentity) {
            log.info("Development identity resolution active (X-User-Id header permitted for local development only).");
        } else {
            log.info("Production identity resolution active (SecurityContext authenticated principal required).");
        }
    }

    /**
     * Resolves the user ID for ownership and authorization.
     *
     * @param devHeaderValue caller-supplied X-User-Id header (development-only)
     * @return validated user ID
     * @throws AccessDeniedException if unauthenticated in production mode
     */
    public String resolveUserId(String devHeaderValue) {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (isProd || !allowHeaderIdentity) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
                throw new AccessDeniedException("Access denied: Real authenticated principal required in production mode.");
            }
            return auth.getName();
        }

        // Development/test profile: Missing identity must be explicitly rejected (no automatic fallback to privileged admin or faculty)
        if (devHeaderValue == null || devHeaderValue.isBlank()) {
            throw new AccessDeniedException("Missing user identity: 'X-User-Id' header must be explicitly supplied in development mode.");
        }
        return devHeaderValue.trim();
    }

    public boolean isHeaderIdentityAllowed() {
        return allowHeaderIdentity;
    }
}

