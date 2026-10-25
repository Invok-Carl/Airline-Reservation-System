
package com.airline.service;

import com.airline.dao.PassengerAccountDAO;
import com.airline.dao.PassengerDAO;
import com.airline.model.Passenger;
import com.airline.model.PassengerAccount;
import com.airline.util.connection.DatabaseConnection;
import com.airline.util.security.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

public class PassengerAccountService {

    private final PassengerDAO passengerDAO;
    private final PassengerAccountDAO passengerAccountDAO;

    public PassengerAccountService(
            PassengerDAO passengerDAO,
            PassengerAccountDAO passengerAccountDAO
    ) {
        this.passengerDAO = passengerDAO;
        this.passengerAccountDAO = passengerAccountDAO;
    }

    public PassengerAccount register(
            Passenger passenger,
            String email,
            String password
    ) throws SQLException {

        if (passenger == null) {
            throw new IllegalArgumentException(
                    "Passenger details are required."
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters."
            );
        }

        if (passenger.getPassengerId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid passenger ID is required."
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        String passwordHash =
                PasswordHasher.hashPassword(password);

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            boolean originalAutoCommit = connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                if (passengerAccountDAO.existsByEmail(
                        connection,
                        normalizedEmail
                )) {
                    throw new IllegalArgumentException(
                            "An account with this email already exists."
                    );
                }

                passenger.setEmail(normalizedEmail);

                boolean passengerInserted =
                        passengerDAO.addPassenger(connection, passenger);

                if (!passengerInserted) {
                    throw new SQLException(
                            "Failed to create passenger record."
                    );
                }

                PassengerAccount account = new PassengerAccount(
                        0L,
                        passenger.getPassengerId(),
                        normalizedEmail,
                        passwordHash,
                        "ACTIVE",
                        null
                );

                PassengerAccount createdAccount =
                        passengerAccountDAO.create(connection, account);

                if (createdAccount == null) {
                    throw new SQLException(
                            "Failed to create passenger account."
                    );
                }

                connection.commit();

                return createdAccount;

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;

            } finally {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                    // Closing the connection is handled by try-with-resources.
                }
            }
        }
    }

    public PassengerAccount login(
            String email,
            String password
    ) throws SQLException {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Password is required."
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            PassengerAccount account =
                    passengerAccountDAO.findByEmail(
                            connection,
                            normalizedEmail
                    );

            if (account == null) {
                return null;
            }

            if (!"ACTIVE".equals(account.getAccountStatus())) {
                return null;
            }

            if (!PasswordHasher.verifyPassword(
                    password,
                    account.getPasswordHash()
            )) {
                return null;
            }

            return account;
        }
    }
}
