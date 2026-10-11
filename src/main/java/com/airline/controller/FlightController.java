
package com.airline.controller;

import com.airline.model.Flight;
import com.airline.service.FlightService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class FlightController implements HttpHandler {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (!method.equals("GET")) {
                exchange.getResponseHeaders()
                        .set("Allow", "GET");

                sendJson(exchange, 405,
                        "{\"error\":\"Method not allowed\"}");
                return;
            }

            // GET /api/flights
            if (path.equals("/api/flights")
                    || path.equals("/api/flights/")) {

                List<Flight> flights =
                        flightService.getAllFlights();

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < flights.size(); i++) {
                    if (i > 0) {
                        json.append(",");
                    }
                    json.append(toJson(flights.get(i)));
                }
                json.append("]");

                sendJson(exchange, 200, json.toString());
                return;
            }

            // GET /api/flights/{id}
            String prefix = "/api/flights/";

            if (path.startsWith(prefix)) {
                String idText = path.substring(prefix.length());

                long flightId;

                try {
                    flightId = Long.parseLong(idText);

                    if (flightId <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    sendJson(exchange, 400,
                            "{\"error\":\"Invalid flight ID\"}");
                    return;
                }

                Flight flight =
                        flightService.findFlightById(flightId);

                if (flight == null) {
                    sendJson(exchange, 404,
                            "{\"error\":\"Flight not found\"}");
                    return;
                }

                sendJson(exchange, 200, toJson(flight));
                return;
            }

            sendJson(exchange, 404,
                    "{\"error\":\"Endpoint not found\"}");

        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400,
                    "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");

        } catch (SQLException e) {
            e.printStackTrace();

            sendJson(exchange, 500,
                    "{\"error\":\"Database operation failed\"}");

        } catch (Exception e) {
            e.printStackTrace();

            sendJson(exchange, 500,
                    "{\"error\":\"Internal server error\"}");

        } finally {
            exchange.close();
        }
    }

    private String toJson(Flight flight) {
        return "{"
                + "\"flight_id\":" + flight.getFlight_id() + ","
                + "\"flight_number\":" + jsonString(flight.getFlight_number()) + ","
                + "\"flight_date\":" + jsonString(
                flight.getFlight_date() == null
                        ? null : flight.getFlight_date().toString()) + ","
                + "\"aircraft_id\":" + flight.getAircraft_id() + ","
                + "\"sched_departure\":" + jsonString(
                flight.getSched_departure() == null
                        ? null : flight.getSched_departure().toString()) + ","
                + "\"sched_arrival\":" + jsonString(
                flight.getSched_arrival() == null
                        ? null : flight.getSched_arrival().toString()) + ","
                + "\"actual_departure\":" + jsonString(
                flight.getActual_departure() == null
                        ? null : flight.getActual_departure().toString()) + ","
                + "\"actual_arrival\":" + jsonString(
                flight.getActual_arrival() == null
                        ? null : flight.getActual_arrival().toString()) + ","
                + "\"flight_status\":" + jsonString(flight.getFlight_status()) + ","
                + "\"route_id\":" + flight.getRoute_id() + ","
                + "\"schedule_id\":" + flight.getSchedule_id()
                + "}";
    }

    private String jsonString(String value) {
        if (value == null) {
            return "null";
        }

        return "\"" + escapeJson(value) + "\"";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            String json
    ) throws IOException {

        byte[] response =
                json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.getResponseHeaders().set(
                "Cache-Control", "no-store");

        exchange.sendResponseHeaders(status, response.length);

        exchange.getResponseBody().write(response);
    }
}
