package org.curriculumai.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class UserIdentityResolverTest {

    @Test
    @DisplayName("Development mode resolves caller-supplied X-User-Id header")
    void testResolveDevHeaderUserId() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        UserIdentityResolver resolver = new UserIdentityResolver(env);
        ReflectionTestUtils.setField(resolver, "allowHeaderIdentity", true);
        resolver.validateSecurityConfiguration();

        String resolved = resolver.resolveUserId("usr-prof-alan");
        assertEquals("usr-prof-alan", resolved);
    }

    @Test
    @DisplayName("Development mode rejects missing or blank X-User-Id header")
    void testResolveDevHeaderRejectsMissingIdentity() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        UserIdentityResolver resolver = new UserIdentityResolver(env);
        ReflectionTestUtils.setField(resolver, "allowHeaderIdentity", true);

        AccessDeniedException ex1 = assertThrows(AccessDeniedException.class, () -> resolver.resolveUserId(null));
        assertTrue(ex1.getMessage().contains("Missing user identity"));

        AccessDeniedException ex2 = assertThrows(AccessDeniedException.class, () -> resolver.resolveUserId("   "));
        assertTrue(ex2.getMessage().contains("Missing user identity"));
    }

    @Test
    @DisplayName("Startup gate aborts application if allowHeaderIdentity is true in prod profile")
    void testStartupAbortsInProdWithDevHeaderEnabled() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        UserIdentityResolver resolver = new UserIdentityResolver(env);
        ReflectionTestUtils.setField(resolver, "allowHeaderIdentity", true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, resolver::validateSecurityConfiguration);
        assertTrue(ex.getMessage().contains("strictly forbidden in production"));
    }

    @Test
    @DisplayName("Production mode extracts authenticated SecurityContext principal and ignores dev header")
    void testProductionModeExtractsSecurityContextPrincipal() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        UserIdentityResolver resolver = new UserIdentityResolver(env);
        ReflectionTestUtils.setField(resolver, "allowHeaderIdentity", false);
        resolver.validateSecurityConfiguration();

        // Set authenticated user in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usr-institutional-faculty-101", "credentials", null)
        );

        try {
            // Even if caller supplies a malicious X-User-Id header, production mode ignores it
            String resolved = resolver.resolveUserId("usr-spoofed-dean");
            assertEquals("usr-institutional-faculty-101", resolved);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Production mode throws AccessDeniedException when caller is unauthenticated")
    void testProductionModeThrowsWhenUnauthenticated() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        UserIdentityResolver resolver = new UserIdentityResolver(env);
        ReflectionTestUtils.setField(resolver, "allowHeaderIdentity", false);
        resolver.validateSecurityConfiguration();

        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> resolver.resolveUserId("usr-random"));
    }
}

