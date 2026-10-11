package com.airline.service;

import com.airline.dao.EmployeeDAO;
import com.airline.model.Employee;
import com.airline.util.security.PasswordHasher;

public class AuthenticationService {

    private final EmployeeDAO employeeDAO;

    public AuthenticationService() {
        employeeDAO = new EmployeeDAO();
    }

    public Employee login(String username, String password) {

        Employee employee =
                employeeDAO.findByUsername(username);

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