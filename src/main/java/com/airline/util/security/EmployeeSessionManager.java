
package com.airline.util.security;

import com.sun.net.httpserver.HttpExchange;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Clock;

public class EmployeeSessionManager {

    public static final String COOKIE_NAME = "AIRLINE_EMPLOYEE_SESSION";
    private final Clock clock;

    private static final long SESSION_DURATION_SECONDS = 8 * 60 * 60;

    private final ConcurrentHashMap<String, Session> sessions =
            new ConcurrentHashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    private final boolean secureCookie;

    public EmployeeSessionManager(boolean secureCookie) {
        this(secureCookie, Clock.systemUTC());
    }

    public EmployeeSessionManager(boolean secureCookie, Clock clock) {
        this.secureCookie = secureCookie;
        this.clock = clock;
    }
    public CreatedSession createSession(
            long employeeId,
            String username,
            String position
    ) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        Session session = new Session(
                employeeId,
                username,
                position,
                Instant.now(clock).plusSeconds(SESSION_DURATION_SECONDS)
        );

        sessions.put(token, session);

        return new CreatedSession(token, session);
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
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    public String createSessionCookieHeader(String token) {
        StringBuilder cookie = new StringBuilder()
                .append(COOKIE_NAME).append("=")
                .append(token)
                .append("; Path=/api")
                .append("; HttpOnly")
                .append("; SameSite=Lax")
                .append("; Max-Age=")
                .append(SESSION_DURATION_SECONDS);

        if (secureCookie) {
            cookie.append("; Secure");
        }

        return cookie.toString();
    }


    public Session getSessionFromRequest(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");

        if (cookies == null) {
            return null;
        }

        String prefix = COOKIE_NAME + "=";

        for (String header : cookies) {
            for (String cookie : header.split(";")) {
                String trimmedCookie = cookie.trim();

                if (trimmedCookie.startsWith(prefix)) {
                    String token = trimmedCookie.substring(prefix.length());

                    return getSession(token);
                }
            }
        }

        return null;
    }
    public String clearSessionCookieHeader() {
        StringBuilder cookie = new StringBuilder()
                .append(COOKIE_NAME).append("=")
                .append("; Path=/api")
                .append("; HttpOnly")
                .append("; SameSite=Lax")
                .append("; Max-Age=0");

        if (secureCookie) {
            cookie.append("; Secure");
        }

        return cookie.toString();
    }

    public record Session(
            long employeeId,
            String username,
            String position,
            Instant expiresAt
    ) {}

    public record CreatedSession(
            String token,
            Session session
    ) {}
}
