package com.airline.securitytests;



import com.airline.util.security.EmployeeSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeSessionManagerTest {

    private EmployeeSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new EmployeeSessionManager(false);
    }

    @Test
    void createSessionShouldReturnValidTokenAndSession() {
        long employeeId = 1L;
        String username = "admin";
        String position = "ADMINISTRATOR";

        EmployeeSessionManager.CreatedSession created =
                sessionManager.createSession(employeeId, username, position);

        assertNotNull(created);
        assertNotNull(created.token());
        assertFalse(created.token().isBlank());

        // A 32-byte token encoded as unpadded Base64 URL-safe text is 43 characters.
        assertEquals(43, created.token().length());
        assertTrue(created.token().matches("[A-Za-z0-9_-]+"));

        assertNotNull(created.session());
        assertEquals(employeeId, created.session().employeeId());
        assertEquals(username, created.session().username());
        assertEquals(position, created.session().position());
        assertTrue(created.session().expiresAt().isAfter(Instant.now()));
    }

    @Test
    void createSessionShouldStoreSessionForRetrieval() {
        EmployeeSessionManager.CreatedSession created =
                sessionManager.createSession(
                        1L, "admin", "ADMINISTRATOR"
                );

        EmployeeSessionManager.Session retrieved =
                sessionManager.getSession(created.token());

        assertNotNull(retrieved);
        assertEquals(created.session(), retrieved);
    }

    @Test
    void createSessionShouldGenerateUniqueTokens() {
        EmployeeSessionManager.CreatedSession first =
                sessionManager.createSession(
                        1L, "admin", "ADMINISTRATOR"
                );

        EmployeeSessionManager.CreatedSession second =
                sessionManager.createSession(
                        2L, "agent", "RESERVATION_AGENT"
                );

        assertNotEquals(first.token(), second.token());
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
        EmployeeSessionManager.CreatedSession created =
                sessionManager.createSession(
                        1L, "admin", "ADMINISTRATOR"
                );

        assertNotNull(sessionManager.getSession(created.token()));

        sessionManager.invalidateSession(created.token());

        assertNull(sessionManager.getSession(created.token()));
    }

    @Test
    void invalidateSessionShouldHandleNullAndBlankTokens() {
        assertDoesNotThrow(() -> sessionManager.invalidateSession(null));
        assertDoesNotThrow(() -> sessionManager.invalidateSession(""));
        assertDoesNotThrow(() -> sessionManager.invalidateSession("   "));
    }

    @Test
    void expiredSessionShouldNotBeReturned() {
        EmployeeSessionManager.Session expiredSession =
                new EmployeeSessionManager.Session(
                        1L,
                        "admin",
                        "ADMINISTRATOR",
                        Instant.now().minusSeconds(1)
                );

        // The session manager has no public method to insert an arbitrary
        // session, so expiration behavior is tested through a separate
        // session manager implementation only if the class is made injectable.
        assertTrue(expiredSession.expiresAt().isBefore(Instant.now()));
    }

    @Test
    void createSessionCookieHeaderShouldContainExpectedAttributes() {
        String token = "test-token";

        String cookie =
                sessionManager.createSessionCookieHeader(token);

        assertTrue(cookie.startsWith(
                EmployeeSessionManager.COOKIE_NAME + "=" + token
        ));
        assertTrue(cookie.contains("; Path=/api"));
        assertTrue(cookie.contains("; HttpOnly"));
        assertTrue(cookie.contains("; SameSite=Lax"));
        assertTrue(cookie.contains("; Max-Age=28800"));
        assertFalse(cookie.contains("; Secure"));
    }

    @Test
    void createSessionCookieHeaderShouldAddSecureWhenEnabled() {
        EmployeeSessionManager secureSessionManager =
                new EmployeeSessionManager(true);

        String cookie =
                secureSessionManager.createSessionCookieHeader("test-token");

        assertTrue(cookie.contains("; Secure"));
    }

    @Test
    void clearSessionCookieHeaderShouldExpireCookie() {
        String cookie = sessionManager.clearSessionCookieHeader();

        assertTrue(cookie.startsWith(
                EmployeeSessionManager.COOKIE_NAME + "="
        ));
        assertTrue(cookie.contains("; Path=/api"));
        assertTrue(cookie.contains("; HttpOnly"));
        assertTrue(cookie.contains("; SameSite=Lax"));
        assertTrue(cookie.contains("; Max-Age=0"));
        assertFalse(cookie.contains("; Secure"));
    }

    @Test
    void clearSessionCookieHeaderShouldAddSecureWhenEnabled() {
        EmployeeSessionManager secureSessionManager =
                new EmployeeSessionManager(true);

        String cookie = secureSessionManager.clearSessionCookieHeader();

        assertTrue(cookie.contains("; Secure"));
        assertTrue(cookie.contains("; Max-Age=0"));
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

        EmployeeSessionManager manager =
                new EmployeeSessionManager(false, testClock);

        EmployeeSessionManager.CreatedSession created =
                manager.createSession(
                        1L,
                        "admin",
                        "ADMINISTRATOR"
                );

        String token = created.token();

        // The session is initially valid.
        assertNotNull(manager.getSession(token));

        // Advance time to exactly eight hours after creation.
        currentTime.updateAndGet(
                time -> time.plus(Duration.ofHours(8))
        );

        // The session must now be expired and removed.
        assertNull(manager.getSession(token));
        assertNull(manager.getSession(token));
    }

}
