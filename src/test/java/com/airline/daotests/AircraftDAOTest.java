package com.airline.daotests;

import com.airline.dao.AircraftDAO;
import com.airline.model.Aircraft;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftDAOTest {
    Aircraft aircraft;
    AircraftDAO aircraftDAO;
    Connection connection;

    @BeforeEach
    void setUp() throws Exception{
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        aircraftDAO = new AircraftDAO();
        aircraft = new Aircraft(
                "N110AA",
                "737-800",
                "Boeing",
                162,
                "ACTIVE"
        );
    }
    @AfterEach
    void tearDown() throws Exception{
        if(connection != null){
            try{
                connection.rollback();
            }finally {
                connection.close();
            }
        }
    }

    @Test
    void testInsertAircraft() throws Exception{
        long aircraftId = aircraftDAO.insertAircraft(connection, aircraft);
        Aircraft actual = aircraftDAO.getAircraftById(connection, aircraftId);

        assertTrue(aircraftId > 0, "Aircraft ID not fetched");
        assertNotNull(actual);

        assertEquals(aircraftId, actual.getAircraftId());
        assertEquals(aircraft.getRegistrationNumber(),
                actual.getRegistrationNumber());
        assertEquals(aircraft.getAircraftModel(), actual.getAircraftModel());
        assertEquals(aircraft.getManufacturer(), actual.getManufacturer());
        assertEquals(aircraft.getCapacity(), actual.getCapacity());
        assertEquals(aircraft.getStatus(), actual.getStatus());
    }

    @Test
    void testDeleteAircraft() throws Exception{
        long aircraftId = aircraftDAO.insertAircraft(connection, aircraft);
        Aircraft actual = aircraftDAO.getAircraftById(connection, aircraftId);

        assertTrue(aircraftId > 0, "Aircraft ID not fetched");
        assertNotNull(actual);

        boolean delete = aircraftDAO.deleteAircraft(connection, aircraftId);
        assertTrue(delete, "Flight not deleted");
        Aircraft deleted = aircraftDAO.getAircraftById(connection, aircraftId);
        assertNull(deleted);

    }

    @Test
    void testGetAllAircrafts() throws Exception{
        long aircraftId = aircraftDAO.insertAircraft(connection, aircraft);

        assertTrue(aircraftId > 0, "Aircraft ID not fetched");

        Map<Long, Aircraft> aircrafts = aircraftDAO.getAllAircraftsMap(connection);
        List<Aircraft> aircrafts2 = aircraftDAO.getAllAircraftsList(connection);

        assertEquals(10, aircrafts.size());
        assertEquals(10, aircrafts2.size());

        Aircraft actual = aircrafts.get(aircraftId);
        assertNotNull(actual);

        assertEquals(aircraftId, actual.getAircraftId());
        assertEquals(aircraft.getRegistrationNumber(),
                actual.getRegistrationNumber());
        assertEquals(aircraft.getAircraftModel(), actual.getAircraftModel());
        assertEquals(aircraft.getManufacturer(), actual.getManufacturer());
        assertEquals(aircraft.getCapacity(), actual.getCapacity());
        assertEquals(aircraft.getStatus(), actual.getStatus());
    }

    @Test
    void testUpdateAircraft() throws Exception {
        long aircraftId = aircraftDAO.insertAircraft(connection, aircraft);
        assertTrue(aircraftId > 0, "Aircraft ID not fetched");

        aircraft.setRegistrationNumber("NUMBER");
        aircraft.setAircraftModel("NEW_AIRCRAFT_MODEL");
        aircraft.setCapacity(220);
        aircraft.setStatus("MAINTENANCE");
        aircraft.setManufacturer("NEW_MANUFACTURER");

        boolean hasUpdated = aircraftDAO.updateAircraft(connection, aircraft, aircraftId);
        assertTrue(hasUpdated, "The aircraft did not update successfully");

        Aircraft actual = aircraftDAO.getAircraftById(connection, aircraftId);
        assertNotNull(actual);

        assertEquals(aircraft.getRegistrationNumber(), actual.getRegistrationNumber());
        assertEquals(aircraft.getAircraftModel(), actual.getAircraftModel());
        assertEquals(aircraft.getCapacity(), actual.getCapacity());
        assertEquals(aircraft.getStatus(), actual.getStatus());
        assertEquals(aircraft.getManufacturer(), actual.getManufacturer());
    }


}
