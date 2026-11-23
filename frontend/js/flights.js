
"use strict";

const API_URL = "/api/flights";

const searchForm = document.getElementById("flight-search-form");
const departureInput = document.getElementById("departure-country");
const arrivalInput = document.getElementById("arrival-country");
const searchButton = document.getElementById("search-button");
const showAllButton = document.getElementById("show-all-button");

const flightList = document.getElementById("flight-list");
const resultsCount = document.getElementById("results-count");
const loadingState = document.getElementById("loading-state");
const emptyState = document.getElementById("empty-state");
const pageMessage = document.getElementById("page-message");

const bookingPanel = document.getElementById("booking-panel");
const selectedFlightDetails =
    document.getElementById("selected-flight-details");
const closeBookingButton =
    document.getElementById("close-booking-button");
const seatMessage = document.getElementById("seat-message");
const seatLoading = document.getElementById("seat-loading");
const seatList = document.getElementById("seat-list");
const seatEmptyState = document.getElementById("seat-empty-state");
const selectedSeatLabel =
    document.getElementById("selected-seat-label");
const confirmBookingButton =
    document.getElementById("confirm-booking-button");

let currentFlight = null;
let selectedSeat = null;
let passenger = null;
let bookingInProgress = false;

async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        credentials: "same-origin",
        headers: {
            "Accept": "application/json",
            ...(options.body
                ? { "Content-Type": "application/json" }
                : {}),
            ...options.headers
        },
        ...options
    });

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
        ? await response.json()
        : await response.text();

    if (!response.ok) {
        const message = typeof data === "object" && data !== null
            ? data.error || data.message
            : data;

        throw new Error(message || `Request failed (${response.status}).`);
    }

    return data;
}

async function fetchFlights(url = API_URL) {
    setLoading(true);
    clearMessage();
    flightList.replaceChildren();
    emptyState.classList.add("hidden");
    resultsCount.textContent = "Loading...";

    try {
        const data = await requestJson(url);

        if (!Array.isArray(data)) {
            throw new Error(
                "The server returned an unexpected flight response."
            );
        }

               displayFlights(data.map(f => ({
                   flight_id: f.flightId,
                   flight_date: f.flightDate,
                   departure_airport: f.departureAirport,
                   arrival_airport: f.arrivalAirport,
                   aircraft_id: f.aircraftId,
                   sched_departure: f.schedDeparture,
                   sched_arrival: f.schedArrival,
                   flight_status: f.flightStatus
               })));

    } catch (error) {
        console.error("Failed to load flights:", error);

        resultsCount.textContent = "Unavailable";
        showMessage(error.message);

    } finally {
        setLoading(false);
    }
}

function displayFlights(flights) {
    flightList.replaceChildren();

    resultsCount.textContent =
        `${flights.length} flight${flights.length === 1 ? "" : "s"}`;

    if (flights.length === 0) {
        emptyState.classList.remove("hidden");
        return;
    }

    emptyState.classList.add("hidden");

    const fragment = document.createDocumentFragment();

    for (const flight of flights) {
        fragment.appendChild(createFlightCard(flight));
    }

    flightList.appendChild(fragment);
}

function createFlightCard(flight) {
    const card = document.createElement("article");
    card.className = "flight-card";

    const main = document.createElement("div");
    main.className = "flight-card-main";

    const top = document.createElement("div");
    top.className = "flight-card-top";

    const flightNumber = document.createElement("span");
    flightNumber.className = "flight-number";
    flightNumber.textContent =
        `Flight ${flight.flight_number ?? flight.flight_id ?? "—"}`;

    const flightDate = document.createElement("span");
    flightDate.className = "flight-date";
    flightDate.textContent = formatDate(flight.flight_date);

    top.append(flightNumber, flightDate);

    const route = document.createElement("div");
    route.className = "flight-route";

    route.append(
        createRoutePoint(
            formatTime(flight.sched_departure),
            flight.departure_airport || "Departure airport"
        ),
        createRouteLine(),
        createRoutePoint(
            formatTime(flight.sched_arrival),
            flight.arrival_airport || "Arrival airport"
        )
    );

    main.append(top, route);

    const side = document.createElement("div");
    side.className = "flight-card-side";

    const status = document.createElement("span");
    const normalizedStatus =
        String(flight.flight_status || "UNKNOWN").toLowerCase();

    status.className =
        `status-badge status-${getStatusClass(normalizedStatus)}`;

    status.textContent =
        String(flight.flight_status || "Unknown").replaceAll("_", " ");

    const aircraft = document.createElement("span");
    aircraft.className = "aircraft-info";
    aircraft.textContent = `Aircraft: ${flight.aircraft_id ?? "—"}`;

    const bookButton = document.createElement("button");
    bookButton.type = "button";
    bookButton.className = "button button-primary";
    bookButton.textContent = "Book flight";
    bookButton.addEventListener("click", () => openBooking(flight));

    side.append(status, aircraft, bookButton);
    card.append(main, side);

    return card;
}

