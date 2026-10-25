(() => {
    "use strict";

    const API_URL = "/api/auth/register";

    const form = document.getElementById("register-form");
    const message = document.getElementById("form-message");
    const button = document.getElementById("submit-button");
    const buttonLabel = document.getElementById("button-label");
    const spinner = document.getElementById("spinner");
    const dob = document.getElementById("date-of-birth");
    const expiry = document.getElementById("passport-expiry");
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirm-password");

    const today = () => new Date().toISOString().slice(0, 10);

    dob.max = today();

    function showError(text) {
        message.textContent = text;
        message.className = "message error";
        message.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });
    }

    function optional(name) {
        const value = form.elements[name].value.trim();
        return value || null;
    }

    function required(name) {
        return form.elements[name].value.trim();
    }

    function loading(active) {
        button.disabled = active;
        buttonLabel.textContent = active
            ? "Creating account..."
            : "Create account";

        spinner.classList.toggle("hidden", !active);
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        message.className = "message hidden";

        if (password.value.length < 12) {
            showError("Your password must contain at least 12 characters.");
            password.focus();
            return;
        }

        if (password.value !== confirmPassword.value) {
            showError("The passwords do not match.");
            confirmPassword.focus();
            return;
        }

        const passengerId = Number(required("passengerId"));

        if (!Number.isSafeInteger(passengerId) || passengerId <= 0) {
            showError("Enter a valid positive passenger ID.");
            form.elements["passengerId"].focus();
            return;
        }

        if (required("dateOfBirth") > today()) {
            showError("Date of birth cannot be in the future.");
            dob.focus();
            return;
        }

        if (optional("passportExpiry") &&
            optional("passportExpiry") < today()) {
            showError("Passport expiry date is in the past. Please check it.");
            expiry.focus();
            return;
        }

        const payload = {
            passengerId,
            firstName: required("firstName"),
            lastName: required("lastName"),
            dateOfBirth: required("dateOfBirth"),
            gender: required("gender"),
            nationality: optional("nationality"),
            phone: optional("phone"),
            email: required("email"),
            address: optional("address"),
            passportNumber: required("passportNumber"),
            passportExpiry: optional("passportExpiry"),
            passportCountry: optional("passportCountry"),
            password: password.value
        };

        loading(true);

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "same-origin",
                body: JSON.stringify(payload)
            });

            let result = {};

            try {
                result = await response.json();
            } catch {
                // The response may not contain JSON.
            }

            if (!response.ok) {
                showError(
                    result.error ||
                    result.message ||
                    `Registration failed (HTTP ${response.status}).`
                );
                return;
            }

            // Registration succeeded; the server should have
            // established the session before returning success.
            window.location.href = "/pages/passenger-dashboard.html";

        } catch (error) {
            console.error("Registration error:", error);

            showError(
                "Could not connect to the server. " +
                "Make sure the Java server is running and try again."
            );
        } finally {
            loading(false);
        }
    });
})();
