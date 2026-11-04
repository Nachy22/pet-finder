// Seller dashboard: uses the authenticated seller's own data, not a hard-coded demo ID.
const dashMessage = document.getElementById("dashboard-message");
let mySeller = null;
let myListings = [];
function statusClass(status) {
  return status === "Available" ? "tag-green" : status === "Reserved" ? "tag-amber" : "tag-grey";
}
function healthText(count) {
  return count === 0 ? "None yet" : count + (count === 1 ? " record" : " records");
}
function showBanner(seller) {
  const banner = document.getElementById("verify-banner");
  const badge = document.getElementById("verify-badge");
  const text = document.getElementById("verify-text");
  const verified = seller.verified;
  banner.classList.toggle("is-verified", verified);
  banner.classList.toggle("is-pending", !verified);
  document.getElementById("verify-icon").textContent = verified ? "✓" : "◷";
  badge.textContent = verified ? "Verified" : "Pending";
  badge.className = "tag " + (verified ? "tag-green" : "tag-amber");
  text.textContent = verified ? "Verified. Your listings are visible to buyers."
    : "Pending verification. Listings are saved but not publicly visible until verified.";
}
function buildRow(listing) {
  const row = document.getElementById("listing-row-template").content.firstElementChild.cloneNode(true);
  const a = listing.animal;
  const thumb = row.querySelector(".listing-thumb");
  if (listing.imageUrl) thumb.src = listing.imageUrl;
  thumb.addEventListener("error", () => thumb.removeAttribute("src"));
  row.querySelector(".listing-name").textContent = displayName(listing);
  row.querySelector(".listing-sub").textContent = [a.sex, a.lifeStage, formatPrice(listing.price)].join(" · ");
  row.querySelector(".listing-type").textContent = a.type;
  row.querySelector(".listing-species").textContent = a.species + " · " + a.breed;
  row.querySelector(".listing-health").textContent = healthText(a.healthRecords?.length || 0);
  const badge = row.querySelector(".listing-status");
  badge.textContent = listing.status;
  badge.classList.add(statusClass(listing.status));
  const edit = row.querySelector(".listing-edit");
  const rehome = row.querySelector(".listing-rehome");
  edit.setAttribute("aria-label", "Change price of " + a.name);
  rehome.setAttribute("aria-label", "Mark " + a.name + " as rehomed");
  edit.addEventListener("click", async () => {
    const next = window.prompt("New price (kwacha) for " + a.name + ":", String(listing.price));
    if (next === null) return;
    if (!/^\d+(\.\d{1,2})?$/.test(next)) { window.alert("Enter a valid price, 0 or more."); return; }
    await updateListing(listing.id, { price: next });
  });
  rehome.addEventListener("click", async () => {
    if (!window.confirm("Mark " + a.name + " as rehomed?")) return;
    await updateListing(listing.id, { status: "Rehomed" });
  });
  return row;
}
async function updateListing(id, patch) {
  try {
    await apiRequest("/api/listings/" + id, { method: "PUT", body: patch });
    await refreshDashboard();
  } catch (error) { window.alert("Could not update listing: " + error.message); }
}
function showListings(listings) {
  const table = document.getElementById("listings-rows");
  table.replaceChildren();
  if (!listings.length) {
    const row = table.insertRow();
    const cell = row.insertCell();
    cell.colSpan = 5;
    cell.className = "listings-empty";
    cell.textContent = "You haven't added any animals yet.";
  } else listings.forEach(l => table.append(buildRow(l)));
}
function formatEnquiryDate(value) {
  return new Date(value).toLocaleString("en-GB", {
    day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit"
  });
}
function buildEnquiryCard(enquiry, listing) {
  const item = document.getElementById("enquiry-template").content.firstElementChild.cloneNode(true);
  item.querySelector(".enquiry-buyer").textContent = enquiry.buyerName;
  const time = item.querySelector(".enquiry-date");
  time.dateTime = enquiry.sentAt;
  time.textContent = formatEnquiryDate(enquiry.sentAt);
  const link = item.querySelector(".enquiry-animal");
  link.textContent = displayName(listing);
  link.href = "animal.html?id=" + listing.id;
  item.querySelector(".enquiry-message").textContent = enquiry.message;
  const call = item.querySelector(".enquiry-call");
  call.textContent = "Call " + enquiry.buyerPhone;
  call.href = "tel:" + enquiry.buyerPhone.replace(/[^\d+]/g, "");
  const wa = item.querySelector(".enquiry-whatsapp");
  wa.href = "https://wa.me/" + whatsappNumber(enquiry.buyerPhone);
  wa.setAttribute("aria-label", "WhatsApp " + enquiry.buyerName);
  return item;
}
async function showEnquiries(listings) {
  const data = await loadEnquiries();
  const known = new Map(listings.map(l => [l.id, l]));
  const valid = data.filter(e => known.has(e.listingId));
  const list = document.getElementById("enquiry-list");
  list.replaceChildren(...valid.map(e => buildEnquiryCard(e, known.get(e.listingId))));
  document.getElementById("enquiries-empty").hidden = valid.length > 0;
}
async function refreshDashboard() {
  try {
    const data = await apiRequest("/api/sellers/me/listings");
    myListings = data.listings;
    showListings(myListings);
    await showEnquiries(myListings);
    return myListings;
  } catch (error) {
    dashMessage.hidden = false;
    dashMessage.textContent = "Could not update dashboard: " + error.message;
    throw error;
  }
}
window.refreshDashboard = refreshDashboard;
async function initDashboard() {
  try {
    mySeller = await currentSeller();
    document.getElementById("seller-name").textContent = mySeller.businessName;
    showBanner(mySeller);
    await refreshDashboard();
    dashMessage.hidden = true;
    document.getElementById("dashboard").hidden = false;
  } catch (error) {
    if (error.status === 401) {
      window.location.replace("seller-login.html");
    } else {
      dashMessage.textContent = "Could not load your dashboard: " + error.message;
    }
  }
}
const logoutLink = document.querySelector('a.nav-link[href="seller-login.html"]');
if (logoutLink) logoutLink.addEventListener("click", async event => {
  event.preventDefault();
  try { await apiRequest("/api/auth/logout", { method: "POST" }); }
  finally { window.location.assign("seller-login.html"); }
});
initDashboard();
