// Seller dashboard: verification banner and the "My listings" table for the logged-in seller.
// Needs js/data.js to be loaded first.

// =====================================================================================
// DEMO DATA: this page reads the sample data in mock/seed_data.json, not real accounts.
// There is no real login yet, so we pretend the logged-in seller is seller id 1
// (Copperbelt Pet Haven). When the backend is ready, get the logged-in seller and
// their listings from it instead, and delete DEMO_SELLER_ID.
// =====================================================================================
const DEMO_SELLER_ID = 1;

const message = document.getElementById("dashboard-message");

const ICON_CHECK =
  '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12.5l4.5 4.5L19 7.5"/></svg>';
const ICON_CLOCK =
  '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"><circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 2"/></svg>';

function showBanner(seller) {
  const banner = document.getElementById("verify-banner");
  const badge = document.getElementById("verify-badge");
  const text = document.getElementById("verify-text");

  if (seller.verified) {
    banner.classList.add("is-verified");
    document.getElementById("verify-icon").innerHTML = ICON_CHECK;
    badge.textContent = "Verified";
    badge.classList.add("tag-green");
    text.innerHTML = "<strong>Verified.</strong> Your listings are live.";
  } else {
    banner.classList.add("is-pending");
    document.getElementById("verify-icon").innerHTML = ICON_CLOCK;
    badge.textContent = "Pending";
    badge.classList.add("tag-amber");
    text.innerHTML =
      "<strong>Pending verification.</strong> Your listings go live once an admin verifies your account.";
  }
}

// 0 -> "None yet", 1 -> "1 record", 2 -> "2 records"
function healthText(count) {
  if (count === 0) return "None yet";
  return count + (count === 1 ? " record" : " records");
}

function statusClass(status) {
  if (status === "Available") return "tag-green";
  if (status === "Reserved") return "tag-amber";
  return "tag-grey";
}

function buildRow(listing) {
  const row = document.getElementById("listing-row-template").content.firstElementChild.cloneNode(true);
  const animal = listing.animal;
  const name = displayName(listing);

  const thumb = row.querySelector(".listing-thumb");
  if (listing.imageUrl) thumb.src = listing.imageUrl;
  thumb.addEventListener("error", () => thumb.removeAttribute("src"));

  row.querySelector(".listing-name").textContent = name;
  row.querySelector(".listing-sub").textContent = [
    animal.sex,
    animal.lifeStage,
    formatPrice(listing.price) + (listing.quantity > 1 ? " each" : ""),
  ].join(" · ");

  row.querySelector(".listing-type").textContent = animal.type;
  row.querySelector(".listing-species").textContent = animal.species + " · " + animal.breed;

  const count = (animal.healthRecords || []).length;
  const health = row.querySelector(".listing-health");
  health.textContent = healthText(count);
  if (count === 0) health.classList.add("is-missing");

  const status = row.querySelector(".listing-status");
  status.textContent = listing.status;
  status.classList.add(statusClass(listing.status));

  // Say which animal each button is for, so screen readers don't just hear "Edit, Edit, Edit"
  row.querySelector(".listing-edit").setAttribute("aria-label", "Edit " + name);
  row.querySelector(".listing-rehome").setAttribute("aria-label", "Mark " + name + " as rehomed");

  return row;
}

function showListings(listings) {
  const rows = document.getElementById("listings-rows");

  if (listings.length === 0) {
    const row = rows.insertRow();
    const cell = row.insertCell();
    cell.colSpan = 5;
    cell.className = "listings-empty";
    cell.textContent = "You haven't added any animals yet.";
    return;
  }

  for (const listing of listings) rows.append(buildRow(listing));
}

async function init() {
  let data;
  try {
    data = await loadPetData();
  } catch (error) {
    console.error("Could not load dashboard:", error);
    message.textContent =
      "Could not load your dashboard. Open this page through a local web server, not by double-clicking the file.";
    return;
  }

  const seller = findSeller(data, DEMO_SELLER_ID);
  if (!seller) {
    message.textContent = "We couldn't find your seller account.";
    return;
  }

  document.getElementById("seller-name").textContent = seller.businessName;
  showBanner(seller);
  showListings(data.listings.filter((l) => l.sellerId === seller.id));

  message.hidden = true;
  document.getElementById("dashboard").hidden = false;
}

init();
