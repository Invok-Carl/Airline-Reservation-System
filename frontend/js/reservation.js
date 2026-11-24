document.addEventListener("DOMContentLoaded", () => {
    const API_BASE = "/api";

    const elements = {
        selectedFlightList: document.getElementById("selected-flight-list"),

        passengerSelect: document.getElementById("passenger-select"),
        addPassengerButton: document.getElementById("add-passenger-button"),
        selectedPassengerList: document.getElementById("selected-passenger-list"),
        passengerCount: document.getElementById("passenger-count"),
        passengerMessage: document.getElementById("passenger-message"),

        seatMessage: document.getElementById("seat-message"),
        seatFlightSelect: document.getElementById("seat-flight-select"),
        seatPassengerSelect: document.getElementById("seat-passenger-select"),
        loadSeatsButton: document.getElementById("load-seats-button"),
        seatLoading: document.getElementById("seat-loading"),
        seatSelectionPanel: document.getElementById("seat-selection-panel"),
        seatList: document.getElementById("seat-list"),
        selectedSeatLabel: document.getElementById("selected-seat-label"),
        confirmSeatButton: document.getElementById("confirm-seat-button"),
        seatEmptyState: document.getElementById("seat-empty-state"),
        seatAssignmentList: document.getElementById("seat-assignment-list"),

        reservationReview: document.getElementById("reservation-review"),
        reviewMessage: document.getElementById("review-message"),
        cancelReservationButton: document.getElementById("cancel-reservation-button"),
        createReservationButton: document.getElementById("create-reservation-button"),
        reservationLoading: document.getElementById("reservation-loading")
    };

    let reservation = ReservationStorage.getOrCreate();

    let availableSeats = [];
    let selectedSeatId = null;

    let passengersFromServer = [];

    initialize();

    // --------------------------------------------------
    // Initialization
    // --------------------------------------------------

    async function initialize() {
        if (!Array.isArray(reservation.flights) ||
            reservation.flights.length === 0) {

            showMessage(
                elements.reviewMessage,
                "No flights have been selected. Return to the flight search page.",
                "error"
            );

            disableReservationControls();
            return;
        }

        if (!Array.isArray(reservation.passengers)) {
            reservation.passengers = [];
        }

        reservation.flights.forEach(flight => {
            if (!flight.seats) {
                flight.seats = {};
            }
        });

        ReservationStorage.save(reservation);

        renderSelectedFlights();
        renderSelectedPassengers();
        populateSeatFlightSelect();
        populateSeatPassengerSelect();
        renderSeatAssignments();
        renderReview();
        updateSeatLoadButton();

        attachEventListeners();

        await loadPassengers();
    }

    function attachEventListeners() {
        elements.addPassengerButton?.addEventListener(
            "click",
            addPassenger
        );

        elements.loadSeatsButton?.addEventListener(
            "click",
            loadAvailableSeats
        );

        elements.confirmSeatButton?.addEventListener(
            "click",
            confirmSeatAssignment
        );

        elements.createReservationButton?.addEventListener(
            "click",
            createReservation
        );

        elements.cancelReservationButton?.addEventListener(
            "click",
            cancelReservation
        );

        elements.seatFlightSelect?.addEventListener(
            "change",
            handleSeatSelectionChange
        );

        elements.seatPassengerSelect?.addEventListener(
            "change",
            handleSeatSelectionChange
        );
    }

    function handleSeatSelectionChange() {
        resetSeatSelection();
        updateSeatLoadButton();
    }

    function updateSeatLoadButton() {
        if (!elements.loadSeatsButton) return;

        const flightSelected = Boolean(elements.seatFlightSelect?.value);
        const passengerSelected = Boolean(elements.seatPassengerSelect?.value);

        elements.loadSeatsButton.disabled =
            !(flightSelected && passengerSelected);
    }

    function updateCreateReservationButton() {
        if (!elements.createReservationButton) return;

        elements.createReservationButton.disabled =
            validateReservation() !== null;
    }

    function disableReservationControls() {
        if (elements.addPassengerButton) {
            elements.addPassengerButton.disabled = true;
        }

        if (elements.createReservationButton) {
            elements.createReservationButton.disabled = true;
        }
    }

    // --------------------------------------------------
    // API helper
    // --------------------------------------------------

    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                ...(options.body ? {
                    "Content-Type": "application/json"
                } : {}),
                ...options.headers
            }
        });

        const responseText = await response.text();

        let data = null;

        if (responseText) {
            try {
                data = JSON.parse(responseText);
            } catch {
                data = responseText;
            }
        }

        if (!response.ok) {
            const message =
                typeof data === "object" && data !== null
                    ? data.message || data.error
                    : data;

            throw new Error(
                message || `Request failed with status ${response.status}.`
            );
        }

        return data;
    }

    // --------------------------------------------------
    // Utility functions
    // --------------------------------------------------

    function getProperty(object, camelCase, snakeCase) {
        return object?.[camelCase] ?? object?.[snakeCase];
    }

    function getPassengerId(passenger) {
        return Number(
            getProperty(passenger, "passengerId", "passenger_id")
        );
    }

    function getFlightId(flight) {
        return Number(
            getProperty(flight, "flightId", "flight_id")
        );
    }

    function getSeatId(seat) {
        const seatId = Number(getProperty(seat, "seatId", "seat_id"));

        return Number.isFinite(seatId) && seatId > 0
            ? seatId
            : null;
    }

    function getSeatLabel(seat) {
        return getProperty(seat, "seatNumber", "seat_number") ?? "Unknown seat";
    }

    function getPassengerName(passenger) {
        return `${passenger.firstName ?? passenger.first_name} ` +
               `${passenger.lastName ?? passenger.last_name}`;
    }

    function formatDate(dateValue) {
        if (!dateValue) {
            return "Date unavailable";
        }

        const date = new Date(dateValue + "T00:00:00");

        if (Number.isNaN(date.getTime())) {
            return dateValue;
        }

        return date.toLocaleDateString();
    }

    function formatTime(timeValue) {
        if (!timeValue) {
            return "Time unavailable";
        }

        return String(timeValue).substring(0, 5);
    }

    // The HTML uses class="hidden" while the JS used the `hidden` attribute.
    // Toggle both so an element is really shown/hidden either way.
    function setHidden(element, hidden) {
        if (!element) return;

        element.hidden = hidden;
        element.classList.toggle("hidden", hidden);
    }

    function showMessage(element, message, type = "info") {
        if (!element) return;

        element.textContent = message;
        element.className = `message ${type}`;
        element.hidden = false;
    }

    function clearMessage(element) {
        if (!element) return;

        element.textContent = "";
        setHidden(element, true);
    }

    function setLoading(element, loading, message = "Loading...") {
        if (!element) return;

        setHidden(element, !loading);

        if (loading) {
            element.textContent = message;
        }
    }

    function saveReservation() {
        ReservationStorage.save(reservation);
    }

    // --------------------------------------------------
    // Selected flights
    // --------------------------------------------------

    function renderSelectedFlights() {
        const container = elements.selectedFlightList;

        if (!container) return;

        container.innerHTML = "";

        if (reservation.flights.length === 0) {
            container.textContent = "No flights selected.";
            return;
        }

        reservation.flights.forEach(flight => {
            const flightId = getFlightId(flight);

            const card = document.createElement("div");
            card.className = "selected-flight-card";

            const title = document.createElement("h4");
            title.textContent = `Flight ${flightId}`;

            const route = document.createElement("p");
            route.textContent =
                `${flight.departureAirport ?? flight.departure_airport ?? "Unknown"} → ` +
                `${flight.arrivalAirport ?? flight.arrival_airport ?? "Unknown"}`;

            const date = document.createElement("p");
            date.textContent = formatDate(
                flight.flightDate ?? flight.flight_date
            );

            const time = document.createElement("p");
            time.textContent =
                `${formatTime(flight.schedDeparture ?? flight.sched_departure)} - ` +
                `${formatTime(flight.schedArrival ?? flight.sched_arrival)}`;

            card.append(title, route, date, time);
            container.appendChild(card);
        });
    }

    // --------------------------------------------------
    // Passengers
    // --------------------------------------------------

    async function loadPassengers() {
        try {
            const data = await apiRequest(`${API_BASE}/passengers`);

            passengersFromServer = Array.isArray(data)
                ? data
                : data?.passengers ?? [];

            populatePassengerSelect();

        } catch (error) {
            console.error("Failed to load passengers:", error);

            showMessage(
                elements.passengerMessage,
                error.message || "Could not load passengers.",
                "error"
            );
        }
    }

    function populatePassengerSelect() {
        const select = elements.passengerSelect;

        if (!select) return;

        select.innerHTML = "";

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Select a passenger";
        select.appendChild(placeholder);

        const selectedIds = new Set(
            reservation.passengers.map(getPassengerId)
        );

        const availablePassengers = passengersFromServer.filter(
            passenger => !selectedIds.has(getPassengerId(passenger))
        );

        availablePassengers.forEach(passenger => {
            const option = document.createElement("option");

            option.value = getPassengerId(passenger);
            option.textContent = getPassengerName(passenger);

            select.appendChild(option);
        });

        select.disabled = availablePassengers.length === 0;

        if (elements.addPassengerButton) {
            elements.addPassengerButton.disabled =
                availablePassengers.length === 0;
        }
    }

    function addPassenger() {
        clearMessage(elements.passengerMessage);

        const passengerId = Number(elements.passengerSelect?.value);

        if (!passengerId) {
            showMessage(
                elements.passengerMessage,
                "Select a passenger first.",
                "error"
            );
            return;
        }

        const passenger = passengersFromServer.find(
            item => getPassengerId(item) === passengerId
        );

        if (!passenger) {
            showMessage(
                elements.passengerMessage,
                "Passenger not found.",
                "error"
            );
            return;
        }

        const alreadyAdded = reservation.passengers.some(
            item => getPassengerId(item) === passengerId
        );

        if (alreadyAdded) {
            showMessage(
                elements.passengerMessage,
                "This passenger has already been added.",
                "error"
            );
            return;
        }

        reservation.passengers.push(passenger);

        saveReservation();

        renderSelectedPassengers();
        populatePassengerSelect();
        populateSeatPassengerSelect();
        updateSeatLoadButton();
        renderSeatAssignments();
        renderReview();

        showMessage(
            elements.passengerMessage,
            "Passenger added successfully.",
            "success"
        );
    }

    function removePassenger(passengerId) {
        reservation.passengers = reservation.passengers.filter(
            passenger => getPassengerId(passenger) !== passengerId
        );

        // Remove this passenger's seat assignments from every flight.
        reservation.flights.forEach(flight => {
            delete flight.seats[passengerId];
        });

        saveReservation();

        renderSelectedPassengers();
        populatePassengerSelect();
        populateSeatPassengerSelect();
        updateSeatLoadButton();
        renderSeatAssignments();
        renderReview();

        resetSeatSelection();
    }

    function renderSelectedPassengers() {
        const container = elements.selectedPassengerList;

        if (!container) return;

        container.innerHTML = "";

        if (reservation.passengers.length === 0) {
            container.textContent = "No passengers added.";
        }

        reservation.passengers.forEach(passenger => {
            const passengerId = getPassengerId(passenger);

            const item = document.createElement("div");
            item.className = "selected-passenger";

            const name = document.createElement("span");
            name.textContent = getPassengerName(passenger);

            const removeButton = document.createElement("button");
            removeButton.type = "button";
            removeButton.textContent = "Remove";
            removeButton.addEventListener("click", () => {
                removePassenger(passengerId);
            });

            item.append(name, removeButton);
            container.appendChild(item);
        });

        if (elements.passengerCount) {
            elements.passengerCount.textContent =
                reservation.passengers.length;
        }
    }

    // --------------------------------------------------
    // Seat selection
    // --------------------------------------------------

    function populateSeatFlightSelect() {
        const select = elements.seatFlightSelect;

        if (!select) return;

        select.innerHTML = "";

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Select a flight";
        select.appendChild(placeholder);

        reservation.flights.forEach(flight => {
            const option = document.createElement("option");

            option.value = getFlightId(flight);
            option.textContent =
                `Flight ${getFlightId(flight)}: ` +
                `${flight.departureAirport ?? flight.departure_airport} → ` +
                `${flight.arrivalAirport ?? flight.arrival_airport}`;

            select.appendChild(option);
        });
    }

    function populateSeatPassengerSelect() {
        const select = elements.seatPassengerSelect;

        if (!select) return;

        select.innerHTML = "";

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Select a passenger";
        select.appendChild(placeholder);

        reservation.passengers.forEach(passenger => {
            const option = document.createElement("option");

            option.value = getPassengerId(passenger);
            option.textContent = getPassengerName(passenger);

            select.appendChild(option);
        });
    }

    async function loadAvailableSeats() {
        clearMessage(elements.seatMessage);
        resetSeatSelection();

        const flightId = Number(elements.seatFlightSelect?.value);
        const passengerId = Number(elements.seatPassengerSelect?.value);

        if (!flightId || !passengerId) {
            showMessage(
                elements.seatMessage,
                "Select both a flight and a passenger.",
                "error"
            );
            return;
        }

        setLoading(
            elements.seatLoading,
            true,
            "Loading available seats..."
        );
        elements.loadSeatsButton.disabled = true;

        try {
            const data = await apiRequest(
                `${API_BASE}/flights/${flightId}/available-seats`
            );

            console.log("Available seats response:", data);

            availableSeats = extractSeats(data);

            if (availableSeats.length === 0) {
                setHidden(elements.seatEmptyState, false);
                return;
            }

            renderAvailableSeats(flightId, passengerId);

            setHidden(elements.seatSelectionPanel, false);

        } catch (error) {
            console.error("Failed to load available seats:", error);

            showMessage(
                elements.seatMessage,
                error.message || "Could not load available seats.",
                "error"
            );

        } finally {
            setLoading(elements.seatLoading, false);
            updateSeatLoadButton();
        }
    }

    /*
     * Accepts either:
     *   [ seat, seat, ... ]
     * or an object containing an array such as:
     *   { availableSeats: [...] }
     *
     * Adjust this function if your endpoint uses another response shape.
     */
    function extractSeats(data) {
        if (Array.isArray(data)) {
            return data;
        }

        if (data && Array.isArray(data.availableSeats)) {
            return data.availableSeats;
        }

        if (data && Array.isArray(data.available_seats)) {
            return data.available_seats;
        }

        throw new Error(
            "Invalid response from available seats endpoint."
        );
    }

    function renderAvailableSeats(flightId, passengerId) {
        const container = elements.seatList;

        if (!container) return;

        container.innerHTML = "";

        const flight = reservation.flights.find(
            item => getFlightId(item) === flightId
        );

        if (!flight) return;

        availableSeats.forEach(seat => {
            const seatId = getSeatId(seat);

            if (seatId === null) return;

            const button = document.createElement("button");

            button.type = "button";
            button.className = "seat-button";
            button.textContent = getSeatLabel(seat);
            button.dataset.seatId = seatId;

            const assignedToAnotherPassenger =
                Object.entries(flight.seats).some(
                    ([assignedPassengerId, assignedSeatId]) =>
                        Number(assignedSeatId) === seatId &&
                        Number(assignedPassengerId) !== passengerId
                );

            const currentlyAssignedToPassenger =
                Number(flight.seats[passengerId]) === seatId;

            button.disabled = assignedToAnotherPassenger;

            if (currentlyAssignedToPassenger) {
                button.classList.add("selected");
            }

            if (assignedToAnotherPassenger) {
                button.classList.add("unavailable");
            }

            button.addEventListener("click", () => {
                selectSeat(seatId, seat);
            });

            container.appendChild(button);
        });

        if (container.children.length === 0) {
            setHidden(elements.seatEmptyState, false);

            const emptyText = elements.seatEmptyState?.querySelector("p");

            if (emptyText) {
                emptyText.textContent = "No recognizable seats were returned.";
            }
        }
    }

    function selectSeat(seatId, seat) {
        selectedSeatId = seatId;

        elements.seatList
            .querySelectorAll(".seat-button")
            .forEach(button => {
                button.classList.toggle(
                    "selected",
                    Number(button.dataset.seatId) === seatId
                );
            });

        elements.selectedSeatLabel.textContent = getSeatLabel(seat);

        elements.confirmSeatButton.disabled = false;
    }

    function confirmSeatAssignment() {
        const flightId = Number(elements.seatFlightSelect?.value);
        const passengerId = Number(elements.seatPassengerSelect?.value);

        if (!flightId || !passengerId || selectedSeatId === null) {
            showMessage(
                elements.seatMessage,
                "Select a flight, passenger and seat first.",
                "error"
            );
            return;
        }

        const flight = reservation.flights.find(
            item => getFlightId(item) === flightId
        );

        if (!flight) {
            showMessage(
                elements.seatMessage,
                "Selected flight was not found.",
                "error"
            );
            return;
        }

        // Prevent two passengers from receiving the same seat on this flight.
        const seatAlreadyAssigned = Object.entries(flight.seats).some(
            ([assignedPassengerId, assignedSeatId]) =>
                Number(assignedSeatId) === selectedSeatId &&
                Number(assignedPassengerId) !== passengerId
        );

        if (seatAlreadyAssigned) {
            showMessage(
                elements.seatMessage,
                "This seat is already assigned to another passenger.",
                "error"
            );
            return;
        }

        flight.seats[passengerId] = selectedSeatId;

        saveReservation();

        renderSeatAssignments();
        renderReview();

        resetSeatSelection();

        showMessage(
            elements.seatMessage,
            "Seat assigned successfully.",
            "success"
        );
    }

    function resetSeatSelection() {
        selectedSeatId = null;
        availableSeats = [];

        if (elements.seatList) {
            elements.seatList.innerHTML = "";
        }

        if (elements.selectedSeatLabel) {
            elements.selectedSeatLabel.textContent = "None";
        }

        if (elements.confirmSeatButton) {
            elements.confirmSeatButton.disabled = true;
        }

        setHidden(elements.seatSelectionPanel, true);
        setHidden(elements.seatEmptyState, true);
    }

    // --------------------------------------------------
    // Seat assignment overview
    // --------------------------------------------------

    function renderSeatAssignments() {
        const container = elements.seatAssignmentList;

        if (!container) return;

        container.innerHTML = "";

        if (reservation.passengers.length === 0 ||
            reservation.flights.length === 0) {

            container.textContent =
                "Add passengers and select flights to assign seats.";

            return;
        }

        reservation.flights.forEach(flight => {
            const flightId = getFlightId(flight);

            const heading = document.createElement("h4");
            heading.textContent = `Flight ${flightId}`;

            container.appendChild(heading);

            reservation.passengers.forEach(passenger => {
                const passengerId = getPassengerId(passenger);

                const seatId = flight.seats[passengerId];

                const row = document.createElement("div");
                row.className = "seat-assignment-row";

                const passengerName = document.createElement("span");
                passengerName.textContent = getPassengerName(passenger);

                const seatText = document.createElement("span");
                seatText.textContent = seatId
                    ? `Seat ID: ${seatId}`
                    : "No seat assigned";

                const removeButton = document.createElement("button");
                removeButton.type = "button";
                removeButton.textContent = "Remove seat";
                removeButton.disabled = !seatId;

                removeButton.addEventListener("click", () => {
                    delete flight.seats[passengerId];

                    saveReservation();

                    renderSeatAssignments();
                    renderReview();
                });

                row.append(
                    passengerName,
                    seatText,
                    removeButton
                );

                container.appendChild(row);
            });
        });
    }

    // --------------------------------------------------
    // Reservation review
    // --------------------------------------------------

    function getReservationSegments() {
        const segments = [];

        reservation.flights.forEach(flight => {
            const flightId = getFlightId(flight);

            reservation.passengers.forEach(passenger => {
                const passengerId = getPassengerId(passenger);
                const seatId = flight.seats[passengerId];

                if (seatId) {
                    segments.push({
                        passenger_id: passengerId,
                        flight_id: flightId,
                        seat_id: Number(seatId)
                    });
                }
            });
        });

        return segments;
    }

    function renderReview() {
        const container = elements.reservationReview;

        if (!container) return;

        container.innerHTML = "";

        const segments = getReservationSegments();

        const flightCount = reservation.flights.length;
        const passengerCount = reservation.passengers.length;
        const requiredSegments = flightCount * passengerCount;

        const summary = document.createElement("p");
        summary.textContent =
            `Flights: ${flightCount} | ` +
            `Passengers: ${passengerCount} | ` +
            `Assigned seats: ${segments.length}/${requiredSegments}`;

        container.appendChild(summary);

        if (requiredSegments === 0) {
            const message = document.createElement("p");
            message.textContent =
                "Select at least one flight and one passenger.";

            container.appendChild(message);
            updateCreateReservationButton();
            return;
        }

        reservation.flights.forEach(flight => {
            const flightId = getFlightId(flight);

            const heading = document.createElement("h4");
            heading.textContent = `Flight ${flightId}`;

            container.appendChild(heading);

            reservation.passengers.forEach(passenger => {
                const passengerId = getPassengerId(passenger);
                const seatId = flight.seats[passengerId];

                const row = document.createElement("p");

                row.textContent =
                    `${getPassengerName(passenger)}: ` +
                    (seatId ? `Seat ID ${seatId}` : "Seat not assigned");

                container.appendChild(row);
            });
        });

        updateCreateReservationButton();
    }

    function validateReservation() {
        if (reservation.flights.length === 0) {
            return "Select at least one flight.";
        }

        if (reservation.passengers.length === 0) {
            return "Add at least one passenger.";
        }

        for (const flight of reservation.flights) {
            for (const passenger of reservation.passengers) {
                const passengerId = getPassengerId(passenger);
                const seatId = flight.seats[passengerId];

                if (!seatId) {
                    return (
                        `Assign a seat to ` +
                        `${getPassengerName(passenger)} ` +
                        `on flight ${getFlightId(flight)}.`
                    );
                }
            }
        }

        return null;
    }

    // --------------------------------------------------
    // Create reservation
    // --------------------------------------------------

    async function createReservation() {
        clearMessage(elements.reviewMessage);

        const validationError = validateReservation();

        if (validationError) {
            showMessage(
                elements.reviewMessage,
                validationError,
                "error"
            );
            return;
        }

        const segments = getReservationSegments();

        const requestBody = {
            segments: segments
        };

        if (elements.createReservationButton) {
            elements.createReservationButton.disabled = true;
        }

        setLoading(
            elements.reservationLoading,
            true,
            "Creating reservation..."
        );

        try {
            const response = await apiRequest(
                `${API_BASE}/reservations`,
                {
                    method: "POST",
                    body: JSON.stringify(requestBody)
                }
            );

            ReservationStorage.clear();

            showMessage(
                elements.reviewMessage,
                response?.message ||
                    "Reservation created successfully.",
                "success"
            );

            if (elements.reservationReview) {
                elements.reservationReview.innerHTML = "";
            }

            if (elements.createReservationButton) {
                elements.createReservationButton.hidden = true;
            }

            console.log("Reservation response:", response);

        } catch (error) {
            console.error("Failed to create reservation:", error);

            showMessage(
                elements.reviewMessage,
                error.message || "Failed to create reservation.",
                "error"
            );

            if (elements.createReservationButton) {
                elements.createReservationButton.disabled = false;
            }

        } finally {
            setLoading(elements.reservationLoading, false);
        }
    }

    // --------------------------------------------------
    // Cancel reservation
    // --------------------------------------------------

    function cancelReservation() {
        const confirmed = window.confirm(
            "Are you sure you want to cancel this reservation process?"
        );

        if (!confirmed) return;

        ReservationStorage.clear();

        window.location.href = "flights.html";
    }
});
