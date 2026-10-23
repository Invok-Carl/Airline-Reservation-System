package com.airline.daotests;


import com.airline.dao.TicketDAO;
import com.airline.model.Ticket;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketDAOTest {

    private Connection connection;
    private TicketDAO ticketDAO;

    private long ticketId;
    private long segmentId;
    private String ticketNumber;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);

        ticketDAO = new TicketDAO();

        segmentId = 132;

        ticketNumber = "ASDF101";

        Ticket ticket = new Ticket(
                ticketNumber,
                segmentId,
                LocalDateTime.now(),
                "ISSUED"
        );

        ticketId = ticketDAO.createTicket(connection, ticket);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            try {
                connection.rollback();
            } finally {
                connection.close();
            }
        }
    }



    @Test
    void createTicketShouldGenerateId() {
        assertTrue(ticketId > 0);
    }

    @Test
    void getTicketByIdShouldReturnTicket() throws SQLException {
        Ticket ticket = ticketDAO.getTicketById(connection, ticketId);

        assertNotNull(ticket);
        assertEquals(ticketId, ticket.getTicketId());
        assertEquals(ticketNumber, ticket.getTicketNumber());
        assertEquals(segmentId, ticket.getSegmentId());
        assertEquals("ISSUED", ticket.getStatus());
    }

    @Test
    void getTicketByIdShouldReturnNullWhenNotFound()
            throws SQLException {
        Ticket ticket = ticketDAO.getTicketById(connection, -999L);

        assertNull(ticket);
    }

    @Test
    void getTicketByNumberShouldReturnTicket() throws SQLException {
        Ticket ticket = ticketDAO.getTicketByNumber(
                connection, ticketNumber);

        assertNotNull(ticket);
        assertEquals(ticketId, ticket.getTicketId());
        assertEquals(ticketNumber, ticket.getTicketNumber());
    }

    @Test
    void getTicketByNumberShouldReturnNullWhenNotFound()
            throws SQLException {
        Ticket ticket = ticketDAO.getTicketByNumber(
                connection, "NOT-FOUND");

        assertNull(ticket);
    }

    @Test
    void getTicketsByReservationShouldReturnList()
            throws SQLException {

        long reservationId = 25;

        List<Ticket> tickets = ticketDAO.getTicketsByReservation(
                connection, reservationId);

        assertNotNull(tickets);
        assertTrue(tickets.stream().anyMatch(
                ticket -> ticket.getTicketId() == ticketId));
    }

    @Test
    void getTicketsByPassengerShouldReturnList()
            throws SQLException {

        long passengerId = 11111111111111L;

        List<Ticket> tickets = ticketDAO.getTicketsByPassenger(
                connection, passengerId);

        assertNotNull(tickets);
        assertTrue(tickets.stream().anyMatch(
                ticket -> ticket.getTicketId() == ticketId));
    }

    @Test
    void updateTicketStatusShouldUpdateStatus()
            throws SQLException {

        boolean updated = ticketDAO.updateTicketStatus(
                connection, ticketId, "CANCELLED");

        assertTrue(updated);

        Ticket ticket = ticketDAO.getTicketById(
                connection, ticketId);

        assertNotNull(ticket);
        assertEquals("CANCELLED", ticket.getStatus());
    }

    @Test
    void updateTicketStatusShouldReturnFalseWhenNotFound()
            throws SQLException {

        assertFalse(ticketDAO.updateTicketStatus(
                connection, -999L, "CANCELLED"));
    }

    @Test
    void deleteTicketShouldDeleteTicket() throws SQLException {
        assertTrue(ticketDAO.deleteTicket(connection, ticketId));

        assertNull(ticketDAO.getTicketById(connection, ticketId));
    }

    @Test
    void deleteTicketShouldReturnFalseWhenNotFound()
            throws SQLException {

        assertFalse(ticketDAO.deleteTicket(connection, -999L));
    }

}