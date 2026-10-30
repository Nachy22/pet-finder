// Animal details page: shows one listing, picked by the ?id= in the address.
// Needs js/data.js to be loaded first.

const message = document.getElementById("detail-message");
const detail = document.getElementById("detail");

function setText(id, text) {
  document.getElementById(id).textContent = text;
}

// "2026-08-12" becomes "12 Aug 2026"
function formatDate(isoDate) {
  return new Date(isoDate).toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
    timeZone: "UTC",
  });
}

// "0970000001" becomes "097 000 0001"
function formatPhone(phone) {
  return phone.replace(/^(\d{3})(\d{3})(\d{4})$/, "$1 $2 $3");
}

function showMessage(text) {
  message.replaceChildren(text + " ");
  const back = document.createElement("a");
  back.href = "browse.html";
  back.textContent = "Browse all animals";
  message.append(back);
}

function showStatus(listing) {
  const badge = document.getElementById("status-badge");
  badge.textContent = listing.status;
  badge.classList.add(
    listing.status === "Available" ? "tag-green" : listing.status === "Reserved" ? "tag-amber" : "tag-grey"
  );

  const notice = document.getElementById("status-notice");
  if (listing.status === "Reserved") {
    notice.innerHTML =
      "<strong>Reserved.</strong> The seller is holding this animal for another buyer. " +
      "You can still send an enquiry in case it becomes available again.";
    notice.classList.add("status-notice-reserved");
    notice.hidden = false;
  } else if (listing.status === "Rehomed") {
    notice.innerHTML =
      "<strong>Rehomed.</strong> This animal has already gone to a new home and is no longer for sale.";
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
  if (listing.imageUrl) {
    img.src = listing.imageUrl;
    img.alt = listing.animal.name + ", " + listing.animal.breed;
    img.addEventListener("error", () => photo.classList.add("no-image"));
  } else {
    photo.classList.add("no-image");
  }
}

function showFacts(listing) {
  const animal = listing.animal;
  setText("fact-breed", animal.breed);
  setText("fact-species", animal.species);
  setText("fact-colour", animal.colour);
  setText("fact-age", animal.lifeStage);
  setText("fact-sex", animal.sex);

  if (listing.quantity > 1) {
    setText("fact-quantity", listing.quantity);
    document.getElementById("fact-quantity-box").hidden = false;
  }
}

function showHealthRecords(records) {
  const rows = document.getElementById("health-rows");

  if (!records || records.length === 0) {
    const row = rows.insertRow();
    const cell = row.insertCell();
    cell.colSpan = 2;
    cell.textContent = "No records added yet. Ask the seller for the vet card.";
    return;
  }

  for (const record of records) {
    const row = rows.insertRow();
    row.insertCell().textContent = record.vaccine;
    row.insertCell().textContent = formatDate(record.date);
  }
}

function showBreed(breedName, breeds) {
  const profile = breeds[breedName];
  if (!profile) return; // no profile for this breed yet, so keep the box hidden

  setText("breed-heading", "Breed profile: " + breedName);
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
  phone.textContent = formatPhone(seller.phone);
  phone.href = "tel:" + seller.phone;

  const whatsapp = document.getElementById("seller-whatsapp");
  whatsapp.textContent = formatPhone(seller.phone);
  whatsapp.href = "https://wa.me/" + whatsappNumber(seller.phone);

  const email = document.getElementById("seller-email");
  email.textContent = seller.email;
  email.href = "mailto:" + seller.email;

  setText("seller-town", seller.town);
  setText("map-label", "Map placeholder · approximate area: " + seller.town);
}

function setUpEnquiryForm(listing) {
  const form = document.getElementById("enquiry-form");
  const feedback = document.getElementById("enquiry-feedback");
  const sex = listing.animal.sex;
  const them = listing.quantity > 1 ? "them" : sex === "Male" ? "him" : sex === "Female" ? "her" : "them";
  const question =
    listing.quantity > 1
      ? "Hello, are the " + shortName(listing) + " still available?"
      : "Hello, is " + listing.animal.name + " still available?";

  document.getElementById("enquiry-message").value = question + " When can I come to see " + them + "?";

  // Check the fields, then show the scam warning. The enquiry is only "sent" after "I understand".
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const firstEmpty = [...form.elements].find((field) => field.required && !field.value.trim());
    if (firstEmpty) {
      feedback.textContent = "Please fill in your name, phone and message.";
      feedback.className = "form-feedback is-error";
      firstEmpty.focus();
      return;
    }
    const phone = document.getElementById("enquiry-phone");
    if (!/^\+?\d{9,13}$/.test(phone.value.replace(/[\s-]/g, ""))) {
      feedback.textContent = "Please enter a phone number like 097 000 0001.";
      feedback.className = "form-feedback is-error";
      phone.focus();
      return;
    }
    feedback.textContent = "";
    openScamWarning(() => {
      // DEMO: instead of sending to the backend, save the enquiry in this browser so the
      // seller dashboard can show it. Replace this with a call to the backend when it's ready.
      const saved = saveDemoEnquiry({
        id: "local-" + Date.now(),
        listingId: listing.id,
        buyerName: document.getElementById("enquiry-name").value.trim(),
        buyerPhone: phone.value.trim(),
        message: document.getElementById("enquiry-message").value.trim(),
        sentAt: new Date().toISOString(),
      });
      feedback.textContent = saved
        ? "Enquiry sent (demo). It was saved in this browser only, for the seller dashboard demo."
        : "Enquiry sent (demo), but this browser wouldn't let us save it for the dashboard.";
      feedback.className = "form-feedback";
    });
  });
}

