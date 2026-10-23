package com.airline.dao;

import com.airline.model.Ticket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class TicketDAO {

    public long createTicket(Connection connection, Ticket ticket)
            throws SQLException {

        String sql = """
                INSERT INTO TICKET (
                    TICKET_NUMBER,
                    SEGMENT_ID,
                    ISSUE_DATE,
                    STATUS
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, new String[]{"TICKET_ID"})) {

            statement.setString(1, ticket.getTicketNumber());
            statement.setLong(2, ticket.getSegmentId());
            statement.setTimestamp(
                    3, Timestamp.valueOf(ticket.getIssueDate()));
            statement.setString(4, ticket.getStatus().toUpperCase());

            long result = statement.executeUpdate();

            if (result == 0) {
                throw new SQLException(
                        "Adding ticket did not affect any rows");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }

            throw new SQLException("Ticket added, but no ID returned");
        }
    }

    public Ticket getTicketById(Connection connection, long ticketId)
            throws SQLException {

        String sql = """
                SELECT
                    TICKET_ID,
                    TICKET_NUMBER,
                    SEGMENT_ID,
                    ISSUE_DATE,
                    STATUS
                FROM TICKET
                WHERE TICKET_ID = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToTicket(resultSet);
                }
                return null;
            }
        }
    }

    public Ticket getTicketByNumber(Connection connection,
                                    String ticketNumber)
            throws SQLException {

        String sql = """
                SELECT
                    TICKET_ID,
                    TICKET_NUMBER,
                    SEGMENT_ID,
                    ISSUE_DATE,
                    STATUS
                FROM TICKET
                WHERE TICKET_NUMBER = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, ticketNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToTicket(resultSet);
                }
                return null;
            }
        }
    }

    public List<Ticket> getTicketsByReservation(Connection connection,
                                                long reservationId)
            throws SQLException {

        String sql = """
                SELECT
                    t.TICKET_ID,
                    t.TICKET_NUMBER,
                    t.SEGMENT_ID,
                    t.ISSUE_DATE,
                    t.STATUS
                FROM TICKET t
                JOIN BOOKING_SEGMENT s
                    ON s.SEGMENT_ID = t.SEGMENT_ID
                WHERE s.RESERVATION_ID = ?
                ORDER BY t.TICKET_ID
                """;

        List<Ticket> tickets = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tickets.add(mapResultSetToTicket(resultSet));
                }
            }
        }

        return tickets;
    }

    public List<Ticket> getTicketsByPassenger(Connection connection,
                                              long passengerId)
            throws SQLException {

        String sql = """
                SELECT
                    t.TICKET_ID,
                    t.TICKET_NUMBER,
                    t.SEGMENT_ID,
                    t.ISSUE_DATE,
                    t.STATUS
                FROM TICKET t
                JOIN BOOKING_SEGMENT s
                    ON s.SEGMENT_ID = t.SEGMENT_ID
                WHERE s.PASSENGER_ID = ?
                ORDER BY t.TICKET_ID
                """;

        List<Ticket> tickets = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passengerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tickets.add(mapResultSetToTicket(resultSet));
                }
            }
        }

        return tickets;
    }

    public boolean updateTicketStatus(Connection connection,
                                      long ticketId,
                                      String status)
            throws SQLException {

        String sql = """
                UPDATE TICKET
                SET STATUS = ?
                WHERE TICKET_ID = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status.toUpperCase());
            statement.setLong(2, ticketId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteTicket(Connection connection, long ticketId)
            throws SQLException {

        String sql = """
                DELETE FROM TICKET
                WHERE TICKET_ID = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, ticketId);

            return statement.executeUpdate() > 0;
        }
    }

    private Ticket mapResultSetToTicket(ResultSet resultSet)
            throws SQLException {

        return new Ticket(
                resultSet.getLong("TICKET_ID"),
                resultSet.getString("TICKET_NUMBER"),
                resultSet.getLong("SEGMENT_ID"),
                resultSet.getTimestamp("ISSUE_DATE").toLocalDateTime(),
                resultSet.getString("STATUS")
        );
    }
}