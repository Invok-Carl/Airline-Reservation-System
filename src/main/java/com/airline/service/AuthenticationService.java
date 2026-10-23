package com.airline.service;

import com.airline.dao.EmployeeDAO;
import com.airline.model.Employee;
import com.airline.util.connection.ConnectionProvider;
import com.airline.util.security.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthenticationService {

    private final EmployeeDAO employeeDAO;
    private final ConnectionProvider connectionProvider;

    public AuthenticationService(
            EmployeeDAO employeeDAO, ConnectionProvider connectionProvider
            ) {
        this.employeeDAO = employeeDAO;
        this.connectionProvider = connectionProvider;
    }

    public Employee login(String username, String password) throws SQLException {
        try (Connection connection =  connectionProvider.getConnection()){
            Employee employee =
                    employeeDAO.findByUsername(connection, username);

            if (employee == null) {
                return null;
            }

            if (!employee.getEmploymentStatus().equals("ACTIVE")) {
                return null;
            }

            if (!PasswordHasher.verifyPassword(
                    password,
                    employee.getPasswordHash())) {

                return null;
            }

            return employee;
        }
    }
}