function createRoutePoint(time, airport) {
    const point = document.createElement("div");
    point.className = "route-point";

    const timeElement = document.createElement("span");
    timeElement.className = "route-time";
    timeElement.textContent = time;

    const airportElement = document.createElement("span");
    airportElement.className = "route-airport";
    airportElement.textContent = airport;

    point.append(timeElement, airportElement);

    return point;
}

function createRouteLine() {
    const line = document.createElement("div");
    line.className = "route-line";
    line.setAttribute("aria-label", "Flight route");

    const plane = document.createElement("span");
    plane.className = "route-plane";
    plane.setAttribute("aria-hidden", "true");
    plane.textContent = "✈";

    line.appendChild(plane);
    return line;
}

async function openBooking(flight) {
    clearSeatMessage();
    currentFlight = flight;
    selectedSeat = null;

    seatList.replaceChildren();
    seatEmptyState.classList.add("hidden");
    selectedSeatLabel.textContent = "None";
    confirmBookingButton.disabled = true;

    selectedFlightDetails.textContent =
        `${flight.flight_number ?? `Flight ${flight.flight_id}`} · ` +
        `${formatDate(flight.flight_date)} · ` +
        `${flight.departure_airport || "Departure"} → ` +
        `${flight.arrival_airport || "Arrival"}`;

    bookingPanel.classList.remove("hidden");
    bookingPanel.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    confirmBookingButton.disabled = true;
    seatLoading.classList.remove("hidden");

    try {
        // Require a passenger login before allowing booking.
        if (!passenger) {
            passenger = await requestJson("/api/auth/me");
        }

        if (!passenger.passengerId) {
            throw new Error(
                "Your account has no associated passenger profile."
            );
        }

        const response = await requestJson(
            `/api/flights/${encodeURIComponent(flight.flight_id)}/seats`
        );

        const seats = normalizeSeats(response);

        if (seats.length === 0) {
            seatEmptyState.classList.remove("hidden");
            return;
        }

        displaySeats(seats);

    } catch (error) {
        if (error.message.includes("401")) {
            window.location.href = "/pages/login.html";
            return;
        }

        showSeatMessage(error.message);

    } finally {
        seatLoading.classList.add("hidden");
    }
}

function normalizeSeats(response) {
    // The API returns { flightId: ..., seats: [...] }.
    if (response && !Array.isArray(response) &&
        Array.isArray(response.seats)) {
        return response.seats;
    }

    // Also support APIs that return an array directly.
    if (Array.isArray(response)) {
        return response;
    }

    // Support APIs that return a map/object of seat records.
    if (response && typeof response === "object") {
        return Object.values(response).filter(value =>
            value &&
            typeof value === "object" &&
            !Array.isArray(value) &&
            (value.seat_id !== undefined ||
             value.seatId !== undefined)
        );
    }

    return [];
}

function getSeatId(seat) {
    return seat.seat_id ?? seat.seatId;
}

function getSeatLabel(seat) {
    return seat.seat_number
        ?? seat.seatNumber
        ?? `Seat ${getSeatId(seat)}`;
}

function displaySeats(seats) {
    seatList.replaceChildren();

    for (const seat of seats) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "seat-button";
        button.textContent = getSeatLabel(seat);

        button.addEventListener("click", () => {
            selectedSeat = seat;

            for (const item of seatList.querySelectorAll(".seat-button")) {
                item.classList.remove("selected");
                item.setAttribute("aria-pressed", "false");
            }

            button.classList.add("selected");
            button.setAttribute("aria-pressed", "true");

            selectedSeatLabel.textContent = getSeatLabel(seat);
            confirmBookingButton.disabled = false;
        });

        button.setAttribute("aria-pressed", "false");
        seatList.appendChild(button);
    }
}

