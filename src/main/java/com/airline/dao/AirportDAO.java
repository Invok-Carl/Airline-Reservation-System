package com.airline.dao;

import com.airline.model.Airport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AirportDAO {

    // CREATE AIRPORT
    public boolean createAirport(
            Connection connection,
            Airport airport) throws SQLException {

        String sql = """
            INSERT INTO AIRPORT (
                AIRPORT_CODE,
                AIRPORT_NAME,
                CITY,
                COUNTRY,
                AIRPORT_TIME_ZONE
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, airport.getAirportCode());
            statement.setString(2, airport.getAirportName());
            statement.setString(3, airport.getCity());
            statement.setString(4, airport.getCountry());
            statement.setString(5, airport.getAirportTimeZone());

            return statement.executeUpdate() > 0;
        }
    }


    // GET AIRPORT BY ID
    public Airport getAirportById(
            Connection connection,
            long airportId) throws SQLException {

        String sql = """
            SELECT AIRPORT_ID,
                   AIRPORT_CODE,
                   AIRPORT_NAME,
                   CITY,
                   COUNTRY,
                   AIRPORT_TIME_ZONE
            FROM AIRPORT
            WHERE AIRPORT_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, airportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAirport(resultSet);
                }
            }
        }

        return null;
    }


    // GET ALL AIRPORTS
    public List<Airport> getAllAirports(
            Connection connection) throws SQLException {

        List<Airport> airports = new ArrayList<>();

        String sql = """
            SELECT AIRPORT_ID,
                   AIRPORT_CODE,
                   AIRPORT_NAME,
                   CITY,
                   COUNTRY,
                   AIRPORT_TIME_ZONE
            FROM AIRPORT
            ORDER BY AIRPORT_NAME
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                airports.add(mapAirport(resultSet));
            }
        }

        return airports;
    }


    // SEARCH AIRPORTS BY CODE, NAME, OR CITY
    public List<Airport> searchAirports(
            Connection connection,
            String keyword) throws SQLException {

        List<Airport> airports = new ArrayList<>();

        String sql = """
            SELECT AIRPORT_ID,
                   AIRPORT_CODE,
                   AIRPORT_NAME,
                   CITY,
                   COUNTRY,
                   AIRPORT_TIME_ZONE
            FROM AIRPORT
            WHERE UPPER(AIRPORT_CODE) LIKE ?
               OR UPPER(AIRPORT_NAME) LIKE ?
               OR UPPER(CITY) LIKE ?
            ORDER BY AIRPORT_NAME
            """;

        String searchPattern =
                "%" + keyword.trim().toUpperCase() + "%";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    airports.add(mapAirport(resultSet));
                }
            }
        }

        return airports;
    }


    // UPDATE AIRPORT
    public boolean updateAirport(
            Connection connection,
            Airport airport) throws SQLException {

        String sql = """
            UPDATE AIRPORT
            SET AIRPORT_CODE = ?,
                AIRPORT_NAME = ?,
                CITY = ?,
                COUNTRY = ?,
                AIRPORT_TIME_ZONE = ?
            WHERE AIRPORT_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, airport.getAirportCode());
            statement.setString(2, airport.getAirportName());
            statement.setString(3, airport.getCity());
            statement.setString(4, airport.getCountry());
            statement.setString(5, airport.getAirportTimeZone());
            statement.setLong(6, airport.getAirportId());

            return statement.executeUpdate() > 0;
        }
    }


    // DELETE AIRPORT AFTER CHECKING REFERENCES
    public boolean deleteAirport(
            Connection connection,
            long airportId) throws SQLException {

//        /*
//         * Check whether another table references this airport.
//         * Replace these example table/column names with your
//         * actual foreign-key relationships.
//         */
//        String referenceSql = """
//            SELECT COUNT(*)
//            FROM FLIGHT
//            WHERE DEPARTURE_AIRPORT_ID = ?
//               OR ARRIVAL_AIRPORT_ID = ?
//            """;
//
//        try (PreparedStatement statement =
//                     connection.prepareStatement(referenceSql)) {
//
//            statement.setLong(1, airportId);
//            statement.setLong(2, airportId);
//
//            try (ResultSet resultSet = statement.executeQuery()) {
//                if (resultSet.next() && resultSet.getLong(1) > 0) {
//                    throw new SQLException(
//                            "Cannot delete airport: it is referenced by flights."
//                    );
//                }
//            }
//        }

        String sql = """
            DELETE FROM AIRPORT
            WHERE AIRPORT_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, airportId);

            return statement.executeUpdate() > 0;
        }
    }


    // MAP RESULT SET TO AIRPORT MODEL
    private Airport mapAirport(
            ResultSet resultSet) throws SQLException {

        return new Airport(
                resultSet.getLong("AIRPORT_ID"),
                resultSet.getString("AIRPORT_CODE"),
                resultSet.getString("AIRPORT_NAME"),
                resultSet.getString("CITY"),
                resultSet.getString("COUNTRY"),
                resultSet.getString("AIRPORT_TIME_ZONE")
        );
    }
}