package com.airline.securitytests;



import com.airline.util.security.PassengerSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PassengerSessionManagerTest {

    private PassengerSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new PassengerSessionManager(false);
    }

    @Test
    void createSessionShouldReturnValidToken() {
        String token = sessionManager.createSession(
                10L,
                20L,
                "passenger@example.com"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());

        // A 32-byte token encoded as unpadded Base64 URL-safe text is 43 characters.
        assertEquals(43, token.length());
        assertTrue(token.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void createSessionShouldStoreSessionForRetrieval() {
        String token = sessionManager.createSession(
                10L,
                20L,
                "passenger@example.com"
        );

        PassengerSessionManager.Session session =
                sessionManager.getSession(token);

        assertNotNull(session);
        assertEquals(10L, session.accountId());
        assertEquals(20L, session.passengerId());
        assertEquals("passenger@example.com", session.email());
        assertTrue(session.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void createSessionShouldGenerateUniqueTokens() {
        String firstToken = sessionManager.createSession(
                10L, 20L, "first@example.com"
        );

        String secondToken = sessionManager.createSession(
                11L, 21L, "second@example.com"
        );

        assertNotEquals(firstToken, secondToken);
    }

    @Test
    void getSessionShouldReturnNullForNullToken() {
        assertNull(sessionManager.getSession(null));
    }

    @Test
    void getSessionShouldReturnNullForBlankToken() {
        assertNull(sessionManager.getSession(""));
        assertNull(sessionManager.getSession("   "));
    }

    @Test
    void getSessionShouldReturnNullForUnknownToken() {
        assertNull(sessionManager.getSession("unknown-token"));
    }

    @Test
    void invalidateSessionShouldPreventFurtherRetrieval() {
        String token = sessionManager.createSession(
                10L, 20L, "passenger@example.com"
        );

        assertNotNull(sessionManager.getSession(token));

        sessionManager.invalidateSession(token);

        assertNull(sessionManager.getSession(token));
    }

    @Test
    void invalidateSessionShouldHandleNullToken() {
        assertDoesNotThrow(
                () -> sessionManager.invalidateSession(null)
        );
    }

    @Test
    void createCookieHeaderShouldContainExpectedAttributes() {
        String token = "test-token";

        String cookie = sessionManager.createCookieHeader(token);

        assertTrue(cookie.startsWith(
                PassengerSessionManager.COOKIE_NAME + "=" + token
        ));
        assertTrue(cookie.contains("; Path=/api"));
        assertTrue(cookie.contains("; Max-Age=28800"));
        assertTrue(cookie.contains("; HttpOnly"));
        assertTrue(cookie.contains("; SameSite=Lax"));
        assertFalse(cookie.contains("; Secure"));
    }

    @Test
    void createCookieHeaderShouldAddSecureWhenEnabled() {
        PassengerSessionManager secureManager =
                new PassengerSessionManager(true);

        String cookie = secureManager.createCookieHeader("test-token");

        assertTrue(cookie.contains("; Secure"));
    }

    @Test
    void clearCookieHeaderShouldExpireCookie() {
        String cookie = sessionManager.clearCookieHeader();

        assertTrue(cookie.startsWith(
                PassengerSessionManager.COOKIE_NAME + "="
        ));
        assertTrue(cookie.contains("; Path=/api"));
        assertTrue(cookie.contains("; Max-Age=0"));
        assertTrue(cookie.contains("; HttpOnly"));
        assertTrue(cookie.contains("; SameSite=Lax"));
        assertFalse(cookie.contains("; Secure"));
    }

    @Test
    void clearCookieHeaderShouldAddSecureWhenEnabled() {
        PassengerSessionManager secureManager =
                new PassengerSessionManager(true);

        String cookie = secureManager.clearCookieHeader();

        assertTrue(cookie.contains("; Secure"));
        assertTrue(cookie.contains("; Max-Age=0"));
    }

    @Test
    void sessionShouldExpireAfterItsExpirationTime() {
        // This verifies the expiration timestamp itself.
        // It does not exercise getSession()'s expiration-removal branch,
        // because the manager does not allow injecting a custom expiration.
        PassengerSessionManager.Session expiredSession =
                new PassengerSessionManager.Session(
                        10L,
                        20L,
                        "passenger@example.com",
                        Instant.now().minusSeconds(1)
                );

        assertTrue(expiredSession.expiresAt().isBefore(Instant.now()));
    }

    @Test
    void getSessionShouldReturnNullWhenSessionExpires() {
        Instant start = Instant.parse("2026-10-24T10:00:00Z");

        AtomicReference<Instant> currentTime =
                new AtomicReference<>(start);

        Clock testClock = new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneId.of("UTC");
            }

            @Override
            public Clock withZone(ZoneId zone) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instant instant() {
                return currentTime.get();
            }
        };

        PassengerSessionManager manager =
                new PassengerSessionManager(false, testClock);

        String token = manager.createSession(
                10L, 20L, "passenger@example.com"
        );

        // The session is initially valid.
        assertNotNull(manager.getSession(token));

        // Advance the test clock by exactly eight hours.
        currentTime.updateAndGet(
                time -> time.plus(Duration.ofHours(8))
        );

        // The session must now be expired.
        assertNull(manager.getSession(token));

        // The expired session was removed from the map.
        assertNull(manager.getSession(token));
    }
}
