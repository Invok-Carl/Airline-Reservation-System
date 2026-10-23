
package com.airline.service;

import com.airline.dao.FlightDAO;
import com.airline.dao.FlightScheduleDAO;
import com.airline.model.Employee;
import com.airline.model.Flight;
import com.airline.model.FlightSchedule;
import com.airline.util.connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class FlightService {

    private final FlightDAO flightDAO;
    private final FlightScheduleDAO flightScheduleDAO;
    private final AuthorizationService authorizationService;

    public FlightService() {
        this.flightDAO = new FlightDAO();
        this.flightScheduleDAO = new FlightScheduleDAO();
        this.authorizationService = new AuthorizationService();
    }

    public Flight findFlightById(long flightId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return flightDAO.findFlightById(connection, flightId);
        }
    }

    public List<Flight> getAllFlights() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return flightDAO.getAllFlightsList(connection);
        }
    }

    public Map<Long, Flight> getAllFlightsMap() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return flightDAO.getAllFlightsMap(connection);
        }
    }

    public Map<Long, Flight> findFlightsByPassengerId(long passengerId)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return flightDAO.findFlightsByPassengerId(
                    connection,
                    passengerId
            );
        }
    }



    public List<Flight> getFlightsByDepAndArrCountries(
            String departureCountry, String arrivalCountry
            ) throws SQLException{
        try(Connection connection = DatabaseConnection.getConnection()){
            return flightDAO.getFlightsByDepAndArrivalCountry(
                    connection,
                    departureCountry,
                    arrivalCountry);
        }
    }

    public boolean updateFlightActualDeparture(
            Employee employee, long flight_id, LocalTime actualDeparture)
    throws SQLException{
        if(!authorizationService.canAddFlight(employee)){
            throw new SecurityException("Only administrator can update departure time");
        }
        try(Connection connection = DatabaseConnection.getConnection()){
            return flightDAO.updateActualDeparture(connection, flight_id, actualDeparture);
        }
    }


    public boolean updateFlightActualArrival(
            Employee employee, long flight_id, LocalTime actualarrival)
            throws SQLException{
        if(!authorizationService.canAddFlight(employee)){
            throw new SecurityException("Only administrator can update arrival time");
        }
        try(Connection connection = DatabaseConnection.getConnection()){
            return flightDAO.updateActualDeparture(connection, flight_id, actualarrival);
        }
    }

    public long addFlight(Employee employee, Flight flight)
            throws SQLException {

        if (!authorizationService.canAddFlight(employee)) {
            throw new SecurityException(
                    "Only an administrator can add a flight."
            );
        }

        validateNewFlight(flight);

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Verify that the referenced schedule exists.
            FlightSchedule schedule = flightScheduleDAO.getScheduleById(
                    connection,
                    flight.getSchedule_id()
            );

            if (schedule == null) {
                throw new IllegalArgumentException(
                        "The specified flight schedule does not exist."
                );
            }

            // The flight date must be within the schedule's date range.
            LocalDate flightDate = flight.getFlight_date();

            if (flightDate.isBefore(schedule.getStart_date())
                    || flightDate.isAfter(schedule.getEnd_date())) {
                throw new IllegalArgumentException(
                        "The flight date must fall within the schedule's "
                                + "start and end dates."
                );
            }

            // Insert the flight using the same connection.
            return flightDAO.addFlight(connection, flight);
        }
    }

    private void validateNewFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException(
                    "Flight must not be null."
            );
        }

        if (flight.getFlight_date() == null) {
            throw new IllegalArgumentException(
                    "Flight date is required."
            );
        }

        if (flight.getSchedule_id() <= 0) {
            throw new IllegalArgumentException(
                    "A valid schedule ID is required."
            );
        }

        if (flight.getFlight_status() == null
                || flight.getFlight_status().isBlank()) {
            throw new IllegalArgumentException(
                    "Flight status is required."
            );
        }

        if (flight.getActual_arrival() != null
                && flight.getActual_departure() == null) {
            throw new IllegalArgumentException(
                    "Actual arrival cannot be set without actual departure."
            );
        }

        if (flight.getActual_departure() != null
                && flight.getActual_arrival() != null
                && !flight.getActual_arrival()
                .isAfter(flight.getActual_departure())) {
            throw new IllegalArgumentException(
                    "Actual arrival must be later than actual departure."
            );
        }
    }
}
