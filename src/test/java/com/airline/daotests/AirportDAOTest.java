package com.airline.daotests;

import com.airline.dao.AirportDAO;
import com.airline.model.Airport;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AirportDAOTest {

    private Connection connection;
    private AirportDAO airportDAO;
    private Airport testAirport;

    private String testCode;
    private String testName;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);

        airportDAO = new AirportDAO();

        // AIRPORT_CODE allows a maximum of 4 characters.
        testCode = "T" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 3)
                .toUpperCase();

        testName = "Test Airport " + testCode;

        testAirport = new Airport(
                testCode,
                testName,
                "Test City",
                "Test Country",
                "Asia/Kabul"
        );
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            try {
                // Undo test data without affecting committed records.
                connection.rollback();
            } finally {
                connection.close();
            }
        }
    }

    // Helper: create an airport and retrieve its generated ID.
    private Airport createAndFindAirport() throws SQLException {
        assertTrue(airportDAO.createAirport(connection, testAirport));

        List<Airport> results =
                airportDAO.searchAirports(connection, testCode);

        return results.stream()
                .filter(a -> testCode.equals(a.getAirportCode()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Created airport was not found"));
    }

    @Test
    void testCreateAirport() throws SQLException {
        assertTrue(
                airportDAO.createAirport(connection, testAirport)
        );

        List<Airport> results =
                airportDAO.searchAirports(connection, testCode);

        assertTrue(results.stream().anyMatch(
                a -> testCode.equals(a.getAirportCode())
        ));
    }

    @Test
    void testGetAirportById() throws SQLException {
        Airport created = createAndFindAirport();

        Airport found = airportDAO.getAirportById(
                connection,
                created.getAirportId()
        );

        assertNotNull(found);
        assertEquals(created.getAirportId(), found.getAirportId());
        assertEquals(testCode, found.getAirportCode());
        assertEquals(testName, found.getAirportName());
        assertEquals("Test City", found.getCity());
        assertEquals("Test Country", found.getCountry());
        assertEquals("Asia/Kabul", found.getAirportTimeZone());
    }

    @Test
    void testGetAirportByIdWhenNotFound() throws SQLException {
        Airport found = airportDAO.getAirportById(
                connection,
                -999999L
        );

        assertNull(found);
    }

    @Test
    void testGetAllAirports() throws SQLException {
        Airport created = createAndFindAirport();

        List<Airport> airports =
                airportDAO.getAllAirports(connection);

        assertNotNull(airports);
        assertFalse(airports.isEmpty());

        assertTrue(airports.stream().anyMatch(
                a -> a.getAirportId() == created.getAirportId()
        ));
    }

    @Test
    void testSearchAirportsByCode() throws SQLException {
        createAndFindAirport();

        List<Airport> results =
                airportDAO.searchAirports(connection, testCode);

        assertTrue(results.stream().anyMatch(
                a -> testCode.equals(a.getAirportCode())
        ));
    }

    @Test
    void testSearchAirportsByName() throws SQLException {
        createAndFindAirport();

        List<Airport> results =
                airportDAO.searchAirports(connection, testName);

        assertTrue(results.stream().anyMatch(
                a -> testName.equals(a.getAirportName())
        ));
    }

    @Test
    void testSearchAirportsByCity() throws SQLException {
        createAndFindAirport();

        List<Airport> results =
                airportDAO.searchAirports(connection, "Test City");

        assertTrue(results.stream().anyMatch(
                a -> testCode.equals(a.getAirportCode())
        ));
    }

    @Test
    void testSearchAirportsIsCaseInsensitive() throws SQLException {
        createAndFindAirport();

        List<Airport> results =
                airportDAO.searchAirports(
                        connection,
                        testCode.toLowerCase()
                );

        assertTrue(results.stream().anyMatch(
                a -> testCode.equals(a.getAirportCode())
        ));
    }

    @Test
    void testUpdateAirport() throws SQLException {
        Airport created = createAndFindAirport();

        Airport updatedAirport = new Airport(
                created.getAirportId(),
                testCode,
                "Updated Airport " + testCode,
                "Updated City",
                "Updated Country",
                "UTC"
        );

        assertTrue(
                airportDAO.updateAirport(connection, updatedAirport)
        );

        Airport found = airportDAO.getAirportById(
                connection,
                created.getAirportId()
        );

        assertNotNull(found);
        assertEquals(
                "Updated Airport " + testCode,
                found.getAirportName()
        );
        assertEquals("Updated City", found.getCity());
        assertEquals("Updated Country", found.getCountry());
        assertEquals("UTC", found.getAirportTimeZone());
    }

    @Test
    void testUpdateAirportWhenNotFound() throws SQLException {
        Airport nonexistent = new Airport(
                -999999L,
                testCode,
                "Nonexistent Airport",
                "Test City",
                "Test Country",
                "UTC"
        );

        assertFalse(
                airportDAO.updateAirport(connection, nonexistent)
        );
    }

    @Test
    void testDeleteAirport() throws SQLException {
        Airport created = createAndFindAirport();

        assertTrue(
                airportDAO.deleteAirport(
                        connection,
                        created.getAirportId()
                )
        );

        assertNull(
                airportDAO.getAirportById(
                        connection,
                        created.getAirportId()
                )
        );
    }

    @Test
    void testDeleteAirportWhenNotFound() throws SQLException {
        assertFalse(
                airportDAO.deleteAirport(connection, -999999L)
        );
    }
}