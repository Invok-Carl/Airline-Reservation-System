package com.airline.dao;
import com.airline.model.Flight;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {
    public long addFlight(Connection connection, Flight flight) throws SQLException {
        String sql = """
                INSERT INTO FLIGHT (
                    FLIGHT_NUMBER,
                    FLIGHT_DATE,
                    AIRCRAFT_ID,
                    SCHED_DEPARTURE,
                    SCHED_ARRIVAL,
                    ACTUAL_DEPARTURE,
                    ACTUAL_ARRIVAL,
                    FLIGHT_STATUS,
                    ROUTE_ID,
                    SCHEDULE_ID
                ) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql,
                new String[] {"FLIGHT_ID"}
                )) {

            statement.setString(1, flight.getFlight_number());
            statement.setDate(2, Date.valueOf(flight.getFlight_date()));
            statement.setLong(3, flight.getAircraft_id());
            statement.setTimestamp(4, Timestamp.valueOf(flight.getSched_departure()));
            statement.setTimestamp(5, Timestamp.valueOf(flight.getSched_arrival()));
            statement.setTimestamp(6, Timestamp.valueOf(flight.getActual_departure()));
            statement.setTimestamp(7, Timestamp.valueOf(flight.getActual_arrival()));
            statement.setString(8, flight.getFlight_status());
            statement.setLong(9, flight.getRoute_id());
            statement.setLong(10, flight.getSchedule_id());
            int result = statement.executeUpdate();
            if (result == 0) throw new SQLException("Flight insertion didn't affect any rows");
            try(ResultSet resultSet = statement.getGeneratedKeys()){
                if(resultSet.next()){
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("Flight Inserted, but Flight_id was not returned");
        }
    }

    public Flight findFlightById(Connection connection, long flightId
    ) throws SQLException {
        String sql = """
                SELECT 
                    FLIGHT_ID,
                    FLIGHT_NUMBER,
                    FLIGHT_DATE,
                    AIRCRAFT_ID,
                    SCHED_DEPARTURE,
                    SCHED_ARRIVAL,
                    ACTUAL_DEPARTURE,
                    ACTUAL_ARRIVAL,
                    FLIGHT_STATUS,
                    ROUTE_ID,
                    SCHEDULE_ID
                FROM FLIGHT
                WHERE FLIGHT_ID = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, flightId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToFlight(resultSet);
                }
                return null;
            }
        }
    }

    public Flight mapResultSetToFlight(ResultSet resultSet) throws SQLException {
        long aircraftId = resultSet.getLong("AIRCRAFT_ID");
        if (resultSet.wasNull()) {
            aircraftId = -1;
        }

        Timestamp actualDeparture = resultSet.getTimestamp("ACTUAL_DEPARTURE");
        Timestamp actualArrival = resultSet.getTimestamp("ACTUAL_ARRIVAL");
        return new Flight(
                resultSet.getLong("FLIGHT_ID"),
                resultSet.getString("FLIGHT_NUMBER"),
                resultSet.getDate("FLIGHT_DATE").toLocalDate(),
                aircraftId > 0 ? aircraftId : -1,
                resultSet.getTimestamp("SCHED_DEPARTURE").toLocalDateTime(),
                resultSet.getTimestamp("SCHED_ARRIVAL").toLocalDateTime(),
                actualDeparture != null ? actualDeparture.toLocalDateTime() : null,
                actualArrival != null ? actualArrival.toLocalDateTime() : null,
                resultSet.getString("FLIGHT_STATUS"),
                resultSet.getLong("ROUTE_ID"),
                resultSet.getLong("SCHEDULE_ID")
        );
    }

    public List<Flight> getAllFlights(Connection connection) throws SQLException {
        String sql = """
                SELECT 
                    FLIGHT_ID,
                    FLIGHT_NUMBER,
                    FLIGHT_DATE,
                    AIRCRAFT_ID,
                    SCHED_DEPARTURE,
                    SCHED_ARRIVAL,
                    ACTUAL_DEPARTURE,
                    ACTUAL_ARRIVAL,
                    FLIGHT_STATUS,
                    ROUTE_ID,
                    SCHEDULE_ID
                FROM FLIGHT
                """;
        List<Flight> flightList = new ArrayList<>();
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
            ){

            while(resultSet.next()){
                flightList.add(mapResultSetToFlight(resultSet));
            }
        }
        return flightList;
    }
}
