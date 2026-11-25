package com.airline.service;

import com.airline.dao.EmployeeDAO;
import com.airline.model.Employee;
import com.airline.util.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeManagementService {

    private final EmployeeDAO employeeDAO;
    private final ConnectionProvider connectionProvider;

    public EmployeeManagementService(EmployeeDAO employeeDAO, ConnectionProvider connectionProvider) {
        this.employeeDAO = employeeDAO;
        this.connectionProvider = connectionProvider;
    }

    public List<Employee> getAllEmployees() throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            return new ArrayList<>(employeeDAO.getAllEmployeesMap(connection).values());
        }
    }
}