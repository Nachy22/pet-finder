// Add animal form on the seller dashboard.
// Needs js/data.js and js/seller-dashboard.js to be loaded first (it uses buildRow and DEMO_SELLER_ID).

// DEMO: publishing only adds a row to the table on this page. Nothing is saved, and the
// row disappears when the page is reloaded. Replace with a call to the backend when it's ready.

const addButton = document.getElementById("add-animal");
const addSection = document.getElementById("add-animal-section");
const addForm = document.getElementById("add-animal-form");
const speciesSelect = document.getElementById("animal-species");
const healthRows = document.getElementById("health-rows");
const photosInput = document.getElementById("animal-photos");
const publishMessage = document.getElementById("publish-message");

const SPECIES = {
  Pet: ["Dog", "Cat", "Rabbit", "Bird", "Guinea pig", "Other"],
  Livestock: ["Cattle", "Goat", "Sheep", "Pig", "Chicken", "Duck", "Donkey", "Other"],
};
const MAX_PHOTOS = 8;
const HEALTH_REQUIRED_MESSAGE = "Add at least one health or vaccination record before publishing.";

let demoListingId = 1000; // ids for demo listings, so they don't clash with the sample data

// ---------- Showing and hiding the form ----------

function openAddAnimal() {
  addSection.hidden = false;
  addButton.setAttribute("aria-expanded", "true");
  publishMessage.hidden = true;
  addSection.scrollIntoView({ behavior: "smooth", block: "start" });
  document.getElementById("add-animal-heading").focus({ preventScroll: true });
}

function closeAddAnimal() {
  addSection.hidden = true;
  addButton.setAttribute("aria-expanded", "false");
  resetForm();
}

addButton.addEventListener("click", openAddAnimal);
document.getElementById("cancel-add-animal").addEventListener("click", () => {
  closeAddAnimal();
  addButton.focus();
});

// ---------- Field errors ----------

function showError(field, errorElement, text) {
  errorElement.textContent = text;
  errorElement.hidden = !text;
  if (text) field.setAttribute("aria-invalid", "true");
  else field.removeAttribute("aria-invalid");
  return !text;
}

function fieldError(id, text) {
  return showError(document.getElementById(id), document.getElementById(id + "-error"), text);
}

function requireValue(id, message) {
  return fieldError(id, document.getElementById(id).value.trim() ? "" : message);
}

// Clear a field's message as soon as the person changes it
addForm.addEventListener("input", (event) => {
  const field = event.target;
  if (field.getAttribute("aria-invalid") !== "true") return;
  const error = field.closest(".field")?.querySelector(".field-error");
  if (error) showError(field, error, "");
});

// ---------- Pet or Livestock ----------

function chosenType() {
  return addForm.querySelector('input[name="animalType"]:checked')?.value || "";
}

addForm.addEventListener("change", (event) => {
  if (event.target.name !== "animalType") return;
  const type = chosenType();
  fieldError("animal-type", "");

  // Species list depends on the type
  speciesSelect.replaceChildren(new Option("Choose species", ""));
  for (const species of SPECIES[type]) speciesSelect.add(new Option(species, species));
  fieldError("animal-species", "");

  // Pets ask for temperament, Livestock for purpose
  document.getElementById("temperament-field").hidden = type !== "Pet";
  document.getElementById("purpose-field").hidden = type !== "Livestock";
});

// ---------- Vaccination rows ----------

// Today's local date as "YYYY-MM-DD", the format date inputs use
function today() {
  const now = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return now.getFullYear() + "-" + pad(now.getMonth() + 1) + "-" + pad(now.getDate());
}

function renumberHealthRows() {
  const rows = healthRows.querySelectorAll(".health-row");
  rows.forEach((row, index) => {
    const number = index + 1;
    row.querySelector(".health-name-label").textContent = "Vaccination " + number;
    row.querySelector(".health-date-which").textContent = " (vaccination " + number + ")";
    row.querySelector(".health-remove").setAttribute("aria-label", "Remove vaccination " + number);
    // The first row can't be removed, so there is always at least one
    row.querySelector(".health-remove").hidden = rows.length === 1;
  });
}

let healthRowCount = 0; // only used to make unique ids
function addHealthRow() {
  const row = document.getElementById("health-row-template").content.firstElementChild.cloneNode(true);
  const n = ++healthRowCount;
  const name = row.querySelector(".health-name");
  const date = row.querySelector(".health-date");
  name.id = "health-name-" + n;
  date.id = "health-date-" + n;
  row.querySelector(".health-name-label").htmlFor = name.id;
  row.querySelector(".health-date-label").htmlFor = date.id;
  row.querySelector(".health-name-error").id = name.id + "-error";
  row.querySelector(".health-date-error").id = date.id + "-error";
  name.setAttribute("aria-describedby", name.id + "-error");
  date.setAttribute("aria-describedby", date.id + "-error");
  date.max = today();

  row.querySelector(".health-remove").addEventListener("click", () => {
    row.remove();
    renumberHealthRows();
    healthRows.querySelector(".health-name").focus();
  });

  healthRows.append(row);
  renumberHealthRows();
  return row;
}

document.getElementById("add-health-row").addEventListener("click", () => {
  addHealthRow().querySelector(".health-name").focus();
});

