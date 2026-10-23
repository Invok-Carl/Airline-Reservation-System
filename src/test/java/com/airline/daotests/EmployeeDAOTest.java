package com.airline.daotests;

import com.airline.dao.EmployeeDAO;
import com.airline.model.Employee;
import com.airline.util.connection.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeDAOTest {
    Employee employee;
    EmployeeDAO employeeDAO;
    Connection connection;


    @BeforeEach
    void setUp() throws Exception{
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        employeeDAO = new EmployeeDAO();
        employee = new Employee(
                123456,
          "Nazir",
          "Ahmad",
          "RESERVATION_AGENT",
                "07007574132",
                "nazirahmad@123.com",
                "ACTIVE",
                "nazirahmad001",
                "asfdcvxzasd421234sadfxcvzxasdf213sa"
        );
    }

    @AfterEach
    void tearDown() throws Exception{
        if(connection != null){
            try {
                connection.rollback();
            }finally {
                connection.rollback();
            }
        }
    }



    @Test
    void testInsertEmployee_and_findbyUsername() throws Exception {
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");
        Employee actual = employeeDAO.findByUsername(connection,"nazirahmad001");
        assertNotNull(actual);

        assertEquals(employee.getEmployeeId(), actual.getEmployeeId());
        assertEquals(employee.getFirstName(), actual.getFirstName());
        assertEquals(employee.getLastName(), actual.getLastName());
        assertEquals(employee.getPosition(), actual.getPosition());
        assertEquals(employee.getPhone(), actual.getPhone());
        assertEquals(employee.getEmail(), actual.getEmail());
        assertEquals(employee.getEmploymentStatus(), actual.getEmploymentStatus());
        assertEquals(employee.getUserName(), actual.getUserName());
        assertEquals(employee.getPasswordHash(), actual.getPasswordHash());
    }

    @Test
    void testFindById() throws Exception{
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");
        Employee actual = employeeDAO.findById(connection,123456);
        assertNotNull(actual);

        assertEquals(employee.getEmployeeId(), actual.getEmployeeId());
        assertEquals(employee.getFirstName(), actual.getFirstName());
        assertEquals(employee.getLastName(), actual.getLastName());
        assertEquals(employee.getPosition(), actual.getPosition());
        assertEquals(employee.getPhone(), actual.getPhone());
        assertEquals(employee.getEmail(), actual.getEmail());
        assertEquals(employee.getEmploymentStatus(), actual.getEmploymentStatus());
        assertEquals(employee.getUserName(), actual.getUserName());
        assertEquals(employee.getPasswordHash(), actual.getPasswordHash());
    }


    @Test
    void testFindByEmail() throws Exception {
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");
        Employee actual = employeeDAO.findByEmail(connection,"nazirahmad@123.com");
        assertNotNull(actual);

        assertEquals(employee.getEmployeeId(), actual.getEmployeeId());
        assertEquals(employee.getFirstName(), actual.getFirstName());
        assertEquals(employee.getLastName(), actual.getLastName());
        assertEquals(employee.getPosition(), actual.getPosition());
        assertEquals(employee.getPhone(), actual.getPhone());
        assertEquals(employee.getEmail(), actual.getEmail());
        assertEquals(employee.getEmploymentStatus(), actual.getEmploymentStatus());
        assertEquals(employee.getUserName(), actual.getUserName());
        assertEquals(employee.getPasswordHash(), actual.getPasswordHash());
    }
    @Test
    void testChangeEmployementStatus() throws Exception {
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");
        boolean statusChanged = employeeDAO.changeEmployeeStatusById(
                connection, 123456, "INACTIVE");
        Employee actual = employeeDAO.findByEmail(connection,"nazirahmad@123.com");
        assertNotNull(actual);

        assertEquals(employee.getEmployeeId(), actual.getEmployeeId());
        assertEquals(employee.getFirstName(), actual.getFirstName());
        assertEquals(employee.getLastName(), actual.getLastName());
        assertEquals(employee.getPosition(), actual.getPosition());
        assertEquals(employee.getPhone(), actual.getPhone());
        assertEquals(employee.getEmail(), actual.getEmail());
        assertEquals("INACTIVE", actual.getEmploymentStatus());
        assertEquals(employee.getUserName(), actual.getUserName());
        assertEquals(employee.getPasswordHash(), actual.getPasswordHash());

    }
    @Test
    void testDeleteEmployee() throws Exception {
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");
        Employee actual = employeeDAO.findByEmail(connection,"nazirahmad@123.com");
        assertNotNull(actual);

        boolean delete = employeeDAO.deleteEmployeeById(connection, actual.getEmployeeId());
        assertTrue(delete, "Employee not inserted");
        Employee deleted = employeeDAO.findByEmail(connection,"nazirahmad@123.com");
        assertNull(deleted);
    }
    @Test
    void testGetAllEmployees() throws Exception {
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");


        Map<Long, Employee> employees = employeeDAO.getAllEmployeesMap(connection);
        List<Employee> employeesList = employeeDAO.getAllEmployeesList(connection);

        assertEquals(12, employees.size());
        assertEquals(12, employeesList.size());


        Employee actual = employees.get(123456L);
        assertNotNull(actual);
        assertEquals(employee.getEmployeeId(), actual.getEmployeeId());
        assertEquals(employee.getFirstName(), actual.getFirstName());
        assertEquals(employee.getLastName(), actual.getLastName());
        assertEquals(employee.getPosition(), actual.getPosition());
        assertEquals(employee.getPhone(), actual.getPhone());
        assertEquals(employee.getEmail(), actual.getEmail());
        assertEquals(employee.getEmploymentStatus(), actual.getEmploymentStatus());
        assertEquals(employee.getUserName(), actual.getUserName());
        assertEquals(employee.getPasswordHash(), actual.getPasswordHash());
    }

}
