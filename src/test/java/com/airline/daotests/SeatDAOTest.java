package com.airline.daotests;

import com.airline.dao.BookingSegmentDAO;
import com.airline.dao.PassengerDAO;
import com.airline.dao.SeatDAO;
import com.airline.model.BookingSegment;
import com.airline.model.Passenger;
import com.airline.model.Seat;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeatDAOTest {

    private Connection connection;
    private SeatDAO seatDAO;
    private Seat seat;
    private BookingSegmentDAO bookingSegmentDAO;
    private BookingSegment bookingSegment;

    @BeforeEach
    void setup() throws Exception {
        connection = DatabaseConnection.getConnection();
        bookingSegmentDAO = new BookingSegmentDAO();
        connection.setAutoCommit(false);
        seatDAO = new SeatDAO();
        seat = new Seat(1, "99C", "ECONOMY", "WINDOW");
        bookingSegment = new BookingSegment(
                26, 11111111111118L,
                1, 1623,  1);
    }
    @AfterEach
    void cleanup() throws Exception {
        if(connection != null){
            try{
                connection.rollback();
            }finally {
                connection.close();
            }
        }
    }

    @Test
    void testAddSeat_getSeatById() throws Exception {
        long seatId = seatDAO.insertSeat(connection, seat);
        assertTrue(seatId > 0, "Seat not Inserted");

        Seat actual = seatDAO.getSeatById(connection, seatId);
        assertEquals( seatId, actual.getSeatId());
        assertEquals( seat.getAircraftId(), actual.getAircraftId());
        assertEquals( seat.getSeatNumber(), actual.getSeatNumber());
        assertEquals( seat.getSeatClass(), actual.getSeatClass());
        assertEquals( seat.getSeatType(), actual.getSeatType());
    }

    @Test
    void testGetAvailableSeatsByFlightId() throws Exception {
        long seatId = seatDAO.insertSeat(connection, seat);

        // Create a booking segment for this seat and flight.
        bookingSegment.setSeatId(seatId);
        bookingSegment.setFlightId(1);

        bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        Seat addedSeat = seatDAO.getSeatById(connection, seatId);
        assertNotNull(addedSeat);

        Map<Long, Seat> seats =
                seatDAO.getAvailableSeatsMapByFlight(connection, 1);

        assertTrue(seats.size() > 176, "seats are less than expected");
        assertNull(seats.get(seatId));
    }
    @Test
    void testGetSeatsByFlightMapAndList() throws SQLException{
        Map<Long, Seat> seats1 = seatDAO.getSeatsMapByFlight(connection, 1L);
        List<Seat> seats = seatDAO.getSeatsListByFlight(connection, 1L);
        assertNotNull(seats1.get(1623L));
        assertNotNull(seats.get(179));
        assertEquals(180, seats.size());
        assertEquals(180, seats1.size());
    }


    @Test
    void testDeleteSeatById() throws Exception{
        long seatId = seatDAO.insertSeat(connection, seat);
        Seat actual = seatDAO.getSeatById(connection, seatId);
        assertEquals(seatId, actual.getSeatId());

        boolean deleted = seatDAO.deleteSeatbyId(connection, seatId);
        assertTrue(deleted);
        assertFalse(seatDAO.deleteSeatbyId(connection, seatId));
        assertNull(seatDAO.getSeatById(connection, seatId));
    }

    @Test
    void testGetAllSeats() throws Exception {
        Map<Long, Seat> seats = seatDAO.getAllSeats(connection);
        assertFalse(seats.isEmpty(), "no seats fetched");
        assertEquals(1434, seats.size());
        assertNotNull(seats.get(1811L).getSeatId());
        assertEquals(1811L, seats.get(1811L).getSeatId());
    }
}
