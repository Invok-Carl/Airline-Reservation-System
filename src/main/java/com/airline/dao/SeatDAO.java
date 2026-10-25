package com.airline.dao;

import com.airline.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SeatDAO {
    public long insertSeat(Connection connection, Seat seat) throws SQLException {
        String sql = """
                INSERT INTO SEAT(
                AIRCRAFT_ID,
                SEAT_NUMBER,
                SEAT_CLASS,
                SEAT_TYPE
                ) 
                VALUES 
                (?, ?, ?, ?)
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql,
                new String[] {"SEAT_ID"}
        )
        ){
            statement.setLong(1, seat.getAircraft_id());
            statement.setString(2, seat.getSeat_number());
            statement.setString(3, seat.getSeat_class().toUpperCase());
            statement.setString(4, seat.getSeat_type().toUpperCase());
            int result = statement.executeUpdate();
            if(result == 0) throw new SQLException("No rows affected");
            try(ResultSet resultSet = statement.getGeneratedKeys()){
                if(resultSet.next()){
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("Seat inserted, but no seat_id returned");
        }
    }



    public boolean deleteSeatbyId(Connection connection, long seatId) throws SQLException{
        String sql = """
                DELETE FROM SEAT 
                WHERE SEAT_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, seatId);
            return statement.executeUpdate() > 0;
        }
    }


    public Map<Long, Seat> getSeatsMapByFlight(Connection connection, long flight_id) throws SQLException {
        String sql = """
                SELECT
                    S.SEAT_ID,
                    S.AIRCRAFT_ID,
                    S.SEAT_NUMBER,
                    S.SEAT_CLASS,
                    S.SEAT_TYPE
                FROM FLIGHT F
                JOIN SEAT S ON F.AIRCRAFT_ID = S.AIRCRAFT_ID
                WHERE F.FLIGHT_ID = ?
                """;
        Map<Long, Seat> seats = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, flight_id);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    seats.put( resultSet.getLong("SEAT_ID") , mapResultSetToSeat(resultSet));
                }
            }
        }
        return seats;
    }

    public List<Seat> getSeatsListByFlight(Connection connection, long flight_id) throws SQLException {
        String sql = """
                SELECT
                    S.SEAT_ID,
                    S.AIRCRAFT_ID,
                    S.SEAT_NUMBER,
                    S.SEAT_CLASS,
                    S.SEAT_TYPE
                FROM FLIGHT F
                JOIN SEAT S ON F.AIRCRAFT_ID = S.AIRCRAFT_ID
                WHERE F.FLIGHT_ID = ?
                ORDER BY S.SEAT_ID
                """;
        List<Seat> seats = new ArrayList<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, flight_id);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    seats.add(mapResultSetToSeat(resultSet));
                }
            }
        }
        return seats;
    }



    public Map<Long, Seat> getAvailableSeatsMapByFlight(Connection connection, long flight_id) throws SQLException {
        String sql = """
                SELECT
                    S.SEAT_ID,
                    S.AIRCRAFT_ID,
                    S.SEAT_NUMBER,
                    S.SEAT_CLASS,
                    S.SEAT_TYPE
                FROM FLIGHT F JOIN SEAT S
                    ON F.AIRCRAFT_ID = S.AIRCRAFT_ID
                    AND F.FLIGHT_ID = ?
                    AND NOT EXISTS (
                      SELECT 1
                      FROM BOOKING_SEGMENT BS
                      JOIN RESERVATION R ON BS.RESERVATION_ID = R.RESERVATION_ID
                      WHERE BS.FLIGHT_ID = F.FLIGHT_ID
                        AND BS.SEAT_ID = S.SEAT_ID
                        AND R.STATUS <> ('CANCELLED')
                  )
                """;
        Map<Long, Seat> seats = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, flight_id);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    seats.put( resultSet.getLong("SEAT_ID") , mapResultSetToSeat(resultSet));
                }
            }
        }
        return seats;
    }

    public List<Seat> getAvailableSeatsListByFlight(Connection connection, long flight_id) throws SQLException {
        String sql = """
                SELECT
                    S.SEAT_ID,
                    S.AIRCRAFT_ID,
                    S.SEAT_NUMBER,
                    S.SEAT_CLASS,
                    S.SEAT_TYPE
                FROM FLIGHT F JOIN SEAT S
                    ON F.AIRCRAFT_ID = S.AIRCRAFT_ID
                    AND F.FLIGHT_ID = ?
                    AND NOT EXISTS (
                      SELECT 1
                      FROM BOOKING_SEGMENT BS
                      JOIN RESERVATION R ON BS.RESERVATION_ID = R.RESERVATION_ID
                      WHERE BS.FLIGHT_ID = F.FLIGHT_ID
                        AND BS.SEAT_ID = S.SEAT_ID
                        AND R.STATUS <> ('CANCELLED')
                  )
                ORDER BY SEAT_ID
                """;
        List<Seat> seats = new ArrayList<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, flight_id);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    seats.add(mapResultSetToSeat(resultSet));
                }
            }
        }
        return seats;
    }

    public boolean isSeatAvailable(
            Connection connection,
            long flightId,
            long seatId
    ) throws SQLException {

        String sql = """
            SELECT 1
            FROM FLIGHT F
            JOIN SEAT S
                ON S.AIRCRAFT_ID = F.AIRCRAFT_ID
            WHERE F.FLIGHT_ID = ?
              AND S.SEAT_ID = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM BOOKING_SEGMENT BS
                  JOIN RESERVATION R
                      ON R.RESERVATION_ID = BS.RESERVATION_ID
                  WHERE BS.FLIGHT_ID = F.FLIGHT_ID
                    AND BS.SEAT_ID = S.SEAT_ID
                    AND R.STATUS <> 'CANCELLED'
              )
            FETCH FIRST 1 ROW ONLY
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, flightId);
            statement.setLong(2, seatId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Map<Long, Seat> getAllSeats(Connection connection) throws SQLException {
        String sql = """
                SELECT 
                    SEAT_ID,
                    AIRCRAFT_ID,
                    SEAT_NUMBER,
                    SEAT_CLASS,
                    SEAT_TYPE
                FROM SEAT
                """;
        Map<Long, Seat> seats = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    seats.put(resultSet.getLong("SEAT_ID") , mapResultSetToSeat(resultSet));
                }
            }
        }
        return seats;
    }

    public Seat getSeatById(Connection connection, long seatId) throws SQLException {
        String sql = """
                SELECT 
                    SEAT_ID,
                    AIRCRAFT_ID,
                    SEAT_NUMBER,
                    SEAT_CLASS,
                    SEAT_TYPE
                FROM SEAT
                WHERE SEAT_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, seatId);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return mapResultSetToSeat(resultSet);
                }
            }
        }
        return null;
    }


    public String getSeatClassBySeatId(Connection connection, long seatId
    )throws SQLException {
        Seat seat = getSeatById(connection, seatId);
        return seat.getSeat_class();
    }

    private Seat mapResultSetToSeat(ResultSet resultSet) throws SQLException {
        return new Seat(
                resultSet.getLong("SEAT_ID"),
                resultSet.getLong("AIRCRAFT_ID"),
                resultSet.getString("SEAT_NUMBER"),
                resultSet.getString("SEAT_CLASS"),
                resultSet.getString("SEAT_TYPE")
        );
    }
}
