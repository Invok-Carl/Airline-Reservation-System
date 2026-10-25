
package com.airline.controller;

import com.airline.model.Passenger;
import com.airline.model.PassengerAccount;
import com.airline.service.PassengerAccountService;
import com.airline.util.security.PassengerSessionManager;
import com.airline.util.security.PassengerSessionManager.Session;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class PassengerAccountController implements HttpHandler {

    private final PassengerAccountService passengerAccountService;
    private final PassengerSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public PassengerAccountController(
            PassengerAccountService passengerAccountService,
            PassengerSessionManager sessionManager
    ) {
        this.passengerAccountService = passengerAccountService;
        this.sessionManager = sessionManager;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
        this.objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                true
        );
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.equals("/api/auth/register")
                    && method.equals("POST")) {
                handleRegister(exchange);
                return;
            }

            if (path.equals("/api/auth/login")
                    && method.equals("POST")) {
                handleLogin(exchange);
                return;
            }

            if (path.equals("/api/auth/logout")
                    && method.equals("POST")) {
                handleLogout(exchange);
                return;
            }

            if (path.equals("/api/auth/me")
                    && method.equals("GET")) {
                handleMe(exchange);
                return;
            }

            exchange.getResponseHeaders().set(
                    "Allow", "GET, POST"
            );

            sendJson(exchange, 404, Map.of(
                    "error", "Endpoint not found."
            ));

        } catch (SecurityException e) {
            sendJson(exchange, 401, Map.of(
                    "error", "Authentication is required."
            ));

        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, Map.of(
                    "error", e.getMessage()
            ));

        } catch (SQLException e) {
            e.printStackTrace();

            sendJson(exchange, 500, Map.of(
                    "error", "A database error occurred."
            ));

        } catch (Exception e) {
            e.printStackTrace();

            sendJson(exchange, 500, Map.of(
                    "error", "An internal server error occurred."
            ));

        } finally {
            exchange.close();
        }
    }

    private void handleRegister(HttpExchange exchange)
            throws IOException, SQLException {

        RegisterRequest request = objectMapper.readValue(
                exchange.getRequestBody(),
                RegisterRequest.class
        );

        validateRegistration(request);

        Passenger passenger = new Passenger(
                request.passengerId(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.dateOfBirth(),
                request.gender(),
                request.nationality(),
                request.phone(),
                request.email().trim(),
                request.address(),
                request.passportNumber(),
                request.passportExpiry(),
                request.passportCountry()
        );

        PassengerAccount account = passengerAccountService.register(
                passenger,
                request.email(),
                request.password()
        );

        String token = sessionManager.createSession(
                account.getAccountId(),
                account.getPassengerId(),
                account.getEmail()
        );

        exchange.getResponseHeaders().add(
                "Set-Cookie",
                sessionManager.createCookieHeader(token)
        );

        sendJson(exchange, 201, Map.of(
                "message", "Registration successful.",
                "account", publicAccount(account)
        ));
    }

    private void handleLogin(HttpExchange exchange)
            throws IOException, SQLException {

        LoginRequest request = objectMapper.readValue(
                exchange.getRequestBody(),
                LoginRequest.class
        );

        if (request.email() == null || request.email().isBlank()
                || request.password() == null
                || request.password().isEmpty()) {
            throw new IllegalArgumentException(
                    "Email and password are required."
            );
        }

        PassengerAccount account = passengerAccountService.login(
                request.email(),
                request.password()
        );

        if (account == null) {
            sendJson(exchange, 401, Map.of(
                    "error", "Invalid email or password."
            ));
            return;
        }

        String token = sessionManager.createSession(
                account.getAccountId(),
                account.getPassengerId(),
                account.getEmail()
        );

        exchange.getResponseHeaders().add(
                "Set-Cookie",
                sessionManager.createCookieHeader(token)
        );

        sendJson(exchange, 200, Map.of(
                "message", "Login successful.",
                "account", publicAccount(account)
        ));
    }

    private void handleLogout(HttpExchange exchange)
            throws IOException {

        String token = extractSessionToken(exchange);

        sessionManager.invalidateSession(token);

        exchange.getResponseHeaders().add(
                "Set-Cookie",
                sessionManager.clearCookieHeader()
        );

        sendJson(exchange, 200, Map.of(
                "message", "Logout successful."
        ));
    }

    private void handleMe(HttpExchange exchange)
            throws IOException {

        Session session = resolveSession(exchange);

        if (session == null) {
            sendJson(exchange, 401, Map.of(
                    "error", "Authentication is required."
            ));
            return;
        }

        sendJson(exchange, 200, Map.of(
                "accountId", session.accountId(),
                "passengerId", session.passengerId(),
                "email", session.email()
        ));
    }

    public Session resolveSession(HttpExchange exchange) {
        String token = extractSessionToken(exchange);
        return sessionManager.getSession(token);
    }

    private String extractSessionToken(HttpExchange exchange) {
        String cookieHeader =
                exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);

            if (parts.length == 2
                    && parts[0].equals(
                    PassengerSessionManager.COOKIE_NAME)) {
                return parts[1];
            }
        }

        return null;
    }

    private void validateRegistration(RegisterRequest request) {
        if (request.passengerId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid passenger ID is required."
            );
        }

        requireText(request.firstName(), "First name");
        requireText(request.lastName(), "Last name");
        requireText(request.gender(), "Gender");
        requireText(request.email(), "Email");
        requireText(request.passportNumber(), "Passport number");

        if (request.dateOfBirth() == null) {
            throw new IllegalArgumentException(
                    "Date of birth is required."
            );
        }

        if (request.password() == null
                || request.password().length() < 12) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters."
            );
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }
    }

    private Map<String, Object> publicAccount(
            PassengerAccount account
    ) {
        return Map.of(
                "accountId", account.getAccountId(),
                "passengerId", account.getPassengerId(),
                "email", account.getEmail(),
                "accountStatus", account.getAccountStatus()
        );
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            Object body
    ) throws IOException {

        byte[] json = objectMapper.writeValueAsBytes(body);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );
        exchange.getResponseHeaders().set(
                "X-Content-Type-Options",
                "nosniff"
        );
        exchange.getResponseHeaders().set(
                "Cache-Control",
                "no-store"
        );

        exchange.sendResponseHeaders(status, json.length);
        exchange.getResponseBody().write(json);
    }

    public record RegisterRequest(
            long passengerId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String nationality,
            String phone,
            String email,
            String address,
            String passportNumber,
            LocalDate passportExpiry,
            String passportCountry,
            String password
    ) {}

    public record LoginRequest(
            String email,
            String password
    ) {}
}
