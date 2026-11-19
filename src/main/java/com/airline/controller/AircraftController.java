
package com.airline.controller;

import com.airline.model.Aircraft;
import com.airline.model.Employee;
import com.airline.service.AircraftService;
import com.airline.util.http.HttpResponseUtil;
import com.airline.util.security.EmployeeSessionManager;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AircraftController implements HttpHandler {

    private final AircraftService aircraftService;

    private final Function<HttpExchange, EmployeeSessionManager.Session>
            employeeResolver;

    private final ObjectMapper objectMapper;
    private final HttpResponseUtil responseUtil;

    public AircraftController(
            AircraftService aircraftService,
            Function<HttpExchange, EmployeeSessionManager.Session> employeeResolver,
            ObjectMapper objectMapper
    ) {
        this.aircraftService = aircraftService;
        this.employeeResolver = employeeResolver;

        this.objectMapper = objectMapper.copy()
                .configure(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        true
                );

        this.responseUtil = new HttpResponseUtil(this.objectMapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange);
                default -> {
                    exchange.getResponseHeaders().set(
                            "Allow", "GET, POST, PUT"
                    );

                    responseUtil.sendMethodNotAllowed(
                            exchange, "GET, POST, PUT"
                    );
                }
            }

        } catch (IllegalAccessException e) {
            responseUtil.sendError(exchange, 403, e.getMessage());

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();

            if (message != null &&
                    message.startsWith("Aircraft with ID:")) {
                responseUtil.sendError(exchange, 404, message);
            } else {
                responseUtil.sendError(exchange, 400, message);
            }

        } catch (SQLException e) {
            responseUtil.sendError(
                    exchange, 500, "Database operation failed."
            );

        } catch (Exception e) {
            responseUtil.sendError(
                    exchange, 500, "Internal server error."
            );

        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange)
            throws IOException, SQLException, IllegalAccessException {

        Employee employee = getAuthenticatedEmployee(exchange);

        if (employee == null) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/aircraft";

        if (path.equals(basePath) || path.equals(basePath + "/")) {

            List<Aircraft> aircraftList =
                    aircraftService.getAllAircrafts(employee);

            responseUtil.sendJson(exchange, 200, aircraftList);

        } else {

            long aircraftId = parseAircraftId(path, basePath);

            Aircraft aircraft =
                    aircraftService.getAircraft(employee, aircraftId);

            responseUtil.sendJson(exchange, 200, aircraft);
        }
    }

    private void handlePost(HttpExchange exchange)
            throws IOException, SQLException, IllegalAccessException {

        Employee employee = getAuthenticatedEmployee(exchange);

        if (employee == null) {
            return;
        }

        if (!exchange.getRequestURI().getPath().equals("/api/aircraft")) {
            responseUtil.sendError(exchange, 404, "Endpoint not found.");
            return;
        }

        AircraftRequest request = objectMapper.readValue(
                exchange.getRequestBody(),
                AircraftRequest.class
        );

        Aircraft aircraft = new Aircraft(
                request.registrationNumber(),
                request.aircraftModel(),
                request.manufacturer(),
                request.capacity(),
                request.status()
        );

        long aircraftId = aircraftService.addAircraft(aircraft, employee);

        responseUtil.sendJson(
                exchange,
                201,
                Map.of(
                        "message", "Aircraft registered successfully.",
                        "aircraftId", aircraftId
                )
        );
    }

    private void handlePut(HttpExchange exchange)
            throws IOException, SQLException, IllegalAccessException {

        Employee employee = getAuthenticatedEmployee(exchange);

        if (employee == null) {
            return;
        }

        String path = exchange.getRequestURI().getPath();

        long aircraftId = parseAircraftId(path, "/api/aircraft");

        AircraftRequest request = objectMapper.readValue(
                exchange.getRequestBody(),
                AircraftRequest.class
        );

        Aircraft aircraft = new Aircraft(
                aircraftId,
                request.registrationNumber(),
                request.aircraftModel(),
                request.manufacturer(),
                request.capacity(),
                request.status()
        );

        boolean updated = aircraftService.updateAircraft(
                aircraft, employee, aircraftId
        );

        responseUtil.sendJson(
                exchange,
                200,
                Map.of(
                        "message",
                        updated
                                ? "Aircraft updated successfully."
                                : "Aircraft was not updated."
                )
        );
    }

    private Employee getAuthenticatedEmployee(HttpExchange exchange)
            throws IOException {

        EmployeeSessionManager.Session session =
                employeeResolver.apply(exchange);

        if (session == null) {
            responseUtil.sendError(
                    exchange, 401, "Authentication required."
            );
            return null;
        }

        return new Employee(
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
    }

    private long parseAircraftId(String path, String basePath) {

        String prefix = basePath + "/";

        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException(
                    "Invalid aircraft endpoint."
            );
        }

        String idValue = path.substring(prefix.length());

        if (idValue.isBlank() || idValue.contains("/")) {
            throw new IllegalArgumentException("Invalid aircraft ID.");
        }

        try {
            long aircraftId = Long.parseLong(idValue);

            if (aircraftId < 1) {
                throw new IllegalArgumentException(
                        "Invalid aircraft ID."
                );
            }

            return aircraftId;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid aircraft ID.");
        }
    }

    private record AircraftRequest(
            String registrationNumber,
            String aircraftModel,
            String manufacturer,
            int capacity,
            String status
    ) {
    }
}
