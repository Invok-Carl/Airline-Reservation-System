
package com.airline;

import com.airline.controller.*;
import com.airline.dao.*;
import com.airline.model.Employee;
import com.airline.service.*;
import com.airline.util.connection.DatabaseConnection;
import com.airline.util.security.EmployeeSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.function.Function;

import com.airline.util.security.PassengerSessionManager;

public class AirlineHttpServer {

    private static final int PORT = 8080;

    // Front-end directory, relative to the project root.
    private static final Path FRONTEND_DIR =
            Path.of("frontend").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException, SQLException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(PORT), 0
        );

        FlightService flightService = new FlightService();
AircraftDAO aircraftDAO = new AircraftDAO();

AircraftService aircraftService = new AircraftService(
        aircraftDAO,
        DatabaseConnection::getConnection,
        new AuthorizationService()
);


        PassengerDAO passengerDAO = new PassengerDAO();
        PassengerAccountDAO passengerAccountDAO =
                new PassengerAccountDAO();

        PassengerAccountService passengerAccountService =
                new PassengerAccountService(
                        passengerDAO,
                        passengerAccountDAO
                );

        // false for local HTTP development.
        // Set true when the site is served over HTTPS.
        PassengerSessionManager passengerSessionManager =
                new PassengerSessionManager(false);

        EmployeeDAO employeeDAO = new EmployeeDAO();
        SeatDAO seatDAO = new SeatDAO();
        SeatService seatService = new SeatService(DatabaseConnection::getConnection, seatDAO);

        AuthenticationService authenticationService =
                new AuthenticationService(employeeDAO, DatabaseConnection::getConnection);

        AuthorizationService authorizationService =
                new AuthorizationService();
        ReservationService reservationService = new ReservationService();



        EmployeeSessionManager employeeSessionManager =
                new EmployeeSessionManager(false); // Local HTTP development
        Function<HttpExchange, EmployeeSessionManager.Session> employeeResolver =
                employeeSessionManager::getSessionFromRequest;
        Function<HttpExchange, PassengerSessionManager.Session> passengerResolver =
                passengerSessionManager::getPassengerSession;
        PassengerService passengerService = new PassengerService(DatabaseConnection::getConnection,
                passengerDAO);



        server.createContext(
                "/api/reservations",
                new ReservationController(reservationService, passengerSessionManager)
        );

        server.createContext("/api/flights", new FlightController(
                flightService,
                seatService,
                employeeResolver,
                authorizationService
        ));

        RouteService routeService = new RouteService(new RouteDAO(), DatabaseConnection::getConnection);
        server.createContext("/api/routes", new RouteController(
                routeService,
                employeeResolver,
                authorizationService
        ));
        EmployeeManagementService employeeManagementService =
                new EmployeeManagementService(new EmployeeDAO(), DatabaseConnection::getConnection);
        server.createContext("/api/employees", new EmployeeController(
                employeeManagementService,
                employeeResolver,
                authorizationService
        ));

        ObjectMapper objectMapper = new ObjectMapper();
server.createContext(
        "/api/aircraft",
        new AircraftController(
                aircraftService,
                employeeResolver,
                objectMapper
        )
);
        server.createContext(
                "/api/employee-auth",
                new EmployeeAuthenticationController(
                        authenticationService,
                        authorizationService,
                        employeeSessionManager,
                        objectMapper
                )
        );

        server.createContext(
                "/api/auth",
                new PassengerAccountController(
                        passengerAccountService,
                        passengerSessionManager
                )
        );
        server.createContext(
                "/api/passengers",
                new PassengerController(
                        passengerService,
                        objectMapper,
                        passengerResolver,
                        employeeResolver
                )
        );


        // Serve HTML, CSS, JavaScript, and other static files.
        server.createContext("/", exchange -> {
            try {
                String requestPath =
                        exchange.getRequestURI().getPath();

                String method = exchange.getRequestMethod();

                if (!method.equals("GET") && !method.equals("HEAD")) {
                    exchange.getResponseHeaders().set(
                            "Allow", "GET, HEAD"
                    );
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                // Default page.
                if (requestPath.equals("/")) {
                    requestPath = "/index.html";
                }

                // Resolve the requested path safely within frontend/.
                Path file = FRONTEND_DIR
                        .resolve(requestPath.substring(1))
                        .normalize();

                if (!file.startsWith(FRONTEND_DIR)
                        || !Files.isRegularFile(file)) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                byte[] content = Files.readAllBytes(file);

                exchange.getResponseHeaders().set(
                        "Content-Type", getContentType(file)
                );
                exchange.getResponseHeaders().set(
                        "X-Content-Type-Options", "nosniff"
                );

                if (method.equals("HEAD")) {
                    exchange.getResponseHeaders().set(
                            "Content-Length",
                            String.valueOf(content.length)
                    );
                    exchange.sendResponseHeaders(200, -1);
                    return;
                }

                exchange.sendResponseHeaders(200, content.length);
                exchange.getResponseBody().write(content);

            } catch (IOException e) {
                e.printStackTrace();

                if (exchange.getResponseCode() == -1) {
                    try {
                        exchange.sendResponseHeaders(500, -1);
                    } catch (IOException ignored) {
                        // The connection may already be closed.
                    }
                }
            } finally {
                exchange.close();
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(10));

        server.start();

        System.out.println(
                "Airline server running at http://localhost:" + PORT
        );
        System.out.println(
                "Flight page: http://localhost:" + PORT
                        + "/pages/flights.html"
        );
        System.out.println(
                "Login page: http://localhost:" + PORT
                        + "/pages/login.html"
        );
        System.out.println(
                "Register page: http://localhost:" + PORT
                        + "/pages/register.html"
        );
    }

    private static String getContentType(Path file) {
        String name = file.getFileName()
                .toString()
                .toLowerCase();

        if (name.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (name.endsWith(".js")) {
            return "text/javascript; charset=UTF-8";
        }
        if (name.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (name.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (name.endsWith(".ico")) {
            return "image/x-icon";
        }

        return "application/octet-stream";
    }
}
