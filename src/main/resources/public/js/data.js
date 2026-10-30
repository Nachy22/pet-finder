// Shared helpers for loading and showing Pet Finder data. Used by every page that shows animals.

// TEMPORARY: these read the mock files in /mock.
// When the backend is ready, change these to its API addresses.
const DATA_URL = "mock/seed_data.json";
const BREEDS_URL = "mock/breeds.json";

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
