package com.airline.dao;

import com.airline.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
            statement.setString(3, seat.getSeat_class());
            statement.setString(4, seat.getSeat_type());
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

    public Map<Long, Seat> getSeatsByFlight(Connection connection, long flight_id) throws SQLException {
        String sql = """
                SELECT 
                s.SEAT_ID,
                s.AIRCRAFT_ID,
                s.SEAT_NUMBER,
                s.SEAT_CLASS,
                s.SEAT_TYPE
                FROM FLIGHT
                JOIN SEAT s 
                ON FLIGHT.AIRCRAFT_ID = s.AIRCRAFT_ID
                WHERE FLIGHT_ID = ? AND SEAT_ID NOT IN 
                    (SELECT
                         SEAT_ID 
                     FROM BOOKING_SEGMENT JOIN RESERVATION 
                        ON BOOKING_SEGMENT.RESERVATION_ID = RESERVATION.RESERVATION_ID
                     WHERE 
                        FLIGHT_ID = ? AND SEAT_ID IS NOT NULL AND STATUS <> 'CANCELLED')
                """;
        Map<Long, Seat> seats = new HashMap<>();
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, flight_id);
            statement.setLong(2, flight_id);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    seats.put( resultSet.getLong("SEAT_ID") , mapResultSetToSeat(resultSet));
                }
            }
        }
        return seats;
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
