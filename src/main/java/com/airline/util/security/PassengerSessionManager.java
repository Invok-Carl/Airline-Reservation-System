
package com.airline.util.security;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Clock;

public class PassengerSessionManager {
    private final Clock clock;

    public static final String COOKIE_NAME = "AIRLINE_SESSION";

    private static final long SESSION_DURATION_SECONDS = 8 * 60 * 60;

    private final SecureRandom secureRandom = new SecureRandom();

    private final ConcurrentHashMap<String, Session> sessions =
            new ConcurrentHashMap<>();

    private final boolean secureCookie;

    public PassengerSessionManager(boolean secureCookie) {
        this(secureCookie, Clock.systemUTC());
    }

    public PassengerSessionManager(boolean secureCookie, Clock clock) {
        this.secureCookie = secureCookie;
        this.clock = clock;
    }

    public String createSession(
            long accountId,
            long passengerId,
            String email
    ) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        Session session = new Session(
                accountId,
                passengerId,
                email,
                Instant.now(clock).plusSeconds(SESSION_DURATION_SECONDS)
        );

        sessions.put(token, session);

        return token;
    }

    public Session getSession(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        Session session = sessions.get(token);

        if (session == null) {
            return null;
        }

        if (!Instant.now(clock).isBefore(session.expiresAt())) {
            sessions.remove(token, session);
            return null;
        }

        return session;
    }

    public void invalidateSession(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public String createCookieHeader(String token) {
        return COOKIE_NAME + "=" + token
                + "; Path=/api"
                + "; Max-Age=" + SESSION_DURATION_SECONDS
                + "; HttpOnly"
                + "; SameSite=Lax"
                + (secureCookie ? "; Secure" : "");
    }

    public String clearCookieHeader() {
        return COOKIE_NAME + "="
                + "; Path=/api"
                + "; Max-Age=0"
                + "; HttpOnly"
                + "; SameSite=Lax"
                + (secureCookie ? "; Secure" : "");
    }

    public record Session(
            long accountId,
            long passengerId,
            String email,
            Instant expiresAt
    ) {}
}
