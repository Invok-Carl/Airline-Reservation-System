document.addEventListener("DOMContentLoaded", () => {
    const API_BASE = "/api";

    /*
     * Your spec says "POST api/passengers{passengerId}" and
     * "passenger ID must be provided in the URL".
     *
     * true  -> POST /api/passengers/{passengerId}  (user types the ID in the form)
     * false -> POST /api/passengers                (server generates the ID)
     */
    const CREATE_WITH_ID_IN_URL = true;

    const elements = {
        pageMessage: document.getElementById("page-message"),
        formMessage: document.getElementById("form-message"),

        form: document.getElementById("passenger-form"),
        formModeLabel: document.getElementById("form-mode-label"),
        passengerIdGroup: document.getElementById("passenger-id-group"),
        saveButton: document.getElementById("save-button"),
        cancelEditButton: document.getElementById("cancel-edit-button"),

        listLoading: document.getElementById("list-loading"),
        listEmpty: document.getElementById("list-empty"),
        table: document.getElementById("passenger-table"),
        tableBody: document.getElementById("passenger-table-body"),
        passengerCount: document.getElementById("passenger-count")
    };

    const FIELDS = [
        "passengerId",
        "firstName",
        "lastName",
        "dateOfBirth",
        "gender",
        "nationality",
        "phone",
        "email",
        "address",
        "passportNumber",
        "passportExpiry",
        "passportCountry"
    ];

    const REQUIRED_FIELDS = {
        firstName: "First name",
        lastName: "Last name",
        dateOfBirth: "Date of birth",
        gender: "Gender",
        nationality: "Nationality",
        passportNumber: "Passport number",
        passportExpiry: "Passport expiry",
        passportCountry: "Passport country"
    };

    let passengers = [];
    let editingPassengerId = null;
    let saving = false;

    initialize();

    // --------------------------------------------------
    // Initialization
    // --------------------------------------------------

    function initialize() {
        elements.form.addEventListener("submit", handleSubmit);
        elements.cancelEditButton.addEventListener("click", exitEditMode);

        enterCreateMode();
        loadPassengers();
    }

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                ...(options.body ? { "Content-Type": "application/json" } : {}),
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

            if (response.status === 401) {
                throw new Error(message || "Please sign in to continue.");
            }

            if (response.status === 403) {
                throw new Error(
                    message || "You do not have permission to do that."
                );
            }

            throw new Error(
                message || `Request failed with status ${response.status}.`
            );
        }

        return data;
    }

    function getProperty(object, camelCase, snakeCase) {
        return object?.[camelCase] ?? object?.[snakeCase];
    }

    function toSnake(name) {
        return name.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
    }

    function getPassengerId(passenger) {
        return Number(getProperty(passenger, "passengerId", "passenger_id"));
    }

    function getField(passenger, name) {
        return getProperty(passenger, name, toSnake(name)) ?? "";
    }

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

    function formatDate(value) {
        if (!value) return "";

        const date = new Date(value + "T00:00:00");

        return Number.isNaN(date.getTime())
            ? value
            : date.toLocaleDateString();
    }

    // --------------------------------------------------
    // Loading and rendering the list
    // --------------------------------------------------

    async function loadPassengers() {
        setHidden(elements.listLoading, false);
        setHidden(elements.listEmpty, true);
        setHidden(elements.table, true);

        try {
            const data = await apiRequest(`${API_BASE}/passengers`);

            passengers = Array.isArray(data)
                ? data
                : data?.passengers ?? [];

            renderPassengers();

        } catch (error) {
            console.error("Failed to load passengers:", error);

            showMessage(
                elements.pageMessage,
                error.message || "Could not load passengers.",
                "error"
            );

        } finally {
            setHidden(elements.listLoading, true);
        }
    }

    function renderPassengers() {
        const body = elements.tableBody;

        body.innerHTML = "";

        elements.passengerCount.textContent =
            `${passengers.length} ${passengers.length === 1 ? "passenger" : "passengers"}`;

        if (passengers.length === 0) {
            setHidden(elements.listEmpty, false);
            setHidden(elements.table, true);
            return;
        }

        setHidden(elements.listEmpty, true);
        setHidden(elements.table, false);

        passengers.forEach(passenger => {
            const passengerId = getPassengerId(passenger);
            const row = document.createElement("tr");

            const cells = [
                passengerId,
                `${getField(passenger, "firstName")} ${getField(passenger, "lastName")}`,
                formatDate(getField(passenger, "dateOfBirth")),
                getField(passenger, "nationality"),
                getField(passenger, "email"),
                getField(passenger, "passportNumber"),
                formatDate(getField(passenger, "passportExpiry"))
            ];

            cells.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

            const actionCell = document.createElement("td");

            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.className = "button button-secondary";
            editButton.textContent = "Edit";
            editButton.addEventListener("click", () => {
                enterEditMode(passenger);
            });

            actionCell.appendChild(editButton);
            row.appendChild(actionCell);

            body.appendChild(row);
        });
    }

    // --------------------------------------------------
    // Form modes
    // --------------------------------------------------

    function enterCreateMode() {
        editingPassengerId = null;

        elements.form.reset();
        clearMessage(elements.formMessage);

        elements.formModeLabel.textContent = "NEW PASSENGER";
        elements.saveButton.textContent = "Create passenger";

        // The ID field is only needed when creating with the ID in the URL.
        setHidden(elements.passengerIdGroup, !CREATE_WITH_ID_IN_URL);
        elements.form.elements.passengerId.disabled = false;

        setHidden(elements.cancelEditButton, true);
    }

    function enterEditMode(passenger) {
        editingPassengerId = getPassengerId(passenger);

        clearMessage(elements.formMessage);
        clearMessage(elements.pageMessage);

        FIELDS.forEach(name => {
            const input = elements.form.elements[name];

            if (!input) return;

            input.value = name === "passengerId"
                ? editingPassengerId
                : getField(passenger, name);
        });

        elements.formModeLabel.textContent = `EDITING PASSENGER ${editingPassengerId}`;
        elements.saveButton.textContent = "Save changes";

        // The ID is in the URL for PUT and cannot be changed.
        setHidden(elements.passengerIdGroup, false);
        elements.form.elements.passengerId.disabled = true;

        setHidden(elements.cancelEditButton, false);

        elements.form.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    function exitEditMode() {
        enterCreateMode();
    }

    // --------------------------------------------------
    // Form handling
    // --------------------------------------------------

    function readForm() {
        const values = {};

        FIELDS.forEach(name => {
            const input = elements.form.elements[name];

            if (input) {
                values[name] = input.value.trim();
            }
        });

        return values;
    }

    function validate(values) {
        for (const [name, label] of Object.entries(REQUIRED_FIELDS)) {
            if (!values[name]) {
                return `${label} is required.`;
            }
        }

        if (editingPassengerId === null && CREATE_WITH_ID_IN_URL) {
            const id = Number(values.passengerId);

            if (!Number.isInteger(id) || id <= 0) {
                return "Enter a valid passenger ID (a positive whole number).";
            }
        }

        const today = new Date().toISOString().slice(0, 10);

        if (values.dateOfBirth >= today) {
            return "Date of birth must be in the past.";
        }

        if (values.passportExpiry <= today) {
            return "Passport expiry must be in the future.";
        }

        if (values.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
            return "Enter a valid email address.";
        }

        return null;
    }

    function buildRequestBody(values) {
        // createdBy is not sent: the server should take it from the signed-in account.
        return {
            firstName: values.firstName,
            lastName: values.lastName,
            dateOfBirth: values.dateOfBirth,
            gender: values.gender,
            nationality: values.nationality,
            phone: values.phone || null,
            email: values.email || null,
            address: values.address || null,
            passportNumber: values.passportNumber,
            passportExpiry: values.passportExpiry,
            passportCountry: values.passportCountry
        };
    }

    async function handleSubmit(event) {
        console.count("Passenger form submitted");
        event.preventDefault();

        if (saving) return;
        clearMessage(elements.formMessage);
        clearMessage(elements.pageMessage);

        const values = readForm();

        // The ID input is disabled while editing, so it is not in readForm().
        if (editingPassengerId !== null) {
            values.passengerId = String(editingPassengerId);
        }

        const validationError = validate(values);

        if (validationError) {
            showMessage(elements.formMessage, validationError, "error");
            return;
        }

        const isEditing = editingPassengerId !== null;
        const body = buildRequestBody(values);

        let url;
        let method;

        if (isEditing) {
            method = "PUT";
            url = `${API_BASE}/passengers/${editingPassengerId}`;
        } else if (CREATE_WITH_ID_IN_URL) {
            method = "POST";
            url = `${API_BASE}/passengers/${Number(values.passengerId)}`;
        } else {
            method = "POST";
            url = `${API_BASE}/passengers`;
        }

        saving = true;
        elements.saveButton.disabled = true;

        try {
            const response = await apiRequest(url, {
                method,
                body: JSON.stringify(body)
            });

            console.log("Save passenger response:", response);

            const message = response?.message ||
                (isEditing
                    ? "Passenger updated successfully."
                    : "Passenger created successfully.");

            enterCreateMode();

            showMessage(elements.pageMessage, message, "success");

            await loadPassengers();

        } catch (error) {
            console.error("Failed to save passenger:", error);

            showMessage(
                elements.formMessage,
                error.message || "Could not save the passenger.",
                "error"
            );

        } finally {
            saving = false;
            elements.saveButton.disabled = false;
        }
    }
});
