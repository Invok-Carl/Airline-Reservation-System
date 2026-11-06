package com.airline.dao;

import com.airline.model.Fare;
import com.airline.model.FlightSchedule;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightScheduleDAO {
    public long insertFlightSchedule(
            Connection connection, FlightSchedule flightSchedule) throws SQLException {
        String sql = """
                INSERT INTO FLIGHT_SCHEDULE(
                FLIGHT_NUMBER,
                ROUTE_ID,
                DAYS_OF_OPERATION,
                START_DATE,
                END_DATE,
                SCHEDULE_STATUS,
                DEP_TIME,
                ARR_TIME
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                new String[]{"SCHEDULE_ID"}
        )) {
            statement.setString(1, flightSchedule.getFlightNumber());
            statement.setLong(2, flightSchedule.getRouteId());
            statement.setString(3, flightSchedule.getDaysOfOperation());
            statement.setDate(4, Date.valueOf(flightSchedule.getStartDate()));
            statement.setDate(5, Date.valueOf(flightSchedule.getEndDate()));
            statement.setString(6, flightSchedule.getScheduleStatus());
            statement.setString(7, flightSchedule.getDepartureTime().toString());
            statement.setString(8, flightSchedule.getArrivalTime().toString());




            long result = statement.executeUpdate();
            if (result == 0) throw new SQLException("Schedule insertion did not affect any rows");
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("flight_schedule inserted, but No key returned for the schedule");
        }
    }

    public boolean deleteFlightScheduleById(Connection connection, long schedule_id)
    throws SQLException {
        String sql = """
                DELETE FROM FLIGHT_SCHEDULE
                WHERE SCHEDULE_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, schedule_id);
            return statement.executeUpdate() > 0;
        }
    }


    public FlightSchedule getScheduleById(Connection connection, long scheduleId)
    throws SQLException{
        String sql = """
                SELECT 
                    SCHEDULE_ID,
                    FLIGHT_NUMBER,
                    ROUTE_ID,
                    DAYS_OF_OPERATION,
                    START_DATE,
                    END_DATE,
                    SCHEDULE_STATUS,
                    DEP_TIME, 
                    ARR_TIME
                FROM FLIGHT_SCHEDULE
                WHERE SCHEDULE_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, scheduleId);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToFlightSchedule(resultSet);
                }
            }
            return  null;
        }
    }



    public Map<Long, FlightSchedule> getAllFlightSchedulesMap(
            Connection connection
    ) throws SQLException{
        String sql = """
                SELECT 
                    SCHEDULE_ID,
                    FLIGHT_NUMBER,
                    ROUTE_ID,
                    DAYS_OF_OPERATION,
                    START_DATE,
                    END_DATE,
                    SCHEDULE_STATUS,
                    DEP_TIME, 
                    ARR_TIME
                FROM FLIGHT_SCHEDULE
                """;
        Map<Long, FlightSchedule> schedules = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    schedules.put(
                            resultSet.getLong(1),
                            mapResultSetToFlightSchedule(resultSet)
                    );
                }
            }
            return  schedules;
        }
    }



    public List< FlightSchedule> getAllFlightSchedulesList(Connection connection)
            throws SQLException{
        String sql = """
                SELECT 
                    SCHEDULE_ID,
                    FLIGHT_NUMBER,
                    ROUTE_ID,
                    DAYS_OF_OPERATION,
                    START_DATE,
                    END_DATE,
                    SCHEDULE_STATUS,
                    DEP_TIME, 
                    ARR_TIME
                FROM FLIGHT_SCHEDULE
                """;
        List< FlightSchedule> schedules = new ArrayList<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    schedules.add(mapResultSetToFlightSchedule(resultSet));
                }
            }
            return  schedules;
        }
    }

    private FlightSchedule mapResultSetToFlightSchedule(ResultSet resultSet)
    throws SQLException {
        return new FlightSchedule(
                resultSet.getLong("SCHEDULE_ID"),
                resultSet.getString("FLIGHT_NUMBER"),
                resultSet.getLong("ROUTE_ID"),
                resultSet.getString("DAYS_OF_OPERATION"),
                resultSet.getDate("START_DATE").toLocalDate(),
                resultSet.getDate("END_DATE").toLocalDate(),
                resultSet.getString("SCHEDULE_STATUS"),
                LocalTime.parse(resultSet.getString("DEP_TIME")),
                LocalTime.parse(resultSet.getString("ARR_TIME"))
        );
    }
}

