package com.airline.service;

import com.airline.dao.FlightDAO;
import com.airline.model.Flight;
import com.airline.util.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

public class FlightService {

    private final FlightDAO flightDAO;
    private final ConnectionProvider connectionProvider;

    public FlightService(FlightDAO flightDAO,
                         ConnectionProvider connectionProvider) {
        this.flightDAO = flightDAO;
        this.connectionProvider = connectionProvider;
    }

    public List<Flight> getAllFlights() throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            return flightDAO.getAllFlights(connection);
        }
    }

    public Flight findFlightById(long flightId) throws SQLException {
        if (flightId <= 0) {
            throw new IllegalArgumentException(
                    "Flight ID must be positive."
            );
        }

        try (Connection connection = connectionProvider.getConnection()) {
            return flightDAO.findFlightById(connection, flightId);
        }
    }

    public long addFlight(Flight flight) throws SQLException {
        validateFlight(flight);

        try (Connection connection = connectionProvider.getConnection()) {
            return flightDAO.addFlight(connection, flight);
        }
    }

    private void validateFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException(
                    "Flight cannot be null."
            );
        }

        if (flight.getFlight_number() == null
                || flight.getFlight_number().isBlank()) {
            throw new IllegalArgumentException(
                    "Flight number is required."
            );
        }

        if (flight.getFlight_date() == null) {
            throw new IllegalArgumentException(
                    "Flight date is required."
            );
        }

        if (flight.getAircraft_id() <= 0) {
            throw new IllegalArgumentException(
                    "A valid aircraft ID is required."
            );
        }

        if (flight.getRoute_id() <= 0) {
            throw new IllegalArgumentException(
                    "A valid route ID is required."
            );
        }

        if (flight.getSchedule_id() <= 0) {
            throw new IllegalArgumentException(
                    "A valid schedule ID is required."
            );
        }

        if (flight.getSched_departure() == null
                || flight.getSched_arrival() == null) {
            throw new IllegalArgumentException(
                    "Scheduled departure and arrival are required."
            );
        }

        if (!flight.getSched_arrival()
                .isAfter(flight.getSched_departure())) {
            throw new IllegalArgumentException(
                    "Scheduled arrival must be after departure."
            );
        }

        if (flight.getActual_departure() != null
                && flight.getActual_arrival() != null
                && !flight.getActual_arrival()
                .isAfter(flight.getActual_departure())) {
            throw new IllegalArgumentException(
                    "Actual arrival must be after actual departure."
            );
        }
    }
}
