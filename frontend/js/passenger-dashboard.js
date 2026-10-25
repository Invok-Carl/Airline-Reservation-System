
const emailElement = document.getElementById("passenger-email");
const welcomeElement = document.getElementById("welcome");
const statusElement = document.getElementById("account-status");
const messageElement = document.getElementById("message");

async function loadPassenger() {
    try {
        const response = await fetch("/api/auth/me", {
            method: "GET",
            credentials: "same-origin",
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.replace("/pages/login.html");
            return;
        }

        if (!response.ok) {
            throw new Error("Could not load your account.");
        }

        const account = await response.json();

        emailElement.textContent = account.email || "";

        welcomeElement.textContent = account.firstName
            ? `Welcome, ${account.firstName}`
            : "Welcome";

        statusElement.textContent =
            `Account ID: ${account.accountId} | ` +
            `Passenger ID: ${account.passengerId}`;

    } catch (error) {
        statusElement.textContent =
            "Account information is unavailable.";

        messageElement.textContent = error.message;
    }
}

document
    .getElementById("logout-button")
    .addEventListener("click", async () => {
        const button = document.getElementById("logout-button");

        button.disabled = true;
        messageElement.textContent = "";

        try {
            const response = await fetch("/api/auth/logout", {
                method: "POST",
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error("Logout failed. Please try again.");
            }

            window.location.replace("/pages/login.html");

        } catch (error) {
            messageElement.textContent = error.message;
            button.disabled = false;
        }
    });

loadPassenger();
