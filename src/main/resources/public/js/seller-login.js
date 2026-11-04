const loginForm = document.getElementById("login-form");
const loginInput = document.getElementById("login-id");
const passwordInput = document.getElementById("login-password");
const alertBox = document.getElementById("login-alert");
const FAILED_LOGIN = "Email or phone, or password is incorrect. Please try again.";
function showError(input, text) {
  const error = document.getElementById(input.id + "-error");
  error.textContent = text;
  error.hidden = !text;
  if (text) input.setAttribute("aria-invalid", "true");
  else input.removeAttribute("aria-invalid");
}
for (const input of [loginInput, passwordInput]) {
  input.addEventListener("input", () => showError(input, ""));
}
loginForm.addEventListener("submit", async event => {
  event.preventDefault();
  alertBox.hidden = true;
  let valid = true;
  if (!loginInput.value.trim()) { showError(loginInput, "Enter your email or phone."); valid = false; }
  if (!passwordInput.value) { showError(passwordInput, "Enter your password."); valid = false; }
  if (!valid) return;
  const button = loginForm.querySelector('button[type="submit"]');
  button.disabled = true;
  try {
    const response = await fetch("/api/auth/login", {
      method: "POST", credentials: "same-origin",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ emailOrPhone: loginInput.value.trim(), password: passwordInput.value })
    });
    if (!response.ok) throw new Error(FAILED_LOGIN);
    window.location.assign("seller-dashboard.html");
  } catch {
    document.getElementById("login-alert-text").textContent = FAILED_LOGIN;
    alertBox.hidden = false;
    passwordInput.value = "";
    passwordInput.focus();
  } finally { button.disabled = false; }
});
document.getElementById("forgot-password").addEventListener("click", event => {
  event.preventDefault();
  document.getElementById("forgot-note").textContent = "Password reset is not available yet.";
});
