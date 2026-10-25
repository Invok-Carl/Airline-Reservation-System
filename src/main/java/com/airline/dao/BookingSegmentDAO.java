package com.airline.dao;

import com.airline.model.BookingSegment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BookingSegmentDAO {
    public long addBookingSegment(Connection connection, BookingSegment bookingSegment)
    throws SQLException {
        String sql = """
                INSERT INTO BOOKING_SEGMENT(
                    RESERVATION_ID,
                    PASSENGER_ID,
                    FLIGHT_ID,
                    SEAT_ID,
                    FARE_ID
                )
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql,
             new String[] {"SEGMENT_ID"}   )
        ){
            statement.setLong(1, bookingSegment.getReservation_id());
            statement.setLong(2, bookingSegment.getPassenger_id());
            statement.setLong(3, bookingSegment.getFlight_id());
            statement.setLong(4, bookingSegment.getSeat_id());
            statement.setLong(5, bookingSegment.getFare_id());
            long result = statement.executeUpdate();
            if (result == 0) throw new SQLException("Adding Segment did not Affect Any rows");
            try(ResultSet resultSet = statement.getGeneratedKeys()){
                if(resultSet.next()){
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("Booking segment added by no id returned");
        }

    }

    public Map<Long, BookingSegment> getAllBookingSegments(Connection connection) throws SQLException{
        String sql = """
                SELECT 
                    SEGMENT_ID,
                    RESERVATION_ID,
                    PASSENGER_ID,
                    FLIGHT_ID,
                    SEAT_ID,
                    FARE_ID
                FROM BOOKING_SEGMENT
                """;
        Map<Long, BookingSegment> segs = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    segs.put(resultSet.getLong("SEGMENT_ID"),
                                mapResultSetToBookingSegment(resultSet));
                }
                return segs;
            }
        }
    }

    public boolean hasActiveBookingForPassengerAndFlight(
            Connection connection,
            long passengerId,
            long flightId
    ) throws SQLException {

        String sql = """
            SELECT 1
            FROM BOOKING_SEGMENT BS
            JOIN RESERVATION R
                ON R.RESERVATION_ID = BS.RESERVATION_ID
            WHERE BS.PASSENGER_ID = ?
              AND BS.FLIGHT_ID = ?
              AND R.STATUS <> 'CANCELLED'
            FETCH FIRST 1 ROW ONLY
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passengerId);
            statement.setLong(2, flightId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }




    public BookingSegment getBookingSegmentBySegId(Connection connection
            , long segmentId) throws SQLException{
        String sql = """
                SELECT
                    SEGMENT_ID,
                    RESERVATION_ID,
                    PASSENGER_ID,
                    FLIGHT_ID,
                    SEAT_ID,
                    FARE_ID
                FROM BOOKING_SEGMENT
                WHERE SEGMENT_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, segmentId);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()) {
                    return mapResultSetToBookingSegment(resultSet);
                }
                return null;
            }
        }

    }

    public Map<Long, BookingSegment> getBookingSegmentByPassengerId(Connection connection
            , long passengerId) throws SQLException{
        String sql = """
                SELECT
                    SEGMENT_ID,
                    RESERVATION_ID,
                    PASSENGER_ID,
                    FLIGHT_ID,
                    SEAT_ID,
                    FARE_ID
                FROM BOOKING_SEGMENT
                WHERE PASSENGER_ID = ?
                """;
        Map<Long, BookingSegment> segs = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, passengerId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    segs.put(resultSet.getLong("SEGMENT_ID"),
                            mapResultSetToBookingSegment(resultSet));
                }
                return segs;
            }
        }

    }

    public Map<Long, BookingSegment> getBookingSegmentsByReservationId(Connection connection
            , long resrvationId) throws SQLException{
        String sql = """
                SELECT
                    SEGMENT_ID,
                    RESERVATION_ID,
                    PASSENGER_ID,
                    FLIGHT_ID,
                    SEAT_ID,
                    FARE_ID
                FROM BOOKING_SEGMENT
                WHERE RESERVATION_ID = ?
                """;
        Map<Long, BookingSegment> segs = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, resrvationId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    segs.put(resultSet.getLong("SEGMENT_ID"),
                            mapResultSetToBookingSegment(resultSet));
                }
                return segs;
            }
        }

    }


    public Map<Long, BookingSegment> getBookingSegmentsByBookingReference(Connection connection
            , String booking_reference) throws SQLException{
        String sql = """
                SELECT
                    b.SEGMENT_ID,
                    b.RESERVATION_ID,
                    b.PASSENGER_ID,
                    b.FLIGHT_ID,
                    b.SEAT_ID,
                    b.FARE_ID
                FROM BOOKING_SEGMENT b JOIN RESERVATION r
                ON r.RESERVATION_ID = b.RESERVATION_ID
                WHERE r.BOOKING_REFERENCE = ?
                """;
        Map<Long, BookingSegment> segs = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, booking_reference);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    segs.put(resultSet.getLong("SEGMENT_ID"),
                            mapResultSetToBookingSegment(resultSet));
                }
                return segs;
            }
        }

    }

    public boolean removeBookingSegmentBySegId(Connection connection
            , long segmentId) throws SQLException{
        String sql = """
                DELETE FROM
                    BOOKING_SEGMENT
                WHERE
                    SEGMENT_ID = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, segmentId);
            return statement.executeUpdate() > 0;
        }
    }


    public BookingSegment getBookingSegmentByIdAndReservationId(
            Connection connection,
            long segmentId,
            long reservationId
    ) throws SQLException {

        String sql = """
            SELECT *
            FROM BOOKING_SEGMENT
            WHERE SEGMENT_ID = ?
              AND RESERVATION_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, segmentId);
            statement.setLong(2, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToBookingSegment(resultSet);
                }

                return null;
            }
        }
    }
    public int countSegmentsByReservationId(
            Connection connection,
            long reservationId
    ) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM BOOKING_SEGMENT
            WHERE RESERVATION_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }
    public void deleteBookingSegment(
            Connection connection,
            long segmentId,
            long reservationId
    ) throws SQLException {

        String sql = """
            DELETE FROM BOOKING_SEGMENT
            WHERE SEGMENT_ID = ?
              AND RESERVATION_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, segmentId);
            statement.setLong(2, reservationId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted != 1) {
                throw new SQLException(
                        "Could not delete the booking segment."
                );
            }
        }
    }


    private BookingSegment mapResultSetToBookingSegment(ResultSet resultSet)
            throws SQLException{
        return new BookingSegment(
                resultSet.getLong("SEGMENT_ID"),
                resultSet.getLong("RESERVATION_ID"),
                resultSet.getLong("PASSENGER_ID"),
                resultSet.getLong("FLIGHT_ID"),
                resultSet.getLong("SEAT_ID"),
                resultSet.getLong("FARE_ID")
        );
    }
}
