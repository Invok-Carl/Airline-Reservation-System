package com.airline.daotests;

import com.airline.dao.ReservationDAO;
import com.airline.model.Reservation;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReservationDAOTest {

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
                new BigDecimal("240"),
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
        assertEquals(reservationId, actual.getReservationId());
        assertEquals(reservation.getReservationDate(), actual.getReservationDate());
        assertEquals(reservation.getCreatedBy(), actual.getCreatedBy());
        assertEquals(reservation.getBookingReference(), actual.getBookingReference());
        assertEquals(reservation.getClass(), actual.getClass());

        assertEquals(0,
                reservation.getTotalAmount().compareTo( actual.getTotalAmount()));


        actual = reservationDAO.getReservationByBookingReference(connection,
                actual.getBookingReference());
        assertEquals(reservationId, actual.getReservationId());
        assertEquals(reservation.getReservationDate(), actual.getReservationDate());
        assertEquals(reservation.getCreatedBy(), actual.getCreatedBy());
        assertEquals(reservation.getBookingReference(), actual.getBookingReference());
        assertEquals(reservation.getClass(), actual.getClass());
        assertEquals(0,
                reservation.getTotalAmount().compareTo( actual.getTotalAmount()));
    }

    @Test
    void testUpdateReservationStatus_By_ID_AND_REF() throws Exception {
        long reservationId = reservationDAO.createReservation(connection, reservation);
        boolean status_conf = reservationDAO.updateReservationStatusByReservationId(
                connection, reservationId, "CONFIRMED");


        assertTrue(status_conf, "ById returned false");
        Reservation actual = reservationDAO.getReservationById(connection, reservationId);
        assertEquals("CONFIRMED", actual.getStatus());

        String booking_ref = actual.getBookingReference();

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

        assertTrue(reservations.size() > 0, "No reservations found in the list");
        assertTrue(reservations1.size() > 0, "No reservations found in the list");
        assertTrue(reservations.containsKey(25L), "Key not found");

        long reservationId = reservationDAO.createReservation(connection, reservation);


        reservations = reservationDAO.getAllReservationsHash(
                connection);
        reservations1 = reservationDAO.getAllReservationsList(connection);

        assertTrue(reservations.containsKey(reservationId), "Key not found");


    }
}
