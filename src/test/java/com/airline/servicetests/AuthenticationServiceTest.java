
package com.airline.servicetests;

import com.airline.dao.EmployeeDAO;
import com.airline.model.Employee;
import com.airline.service.AuthenticationService;
import com.airline.util.connection.ConnectionProvider;
import com.airline.util.security.PasswordHasher;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private Connection createConnection() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) {
                        return null;
                    }

                    if (method.getName().equals("isClosed")) {
                        return false;
                    }

                    if (method.getName().equals("isWrapperFor")) {
                        return false;
                    }

                    if (method.getName().equals("unwrap")) {
                        throw new SQLException("Not a wrapper");
                    }

                    Class<?> returnType = method.getReturnType();

                    if (returnType == boolean.class) return false;
                    if (returnType == int.class) return 0;
                    if (returnType == long.class) return 0L;

                    return null;
                }
        );
    }

    private Employee createEmployee(String status, String passwordHash) {
        return new Employee(
                1L,
                "Test",
                "Employee",
                "RESERVATION_AGENT",
                "0700000000",
                "employee@example.com",
                status,
                "testemployee",
                passwordHash
        );
    }

    private String hashPassword(String password) {
        return PasswordHasher.hashPassword(password);
    }

    @Test
    void loginReturnsEmployeeWhenCredentialsAreCorrect() throws SQLException {
        String password = "CorrectPassword123!";
        Employee employee = createEmployee(
                "ACTIVE",
                hashPassword(password)
        );

        Connection connection = createConnection();

        ConnectionProvider connectionProvider = () -> connection;

        EmployeeDAO employeeDAO = new EmployeeDAO() {
            @Override
            public Employee findByUsername(
                    Connection conn,
                    String username
            ) {
                assertEquals("testemployee", username);
                assertSame(connection, conn);
                return employee;
            }
        };

        AuthenticationService service =
                new AuthenticationService(employeeDAO, connectionProvider);

        Employee result = service.login("testemployee", password);

        assertSame(employee, result);
    }

    @Test
    void loginReturnsNullWhenEmployeeDoesNotExist() throws SQLException {
        Connection connection = createConnection();

        ConnectionProvider connectionProvider = () -> connection;

        EmployeeDAO employeeDAO = new EmployeeDAO() {
            @Override
            public Employee findByUsername(
                    Connection conn,
                    String username
            ) {
                return null;
            }
        };

        AuthenticationService service =
                new AuthenticationService(employeeDAO, connectionProvider);

        Employee result = service.login("unknown", "SomePassword123!");

        assertNull(result);
    }

    @Test
    void loginReturnsNullWhenEmployeeIsInactive() throws SQLException {
        Employee employee = createEmployee(
                "INACTIVE",
                hashPassword("CorrectPassword123!")
        );

        Connection connection = createConnection();

        ConnectionProvider connectionProvider = () -> connection;

        EmployeeDAO employeeDAO = new EmployeeDAO() {
            @Override
            public Employee findByUsername(
                    Connection conn,
                    String username
            ) {
                return employee;
            }
        };

        AuthenticationService service =
                new AuthenticationService(employeeDAO, connectionProvider);

        Employee result = service.login(
                "testemployee",
                "CorrectPassword123!"
        );

        assertNull(result);
    }

    @Test
    void loginReturnsNullWhenPasswordIsIncorrect() throws SQLException {
        Employee employee = createEmployee(
                "ACTIVE",
                hashPassword("CorrectPassword123!")
        );

        Connection connection = createConnection();

        ConnectionProvider connectionProvider = () -> connection;

        EmployeeDAO employeeDAO = new EmployeeDAO() {
            @Override
            public Employee findByUsername(
                    Connection conn,
                    String username
            ) {
                return employee;
            }
        };

        AuthenticationService service =
                new AuthenticationService(employeeDAO, connectionProvider);

        Employee result = service.login(
                "testemployee",
                "WrongPassword123!"
        );

        assertNull(result);
    }

    @Test
    void loginPropagatesSQLExceptionFromDao() throws SQLException {
        Connection connection = createConnection();

        ConnectionProvider connectionProvider = () -> connection;

        EmployeeDAO employeeDAO = new EmployeeDAO() {
            @Override
            public Employee findByUsername(
                    Connection conn,
                    String username
            ) throws SQLException {
                throw new SQLException("Database query failed");
            }
        };

        AuthenticationService service =
                new AuthenticationService(employeeDAO, connectionProvider);

        SQLException exception = assertThrows(
                SQLException.class,
                () -> service.login("testemployee", "SomePassword123!")
        );

        assertEquals("Database query failed", exception.getMessage());
    }
}
