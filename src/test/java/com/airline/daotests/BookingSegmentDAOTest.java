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

        assertEquals(segId, actual.getSegmentId());
        assertEquals(bookingSegment.getFareId(), actual.getFareId());
        assertEquals(bookingSegment.getPassengerId(), actual.getPassengerId());
        assertEquals(bookingSegment.getSeatId(), actual.getSeatId());
        assertEquals(bookingSegment.getReservationId(), actual.getReservationId());

    }
    @Test
    void testRemoveBookingSegmentBySegId() throws Exception{
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getAllBookingSegments(connection);
        assertTrue(segs.containsKey(actual.getSegmentId()));

        assertTrue(bookingSegmentDAO.removeBookingSegmentBySegId(connection, segId));
        segs = bookingSegmentDAO.getAllBookingSegments(connection);
        assertFalse(segs.containsKey(actual.getSegmentId()));
    }

    @Test
    void testGetBookingSegmentsByPassengerId() throws Exception {
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);

        Map<Long, BookingSegment> segs = bookingSegmentDAO.getBookingSegmentByPassengerId(connection,
                actual.getPassengerId());
        assertTrue(segs.containsKey(actual.getSegmentId()));
        assertEquals(1, segs.size());
    }
    @Test
    void testGetAllBookingSegs() throws Exception {
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getAllBookingSegments(connection);
        assertTrue(segs.containsKey(actual.getSegmentId()));
    }

    @Test
    void testGetAllBookingSegsByReservationId() throws Exception{
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getBookingSegmentsByReservationId(
                connection, actual.getReservationId());
        assertTrue(segs.containsKey(actual.getSegmentId()));
        assertEquals(1, segs.size());

    }

    @Test
    void testGetAllBookingSegsByBookingReference() throws Exception{
        long segId = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        BookingSegment actual = bookingSegmentDAO.getBookingSegmentBySegId(connection, segId);
        Map<Long, BookingSegment> segs = bookingSegmentDAO.getBookingSegmentsByBookingReference(
                connection, "BK100002");
        assertTrue(segs.containsKey(actual.getSegmentId()));
        assertEquals(1, segs.size());

    }
}
