const API_URL = "http://localhost:8080/api/employee-auth/login";

document.getElementById("employeeLoginForm").addEventListener("submit", async function (e) {
  e.preventDefault();
  const msg = document.getElementById("msg");
  msg.textContent = "";

  const employeeId = document.getElementById("employeeId").value.trim();
  const password = document.getElementById("password").value;

  try {
    const res = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: employeeId, password: password })
    });

    if (!res.ok) {
      msg.textContent = "Wrong ID or password.";
      return;
    }

    const data = await res.json();

    // د ننوتلو ساتل
    sessionStorage.setItem("employee", JSON.stringify(data));
    sessionStorage.setItem("role", "employee");

    window.location.href = "employee-dashboard.html";
  } catch (err) {
    msg.textContent = "Cannot reach the server.";
  }
});