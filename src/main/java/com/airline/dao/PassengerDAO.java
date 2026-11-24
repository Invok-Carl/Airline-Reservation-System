package com.airline.dao;

import com.airline.model.Passenger;
import oracle.jdbc.proxy.annotation.Pre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    // =========================
    // ADD PASSENGER
    // =========================
    public boolean addPassenger(
            Connection connection,
            Passenger passenger
    ) throws SQLException {

        String sql = """
            INSERT INTO PASSENGER (
                PASSENGER_ID,
                FIRST_NAME,
                LAST_NAME,
                DATE_OF_BIRTH,
                GENDER,
                NATIONALITY,
                PHONE,
                EMAIL,
                ADDRESS,
                PASSPORT_NUMBER,
                PASSPORT_EXPIRY,
                PASSPORT_COUNTRY
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passenger.getPassengerId());
            statement.setString(2, passenger.getFirstName());
            statement.setString(3, passenger.getLastName());

            if (passenger.getDateOfBirth() != null) {
                statement.setDate(
                        4, Date.valueOf(passenger.getDateOfBirth())
                );
            } else {
                statement.setNull(4, Types.DATE);
            }

            statement.setString(5, passenger.getGender().toUpperCase());
            statement.setString(6, passenger.getNationality());
            statement.setString(7, passenger.getPhone());
            statement.setString(8, passenger.getEmail());
            statement.setString(9, passenger.getAddress());
            statement.setString(10, passenger.getPassportNumber().toUpperCase());

            if (passenger.getPassportExpiry() != null) {
                statement.setDate(
                        11, Date.valueOf(passenger.getPassportExpiry())
                );
            } else {
                statement.setNull(11, Types.DATE);
            }

            statement.setString(12, passenger.getPassportCountry());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean addPassenger(
            Connection connection,
            Passenger passenger,
            long createdBy
    ) throws SQLException {

        String sql = """
            INSERT INTO PASSENGER (
                PASSENGER_ID,
                FIRST_NAME,
                LAST_NAME,
                DATE_OF_BIRTH,
                GENDER,
                NATIONALITY,
                PHONE,
                EMAIL,
                ADDRESS,
                PASSPORT_NUMBER,
                PASSPORT_EXPIRY,
                PASSPORT_COUNTRY,
                CREATED_BY
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passenger.getPassengerId());
            statement.setString(2, passenger.getFirstName());
            statement.setString(3, passenger.getLastName());

            if (passenger.getDateOfBirth() != null) {
                statement.setDate(
                        4, Date.valueOf(passenger.getDateOfBirth())
                );
            } else {
                statement.setNull(4, Types.DATE);
            }

            statement.setString(5, passenger.getGender().toUpperCase());
            statement.setString(6, passenger.getNationality());
            statement.setString(7, passenger.getPhone());
            statement.setString(8, passenger.getEmail());
            statement.setString(9, passenger.getAddress());
            statement.setString(10, passenger.getPassportNumber().toUpperCase());

            if (passenger.getPassportExpiry() != null) {
                statement.setDate(
                        11, Date.valueOf(passenger.getPassportExpiry())
                );
            } else {
                statement.setNull(11, Types.DATE);
            }

            statement.setString(12, passenger.getPassportCountry());
            if(createdBy <= 0L){
                statement.setNull(13, Types.NUMERIC);
            }else{
                statement.setLong(13, createdBy);
            }

            return statement.executeUpdate() > 0;
        }
    }

    // =========================
    // FIND PASSENGER BY ID
    // =========================
    public Passenger getPassengerById(
            Connection connection,
            Long passengerId
    ) throws SQLException {

        String sql = """
            SELECT
                PASSENGER_ID,
                FIRST_NAME,
                LAST_NAME,
                DATE_OF_BIRTH,
                GENDER,
                NATIONALITY,
                PHONE,
                EMAIL,
                ADDRESS,
                PASSPORT_NUMBER,
                PASSPORT_EXPIRY,
                PASSPORT_COUNTRY,
                CREATED_BY
            FROM PASSENGER
            WHERE PASSENGER_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passengerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToPassenger(resultSet);
                }
                return null;
            }
        }
    }

    // =========================
    // GET ALL PASSENGERS
    // =========================
    public List<Passenger> getAllPassengers(
            Connection connection
    ) throws SQLException {

        String sql = """
            SELECT
                PASSENGER_ID,
                FIRST_NAME,
                LAST_NAME,
                DATE_OF_BIRTH,
                GENDER,
                NATIONALITY,
                PHONE,
                EMAIL,
                ADDRESS,
                PASSPORT_NUMBER,
                PASSPORT_EXPIRY,
                PASSPORT_COUNTRY,
                CREATED_BY
            FROM PASSENGER
            ORDER BY PASSENGER_ID
            """;

        List<Passenger> passengers = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                passengers.add(mapResultSetToPassenger(resultSet));
            }
        }

        return passengers;
    }

    public void updatePassengerById(
            Connection connection, Passenger passenger
                                       ) throws SQLException{
        String sql = """
                UPDATE PASSENGER SET
                    FIRST_NAME = ?,
                    LAST_NAME = ?,
                    DATE_OF_BIRTH = ?,
                    GENDER = ?,
                    NATIONALITY = ?,
                    PHONE = ?,
                    EMAIL = ?,
                    ADDRESS = ?,
                    PASSPORT_NUMBER = ?,
                    PASSPORT_EXPIRY = ?,
                    PASSPORT_COUNTRY = ?
                WHERE PASSENGER_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, passenger.getFirstName());
            statement.setString(2, passenger.getLastName());
            statement.setDate(3, Date.valueOf(passenger.getDateOfBirth()));
            statement.setString(4, passenger.getGender());
            statement.setString(5, passenger.getNationality());
            statement.setString(6, passenger.getPhone());
            statement.setString(7, passenger.getEmail());
            statement.setString(8, passenger.getAddress());
            statement.setString(9, passenger.getPassportNumber());
            statement.setDate(10, Date.valueOf(passenger.getPassportExpiry()));
            statement.setString(11, passenger.getPassportCountry());
            statement.setLong(12, passenger.getPassengerId());

            int result = statement.executeUpdate();
            if(result <= 0) throw new SQLException("Passenger info not updated");
        }
    }


    // =========================
    // DELETE PASSENGER
    // =========================
    public boolean deletePassenger(
            Connection connection,
            Long passengerId
    ) throws SQLException {

        String sql = """
            DELETE FROM PASSENGER
            WHERE PASSENGER_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passengerId);

            return statement.executeUpdate() > 0;
        }
    }


    public List<Passenger> getPassengersByCreatedBy(Connection connection, long createdBy)
        throws SQLException{
        String sql = """
            SELECT
                PASSENGER_ID,
                FIRST_NAME,
                LAST_NAME,
                DATE_OF_BIRTH,
                GENDER,
                NATIONALITY,
                PHONE,
                EMAIL,
                ADDRESS,
                PASSPORT_NUMBER,
                PASSPORT_EXPIRY,
                PASSPORT_COUNTRY,
                CREATED_BY
            FROM PASSENGER
            WHERE CREATED_BY = ?
            ORDER BY PASSENGER_ID
                """;
        List<Passenger> passengers = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setLong(1, createdBy);
            try (ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()) {
                    passengers.add(mapResultSetToPassenger(resultSet));
                }
            }
        }

        return passengers;
    }

    public boolean deletePassenger(
            Connection connection,
            Long passengerId,
            long createdBy
    ) throws SQLException {

        String sql = """
            DELETE FROM PASSENGER
            WHERE PASSENGER_ID = ?
            AND CREATED_BY = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passengerId);
            statement.setLong(2, createdBy);

            return statement.executeUpdate() > 0;
        }
    }

    // =========================
    // RESULTSET → PASSENGER
    // =========================
    private Passenger mapResultSetToPassenger(
            ResultSet resultSet
    ) throws SQLException {

        Date dateOfBirth = resultSet.getDate("DATE_OF_BIRTH");
        Date passportExpiry = resultSet.getDate("PASSPORT_EXPIRY");
        long createdBy = resultSet.getLong("CREATED_BY");

        return new Passenger(
                resultSet.getLong("PASSENGER_ID"),
                resultSet.getString("FIRST_NAME"),
                resultSet.getString("LAST_NAME"),
                dateOfBirth != null ? dateOfBirth.toLocalDate() : null,
                resultSet.getString("GENDER"),
                resultSet.getString("NATIONALITY"),
                resultSet.getString("PHONE"),
                resultSet.getString("EMAIL"),
                resultSet.getString("ADDRESS"),
                resultSet.getString("PASSPORT_NUMBER"),
                passportExpiry != null ? passportExpiry.toLocalDate() : null,
                resultSet.getString("PASSPORT_COUNTRY"),
                createdBy < 1L ? 0L : createdBy

        );
    }
}
