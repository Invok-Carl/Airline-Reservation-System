package com.airline.dao;
import com.airline.model.Flight;
import oracle.jdbc.proxy.annotation.Pre;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightDAO {
    public long addFlight(Connection connection, Flight flight) throws SQLException {
        String sql = """
                INSERT INTO FLIGHT (
                    FLIGHT_DATE,
                    AIRCRAFT_ID,
                    ACTUAL_DEPARTURE,
                    ACTUAL_ARRIVAL,
                    FLIGHT_STATUS,
                    SCHEDULE_ID
                ) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql,
                new String[] {"FLIGHT_ID"}
                )) {

            statement.setDate(1, Date.valueOf(flight.getFlight_date()));
            statement.setLong(2, flight.getAircraft_id());

            if (flight.getActual_departure() == null) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(
                        3, flight.getActual_departure().toString()
                );
            }

            if (flight.getActual_arrival() == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(
                        4, flight.getActual_arrival().toString()
                );
            }

            statement.setString(5, flight.getFlight_status().toUpperCase());
            statement.setLong(6, flight.getSchedule_id());
            int result = statement.executeUpdate();
            if (result == 0) throw new SQLException("Flight insertion didn't affect any rows");
            try(ResultSet resultSet = statement.getGeneratedKeys()){
                if(resultSet.next()){
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("Flight Inserted, but flight_id was not returned");
        }
    }

    public Flight findFlightById(Connection connection, long flightId
    ) throws SQLException {
        String sql = """
                SELECT
                    F.FLIGHT_ID,
                    F.FLIGHT_DATE,
                    DEP.AIRPORT_NAME as DEPARTURE_AIRPORT,
                    ARR.AIRPORT_NAME as ARRIVAL_AIRPORT,
                    F.AIRCRAFT_ID,
                    FS.DEP_TIME,
                    FS.ARR_TIME,
                    F.ACTUAL_DEPARTURE,
                    F.ACTUAL_ARRIVAL,
                    F.FLIGHT_STATUS,
                    F.SCHEDULE_ID
                FROM FLIGHT F join FLIGHT_SCHEDULE FS
                    ON F.SCHEDULE_ID = FS.SCHEDULE_ID
                    JOIN ROUTE R ON R.ROUTE_ID = FS.ROUTE_ID
                    JOIN AIRPORT DEP ON DEP.AIRPORT_ID = R.ORIGIN_AIRPORT_ID
                    JOIN AIRPORT ARR ON ARR.AIRPORT_ID = R.DEST_AIRPORT_ID
                WHERE F.FLIGHT_ID = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, flightId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToFlight(resultSet);
                }
                return null;
            }
        }
    }


    public Map<Long, Flight> findFlightsByPassengerId(Connection connection, long passenger_id
    ) throws SQLException {
        String sql = """
                SELECT
                    F.FLIGHT_ID,
                    F.FLIGHT_DATE,
                    DEP.AIRPORT_NAME AS DEPARTURE_AIRPORT,
                    ARR.AIRPORT_NAME AS ARRIVAL_AIRPORT,
                    F.AIRCRAFT_ID,
                    FS.DEP_TIME,
                    FS.ARR_TIME,
                    F.ACTUAL_DEPARTURE,
                    F.ACTUAL_ARRIVAL,
                    F.FLIGHT_STATUS,
                    F.SCHEDULE_ID
                FROM FLIGHT F
                JOIN FLIGHT_SCHEDULE FS
                    ON F.SCHEDULE_ID = FS.SCHEDULE_ID
                JOIN ROUTE R
                    ON R.ROUTE_ID = FS.ROUTE_ID
                JOIN AIRPORT DEP
                    ON DEP.AIRPORT_ID = R.ORIGIN_AIRPORT_ID
                JOIN AIRPORT ARR
                    ON ARR.AIRPORT_ID = R.DEST_AIRPORT_ID
                WHERE EXISTS (
                    SELECT 1
                    FROM BOOKING_SEGMENT BS
                    JOIN RESERVATION RR
                        ON RR.RESERVATION_ID = BS.RESERVATION_ID
                    WHERE BS.FLIGHT_ID = F.FLIGHT_ID
                      AND BS.PASSENGER_ID = ?
                      AND RR.STATUS <> 'CANCELLED'
                )
                """;
        Map<Long, Flight> flights = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, passenger_id);
            try (
                    ResultSet resultSet = statement.executeQuery()
            ){

                while(resultSet.next()){
                    flights.put(
                            resultSet.getLong(1)
                            ,mapResultSetToFlight(resultSet));
                }
            }
            return flights;
        }
    }


    public boolean updateActualDeparture(
            Connection connection, long flight_id, LocalTime actualDeparture
            )throws SQLException {
        String sql = """
                UPDATE FLIGHT
                SET ACTUAL_DEPARTURE = ?
                WHERE FLIGHT_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, actualDeparture.toString());
            statement.setLong(2, flight_id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateActualArrival(
            Connection connection, long flight_id, LocalTime actualArrival
    )throws SQLException {
        String sql = """
                UPDATE FLIGHT
                SET ACTUAL_ARRIVAL = ?
                WHERE FLIGHT_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, actualArrival.toString());
            statement.setLong(2, flight_id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateFlightStatus(
            Connection connection, long flight_id, String flightStatus
    )throws SQLException{
        String sql = """
                UPDATE FLIGHT
                SET FLIGHT_STATUS = ?
                WHERE FLIGHT_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, flightStatus);
            statement.setLong(2, flight_id);
            return statement.executeUpdate() > 0;
        }
    }



    public List<Flight> getAllFlightsList(Connection connection) throws SQLException {
        String sql = """
                SELECT
                    F.FLIGHT_ID,
                    F.FLIGHT_DATE,
                    DEP.AIRPORT_NAME as DEPARTURE_AIRPORT,
                    ARR.AIRPORT_NAME as ARRIVAL_AIRPORT,
                    F.AIRCRAFT_ID,
                    FS.DEP_TIME,
                    FS.ARR_TIME,
                    F.ACTUAL_DEPARTURE,
                    F.ACTUAL_ARRIVAL,
                    F.FLIGHT_STATUS,
                    F.SCHEDULE_ID
                FROM FLIGHT F join FLIGHT_SCHEDULE FS
                    ON F.SCHEDULE_ID = FS.SCHEDULE_ID
                    JOIN ROUTE R ON R.ROUTE_ID = FS.ROUTE_ID
                    JOIN AIRPORT DEP ON DEP.AIRPORT_ID = R.ORIGIN_AIRPORT_ID
                    JOIN AIRPORT ARR ON ARR.AIRPORT_ID = R.DEST_AIRPORT_ID
                """;
        List<Flight> flightList = new ArrayList<>();
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
            ){

            while(resultSet.next()){
                flightList.add(mapResultSetToFlight(resultSet));
            }
        }
        return flightList;
    }


    public Map<Long, Flight> getAllFlightsMap(Connection connection) throws SQLException {
        String sql = """
                SELECT
                    F.FLIGHT_ID,
                    F.FLIGHT_DATE,
                    DEP.AIRPORT_NAME as DEPARTURE_AIRPORT,
                    ARR.AIRPORT_NAME as ARRIVAL_AIRPORT,
                    F.AIRCRAFT_ID,
                    FS.DEP_TIME,
                    FS.ARR_TIME,
                    F.ACTUAL_DEPARTURE,
                    F.ACTUAL_ARRIVAL,
                    F.FLIGHT_STATUS,
                    F.SCHEDULE_ID
                FROM FLIGHT F join FLIGHT_SCHEDULE FS
                    ON F.SCHEDULE_ID = FS.SCHEDULE_ID
                    JOIN ROUTE R ON R.ROUTE_ID = FS.ROUTE_ID
                    JOIN AIRPORT DEP ON DEP.AIRPORT_ID = R.ORIGIN_AIRPORT_ID
                    JOIN AIRPORT ARR ON ARR.AIRPORT_ID = R.DEST_AIRPORT_ID
                """;
        Map<Long, Flight> flights = new HashMap<>();
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ){

            while(resultSet.next()){
                flights.put(
                        resultSet.getLong(1)
                        ,mapResultSetToFlight(resultSet));
            }
        }
        return flights;
    }

    public List<Flight> getFlightsByDepAndArrivalCountry(
            Connection connection, String departureCountry,
            String arrivalCOuntry) throws SQLException{
        String sql = """
                SELECT
                    F.FLIGHT_ID,
                    F.FLIGHT_DATE,
                    DEP.AIRPORT_NAME as DEPARTURE_AIRPORT,
                    ARR.AIRPORT_NAME as ARRIVAL_AIRPORT,
                    F.AIRCRAFT_ID,
                    FS.DEP_TIME,
                    FS.ARR_TIME,
                    F.ACTUAL_DEPARTURE,
                    F.ACTUAL_ARRIVAL,
                    F.FLIGHT_STATUS,
                    F.SCHEDULE_ID
                FROM FLIGHT F join FLIGHT_SCHEDULE FS
                    ON F.SCHEDULE_ID = FS.SCHEDULE_ID
                    JOIN ROUTE R ON R.ROUTE_ID = FS.ROUTE_ID
                    JOIN AIRPORT DEP ON DEP.AIRPORT_ID = R.ORIGIN_AIRPORT_ID
                    JOIN AIRPORT ARR ON ARR.AIRPORT_ID = R.DEST_AIRPORT_ID
                WHERE DEP.COUNTRY = ?
                  AND ARR.COUNTRY = ?
                """;
        List<Flight> flightList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, departureCountry);
            statement.setString(2, arrivalCOuntry);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    flightList.add(mapResultSetToFlight(resultSet));
                }
            }
        }
        return flightList;
    }


    private Flight mapResultSetToFlight(ResultSet resultSet) throws SQLException {
        String actualDeparture = resultSet.getString("ACTUAL_DEPARTURE");
        String actualArrival = resultSet.getString("ACTUAL_ARRIVAL");
        return new Flight(
                resultSet.getLong("FLIGHT_ID"),
                resultSet.getDate("FLIGHT_DATE").toLocalDate(),
                resultSet.getString("DEPARTURE_AIRPORT"),
                resultSet.getString("ARRIVAL_AIRPORT"),
                resultSet.getLong("AIRCRAFT_ID"),
                LocalTime.parse(resultSet.getString("DEP_TIME")),
                LocalTime.parse(resultSet.getString("ARR_TIME")),
                actualDeparture != null ? LocalTime.parse(actualDeparture) : null,
                actualArrival != null ? LocalTime.parse(actualArrival) : null,
                resultSet.getString("FLIGHT_STATUS"),
                resultSet.getLong("SCHEDULE_ID")
        );
    }


}
