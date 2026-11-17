package com.airline.service;

import com.airline.dao.PassengerDAO;
import com.airline.model.Employee;
import com.airline.model.Passenger;
import com.airline.util.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;


public class PassengerService {
    PassengerDAO passengerDAO;

    AuthorizationService authorizationService;
    ConnectionProvider connectionProvider;
    public PassengerService(ConnectionProvider connectionProvider, PassengerDAO passengerDAO)
    throws SQLException {
        this.connectionProvider = connectionProvider;
        this.passengerDAO = passengerDAO;
        authorizationService = new AuthorizationService();
    }

    public long addPassenger(Passenger passenger, long createdBy)
            throws Exception {
        try (Connection connection = connectionProvider.getConnection()){
            if(createdBy > 0){
                passengerDAO.addPassenger(connection, passenger, createdBy);
            }else {
                throw new Exception("ID must be greater than zero");
            }
            return passenger.getPassengerId();
        }
    }

    public long addPassengerByEmployee(Passenger passenger, Employee employee)
            throws SQLException {
        try(Connection connection = connectionProvider.getConnection()){
            if(!authorizationService.canAddPassenger(employee)){
                throw new SecurityException("This employee cannot add passenger info");
            }
            passengerDAO.addPassenger(connection, passenger, 0L);
            return passenger.getPassengerId();
        }
    }


    //this assumes that createdBy is supplied by the session manager, or it's a staff member
    public void updatePassenger (Passenger passenger, long createdBy) throws SQLException {
        try(Connection connection = connectionProvider.getConnection()){
            if(createdBy < 0){
                throw new IllegalArgumentException("Passenger account cannot have negative id");
            }
            if(createdBy > 0){
                if(!AccountOwnsPassenger(connection, passenger, createdBy)){
                    throw new SecurityException("This account does not own the passenger");
                }
            }
            passengerDAO.updatePassengerById(connection, passenger);
        }
    }

    public void updatePassengerByEmployee (Passenger passenger, Employee employee) throws SQLException, IllegalAccessException {
        try(Connection connection = connectionProvider.getConnection()){
            if(!authorizationService.canUpdatePassenger(employee)){
                throw new SecurityException("This employee cannot add passenger info");
            }
            passengerDAO.updatePassengerById(connection, passenger);
        }
    }

    private boolean AccountOwnsPassenger(Connection con, Passenger passenger, long createdBy)
    throws SQLException {
        Passenger desired =
                passengerDAO.getPassengerById(con, passenger.getPassengerId());
        if(desired != null && desired.getCreatedBy() == createdBy) return true;
        return false;
    }
}
