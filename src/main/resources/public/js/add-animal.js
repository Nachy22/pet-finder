// Full add-animal form: validates fields and uploads one real listing to Javalin/MySQL.
const addButton = document.getElementById("add-animal");
const addSection = document.getElementById("add-animal-section");
const addForm = document.getElementById("add-animal-form");
const speciesSelect = document.getElementById("animal-species");
const healthRows = document.getElementById("health-rows");
const photosInput = document.getElementById("animal-photos");
const publishMessage = document.getElementById("publish-message");
const SPECIES = {
  Pet: ["Dog", "Cat", "Rabbit", "Bird", "Guinea pig", "Other"],
  Livestock: ["Cattle", "Goat", "Sheep", "Pig", "Chicken", "Duck", "Donkey", "Other"]
};
const MAX_PHOTOS = 8;
let healthRowCount = 0;
function chosenType() {
  return addForm.querySelector('input[name="animalType"]:checked')?.value || "";
}
function value(id) { return document.getElementById(id).value.trim(); }
function formError(id, message) {
  const element = document.getElementById(id);
  const note = document.getElementById(id + "-error");
  if (note) { note.textContent = message; note.hidden = !message; }
  if (message) element.setAttribute("aria-invalid", "true");
  else element.removeAttribute("aria-invalid");
  return !message;
}
function required(id, label) { return formError(id, value(id) ? "" : label + " is required."); }
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
document.getElementById("cancel-add-animal").addEventListener("click", closeAddAnimal);
addForm.addEventListener("change", event => {
  if (event.target.name !== "animalType") return;
  const type = chosenType();
  speciesSelect.replaceChildren(new Option("Choose species", ""));
  for (const species of (SPECIES[type] || [])) speciesSelect.add(new Option(species, species));
  document.getElementById("temperament-field").hidden = type !== "Pet";
  document.getElementById("purpose-field").hidden = type !== "Livestock";
  formError("animal-type", "");
});
function today() {
  const now = new Date();
  const pad = num => String(num).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}
function renumberHealthRows() {
  const rows = [...healthRows.querySelectorAll(".health-row")];
  rows.forEach((row, index) => {
    const number = index + 1;
    row.querySelector(".health-name-label").textContent = "Vaccination " + number;
    row.querySelector(".health-date-which").textContent = " (vaccination " + number + ")";
    row.querySelector(".health-remove").hidden = rows.length === 1;
    row.querySelector(".health-remove").setAttribute("aria-label", "Remove vaccination " + number);
  });
}
function addHealthRow() {
  const row = document.getElementById("health-row-template").content.firstElementChild.cloneNode(true);
  const number = ++healthRowCount;
  const name = row.querySelector(".health-name");
  const date = row.querySelector(".health-date");
  name.id = "health-name-" + number;
  date.id = "health-date-" + number;
  row.querySelector(".health-name-label").htmlFor = name.id;
  row.querySelector(".health-date-label").htmlFor = date.id;
  row.querySelector(".health-name-error").id = name.id + "-error";
  row.querySelector(".health-date-error").id = date.id + "-error";
  date.max = today();
  row.querySelector(".health-remove").addEventListener("click", () => {
    row.remove();
    renumberHealthRows();
  });
  healthRows.append(row);
  renumberHealthRows();
  return row;
}
document.getElementById("add-health-row").addEventListener("click", () => addHealthRow());
function getHealthRecords() {
  const result = [];
  let good = true;
  for (const row of healthRows.querySelectorAll(".health-row")) {
    const name = row.querySelector(".health-name").value.trim();
    const date = row.querySelector(".health-date").value;
    if (name && date && date <= today()) result.push({ vaccine: name, date });
    else if (name || date) good = false;
  }
  if (!result.length || !good) {
    const note = document.getElementById("health-error");
    note.hidden = false;
    note.textContent = "Provide at least one complete vaccination or treatment record (not dated in the future).";
    return null;
  }
  document.getElementById("health-error").hidden = true;
  return result;
}
function validatePhotos() {
  const files = [...photosInput.files];
  const ok = files.length <= MAX_PHOTOS && files.every(file =>
    ["image/jpeg", "image/png", "image/webp"].includes(file.type) && file.size <= 5 * 1024 * 1024
  );
  const note = document.getElementById("photos-error");
  note.textContent = ok ? "" : "Choose up to 8 JPG, PNG or WebP photos, each at most 5 MB.";
  note.hidden = ok;
  return ok;
}
photosInput.addEventListener("change", () => {
  const n = photosInput.files.length;
  document.getElementById("photos-chosen").textContent = n === 0 ? "" : `Chosen: ${n} photo(s)`;
  validatePhotos();
});
addForm.addEventListener("submit", async event => {
  event.preventDefault();
  let good = true;
  for (const [id, label] of [
    ["animal-name", "Name"], ["animal-species", "Species"],
    ["animal-breed", "Breed"], ["animal-colour", "Colour"],
    ["animal-age", "Age group"], ["animal-sex", "Sex"]
  ]) good = required(id, label) && good;
  const type = chosenType();
  good = formError("animal-type", type ? "" : "Choose Pet or Livestock.") && good;
  if (type === "Pet") good = required("animal-temperament", "Temperament") && good;
  if (type === "Livestock") good = required("animal-purpose", "Purpose") && good;
  good = formError("animal-price", /^\d+$/.test(value("animal-price"))
    ? "" : "Enter a whole number of kwacha, 0 or more.") && good;
  good = validatePhotos() && good;
  const records = getHealthRecords();
  if (!good || !records) return;

  const animal = {
    type, name: value("animal-name"), species: value("animal-species"),
    breed: value("animal-breed"), colour: value("animal-colour"),
    lifeStage: value("animal-age"), sex: value("animal-sex"), healthRecords: records
  };
  if (type === "Pet") animal.temperament = value("animal-temperament");
  else animal.purpose = value("animal-purpose");
  const payload = { animal, price: value("animal-price"), quantity: 1 };
  const formData = new FormData();
  formData.append("payload", JSON.stringify(payload));
  for (const photo of photosInput.files) formData.append("photos", photo);
  const button = addForm.querySelector('button[type="submit"]');
  button.disabled = true;
  try {
    const listing = await apiRequest("/api/listings", { method: "POST", body: formData });
    closeAddAnimal();
    publishMessage.textContent = listing.animal.name + " has been saved to MySQL. " +
      (mySeller?.verified ? "It is visible to buyers." : "It will become public after seller verification.");
    publishMessage.hidden = false;
    publishMessage.focus();
    await window.refreshDashboard();
  } catch (error) {
    publishMessage.textContent = "Could not save listing: " + error.message;
    publishMessage.hidden = false;
  } finally { button.disabled = false; }
});
function resetForm() {
  addForm.reset();
  addForm.querySelectorAll("[aria-invalid]").forEach(el => el.removeAttribute("aria-invalid"));
  addForm.querySelectorAll(".field-error").forEach(note => { note.hidden = true; note.textContent = ""; });
  speciesSelect.replaceChildren(new Option("Choose Pet or Livestock first", ""));
  document.getElementById("temperament-field").hidden = true;
  document.getElementById("purpose-field").hidden = true;
  document.getElementById("photos-chosen").textContent = "";
  healthRows.replaceChildren();
  addHealthRow();
}
resetForm();
