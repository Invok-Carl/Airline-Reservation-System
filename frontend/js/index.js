
"use strict";

const DASHBOARD_API = {
    summary: "/api/dashboard/summary",
    flights: "/api/flights",
    reservations: "/api/reservations",
    passengers: "/api/passengers",
    aircraft: "/api/aircraft"
};

const appShell = document.querySelector(".app-shell");

const dashboardElements = {
    sidebarToggle: document.getElementById("sidebar-toggle"),
    sidebar: document.getElementById("sidebar"),
    refresh: document.getElementById("dashboard-refresh"),
    checkStatus: document.getElementById("check-status"),
    date: document.getElementById("current-date"),
    message: document.getElementById("dashboard-message"),

    applicationStatus: document.getElementById("application-status"),
    applicationIndicator: document.querySelector(".indicator-dot"),

    apiStatus: document.getElementById("api-status"),
    apiIndicator: document.getElementById("api-indicator"),
    apiBadge: document.getElementById("api-badge"),

    dataStatus: document.getElementById("data-status"),
    dataIndicator: document.getElementById("data-indicator"),
    dataBadge: document.getElementById("data-badge"),

    moduleSearch: document.getElementById("module-search"),
    moduleGrid: document.getElementById("module-grid"),
    noModules: document.getElementById("no-modules")
};

const metricElements = {
    flights: document.getElementById("metric-flights"),
    reservations: document.getElementById("metric-reservations"),
    passengers: document.getElementById("metric-passengers"),
    aircraft: document.getElementById("metric-aircraft")
};

let dashboardLoading = false;

function showDashboardMessage(message, type = "info") {
    dashboardElements.message.textContent = message;
    dashboardElements.message.className = `alert ${type}`;
}

function clearDashboardMessage() {
    dashboardElements.message.textContent = "";
    dashboardElements.message.className = "alert hidden";
}

function setConnectionStatus(connected, message) {
    const { apiStatus, apiIndicator, apiBadge,
        applicationStatus, applicationIndicator } = dashboardElements;

    apiStatus.textContent = message;
    apiIndicator.className =
        `status-indicator ${connected ? "connected" : "disconnected"}`;

    apiBadge.textContent = connected ? "Reachable" : "Unavailable";
    apiBadge.className =
        `status-badge ${connected ? "online" : "offline"}`;

    applicationStatus.textContent =
        connected ? "API reachable" : "API unavailable";

    applicationIndicator.className =
        `indicator-dot ${connected ? "connected" : "disconnected"}`;
}

function setDataStatus(status, message) {
    const { dataStatus, dataIndicator, dataBadge } = dashboardElements;

    dataStatus.textContent = message;
    dataBadge.textContent = status;

    dataIndicator.className = "status-indicator";

    if (status === "Loaded") {
        dataIndicator.classList.add("connected");
        dataBadge.className = "status-badge online";
    } else if (status === "Partial") {
        dataBadge.className = "status-badge partial";
    } else {
        dataBadge.className = "status-badge";
    }
}

function formatNumber(value) {
    if (value === null || value === undefined ||
        !Number.isFinite(Number(value))) {
        return "—";
    }

    return new Intl.NumberFormat().format(Number(value));
}

function setMetric(name, value) {
    if (metricElements[name]) {
        metricElements[name].textContent = formatNumber(value);
    }
}

function getProperty(object, ...keys) {
    for (const key of keys) {
        if (object?.[key] !== undefined && object[key] !== null) {
            return object[key];
        }
    }

    return undefined;
}

