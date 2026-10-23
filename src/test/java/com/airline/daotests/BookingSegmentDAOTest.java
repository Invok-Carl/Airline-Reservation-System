package com.airline.daotests;

import com.airline.dao.BookingSegmentDAO;
import com.airline.model.BookingSegment;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BookingSegmentDAOTest {
    BookingSegment bookingSegment;
    BookingSegmentDAO bookingSegmentDAO;
    Connection connection;


    @BeforeEach
    void setup() throws Exception {
        bookingSegmentDAO = new BookingSegmentDAO();
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        bookingSegment = new BookingSegment(
                26, 11111111111118L,
                1, 1811, 1);
    }
    @AfterEach
    void tearDown() throws SQLException{
        if(connection != null){
            try{
                connection.rollback();
            }finally {
                connection.close();
            }
        }
    }

    @Test
    void testInsertBookingSegment_getBookingSegmentById() throws SQLException {
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        assertTrue(segId > 0);

        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);

        assertNotNull(actual);

        assertEquals(segId, actual.getSegment_id());
        assertEquals(bookingSegment.getFare_id(), actual.getFare_id());
        assertEquals(bookingSegment.getPassenger_id(), actual.getPassenger_id());
        assertEquals(bookingSegment.getSeat_id(), actual.getSeat_id());
        assertEquals(bookingSegment.getReservation_id(), actual.getReservation_id());

    }
    @Test
    void testRemoveBookingSegmentBySegId() throws Exception{
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getAllBookingSegments(connection);
        assertTrue(segs.containsKey(actual.getSegment_id()));
        assertEquals(1, segs.size());

        assertTrue(bookingSegmentDAO.removeBookingSegmentBySegId(connection, segId));
        segs = bookingSegmentDAO.getAllBookingSegments(connection);
        assertFalse(segs.containsKey(actual.getSegment_id()));
        assertEquals(0, segs.size());
    }

    @Test
    void testGetBookingSegmentsByPassengerId() throws Exception {
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);

        Map<Long, BookingSegment> segs = bookingSegmentDAO.getBookingSegmentByPassengerId(connection,
                actual.getPassenger_id());
        assertTrue(segs.containsKey(actual.getSegment_id()));
        assertEquals(1, segs.size());
    }
    @Test
    void testGetAllBookingSegs() throws Exception {
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getAllBookingSegments(connection);
        assertTrue(segs.containsKey(actual.getSegment_id()));
        assertEquals(1, segs.size());
    }

    @Test
    void testGetAllBookingSegsByReservationId() throws Exception{
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getBookingSegmentsByReservationId(
                connection, actual.getReservation_id());
        assertTrue(segs.containsKey(actual.getSegment_id()));
        assertEquals(1, segs.size());

    }

    @Test
    void testGetAllBookingSegsByBookingReference() throws Exception{
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getBookingSegmentsByBookingReference(
                connection, "BK100002");
        assertTrue(segs.containsKey(actual.getSegment_id()));
        assertEquals(1, segs.size());

    }
}
