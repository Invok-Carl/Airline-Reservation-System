package com.airline.dao;

import com.airline.model.Aircraft;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AircraftDAO {
    public long insertAircraft(Connection connection, Aircraft aircraft) throws SQLException{
        String sql = """
                INSERT INTO AIRCRAFT(
                REGISTRATION_NUMBER,
                AIRCRAFT_MODEL,
                MANUFACTURER,
                AIRCRAFT_CAPACITY,
                STATUS
                )
                VALUES
                (?, ?, ?, ?, ?)
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql,
                new String[] {"AIRCRAFT_ID"})){
            statement.setString(1, aircraft.getRegistrantionNumber());
            statement.setString(2, aircraft.getAircraftModel().toUpperCase());
            statement.setString(3, aircraft.getManufacturer());
            statement.setLong(4, aircraft.getCapacity());
            statement.setString(5, aircraft.getStatus().toUpperCase());
            long result = statement.executeUpdate();
            if (result == 0) throw new SQLException("Inserting aircraft did not affect any rows");
            try(ResultSet resultSet = statement.getGeneratedKeys()){
                if(resultSet.next()){
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("Aircraft Inserted but no row returned");
        }
    }

    public Aircraft getAircraftById (Connection connection, long aircraftId)
            throws SQLException{
        String sql = """
                SELECT
                    AIRCRAFT_ID,
                    REGISTRATION_NUMBER,
                    AIRCRAFT_MODEL,
                    MANUFACTURER,
                    AIRCRAFT_CAPACITY,
                    STATUS
                FROM AIRCRAFT
                WHERE AIRCRAFT_ID = ? 
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, aircraftId);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToAircraft(resultSet);
                }
                return null;
            }
        }
    }

    public Map<Long, Aircraft> getAllAircraftsMap(Connection connection)
            throws SQLException{
        String sql = """
                SELECT
                    AIRCRAFT_ID,
                    REGISTRATION_NUMBER,
                    AIRCRAFT_MODEL,
                    MANUFACTURER,
                    AIRCRAFT_CAPACITY,
                    STATUS
                FROM AIRCRAFT
                """;
        Map<Long, Aircraft> aircrafts = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    aircrafts.put(
                            resultSet.getLong("AIRCRAFT_ID"),
                            mapResultSetToAircraft(resultSet)
                    );
                }
                return aircrafts;
            }
        }
    }

    public List<Aircraft> getAllAircraftsList(Connection connection)
            throws SQLException{
        String sql = """
                SELECT
                    AIRCRAFT_ID,
                    REGISTRATION_NUMBER,
                    AIRCRAFT_MODEL,
                    MANUFACTURER,
                    AIRCRAFT_CAPACITY,
                    STATUS
                FROM AIRCRAFT
                """;
        List<Aircraft> aircrafts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    aircrafts.add(
                            mapResultSetToAircraft(resultSet)
                    );
                }
                return aircrafts;
            }
        }
    }



    public boolean deleteAircraft(Connection connection, long aircraftId)
    throws SQLException {
        String sql = """
                    DELETE
                    FROM AIRCRAFT
                    WHERE AIRCRAFT_ID = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, aircraftId);
            return statement.executeUpdate() > 0;
        }
    }

    private Aircraft mapResultSetToAircraft(ResultSet resultSet) throws SQLException{
        return new Aircraft(
                resultSet.getLong("AIRCRAFT_ID"),
                resultSet.getString("REGISTRATION_NUMBER"),
                resultSet.getString("AIRCRAFT_MODEL"),
                resultSet.getString("MANUFACTURER"),
                resultSet.getLong("AIRCRAFT_CAPACITY"),
                resultSet.getString("STATUS")
        );
    }
}