// Returns the filled-in records, or null if any row is half filled in or wrong.
function readHealthRecords() {
  const records = [];
  let ok = true;

  for (const row of healthRows.querySelectorAll(".health-row")) {
    const name = row.querySelector(".health-name");
    const date = row.querySelector(".health-date");
    const nameValue = name.value.trim();
    const dateValue = date.value;

    let nameMessage = "";
    let dateMessage = "";
    if (nameValue && !dateValue) dateMessage = "Enter the date this was given.";
    if (!nameValue && dateValue) nameMessage = "Enter the vaccine or treatment name.";
    if (dateValue && dateValue > today()) dateMessage = "The date can't be in the future.";

    ok = showError(name, row.querySelector(".health-name-error"), nameMessage) && ok;
    ok = showError(date, row.querySelector(".health-date-error"), dateMessage) && ok;

    if (nameValue && dateValue && !nameMessage && !dateMessage) {
      records.push({ vaccine: nameValue, date: dateValue });
    }
  }
  return ok ? records : null;
}

// ---------- Photos ----------

function checkPhotos() {
  const files = [...photosInput.files];
  let message = "";
  if (files.length > MAX_PHOTOS) message = "Choose up to " + MAX_PHOTOS + " photos.";
  else if (files.some((f) => !f.type.startsWith("image/"))) message = "Photos must be JPG, PNG or WebP images.";
  document.getElementById("photos-box").classList.toggle("is-invalid", message !== "");
  return showError(photosInput, document.getElementById("photos-error"), message);
}

photosInput.addEventListener("change", () => {
  const count = photosInput.files.length;
  document.getElementById("photos-chosen").textContent =
    count === 0 ? "" : count === 1 ? "Chosen: " + photosInput.files[0].name : "Chosen: " + count + " photos";
  checkPhotos();
});

// ---------- Publishing ----------

function validate() {
  const type = chosenType();
  // Check every field (not stopping at the first problem) so all messages show at once
  const results = [
    requireValue("animal-name", "Enter the animal's name."),
    fieldError("animal-type", type ? "" : "Choose Pet or Livestock."),
    requireValue("animal-species", type ? "Choose a species." : "Choose Pet or Livestock first, then a species."),
    requireValue("animal-breed", "Enter the breed, or \"Mixed\" if you're not sure."),
    requireValue("animal-colour", "Enter the colour."),
    requireValue("animal-age", "Choose an age group."),
    requireValue("animal-sex", "Choose the sex."),
    type === "Pet" ? requireValue("animal-temperament", "Describe the pet's temperament, e.g. calm or playful.") : true,
    type === "Livestock" ? requireValue("animal-purpose", "Choose a purpose.") : true,
    checkPrice(),
    checkPhotos(),
  ];

  const records = readHealthRecords();
  const healthError = document.getElementById("health-error");
  const noRecords = records !== null && records.length === 0;
  healthError.textContent = noRecords ? HEALTH_REQUIRED_MESSAGE : "";
  healthError.hidden = !noRecords;
  document.getElementById("health-fieldset").classList.toggle("is-invalid", noRecords);

  const valid = !results.includes(false) && records !== null && records.length > 0;
  return valid ? records : null;
}

function checkPrice() {
  const value = document.getElementById("animal-price").value.trim();
  let message = "";
  if (value === "") message = "Enter a price, or 0 to list the animal as Free.";
  else if (!/^\d+$/.test(value)) message = "Enter a whole number of kwacha, 0 or more.";
  return fieldError("animal-price", message);
}

function focusFirstProblem() {
  const first = addForm.querySelector('[aria-invalid="true"]');
  if (first) {
    (first.matches("fieldset") ? first.querySelector("input") : first).focus();
  } else {
    // Only the "no health record" message is showing
    healthRows.querySelector(".health-name").focus();
  }
}

addForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const records = validate();
  if (!records) {
    focusFirstProblem();
    return;
  }

  const type = chosenType();
  const value = (id) => document.getElementById(id).value.trim();
  const listing = {
    id: ++demoListingId,
    sellerId: DEMO_SELLER_ID,
    status: "Available",
    price: Number(value("animal-price")),
    quantity: 1,
    animal: {
      type,
      name: value("animal-name"),
      species: value("animal-species"),
      breed: value("animal-breed"),
      lifeStage: value("animal-age"),
      sex: value("animal-sex"),
      colour: value("animal-colour"),
      healthRecords: records,
    },
    // DEMO: a temporary in-browser link to the first chosen photo; nothing is uploaded
    imageUrl: photosInput.files[0] ? URL.createObjectURL(photosInput.files[0]) : null,
  };
  if (type === "Pet") listing.animal.temperament = value("animal-temperament");
  else listing.animal.purpose = value("animal-purpose");

  addListingRow(listing);
  closeAddAnimal();

  publishMessage.textContent = "Listing published (demo). " + listing.animal.name + " has been added to your listings.";
  publishMessage.hidden = false;
  publishMessage.focus();
});

function addListingRow(listing) {
  const rows = document.getElementById("listings-rows");
  rows.querySelector(".listings-empty")?.closest("tr").remove();
  const row = buildRow(listing);
  row.classList.add("is-new");
  rows.append(row);
}

function resetForm() {
  addForm.reset();
  for (const field of addForm.querySelectorAll("[aria-invalid]")) field.removeAttribute("aria-invalid");
  for (const error of addForm.querySelectorAll(".field-error")) error.hidden = true;
  addForm.querySelectorAll(".is-invalid").forEach((el) => el.classList.remove("is-invalid"));
  speciesSelect.replaceChildren(new Option("Choose Pet or Livestock first", ""));
  document.getElementById("temperament-field").hidden = true;
  document.getElementById("purpose-field").hidden = true;
  document.getElementById("photos-chosen").textContent = "";
  healthRows.replaceChildren();
  addHealthRow();
}

resetForm();
