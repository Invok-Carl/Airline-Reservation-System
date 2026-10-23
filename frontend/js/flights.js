
const searchForm = document.getElementById("search-form");
const departureInput = document.getElementById("departure-country");
const arrivalInput = document.getElementById("arrival-country");
const showAllButton = document.getElementById("show-all-button");

const resultsContainer = document.getElementById("flight-results");
const statusMessage = document.getElementById("status-message");
const resultCount = document.getElementById("result-count");

async function loadFlights(departureCountry = "", arrivalCountry = "") {
    resultsContainer.replaceChildren();
    statusMessage.textContent = "Loading flights...";
    statusMessage.classList.remove("error");
    resultCount.textContent = "";

    const params = new URLSearchParams();

    if (departureCountry) {
        params.set("departureCountry", departureCountry);
    }

    if (arrivalCountry) {
        params.set("arrivalCountry", arrivalCountry);
    }

    const queryString = params.toString();
    const url = queryString
        ? `/api/flights?${queryString}`
        : "/api/flights";

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Server returned HTTP ${response.status}`);
        }

        const flights = await response.json();

        if (!Array.isArray(flights)) {
            throw new Error("The server did not return a list of flights.");
        }

        displayFlights(flights);
    } catch (error) {
        console.error("Failed to load flights:", error);

        resultsContainer.replaceChildren();
        statusMessage.textContent =
            "Could not load flights. Please check the server and try again.";
        statusMessage.classList.add("error");
        resultCount.textContent = "";
    }
}

function displayFlights(flights) {
    resultsContainer.replaceChildren();

    resultCount.textContent =
        `${flights.length} flight${flights.length === 1 ? "" : "s"}`;

    if (flights.length === 0) {
        statusMessage.textContent = "No flights found.";
        resultsContainer.innerHTML =
            '<p class="empty-results">Try different search criteria.</p>';
        return;
    }

    statusMessage.textContent = "";

    for (const flight of flights) {
        const card = document.createElement("article");
        card.className = "flight-card";

        const title = document.createElement("h4");
        title.textContent = `Flight ${flight.flight_number ?? flight.flight_id ?? "Unknown"}`;
        card.appendChild(title);

        addDetail(card, "Departure", flight.departure_airport);
        addDetail(card, "Arrival", flight.arrival_airport);
        addDetail(card, "Date", formatDate(flight.flight_date));

        const statusLabel = document.createElement("p");
        const statusHeading = document.createElement("strong");
        statusHeading.textContent = "Status: ";

        const statusValue = document.createElement("span");
        statusValue.className = "flight-status";
        statusValue.textContent = flight.flight_status ?? "Unknown";

        statusLabel.append(statusHeading, statusValue);
        card.appendChild(statusLabel);

        resultsContainer.appendChild(card);
    }
}

function addDetail(card, label, value) {
    const paragraph = document.createElement("p");
    const heading = document.createElement("strong");

    heading.textContent = `${label}: `;
    paragraph.appendChild(heading);
    paragraph.append(document.createTextNode(value ?? "Not available"));

    card.appendChild(paragraph);
}

function formatDate(value) {
    if (!value) {
        return "Not available";
    }

    // Handles an ISO date such as 2026-10-05 without timezone shifts.
    const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value);

    if (match) {
        return `${match[3]}-${match[2]}-${match[1]}`;
    }

    return value;
}

searchForm.addEventListener("submit", event => {
    event.preventDefault();

    const departureCountry = departureInput.value.trim();
    const arrivalCountry = arrivalInput.value.trim();

    loadFlights(departureCountry, arrivalCountry);
});

showAllButton.addEventListener("click", () => {
    departureInput.value = "";
    arrivalInput.value = "";

    loadFlights();
});

// Load all flights when the page opens.
loadFlights();
