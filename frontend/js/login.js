
"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    const identityInput = document.getElementById("identity");
    const identityLabel = document.getElementById("identityLabel");
    const passwordInput = document.getElementById("password");
    const togglePasswordButton = document.getElementById("togglePassword");
    const formMessage = document.getElementById("formMessage");
    const submitButton = document.getElementById("submitButton");
    const submitText = document.getElementById("submitText");
    const registerPrompt = document.getElementById("registerPrompt");
    const roleInputs = document.querySelectorAll('input[name="role"]');

    const EMPLOYEE_LOGIN_URL = "/api/employee-auth/login";
    const EMPLOYEE_LOGOUT_URL = "/api/employee-auth/logout";
    const PASSENGER_LOGIN_URL = "/api/auth/login";

    let isSubmitting = false;

    // Get the selected account type.
    function getSelectedRole() {
        return document.querySelector('input[name="role"]:checked')?.value;
    }

    // Update the identity field when the account type changes.
    function updateRoleFields() {
        const role = getSelectedRole();
        const isPassenger = role === "passenger";

        identityLabel.textContent = isPassenger
            ? "Email address"
            : "Username";

        identityInput.type = isPassenger ? "email" : "text";

        identityInput.autocomplete = isPassenger
            ? "username"
            : "username";

        identityInput.placeholder = isPassenger
            ? "you@example.com"
            : "Enter your username";

        registerPrompt.hidden = !isPassenger;

        clearMessage();
    }

    // Display an error or status message.
    function showMessage(message, type = "error") {
        formMessage.textContent = message;
        formMessage.className = "form-message";

        if (type === "success") {
            formMessage.classList.add("success");
        }
    }

    // Clear the current message.
    function clearMessage() {
        formMessage.textContent = "";
        formMessage.className = "form-message";
    }

    // Enable or disable the submit button while a request is running.
    function setSubmitting(submitting) {
        isSubmitting = submitting;
        submitButton.disabled = submitting;

        submitText.textContent = submitting
            ? "Signing in..."
            : "Sign in";
    }

    // Parse JSON safely and handle unexpected server responses.
    async function readResponse(response) {
        const text = await response.text();

        if (!text) {
            return {};
        }

        try {
            return JSON.parse(text);
        } catch {
            throw new Error("The server returned an invalid response.");
        }
    }

    // Get a useful error message from the backend response.
    function getErrorMessage(data, fallback) {
        return (
            data.message ||
            data.error ||
            fallback
        );
    }

    // Handle passenger login.
    async function loginPassenger(email, password) {
        const response = await fetch(PASSENGER_LOGIN_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email,
                password
            })
        });

        const data = await readResponse(response);

        if (!response.ok) {
            throw new Error(
                getErrorMessage(data, "Invalid email or password.")
            );
        }

        showMessage("Login successful. Redirecting...", "success");

        window.location.href = "/pages/passenger-dashboard.html";
    }

    // Handle employee login for staff and administrators.
    async function loginEmployee(username, password, selectedRole) {
        const response = await fetch(EMPLOYEE_LOGIN_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username,
                password
            })
        });

        const data = await readResponse(response);

        if (!response.ok) {
            throw new Error(
                getErrorMessage(data, "Invalid username or password.")
            );
        }

        /*
         * Expected response:
         * {
         *     "message": "Login successful",
         *     "employee": {
         *         "employeeId": 1,
         *         "username": "admin",
         *         "position": "ADMINISTRATOR"
         *     }
         * }
         *
         * Adjust the property access below if your controller
         * returns a different response structure.
         */

        const employee = data.employee;

        if (!employee || !employee.position) {
            await logoutEmployee();

            throw new Error(
                "The server response did not contain employee details."
            );
        }

        const position = employee.position.toUpperCase();

        const isAdministrator = position === "ADMINISTRATOR";

        // Ensure the selected account type matches the employee role.
        if (selectedRole === "admin" && !isAdministrator) {
            await logoutEmployee();

            throw new Error(
                "This account is not an administrator account."
            );
        }

        if (selectedRole === "staff" && isAdministrator) {
            await logoutEmployee();

            throw new Error(
                "Please select Administrator for this account."
            );
        }

        if (
            selectedRole === "staff" &&
            !["RESERVATION_AGENT", "AIRPORT_STAFF"].includes(position)
        ) {
            await logoutEmployee();

            throw new Error(
                "This account does not have a supported staff role."
            );
        }

        showMessage("Login successful. Redirecting...", "success");


        window.location.href = "employee-dashboard.html";

    }

    // Invalidate an employee session when the selected role is incorrect.
    async function logoutEmployee() {
        try {
            await fetch(EMPLOYEE_LOGOUT_URL, {
                method: "POST",
                credentials: "include"
            });
        } catch (error) {
            console.error("Unable to invalidate employee session:", error);
        }
    }

    // Handle form submission.
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (isSubmitting) {
            return;
        }

        clearMessage();

        const role = getSelectedRole();
        const identity = identityInput.value.trim();
        const password = passwordInput.value;

        if (!role) {
            showMessage("Please select an account type.");
            return;
        }

        if (!identity || !password) {
            showMessage("Please enter your username/email and password.");
            return;
        }

        if (role === "passenger" && !identityInput.validity.valid) {
            showMessage("Please enter a valid email address.");
            identityInput.focus();
            return;
        }

        setSubmitting(true);

        try {
            if (role === "passenger") {
                await loginPassenger(identity, password);
            } else {
                await loginEmployee(identity, password, role);
            }
        } catch (error) {
            console.error("Login failed:", error);

            showMessage(
                error instanceof TypeError
                    ? "Could not connect to the server. Please try again."
                    : error.message || "Login failed. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    });

    // Show or hide the password.
    togglePasswordButton.addEventListener("click", () => {
        const isPasswordHidden = passwordInput.type === "password";

        passwordInput.type = isPasswordHidden ? "text" : "password";

        togglePasswordButton.textContent = isPasswordHidden
            ? "Hide"
            : "Show";

        togglePasswordButton.setAttribute(
            "aria-label",
            isPasswordHidden ? "Hide password" : "Show password"
        );

        togglePasswordButton.setAttribute(
            "aria-pressed",
            String(isPasswordHidden)
        );
    });

    // Update fields when the selected account type changes.
    roleInputs.forEach((input) => {
        input.addEventListener("change", updateRoleFields);
    });

    // Initialize the form.
    updateRoleFields();
});
