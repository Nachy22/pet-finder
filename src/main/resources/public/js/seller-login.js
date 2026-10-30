// Seller login page: checks the fields are filled in, then "logs in" and goes to the dashboard.

// =====================================================================================
// TEMPORARY FAKE LOGIN, for building the front end only.
// Any non-empty email or phone works with the password below. Nothing is checked
// against real accounts and nothing is secure about this.
// This MUST be replaced by the real login from the backend before the site goes live:
// send the email/phone and password to the backend, and only go to the dashboard
// if the backend says they are correct. Then delete DEMO_PASSWORD.
// =====================================================================================
const DEMO_PASSWORD = "demo123";

const form = document.getElementById("login-form");
const loginInput = document.getElementById("login-id");
const passwordInput = document.getElementById("login-password");
const alertBox = document.getElementById("login-alert");

const FAILED_LOGIN_MESSAGE = "Email or phone, or password is incorrect. Please try again.";

function showError(input, text) {
  const error = document.getElementById(input.id + "-error");
  error.textContent = text;
  error.hidden = !text;
  if (text) input.setAttribute("aria-invalid", "true");
  else input.removeAttribute("aria-invalid");
}

function checkFilledIn(input, message) {
  const empty = input.value.trim() === "";
  showError(input, empty ? message : "");
  return !empty;
}

function showFailedLogin() {
  document.getElementById("login-alert-text").textContent = FAILED_LOGIN_MESSAGE;
  alertBox.hidden = false;
  // Clear the password so it can be typed again, and put the cursor there
  passwordInput.value = "";
  passwordInput.focus();
}

// Once a field has shown a message, clear it as soon as something is typed
for (const input of [loginInput, passwordInput]) {
  input.addEventListener("input", () => {
    if (input.hasAttribute("aria-invalid")) showError(input, "");
  });
}

form.addEventListener("submit", (event) => {
  event.preventDefault();
  alertBox.hidden = true;

  const loginFilled = checkFilledIn(loginInput, "Enter your email or phone.");
  const passwordFilled = checkFilledIn(passwordInput, "Enter your password.");
  if (!loginFilled || !passwordFilled) {
    (loginFilled ? passwordInput : loginInput).focus();
    return;
  }

  // TEMPORARY FAKE LOGIN (see the note at the top of this file)
  if (passwordInput.value === DEMO_PASSWORD) {
    window.location.href = "seller-dashboard.html";
  } else {
    showFailedLogin();
  }
});

// Password reset isn't built yet, so the link only says so.
document.getElementById("forgot-password").addEventListener("click", (event) => {
  event.preventDefault();
  document.getElementById("forgot-note").textContent = "Password reset isn't available yet (demo).";
});
