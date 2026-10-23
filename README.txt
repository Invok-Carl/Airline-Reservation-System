2. Exact functionalities for each DAO
A. Administration and reference data
These DAOs manage the information required to configure the airline system.

AirlineDAO.java
- createAirline(...)
- getAirlineById(long airlineId)
- getAllAirlines()
- updateAirline(...)
- deleteAirline(long airlineId)
Manage airline name, code, contact details,
and other airline attributes defined in your schema.




EmployeeDAO.java
- createEmployee(...)
- getEmployeeById(long employeeId)
- getEmployeeByUsername(String username)
- getAllEmployees()
- updateEmployee(...)
- deleteEmployee(long employeeId)
Your authentication service can use this DAO to retrieve an employee and
 verify the stored password hash. Never store or retrieve plaintext passwords.




AirportDAO.java
- createAirport(...)
- getAirportById(long airportId)
- getAllAirports()
- searchAirports(String keyword)
- updateAirport(...)
- deleteAirport(long airportId)
Search airports by code, name, or city if those columns exist.
 Check references before deleting an airport.




RouteDAO.java
- createRoute(...)
- getRouteById(long routeId)
- getAllRoutes()
- getRoutesByOrigin(long airportId)
- getRoutesByDestination(long airportId)
- updateRoute(...)
- deleteRoute(long routeId)
Retrieve routes using their origin and destination airports.
Route creation should validate that the referenced airports exist.




AircraftDAO.java
- createAircraft(...)
- getAircraftById(long aircraftId)
- getAllAircraft()
- updateAircraft(...)
- deleteAircraft(long aircraftId)
- getAircraftByAirline(long airlineId)
Manage aircraft details and retrieve aircraft belonging to an airline, where applicable.




SeatDAO.java
- createSeat(...)
- getSeatById(long seatId)
- getSeatsByAircraft(long aircraftId)
- getSeatsByFlight(long flightId)
- getAvailableSeatsByFlight(long flightId)
- updateSeat(...)
- deleteSeat(long seatId)
Your existing DAO already handles flight-seat retrieval.
 Keep seat availability queries here, but make the reservation service responsible for actually reserving a seat safely.



B. Flight management
These DAOs handle schedule templates, actual flight instances, and prices.

FlightScheduleDAO.java
- createFlightSchedule(...)
- getFlightScheduleById(long scheduleId)
- getAllFlightSchedulesList(Connection connection) — your existing method
- updateFlightSchedule(...)
- updateScheduleStatus(...)
- deleteFlightSchedule(long scheduleId)
Manage recurring templates: flight number, route,
operating days, start/end dates, departure time, arrival time, and schedule status.




FlightDAO.java
- createFlight(...)
- getFlightById(long flightId)
- getAllFlights()
- searchFlights(origin, destination, date)
- getFlightsByDate(LocalDate date)
- getFlightsBySchedule(long scheduleId)
- updateFlight(...)
- updateFlightStatus(long flightId, String status)
- deleteFlight(long flightId)
Manage individual flights with actual dates, departure/arrival timestamps,
aircraft assignments, and flight status.
This DAO should support the manage-flight page you are building.




FareDAO.java
- createFare(...)
- getFareById(long fareId)
- getFaresByFlight(long flightId)
- getActiveFaresByFlight(long flightId)
- updateFare(...)
- deleteFare(long fareId)
Retrieve and maintain prices and fare categories according to your actual FARE columns.
If fares depend on passenger type, cabin class, or validity dates,
include those criteria in the relevant queries.



Important distinction: FlightScheduleDAO manages a template;
FlightDAO manages a particular flight on a particular date.
Generating flights from a schedule should be orchestrated by a service,
which calls the appropriate DAOs.
C. Reservations and passenger management
These are the most important DAOs for the booking workflow.

PassengerDAO.java
- createPassenger(...)
- getPassengerById(long passengerId)
- getPassengerByPassport(String passportNumber)
- getAllPassengers()
- searchPassengers(String keyword)
- updatePassenger(...)
- deletePassenger(long passengerId)
Manage passenger identity and contact information.
 Avoid deleting passengers who are referenced by existing reservations or tickets.




ReservationDAO.java
- createReservation(...)
- getReservationById(long reservationId)
- getReservationByReference(String bookingReference)
- getReservationsByCustomer(long customerId)
- getAllReservations()
- updateReservationStatus(...)
- updateReservationTotal(...)
- deleteReservation(long reservationId) — only where business rules permit
Manage the reservation header, customer association, booking reference, status,
total amount, and reservation dates if present in your schema.




BookingSegmentDAO.java
- createBookingSegment(...)
- getBookingSegmentById(long segmentId)
- getSegmentsByReservation(long reservationId)
- getSegmentsByFlight(long flightId)
- assignSeat(long segmentId, long seatId)
- updateBookingSegment(...)
- deleteBookingSegment(long segmentId) — subject to booking rules
Manage each reservation's individual flight segment, including its flight,
passenger association where applicable, and assigned seat. Your existing seat-allocation
logic should use this DAO.



A key design point for your schema: a reservation and a booking segment are not the same thing.
 One reservation can contain multiple flight segments. If your schema allows multiple passengers in one reservation,
 ensure the passenger-to-segment relationship is represented correctly; do not assume every
  segment belongs to exactly one passenger unless your foreign keys enforce that.
D. Tickets, payments, and airport operations

TicketDAO.java
- createTicket(...)
- getTicketById(long ticketId)
- getTicketByNumber(String ticketNumber)
- getTicketsByReservation(long reservationId)
- getTicketsByPassenger(long passengerId)
- updateTicketStatus(...)
- deleteTicket(long ticketId) — only if permitted
Store and retrieve issued tickets, their associated booking/flight information,
 ticket numbers, and statuses according to your schema.




PaymentDAO.java
- createPayment(...)
- getPaymentById(long paymentId)
- getPaymentsByReservation(long reservationId)
- getAllPayments()
- updatePaymentStatus(...)
- getPaidAmountByReservation(long reservationId)
Record payment amounts, methods, dates, and statuses.
 The service should enforce payment rules and decide when a reservation can become confirmed.




CancellationDAO.java
- createCancellation(...)
- getCancellationById(long cancellationId)
- getCancellationsByReservation(long reservationId)
- getAllCancellations()
- updateCancellation(...) — if permitted by the business rules
Store cancellation records, reasons, timestamps,
and refund details if your CANCELLATION table contains them.
 The service should coordinate cancellation, seat release, ticket updates, and refunds.




CheckInDAO.java
- createCheckIn(...)
- getCheckInById(long checkInId)
- getCheckInsByTicket(long ticketId)
- getCheckInsByFlight(long flightId)
- updateCheckInStatus(...)
Record check-in time, status, and other fields your schema supports.
Eligibility checks and duplicate-check-in prevention belong in the service
 and database constraints.




BaggageDAO.java
- createBaggage(...)
- getBaggageById(long baggageId)
- getBaggageByTicket(long ticketId)
- getBaggageByPassenger(long passengerId) — if supported by the schema
- updateBaggageStatus(...)
- deleteBaggage(long baggageId) — subject to business rules
Manage baggage registration, weight, tags,
and handling status according to your actual Baggage columns.