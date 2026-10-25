
package com.airline.controllertests;

import com.airline.controller.EmployeeAuthenticationController;
import com.airline.model.Employee;
import com.airline.service.AuthenticationService;
import com.airline.service.AuthorizationService;
import com.airline.util.security.EmployeeSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeAuthenticationControllerTest {

    private AuthenticationService authenticationService;
    private EmployeeSessionManager sessionManager;
    private EmployeeAuthenticationController controller;
    private ObjectMapper objectMapper;

    private Employee admin;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        sessionManager = new EmployeeSessionManager(false);
        objectMapper = new ObjectMapper().findAndRegisterModules();

        controller = new EmployeeAuthenticationController(
                authenticationService,
                new AuthorizationService(),
                sessionManager,
                objectMapper
        );

        admin = new Employee(
                1L,
                "Test",
                "Admin",
                "ADMINISTRATOR",
                "0700000000",
                "admin@example.com",
                "ACTIVE",
                "admin",
                "unused-hash"
        );
    }

    @Test
    void loginShouldReturn200AndCreateSession() throws Exception {
        when(authenticationService.login("admin", "password"))
                .thenReturn(admin);

        TestRequest request = request(
                "POST",
                "/api/employee-auth/login",
                """
                {
                    "username": "admin",
                    "password": "password"
                }
                """
        );

        controller.handle(request.exchange());

        assertEquals(200, request.status());

        JsonNode response = responseJson(request);

        assertEquals(
                "Login successful.",
                response.path("message").asText()
        );
        assertEquals(
                "admin",
                response.path("employee").path("username").asText()
        );
        assertEquals(
                "ADMINISTRATOR",
                response.path("employee").path("position").asText()
        );
        assertTrue(
                response.path("employee")
                        .path("permissions")
                        .path("canAddEmployee")
                        .asBoolean()
        );

        String cookie = request.responseHeaders().getFirst("Set-Cookie");

        assertNotNull(cookie);
        assertTrue(cookie.startsWith(
                EmployeeSessionManager.COOKIE_NAME + "="
        ));

        String token = cookie.split(";", 2)[0].split("=", 2)[1];
        assertNotNull(sessionManager.getSession(token));

        verify(authenticationService).login("admin", "password");
    }

    @Test
    void loginShouldReturn400ForInvalidJson() throws Exception {
        TestRequest request = request(
                "POST",
                "/api/employee-auth/login",
                "{ invalid json"
        );

        controller.handle(request.exchange());

        assertEquals(400, request.status());
        assertEquals(
                "Invalid JSON request.",
                responseJson(request).path("error").asText()
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    void loginShouldReturn400WhenCredentialsAreMissing()
            throws Exception {

        TestRequest request = request(
                "POST",
                "/api/employee-auth/login",
                """
                {
                    "username": "admin",
                    "password": " "
                }
                """
        );

        controller.handle(request.exchange());

        assertEquals(400, request.status());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void loginShouldReturn401ForInvalidCredentials() throws Exception {
        when(authenticationService.login("unknown", "wrong"))
                .thenReturn(null);

        TestRequest request = request(
                "POST",
                "/api/employee-auth/login",
                """
                {
                    "username": "unknown",
                    "password": "wrong"
                }
                """
        );

        controller.handle(request.exchange());

        assertEquals(401, request.status());
        assertEquals(
                "Invalid credentials or inactive account.",
                responseJson(request).path("error").asText()
        );

        assertNull(request.responseHeaders().getFirst("Set-Cookie"));
    }

    @Test
    void loginShouldReturn500WhenAuthenticationThrowsSQLException()
            throws Exception {

        when(authenticationService.login("admin", "password"))
                .thenThrow(new SQLException("Database unavailable"));

        TestRequest request = request(
                "POST",
                "/api/employee-auth/login",
                """
                {
                    "username": "admin",
                    "password": "password"
                }
                """
        );

        controller.handle(request.exchange());

        assertEquals(500, request.status());
        assertEquals(
                "Authentication service unavailable.",
                responseJson(request).path("error").asText()
        );
    }

    @Test
    void getCurrentEmployeeShouldReturn401WithoutSession()
            throws Exception {

        TestRequest request = request(
                "GET",
                "/api/employee-auth/me",
                ""
        );

        controller.handle(request.exchange());

        assertEquals(401, request.status());
        assertEquals(
                "Authentication required.",
                responseJson(request).path("error").asText()
        );
    }

    @Test
    void getCurrentEmployeeShouldReturn200WithValidSession()
            throws Exception {

        EmployeeSessionManager.CreatedSession created =
                sessionManager.createSession(
                        admin.getEmployeeId(),
                        admin.getUserName(),
                        admin.getPosition()
                );

        TestRequest request = request(
                "GET",
                "/api/employee-auth/me",
                ""
        );

        request.requestHeaders().add(
                "Cookie",
                EmployeeSessionManager.COOKIE_NAME
                        + "=" + created.token()
        );

        controller.handle(request.exchange());

        assertEquals(200, request.status());

        JsonNode response = responseJson(request);

        assertTrue(response.path("authenticated").asBoolean());
        assertEquals(
                "admin",
                response.path("employee").path("username").asText()
        );
    }

    @Test
    void logoutShouldInvalidateSessionAndClearCookie()
            throws Exception {

        EmployeeSessionManager.CreatedSession created =
                sessionManager.createSession(
                        admin.getEmployeeId(),
                        admin.getUserName(),
                        admin.getPosition()
                );

        TestRequest request = request(
                "POST",
                "/api/employee-auth/logout",
                ""
        );

        request.requestHeaders().add(
                "Cookie",
                EmployeeSessionManager.COOKIE_NAME
                        + "=" + created.token()
        );

        controller.handle(request.exchange());

        assertEquals(200, request.status());
        assertEquals(
                "Logout successful.",
                responseJson(request).path("message").asText()
        );
        assertNull(sessionManager.getSession(created.token()));

        assertTrue(
                request.responseHeaders()
                        .getFirst("Set-Cookie")
                        .contains("Max-Age=0")
        );
    }

    @Test
    void unknownEndpointShouldReturn404() throws Exception {
        TestRequest request = request(
                "GET",
                "/api/employee-auth/unknown",
                ""
        );

        controller.handle(request.exchange());

        assertEquals(404, request.status());
        assertEquals(
                "Endpoint not found.",
                responseJson(request).path("error").asText()
        );
    }

    private TestRequest request(
            String method,
            String path,
            String body
    ) throws IOException {
        return new TestRequest(method, path, body);
    }

    private JsonNode responseJson(TestRequest request)
            throws IOException {
        return objectMapper.readTree(request.responseBody().toByteArray());
    }

    /**
     * Small test fixture that mocks HttpExchange using Mockito.
     */
    private static class TestRequest {

        private final HttpExchange exchange;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody =
                new ByteArrayOutputStream();

        private int status = -1;

        TestRequest(String method, String path, String body)
                throws IOException {

            exchange = mock(HttpExchange.class);

            when(exchange.getRequestMethod()).thenReturn(method);
            when(exchange.getRequestURI()).thenReturn(
                    java.net.URI.create(path)
            );
            when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
            when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
            when(exchange.getRequestBody()).thenReturn(
                    new ByteArrayInputStream(
                            body.getBytes(StandardCharsets.UTF_8)
                    )
            );
            when(exchange.getResponseBody()).thenReturn(responseBody);

            doAnswer(invocation -> {
                status = invocation.getArgument(0);
                return null;
            }).when(exchange).sendResponseHeaders(
                    anyInt(),
                    anyLong()
            );

            when(exchange.getResponseCode()).thenAnswer(
                    invocation -> status
            );
        }

        HttpExchange exchange() {
            return exchange;
        }

        int status() {
            return status;
        }

        Headers requestHeaders() {
            return requestHeaders;
        }

        Headers responseHeaders() {
            return responseHeaders;
        }

        ByteArrayOutputStream responseBody() {
            return responseBody;
        }
    }
}
