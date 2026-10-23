package com.airline.service;

import com.airline.dao.FlightScheduleDAO;
import com.airline.model.FlightSchedule;
import com.airline.util.connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class FlightScheduleService {

    private final FlightScheduleDAO flightScheduleDAO;

    public FlightScheduleService(FlightScheduleDAO flightScheduleDAO) {
        this.flightScheduleDAO = flightScheduleDAO;
    }

    public List<FlightSchedule> getAllFlightSchedules()
            throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            return flightScheduleDAO.getAllFlightSchedulesList(connection);
        }
    }
}
