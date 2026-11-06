package com.airline.dao;

import com.airline.model.CheckIn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckInDAO {

    public long createCheckIn(Connection connection, CheckIn checkIn)
            throws SQLException {

        String sql = """
                INSERT INTO CHECK_IN (
                    SEGMENT_ID,
                    CHECKIN_TIME,
                    SEAT_ID,
                    STATUS,
                    EMPLOYEE_ID
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, new String[]{"CHECKIN_ID"})) {

            statement.setLong(1, checkIn.getSegmentId());
            statement.setTimestamp(
                    2, Timestamp.valueOf(checkIn.getCheckinTime()));

            if (checkIn.getSeatId() == null) {
                statement.setNull(3, Types.NUMERIC);
            } else {
                statement.setLong(3, checkIn.getSeatId());
            }

            statement.setString(4, checkIn.getStatus().toUpperCase());
            statement.setLong(5, checkIn.getEmployeeId());

            long result = statement.executeUpdate();

            if (result == 0) {
                throw new SQLException(
                        "Adding check-in did not affect any rows");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }

            throw new SQLException("Check-in added, but no ID returned");
        }
    }

    public CheckIn getCheckInById(Connection connection, long checkInId)
            throws SQLException {

        String sql = """
                SELECT
                    CHECKIN_ID,
                    SEGMENT_ID,
                    CHECKIN_TIME,
                    SEAT_ID,
                    STATUS,
                    EMPLOYEE_ID
                FROM CHECK_IN
                WHERE CHECKIN_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, checkInId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCheckIn(resultSet);
                }
                return null;
            }
        }
    }

    public List<CheckIn> getCheckInsByTicket(Connection connection,
                                             long ticketId)
            throws SQLException {

        String sql = """
                SELECT DISTINCT
                    c.CHECKIN_ID,
                    c.SEGMENT_ID,
                    c.CHECKIN_TIME,
                    c.SEAT_ID,
                    c.STATUS,
                    c.EMPLOYEE_ID
                FROM CHECK_IN c
                JOIN TICKET t
                    ON t.SEGMENT_ID = c.SEGMENT_ID
                WHERE t.TICKET_ID = ?
                ORDER BY c.CHECKIN_TIME DESC
                """;

        List<CheckIn> checkIns = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    checkIns.add(mapResultSetToCheckIn(resultSet));
                }
            }
        }

        return checkIns;
    }

    public List<CheckIn> getCheckInsByFlight(Connection connection,
                                             long flightId)
            throws SQLException {

        String sql = """
                SELECT
                    c.CHECKIN_ID,
                    c.SEGMENT_ID,
                    c.CHECKIN_TIME,
                    c.SEAT_ID,
                    c.STATUS,
                    c.EMPLOYEE_ID
                FROM CHECK_IN c
                JOIN BOOKING_SEGMENT s
                    ON s.SEGMENT_ID = c.SEGMENT_ID
                WHERE s.FLIGHT_ID = ?
                ORDER BY c.CHECKIN_TIME DESC
                """;

        List<CheckIn> checkIns = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, flightId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    checkIns.add(mapResultSetToCheckIn(resultSet));
                }
            }
        }

        return checkIns;
    }

    public boolean updateCheckInStatus(Connection connection,
                                       long checkInId,
                                       String status)
            throws SQLException {

        String sql = """
                UPDATE CHECK_IN
                SET STATUS = ?
                WHERE CHECKIN_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toUpperCase());
            statement.setLong(2, checkInId);

            return statement.executeUpdate() > 0;
        }
    }

    private CheckIn mapResultSetToCheckIn(ResultSet resultSet)
            throws SQLException {

        Long seatId = resultSet.getObject("SEAT_ID", Long.class);

        return new CheckIn(
                resultSet.getLong("CHECKIN_ID"),
                resultSet.getLong("SEGMENT_ID"),
                resultSet.getTimestamp("CHECKIN_TIME").toLocalDateTime(),
                seatId,
                resultSet.getString("STATUS"),
                resultSet.getLong("EMPLOYEE_ID")
        );
    }
}