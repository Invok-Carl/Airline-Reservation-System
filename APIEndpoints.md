========================================================
flights:
========================================================
/api/flights
1    GET /api/flights
2    GET /api/flights/{flightId}
3    GET /api/flights?id=1               
4    GET api/flights/{FLIGHT_ID}/available-seats
5    GET api/flights/{FLIGHT_ID}/seats get all seats for flight
6    GET /api/flights?departureCountry=...&arrivalCountry=...
    
    public Flight{
        private long flight_id
        private LocalDate flight_date
        private String departure_airport
        private String arrival_airport
        private long aircraft_id;
        private LocalTime sched_departure
        private LocalTime sched_arrival
        private LocalTime actual_departure
        private LocalTime actual_arrival
        private String flight_status
        private long schedule_id
    }

    POST /api/flights -> only happens with a supplied valid session

    public record AddFlightRequest(
    LocalDate flight_date,
    Long aircraft_id,
    LocalTime actual_departure,
    LocalTime actual_arrival,
    String flight_status,
    long schedule_id
    ) {}

=========================================================================
reservation API:
=========================================================================
api/reservations, all operations require a valid session
1    POST api/reservatoins
2    POST /api/reservations/{id}/segments -> provide reservation id to add segment(s)
    [{
        "segments": [
                        {
                        "passenger_id": 12,
                        "flight_id": 1,
                        "seat_id": 1653
                        },
                        {
                        "passenger_id": 13,
                        "flight_id": 4,
                        "seat_id": 1532
                        }...
                    ]
    }
3    DELETE /api/reservations/{id}/segments/{segmentId} -> remove a segment

==================================================================
Employee Auth API:
==================================================================
 /api/employee-auth
1    POST api/employee-auth/login -> creates a session and returns a cookie
    public record LoginRequest(
    String username,
    String password
    ) {
    }
 2   GET api/employee-auth/me    -> returns cookie for current session
 3   POST api/employee-auth/logout -> removes session and resets the cookie

    public Employee(
            long employeeId, String firstName, String lastName, String position,
            String phone, String email, String employmentStatus, String userName, String passwordHash
            ) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.phone = phone;
        this.email = email;
        this.employmentStatus = employmentStatus;
        this.userName = userName;
        this.passwordHash = passwordHash;
    }
===================================================================
Passenger Account auth:
===================================================================
 api/auth
    POST api/auth/register -> register and creates a session
    POST api/auth/login    -> creates a session
    POST api/auth/logout   -> removes the session
    GET  api/auth/me       -> returns the cookie (if available)

    public PassengerAccount(long accountId, long passengerId,
    String email, String passwordHash,
    String accountStatus, LocalDateTime createdAt) {
    this.accountId = accountId;
    this.passengerId = passengerId;
    this.email = email;
    this.passwordHash = passwordHash;
    this.accountStatus = accountStatus;
    this.createdAt = createdAt;
    }

========================================================================
Passengers API:
==============================================================
/api/passengers
    POST    api/passengers{passengerId} -> only admin/passengers with accounts are allowed
    PUT     api/passengers/{passengerId} -> admins/reservation agents, or passengers with accounts
                    (who have created the passenger in the first place) are allowed.
    accounts to whom the passenger belongs

    NOTE: passenger ID must be provided in the URL.
    public Passenger(Long passengerId, String firstName, String lastName,
    LocalDate dateOfBirth, String gender, String nationality,
    String phone, String email, String address,
    String passportNumber, LocalDate passportExpiry,
    String passportCountry, long createdBy) {

        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.passportNumber = passportNumber;
        this.passportExpiry = passportExpiry;
        this.passportCountry = passportCountry;
    }
===================================================================================
aircraft API
===========================================================================
GET	     /api/aircraft	     Retrieve all aircraft
GET	     /api/aircraft/{id}	 Retrieve one aircraft
POST	 /api/aircraft	     Register a new aircraft
PUT	     /api/aircraft/{id}	 Update aircraft information
    
    