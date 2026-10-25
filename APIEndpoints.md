# Airline Reservation System — API Endpoint Reference

**Stack:** Vanilla Java HTTP server, JDBC, Oracle 19c, Jackson  
**Base path:** `/api`  
**Format:** JSON request and response bodies unless otherwise noted.

> **Scope note:** This document is the proposed endpoint inventory for the complete system. It includes the existing `GET /api/flights` and `GET /api/employee-auth/me` routes discussed in the project, plus recommended endpoints that may not yet be implemented.

## Table of Contents

1. [Conventions](#conventions)
2. [Flights](#1-flights)
3. [Seats](#2-seats)
4. [Routes and airports](#3-routes-and-airports)
5. [Reservations](#4-reservations)
6. [Passengers](#5-passengers)
7. [Customer authentication](#6-customer-authentication)
8. [Customer accounts](#7-customer-accounts)
9. [Payments](#8-payments)
10. [Employee authentication](#9-employee-authentication)
11. [Flight administration](#10-flight-administration)
12. [Flight schedule administration](#11-flight-schedule-administration)
13. [Aircraft and seat administration](#12-aircraft-and-seat-administration)
14. [Route and airport administration](#13-route-and-airport-administration)
15. [Reservation administration](#14-reservation-administration)
16. [Employee administration](#15-employee-administration)
17. [Recommended first-release scope](#16-recommended-first-release-scope)
18. [HTTP status codes](#http-status-codes)
19. [Architecture and security rules](#architecture-and-security-rules)

## Conventions

- `{flightId}`, `{reservationId}`, and similar values are path parameters.
- `?key=value` represents a query parameter.
- **Public** means no login is required.
- **Authenticated** means a valid session is required.
- **Owner or verified guest** means the requester must demonstrate permission to access that booking.
- **Staff/Admin** means an authenticated employee with the required role and permissions.
- Endpoints listed here are a design inventory, not a claim that every route is already implemented.
- Use the actual property names and enum values defined by the Java models and Oracle schema when implementing request/response JSON.

## 1. Flights

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/flights` | Public | Search/list flights |
| `GET` | `/api/flights/{flightId}` | Public | Get details for one flight |
| `GET` | `/api/flights/{flightId}/available-seats` | Public | Get seats currently available for a flight |
| `GET` | `/api/flights/{flightId}/seats` | Public, if desired | Get a seat map; decide whether occupied-seat information is exposed |
| `GET` | `/api/routes` | Public | List routes |
| `GET` | `/api/airports` | Public | List airports |
| `GET` | `/api/airports/{airportId}` | Public | Get airport details |

### Flight search query parameters

Potential parameters for `GET /api/flights`:

| Parameter | Meaning |
|---|---|
| `departureCountry` | Filter by departure country |
| `arrivalCountry` | Filter by arrival country |
| `departureAirportId` | Filter by departure airport |
| `arrivalAirportId` | Filter by arrival airport |
| `departureDate` | Filter by departure date, e.g. `2026-11-15` |
| `passengers` | Number of passengers needing seats |

Implement only filters supported by your actual query and schema.

**Example**

```http
GET /api/flights?departureCountry=Afghanistan&arrivalCountry=Turkey&departureDate=2026-11-15
```

**Get one flight**

```http
GET /api/flights/1
```

Illustrative response:

```json
{
  "flightId": 1,
  "flightNumber": "FL1001",
  "departureTime": "2026-11-15T08:00:00",
  "arrivalTime": "2026-11-15T11:30:00",
  "status": "SCHEDULED"
}
```

## 2. Seats

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/flights/{flightId}/available-seats` | Public | Return seats available on this particular flight |
| `GET` | `/api/flights/{flightId}/seats` | Public, optional | Return the seat map, with an intentional policy for occupied seats |

Example:

```http
GET /api/flights/1/available-seats
```

Illustrative response:

```json
{
  "flightId": 1,
  "availableSeats": [
    {
      "seatId": 12,
      "seatNumber": "3A",
      "seatClass": "ECONOMY"
    },
    {
      "seatId": 13,
      "seatNumber": "3B",
      "seatClass": "ECONOMY"
    }
  ]
}
```

**Rules**
- Availability is per flight, not merely per aircraft.
- The database/service must prevent the same seat from being assigned twice on the same flight, including under concurrent requests.
- Availability results can become stale; validate seat availability again inside the reservation transaction.
- A cancelled reservation should not continue to block a seat if the business rules release its seats.

## 3. Routes and airports

### Public routes and airports

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/routes` | Public | List routes |
| `GET` | `/api/airports` | Public | List airports |
| `GET` | `/api/airports/{airportId}` | Public | Get airport details |

Administrative create/update endpoints are listed in [Route and airport administration](#13-route-and-airport-administration).

## 4. Reservations

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/reservations` | Public guest flow or authenticated customer | Create a reservation |
| `GET` | `/api/reservations/{reservationId}` | Owner or verified guest; authorized staff | Retrieve a reservation |
| `GET` | `/api/reservations/reference/{bookingReference}` | Owner/verified guest; authorized staff | Retrieve a reservation by booking reference |
| `POST` | `/api/reservations/{reservationId}/cancel` | Owner or verified guest; authorized staff | Cancel a reservation |
| `GET` | `/api/reservations/{reservationId}/passengers` | Owner or verified guest; authorized staff | List passengers for a reservation |
| `GET` | `/api/reservations/{reservationId}/segments` | Owner or verified guest; authorized staff | List flight segments |
| `GET` | `/api/reservations/{reservationId}/payments` | Owner or verified guest; authorized staff | List payments associated with the reservation |

Avoid adding separate passenger/segment routes if the reservation response already contains that data.

### Create a reservation

```http
POST /api/reservations
Content-Type: application/json
```

Illustrative request:

```json
{
  "passengers": [
    {
      "firstName": "Ahmad",
      "lastName": "Khan",
      "passportNumber": "P1234567",
      "dateOfBirth": "2000-05-10"
    },
    {
      "firstName": "Ali",
      "lastName": "Khan",
      "passportNumber": "P7654321",
      "dateOfBirth": "2002-08-20"
    }
  ],
  "segments": [
    {
      "flightId": 1,
      "passengers": [
        {
          "passportNumber": "P1234567",
          "seatId": 12
        },
        {
          "passportNumber": "P7654321",
          "seatId": 13
        }
      ]
    }
  ]
}
```

This is an illustrative contract. Adapt it to your `ReservationService`, `Passenger` model, `RESERVATION_SEGMENT`, and `BOOKING_SEGMENT` tables.

**Important identity rule:** Do not trust a `contactCustomerId` sent by the browser to establish reservation ownership. For an authenticated customer, derive the customer identity from the server-side session. If guest bookings are supported, define a separate verification flow.

### Reservation creation transaction

The service should coordinate the following in one database transaction:

1. Validate the request and required fields.
2. Validate the flight and passengers.
3. Check the requested seats.
4. Create the reservation.
5. Create or associate passenger records.
6. Create reservation-segment records.
7. Assign seats through booking-segment records.
8. Commit only if all required operations succeed.

If any required operation fails, roll back. Enforce seat uniqueness at the database level where possible, and handle concurrency so two simultaneous requests cannot book the same seat on the same flight.

### Get a reservation

```http
GET /api/reservations/1001
```

Illustrative response:

```json
{
  "reservationId": 1001,
  "bookingReference": "AB12CD",
  "status": "CONFIRMED",
  "passengers": [],
  "segments": [],
  "totalAmount": 450.00
}
```

The empty arrays are placeholders. A real response should include the associated records.

A reservation ID or booking reference alone should not automatically grant access to sensitive passenger data. Apply ownership checks or guest-booking verification.

### Cancel a reservation

```http
POST /api/reservations/1001/cancel
Content-Type: application/json
```

Illustrative request:

```json
{
  "reason": "Change of travel plans"
}
```

Validate ownership, current reservation status, cancellation rules, and any refund/payment implications. Prefer updating the status to `CANCELLED` over deleting reservation history.

## 5. Passengers

The reservation endpoints above may be sufficient for the initial release. If passenger records need independent management, consider these routes:

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/reservations/{reservationId}/passengers` | Owner/verified guest; authorized staff | List reservation passengers |
| `GET` | `/api/reservations/{reservationId}/passengers/{passengerId}` | Owner/verified guest; authorized staff | Get a passenger within a reservation |
| `PATCH` | `/api/reservations/{reservationId}/passengers/{passengerId}` | Owner/verified guest, subject to rules | Update permitted passenger details |

Passenger information is sensitive. Limit the fields returned and ensure the passenger belongs to the specified reservation before reading or updating it. Avoid a general public endpoint that exposes passengers by ID.

## 6. Customer authentication

These endpoints are for customer accounts, if customer registration/login is part of the application.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a customer |
| `POST` | `/api/auth/login` | Public | Authenticate a customer |
| `POST` | `/api/auth/logout` | Authenticated | End the customer's session |
| `GET` | `/api/auth/me` | Authenticated | Get the current customer's identity |
| `POST` | `/api/auth/change-password` | Authenticated | Change password |

### Login example

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "customer@example.com",
  "password": "example-password"
}
```

Illustrative response:

```json
{
  "customerId": 15,
  "email": "customer@example.com",
  "role": "CUSTOMER"
}
```

With server-side sessions, deliver the session identifier through an appropriately configured cookie. Do not return the session ID in the JSON body.

## 7. Customer accounts

Include these if customers can log in and manage their accounts.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/customers/me` | Authenticated customer | Get own profile |
| `PATCH` | `/api/customers/me` | Authenticated customer | Update allowed profile fields |
| `GET` | `/api/customers/me/reservations` | Authenticated customer | List own reservations |
| `GET` | `/api/customers/me/reservations/{reservationId}` | Authenticated customer | Get one of own reservations |

The backend should derive the customer ID from the session and return only that customer's data. Do not accept a browser-supplied customer ID as proof of identity.

## 8. Payments

Implement these only if the system supports payment processing.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/reservations/{reservationId}/payments` | Reservation owner/verified guest | Initiate payment |
| `GET` | `/api/reservations/{reservationId}/payments` | Reservation owner/verified guest; authorized staff | List reservation payments |
| `GET` | `/api/payments/{paymentId}` | Payment owner or authorized staff | Get payment status |
| `POST` | `/api/payments/{paymentId}/cancel` | Authorized user/staff, subject to provider rules | Request cancellation or reversal where supported |

Example:

```http
POST /api/reservations/1001/payments
Content-Type: application/json
```

```json
{
  "paymentMethod": "CARD"
}
```

The request contract depends on the payment provider. The server must verify payment outcomes with the provider or trusted provider callback. Never allow a client request by itself to mark a payment as successful.

## 9. Employee authentication

The project already includes the route:

`GET /api/employee-auth/me`

Keep that endpoint. Recommended employee authentication routes:

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/employee-auth/login` | Public | Authenticate employee |
| `POST` | `/api/employee-auth/logout` | Authenticated employee | Invalidate employee session |
| `GET` | `/api/employee-auth/me` | Authenticated employee | Get current employee identity and role |
| `POST` | `/api/employee-auth/change-password` | Authenticated employee | Change employee password |

Illustrative `/api/employee-auth/me` response:

```json
{
  "employeeId": 10,
  "username": "employee01",
  "role": "ADMIN"
}
```

Use the real employee model and role names in the application.

**Security rules**
- The browser may use the returned role to decide which navigation items to display, but the server must independently authorize every protected request.
- Do not trust a role or employee ID supplied by the browser.
- Validate the session ID from the cookie against server-side session state.
- Use appropriate cookie flags, including `HttpOnly` and `SameSite`; use `Secure` when served over HTTPS.
- Protect state-changing cookie-authenticated requests against CSRF as appropriate.
- Invalidate sessions when an employee logs out or is deactivated.

## 10. Flight administration

These routes require an authenticated employee with the appropriate permissions.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/flights` | Staff/Admin | Search/list flights for operations |
| `POST` | `/api/admin/flights` | Authorized staff | Create a flight |
| `GET` | `/api/admin/flights/{flightId}` | Staff/Admin | Get administrative flight details |
| `PATCH` | `/api/admin/flights/{flightId}` | Authorized staff | Update permitted flight fields |
| `POST` | `/api/admin/flights/{flightId}/cancel` | Authorized staff | Cancel a flight |
| `POST` | `/api/admin/flights/{flightId}/status` | Authorized staff | Change flight status when business rules permit |

Example:

```http
POST /api/admin/flights
Content-Type: application/json
```

```json
{
  "flightNumber": "FL1002",
  "routeId": 3,
  "aircraftId": 1,
  "departureTime": "2026-11-15T14:00:00",
  "arrivalTime": "2026-11-15T17:30:00"
}
```

Validate that the route and aircraft exist and that departure and arrival times are consistent. Adapt the request to the existing `FLIGHT`, `ROUTE`, and related schema.

## 11. Flight schedule administration

Your `DAYS_OF_OPERATION`, `START_DATE`, and `END_DATE` fields belong to `FLIGHT_SCHEDULE`, rather than an individual flight instance.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/flight-schedules` | Staff/Admin | List schedules |
| `POST` | `/api/admin/flight-schedules` | Authorized staff | Create schedule |
| `GET` | `/api/admin/flight-schedules/{scheduleId}` | Staff/Admin | Get a schedule |
| `PATCH` | `/api/admin/flight-schedules/{scheduleId}` | Authorized staff | Update schedule |
| `POST` | `/api/admin/flight-schedules/{scheduleId}/deactivate` | Authorized staff | Deactivate schedule |

## 12. Aircraft and seat administration

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/aircraft` | Staff/Admin | List aircraft |
| `POST` | `/api/admin/aircraft` | Authorized staff | Register aircraft |
| `GET` | `/api/admin/aircraft/{aircraftId}` | Staff/Admin | Get aircraft details |
| `PATCH` | `/api/admin/aircraft/{aircraftId}` | Authorized staff | Update permitted aircraft fields |
| `GET` | `/api/admin/aircraft/{aircraftId}/seats` | Staff/Admin | List aircraft seats |
| `POST` | `/api/admin/aircraft/{aircraftId}/seats` | Authorized staff | Add aircraft seats |
| `PATCH` | `/api/admin/seats/{seatId}` | Authorized staff | Update permitted seat details |

Aircraft and seat changes must not silently invalidate confirmed seat assignments. Validate existing flights and reservations before changing a configuration.

## 13. Route and airport administration

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/routes` | Staff/Admin | List routes |
| `POST` | `/api/admin/routes` | Authorized staff | Create route |
| `PATCH` | `/api/admin/routes/{routeId}` | Authorized staff | Update route |
| `GET` | `/api/admin/airports` | Staff/Admin | List airports |
| `POST` | `/api/admin/airports` | Authorized staff | Create airport |
| `PATCH` | `/api/admin/airports/{airportId}` | Authorized staff | Update airport |

## 14. Reservation administration

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/reservations` | Authorized staff | Search/list reservations |
| `GET` | `/api/admin/reservations/{reservationId}` | Authorized staff | Get reservation details |
| `POST` | `/api/admin/reservations/{reservationId}/cancel` | Authorized staff | Cancel when business rules permit |
| `PATCH` | `/api/admin/reservations/{reservationId}/status` | Specifically authorized staff | Change status when business rules permit |

Potential list filters include booking reference, customer ID, flight ID, reservation status, and date. Restrict access to passenger data according to employee role and operational need.

## 15. Employee administration

These endpoints are for administrators who manage employee accounts.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/employees` | Admin/authorized HR role | List employees |
| `POST` | `/api/admin/employees` | Admin/authorized HR role | Create employee account |
| `GET` | `/api/admin/employees/{employeeId}` | Admin/authorized HR role | Get employee details |
| `PATCH` | `/api/admin/employees/{employeeId}` | Admin/authorized HR role | Update permitted employee fields |
| `POST` | `/api/admin/employees/{employeeId}/deactivate` | Admin/authorized HR role | Deactivate employee account |
| `PATCH` | `/api/admin/employees/{employeeId}/role` | Specifically authorized admin | Change employee role |

Only appropriately authorized administrators should grant elevated roles. Deactivation should invalidate active sessions or make them fail authorization checks.

## 16. Recommended first-release scope

Implement these first, rather than building the entire proposed API at once.

| Priority | Method | Endpoint | Purpose |
|---:|---|---|---|
| 1 | `GET` | `/api/flights` | Search flights |
| 2 | `GET` | `/api/flights/{flightId}` | Flight details |
| 3 | `GET` | `/api/flights/{flightId}/available-seats` | Seat availability |
| 4 | `POST` | `/api/reservations` | Create reservation |
| 5 | `GET` | `/api/reservations/{reservationId}` | Retrieve reservation |
| 6 | `POST` | `/api/reservations/{reservationId}/cancel` | Cancel reservation |
| 7 | `POST` | `/api/employee-auth/login` | Employee login |
| 8 | `POST` | `/api/employee-auth/logout` | Employee logout |
| 9 | `GET` | `/api/employee-auth/me` | Current employee identity; already discussed in the project |
| 10 | `GET` | `/api/admin/flights` | List/manage flights |
| 11 | `POST` | `/api/admin/flights` | Create flight |
| 12 | `PATCH` | `/api/admin/flights/{flightId}` | Update flight |
| 13 | `POST` | `/api/admin/flights/{flightId}/cancel` | Cancel flight |

Add customer accounts, payments, schedules, aircraft management, and employee management when the relevant features are implemented.

## HTTP status codes

| Status | Meaning | Typical use |
|---|---|---|
| `200 OK` | Request succeeded | Successful GET or update returning a body |
| `201 Created` | Resource created | Reservation or flight created |
| `204 No Content` | Succeeded without response body | Successful operation with no body |
| `400 Bad Request` | Invalid request | Malformed JSON or invalid fields |
| `401 Unauthorized` | Authentication required or invalid | Missing/invalid session |
| `403 Forbidden` | Authenticated but not permitted | Wrong role or insufficient permission |
| `404 Not Found` | Resource not found or not accessible | Unknown flight/reservation |
| `409 Conflict` | Conflict with current state | Seat already taken or invalid state transition |
| `500 Internal Server Error` | Unexpected server failure | Unhandled internal error |

Return a consistent JSON error format, for example:

```json
{
  "error": "SEAT_UNAVAILABLE",
  "message": "The requested seat is no longer available."
}
```

Do not return stack traces, database credentials, SQL text, or sensitive internal details to clients.

## Architecture and security rules

Recommended request flow:

```text
Browser
  |
  | HTTP + JSON
  v
AirlineHttpServer
  |
  v
Controllers
  |
  v
Services
  |
  v
DAOs
  |
  v
Oracle 19c
```

Responsibilities:

- **`AirlineHttpServer`:** Register HTTP contexts and route requests to handlers/controllers.
- **Controllers:** Parse HTTP input, validate request shape, invoke services, and produce JSON/status codes.
- **Services:** Enforce business rules, authorization requirements, and transaction boundaries.
- **DAOs:** Execute SQL and map database records to Java models.
- **Session management:** Authenticate requests using server-side session state; do not trust client-supplied roles.
- **Transactions:** Keep reservation creation and seat assignment in one transaction and roll back if any required step fails.
- **Authorization:** Check access to each reservation and passenger record; knowing an ID is not proof of ownership.
- **Input validation:** Validate path parameters, query parameters, JSON fields, enum values, and date/time ranges.
- **Database safety:** Use prepared statements and enforce important integrity rules in Oracle as well as in Java.
- **Implementation status:** Mark each route in project documentation as `Planned`, `Implemented`, or `Tested` as development progresses.

This document is the endpoint plan. Request/response fields and access policies should be finalized against the actual Java models, Oracle schema, and business rules before implementation.
