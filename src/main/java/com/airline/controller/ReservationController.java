package com.airline.controller;



import com.airline.model.BookingSegment;
import com.airline.service.ReservationService.ReservationNotFoundException;
import com.airline.service.ReservationService;
import com.airline.util.security.PassengerSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReservationController implements HttpHandler {

    private static final String BASE_PATH = "/api/reservations";
    private static final String COOKIE_NAME = "AIRLINE_SESSION";


    // i.e. /api/reservations/14/segments -> add segments to reservation 14
    private static final Pattern ADD_SEGMENT_PATH =
            Pattern.compile("^/api/reservations/(\\d+)/segments$");

    private static final Pattern REMOVE_SEGMENT_PATH =
            Pattern.compile(
                    "^/api/reservations/(\\d+)/segments/(\\d+)$"
            );

    private final ReservationService reservationService;
    private final PassengerSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public ReservationController(
            ReservationService reservationService,
            PassengerSessionManager sessionManager
    ) {
        this.reservationService = reservationService;
        this.sessionManager = sessionManager;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            // Authenticate every reservation operation.
            PassengerSessionManager.Session session =
                    getAuthenticatedSession(exchange);

            if (session == null) {
                sendError(
                        exchange,
                        401,
                        "Authentication required."
                );
                return;
            }

            long accountId = session.accountId();

            // POST /api/reservations
            if (BASE_PATH.equals(path)
                    && "POST".equals(method)) {

                createReservation(exchange, accountId);
                return;
            }

            // POST /api/reservations/{id}/segments
            Matcher addMatcher =
                    ADD_SEGMENT_PATH.matcher(path);

            if (addMatcher.matches()
                    && "POST".equals(method)) {

                long reservationId =
                        parsePositiveId(addMatcher.group(1));

                addSegment(exchange, accountId, reservationId);
                return;
            }

            // DELETE /api/reservations/{id}/segments/{segmentId}
            Matcher removeMatcher =
                    REMOVE_SEGMENT_PATH.matcher(path);

            if (removeMatcher.matches()
                    && "DELETE".equals(method)) {

                long reservationId =
                        parsePositiveId(removeMatcher.group(1));

                long segmentId =
                        parsePositiveId(removeMatcher.group(2));

                removeSegment(
                        exchange,
                        accountId,
                        reservationId,
                        segmentId
                );
                return;
            }

            // Known paths with unsupported methods.
            if (BASE_PATH.equals(path)
                    || addMatcher.matches()
                    || removeMatcher.matches()) {

                exchange.getResponseHeaders().set(
                        "Allow",
                        allowedMethods(path)
                );

                sendError(
                        exchange,
                        405,
                        "HTTP method not allowed."
                );
                return;
            }

            sendError(
                    exchange,
                    404,
                    "Reservation endpoint not found."
            );

        } catch (ReservationNotFoundException e) {
            sendError(exchange, 404, e.getMessage());

        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());

        } catch (IllegalStateException e) {
            sendError(exchange, 409, e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();

            sendError(
                    exchange,
                    500,
                    "A database error occurred."
            );

        } catch (Exception e) {
            e.printStackTrace();

            sendError(
                    exchange,
                    500,
                    "An unexpected server error occurred."
            );
        }
    }

    private void createReservation(
            HttpExchange exchange,
            long accountId
    ) throws IOException, SQLException {
        /*
        * {
              "segments": [
                {
                  "passenger_id": 12,
                  "flight_id": 1,
                  "seat_id": 1653
                },
                {
                  "passenger_id": 13,
                  "flight_id": 4,
                  "seat_id": 1532
                }...
              ]
           }
        */

        JsonNode body = objectMapper.readTree(
                exchange.getRequestBody()
        );

        if (body == null || !body.isObject()) {
            throw new IllegalArgumentException(
                    "A JSON object is required."
            );
        }

        JsonNode segmentsNode = body.get("segments");

        if (segmentsNode == null || !segmentsNode.isArray()
                || segmentsNode.isEmpty()) {
            throw new IllegalArgumentException(
                    "The segments array must not be empty."
            );
        }

        List<BookingSegment> segments = new ArrayList<>();

        for (JsonNode node : segmentsNode) {
            if (!node.isObject()) {
                throw new IllegalArgumentException(
                        "Each segment must be a JSON object."
                );
            }

            BookingSegment segment = new BookingSegment();

            segment.setPassengerId(
                    requirePositiveLong(node, "passenger_id")
            );

            segment.setFlightId(
                    requirePositiveLong(node, "flight_id")
            );

            segment.setSeatId(
                    requirePositiveLong(node, "seat_id")
            );

            segments.add(segment);
        }

        // The authenticated account ID comes from the session,
        // never from the request body.
        long reservationId =
                reservationService.createReservation(
                        accountId,
                        segments
                );

        ObjectNode response = objectMapper.createObjectNode();

        response.put("message", "Reservation created successfully.");
        response.put("reservation_id", reservationId);

        sendJson(exchange, 201, response);
    }

    private void addSegment(
            HttpExchange exchange,
            long accountId,
            long reservationId
    ) throws IOException, SQLException {

        JsonNode body = objectMapper.readTree(
                exchange.getRequestBody()
        );

        if (body == null || !body.isObject()) {
            throw new IllegalArgumentException(
                    "A JSON object is required."
            );
        }

        BookingSegment segment = new BookingSegment();

        segment.setPassengerId(
                requirePositiveLong(body, "passenger_id")
        );

        segment.setFlightId(
                requirePositiveLong(body, "flight_id")
        );

        segment.setSeatId(
                requirePositiveLong(body, "seat_id")
        );

        long resultId = reservationService.addSegment(
                accountId,
                reservationId,
                segment
        );

        ObjectNode response = objectMapper.createObjectNode();

        response.put("message", "Segment added successfully.");
        response.put("reservation_id", resultId);

        sendJson(exchange, 201, response);
    }

    private void removeSegment(
            HttpExchange exchange,
            long accountId,
            long reservationId,
            long segmentId
    ) throws SQLException, IOException {

        long resultId = reservationService.removeSegment(
                accountId,
                reservationId,
                segmentId
        );

        ObjectNode response = objectMapper.createObjectNode();

        response.put("message", "Segment removed successfully.");
        response.put("reservation_id", resultId);
        response.put("segment_id", segmentId);

        sendJson(exchange, 200, response);
    }

    private PassengerSessionManager.Session getAuthenticatedSession(
            HttpExchange exchange
    ) {
        String cookieHeader =
                exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        String token = null;

        for (String cookie : cookieHeader.split(";")) {
            String trimmedCookie = cookie.trim();
            int separator = trimmedCookie.indexOf('=');

            if (separator < 0) {
                continue;
            }

            String name = trimmedCookie
                    .substring(0, separator)
                    .trim();

            if (COOKIE_NAME.equals(name)) {
                token = trimmedCookie
                        .substring(separator + 1)
                        .trim();
                break;
            }
        }

        if (token == null || token.isBlank()) {
            return null;
        }

        // Validate the opaque token against server-side session storage.
        return sessionManager.getSession(token);
    }

    private long requirePositiveLong(
            JsonNode body,
            String fieldName
    ) {
        JsonNode value = body.get(fieldName);

        if (value == null
                || !value.isIntegralNumber()
                || !value.canConvertToLong()
                || value.longValue() <= 0) {

            throw new IllegalArgumentException(
                    "'" + fieldName
                            + "' must be a positive integer."
            );
        }

        return value.longValue();
    }

    private long parsePositiveId(String value) {
        try {
            long id = Long.parseLong(value);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "IDs must be positive integers."
                );
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid ID."
            );
        }
    }

    private String allowedMethods(String path) {
        if (BASE_PATH.equals(path)) {
            return "POST";
        }

        if (ADD_SEGMENT_PATH.matcher(path).matches()) {
            return "POST";
        }

        return "DELETE";
    }

    private void sendError(
            HttpExchange exchange,
            int status,
            String message
    ) throws IOException {

        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);

        sendJson(exchange, status, error);
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            Object body
    ) throws IOException {

        byte[] responseBytes =
                objectMapper.writeValueAsBytes(body);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.getResponseHeaders().set(
                "Cache-Control",
                "no-store"
        );

        exchange.sendResponseHeaders(
                status,
                responseBytes.length
        );

        try (var output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}
