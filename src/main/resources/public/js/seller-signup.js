// Seller sign-up page: checks every field and shows a message under any that need fixing.
// Nothing is sent or uploaded yet; a correct form just shows a demo success message.

const form = document.getElementById("signup-form");
const documentInput = document.getElementById("document");
const documentChosen = document.getElementById("document-chosen");
const success = document.getElementById("signup-success");

const MAX_DOCUMENT_BYTES = 5 * 1024 * 1024;
const DOCUMENT_TYPES = ["application/pdf", "image/jpeg", "image/png"];

// Each check returns an error message, or "" when the field is fine.
const checks = {
  "business-name": (value) => (value ? "" : "Enter your business or seller name."),
  "contact-person": (value) => (value ? "" : "Enter the name of the person buyers should contact."),
  phone: (value) => {
    if (!value) return "Enter a phone number.";
    // Allows spaces and a leading +, e.g. "+260 97 000 0001" or "0970000001"
    return /^\+?\d{9,13}$/.test(value.replace(/[\s-]/g, "")) ? "" : "Enter a phone number like 097 000 0001.";
  },
  email: (value) => {
    if (!value) return "Enter an email address.";
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? "" : "Enter an email address like name@example.com.";
  },
  town: (value) => (value ? "" : "Choose a town."),
  area: (value) => (value ? "" : "Enter your area, e.g. Kansenshi."),
  password: (value) => {
    if (!value) return "Enter a password.";
    if (value.length < 8 || !/\d/.test(value)) return "Use at least 8 characters, with a number.";
    return "";
  },
};

function showError(fieldId, errorId, text) {
  const error = document.getElementById(errorId);
  error.textContent = text;
  error.hidden = !text;
  const field = document.getElementById(fieldId);
  if (text) field.setAttribute("aria-invalid", "true");
  else field.removeAttribute("aria-invalid");
}

function checkTextField(id) {
  const message = checks[id](document.getElementById(id).value.trim());
  showError(id, id + "-error", message);
  return message === "";
}

function checkSellerType() {
  const chosen = form.querySelector('input[name="sellerType"]:checked');
  showError("seller-type", "seller-type-error", chosen ? "" : "Choose Breeder, Farmer or Individual.");
  return Boolean(chosen);
}

function checkDocument() {
  const file = documentInput.files[0];
  let message = "";
  if (!file) message = "Choose a verification document.";
  else if (!DOCUMENT_TYPES.includes(file.type)) message = "The document must be a PDF, JPG or PNG file.";
  else if (file.size > MAX_DOCUMENT_BYTES) message = "The document is too big. It must be 5 MB or smaller.";
  showError("document", "document-error", message);
  document.getElementById("upload-box").classList.toggle("is-invalid", message !== "");
  return message === "";
}

// Show the chosen file's name in the upload box
documentInput.addEventListener("change", () => {
  const file = documentInput.files[0];
  documentChosen.textContent = file ? "Chosen: " + file.name : "";
  checkDocument();
});

// Once a field has shown an error, re-check it as the person fixes it
for (const id of Object.keys(checks)) {
  const field = document.getElementById(id);
  const recheck = () => {
    if (field.hasAttribute("aria-invalid")) checkTextField(id);
  };
  field.addEventListener("input", recheck);
  field.addEventListener("change", recheck);
}

form.addEventListener("change", (event) => {
  if (event.target.name === "sellerType") checkSellerType();
});

form.addEventListener("submit", (event) => {
  event.preventDefault();

  // Check every field (not stopping at the first problem) so all messages show at once
  const results = [
    checkTextField("business-name"),
    checkSellerType(),
    checkTextField("contact-person"),
    checkTextField("phone"),
    checkTextField("email"),
    checkTextField("town"),
    checkTextField("area"),
    checkDocument(),
    checkTextField("password"),
  ];

  if (results.includes(false)) {
    // Move to the first field that needs fixing
    const firstInvalid = form.querySelector('[aria-invalid="true"]');
    const focusTarget = firstInvalid.matches("fieldset") ? firstInvalid.querySelector("input") : firstInvalid;
    focusTarget.focus();
    return;
  }

  // DEMO: nothing is sent or uploaded. Replace this with a call to the backend when it's ready.
  form.hidden = true;
  success.hidden = false;
  success.focus();
});
