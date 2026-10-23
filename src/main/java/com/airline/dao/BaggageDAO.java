package com.airline.dao;

import com.airline.model.Baggage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaggageDAO {

    public long createBaggage(Connection connection, Baggage baggage)
            throws SQLException {

        String sql = """
                INSERT INTO BAGGAGE (
                    TAG_NUMBER,
                    SEGMENT_ID,
                    WEIGHT_KG,
                    STATUS
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, new String[]{"BAGGAGE_ID"})) {

            statement.setString(1, baggage.getTag_number());
            statement.setLong(2, baggage.getSegment_id());
            statement.setBigDecimal(3, baggage.getWeight_kg());
            statement.setString(4, baggage.getStatus().toUpperCase());

            int result = statement.executeUpdate();

            if (result == 0) {
                throw new SQLException(
                        "Adding baggage did not affect any rows");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }

            throw new SQLException("Baggage added, but no ID returned");
        }
    }

    public Baggage getBaggageById(Connection connection, long baggageId)
            throws SQLException {

        String sql = """
                SELECT
                    BAGGAGE_ID,
                    TAG_NUMBER,
                    SEGMENT_ID,
                    WEIGHT_KG,
                    STATUS
                FROM BAGGAGE
                WHERE BAGGAGE_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, baggageId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToBaggage(resultSet);
                }
                return null;
            }
        }
    }

    public List<Baggage> getBaggageByTicket(Connection connection,
                                            long ticketId)
            throws SQLException {

        String sql = """
                SELECT DISTINCT
                    b.BAGGAGE_ID,
                    b.TAG_NUMBER,
                    b.SEGMENT_ID,
                    b.WEIGHT_KG,
                    b.STATUS
                FROM BAGGAGE b
                JOIN TICKET t
                    ON t.SEGMENT_ID = b.SEGMENT_ID
                WHERE t.TICKET_ID = ?
                ORDER BY b.BAGGAGE_ID
                """;

        List<Baggage> baggageList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    baggageList.add(mapResultSetToBaggage(resultSet));
                }
            }
        }

        return baggageList;
    }

    public List<Baggage> getBaggageByPassenger(Connection connection,
                                               long passengerId)
            throws SQLException {

        String sql = """
                SELECT DISTINCT
                    b.BAGGAGE_ID,
                    b.TAG_NUMBER,
                    b.SEGMENT_ID,
                    b.WEIGHT_KG,
                    b.STATUS
                FROM BAGGAGE b
                JOIN BOOKING_SEGMENT s
                    ON s.SEGMENT_ID = b.SEGMENT_ID
                WHERE s.PASSENGER_ID = ?
                ORDER BY b.BAGGAGE_ID
                """;

        List<Baggage> baggageList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, passengerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    baggageList.add(mapResultSetToBaggage(resultSet));
                }
            }
        }

        return baggageList;
    }

    public boolean updateBaggageStatus(Connection connection,
                                       long baggageId,
                                       String status)
            throws SQLException {

        String sql = """
                UPDATE BAGGAGE
                SET STATUS = ?
                WHERE BAGGAGE_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toUpperCase());
            statement.setLong(2, baggageId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteBaggage(Connection connection, long baggageId)
            throws SQLException {

        String sql = """
                DELETE FROM BAGGAGE
                WHERE BAGGAGE_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, baggageId);

            return statement.executeUpdate() > 0;
        }
    }

    private Baggage mapResultSetToBaggage(ResultSet resultSet)
            throws SQLException {

        return new Baggage(
                resultSet.getLong("BAGGAGE_ID"),
                resultSet.getString("TAG_NUMBER"),
                resultSet.getLong("SEGMENT_ID"),
                resultSet.getBigDecimal("WEIGHT_KG"),
                resultSet.getString("STATUS")
        );
    }
}