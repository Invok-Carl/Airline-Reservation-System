
document.addEventListener("DOMContentLoaded", () => {

    // -----------------------------------------
    // DOM ELEMENTS
    // -----------------------------------------

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

    const reservationPanel = document.getElementById("reservation-panel");
    const selectedFlightList = document.getElementById("selected-flight-list");

    const selectedFlightCount = document.getElementById("selected-flight-count");
    const selectedFlightTotal = document.getElementById("selected-flight-total");

    const clearReservationButton =
        document.getElementById("clear-reservation-button");

    const continueReservationButton =
        document.getElementById("continue-reservation-button");

    const reservationMessage =
        document.getElementById("reservation-message");


    // -----------------------------------------
    // STATE
    // -----------------------------------------

    let flights = [];

    // -----------------------------------------
    // INITIALIZATION
    // -----------------------------------------

    initialize();

    async function initialize() {
        renderSelectedFlights();
        await loadFlights();
    }


    // -----------------------------------------
    // FETCH FLIGHTS
    // -----------------------------------------

    async function loadFlights(departureCountry = "", arrivalCountry = "") {

        setLoading(true);
        clearPageMessage();

        flightList.replaceChildren();

        try {

            const params = new URLSearchParams();

            if (departureCountry) {
                params.set("departureCountry", departureCountry);
            }

            if (arrivalCountry) {
                params.set("arrivalCountry", arrivalCountry);
            }

            const query = params.toString();

            const url = query
                ? `/api/flights?${query}`
                : "/api/flights";

            const response = await fetch(url, {
                method: "GET",
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error("Failed to load flights.");
            }

            const data = await response.json();

            // API is expected to return an array.
            flights = Array.isArray(data) ? data : data.flights || [];

            renderFlights();

        } catch (error) {

            console.error("Flight loading error:", error);

            showPageMessage(
                error.message || "Unable to load flights.",
                "error"
            );

            resultsCount.textContent = "0 flights";

        } finally {

            setLoading(false);

        }
    }


    // -----------------------------------------
    // RENDER FLIGHTS
    // -----------------------------------------


function renderFlights() {

    flightList.replaceChildren();

    resultsCount.textContent =
        `${flights.length} flight${flights.length === 1 ? "" : "s"}`;

    emptyState.classList.toggle("hidden", flights.length !== 0);

    flights.forEach(flight => {

        const card = document.createElement("article");
        card.className = "flight-card";

        const heading = document.createElement("h3");
        heading.textContent = `Flight ${flight.flightId}`;

        const route = document.createElement("p");
        route.textContent =
            `${flight.departureAirport} → ${flight.arrivalAirport}`;

        const date = document.createElement("p");
        date.textContent =
            `Date: ${formatDate(flight.flightDate)}`;

        const departure = document.createElement("p");
        departure.textContent =
            `Departure: ${flight.schedDeparture || "N/A"}`;

        const arrival = document.createElement("p");
        arrival.textContent =
            `Arrival: ${flight.schedArrival || "N/A"}`;

        const status = document.createElement("p");
        status.textContent =
            `Status: ${flight.flightStatus || "Unknown"}`;

        const addButton = document.createElement("button");

        addButton.type = "button";
        addButton.className = "button button-primary";

        const alreadySelected = isFlightSelected(flight.flightId);

        addButton.textContent = alreadySelected
            ? "Added to reservation"
            : "Add flight";

        addButton.disabled = alreadySelected;

        addButton.addEventListener("click", () => {
            selectFlight(flight);
        });

        card.append(
            heading,
            route,
            date,
            departure,
            arrival,
            status,
            addButton
        );

        flightList.appendChild(card);

    });
}



    // -----------------------------------------
    // ADD FLIGHT TO RESERVATION
    // -----------------------------------------


function selectFlight(flight) {

    const reservation = ReservationStorage.getOrCreate();

    if (reservation.flights.some(
        selected => selected.flightId === flight.flightId
    )) {
        return;
    }

    reservation.flights.push({

        flightId: flight.flightId,
        flightDate: flight.flightDate,

        departureAirport: flight.departureAirport,
        arrivalAirport: flight.arrivalAirport,

        schedDeparture: flight.schedDeparture,
        schedArrival: flight.schedArrival,

        seats: {}

    });

    ReservationStorage.save(reservation);

    renderFlights();
    renderSelectedFlights();

    showReservationMessage(
        "Flight added to your reservation.",
        "success"
    );
}



    // -----------------------------------------
    // CHECK WHETHER FLIGHT IS SELECTED
    // -----------------------------------------


function isFlightSelected(flightId) {

    const reservation = ReservationStorage.get();

    if (!reservation) {
        return false;
    }

    return reservation.flights.some(
        flight => flight.flightId === flightId
    );
}



    // -----------------------------------------
    // REMOVE FLIGHT
    // -----------------------------------------


function removeFlight(flightId) {

    const reservation = ReservationStorage.get();

    if (!reservation) {
        return;
    }

    reservation.flights = reservation.flights.filter(
        flight => flight.flightId !== flightId
    );

    ReservationStorage.save(reservation);

    renderFlights();
    renderSelectedFlights();

    showReservationMessage(
        "Flight removed from your reservation.",
        "success"
    );
}



    // -----------------------------------------
    // RENDER SELECTED FLIGHTS
    // -----------------------------------------


function renderSelectedFlights() {

    const reservation = ReservationStorage.get();

    const selectedFlights = reservation
        ? reservation.flights
        : [];

    selectedFlightList.replaceChildren();

    const count = selectedFlights.length;

    selectedFlightCount.textContent =
        `${count} flight${count === 1 ? "" : "s"}`;

    selectedFlightTotal.textContent = count;

    reservationPanel.classList.toggle("hidden", count === 0);

    continueReservationButton.disabled = count === 0;

    selectedFlights.forEach(flight => {

        const card = document.createElement("article");
        card.className = "flight-card";

        const heading = document.createElement("h3");
        heading.textContent = `Flight ${flight.flightId}`;

        const route = document.createElement("p");
        route.textContent =
            `${flight.departureAirport} → ${flight.arrivalAirport}`;

        const date = document.createElement("p");
        date.textContent =
            `Date: ${formatDate(flight.flightDate)}`;

        const removeButton = document.createElement("button");

        removeButton.type = "button";
        removeButton.className = "text-button";
        removeButton.textContent = "Remove flight";

        removeButton.addEventListener("click", () => {
            removeFlight(flight.flightId);
        });

        card.append(
            heading,
            route,
            date,
            removeButton
        );

        selectedFlightList.appendChild(card);

    });
}



    // -----------------------------------------
    // CLEAR SELECTED FLIGHTS
    // -----------------------------------------

    function clearReservation() {

        const reservation = ReservationStorage.get();

        if (!reservation) {
            return;
        }

        reservation.flights = [];

        ReservationStorage.save(reservation);

        renderFlights();
        renderSelectedFlights();

        clearReservationMessage();
    }


    // -----------------------------------------
    // CONTINUE TO RESERVATION PAGE
    // -----------------------------------------

    function continueReservation() {

        const reservation = ReservationStorage.get();

        if (!reservation || reservation.flights.length === 0) {
            showReservationMessage(
                "Select at least one flight.",
                "error"
            );

            return;
        }

        window.location.href = "reservation.html";
    }


    // -----------------------------------------
    // SEARCH EVENTS
    // -----------------------------------------

    searchForm.addEventListener("submit", async event => {

        event.preventDefault();

        const departureCountry = departureInput.value.trim();
        const arrivalCountry = arrivalInput.value.trim();

        await loadFlights(departureCountry, arrivalCountry);

    });


    // -----------------------------------------
    // SHOW ALL FLIGHTS
    // -----------------------------------------

    showAllButton.addEventListener("click", async () => {

        searchForm.reset();

        await loadFlights();

    });


    // -----------------------------------------
    // CLEAR RESERVATION EVENT
    // -----------------------------------------

    clearReservationButton.addEventListener(
        "click",
        clearReservation
    );


    // -----------------------------------------
    // CONTINUE EVENT
    // -----------------------------------------

    continueReservationButton.addEventListener(
        "click",
        continueReservation
    );


    // -----------------------------------------
    // UI HELPERS
    // -----------------------------------------

    function setLoading(loading) {

        loadingState.classList.toggle("hidden", !loading);

        searchButton.disabled = loading;
        showAllButton.disabled = loading;

    }


    function showPageMessage(message, type) {

        pageMessage.textContent = message;

        pageMessage.classList.remove("hidden", "success", "error");
        pageMessage.classList.add(type);

    }


    function clearPageMessage() {

        pageMessage.textContent = "";
        pageMessage.classList.add("hidden");

    }


    function showReservationMessage(message, type) {

        reservationMessage.textContent = message;

        reservationMessage.classList.remove(
            "hidden",
            "success",
            "error"
        );

        reservationMessage.classList.add(type);

    }


    function clearReservationMessage() {

        reservationMessage.textContent = "";
        reservationMessage.classList.add("hidden");

    }


    function formatDate(dateValue) {

        if (!dateValue) {
            return "N/A";
        }

        const date = new Date(dateValue + "T00:00:00");

        if (Number.isNaN(date.getTime())) {
            return dateValue;
        }

        return date.toLocaleDateString();

    }

});
