package com.airline.daotests;

import com.airline.dao.BookingSegmentDAO;
import com.airline.dao.FlightDAO;
import com.airline.model.BookingSegment;
import com.airline.model.Flight;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlightDAOTest {
    private Connection connection;
    private FlightDAO flightDAO;
    private Flight flight;
    BookingSegment bookingSegment;
    BookingSegmentDAO bookingSegmentDAO;

    @BeforeEach
    void setup () throws Exception{
        bookingSegmentDAO = new BookingSegmentDAO();
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        bookingSegment = new BookingSegment(
                26, 11111111111118L,
                1, 1811, 1);

        connection.setAutoCommit(false);
        flightDAO = new FlightDAO();


        flight = new Flight(
                LocalDate.of(2026, 11, 23),
                1,
                LocalTime.of(8, 30),
                LocalTime.of(11, 30),
                "SCHEDULED", 1L
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
        assertEquals(flightId, actual.getFlightId());
        assertEquals(flight.getFlightDate(), actual.getFlightDate());
        assertEquals(flight.getAircraftId(), actual.getAircraftId());
        assertEquals(flight.getActualDeparture(), actual.getActualDeparture());
        assertEquals(flight.getActualArrival(), actual.getActualArrival());
        assertEquals(flight.getFlightStatus(), actual.getFlightStatus());
        assertEquals(flight.getScheduleId(), actual.getScheduleId());
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
                flightDAO.getAllFlightsList(connection);
        flights.forEach(System.out::println);

        assertFalse(flights.isEmpty(), "Passenger list is empty");

        boolean found = flights.stream().anyMatch(p ->
                Long.valueOf(p.getFlightId())
                        .equals(Long.valueOf(flightId))
        );


        assertTrue(
                found,
                "Flight ID " + flight.getFlightId()
                        + " was not found in getAllPassengers(). "
                        + "Returned IDs: "
                        + flights.stream()
                        .map(Flight::getFlightId)
                        .toList()
        );
    }

    @Test
    void testGetAllFlightsByPassengerId() throws Exception {
        Map<Long, Flight> flights = flightDAO.findFlightsByPassengerId(connection, 11111111111118L);
        assertTrue(flights.isEmpty(), "Flights shoud've been empty");
        long BSid = bookingSegmentDAO.addBookingSegment(connection, bookingSegment);
        assertTrue(BSid > 0, "No id returned for the booking");
        flights = flightDAO.findFlightsByPassengerId(connection, 11111111111118L);
        assertFalse(flights.isEmpty(), "Flights should have one entry");
        assertEquals(1L, flights.get(1L).getFlightId() );
    }

    @Test
    void testGetFlightsByDepartureAndArrivalCountry() throws Exception {
        List<Flight> flights =
                flightDAO.getFlightsByDepAndArrivalCountry(connection,
                        "Afghanistan",
                        "United Arab Emirates"
                        );
        assertFalse(flights.isEmpty(), "No flights returned");

    }

    @Test
    void testUpdateActualArrival() throws Exception{
        boolean updated = flightDAO.updateActualArrival(
                connection, 1L, LocalTime.of(01, 30));
        assertTrue(updated, "actaul arrival not updated");
        Flight actual = flightDAO.findFlightById(connection, 1L);
        assertEquals(
                LocalTime.of(01, 30).toString(),
                actual.getActualArrival().toString());
    }
    @Test
    void testUpdateActualDeparture() throws Exception{
        boolean updated = flightDAO.updateActualDeparture(
                connection, 1L, LocalTime.of(02, 30));
        assertTrue(updated, "actaul departure not updated");
        Flight actual = flightDAO.findFlightById(connection, 1L);
        assertEquals(
                LocalTime.of(02, 30).toString(),
                actual.getActualDeparture().toString());
    }

    @Test
    void testUpdateStatus() throws Exception{
        boolean updated = flightDAO.updateFlightStatus(
                connection, 1L, "DEPARTED");
        assertTrue(updated, "flight status not updated");
        Flight actual = flightDAO.findFlightById(connection, 1L);
        assertEquals("DEPARTED", actual.getFlightStatus());
    }
}
