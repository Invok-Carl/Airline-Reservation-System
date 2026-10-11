package com.airline.daotests.flightdoartest;

import com.airline.dao.FlightDAO;
import com.airline.model.Flight;
import com.airline.model.Passenger;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlightDAOTest {
    private Connection connection;
    private FlightDAO flightDAO;
    private Flight flight;

    @BeforeEach
    void setup () throws Exception{
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        flightDAO = new FlightDAO();


        flight = new Flight(
                "FL1001",
                LocalDate.of(2026, 11, 23),
                1,
                LocalDateTime.of(
                        LocalDate.of(2026, 11, 23),
                        LocalTime.of(8, 00)
                ),
                LocalDateTime.of(
                    LocalDate.of(2026, 11, 23),
                    LocalTime.of(11, 30)
                ),
                LocalDateTime.of(
                    LocalDate.of(2026, 11, 23),
                    LocalTime.of(8, 30)
                ),
                LocalDateTime.of(
                        LocalDate.of(2026, 11, 23),
                        LocalTime.of(11, 30)
                ), "SCHEDULED", 1, 1
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
    void testAddFlight() throws Exception{
        long flightId = flightDAO.addFlight(connection, flight);
        assertTrue(flightId > 0);

        Flight actual = flightDAO.findFlightById(connection, flightId);
        assertNotNull(actual);
        assertEquals(flightId, actual.getFlight_id());
        assertEquals(flight.getFlight_number(), actual.getFlight_number());
        assertEquals(flight.getFlight_date(), actual.getFlight_date());
        assertEquals(flight.getAircraft_id(), actual.getAircraft_id());
        assertEquals(flight.getSched_departure(), actual.getSched_departure());
        assertEquals(flight.getSched_arrival(), actual.getSched_arrival());
        assertEquals(flight.getActual_departure(), actual.getActual_departure());
        assertEquals(flight.getActual_arrival(), actual.getActual_arrival());
        assertEquals(flight.getFlight_status(), actual.getFlight_status());
        assertEquals(flight.getRoute_id(), actual.getRoute_id());
        assertEquals(flight.getSchedule_id(), actual.getSchedule_id());
    }
    @Test
    void testGetFlightById_returnsNullWhenDNE() throws Exception{
        Flight actual = flightDAO.findFlightById(connection, 99);
        assertNull(actual);
    }

    @Test
    void getAllFlights_containsInsertedFlight() throws Exception {
        long flightId = flightDAO.addFlight(connection, flight);

        assertTrue(flightId > 0, "Passenger insert failed");

        List<Flight> flights =
                flightDAO.getAllFlights(connection);
        flights.forEach(System.out::println);

        assertFalse(flights.isEmpty(), "Passenger list is empty");

        boolean found = flights.stream().anyMatch(p ->
                Long.valueOf(p.getFlight_id())
                        .equals(Long.valueOf(flightId))
        );


        assertTrue(
                found,
                "Flight ID " + flight.getFlight_id()
                        + " was not found in getAllPassengers(). "
                        + "Returned IDs: "
                        + flights.stream()
                        .map(Flight::getFlight_id)
                        .toList()
        );

    }



}
