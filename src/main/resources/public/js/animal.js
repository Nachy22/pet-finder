// Real animal details and enquiries. Requires data.js.
const detailMessage = document.getElementById("detail-message");
const detail = document.getElementById("detail");
const scamDialog = document.getElementById("scam-dialog");
const scamThanks = document.getElementById("scam-report-thanks");
const scamReportButton = document.getElementById("scam-report");
let afterScamWarning = null;
let currentListing = null;
function setText(id, text) { document.getElementById(id).textContent = text; }
function formatDate(date) {
  return new Date(date).toLocaleDateString("en-GB", {
    day: "numeric", month: "short", year: "numeric", timeZone: "UTC"
  });
}
function showStatus(listing) {
  const badge = document.getElementById("status-badge");
  badge.textContent = listing.status;
  badge.classList.add(listing.status === "Available" ? "tag-green"
    : listing.status === "Reserved" ? "tag-amber" : "tag-grey");
  const notice = document.getElementById("status-notice");
  if (listing.status === "Reserved") {
    notice.textContent = "Reserved. Another buyer may be viewing this animal.";
    notice.classList.add("status-notice-reserved");
    notice.hidden = false;
  } else if (listing.status === "Rehomed") {
    notice.textContent = "Rehomed. This animal is no longer available.";
    notice.classList.add("status-notice-rehomed");
    notice.hidden = false;
    document.getElementById("detail-photo").classList.add("is-rehomed");
    document.getElementById("enquiry-closed").hidden = false;
    document.getElementById("enquiry-fields").disabled = true;
  }
}
function showPhoto(listing) {
  const photo = document.getElementById("detail-photo");
  const img = document.getElementById("animal-photo");
  if (!listing.imageUrl) { photo.classList.add("no-image"); return; }
  img.src = listing.imageUrl;
  img.alt = listing.animal.name + ", " + listing.animal.breed;
  img.addEventListener("error", () => photo.classList.add("no-image"));
}
function showFacts(listing) {
  const a = listing.animal;
  for (const [id, val] of Object.entries({
    "fact-breed": a.breed, "fact-species": a.species,
    "fact-colour": a.colour, "fact-age": a.lifeStage, "fact-sex": a.sex
  })) setText(id, val);
  if (listing.quantity > 1) {
    setText("fact-quantity", listing.quantity);
    document.getElementById("fact-quantity-box").hidden = false;
  }
}
function showHealthRecords(records) {
  const rows = document.getElementById("health-rows");
  for (const record of records || []) {
    const row = rows.insertRow();
    row.insertCell().textContent = record.vaccine;
    row.insertCell().textContent = formatDate(record.date);
  }
  if (!records?.length) {
    const row = rows.insertRow();
    const cell = row.insertCell();
    cell.colSpan = 2;
    cell.textContent = "No health record. Ask the seller for the original vet card.";
  }
}
function showBreed(breed, breeds) {
  const profile = breeds[breed];
  if (!profile) return;
  setText("breed-heading", "Breed profile: " + breed);
  setText("breed-summary", profile.summary);
  setText("breed-size", profile.adultSize);
  setText("breed-lifespan", profile.lifespan);
  setText("breed-needs", profile.needs);
  setText("breed-good", profile.goodToKnow);
  document.getElementById("breed-panel").hidden = false;
}
function showSeller(seller) {
  setText("seller-name", seller.businessName);
  const badge = document.getElementById("seller-badge");
  badge.textContent = seller.verified ? "✓ Verified" : "Verification pending";
  badge.classList.add(seller.verified ? "tag-green" : "tag-amber");
  const phone = document.getElementById("seller-phone");
  phone.textContent = seller.phone;
  phone.href = "tel:" + seller.phone;
  const wa = document.getElementById("seller-whatsapp");
  wa.textContent = seller.phone;
  wa.href = "https://wa.me/" + whatsappNumber(seller.phone);
  const email = document.getElementById("seller-email");
  email.textContent = seller.email;
  email.href = "mailto:" + seller.email;
  setText("seller-town", seller.town);
  setText("map-label", "Map placeholder · approximate area: " + seller.town);
}
function openScamWarning(callback) {
  afterScamWarning = callback;
  scamThanks.textContent = "";
  scamReportButton.disabled = false;
  document.body.classList.add("has-dialog");
  scamDialog.showModal();
}
document.getElementById("scam-understand").addEventListener("click", () => {
  const callback = afterScamWarning;
  afterScamWarning = null;
  scamDialog.close();
  if (callback) callback();
});
document.getElementById("scam-close").addEventListener("click", () => scamDialog.close());
scamDialog.addEventListener("close", () => {
  afterScamWarning = null;
  document.body.classList.remove("has-dialog");
});
scamReportButton.addEventListener("click", async () => {
  if (!currentListing) return;
  const reason = window.prompt("Why are you reporting this listing?", "Suspicious or misleading listing");
  if (reason === null || !reason.trim()) return;
  scamReportButton.disabled = true;
  try {
    await apiRequest("/api/reports", {
      method: "POST", body: { listingId: currentListing.id, reason: reason.trim() }
    });
    scamThanks.textContent = "Thank you. Your report has been recorded.";
  } catch (error) {
    scamThanks.textContent = "Could not send report: " + error.message;
    scamReportButton.disabled = false;
  }
});
function setUpEnquiryForm(listing) {
  const form = document.getElementById("enquiry-form");
  const feedback = document.getElementById("enquiry-feedback");
  document.getElementById("enquiry-message").value =
    `Hello, is ${listing.animal.name} still available? When can I come for a viewing?`;
  form.addEventListener("submit", event => {
    event.preventDefault();
    const name = document.getElementById("enquiry-name").value.trim();
    const phone = document.getElementById("enquiry-phone").value.trim();
    const message = document.getElementById("enquiry-message").value.trim();
    if (!name || !message || !/^\+?\d{9,13}$/.test(phone.replace(/[\s-]/g, ""))) {
      feedback.textContent = "Fill in your name, valid phone number and message.";
      feedback.className = "form-feedback is-error";
      return;
    }
    feedback.textContent = "";
    openScamWarning(async () => {
      try {
        await apiRequest("/api/enquiries", {
          method: "POST", body: { listingId: listing.id, buyerName: name, buyerPhone: phone, message }
        });
        feedback.textContent = "Enquiry sent successfully to the seller.";
        feedback.className = "form-feedback";
        form.reset();
      } catch (error) {
        feedback.textContent = "Unable to send enquiry: " + error.message;
        feedback.className = "form-feedback is-error";
      }
    });
  });
}
async function initAnimal() {
  const id = Number(new URLSearchParams(location.search).get("id"));
  if (!Number.isInteger(id) || id <= 0) {
    detailMessage.textContent = "Invalid animal ID.";
    return;
  }
  try {
    const [{ listing, seller }, breeds] = await Promise.all([
      apiRequest("/api/listings/" + id), loadBreeds().catch(() => ({}))
    ]);
    currentListing = listing;
    document.title = displayName(listing) + " | Pet Finder";
    setText("animal-name", displayName(listing));
    setText("animal-price", formatPrice(listing.price) + (listing.quantity > 1 ? " each" : ""));
    showStatus(listing);
    showPhoto(listing);
    showFacts(listing);
    showHealthRecords(listing.animal.healthRecords);
    showBreed(listing.animal.breed, breeds);
    showSeller(seller);
    setUpEnquiryForm(listing);
    // Existing HTML says 'Sample record'; the data is now from MySQL.
    const healthNote = document.querySelector(".panel-note");
    if (healthNote) healthNote.textContent = "Ask to see the original health or vaccination card before paying.";
    detailMessage.hidden = true;
    detail.hidden = false;
  } catch (error) {
    detailMessage.textContent = error.status === 404
      ? "We couldn't find that animal. It may have been removed."
      : "Could not load animal: " + error.message;
  }
}
initAnimal();
