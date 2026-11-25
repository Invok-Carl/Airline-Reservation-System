package com.airline.controller;

import com.airline.model.Employee;
import com.airline.service.AuthorizationService;
import com.airline.service.EmployeeManagementService;
import com.airline.util.security.EmployeeSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EmployeeController implements HttpHandler {

    private final EmployeeManagementService employeeService;
    private final Function<HttpExchange, EmployeeSessionManager.Session> employeeResolver;
    private final AuthorizationService authorizationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmployeeController(
            EmployeeManagementService employeeService,
            Function<HttpExchange, EmployeeSessionManager.Session> employeeResolver,
            AuthorizationService authorizationService) {
        this.employeeService = employeeService;
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

            Employee current = new Employee(
                    session.employeeId(), null, null, session.position(),
                    null, null, "ACTIVE", session.username(), null);

            if (!authorizationService.canManageEmployees(current)) {
                send(exchange, 403, Map.of("error", "Only an administrator can manage employees."));
                return;
            }

            if (!"GET".equals(exchange.getRequestMethod())) {
                send(exchange, 405, Map.of("error", "Method not allowed."));
                return;
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (Employee e : employeeService.getAllEmployees()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("employeeId", e.getEmployeeId());
                row.put("firstName", e.getFirstName());
                row.put("lastName", e.getLastName());
                row.put("position", e.getPosition());
                row.put("phone", e.getPhone());
                row.put("email", e.getEmail());
                row.put("employmentStatus", e.getEmploymentStatus());
                row.put("userName", e.getUserName());
                result.add(row);
            }
            send(exchange, 200, result);

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
}