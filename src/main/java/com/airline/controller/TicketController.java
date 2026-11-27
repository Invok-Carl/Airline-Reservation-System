package com.airline.controller;

import com.airline.dao.ReservationDAO;
import com.airline.dao.TicketDAO;
import com.airline.model.Reservation;
import com.airline.model.Ticket;
import com.airline.util.connection.DatabaseConnection;
import com.airline.util.security.PassengerSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TicketController implements HttpHandler {

    private static final String BASE_PATH = "/api/tickets";

    private final PassengerSessionManager sessionManager;
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketController(PassengerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            PassengerSessionManager.Session session =
                    sessionManager.getPassengerSession(exchange);

            if (session == null) {
                sendError(exchange, 401, "Authentication required.");
                return;
            }

            if (!BASE_PATH.equals(path)) {
                sendError(exchange, 404, "Ticket endpoint not found.");
                return;
            }

            if (!"GET".equals(method)) {
                exchange.getResponseHeaders().set("Allow", "GET");
                sendError(exchange, 405, "HTTP method not allowed.");
                return;
            }

            listTickets(exchange, session.accountId());

        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, "A database error occurred.");

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "An unexpected server error occurred.");
        }
    }

    private void listTickets(HttpExchange exchange, long accountId)
            throws IOException, SQLException {

        ArrayNode response = objectMapper.createArrayNode();

        try (Connection connection = DatabaseConnection.getConnection()) {
            List<Reservation> reservations =
                    reservationDAO.getReservationsByCreatedBy(connection, accountId);

            for (Reservation reservation : reservations) {
                List<Ticket> tickets = ticketDAO.getTicketsByReservation(
                        connection, reservation.getReservationId());

                for (Ticket ticket : tickets) {
                    ObjectNode item = objectMapper.createObjectNode();

                    item.put("ticketId", ticket.getTicketId());
                    item.put("ticketNumber", ticket.getTicketNumber());
                    item.put("segmentId", ticket.getSegmentId());
                    item.put(
                            "issueDate",
                            ticket.getIssueDate() == null
                                    ? null
                                    : ticket.getIssueDate().toString()
                    );
                    item.put("status", ticket.getStatus());
                    item.put("bookingReference", reservation.getBookingReference());

                    response.add(item);
                }
            }
        }

        sendJson(exchange, 200, response);
    }

    private void sendError(HttpExchange exchange, int status, String message)
            throws IOException {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);
        sendJson(exchange, status, error);
    }

    private void sendJson(HttpExchange exchange, int status, Object body)
            throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(body);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");

        exchange.sendResponseHeaders(status, bytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}