async function fetchJson(url) {
    const response = await fetch(url, {
        method: "GET",
        headers: {
            "Accept": "application/json"
        },
        credentials: "same-origin"
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status} for ${url}`);
    }

    return response.json();
}

function getRecordCount(data) {
    if (Array.isArray(data)) {
        return data.length;
    }

    // Also accept APIs that return { data: [...] } or { items: [...] }.
    if (Array.isArray(data?.data)) {
        return data.data.length;
    }

    if (Array.isArray(data?.items)) {
        return data.items.length;
    }

    throw new Error("The endpoint did not return a record list.");
}

function updateDate() {
    dashboardElements.date.textContent =
        new Intl.DateTimeFormat(undefined, {
            weekday: "short",
            day: "numeric",
            month: "short",
            year: "numeric"
        }).format(new Date());
}

function initializeSidebar() {
    dashboardElements.sidebarToggle.addEventListener("click", () => {
        const collapsed = appShell.classList.toggle("sidebar-collapsed");

        dashboardElements.sidebarToggle.setAttribute(
            "aria-expanded",
            String(!collapsed)
        );
    });

    document.querySelectorAll(".nav-group-toggle").forEach(button => {
        button.addEventListener("click", () => {
            const expanded =
                button.getAttribute("aria-expanded") === "true";

            button.setAttribute("aria-expanded", String(!expanded));
        });
    });

    // Start with the sidebar closed on smaller screens.
    if (window.innerWidth <= 950) {
        appShell.classList.add("sidebar-collapsed");
        dashboardElements.sidebarToggle.setAttribute(
            "aria-expanded",
            "false"
        );
    }

    // Close the mobile sidebar after navigation.
    document.querySelectorAll(".navigation a").forEach(link => {
        link.addEventListener("click", () => {
            if (window.innerWidth <= 950) {
                appShell.classList.add("sidebar-collapsed");
                dashboardElements.sidebarToggle.setAttribute(
                    "aria-expanded",
                    "false"
                );
            }
        });
    });
}

function initializeModuleSearch() {
    const cards = [
        ...dashboardElements.moduleGrid.querySelectorAll(".module-card")
    ];

    dashboardElements.moduleSearch.addEventListener("input", () => {
        const query =
            dashboardElements.moduleSearch.value.trim().toLowerCase();

        let visibleCount = 0;

        for (const card of cards) {
            const searchableText = [
                card.textContent,
                card.dataset.module
            ].join(" ").toLowerCase();

            const visible = searchableText.includes(query);

            card.classList.toggle("hidden", !visible);

            if (visible) {
                visibleCount++;
            }
        }

        dashboardElements.noModules.classList.toggle(
            "hidden",
            visibleCount !== 0
        );
    });
}

async function loadSummary() {
    const data = await fetchJson(DASHBOARD_API.summary);

    const flights = getProperty(data, "totalFlights", "flights");
    const reservations = getProperty(
        data, "totalReservations", "reservations"
    );
    const passengers = getProperty(
        data, "totalPassengers", "passengers"
    );
    const aircraft = getProperty(data, "totalAircraft", "aircraft");

    if (flights !== undefined) {
        setMetric(
            "flights",
            typeof flights === "number" ? flights : getRecordCount(flights)
        );
    }

    if (reservations !== undefined) {
        setMetric(
            "reservations",
            typeof reservations === "number"
                ? reservations
                : getRecordCount(reservations)
        );
    }

    if (passengers !== undefined) {
        setMetric(
            "passengers",
            typeof passengers === "number"
                ? passengers
                : getRecordCount(passengers)
        );
    }

    if (aircraft !== undefined) {
        setMetric(
            "aircraft",
            typeof aircraft === "number"
                ? aircraft
                : getRecordCount(aircraft)
        );
    }

    const hasAllMetrics = [
        flights, reservations, passengers, aircraft
    ].every(value => value !== undefined);

    return hasAllMetrics;
}

async function loadCountsIndividually() {
    const endpoints = [
        ["flights", DASHBOARD_API.flights],
        ["reservations", DASHBOARD_API.reservations],
        ["passengers", DASHBOARD_API.passengers],
        ["aircraft", DASHBOARD_API.aircraft]
    ];

    const results = await Promise.allSettled(
        endpoints.map(async ([name, url]) => {
            const data = await fetchJson(url);
            setMetric(name, getRecordCount(data));
            return name;
        })
    );

    const successes = results.filter(
        result => result.status === "fulfilled"
    ).length;

    return {
        successes,
        total: endpoints.length
    };
}

async function refreshDashboard() {
    if (dashboardLoading) {
        return;
    }

    dashboardLoading = true;
    dashboardElements.refresh.disabled = true;
    dashboardElements.checkStatus.disabled = true;

    clearDashboardMessage();

    try {
        let summaryLoaded = false;

        try {
            summaryLoaded = await loadSummary();
        } catch {
            // The summary endpoint may not exist yet.
            // Try the individual list endpoints below.
        }

        if (summaryLoaded) {
            setConnectionStatus(true, "API and summary endpoint responding");
            setDataStatus("Loaded", "Summary data loaded");
            showDashboardMessage(
                "Dashboard updated successfully.",
                "success"
            );
        } else {
            const result = await loadCountsIndividually();

            if (result.successes === result.total) {
                setConnectionStatus(true, "List endpoints responding");
                setDataStatus("Loaded", "Counts loaded from list endpoints");
                showDashboardMessage(
                    "Dashboard updated using individual API endpoints.",
                    "success"
                );
            } else if (result.successes > 0) {
                setConnectionStatus(true, "Some endpoints responding");
                setDataStatus(
                    "Partial",
                    `${result.successes} of ${result.total} lists loaded`
                );
                showDashboardMessage(
                    "Some dashboard counts could not be loaded. " +
                    "Check that the remaining API endpoints are implemented.",
                    "info"
                );
            } else {
                throw new Error("No dashboard data endpoints are available.");
            }
        }
    } catch (error) {
        setConnectionStatus(false, "Could not load dashboard data");
        setDataStatus("Unavailable", "No usable data returned");

        showDashboardMessage(
            `${error.message} Make sure the Java server is running and ` +
            "the API endpoints are implemented.",
            "error"
        );
    } finally {
        dashboardLoading = false;
        dashboardElements.refresh.disabled = false;
        dashboardElements.checkStatus.disabled = false;
    }
}

async function checkConnection() {
    dashboardElements.checkStatus.disabled = true;

    try {
        // Use an existing endpoint as the connectivity check.
        await fetchJson(DASHBOARD_API.flights);

        setConnectionStatus(true, "Flights endpoint responding");
        showDashboardMessage(
            "The backend API is reachable.",
            "success"
        );
    } catch (error) {
        setConnectionStatus(false, "Flights endpoint unavailable");

        showDashboardMessage(
            `Could not reach the API: ${error.message}`,
            "error"
        );
    } finally {
        dashboardElements.checkStatus.disabled = false;
    }
}

function initialize() {
    updateDate();
    initializeSidebar();
    initializeModuleSearch();

    dashboardElements.refresh.addEventListener(
        "click",
        refreshDashboard
    );

    dashboardElements.checkStatus.addEventListener(
        "click",
        checkConnection
    );

    refreshDashboard();
}

document.addEventListener("DOMContentLoaded", initialize);
