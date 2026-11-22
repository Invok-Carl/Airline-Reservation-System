package com.airline.controller;

import com.airline.model.Employee;
import com.airline.model.Route;
import com.airline.service.AuthorizationService;
import com.airline.service.RouteService;
import com.airline.util.security.EmployeeSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RouteController implements HttpHandler {

    private final RouteService routeService;
    private final Function<HttpExchange, EmployeeSessionManager.Session> employeeResolver;
    private final AuthorizationService authorizationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RouteController(
            RouteService routeService,
            Function<HttpExchange, EmployeeSessionManager.Session> employeeResolver,
            AuthorizationService authorizationService) {
        this.routeService = routeService;
        this.employeeResolver = employeeResolver;
        this.authorizationService = authorizationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            EmployeeSessionManager.Session session = employeeResolver.apply(exchange);
            if (session == null) {
                send(exchange, 401, Map.of("error", "Authentication required."));
                return;
            }

            Employee employee = new Employee(
                    session.employeeId(), null, null, session.position(),
                    null, null, "ACTIVE", session.username(), null);

            if (!authorizationService.canAddFlight(employee)) {
                send(exchange, 403, Map.of("error", "Only an administrator can manage routes."));
                return;
            }

            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Route> routes = routeService.getAllRoutes();
                send(exchange, 200, routes);
            } else if ("POST".equals(method)) {
                AddRouteRequest request = objectMapper.readValue(exchange.getRequestBody(), AddRouteRequest.class);

                Route route = new Route(request.originAirportId(), request.destAirportId(),
                        request.distanceKm(), request.estDurationMin(), request.routeStatus());
                routeService.createRoute(route);

                send(exchange, 201, Map.of("message", "Route created."));
            } else if ("PUT".equals(method)) {
                UpdateRouteRequest request = objectMapper.readValue(exchange.getRequestBody(), UpdateRouteRequest.class);
                Route route = new Route(request.routeId(), request.originAirportId(), request.destAirportId(),
                        request.distanceKm(), request.estDurationMin(), request.routeStatus());
                if (routeService.updateRoute(route)) {
                    send(exchange, 200, Map.of("message", "Route updated."));
                } else {
                    send(exchange, 404, Map.of("error", "Route not found."));
                }
            } else {
                send(exchange, 405, Map.of("error", "Method not allowed."));
            }

        } catch (IllegalArgumentException e) {
            send(exchange, 400, Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            send(exchange, 500, Map.of("error", "Database error."));
        } catch (Exception e) {
            send(exchange, 400, Map.of("error", "Invalid request."));
        }
    }

    private void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
    public record AddRouteRequest(
            long originAirportId,
            long destAirportId,
            double distanceKm,
            int estDurationMin,
            String routeStatus) {
    }

    public record UpdateRouteRequest(
            long routeId,
            long originAirportId,
            long destAirportId,
            double distanceKm,
            int estDurationMin,
            String routeStatus) {
    }
}