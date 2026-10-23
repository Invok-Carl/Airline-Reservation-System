
package com.airline;

import com.airline.controller.FlightController;
import com.airline.service.FlightService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class AirlineHttpServer {

    private static final int PORT = 8080;

    // Front-end directory, relative to the project root.
    private static final Path FRONTEND_DIR =
            Path.of("frontend").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(PORT), 0
        );

        FlightService flightService = new FlightService();

        /*
         * Register the API endpoint.
         *
         * The employee resolver currently returns null because
         * authentication/session handling has not been connected yet.
         * Consequently, GET requests work, but POST requests return 401.
         */
        server.createContext(
                "/api/flights",
                new FlightController(flightService, exchange -> null)
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
                        + "/index.html"
        );
        System.out.println(
                "Flights API: http://localhost:" + PORT
                        + "/api/flights"
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
