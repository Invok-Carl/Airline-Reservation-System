package com.airline.dao;

import com.airline.model.PassengerAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PassengerAccountDAO {



    public PassengerAccount findByEmail(Connection connection, String email) throws SQLException {
        String sql = """
                SELECT ACCOUNT_ID,
                       PASSENGER_ID,
                       EMAIL,
                       PASSWORD_HASH,
                       ACCOUNT_STATUS,
                       CREATED_AT
                FROM PASSENGER_ACCOUNT
                WHERE EMAIL = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }

                return null;
            }
        }
    }

    public boolean existsByEmail(Connection connection, String email) throws SQLException {
        String sql = """
                SELECT 1
                FROM PASSENGER_ACCOUNT
                WHERE EMAIL = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public PassengerAccount create(Connection connection, PassengerAccount account) throws SQLException {
        String sql = """
                INSERT INTO PASSENGER_ACCOUNT (
                    PASSENGER_ID,
                    EMAIL,
                    PASSWORD_HASH
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, account.getPassengerId());
            statement.setString(2, account.getEmail());
            statement.setString(3, account.getPasswordHash());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted != 1) {
                throw new SQLException("Creating passenger account failed.");
            }
        }

        // Query the inserted account to retrieve its generated ID
        // and database-generated fields.
        PassengerAccount createdAccount = findByEmail(connection, account.getEmail());

        if (createdAccount == null) {
            throw new SQLException(
                    "Passenger account was inserted but could not be retrieved."
            );
        }

        return createdAccount;
    }

    private PassengerAccount mapRow(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("CREATED_AT");

        LocalDateTime createdAt = timestamp == null
                ? null
                : timestamp.toLocalDateTime();

        return new PassengerAccount(
                resultSet.getLong("ACCOUNT_ID"),
                resultSet.getLong("PASSENGER_ID"),
                resultSet.getString("EMAIL"),
                resultSet.getString("PASSWORD_HASH"),
                resultSet.getString("ACCOUNT_STATUS"),
                createdAt
        );
    }
}