async function confirmBooking() {
    console.log("Confirm reservation clicked");
    if (!currentFlight || !selectedSeat || bookingInProgress) {
        return;
    }

    bookingInProgress = true;
    confirmBookingButton.disabled = true;
    confirmBookingButton.textContent = "Booking...";

    try {
        if (!passenger) {
            passenger = await requestJson("/api/auth/me");
        }

        const result = await requestJson("/api/reservations", {
            method: "POST",
            body: JSON.stringify({
                segments: [{
                    passenger_id: passenger.passengerId,
                    flight_id: currentFlight.flight_id,
                    seat_id: getSeatId(selectedSeat)
                }]
            })
        });

        const reservationId =
            result.reservation_id ?? result.reservationId;

        showSeatMessage(
            reservationId
                ? `Reservation created successfully. Reservation ID: ${reservationId}`
                : "Reservation created successfully."
        );

        seatList.replaceChildren();
        selectedSeat = null;
        selectedSeatLabel.textContent = "None";

        // Refresh seats to reflect the booking.
        const response = await requestJson(
            `/api/flights/${encodeURIComponent(currentFlight.flight_id)}/seats`
        );

        const seats = normalizeSeats(response);

        if (seats.length === 0) {
            seatEmptyState.classList.remove("hidden");
        } else {
            seatEmptyState.classList.add("hidden");
            displaySeats(seats);
        }

        selectedSeat = null;
        selectedSeatLabel.textContent = "None";
        confirmBookingButton.disabled = true;

    } catch (error) {
        showSeatMessage(error.message);

    } finally {
        bookingInProgress = false;
        confirmBookingButton.textContent = "Confirm reservation";
        confirmBookingButton.disabled = !selectedSeat;
    }
}

function formatDate(value) {
    if (!value) {
        return "Date unavailable";
    }

    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);

    if (!match) {
        return String(value);
    }

    const [, year, month, day] = match;

    return new Date(
        Number(year),
        Number(month) - 1,
        Number(day)
    ).toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric"
    });
}

function formatTime(value) {
    if (!value) {
        return "—";
    }

    const match =
        /^(\d{2}):(\d{2})(?::\d{2}(?:\.\d+)?)?$/.exec(value);

    if (!match) {
        return String(value);
    }

    return `${match[1]}:${match[2]}`;
}

function getStatusClass(status) {
    const supportedStatuses = [
        "scheduled", "confirmed", "active",
        "cancelled", "delayed"
    ];

    return supportedStatuses.includes(status) ? status : "unknown";
}

function setLoading(isLoading) {
    loadingState.classList.toggle("hidden", !isLoading);
    searchButton.disabled = isLoading;
    showAllButton.disabled = isLoading;
}

function showMessage(message) {
    pageMessage.textContent = message;
    pageMessage.classList.remove("hidden");
}

function clearMessage() {
    pageMessage.textContent = "";
    pageMessage.classList.add("hidden");
}

function showSeatMessage(message) {
    seatMessage.textContent = message;
    seatMessage.classList.remove("hidden");
}

function clearSeatMessage() {
    seatMessage.textContent = "";
    seatMessage.classList.add("hidden");
}

searchForm.addEventListener("submit", async event => {
    event.preventDefault();

    const departureCountry = departureInput.value.trim();
    const arrivalCountry = arrivalInput.value.trim();

    if (!departureCountry || !arrivalCountry) {
        showMessage("Enter both departure and arrival countries.");
        return;
    }

    const params = new URLSearchParams({
        departureCountry,
        arrivalCountry
    });

    await fetchFlights(`${API_URL}?${params.toString()}`);
});

showAllButton.addEventListener("click", async () => {
    searchForm.reset();
    await fetchFlights();
});

closeBookingButton.addEventListener("click", () => {
    bookingPanel.classList.add("hidden");
    currentFlight = null;
    selectedSeat = null;
});

confirmBookingButton.addEventListener("click", confirmBooking);

fetchFlights();
