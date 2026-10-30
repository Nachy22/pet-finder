// Shared helpers for loading and showing Pet Finder data. Used by every page that shows animals.

// TEMPORARY: these read the mock files in /mock.
// When the backend is ready, change these to its API addresses.
const DATA_URL = "mock/seed_data.json";
const BREEDS_URL = "mock/breeds.json";
const ENQUIRIES_URL = "mock/enquiries.json";

// DEMO: enquiries sent from animal.html are kept in this browser's localStorage under this key,
// so the seller dashboard can show them. Only this browser sees them. The backend will replace this.
const DEMO_ENQUIRIES_KEY = "petFinder.demoEnquiries";

async function fetchJson(url) {
  const response = await fetch(url);
  if (!response.ok) throw new Error(url + " returned HTTP " + response.status);
  return response.json();
}

// Returns { sellers: [...], listings: [...] }
function loadPetData() {
  return fetchJson(DATA_URL);
}

// Returns an object keyed by breed name, e.g. breeds["Doberman"]
function loadBreeds() {
  return fetchJson(BREEDS_URL);
}

// ---------- Enquiries (DEMO DATA: the backend will replace all of this) ----------

// Enquiries saved in this browser by animal.html. Returns [] if there are none or storage is blocked.
function readSavedEnquiries() {
  try {
    const saved = JSON.parse(localStorage.getItem(DEMO_ENQUIRIES_KEY) || "[]");
    return Array.isArray(saved) ? saved : [];
  } catch {
    return [];
  }
}

// Adds one enquiry to this browser's saved list. Returns false if the browser won't let us save.
function saveDemoEnquiry(enquiry) {
  try {
    localStorage.setItem(DEMO_ENQUIRIES_KEY, JSON.stringify([...readSavedEnquiries(), enquiry]));
    return true;
  } catch {
    return false;
  }
}

// The demo enquiries file plus anything saved in this browser.
// Each enquiry: { id, listingId, buyerName, buyerPhone, message, sentAt }
async function loadEnquiries() {
  let fromFile = [];
  try {
    fromFile = (await fetchJson(ENQUIRIES_URL)).enquiries;
  } catch (error) {
    console.error("Could not load demo enquiries file:", error);
  }
  return [...fromFile, ...readSavedEnquiries()];
}

// WhatsApp links need the number with the Zambia code and no spaces or +:
// "0970000001" and "+260 97 000 0001" both become "260970000001"
function whatsappNumber(phone) {
  const digits = phone.replace(/\D/g, "");
  return digits.startsWith("0") ? "260" + digits.slice(1) : digits;
}

function findSeller(data, sellerId) {
  return data.sellers.find((s) => s.id === sellerId) || null;
}

// 3500 becomes "K3,500"; 0 becomes "Free"
function formatPrice(amount) {
  if (amount === 0) return "Free";
  return "K" + amount.toLocaleString("en-US");
}

// "Village chicks (per chick)" with 10 available becomes "Village chicks (10)".
function displayName(listing) {
  if (listing.quantity > 1) {
    return shortName(listing) + " (" + listing.quantity + ")";
  }
  return listing.animal.name;
}

// The name without any "(...)" note on the end, e.g. "Village chicks".
function shortName(listing) {
  return listing.animal.name.replace(/\s*\(.*\)$/, "");
}
