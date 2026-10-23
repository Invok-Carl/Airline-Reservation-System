
const form = document.getElementById("add-flight-form");
const submitButton = document.getElementById("submit-button");
const message = document.getElementById("form-message");

form.addEventListener("submit", async event => {
    event.preventDefault();

    message.textContent = "";
    message.className = "";

    const formData = new FormData(form);

    const flightDate = formData.get("flight_date");
    const aircraftId = Number(formData.get("aircraft_id"));
    const scheduleId = Number(formData.get("schedule_id"));
    const actualDeparture = formData.get("actual_departure");
    const actualArrival = formData.get("actual_arrival");
    const flightStatus = formData.get("flight_status");

    if (!Number.isSafeInteger(aircraftId) || aircraftId <= 0) {
        showMessage("Enter a valid aircraft ID.", "error");
        return;
    }

    if (!Number.isSafeInteger(scheduleId) || scheduleId <= 0) {
        showMessage("Enter a valid flight schedule ID.", "error");
        return;
    }

    // These values must match the AddFlightRequest record in Java.
    const flight = {
        flight_date: flightDate,
        aircraft_id: aircraftId,
        actual_departure: actualDeparture || null,
        actual_arrival: actualArrival || null,
        flight_status: flightStatus,
        schedule_id: scheduleId
    };

    submitButton.disabled = true;
    submitButton.textContent = "Adding Flight...";

    try {
        const response = await fetch("/api/flights", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(flight)
        });

        if (!response.ok) {
            let errorMessage = `Could not add flight (HTTP ${response.status}).`;

            try {
                const errorBody = await response.json();

                if (errorBody.message) {
                    errorMessage = errorBody.message;
                } else if (errorBody.error) {
                    errorMessage = errorBody.error;
                }
            } catch {
                // The server may return an empty or non-JSON error response.
            }

            throw new Error(errorMessage);
        }

        showMessage("Flight added successfully.", "success");
        form.reset();

    } catch (error) {
        console.error("Failed to add flight:", error);

        showMessage(
            error.message || "An unexpected error occurred.",
            "error"
        );
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = "Add Flight";
    }
});

function showMessage(text, type) {
    message.textContent = text;
    message.className = type;
}