// ---------- Scam warning pop-up ----------

const scamDialog = document.getElementById("scam-dialog");
const scamThanks = document.getElementById("scam-report-thanks");
const scamReportButton = document.getElementById("scam-report");

// What to do when the visitor presses "I understand". Set each time the pop-up opens.
let afterScamWarning = null;

function openScamWarning(onConfirm) {
  afterScamWarning = onConfirm;
  scamThanks.textContent = "";
  scamReportButton.disabled = false;
  document.body.classList.add("has-dialog");
  scamDialog.showModal();
}

// The buttons are set up once. The × button and Escape just close the pop-up, so nothing is sent.
document.getElementById("scam-understand").addEventListener("click", () => {
  const onConfirm = afterScamWarning;
  afterScamWarning = null;
  scamDialog.close();
  if (onConfirm) onConfirm();
});

document.getElementById("scam-close").addEventListener("click", () => scamDialog.close());

scamReportButton.addEventListener("click", () => {
  // DEMO: the report isn't sent anywhere yet.
  scamThanks.textContent = "Thank you. We'll check this listing (demo).";
  scamReportButton.disabled = true;
});

// Runs a moment after the pop-up closes, however it was closed.
// The browser puts keyboard focus back on the "Send enquiry" button by itself.
scamDialog.addEventListener("close", () => {
  if (scamDialog.open) return; // already reopened, so this close is out of date
  afterScamWarning = null;
  document.body.classList.remove("has-dialog");
});

async function init() {
  const id = Number(new URLSearchParams(window.location.search).get("id"));

  let data;
  let breeds;
  try {
    [data, breeds] = await Promise.all([loadPetData(), loadBreeds().catch(() => ({}))]);
  } catch (error) {
    console.error("Could not load animal:", error);
    message.textContent =
      "Could not load the animal. Open this page through a local web server, not by double-clicking the file.";
    return;
  }

  const listing = data.listings.find((l) => l.id === id);
  if (!listing) {
    showMessage("We couldn't find that animal. It may have been removed.");
    return;
  }

  document.title = displayName(listing) + " | Pet Finder";
  setText("animal-name", displayName(listing));
  setText("animal-price", formatPrice(listing.price) + (listing.quantity > 1 ? " each" : ""));

  showStatus(listing);
  showPhoto(listing);
  showFacts(listing);
  showHealthRecords(listing.animal.healthRecords);
  showBreed(listing.animal.breed, breeds);

  const seller = findSeller(data, listing.sellerId);
  if (seller) {
    showSeller(seller);
  } else {
    document.querySelector(".seller-panel").hidden = true;
  }

  setUpEnquiryForm(listing);

  message.hidden = true;
  detail.hidden = false;
}

init();
