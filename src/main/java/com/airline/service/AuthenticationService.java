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
    public Employee register(
            String firstName,
            String lastName,
            String username,
            String password) throws SQLException {

        try (Connection connection = connectionProvider.getConnection()) {

            // 1. Username must be unique.
            if (employeeDAO.findByUsername(connection, username) != null) {
                return null;
            }

            // 2. Get a new employee ID from the sequence.
            long newId;
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "SELECT EMPLOYEE_SEQ.NEXTVAL FROM DUAL");
                 java.sql.ResultSet result = statement.executeQuery()) {
                result.next();
                newId = result.getLong(1);
            }

            // 3. Create the employee (role defaults to EMPLOYEE).
            Employee employee = new Employee(
                    newId,
                    firstName,
                    lastName,
                    "RESERVATION_AGENT",
                    null,
                    null,
                    "ACTIVE",
                    username,
                    PasswordHasher.hashPassword(password)
            );

            employeeDAO.insertEmployee(connection, employee);
            return employee;
        }
    }

}