package com.airline.dao;

import com.airline.model.Employee;
import com.airline.util.connection.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeDAO {

    public boolean insertEmployee(
            Connection connection,
            Employee employee) throws SQLException {
        String sql = """
            INSERT INTO EMPLOYEE (
                EMPLOYEE_ID,
                FIRST_NAME,
                LAST_NAME,
                EMP_POSITION,
                PHONE,
                EMAIL,
                EMPLOYMENT_STATUS,
                USERNAME,
                PASSWORD_HASH
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try(
                PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, employee.getEmployeeId());
            statement.setString(2, employee.getFirstName());
            statement.setString(3, employee.getLastName());
            statement.setString(4, employee.getPosition().toUpperCase());
            statement.setString(5, employee.getPhone());
            statement.setString(6, employee.getEmail());
            statement.setString(7, employee.getEmploymentStatus().toUpperCase());
            statement.setString(8, employee.getUserName());
            statement.setString(9, employee.getPasswordHash());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateEmployee(
            Connection connection,
            Employee employee) throws SQLException {

        String sql = """
        UPDATE EMPLOYEE
        SET
            EMPLOYEE_ID = ?,
            FIRST_NAME = ?,
            LAST_NAME = ?,
            EMP_POSITION = ?,
            PHONE = ?,
            EMAIL = ?,
            EMPLOYMENT_STATUS = ?,
            USERNAME = ?,
            PASSWORD_HASH = ?
        WHERE EMPLOYEE_ID = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employee.getEmployeeId());
            statement.setString(2, employee.getFirstName());
            statement.setString(3, employee.getLastName());
            statement.setString(4, employee.getPosition().toUpperCase());
            statement.setString(5, employee.getPhone());
            statement.setString(6, employee.getEmail());
            statement.setString(7, employee.getEmploymentStatus().toUpperCase());
            statement.setString(8, employee.getUserName());
            statement.setString(9, employee.getPasswordHash());
            statement.setLong(10, employee.getEmployeeId());

            return statement.executeUpdate() > 0;
        }
    }

    public Employee findById(
            Connection connection,
            long id) throws SQLException {

        String sql = """
            SELECT
                EMPLOYEE_ID,
                FIRST_NAME,
                LAST_NAME,
                EMP_POSITION,
                PHONE,
                EMAIL,
                EMPLOYMENT_STATUS,
                USERNAME,
                PASSWORD_HASH
            FROM EMPLOYEE
            WHERE EMPLOYEE_ID = ?
            """;

        try (PreparedStatement statement =
                        connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapResultSetToEmployee(result);
                }
            }
            return null;

        }
    }

    public Employee findByUsername(
            Connection connection,
            String username) throws SQLException {

        String sql = """
            SELECT
                EMPLOYEE_ID,
                FIRST_NAME,
                LAST_NAME,
                EMP_POSITION,
                PHONE,
                EMAIL,
                EMPLOYMENT_STATUS,
                USERNAME,
                PASSWORD_HASH
            FROM EMPLOYEE
            WHERE USERNAME = ?
            """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setString(1, username);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {

                        return mapResultSetToEmployee(result);
                    }
                }
                return null;

            }
        }

    public Employee findByEmail(
            Connection connection,
            String email) throws SQLException {

        String sql = """
            SELECT
                EMPLOYEE_ID,
                FIRST_NAME,
                LAST_NAME,
                EMP_POSITION,
                PHONE,
                EMAIL,
                EMPLOYMENT_STATUS,
                USERNAME,
                PASSWORD_HASH
            FROM EMPLOYEE
            WHERE EMAIL = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEmployee(resultSet);
                }
            }
            return null;
        }
    }

        public boolean changeEmployeeStatusById(Connection connection,
                                            long employeeId, String status
        ) throws SQLException {
            String sql = """
                    UPDATE EMPLOYEE
                    SET EMPLOYMENT_STATUS = ?
                    WHERE EMPLOYEE_ID = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, status);
                statement.setLong(2, employeeId);
                return statement.executeUpdate() > 0;
            }
        }

    public boolean deleteEmployeeById(Connection connection,long employeeId
    ) throws SQLException {
        String sql = """
                    DELETE FROM EMPLOYEE
                    WHERE EMPLOYEE_ID = ?
                    """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            return statement.executeUpdate() > 0;
        }
    }


    public List<Employee> getAllEmployeesList(Connection connection)
    throws SQLException {
        String sql = """
                SELECT 
                    EMPLOYEE_ID,
                    FIRST_NAME,
                    LAST_NAME,
                    EMP_POSITION,
                    PHONE,
                    EMAIL,
                    EMPLOYMENT_STATUS,
                    USERNAME,
                    PASSWORD_HASH
                FROM EMPLOYEE
                """;
        List<Employee> employees = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    employees.add(mapResultSetToEmployee(result));
                }
            }
            return employees;
        }
    }

    public Map<Long, Employee> getAllEmployeesMap(Connection connection)
            throws SQLException {
        String sql = """
                SELECT 
                    EMPLOYEE_ID,
                    FIRST_NAME,
                    LAST_NAME,
                    EMP_POSITION,
                    PHONE,
                    EMAIL,
                    EMPLOYMENT_STATUS,
                    USERNAME,
                    PASSWORD_HASH
                FROM EMPLOYEE
                """;
        Map<Long, Employee> employees = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    employees.put(
                            resultSet.getLong("EMPLOYEE_ID"),
                            mapResultSetToEmployee(resultSet)
                    );
                }
            }
            return employees;
        }
    }




    private Employee mapResultSetToEmployee(ResultSet resultSet) throws SQLException {
        Employee employee = new Employee(
                resultSet.getLong("EMPLOYEE_ID"),
                resultSet.getString("FIRST_NAME"),
                resultSet.getString("LAST_NAME"),
                resultSet.getString("EMP_POSITION"),
                resultSet.getString("PHONE"),
                resultSet.getString("EMAIL"),
                resultSet.getString("EMPLOYMENT_STATUS"),
                resultSet.getString("USERNAME"),
                resultSet.getString("PASSWORD_HASH")
        );


        return employee;
    }
}
