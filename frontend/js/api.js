
async function apiRequest(path, options = {}) {
    const response = await fetch(path, {
        credentials: "same-origin",
        headers: {
            "Accept": "application/json",
            ...(options.body ? { "Content-Type": "application/json" } : {}),
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
        const message =
            typeof data === "object" && data !== null
                ? data.error || data.message
                : data;

        throw new Error(message || `Request failed (${response.status}).`);
    }

    return data;
}

const FlightAPI = {
    getAll() {
        return apiRequest("/api/flights");
    },

    getById(id) {
        return apiRequest(`/api/flights/${encodeURIComponent(id)}`);
    },

    getAvailableSeats(id) {
        return apiRequest(
            `/api/flights/${encodeURIComponent(id)}/seats`
        );
    }
};

const ReservationAPI = {
    create(segments) {
        return apiRequest("/api/reservations", {
            method: "POST",
            body: JSON.stringify({ segments })
        });
    },

    addSegment(reservationId, segment) {
        return apiRequest(
            `/api/reservations/${encodeURIComponent(reservationId)}/segments`,
            {
                method: "POST",
                body: JSON.stringify(segment)
            }
        );
    },

    removeSegment(reservationId, segmentId) {
        return apiRequest(
            `/api/reservations/${encodeURIComponent(reservationId)}/segments/${encodeURIComponent(segmentId)}`,
            {
                method: "DELETE"
            }
        );
    }
};
