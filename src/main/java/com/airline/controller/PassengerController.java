package com.airline.controller;

import com.airline.model.Passenger;
import com.airline.service.PassengerService;
import com.airline.util.http.HttpResponseUtil;
import com.airline.util.security.EmployeeSessionManager;
import com.airline.util.security.PassengerSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PassengerController implements HttpHandler {

    private static final Logger LOGGER =
            Logger.getLogger(PassengerController.class.getName());

    private static final String BASE_PATH = "/api/passengers";

    private static final Pattern UPDATE_PASSENGER_PATH =
            Pattern.compile("^/api/passengers/(\\d+)$");

    private final PassengerService passengerService;
    private final ObjectMapper objectMapper;
    private final HttpResponseUtil responseUtil;

    private final Function<
            HttpExchange,
            PassengerSessionManager.Session
            > passengerResolver;

    private final Function<
            HttpExchange,
            EmployeeSessionManager.Session
            > employeeResolver;


    public PassengerController(
            PassengerService passengerService,
            ObjectMapper objectMapper,
            Function<HttpExchange, PassengerSessionManager.Session>
                    passengerResolver,
            Function<HttpExchange, EmployeeSessionManager.Session>
                    employeeResolver
    ) {
        this.passengerService = passengerService;
        this.objectMapper = objectMapper;
        this.responseUtil = new HttpResponseUtil(objectMapper);
        this.passengerResolver = passengerResolver;
        this.employeeResolver = employeeResolver;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            // POST /api/passengers
            if (BASE_PATH.equals(path)) {

                if (!"POST".equals(method)) {
                    responseUtil.sendMethodNotAllowed(
                            exchange,
                            "POST"
                    );
                    return;
                }

                handleCreatePassenger(exchange);
                return;
            }

            // PUT /api/passengers/{id}
            Matcher matcher = UPDATE_PASSENGER_PATH.matcher(path);

            if (matcher.matches()) {

                if (!"PUT".equals(method) && !"GET".equals(method))  {
                    responseUtil.sendMethodNotAllowed(
                            exchange,
                            "PUT"
                    );
                    return;
                }

                long passengerId;

                try {
                    passengerId = Long.parseLong(matcher.group(1));

                    if (passengerId <= 0) {
                        responseUtil.sendError(
                                exchange,
                                400,
                                "Passenger ID must be positive."
                        );
                        return;
                    }

                } catch (NumberFormatException exception) {
                    responseUtil.sendError(
                            exchange,
                            400,
                            "Invalid passenger ID."
                    );
                    return;
                }

                if ("GET".equals(method)) {
                    handleGetPassenger(exchange, passengerId);
                } else {
                    handleUpdatePassenger(exchange, passengerId);
                }
                return;
            }

            responseUtil.sendError(
                    exchange,
                    404,
                    "Endpoint not found."
            );

        } catch (IOException exception) {
            // Do not attempt to send a second response here.
            // The response may already have been committed.
            throw exception;

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unexpected error while handling passenger request.",
                    exception
            );

            // A response might already have been committed.
            // Only attempt a 500 if it has not been committed.
            if (exchange.getResponseCode() == -1) {
                responseUtil.sendError(
                        exchange,
                        500,
                        "An internal server error occurred."
                );
            }
        }
    }


    private void handleCreatePassenger(
            HttpExchange exchange
    ) throws IOException {

        AccountContext account = resolveAccount(exchange);

        if (account == null) {
            responseUtil.sendError(
                    exchange,
                    401,
                    "Authentication required."
            );
            return;
        }

        try {
            Passenger passenger = readPassenger(exchange, 0L);

            // Passenger accounts use their authenticated account ID.
            // Staff use 0, so the passenger profile is not attributed
            // to a passenger account.
            long createdBy = account.passengerAccount()
                    ? account.accountId()
                    : 0L;

            long passengerId =
                    passengerService.addPassenger(passenger, createdBy);

            responseUtil.sendJson(
                    exchange,
                    201,
                    Map.of(
                            "message", "Passenger created successfully.",
                            "passenger_id", passengerId
                    )
            );

        } catch (JsonProcessingException exception) {
            responseUtil.sendError(
                    exchange,
                    400,
                    "Invalid JSON request body."
            );

        } catch (IllegalArgumentException | DateTimeParseException exception) {
            responseUtil.sendError(
                    exchange,
                    400,
                    exception.getMessage() == null
                            ? "Invalid passenger information."
                            : exception.getMessage()
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to create passenger.",
                    exception
            );

            responseUtil.sendError(
                    exchange,
                    500,
                    "Failed to create passenger."
            );
        }
    }


    private void handleUpdatePassenger(
            HttpExchange exchange,
            long passengerId
    ) throws IOException {

        AccountContext account = resolveAccount(exchange);

        if (account == null) {
            responseUtil.sendError(
                    exchange,
                    401,
                    "Authentication required."
            );
            return;
        }

        try {
            Passenger passenger = readPassenger(exchange, passengerId);

            // Passenger accounts must own the profile.
            // Staff bypass the passenger-account ownership check.
            long createdBy = account.passengerAccount()
                    ? account.accountId()
                    : 0L;

            passengerService.updatePassenger(passenger, createdBy);

            responseUtil.sendJson(
                    exchange,
                    200,
                    Map.of(
                            "message", "Passenger updated successfully.",
                            "passenger_id", passengerId
                    )
            );

        } catch (JsonProcessingException exception) {
            responseUtil.sendError(
                    exchange,
                    400,
                    "Invalid JSON request body."
            );

        } catch (IllegalArgumentException | DateTimeParseException exception) {
            responseUtil.sendError(
                    exchange,
                    400,
                    exception.getMessage() == null
                            ? "Invalid passenger information."
                            : exception.getMessage()
            );

        } catch (IllegalAccessException exception) {
            responseUtil.sendError(
                    exchange,
                    403,
                    "You are not authorized to update this passenger."
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to update passenger " + passengerId,
                    exception
            );

            responseUtil.sendError(
                    exchange,
                    500,
                    "Failed to update passenger."
            );
        }
    }

    private void handleGetPassenger(
            HttpExchange exchange,
            long passengerId
    ) throws IOException {

        AccountContext account = resolveAccount(exchange);

        if (account == null) {
            responseUtil.sendError(exchange, 401, "Authentication required.");
            return;
        }

        try {
            Passenger p = passengerService.getPassengerById(passengerId);

            if (p == null) {
                responseUtil.sendError(exchange, 404, "Passenger not found.");
                return;
            }

            if (account.passengerAccount()
                    && p.getCreatedBy() != account.accountId()) {
                responseUtil.sendError(exchange, 403,
                        "You are not authorized to view this passenger.");
                return;
            }

            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("passengerId", p.getPassengerId());
            data.put("firstName", p.getFirstName());
            data.put("lastName", p.getLastName());
            data.put("dateOfBirth", p.getDateOfBirth() == null ? null : p.getDateOfBirth().toString());
            data.put("gender", p.getGender());
            data.put("nationality", p.getNationality());
            data.put("phone", p.getPhone());
            data.put("email", p.getEmail());
            data.put("address", p.getAddress());
            data.put("passportNumber", p.getPassportNumber());
            data.put("passportExpiry", p.getPassportExpiry() == null ? null : p.getPassportExpiry().toString());
            data.put("passportCountry", p.getPassportCountry());

            responseUtil.sendJson(exchange, 200, data);

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load passenger " + passengerId, exception);
            responseUtil.sendError(exchange, 500, "Failed to load passenger.");
        }
    }
    private AccountContext resolveAccount(
            HttpExchange exchange
    ) {

        PassengerSessionManager.Session passengerSession =
                passengerResolver.apply(exchange);

        if (passengerSession != null) {
            return new AccountContext(
                    true,
                    passengerSession.accountId()
            );
        }

        EmployeeSessionManager.Session employeeSession =
                employeeResolver.apply(exchange);

        if (employeeSession != null) {
            return new AccountContext(
                    false,
                    0L
            );
        }

        return null;
    }


    private Passenger readPassenger(
            HttpExchange exchange,
            long passengerId
    ) throws IOException {

        JsonNode body = objectMapper.readTree(
                exchange.getRequestBody()
        );

        if (body == null || !body.isObject()) {
            throw new IllegalArgumentException(
                    "Request body must be a JSON object."
            );
        }

        String firstName = requiredText(body, "firstName");
        String lastName = requiredText(body, "lastName");

        LocalDate dateOfBirth = requiredDate(body, "dateOfBirth");

        String gender = requiredText(body, "gender");
        String nationality = requiredText(body, "nationality");
        String phone = requiredText(body, "phone");
        String email = requiredText(body, "email");
        String address = requiredText(body, "address");
        String passportNumber = requiredText(body, "passportNumber");

        LocalDate passportExpiry = requiredDate(body, "passportExpiry");

        String passportCountry = requiredText(body, "passportCountry");

        // The ID comes from the URL, not the request body.
        // createdBy is controlled by the backend, not the client.
        return new Passenger(
                passengerId,
                firstName,
                lastName,
                dateOfBirth,
                gender,
                nationality,
                phone,
                email,
                address,
                passportNumber,
                passportExpiry,
                passportCountry,
                0L
        );
    }


    private String requiredText(
            JsonNode body,
            String fieldName
    ) {

        JsonNode value = body.get(fieldName);

        if (value == null || !value.isTextual()
                || value.asText().isBlank()) {

            throw new IllegalArgumentException(
                    "Field '" + fieldName + "' is required."
            );
        }

        return value.asText().trim();
    }


    private LocalDate requiredDate(
            JsonNode body,
            String fieldName
    ) {

        String value = requiredText(body, fieldName);

        try {
            return LocalDate.parse(value);

        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Field '" + fieldName
                            + "' must be a valid date in YYYY-MM-DD format."
            );
        }
    }


    private record AccountContext(
            boolean passengerAccount,
            long accountId
    ) {}
}