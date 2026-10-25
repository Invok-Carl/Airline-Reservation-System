package com.airline.service;

import com.airline.dao.SeatDAO;
import com.airline.model.Seat;
import com.airline.util.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SeatService {
    private final SeatDAO seatDAO;
    ConnectionProvider connectionProvider;

    public SeatService(ConnectionProvider connectionProvider, SeatDAO seatDAO){
        this.seatDAO = seatDAO;
        this.connectionProvider = connectionProvider;
    }

    public List<Seat> getAvailableSeatsListByFlightId(long flightId) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        List<Seat> seats = seatDAO.getAvailableSeatsListByFlight(connection, flightId);
        return seats;
    }
    public Map<Long, Seat> getAvailableSeatsMapByFlightId(long flightId) throws SQLException {
        Connection connection = connectionProvider.getConnection();
        Map<Long, Seat> seats = seatDAO.getAvailableSeatsMapByFlight(connection, flightId);
        return seats;
    }

    public List<Seat> getSeatsListByFLightId(long flightId) throws SQLException{
        Connection connection = connectionProvider.getConnection();
        List<Seat> seats = seatDAO.getSeatsListByFlight(connection, flightId);
        return seats;
    }
}
