package com.airline.daotests;

import com.airline.dao.PassengerDAO;
import com.airline.dao.SeatDAO;
import com.airline.model.Passenger;
import com.airline.model.Seat;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeatDAOTest {

    private Connection connection;
    private SeatDAO seatDAO;
    private Seat seat;

    @BeforeEach
    void setup() throws Exception {
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        seatDAO = new SeatDAO();
        seat = new Seat(1, "99C", "ECONOMY", "WINDOW");
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
        assertEquals( seatId, actual.getSeat_id());
        assertEquals( seat.getAircraft_id(), actual.getAircraft_id());
        assertEquals( seat.getSeat_number(), actual.getSeat_number());
        assertEquals( seat.getSeat_class(), actual.getSeat_class());
        assertEquals( seat.getSeat_type(), actual.getSeat_type());
    }

    @Test
    void testGetSeatsByFlightId() throws Exception {
        long seatId = seatDAO.insertSeat(connection, seat);
        Map<Long, Seat> seats = seatDAO.getSeatsByFlight(connection, 1);
        assertEquals(181, seats.size());
        seat.setSeat_id(seatId);
        assertNotNull(seats.get(seatId).getSeat_id());
        assertEquals(seatId, seats.get(seatId).getSeat_id());
    }


    @Test
    void testDeleteSeatById() throws Exception{
        long seatId = seatDAO.insertSeat(connection, seat);
        Seat actual = seatDAO.getSeatById(connection, seatId);
        assertEquals(seatId, actual.getSeat_id());

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
        assertNotNull(seats.get(1811L).getSeat_id());
        assertEquals(1811L, seats.get(1811L).getSeat_id());
    }
}
