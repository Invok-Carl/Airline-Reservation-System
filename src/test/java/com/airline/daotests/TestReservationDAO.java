package com.airline.daotests;

import com.airline.dao.ReservationDAO;
import com.airline.model.Reservation;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestReservationDAO {

    Reservation reservation;
    ReservationDAO reservationDAO;
    Connection connection;

    @BeforeEach
    void setup() throws Exception {
        reservationDAO = new ReservationDAO();
        reservation = new Reservation(
                "BK10009",
                LocalDateTime.of(
                    LocalDate.of(2026, 11, 01),
                    LocalTime.of(8, 30)
                ),
                "PENDING",
                290,
                22);
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        if(connection != null){
            try{
                connection.rollback();
            }finally {
                connection.close();
            }
        }
    }

    @Test
    void testCreateReservation_getReservationById_getReservationByReference() throws Exception {
        long reservationId = reservationDAO.createReservation(connection, reservation);
        assertTrue(reservationId > 0);
        Reservation actual = reservationDAO.getReservationById(connection, reservationId);
        assertNotNull(reservation);
        assertEquals(reservationId, actual.getReservation_id());
        assertEquals(reservation.getReservation_date(), actual.getReservation_date());
        assertEquals(reservation.getCreated_by(), actual.getCreated_by());
        assertEquals(reservation.getBooking_reference(), actual.getBooking_reference());
        assertEquals(reservation.getClass(), actual.getClass());
        assertEquals(reservation.getTotal_amount(), actual.getTotal_amount());


        actual = reservationDAO.getReservationByBookingReference(connection,
                actual.getBooking_reference());
        assertEquals(reservationId, actual.getReservation_id());
        assertEquals(reservation.getReservation_date(), actual.getReservation_date());
        assertEquals(reservation.getCreated_by(), actual.getCreated_by());
        assertEquals(reservation.getBooking_reference(), actual.getBooking_reference());
        assertEquals(reservation.getClass(), actual.getClass());
        assertEquals(reservation.getTotal_amount(), actual.getTotal_amount());
    }

    @Test
    void testUpdateReservationStatus_By_ID_AND_REF() throws Exception {
        long reservationId = reservationDAO.createReservation(connection, reservation);
        boolean status_conf = reservationDAO.updateReservationStatusByReservationId(
                connection, reservationId, "CONFIRMED");


        assertTrue(status_conf, "ById returned false");
        Reservation actual = reservationDAO.getReservationById(connection, reservationId);
        assertEquals("CONFIRMED", actual.getStatus());

        String booking_ref = actual.getBooking_reference();

        boolean staus_cancelled = reservationDAO.updateReservationStatusByBookingReference(
                connection, booking_ref, "CANCELLED");
        actual = reservationDAO.getReservationById(connection, reservationId);
        assertEquals("CANCELLED", actual.getStatus());

    }


    @Test
    void testgetReservations_MAP_AND_HASH() throws Exception {
        Map<Long, Reservation> reservations = reservationDAO.getAllReservationsHash(
                connection);
        List<Reservation> reservations1 = reservationDAO.getAllReservationsList(connection);

        assertEquals(8, reservations.size());
        assertEquals(8, reservations1.size());
        assertTrue(reservations.containsKey(25L), "Key not found");

        long reservationId = reservationDAO.createReservation(connection, reservation);


        reservations = reservationDAO.getAllReservationsHash(
                connection);
        reservations1 = reservationDAO.getAllReservationsList(connection);
        assertEquals(9, reservations.size());
        assertEquals(9, reservations1.size());
        assertTrue(reservations.containsKey(reservationId), "Key not found");


    }
}
