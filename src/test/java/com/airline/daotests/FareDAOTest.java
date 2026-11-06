package com.airline.daotests;

import com.airline.dao.FareDAO;
import com.airline.model.Fare;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static oracle.sql.DatumWithConnection.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

public class FareDAOTest {
    Fare fare;
    FareDAO fareDAO;
    Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        fareDAO = new FareDAO();
        fare = new Fare(
                7, "BUSINESS", new BigDecimal("120"),
                new BigDecimal("20"), new BigDecimal("140"),
                "USD");
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
    }


    @AfterEach
    void tearDown() throws Exception {
        if(connection != null){
            try{
                connection.rollback();
            }finally {
                connection.close();
            }
        }
    }


    @Test
    void testAddFare() throws Exception {
        System.out.println(fare.getTotalFare());
        long fareId = fareDAO.InsertFare(connection, fare);
        Fare actual = fareDAO.getFareByFareId(connection, fareId);
        assertTrue(fareId > 0);
        Assertions.assertNotNull(actual);
        assertEquals(fareId, actual.getFareId());
        assertEquals(fare.getRouteId(), actual.getRouteId());
        assertEquals(fare.getFareClass(), actual.getFareClass());
        assertEquals(fare.getBaseFare(), actual.getBaseFare());
        assertEquals(fare.getTax(), actual.getTax());
        assertEquals(0, (new BigDecimal("140"))
                        .compareTo(actual.getTotalFare()),
                "Fare amount does not match numerically");
        assertEquals(fare.getCurrency(), actual.getCurrency());
    }
    @Test
    void TestDeleteFare() throws Exception {
        long fareId = fareDAO.InsertFare(connection, fare);
        Fare actual = fareDAO.getFareByFareId(connection, fareId);
        assertTrue(fareId > 0);
        Assertions.assertNotNull(actual);
        assertEquals(fareId, actual.getFareId());
        assertEquals(fare.getRouteId(), actual.getRouteId());
        assertEquals(fare.getFareClass(), actual.getFareClass());
        assertEquals(fare.getBaseFare(), actual.getBaseFare());
        assertEquals(fare.getTax(), actual.getTax());
        assertEquals(0, (new BigDecimal("140"))
                        .compareTo(actual.getTotalFare()),
                "Fare amount does not match numerically");
        assertEquals(fare.getCurrency(), actual.getCurrency());

        boolean deleted = fareDAO.DeleteFare(connection, fareId);
        assertTrue(deleted);
        Fare newFare = fareDAO.getFareByFareId(connection, fareId);
        assertNull(newFare);
    }

    @Test
    void getFareByRouteAndClass() throws Exception {
        long fareId = fareDAO.InsertFare(connection, fare);

        Fare actual = fareDAO.getFareByRouteIdAndFareClass(
                connection, 7, "BUSINESS");

        Assertions.assertNotNull(actual);
        assertEquals(fareId, actual.getFareId());
        assertEquals(fare.getRouteId(), actual.getRouteId());
        assertEquals(fare.getFareClass(), actual.getFareClass());
        assertEquals(fare.getBaseFare(), actual.getBaseFare());
        assertEquals(fare.getTax(), actual.getTax());
        assertEquals(0, (new BigDecimal("140"))
                        .compareTo(actual.getTotalFare()),
                "Fare amount does not match numerically");
        assertEquals(fare.getCurrency(), actual.getCurrency());


    }
    @Test
    void testGetAllFares() throws Exception {
        long fareId = fareDAO.InsertFare(connection, fare);
        Fare actual = fareDAO.getFareByFareId(connection, fareId);
        Map<Long, Fare> fares = fareDAO.getAllFaresMap(connection);
        assertFalse(fares.isEmpty());
        assertTrue(fares.size() == 11, "not enough fares fetched");

        Fare fare = fares.get(fareId);
        assertEquals(fareId, actual.getFareId());
        assertEquals(fare.getRouteId(), actual.getRouteId());
        assertEquals(fare.getFareClass(), actual.getFareClass());
        assertEquals(fare.getBaseFare(), actual.getBaseFare());
        assertEquals(fare.getTax(), actual.getTax());
        assertEquals(0, (new BigDecimal("140"))
                        .compareTo(actual.getTotalFare()),
                "Fare amount does not match numerically");
        assertEquals(fare.getCurrency(), actual.getCurrency());

        List<Fare> fares2 = fareDAO.getAllFaresList(connection);
        assertFalse(fares2.isEmpty());
        assertTrue(fares2.size() == 11, "not enough fares fetched");

    }

    @Test
    void testGetFareByFlightIdandSeatId() throws Exception{
        Fare actual = fareDAO.getFareByFlightIdAndSeatId(connection,
                1, 1623L);
        assertEquals(2, actual.getFareId());
    }
}
