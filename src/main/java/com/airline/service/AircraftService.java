
package com.airline.service;

import com.airline.dao.AircraftDAO;
import com.airline.model.Aircraft;
import com.airline.model.Employee;
import com.airline.util.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class AircraftService {

    private final AircraftDAO aircraftDAO;
    private final ConnectionProvider connectionProvider;
    private final AuthorizationService authorizationService;

    public AircraftService(
            AircraftDAO aircraftDAO,
            ConnectionProvider connectionProvider,
            AuthorizationService authorizationService
    ) {
        this.aircraftDAO = aircraftDAO;
        this.connectionProvider = connectionProvider;
        this.authorizationService = authorizationService;
    }

    public long addAircraft(Aircraft aircraft, Employee employee)
            throws IllegalAccessException, SQLException {

        if (!authorizationService.canAddAircraft(employee)) {
            throw new IllegalAccessException("Only admins can add aircraft.");
        }

        validateAircraft(aircraft);

        try (Connection connection = connectionProvider.getConnection()) {
            return aircraftDAO.insertAircraft(connection, aircraft);
        }
    }

    public boolean updateAircraft(
            Aircraft aircraft,
            Employee employee,
            long aircraftId
    ) throws SQLException, IllegalAccessException {

        if (!authorizationService.canUpdateAircraft(employee)) {
            throw new IllegalAccessException("Only admins can update aircraft.");
        }

        if (aircraftId < 1) {
            throw new IllegalArgumentException("Invalid aircraft ID.");
        }

        validateAircraft(aircraft);

        try (Connection connection = connectionProvider.getConnection()) {

            if (!checkAircraftExists(connection, aircraftId)) {
                throw new IllegalArgumentException(
                        "Aircraft with ID: " + aircraftId + " does not exist."
                );
            }

            return aircraftDAO.updateAircraft(connection, aircraft, aircraftId);
        }
    }



    public Aircraft getAircraft(Employee employee, long aircraftId)
            throws SQLException, IllegalAccessException {
        if(!authorizationService.canSeeAircraft(employee)){
            throw new IllegalAccessException("Only admins can see aircraft.");
        }
        try (Connection connection = connectionProvider.getConnection()) {

            if (!checkAircraftExists(connection, aircraftId)) {
                throw new IllegalArgumentException(
                        "Aircraft with ID: " + aircraftId + " does not exist."
                );
            }

            return aircraftDAO.getAircraftById(connection, aircraftId);
        }
    }

    public List<Aircraft> getAllAircrafts(Employee employee, long aircraftId)
            throws SQLException, IllegalAccessException {
        if(!authorizationService.canSeeAircraft(employee)){
            throw new IllegalAccessException("Only admins can see aircraft.");
        }
        try (Connection connection = connectionProvider.getConnection()) {
            return aircraftDAO.getAllAircraftsList(connection);
        }
    }


    private boolean checkAircraftExists(
            Connection connection,
            long aircraftId
    ) throws SQLException {

        Aircraft existingAircraft =
                aircraftDAO.getAircraftById(connection, aircraftId);

        return existingAircraft != null;
    }

    private void validateAircraft(Aircraft aircraft) {

        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (aircraft.getAircraftModel() == null ||
                aircraft.getAircraftModel().isBlank()) {
            throw new IllegalArgumentException("Aircraft model is required.");
        }

        if (aircraft.getRegistrationNumber() == null ||
                aircraft.getRegistrationNumber().isBlank()) {
            throw new IllegalArgumentException("Registration number is required.");
        }

        if (aircraft.getManufacturer() == null ||
                aircraft.getManufacturer().isBlank()) {
            throw new IllegalArgumentException("Manufacturer is required.");
        }

        if (aircraft.getCapacity() < 1) {
            throw new IllegalArgumentException("Aircraft capacity must be positive.");
        }

        if (aircraft.getStatus() == null ||
                aircraft.getStatus().isBlank()) {
            throw new IllegalArgumentException("Aircraft status is required.");
        }
        if(!aircraft.getStatus().equals("ACTIVE") &&
                !aircraft.getStatus().equals("MAINTENANCE") &&
                !aircraft.getStatus().equals("RETIRED")){
            throw new IllegalArgumentException("Aircraft status" +
                    " must be in (ACTIVE, MAINTENANCE, RETIRED).");
        }
    }
}
