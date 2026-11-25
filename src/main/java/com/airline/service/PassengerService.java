package com.airline.service;

import com.airline.dao.PassengerDAO;
import com.airline.model.Passenger;
import com.airline.util.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

public class PassengerService {
    PassengerDAO passengerDAO;
    Connection connection;
    public PassengerService(ConnectionProvider connectionProvider, PassengerDAO passengerDAO)
    throws SQLException {
        connection = connectionProvider.getConnection();
        this.passengerDAO = passengerDAO;
    }

    public long addPassenger(Passenger passenger, long createdBy)
    throws SQLException{
        if(createdBy > 0){
            passengerDAO.addPassenger(connection, passenger, createdBy);
        }else {
            passengerDAO.addPassenger(connection, passenger, 0L);
        }
        return passenger.getPassengerId();
    }
    //this assumes that createdBy is supplied by the session manager or it's a staff member
    public void updatePassenger (Passenger passenger, long createdBy) throws SQLException, IllegalAccessException {
        if(createdBy > 0){
            if(!AccountOwnsPassenger(connection, passenger, createdBy)){
                throw new IllegalAccessException("The account does not own the passenger");
            }
        }
        passengerDAO.updatePassengerById(connection, passenger);
    }

    private boolean AccountOwnsPassenger(Connection connection, Passenger passenger, long createdBy)
    throws SQLException {
        Passenger desired =
                passengerDAO.getPassengerById(connection, passenger.getPassengerId());
        if(desired != null && desired.getCreatedBy() == createdBy) return true;
        return false;
    }

    public Passenger getPassengerById(long passengerId) throws SQLException {
        return passengerDAO.getPassengerById(connection, passengerId);
    }
}
