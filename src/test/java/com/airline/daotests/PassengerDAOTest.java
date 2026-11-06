package com.airline.daotests;

import com.airline.dao.PassengerDAO;
import com.airline.model.Passenger;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class PassengerDAOTest {

    private Connection connection;
    private PassengerDAO passengerDAO;
    private Passenger passenger;

    @BeforeEach
    void setUp() throws Exception {
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);

        passengerDAO = new PassengerDAO();

        Long passengerId = ThreadLocalRandom.current()
                .nextLong(100_000_000, 999_999_999);

        passenger = new Passenger(
                passengerId,
                "TestFirstName",
                "TestLastName",
                LocalDate.of(1995, 5, 15),
                "M",
                "TestNationality",
                null,
                "test" + passengerId + "@example.com",
                "Test Address",
                "TEST" + passengerId,
                LocalDate.of(2030, 12, 31),
                "TestCountry",0L
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            try {
                connection.rollback();
            } finally {
                connection.close();
            }
        }
    }

    @Test
    void addPassenger_insertsPassenger() throws Exception {
        assertTrue(passengerDAO.addPassenger(connection, passenger));

        Passenger actual = passengerDAO.getPassengerById(
                connection, passenger.getPassengerId()
        );

        assertNotNull(actual);
        assertEquals(passenger.getPassengerId(), actual.getPassengerId());
        assertEquals(passenger.getFirstName(), actual.getFirstName());
        assertEquals(passenger.getLastName(), actual.getLastName());
        assertEquals(passenger.getDateOfBirth(), actual.getDateOfBirth());
        assertEquals(passenger.getEmail(), actual.getEmail());
        assertEquals(
                passenger.getPassportNumber(),
                actual.getPassportNumber()
        );
        assertEquals(
                passenger.getPassportExpiry(),
                actual.getPassportExpiry()
        );
    }

    @Test
    void getPassengerById_returnsNullWhenPassengerDoesNotExist()
            throws Exception {

        Passenger actual = passengerDAO.getPassengerById(
                connection, passenger.getPassengerId()
        );

        assertNull(actual);
    }

    @Test
    void getAllPassengers_containsInsertedPassenger() throws Exception {
        boolean inserted = passengerDAO.addPassenger(connection, passenger);

        assertTrue(inserted, "Passenger insert failed");

        List<Passenger> passengers =
                passengerDAO.getAllPassengers(connection);

        assertFalse(passengers.isEmpty(), "Passenger list is empty");

        boolean found = passengers.stream().anyMatch(p ->
                Long.valueOf(p.getPassengerId())
                        .equals(Long.valueOf(passenger.getPassengerId()))
        );

        assertTrue(
                found,
                "Passenger ID " + passenger.getPassengerId()
                        + " was not found in getAllPassengers(). "
                        + "Returned IDs: "
                        + passengers.stream()
                        .map(Passenger::getPassengerId)
                        .toList()
        );
    }

    @Test
    void deletePassenger_removesPassenger() throws Exception {
        passengerDAO.addPassenger(connection, passenger);

        assertTrue(passengerDAO.deletePassenger(
                connection, passenger.getPassengerId()
        ));

        assertNull(passengerDAO.getPassengerById(
                connection, passenger.getPassengerId()
        ));
    }
}