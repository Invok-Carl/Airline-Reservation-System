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
                1234567,
          "Ghalib",
          "Ahmad",
          "RESERVATION_AGENT",
                "07007574133",
                "ghalibahmad@123.com",
                "ACTIVE",
                "ghalibahmad001",
                "$2a$12$rz.togQLobgd2lZuzMDiROAG2v8uLV61nMqXVX.UMNrEmhkS1lvKS"
        );
    }

    @AfterEach
    void tearDown() throws Exception{
        if(connection != null){
            try {
                connection.rollback();
            }finally {
                connection.close();
            }
        }
    }



    @Test
    void testInsertEmployee_and_findbyUsername() throws Exception {
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");
        Employee actual = employeeDAO.findByUsername(connection,"ghalibahmad001");
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
        Employee actual = employeeDAO.findById(connection,1234567);
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
        Employee actual = employeeDAO.findByEmail(connection,"ghalibahmad@123.com");
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
                connection, 1234567, "INACTIVE");
        Employee actual = employeeDAO.findByEmail(connection,"ghalibahmad@123.com");
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
        Employee actual = employeeDAO.findByEmail(connection,"ghalibahmad@123.com");
        assertNotNull(actual);

        boolean delete = employeeDAO.deleteEmployeeById(connection, actual.getEmployeeId());
        assertTrue(delete, "Employee not inserted");
        Employee deleted = employeeDAO.findByEmail(connection,"ghalibahmad@123.com");
        assertNull(deleted);
    }
    @Test
    void testGetAllEmployees() throws Exception {
        boolean inserted = employeeDAO.insertEmployee(connection, employee);
        assertTrue(inserted, "Employee not inserted");


        Map<Long, Employee> employees = employeeDAO.getAllEmployeesMap(connection);
        List<Employee> employeesList = employeeDAO.getAllEmployeesList(connection);

        assertTrue( employees.size() > 0, "List empty");
        assertTrue( employeesList.size() > 0, "List empty");


        Employee actual = employees.get(1234567L);
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
