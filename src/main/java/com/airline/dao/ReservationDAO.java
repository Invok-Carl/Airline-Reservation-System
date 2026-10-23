package com.airline.dao;

import com.airline.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationDAO {
    public long createReservation(Connection connection,
                                  Reservation reservation) throws SQLException {
        String sql = """
                INSERT INTO RESERVATION (
                    BOOKING_REFERENCE,
                    RESERVATION_DATE,
                    STATUS,
                    TOTAL_AMOUNT,
                    CREATED_BY
                )
                VALUES (?, ?, ?, ?, ?)
                """;
            try(PreparedStatement statement = connection.prepareStatement(sql,
                    new String[] {"RESERVATION_ID"}
                    )
            ){
                statement.setString(1, reservation.getBooking_reference());
                statement.setTimestamp(2,
                        Timestamp.valueOf(reservation.getReservation_date()));
                statement.setString(3, reservation.getStatus().toUpperCase());
                statement.setBigDecimal(4, reservation.getTotal_amount());
                statement.setLong(5, reservation.getCreated_by());
                long result = statement.executeUpdate();
                if (result == 0) {
                    throw new SQLException("Adding reservation did not affect any rows");
                }
                try(ResultSet resultSet = statement.getGeneratedKeys()){
                    if(resultSet.next()){
                        return resultSet.getLong(1);
                    }
                }
                throw new SQLException("Reservation added, but no Id returned");
            }
    }
    public boolean updateReservationStatusByReservationId(
            Connection connection, long reservation_id, String status)
    throws SQLException{
        String sql = """
                UPDATE RESERVATION
                SET STATUS = ?
                WHERE RESERVATION_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, status);
            statement.setLong(2, reservation_id);

            return statement.executeUpdate() > 0;
        }
    }
    public boolean updateReservationStatusByBookingReference(
            Connection connection, String booking_reference, String status)
            throws SQLException{
        String sql = """
                UPDATE RESERVATION
                SET STATUS = ?
                WHERE BOOKING_REFERENCE = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, status);
            statement.setString(2, booking_reference);

            return statement.executeUpdate() > 0;
        }
    }

    public Reservation getReservationById(Connection connection, long id)
    throws SQLException {
        String sql = """
                SELECT
                    RESERVATION_ID,
                    BOOKING_REFERENCE,
                    RESERVATION_DATE,
                    STATUS,
                    TOTAL_AMOUNT,
                    CREATED_BY
                FROM RESERVATION
                WHERE RESERVATION_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToReservation(resultSet);
                }
                return null;
            }
        }
    }


    public Reservation getReservationByBookingReference(Connection connection,
                                                        String bookingReference)
            throws SQLException {
        String sql = """
                SELECT
                    RESERVATION_ID,
                    BOOKING_REFERENCE,
                    RESERVATION_DATE,
                    STATUS,
                    TOTAL_AMOUNT,
                    CREATED_BY
                FROM RESERVATION
                WHERE BOOKING_REFERENCE = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, bookingReference);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToReservation(resultSet);
                }
                return null;
            }
        }
    }

    public Map<Long, Reservation> getAllReservationsHash(Connection connection)
            throws SQLException{
        String sql = """
                SELECT
                    RESERVATION_ID,
                    BOOKING_REFERENCE,
                    RESERVATION_DATE,
                    STATUS,
                    TOTAL_AMOUNT,
                    CREATED_BY
                FROM RESERVATION
                """;
        Map<Long, Reservation> reservaitons = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()) {
                    reservaitons.put(resultSet.getLong(
                            "RESERVATION_ID"), mapResultSetToReservation(resultSet)
                    );
                }
                return reservaitons;
            }
        }
    }



    public List<Reservation> getAllReservationsList(Connection connection)
            throws SQLException{
        String sql = """
                SELECT
                    RESERVATION_ID,
                    BOOKING_REFERENCE,
                    RESERVATION_DATE,
                    STATUS,
                    TOTAL_AMOUNT,
                    CREATED_BY
                FROM RESERVATION
                """;
        List<Reservation> reservaitons = new ArrayList<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()) {
                    reservaitons.add(mapResultSetToReservation(resultSet)
                    );
                }
                return reservaitons;
            }
        }
    }


    private Reservation mapResultSetToReservation(ResultSet resultSet) throws SQLException {
        return new Reservation(
                resultSet.getLong("RESERVATION_ID"),
                resultSet.getString("BOOKING_REFERENCE"),
                (resultSet.getTimestamp("RESERVATION_DATE")).toLocalDateTime(),
                resultSet.getString("STATUS"),
                resultSet.getBigDecimal("TOTAL_AMOUNT"),
                resultSet.getLong("CREATED_BY")
        );
    }

}
