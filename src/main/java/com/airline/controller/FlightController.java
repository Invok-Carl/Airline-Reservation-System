package com.airline.controller;

import com.airline.model.Employee;
import com.airline.model.Flight;
import com.airline.model.Seat;
import com.airline.service.AuthorizationService;
import com.airline.service.FlightService;
import com.airline.service.SeatService;
import com.airline.util.security.EmployeeSessionManager;

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
import java.util.List;
import java.util.Map;

public class FlightController implements HttpHandler {

    private final FlightService flightService;
    private final SeatService seatService;
    private final EmployeeSessionManager sessionManager;
    private final AuthorizationService authorizationService;
    private final ObjectMapper objectMapper;

    public FlightController(
            FlightService flightService,
            SeatService seatService,
            EmployeeSessionManager sessionManager,
            AuthorizationService authorizationService
    ) {
        this.flightService = flightService;
        this.seatService = seatService;
        this.sessionManager = sessionManager;
        this.authorizationService = authorizationService;

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
                            "error", "Method not allowed."
                    ));
                }
            }

        } catch (SecurityException e) {
            sendJson(exchange, 403, Map.of(
                    "error", e.getMessage() == null
                            ? "Access denied."
                            : e.getMessage()
            ));

        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, Map.of(
                    "error", e.getMessage() == null
                            ? "Invalid request."
                            : e.getMessage()
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

    // --------------------------------------------------
    // GET endpoints
    // --------------------------------------------------

    private void handleGet(HttpExchange exchange)
            throws SQLException, IOException {

        String path = exchange.getRequestURI().getPath();

        // GET /api/flights/{flightId}/available-seats
        if (path.matches("^/api/flights/[^/]+/available-seats$")) {

            long flightId = extractFlightId(
                    path,
                    "available-seats"
            );

            Flight flight = flightService.findFlightById(flightId);

            if (flight == null) {
                sendJson(exchange, 404, Map.of(
                        "error", "Flight not found."
                ));
                return;
            }

            List<Seat> seats =
                    seatService.getAvailableSeatsListByFlightId(flightId);

            sendJson(exchange, 200, Map.of(
                    "flightId", flightId,
                    "availableSeats", seats
            ));

            return;
        }

        // GET /api/flights/{flightId}/seats
        if (path.matches("^/api/flights/[^/]+/seats$")) {

            long flightId = extractFlightId(path, "seats");

            Flight flight = flightService.findFlightById(flightId);

            if (flight == null) {
                sendJson(exchange, 404, Map.of(
                        "error", "Flight not found."
                ));
                return;
            }

            List<Seat> seats =
                    seatService.getSeatsListByFLightId(flightId);

            sendJson(exchange, 200, Map.of(
                    "flightId", flightId,
                    "seats", seats
            ));

            return;
        }

        // GET /api/flights/{flightId}
        if (path.matches("^/api/flights/[^/]+$")) {

            String idText = path.substring(
                    "/api/flights/".length()
            );

            long flightId = parsePositiveFlightId(idText);

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

        // The remaining GET endpoints must use /api/flights.
        if (!"/api/flights".equals(path)) {
            sendJson(exchange, 404, Map.of(
                    "error", "Endpoint not found."
            ));
            return;
        }

        Map<String, String> params =
                parseQuery(exchange.getRequestURI().getRawQuery());

        // GET /api/flights?id=1
        if (params.containsKey("id")) {

            long flightId = parsePositiveFlightId(
                    params.get("id")
            );

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

            String departureCountry = params.get("departureCountry");
            String arrivalCountry = params.get("arrivalCountry");

            if (departureCountry == null
                    || departureCountry.isBlank()
                    || arrivalCountry == null
                    || arrivalCountry.isBlank()) {

                throw new IllegalArgumentException(
                        "Both departureCountry and arrivalCountry "
                                + "are required."
                );
            }

            sendJson(
                    exchange,
                    200,
                    flightService.getFlightsByDepAndArrCountries(
                            departureCountry,
                            arrivalCountry
                    )
            );

            return;
        }

        // GET /api/flights
        sendJson(
                exchange,
                200,
                flightService.getAllFlights()
        );
    }

    // --------------------------------------------------
    // POST /api/flights
    // --------------------------------------------------

    private void handlePost(HttpExchange exchange)
            throws IOException, SQLException {

        String path = exchange.getRequestURI().getPath();

        if (!"/api/flights".equals(path)) {
            sendJson(exchange, 404, Map.of(
                    "error", "Endpoint not found."
            ));
            return;
        }

        /*
         * STEP 1:
         * Extract the session token from the HTTP Cookie header.
         */
        String token = getSessionToken(exchange);

        /*
         * STEP 2:
         * Validate the token against the server-side session map.
         *
         * getSession() returns null when the token is missing,
         * unknown, or expired.
         */
        EmployeeSessionManager.Session session =
                sessionManager.getSession(token);

        if (session == null) {
            sendJson(exchange, 401, Map.of(
                    "error", "Authentication required."
            ));
            return;
        }

        /*
         * STEP 3:
         * Construct the Employee object from the validated session.
         *
         * These values come from server-side session data,
         * not from the incoming JSON request.
         */
        Employee employee = new Employee(
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

        /*
         * STEP 4:
         * Check whether this employee can add flights.
         */
        if (!authorizationService.canAddFlight(employee)) {
            sendJson(exchange, 403, Map.of(
                    "error",
                    "You do not have permission to add flights."
            ));
            return;
        }

        /*
         * STEP 5:
         * Parse the flight data from the JSON request body.
         */
        AddFlightRequest request = objectMapper.readValue(
                exchange.getRequestBody(),
                AddFlightRequest.class
        );

        /*
         * STEP 6:
         * Validate the request.
         */
        if (request.flight_date() == null
                || request.schedule_id() <= 0
                || request.flight_status() == null
                || request.flight_status().isBlank()) {

            throw new IllegalArgumentException(
                    "Flight date, valid schedule ID, and status "
                            + "are required."
            );
        }

        if (request.aircraft_id() != null
                && request.aircraft_id() <= 0) {

            throw new IllegalArgumentException(
                    "Aircraft ID must be positive."
            );
        }

        if (request.actual_departure() != null
                && request.actual_arrival() != null
                && !request.actual_arrival().isAfter(
                request.actual_departure()
        )) {

            throw new IllegalArgumentException(
                    "Actual arrival must be after actual departure."
            );
        }

        /*
         * STEP 7:
         * Create the Flight object.
         */
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

        /*
         * STEP 8:
         * Pass the authenticated employee and flight to the service.
         *
         * The service performs the flight-creation operation.
         */
        long flightId = flightService.addFlight(employee, flight);

        /*
         * STEP 9:
         * Return HTTP 201 Created.
         */
        sendJson(exchange, 201, Map.of(
                "message", "Flight created successfully.",
                "flightId", flightId
        ));
    }

    // --------------------------------------------------
    // Session cookie handling
    // --------------------------------------------------

    private String getSessionToken(HttpExchange exchange) {

        String cookieHeader =
                exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        for (String cookie : cookieHeader.split(";")) {

            String[] parts = cookie.trim().split("=", 2);

            if (parts.length == 2
                    && EmployeeSessionManager.COOKIE_NAME
                    .equals(parts[0])) {

                return parts[1];
            }
        }

        return null;
    }

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

    private long extractFlightId(
            String path,
            String suffix
    ) {

        String prefix = "/api/flights/";

        String idText = path.substring(prefix.length());

        String ending = "/" + suffix;

        idText = idText.substring(
                0,
                idText.length() - ending.length()
        );

        return parsePositiveFlightId(idText);
    }

    private long parsePositiveFlightId(String value) {

        long flightId;

        try {
            flightId = Long.parseLong(value);

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

        return flightId;
    }

    private Map<String, String> parseQuery(String query) {

        Map<String, String> params = new HashMap<>();

        if (query == null || query.isBlank()) {
            return params;
        }

        for (String pair : query.split("&")) {

            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0],
                    StandardCharsets.UTF_8
            );

            String value = parts.length > 1
                    ? URLDecoder.decode(
                    parts[1],
                    StandardCharsets.UTF_8
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

        try (var output = exchange.getResponseBody()) {
            output.write(json);
        }
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