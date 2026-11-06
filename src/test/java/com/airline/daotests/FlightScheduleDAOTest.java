package com.airline.daotests;

import com.airline.dao.FlightDAO;
import com.airline.dao.FlightScheduleDAO;
import com.airline.model.FlightSchedule;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FlightScheduleDAOTest {
    FlightScheduleDAO flightScheduleDAO;
    FlightSchedule flightSchedule;
    Connection connection;


    @BeforeEach
    void setUp() throws SQLException{
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        flightScheduleDAO = new FlightScheduleDAO();

        flightSchedule = new FlightSchedule(
                "F0200Z",
                8L,
                "0001110",
                LocalDate.of(2026, 03,01),
                LocalDate.of(2026, 04, 23),
                "ACTIVE",
                LocalTime.of(8, 30),
                LocalTime.of(11, 30)
        );
    }

    @AfterEach
    void tearDown() throws SQLException{
        if (connection != null){
            try {
                connection.rollback();
            }finally {
                connection.close();
            }
        }
    }


    @Test
    void testAddFlightSchedule()throws Exception{
        long scheduleId = flightScheduleDAO.insertFlightSchedule(connection, flightSchedule);
        assertTrue(scheduleId > 0, "Schedule not inserted");
        FlightSchedule actual = flightScheduleDAO.getScheduleById(connection, scheduleId);


        assertEquals(scheduleId, actual.getShceduleId());
        assertEquals(flightSchedule.getFlightNumber(), actual.getFlightNumber());
        assertEquals(flightSchedule.getRouteId(), actual.getRouteId());
        assertEquals(flightSchedule.getDaysOfOperation(), actual.getDaysOfOperation());
        assertEquals(flightSchedule.getStartDate(), actual.getStartDate());
        assertEquals(flightSchedule.getEndDate(), actual.getEndDate());
        assertEquals(flightSchedule.getScheduleStatus(), actual.getScheduleStatus());
        assertEquals(flightSchedule.getDepartureTime(), actual.getDepartureTime());
        assertEquals(flightSchedule.getArrivalTime(), actual.getArrivalTime());


    }

    @Test
    void testDeleteFlightSchedule() throws Exception{
        long scheduleId = flightScheduleDAO.insertFlightSchedule(connection, flightSchedule);
        assertTrue(scheduleId > 0, "Schedule not inserted");
        FlightSchedule actual = flightScheduleDAO.getScheduleById(connection, scheduleId);
        assertNotNull(actual);

        boolean deleted = flightScheduleDAO.deleteFlightScheduleById(connection, scheduleId);
        actual = flightScheduleDAO.getScheduleById(connection, scheduleId);
        assertNull(actual);
    }

    @Test
    void testGetAllSchedules() throws Exception{
        Map<Long, FlightSchedule> schedules =
                flightScheduleDAO.getAllFlightSchedulesMap(connection);
        assertFalse(schedules.isEmpty(), "No schedules returned");
        assertEquals(7, schedules.size());
        List<FlightSchedule> schedules1 =
                flightScheduleDAO.getAllFlightSchedulesList(connection);
        assertFalse(schedules1.isEmpty(), "No schedules returned");
        assertEquals(7, schedules1.size());
    }
}
