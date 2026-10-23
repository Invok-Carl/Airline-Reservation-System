package com.airline.dao;


import com.airline.model.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    // CREATE ROUTE
    public boolean createRoute(
            Connection connection,
            Route route) throws SQLException {

        String sql = """
            INSERT INTO ROUTE (
                ORIGIN_AIRPORT_ID,
                DEST_AIRPORT_ID,
                DISTANCE_KM,
                EST_DURATION_MIN,
                ROUTE_STATUS
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, route.getOriginAirportId());
            statement.setLong(2, route.getDestAirportId());
            statement.setDouble(3, route.getDistanceKm());
            statement.setInt(4, route.getEstDurationMin());
            statement.setString(
                    5, route.getRouteStatus().toUpperCase()
            );

            return statement.executeUpdate() > 0;
        }
    }


    // GET ROUTE BY ID
    public Route getRouteById(
            Connection connection,
            long routeId) throws SQLException {

        String sql = """
            SELECT ROUTE_ID,
                   ORIGIN_AIRPORT_ID,
                   DEST_AIRPORT_ID,
                   DISTANCE_KM,
                   EST_DURATION_MIN,
                   ROUTE_STATUS
            FROM ROUTE
            WHERE ROUTE_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, routeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRoute(resultSet);
                }
            }
        }

        return null;
    }


    // GET ALL ROUTES
    public List<Route> getAllRoutes(
            Connection connection) throws SQLException {

        List<Route> routes = new ArrayList<>();

        String sql = """
            SELECT ROUTE_ID,
                   ORIGIN_AIRPORT_ID,
                   DEST_AIRPORT_ID,
                   DISTANCE_KM,
                   EST_DURATION_MIN,
                   ROUTE_STATUS
            FROM ROUTE
            ORDER BY ROUTE_ID
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                routes.add(mapRoute(resultSet));
            }
        }

        return routes;
    }


    // GET ROUTES BY ORIGIN AIRPORT
    public List<Route> getRoutesByOrigin(
            Connection connection,
            long airportId) throws SQLException {

        List<Route> routes = new ArrayList<>();

        String sql = """
            SELECT ROUTE_ID,
                   ORIGIN_AIRPORT_ID,
                   DEST_AIRPORT_ID,
                   DISTANCE_KM,
                   EST_DURATION_MIN,
                   ROUTE_STATUS
            FROM ROUTE
            WHERE ORIGIN_AIRPORT_ID = ?
            ORDER BY ROUTE_ID
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, airportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    routes.add(mapRoute(resultSet));
                }
            }
        }

        return routes;
    }


    // GET ROUTES BY DESTINATION AIRPORT
    public List<Route> getRoutesByDestination(
            Connection connection,
            long airportId) throws SQLException {

        List<Route> routes = new ArrayList<>();

        String sql = """
            SELECT ROUTE_ID,
                   ORIGIN_AIRPORT_ID,
                   DEST_AIRPORT_ID,
                   DISTANCE_KM,
                   EST_DURATION_MIN,
                   ROUTE_STATUS
            FROM ROUTE
            WHERE DEST_AIRPORT_ID = ?
            ORDER BY ROUTE_ID
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, airportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    routes.add(mapRoute(resultSet));
                }
            }
        }

        return routes;
    }


    // UPDATE ROUTE
    public boolean updateRoute(
            Connection connection,
            Route route) throws SQLException {

        String sql = """
            UPDATE ROUTE
            SET ORIGIN_AIRPORT_ID = ?,
                DEST_AIRPORT_ID = ?,
                DISTANCE_KM = ?,
                EST_DURATION_MIN = ?,
                ROUTE_STATUS = ?
            WHERE ROUTE_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, route.getOriginAirportId());
            statement.setLong(2, route.getDestAirportId());
            statement.setDouble(3, route.getDistanceKm());
            statement.setInt(4, route.getEstDurationMin());
            statement.setString(
                    5, route.getRouteStatus().toUpperCase()
            );
            statement.setLong(6, route.getRouteId());

            return statement.executeUpdate() > 0;
        }
    }


    // DELETE ROUTE
    public boolean deleteRoute(
            Connection connection,
            long routeId) throws SQLException {

        String sql = """
            DELETE FROM ROUTE
            WHERE ROUTE_ID = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, routeId);

            return statement.executeUpdate() > 0;
        }
    }


    // MAP RESULT SET TO ROUTE MODEL
    private Route mapRoute(
            ResultSet resultSet) throws SQLException {

        return new Route(
                resultSet.getLong("ROUTE_ID"),
                resultSet.getLong("ORIGIN_AIRPORT_ID"),
                resultSet.getLong("DEST_AIRPORT_ID"),
                resultSet.getDouble("DISTANCE_KM"),
                resultSet.getInt("EST_DURATION_MIN"),
                resultSet.getString("ROUTE_STATUS")
        );
    }
}