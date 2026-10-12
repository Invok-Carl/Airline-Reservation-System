
package com.airline.controller;

import com.airline.model.Flight;
import com.airline.service.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class FlightController implements HttpHandler {

    private final FlightService flightService;
    private final ObjectMapper objectMapper;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (!method.equals("GET")) {
                exchange.getResponseHeaders().set("Allow", "GET");
                sendJson(exchange, 405,
                        "{\"error\":\"Method not allowed\"}");
                return;
            }

            // GET /api/flights
            if (path.equals("/api/flights")
                    || path.equals("/api/flights/")) {

                List<Flight> flights = flightService.getAllFlights();

                sendJson(exchange, 200, flights);
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

                Flight flight = flightService.findFlightById(flightId);

                if (flight == null) {
                    sendJson(exchange, 404,
                            "{\"error\":\"Flight not found\"}");
                    return;
                }

                sendJson(exchange, 200, flight);
                return;
            }

            sendJson(exchange, 404,
                    "{\"error\":\"Endpoint not found\"}");

        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400,
                    new ErrorResponse(e.getMessage()));

        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500,
                    new ErrorResponse("Database operation failed"));

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500,
                    new ErrorResponse("Internal server error"));

        } finally {
            exchange.close();
        }
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            Object data
    ) throws IOException {

        byte[] response = objectMapper.writeValueAsBytes(data);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.getResponseHeaders().set(
                "Cache-Control", "no-store");

        exchange.sendResponseHeaders(status, response.length);

        exchange.getResponseBody().write(response);
    }

    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}


/*
The most useful methods
Method	Purpose
writeValueAsString(object)	         Convert a Java object to a JSON string
writeValueAsBytes(object)	         Convert a Java object to UTF-8 JSON bytes
readValue(json, Class)	             Convert JSON into a Java object
readValue(inputStream, Class)	     Read JSON directly from a request body
readTree(json)	                     Parse JSON into a tree you can inspect
treeToValue(node, Class)	         Convert a JSON tree node to a Java object
convertValue(object, Class)	         Convert between compatible Java representations
writerWithDefaultPrettyPrinter()	 Produce formatted, readable JSON
*/
