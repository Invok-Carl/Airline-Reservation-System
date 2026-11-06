package com.airline.util.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpResponseUtil {
    ObjectMapper objectMapper;
    private static final Logger LOGGER =
            Logger.getLogger(HttpResponseUtil.class.getName());;
    public HttpResponseUtil(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }



    public void sendMethodNotAllowed(
            HttpExchange exchange,
            String allowedMethod
    ) throws IOException {

        exchange.getResponseHeaders().set("Allow", allowedMethod);

        sendError(exchange, 405, "HTTP method not allowed.");
    }

    public void sendError(
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

    public void sendJson(
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
}
