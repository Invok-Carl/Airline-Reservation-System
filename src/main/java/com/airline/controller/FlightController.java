
package com.airline.controller;

import com.airline.model.Employee;
import com.airline.model.Flight;
import com.airline.service.FlightService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FlightController implements HttpHandler {

    private final FlightService flightService;
    private final Function<HttpExchange, Employee> employeeResolver;
    private final ObjectMapper objectMapper;

    public FlightController(
            FlightService flightService,
            Function<HttpExchange, Employee> employeeResolver
    ) {
        this.flightService = flightService;
        this.employeeResolver = employeeResolver;

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
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                default -> {
                    exchange.getResponseHeaders()
                            .set("Allow", "GET, POST");

                    sendJson(exchange, 405, Map.of(
                            "error", "Method not allowed"
                    ));
                }
            }

        } catch (SecurityException e) {
            sendJson(exchange, 403, Map.of(
                    "error", e.getMessage()
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

    private void handleGet(HttpExchange exchange)
            throws SQLException, IOException {

        Map<String, String> params =
                parseQuery(exchange.getRequestURI().getRawQuery());

        // GET /api/flights?id=1
        if (params.containsKey("id")) {
            long flightId;

            try {
                flightId = Long.parseLong(params.get("id"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Flight ID must be a valid integer."
                );
            }

            if (flightId <= 0) {

                throw new IllegalArgumentException(
                        "Flight ID must be positive."
                );
            }

            Flight flight = flightService.findFlightById(flightId);

            if (flight == null) {
                sendJson(exchange, 404, Map.of(
                        "error", "Flight not found."
                ));
                return;
            }

            sendJson(exchange, 200, flight);
            return;
        }

        // GET /api/flights?departureCountry=...&arrivalCountry=...
        boolean hasDeparture = params.containsKey("departureCountry");
        boolean hasArrival = params.containsKey("arrivalCountry");

        if (hasDeparture || hasArrival) {
            if (!hasDeparture || !hasArrival
                    || params.get("departureCountry").isBlank()
                    || params.get("arrivalCountry").isBlank()) {
                throw new IllegalArgumentException(
                        "Both departureCountry and arrivalCountry "
                                + "are required."
                );
            }

            sendJson(
                    exchange,
                    200,
                    flightService.getFlightsByDepAndArrCountries(
                            params.get("departureCountry"),
                            params.get("arrivalCountry")
                    )
            );
            return;
        }

        // GET /api/flights
        sendJson(exchange, 200, flightService.getAllFlights());
    }

    private void handlePost(HttpExchange exchange)
            throws IOException, SQLException {

        Employee employee = employeeResolver.apply(exchange);

        // Do not accept an employee role directly from the request body.
        if (employee == null) {
            sendJson(exchange, 401, Map.of(
                    "error", "Authentication is required."
            ));
            return;
        }

        AddFlightRequest request = objectMapper.readValue(
                exchange.getRequestBody(),
                AddFlightRequest.class
        );

        if (request.flight_date() == null
                || request.schedule_id() <= 0
                || request.flight_status() == null
                || request.flight_status().isBlank()) {
            throw new IllegalArgumentException(
                    "Flight date, valid schedule ID, and status are required."
            );
        }

        // Adapt the incoming request to your existing Flight model.
        Flight flight = new Flight(
                0L,
                request.flight_date(),
                null,
                null,
                request.aircraft_id(),
                null,
                null,
                request.actual_departure(),
                request.actual_arrival(),
                request.flight_status(),
                request.schedule_id()
        );

        long flightId = flightService.addFlight(employee, flight);

        sendJson(exchange, 201, Map.of(
                "message", "Flight created successfully.",
                "flightId", flightId
        ));
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isBlank()) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0], StandardCharsets.UTF_8
            );

            String value = parts.length > 1
                    ? URLDecoder.decode(
                    parts[1], StandardCharsets.UTF_8
            )
                    : "";

            params.put(key, value);
        }

        return params;
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

        exchange.sendResponseHeaders(status, json.length);

        exchange.getResponseBody().write(json);
    }

    public record AddFlightRequest(
            LocalDate flight_date,
            Long aircraft_id,
            LocalTime actual_departure,
            LocalTime actual_arrival,
            String flight_status,
            long schedule_id
    ) {}
}
