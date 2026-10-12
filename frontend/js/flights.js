const statusElement = document.getElementById("status");
const tableBody = document.getElementById("flight-table-body");

async function loadFlights() {
    try {
        const flights = await FlightAPI.getAll();

        if (!Array.isArray(flights)) {
            throw new Error("Expected an array of flights");
        }

        tableBody.replaceChildren();

        for (const flight of flights) {
            const row = document.createElement("tr");

            const values = [
                flight.flight_id,
                flight.flight_number,
                flight.flight_date,
                flight.aircraft_id,
                flight.sched_departure,
                flight.sched_arrival,
                flight.flight_status
            ];

            for (const value of values) {
                const cell = document.createElement("td");
                cell.textContent = value ?? "—";
                row.appendChild(cell);
            }

            tableBody.appendChild(row);
        }

        statusElement.textContent =
            flights.length === 0
                ? "No flights found."
                : `${flights.length} flight(s) found.`;

    } catch (error) {
        console.error("Failed to load flights:", error);
        statusElement.textContent =
            "Could not load flights. Check the server and try again.";
    }
}
loadFlights();
