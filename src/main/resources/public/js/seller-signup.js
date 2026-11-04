const form = document.getElementById("signup-form");
const docInput = document.getElementById("document");
const chosen = document.getElementById("document-chosen");
const success = document.getElementById("signup-success");
const submit = form.querySelector('button[type="submit"]');

function field(id) { return document.getElementById(id); }
function error(id, message) {
  const note = field(id + "-error");
  if (note) { note.textContent = message; note.hidden = !message; }
  if (message) field(id).setAttribute("aria-invalid", "true");
  else field(id).removeAttribute("aria-invalid");
  return !message;
}
function validate() {
  let good = true;
  for (const id of ["business-name", "contact-person", "town", "area"]) {
    good = error(id, field(id).value.trim() ? "" : "This field is required.") && good;
  }
  good = error("seller-type", form.querySelector('input[name="sellerType"]:checked')
    ? "" : "Choose your seller type.") && good;
  const phone = field("phone").value.replace(/[\s-]/g, "");
  good = error("phone", /^\+?\d{9,13}$/.test(phone) ? "" : "Enter a valid phone number.") && good;
  good = error("email", /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(field("email").value.trim())
    ? "" : "Enter a valid email address.") && good;
  const password = field("password").value;
  good = error("password", password.length >= 8 && /\d/.test(password)
    ? "" : "Use at least 8 characters including a number.") && good;
  const file = docInput.files[0];
  good = error("document", !file ? "Choose a verification document."
    : file.size > 5 * 1024 * 1024 ? "Maximum file size is 5 MB."
    : !["application/pdf", "image/jpeg", "image/png"].includes(file.type)
      ? "Choose PDF, JPG or PNG." : "") && good;
  return good;
}
docInput.addEventListener("change", () => {
  chosen.textContent = docInput.files[0] ? "Chosen: " + docInput.files[0].name : "";
  error("document", "");
});
form.addEventListener("submit", async event => {
  event.preventDefault();
  if (!validate()) return;
  const data = new FormData(form);
  submit.disabled = true;
  try {
    const res = await fetch("/api/auth/register", {
      method: "POST", body: data, credentials: "same-origin"
    });
    const result = await res.json();
    if (!res.ok) throw new Error(result.error || "Registration failed");
    form.hidden = true;
    success.hidden = false;
    success.querySelector(".form-success-title").textContent = "Account created";
    success.querySelector(".field-hint")?.remove();
    success.focus();
  } catch (err) {
    const note = field("signup-error") || document.createElement("p");
    note.id = "signup-error";
    note.className = "form-feedback is-error";
    note.textContent = err.message;
    if (!note.isConnected) submit.before(note);
  } finally { submit.disabled = false; }
});
