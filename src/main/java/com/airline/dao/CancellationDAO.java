package com.airline.dao;

import com.airline.model.Cancellation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CancellationDAO {

    public long createCancellation(Connection connection,
                                   Cancellation cancellation)
            throws SQLException {

        String sql = """
                INSERT INTO CANCELLATION (
                    RESERVATION_ID,
                    CANCEL_DATE,
                    REASON,
                    EMPLOYEE_ID
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, new String[]{"CANCELLATION_ID"})) {

            statement.setLong(1, cancellation.getReservation_id());
            statement.setTimestamp(
                    2, Timestamp.valueOf(cancellation.getCancel_date()));
            statement.setString(3, cancellation.getReason());
            statement.setLong(4, cancellation.getEmployee_id());

            long result = statement.executeUpdate();

            if (result == 0) {
                throw new SQLException(
                        "Adding cancellation did not affect any rows");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }

            throw new SQLException(
                    "Cancellation added, but no ID returned");
        }
    }

    public Cancellation getCancellationById(Connection connection,
                                            long cancellationId)
            throws SQLException {

        String sql = """
                SELECT
                    CANCELLATION_ID,
                    RESERVATION_ID,
                    CANCEL_DATE,
                    REASON,
                    EMPLOYEE_ID
                FROM CANCELLATION
                WHERE CANCELLATION_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cancellationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCancellation(resultSet);
                }
                return null;
            }
        }
    }

    public List<Cancellation> getCancellationsByReservation(
            Connection connection, long reservationId)
            throws SQLException {

        String sql = """
                SELECT
                    CANCELLATION_ID,
                    RESERVATION_ID,
                    CANCEL_DATE,
                    REASON,
                    EMPLOYEE_ID
                FROM CANCELLATION
                WHERE RESERVATION_ID = ?
                ORDER BY CANCEL_DATE DESC
                """;

        List<Cancellation> cancellations = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cancellations.add(
                            mapResultSetToCancellation(resultSet));
                }
            }
        }

        return cancellations;
    }

    public List<Cancellation> getAllCancellations(Connection connection)
            throws SQLException {

        String sql = """
                SELECT
                    CANCELLATION_ID,
                    RESERVATION_ID,
                    CANCEL_DATE,
                    REASON,
                    EMPLOYEE_ID
                FROM CANCELLATION
                ORDER BY CANCEL_DATE DESC
                """;

        List<Cancellation> cancellations = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                cancellations.add(mapResultSetToCancellation(resultSet));
            }
        }

        return cancellations;
    }

    public boolean updateCancellation(Connection connection,
                                      long cancellationId,
                                      Cancellation cancellation)
            throws SQLException {

        String sql = """
                UPDATE CANCELLATION
                SET RESERVATION_ID = ?,
                    CANCEL_DATE = ?,
                    REASON = ?,
                    EMPLOYEE_ID = ?
                WHERE CANCELLATION_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cancellation.getReservation_id());
            statement.setTimestamp(
                    2, Timestamp.valueOf(cancellation.getCancel_date()));
            statement.setString(3, cancellation.getReason());
            statement.setLong(4, cancellation.getEmployee_id());
            statement.setLong(5, cancellationId);

            return statement.executeUpdate() > 0;
        }
    }

    private Cancellation mapResultSetToCancellation(
            ResultSet resultSet) throws SQLException {

        return new Cancellation(
                resultSet.getLong("CANCELLATION_ID"),
                resultSet.getLong("RESERVATION_ID"),
                resultSet.getTimestamp("CANCEL_DATE").toLocalDateTime(),
                resultSet.getString("REASON"),
                resultSet.getLong("EMPLOYEE_ID")
        );
    }
}