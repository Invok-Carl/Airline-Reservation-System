async function apiRequest(path, options = {}) {
    const response = await fetch(path, options);

    if (!response.ok) {
        throw new Error(`HTTP error: ${response.status}`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

const FlightAPI = {
    getAll() {
        return apiRequest("/api/flights");
    },

    getById(id) {
        return apiRequest(`/api/flights/${id}`);
    }
};
