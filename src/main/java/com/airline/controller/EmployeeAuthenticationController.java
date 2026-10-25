package com.airline.controller;

import com.airline.model.Employee;
import com.airline.service.AuthenticationService;
import com.airline.service.AuthorizationService;
import com.airline.util.security.EmployeeSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeAuthenticationController implements HttpHandler {

    private static final String CONTEXT_PATH = "/api/employee-auth";

    private static final Logger LOGGER =
            Logger.getLogger(EmployeeAuthenticationController.class.getName());

    private final AuthenticationService authenticationService;
    private final AuthorizationService authorizationService;
    private final EmployeeSessionManager sessionManager;
    private final ObjectMapper objectMapper;


    public EmployeeAuthenticationController(
            AuthenticationService authenticationService,
            AuthorizationService authorizationService,
            EmployeeSessionManager sessionManager,
            ObjectMapper objectMapper
    ) {
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            LOGGER.info(() -> method + " " + path);

            if ((CONTEXT_PATH + "/login").equals(path)) {
                if (!"POST".equals(method)) {
                    sendMethodNotAllowed(exchange, "POST");
                    return;
                }

                login(exchange);
                return;
            }

            if ((CONTEXT_PATH + "/logout").equals(path)) {
                if (!"POST".equals(method)) {
                    sendMethodNotAllowed(exchange, "POST");
                    return;
                }

                logout(exchange);
                return;
            }

            if ((CONTEXT_PATH + "/me").equals(path)) {
                if (!"GET".equals(method)) {
                    sendMethodNotAllowed(exchange, "GET");
                    return;
                }

                getCurrentEmployee(exchange);
                return;
            }

            sendError(exchange, 404, "Endpoint not found.");

        } catch (IOException exception) {
            // An I/O error may mean the client disconnected or the response
            // could not be written. Do not assume another response is possible.
            LOGGER.log(
                    Level.SEVERE,
                    "I/O error while handling an HTTP request.",
                    exception
            );

            throw exception;

        } catch (Exception exception) {
            // Handles unexpected exceptions from services, session management,
            // authorization checks, and other application code.
            LOGGER.log(
                    Level.SEVERE,
                    "Unexpected error in EmployeeAuthenticationController.",
                    exception
            );

            try {
                sendError(
                        exchange,
                        500,
                        "An internal server error occurred."
                );
            } catch (IOException responseException) {
                // The original exception and the response-writing exception
                // are both preserved in the server logs.
                exception.addSuppressed(responseException);

                LOGGER.log(
                        Level.SEVERE,
                        "Could not send the internal server error response.",
                        responseException
                );
            }
        }
    }

    private void login(HttpExchange exchange) throws IOException {

        LoginRequest request;

        // 1. Parse the incoming JSON.
        try {
            request = objectMapper.readValue(
                    exchange.getRequestBody(),
                    LoginRequest.class
            );

        } catch (JsonProcessingException exception) {
            // Invalid JSON or JSON that cannot be mapped to LoginRequest.
            LOGGER.log(
                    Level.WARNING,
                    "Invalid JSON received by the login endpoint.",
                    exception
            );

            sendError(exchange, 400, "Invalid JSON request.");
            return;
        }

        // 2. Validate the request body.
        if (request == null
                || request.username() == null
                || request.username().isBlank()
                || request.password() == null
                || request.password().isBlank()) {

            sendError(
                    exchange,
                    400,
                    "Username and password are required."
            );
            return;
        }

        // 3. Authenticate the employee.
        Employee employee;

        try {
            employee = authenticationService.login(
                    request.username().trim(),
                    request.password()
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Database error during employee authentication.",
                    exception
            );

            sendError(
                    exchange,
                    500,
                    "Authentication service unavailable."
            );
            return;
        }

        // 4. Reject invalid credentials or inactive accounts.
        if (employee == null) {
            sendError(
                    exchange,
                    401,
                    "Invalid credentials or inactive account."
            );
            return;
        }

        // 5. Create an employee session.
        EmployeeSessionManager.CreatedSession createdSession =
                sessionManager.createSession(
                        employee.getEmployeeId(),
                        employee.getUserName(),
                        employee.getPosition()
                );

        // 6. Send the session token to the client in an HTTP cookie.
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                sessionManager.createSessionCookieHeader(
                        createdSession.token()
                )
        );

        // 7. Construct the response body.
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "Login successful.");
        response.put(
                "employee",
                employeeResponse(createdSession.session())
        );

        // 8. Serialize the response and send it to the client.
        sendJson(exchange, 200, response);
    }

    private void logout(HttpExchange exchange) throws IOException {

        String token = getSessionToken(exchange);

        // Invalidate the current session, if the token identifies one.
        sessionManager.invalidateSession(token);

        // Ask the browser to clear its session cookie.
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                sessionManager.clearSessionCookieHeader()
        );

        sendJson(
                exchange,
                200,
                Map.of("message", "Logout successful.")
        );
    }

    private void getCurrentEmployee(HttpExchange exchange)
            throws IOException {

        String token = getSessionToken(exchange);

        EmployeeSessionManager.Session session =
                sessionManager.getSession(token);

        if (session == null) {
            sendError(exchange, 401, "Authentication required.");
            return;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("authenticated", true);
        response.put("employee", employeeResponse(session));

        sendJson(exchange, 200, response);
    }

    private Map<String, Object> employeeResponse(
            EmployeeSessionManager.Session session
    ) {

        Map<String, Object> employee = new LinkedHashMap<>();

        employee.put("employeeId", session.employeeId());
        employee.put("username", session.username());
        employee.put("position", session.position());
        employee.put("expiresAt", session.expiresAt());

        // Use the authenticated employee's position to determine permissions.
        // These permissions are for frontend display only.
        // Backend endpoints must independently enforce authorization.
        Employee roleHolder = new Employee(
                session.employeeId(),
                null,
                null,
                session.position(),
                null,
                null,
                "ACTIVE",
                session.username(),
                null
        );

        Map<String, Boolean> permissions = new LinkedHashMap<>();

        permissions.put(
                "canAddEmployee",
                authorizationService.canAddEmployee(roleHolder)
        );

        permissions.put(
                "canManageEmployees",
                authorizationService.canManageEmployees(roleHolder)
        );

        permissions.put(
                "canCreateReservation",
                authorizationService.canCreateReservation(roleHolder)
        );

        permissions.put(
                "canAddFlight",
                authorizationService.canAddFlight(roleHolder)
        );

        permissions.put(
                "canDeleteFlight",
                authorizationService.canDeleteFlight(roleHolder)
        );

        permissions.put(
                "canCheckInPassenger",
                authorizationService.canCheckInPassenger(roleHolder)
        );

        employee.put("permissions", permissions);

        return employee;
    }

    private String getSessionToken(HttpExchange exchange) {

        String cookieHeader =
                exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        for (String cookie : cookieHeader.split(";")) {

            String[] parts = cookie.trim().split("=", 2);

            if (parts.length == 2
                    && EmployeeSessionManager.COOKIE_NAME.equals(parts[0])) {
                return parts[1];
            }
        }

        return null;
    }

    private void sendMethodNotAllowed(
            HttpExchange exchange,
            String allowedMethod
    ) throws IOException {

        exchange.getResponseHeaders().set("Allow", allowedMethod);

        sendError(exchange, 405, "HTTP method not allowed.");
    }

    private void sendError(
            HttpExchange exchange,
            int status,
            String message
    ) throws IOException {

        Map<String, String> errorResponse = Map.of(
                "error",
                message
        );

        sendJson(exchange, status, errorResponse);
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            Object response
    ) throws IOException {

        byte[] responseBytes;

        // Serialize before sending the HTTP response headers.
        // This avoids committing a response before serialization succeeds.
        try {
            responseBytes = objectMapper.writeValueAsBytes(response);

        } catch (JsonProcessingException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to serialize an HTTP response as JSON.",
                    exception
            );

            // The response has not been committed by this method yet.
            // Throwing allows the outer handler to attempt a generic 500.
            throw exception;
        }

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.getResponseHeaders().set(
                "X-Content-Type-Options",
                "nosniff"
        );

        // Once this succeeds, the response headers have been committed.
        exchange.sendResponseHeaders(status, responseBytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(responseBytes);

        } catch (IOException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to write the HTTP response body.",
                    exception
            );

            // The response may already have been partially sent.
            // Do not attempt to send a second response here.
            throw exception;
        }
    }

    public record LoginRequest(
            String username,
            String password
    ) {
    }
}