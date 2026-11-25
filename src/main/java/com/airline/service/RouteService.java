package com.airline.service;

import com.airline.dao.RouteDAO;
import com.airline.model.Route;
import com.airline.util.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RouteService {

    private final RouteDAO routeDAO;
    private final ConnectionProvider connectionProvider;

    public RouteService(RouteDAO routeDAO, ConnectionProvider connectionProvider) {
        this.routeDAO = routeDAO;
        this.connectionProvider = connectionProvider;
    }

    public List<Route> getAllRoutes() throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            return routeDAO.getAllRoutes(connection);
        }
    }

    public boolean createRoute(Route route) throws SQLException {
        if (route.getOriginAirportId() <= 0 || route.getDestAirportId() <= 0) {
            throw new IllegalArgumentException("Origin and destination airports are required.");
        }
        if (route.getOriginAirportId() == route.getDestAirportId()) {
            throw new IllegalArgumentException("Origin and destination must be different.");
        }
        if (route.getRouteStatus() == null || route.getRouteStatus().isBlank()) {
            throw new IllegalArgumentException("Route status is required.");
        }

        try (Connection connection = connectionProvider.getConnection()) {
            return routeDAO.createRoute(connection, route);
        }
    }
    public boolean updateRoute(Route route) throws SQLException {
        if (route.getRouteId() <= 0) {
            throw new IllegalArgumentException("Route ID is required.");
        }
        if (route.getOriginAirportId() <= 0 || route.getDestAirportId() <= 0) {
            throw new IllegalArgumentException("Origin and destination airports are required.");
        }
        if (route.getOriginAirportId() == route.getDestAirportId()) {
            throw new IllegalArgumentException("Origin and destination must be different.");
        }
        if (route.getRouteStatus() == null || route.getRouteStatus().isBlank()) {
            throw new IllegalArgumentException("Route status is required.");
        }

        try (Connection connection = connectionProvider.getConnection()) {
            return routeDAO.updateRoute(connection, route);
        }
    }
}