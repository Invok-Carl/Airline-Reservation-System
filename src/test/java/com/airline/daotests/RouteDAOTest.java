package com.airline.daotests;
import com.airline.dao.AirportDAO;
import com.airline.dao.RouteDAO;
import com.airline.model.Airport;
import com.airline.model.Route;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RouteDAOTest {

    private Connection connection;
    private RouteDAO routeDAO;

    private long originAirportId;
    private long destinationAirportId;

    private String originCode;
    private String destinationCode;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);

        routeDAO = new RouteDAO();

        // Unique airport codes, limited to 4 characters.
        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 3)
                .toUpperCase();

        originCode = "O" + suffix.substring(0, 3);
        destinationCode = "D" + suffix.substring(0, 3);

        AirportDAO airportDAO = new AirportDAO();

        Airport origin = new Airport(
                originCode,
                "Test Origin Airport " + suffix,
                "Origin City",
                "Test Country",
                "Asia/Kabul"
        );

        Airport destination = new Airport(
                destinationCode,
                "Test Destination Airport " + suffix,
                "Destination City",
                "Test Country",
                "Asia/Kabul"
        );

        assertTrue(airportDAO.createAirport(connection, origin));
        assertTrue(airportDAO.createAirport(connection, destination));

        // Find the generated airport IDs.
        originAirportId = airportDAO
                .searchAirports(connection, originCode)
                .stream()
                .filter(a -> originCode.equals(a.getAirportCode()))
                .mapToLong(Airport::getAirportId)
                .findFirst()
                .orElseThrow();

        destinationAirportId = airportDAO
                .searchAirports(connection, destinationCode)
                .stream()
                .filter(a -> destinationCode.equals(a.getAirportCode()))
                .mapToLong(Airport::getAirportId)
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            try {
                connection.rollback();
            } finally {
                connection.close();
            }
        }
    }

    // Helper method to create a test route.
    private Route createTestRoute() throws SQLException {
        Route route = new Route(
                originAirportId,
                destinationAirportId,
                174.5,
                45,
                "ACTIVE"
        );

        assertTrue(routeDAO.createRoute(connection, route));

        // Retrieve the route using its airport relationship.
        return routeDAO.getRoutesByOrigin(
                        connection, originAirportId)
                .stream()
                .filter(r ->
                        r.getDestAirportId() == destinationAirportId)
                .filter(r ->
                        Math.abs(r.getDistanceKm() - 174.5) < 0.001)
                .filter(r -> r.getEstDurationMin() == 45)
                .filter(r -> "ACTIVE".equals(r.getRouteStatus()))
                .reduce((first, second) -> second)
                .orElseThrow(() ->
                        new AssertionError("Created route was not found"));
    }

    @Test
    void testCreateRoute() throws SQLException {
        Route route = new Route(
                originAirportId,
                destinationAirportId,
                174.5,
                45,
                "ACTIVE"
        );

        assertTrue(routeDAO.createRoute(connection, route));

        List<Route> routes =
                routeDAO.getRoutesByOrigin(connection, originAirportId);

        assertTrue(routes.stream().anyMatch(r ->
                r.getDestAirportId() == destinationAirportId
                        && Math.abs(r.getDistanceKm() - 174.5) < 0.001
                        && r.getEstDurationMin() == 45
                        && "ACTIVE".equals(r.getRouteStatus())
        ));
    }

    @Test
    void testGetRouteById() throws SQLException {
        Route created = createTestRoute();

        Route found = routeDAO.getRouteById(
                connection,
                created.getRouteId()
        );

        assertNotNull(found);
        assertEquals(created.getRouteId(), found.getRouteId());
        assertEquals(originAirportId, found.getOriginAirportId());
        assertEquals(destinationAirportId, found.getDestAirportId());
        assertEquals(174.5, found.getDistanceKm(), 0.001);
        assertEquals(45, found.getEstDurationMin());
        assertEquals("ACTIVE", found.getRouteStatus());
    }

    @Test
    void testGetRouteByIdWhenNotFound() throws SQLException {
        Route found = routeDAO.getRouteById(
                connection,
                -999999L
        );

        assertNull(found);
    }

    @Test
    void testGetAllRoutes() throws SQLException {
        Route created = createTestRoute();

        List<Route> routes = routeDAO.getAllRoutes(connection);

        assertNotNull(routes);
        assertTrue(routes.stream().anyMatch(r ->
                r.getRouteId() == created.getRouteId()
        ));
    }

    @Test
    void testGetRoutesByOrigin() throws SQLException {
        Route created = createTestRoute();

        List<Route> routes = routeDAO.getRoutesByOrigin(
                connection,
                originAirportId
        );

        assertTrue(routes.stream().anyMatch(r ->
                r.getRouteId() == created.getRouteId()
        ));

        assertTrue(routes.stream().allMatch(r ->
                r.getOriginAirportId() == originAirportId
        ));
    }

    @Test
    void testGetRoutesByDestination() throws SQLException {
        Route created = createTestRoute();

        List<Route> routes = routeDAO.getRoutesByDestination(
                connection,
                destinationAirportId
        );

        assertTrue(routes.stream().anyMatch(r ->
                r.getRouteId() == created.getRouteId()
        ));

        assertTrue(routes.stream().allMatch(r ->
                r.getDestAirportId() == destinationAirportId
        ));
    }

    @Test
    void testUpdateRoute() throws SQLException {
        Route created = createTestRoute();

        Route updated = new Route(
                created.getRouteId(),
                originAirportId,
                destinationAirportId,
                250.5,
                60,
                "INACTIVE"
        );

        assertTrue(routeDAO.updateRoute(connection, updated));

        Route found = routeDAO.getRouteById(
                connection,
                created.getRouteId()
        );

        assertNotNull(found);
        assertEquals(250.5, found.getDistanceKm(), 0.001);
        assertEquals(60, found.getEstDurationMin());
        assertEquals("INACTIVE", found.getRouteStatus());
    }

    @Test
    void testUpdateRouteWhenNotFound() throws SQLException {
        Route nonexistent = new Route(
                -999999L,
                originAirportId,
                destinationAirportId,
                100.0,
                30,
                "ACTIVE"
        );

        assertFalse(routeDAO.updateRoute(connection, nonexistent));
    }

    @Test
    void testDeleteRoute() throws SQLException {
        Route created = createTestRoute();

        assertTrue(routeDAO.deleteRoute(
                connection,
                created.getRouteId()
        ));

        assertNull(routeDAO.getRouteById(
                connection,
                created.getRouteId()
        ));
    }

    @Test
    void testDeleteRouteWhenNotFound() throws SQLException {
        assertFalse(routeDAO.deleteRoute(
                connection,
                -999999L
        ));
    }
}
