package com.airline.dao;

import com.airline.model.Employee;
import com.airline.util.connection.DatabaseConnection;
import java.sql.*;

public class EmployeeDAO {

    public boolean insertEmployee(Employee employee) {
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
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ){
            statement.setLong(1, employee.getEmployeeId());
            statement.setString(2, employee.getFirstName());
            statement.setString(3, employee.getLastName());
            statement.setString(4, employee.getPosition());
            statement.setString(5, employee.getPhone());
            statement.setString(6, employee.getEmail());
            statement.setString(7, employee.getEmploymentStatus());
            statement.setString(8, employee.getUserName());
            statement.setString(9, employee.getPasswordHash());

            int rowsAffected = statement.executeUpdate();

            if(rowsAffected == 0){
                return false;
            }
            return true;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public Employee findByUsername(String username) {

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
                Connection connection = DatabaseConnection.getConnection();
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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;        
    }
    }