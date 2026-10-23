package com.airline.dao;

import com.airline.model.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public long createPayment(Connection connection, Payment payment)
            throws SQLException {

        String sql = """
                INSERT INTO PAYMENT (
                    RESERVATION_ID,
                    PAYMENT_DATE,
                    AMOUNT,
                    CURRENCY,
                    PAYMENT_METHOD,
                    PAYMENT_STATUS,
                    TRANSACTION_REF,
                    RECORDED_BY
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, new String[]{"PAYMENT_ID"})) {

            statement.setLong(1, payment.getReservation_id());
            statement.setTimestamp(
                    2, Timestamp.valueOf(payment.getPayment_date()));
            statement.setBigDecimal(3, payment.getAmount());
            statement.setString(4, payment.getCurrency().toUpperCase());
            statement.setString(
                    5, payment.getPayment_method().toUpperCase());
            statement.setString(
                    6, payment.getPayment_status().toUpperCase());
            if(payment.getTransaction_ref() == null){
                statement.setNull(7, Types.VARCHAR);
            }else{
                statement.setString(7, payment.getTransaction_ref());
            }
            statement.setLong(8, payment.getRecorded_by());

            long result = statement.executeUpdate();

            if (result == 0) {
                throw new SQLException(
                        "Adding payment did not affect any rows");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }

            throw new SQLException("Payment added, but no ID returned");
        }
    }

    public Payment getPaymentById(Connection connection, long paymentId)
            throws SQLException {

        String sql = """
                SELECT
                    PAYMENT_ID,
                    RESERVATION_ID,
                    PAYMENT_DATE,
                    AMOUNT,
                    CURRENCY,
                    PAYMENT_METHOD,
                    PAYMENT_STATUS,
                    TRANSACTION_REF,
                    RECORDED_BY
                FROM PAYMENT
                WHERE PAYMENT_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToPayment(resultSet);
                }
                return null;
            }
        }
    }

    public List<Payment> getPaymentsByReservation(
            Connection connection, long reservationId)
            throws SQLException {

        String sql = """
                SELECT
                    PAYMENT_ID,
                    RESERVATION_ID,
                    PAYMENT_DATE,
                    AMOUNT,
                    CURRENCY,
                    PAYMENT_METHOD,
                    PAYMENT_STATUS,
                    TRANSACTION_REF,
                    RECORDED_BY
                FROM PAYMENT
                WHERE RESERVATION_ID = ?
                ORDER BY PAYMENT_DATE DESC
                """;

        List<Payment> payments = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(mapResultSetToPayment(resultSet));
                }
            }
        }

        return payments;
    }

    public List<Payment> getAllPayments(Connection connection)
            throws SQLException {

        String sql = """
                SELECT
                    PAYMENT_ID,
                    RESERVATION_ID,
                    PAYMENT_DATE,
                    AMOUNT,
                    CURRENCY,
                    PAYMENT_METHOD,
                    PAYMENT_STATUS,
                    TRANSACTION_REF,
                    RECORDED_BY
                FROM PAYMENT
                ORDER BY PAYMENT_DATE DESC
                """;

        List<Payment> payments = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                payments.add(mapResultSetToPayment(resultSet));
            }
        }

        return payments;
    }

    public boolean updatePaymentStatus(Connection connection,
                                       long paymentId,
                                       String paymentStatus)
            throws SQLException {

        String sql = """
                UPDATE PAYMENT
                SET PAYMENT_STATUS = ?
                WHERE PAYMENT_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, paymentStatus.toUpperCase());
            statement.setLong(2, paymentId);

            return statement.executeUpdate() > 0;
        }
    }

    public BigDecimal getPaidAmountByReservation(
            Connection connection, long reservationId)
            throws SQLException {

        String sql = """
                SELECT NVL(SUM(AMOUNT), 0) AS PAID_AMOUNT
                FROM PAYMENT
                WHERE RESERVATION_ID = ?
                  AND PAYMENT_STATUS = 'COMPLETED'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("PAID_AMOUNT");
                }
                return BigDecimal.ZERO;
            }
        }
    }

    private Payment mapResultSetToPayment(ResultSet resultSet)
            throws SQLException {
        String transactionRef = resultSet.getString("TRANSACTION_REF");
        return new Payment(
                resultSet.getLong("PAYMENT_ID"),
                resultSet.getLong("RESERVATION_ID"),
                resultSet.getTimestamp("PAYMENT_DATE").toLocalDateTime(),
                resultSet.getBigDecimal("AMOUNT"),
                resultSet.getString("CURRENCY"),
                resultSet.getString("PAYMENT_METHOD"),
                resultSet.getString("PAYMENT_STATUS"),
                transactionRef != null ? transactionRef : null,
                resultSet.getLong("RECORDED_BY")
        );
    }
}