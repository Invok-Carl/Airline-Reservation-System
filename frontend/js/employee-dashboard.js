(() => {
  "use strict";
  const API = "/api/employee-auth";
  const LOGIN_PAGE = "/pages/login.html";
  const permissionDefinitions = [
    ["canCreateReservation", "Create reservations", "▤"],
    ["canCheckInPassenger", "Check in passengers", "✓"],
    ["canAddFlight", "Add flights", "✈"],
    ["canDeleteFlight", "Delete flights", "×"],
    ["canAddEmployee", "Add employees", "＋"],
    ["canManageEmployees", "Manage employees", "♙"]
  ];
  const byId = id => document.getElementById(id);
  const text = (id, value) => { const el = byId(id); if (el && value != null && String(value).trim()) el.textContent = value; };
  const first = (...values) => values.find(v => v !== undefined && v !== null && v !== "");

  function splitResponse(payload) {
    // Supports {employee: ..., permissions: ...} and a flat employee response.
    const employee = payload?.employee ?? payload?.data?.employee ?? payload?.data ?? payload ?? {};
    const permissions = payload?.permissions ?? payload?.data?.permissions ?? employee?.permissions ?? {};
    return { employee, permissions };
  }
  function isAllowed(value) { return value === true || value === 1 || value === "true"; }
  function nameOf(e) {
    const full = [e.firstName, e.lastName].filter(Boolean).join(" ").trim();
    return first(e.fullName, e.name, full, e.username, e.userName, "Employee");
  }
  function showPermissions(permissions) {
    document.querySelectorAll("[data-permission]").forEach(el => {
      el.hidden = !isAllowed(permissions[el.dataset.permission]);
    });
    document.querySelectorAll("[data-any-permission]").forEach(el => {
      el.hidden = !el.dataset.anyPermission.split(",").map(s => s.trim()).some(key => isAllowed(permissions[key]));
    });
    const managementLabel = document.querySelector(".management-heading");
    const keys = ["canAddFlight", "canDeleteFlight", "canManageEmployees", "canAddEmployee"];
    if (managementLabel) managementLabel.hidden = !keys.some(key => isAllowed(permissions[key]));
  }
  function renderPermissions(permissions) {
    const list = byId("permissionList");
    if (!list) return;
    list.replaceChildren();
    permissionDefinitions.forEach(([key, label, symbol]) => {
      const allowed = isAllowed(permissions[key]);
      const row = document.createElement("div"); row.className = "permission-row";
      const name = document.createElement("span"); name.className = "permission-name";
      const icon = document.createElement("span"); icon.className = "permission-symbol"; icon.textContent = symbol;
      icon.setAttribute("aria-hidden", "true");
      const labelEl = document.createElement("span"); labelEl.textContent = label;
      name.append(icon, labelEl);
      const state = document.createElement("span");
      state.className = "permission-state " + (allowed ? "allowed" : "denied");
      state.textContent = allowed ? "Allowed" : "Not allowed";
      row.append(name, state); list.append(row);
    });
  }
  function showError(message) {
    const banner = byId("authError");
    if (banner) banner.hidden = false;
    text("authErrorMessage", message);
  }
  async function loadEmployee() {
    try {
      const response = await fetch(`${API}/me`, {
        method: "GET", credentials: "same-origin", headers: { "Accept": "application/json" }
      });
      if (response.status === 401 || response.status === 403) {
        showError("Your session may have expired. Please sign in again.");
        return;
      }
      if (!response.ok) throw new Error(`Server returned HTTP ${response.status}.`);
      const payload = await response.json();
      const { employee, permissions } = splitResponse(payload);
      const name = nameOf(employee);
      const role = first(employee.position, employee.role, employee.employeePosition, employee.jobTitle, "Employee");
      const username = first(employee.username, employee.userName, employee.email, "Signed-in employee");
      const initial = String(name).trim().charAt(0).toUpperCase() || "E";
      text("welcomeName", name);
      text("employeeName", name);
      text("employeeUsername", username);
      text("employeeRole", String(role).replaceAll("_", " "));
      text("headerAvatar", initial);
      text("profileAvatar", initial);
      showPermissions(permissions);
      renderPermissions(permissions);
    } catch (error) {
      console.error("Unable to load employee session:", error);
      showError("Could not load your employee session. Check that the server is running, then refresh.");
    }
  }
  async function logout() {
    const buttons = [byId("logoutButton"), byId("accountLogoutButton")].filter(Boolean);
    buttons.forEach(button => { button.disabled = true; button.setAttribute("aria-busy", "true"); });
    try {
      const response = await fetch(`${API}/logout`, {
        method: "POST", credentials: "same-origin", headers: { "Accept": "application/json" }
      });
      if (!response.ok) console.warn(`Logout endpoint returned HTTP ${response.status}.`);
    } catch (error) {
      console.error("Logout request failed:", error);
    } finally {
      window.location.assign(LOGIN_PAGE);
    }
  }
  function setupMobileMenu() {
    const sidebar = byId("sidebar"), button = byId("menuButton"), overlay = byId("mobileOverlay");
    const close = () => {
      sidebar?.classList.remove("is-open");
      if (overlay) overlay.hidden = true;
      button?.setAttribute("aria-expanded", "false");
    };
    button?.addEventListener("click", () => {
      const open = sidebar.classList.toggle("is-open");
      overlay.hidden = !open;
      button.setAttribute("aria-expanded", String(open));
    });
    overlay?.addEventListener("click", close);
    sidebar?.querySelectorAll("a").forEach(a => a.addEventListener("click", close));
  }
  document.addEventListener("DOMContentLoaded", () => {
    const now = new Date();
    text("currentDate", new Intl.DateTimeFormat(undefined, {weekday:"short", day:"numeric", month:"short", year:"numeric"}).format(now));
    text("footerYear", now.getFullYear());
    setupMobileMenu();
    byId("logoutButton")?.addEventListener("click", logout);
    byId("accountLogoutButton")?.addEventListener("click", logout);
    loadEmployee();
  });
})();
