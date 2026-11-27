const listElement = document.getElementById("list");
const stateElement = document.getElementById("state");

function showState(html) {
    stateElement.innerHTML = html;
    stateElement.style.display = "block";
}

function formatDate(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (isNaN(date)) return value;
    return date.toLocaleString([], {
        year: "numeric", month: "short", day: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

function formatMoney(value) {
    const number = Number(value);
    if (isNaN(number)) return "-";
    return new Intl.NumberFormat("en-US", {
        style: "currency", currency: "USD"
    }).format(number);
}

function createCard(reservation) {
    const card = document.createElement("div");
    card.className = "card";

    const left = document.createElement("div");

    const title = document.createElement("h3");
    title.textContent = reservation.bookingReference || "-";

    const meta = document.createElement("div");
    meta.className = "meta";
    meta.textContent = "Booked on " + formatDate(reservation.reservationDate);

    const badge = document.createElement("span");
    const status = reservation.status || "UNKNOWN";
    badge.className = "badge " + status.toLowerCase();
    badge.textContent = status;
    badge.style.marginTop = "8px";

    left.append(title, meta, badge);

    const amount = document.createElement("div");
    amount.className = "amount";
    amount.textContent = formatMoney(reservation.totalAmount);

    card.append(left, amount);
    return card;
}

async function loadReservations() {
    try {
        const response = await fetch("/api/reservations", {
            method: "GET",
            credentials: "same-origin",
            headers: { "Accept": "application/json" }
        });

        if (response.status === 401) {
            window.location.replace("/pages/login.html");
            return;
        }

        if (response.status === 404 || response.status === 405) {
            showState("Reservation list is not available yet.");
            return;
        }

        if (!response.ok) {
            throw new Error("Could not load reservations.");
        }

        const reservations = await response.json();

        if (!Array.isArray(reservations) || reservations.length === 0) {
            showState('You have no reservations yet. <a href="/pages/flights.html">Search flights</a>');
            return;
        }

        stateElement.style.display = "none";
        reservations.forEach(function (reservation) {
            listElement.appendChild(createCard(reservation));
        });

    } catch (error) {
        showState(error.message);
    }
}

loadReservations();