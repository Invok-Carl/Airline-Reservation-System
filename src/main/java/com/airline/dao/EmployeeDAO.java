package com.airline.dao;

import com.airline.model.Employee;
import com.airline.util.connection.DatabaseConnection;
import java.sql.*;

public class EmployeeDAO {

    public long insertEmployee(
            Connection connection,
            Employee employee) throws SQLException {
        String sql = """
            INSERT INTO EMPLOYEE (
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
                PreparedStatement statement = connection.prepareStatement(sql,
                        new String[] {"Employee_id"}
                        )
        ){
            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setString(3, employee.getPosition());
            statement.setString(4, employee.getPhone());
            statement.setString(5, employee.getEmail());
            statement.setString(6, employee.getEmploymentStatus());
            statement.setString(7, employee.getUserName());
            statement.setString(8, employee.getPasswordHash());

            int rowsAffected = statement.executeUpdate();

            if(rowsAffected == 0){
                throw new SQLException("Insertion affected no rows");
            }
            try(ResultSet resultSet = statement.getGeneratedKeys()){
                if(resultSet.next()){
                    return resultSet.getLong(1);
                }
            }
            throw new SQLException("Insertion succeeded, but no ID returned");
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

                        return new Employee(
                                result.getLong("EMPLOYEE_ID"),
                                result.getString("FIRST_NAME"),
                                result.getString("LAST_NAME"),
                                result.getString("EMP_POSITION"),
                                result.getString("PHONE"),
                                result.getString("EMAIL"),
                                result.getString("EMPLOYMENT_STATUS"),
                                result.getString("USERNAME"),
                                result.getString("PASSWORD_HASH")
                        );
                    }
                }
                return null;

            }
        }
    }