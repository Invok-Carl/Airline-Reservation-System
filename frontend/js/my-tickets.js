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

function createCard(ticket) {
    const card = document.createElement("div");
    card.className = "card";

    const left = document.createElement("div");

    const title = document.createElement("h3");
    title.textContent = "Ticket " + (ticket.ticketNumber || "-");

    const meta = document.createElement("div");
    meta.className = "meta";
    meta.textContent = "Issued on " + formatDate(ticket.issueDate);

    const badge = document.createElement("span");
    const status = ticket.status || "UNKNOWN";
    badge.className = "badge " + status.toLowerCase();
    badge.textContent = status;
    badge.style.marginTop = "8px";

    left.append(title, meta, badge);

    const right = document.createElement("div");
    right.className = "meta";
    right.style.textAlign = "right";
    right.textContent = "Booking: " + (ticket.bookingReference || "-");

    card.append(left, right);
    return card;
}

async function loadTickets() {
    try {
        const response = await fetch("/api/tickets", {
            method: "GET",
            credentials: "same-origin",
            headers: { "Accept": "application/json" }
        });

        if (response.status === 401) {
            window.location.replace("/pages/login.html");
            return;
        }

        if (response.status === 404 || response.status === 405) {
            showState("Ticket list is not available yet.");
            return;
        }

        if (!response.ok) {
            throw new Error("Could not load tickets.");
        }

        const tickets = await response.json();

        if (!Array.isArray(tickets) || tickets.length === 0) {
            showState('You have no tickets yet. <a href="/pages/flights.html">Search flights</a>');
            return;
        }

        stateElement.style.display = "none";
        tickets.forEach(function (ticket) {
            listElement.appendChild(createCard(ticket));
        });

    } catch (error) {
        showState(error.message);
    }
}

loadTickets();