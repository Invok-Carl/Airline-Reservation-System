package com.airline.dao;

import com.airline.model.Fare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FareDAO {
    public long InsertFare(Connection connection, Fare fare) throws SQLException {
        String sql = """
                INSERT INTO FARE(
                ROUTE_ID,
                FARE_CLASS,
                BASE_FARE,
                TAX,
                CURRENCY)
                VALUES (?,  ?, ?, ?, ?)
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql,
                new String[] {"FARE_ID"})){
            statement.setLong(1, fare.getRoute_id());
            statement.setString(2, fare.getFare_class().toUpperCase());
            statement.setBigDecimal(3, fare.getBase_fare());
            statement.setBigDecimal(4, fare.getTax());
            statement.setString(5, fare.getCurrency().toUpperCase());

            long result = statement.executeUpdate();
            if (result == 0) throw new SQLException("Adding fare did not affect any rows");
            try(ResultSet resultSet = statement.getGeneratedKeys()){
                if(resultSet.next()){
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("Row added to fare, but no ID returned");
        }
    }

    public boolean DeleteFare(Connection connection, long fareId) throws SQLException {
        String sql = """
                DELETE FROM FARE 
                WHERE FARE_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, fareId);
            return statement.executeUpdate() > 0;
        }
    }
    public Fare getFareByFareId(
            Connection connection, long fareId
    ) throws SQLException{
        String sql = """
                SELECT 
                    FARE_ID,
                    ROUTE_ID,
                    FARE_CLASS,
                    BASE_FARE,
                    TAX,
                    TOTAL_FARE,
                    CURRENCY
                FROM FARE
                WHERE
                FARE_ID = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, fareId);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToFare(resultSet);
                }
                return null;
            }
        }
    }




    public Fare getFareByRouteIdAndFareClass(
            Connection connection, long routeId, String fareClass
                                             ) throws SQLException{
        String sql = """
                SELECT 
                    FARE_ID,
                    ROUTE_ID,
                    FARE_CLASS,
                    BASE_FARE,
                    TAX,
                    TOTAL_FARE,
                    CURRENCY
                FROM FARE
                WHERE
                ROUTE_ID = ? AND FARE_CLASS = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, routeId);
            statement.setString(2, fareClass);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToFare(resultSet);
                }
                return null;
            }
        }
    }

    public Fare getFareByFlightIdAndSeatId(
            Connection connection, long flightId, long seatId
    ) throws SQLException{
        String sql = """
                SELECT
                    F.FARE_ID,
                    F.ROUTE_ID,
                    F.FARE_CLASS,
                    F.BASE_FARE,
                    F.TAX,
                    F.TOTAL_FARE,
                    F.CURRENCY
                FROM FLIGHT FL
                JOIN FLIGHT_SCHEDULE FS
                    ON FS.SCHEDULE_ID = FL.SCHEDULE_ID
                JOIN SEAT S
                    ON S.AIRCRAFT_ID = FL.AIRCRAFT_ID
                JOIN FARE F
                    ON F.ROUTE_ID = FS.ROUTE_ID
                   AND F.FARE_CLASS = S.SEAT_CLASS
                WHERE FL.FLIGHT_ID = ?
                  AND S.SEAT_ID = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, flightId);
            statement.setLong(2, seatId);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToFare(resultSet);
                }
                return null;
            }
        }
    }

    public Map<Long, Fare> getAllFaresMap(Connection connection) throws SQLException{
        String sql = """
                SELECT 
                    FARE_ID,
                    ROUTE_ID,
                    FARE_CLASS,
                    BASE_FARE,
                    TAX,
                    TOTAL_FARE,
                    CURRENCY
                FROM FARE
                """;
        Map<Long, Fare> fares = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    fares.put(resultSet.getLong("FARE_ID"), mapResultSetToFare(resultSet));
                }
                return fares;
            }
        }
    }

    public List<Fare> getAllFaresList(Connection connection) throws SQLException{
        String sql = """
                SELECT 
                    FARE_ID,
                    ROUTE_ID,
                    FARE_CLASS,
                    BASE_FARE,
                    TAX,
                    TOTAL_FARE,
                    CURRENCY
                FROM FARE
                """;
        List<Fare> fares = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    fares.add(mapResultSetToFare(resultSet));
                }
                return fares;
            }
        }
    }

    private Fare mapResultSetToFare(ResultSet resultSet) throws SQLException {
        return new Fare(
                resultSet.getLong("FARE_ID"),
                resultSet.getLong("ROUTE_ID"),
                resultSet.getString("FARE_CLASS"),
                resultSet.getBigDecimal("BASE_FARE"),
                resultSet.getBigDecimal("TAX"),
                resultSet.getBigDecimal("TOTAL_FARE"),
                resultSet.getString("CURRENCY")
        );
    }
}
