// Shared Pet Finder API helpers. Runs on the same origin as the Javalin server.
const DATA_URL = "/api/data";
const BREEDS_URL = "/api/breeds";
let csrfToken = "";

async function apiRequest(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (csrfToken && !["GET", "HEAD"].includes((options.method || "GET").toUpperCase())) {
    headers["X-CSRF-Token"] = csrfToken;
  }
  if (options.body && !(options.body instanceof FormData) && typeof options.body !== "string") {
    headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(options.body);
  }
  const response = await fetch(path, {
    ...options,
    headers,
    credentials: "same-origin",
  });
  let data = null;
  try { data = await response.json(); } catch { /* Error may not be JSON. */ }
  if (!response.ok) {
    const error = new Error(data?.error || `HTTP ${response.status}`);
    error.status = response.status;
    throw error;
  }
  return data;
}

function loadPetData() { return apiRequest(DATA_URL); }
function loadBreeds() { return apiRequest(BREEDS_URL); }
async function currentSeller() {
  const data = await apiRequest("/api/auth/me");
  csrfToken = data.csrfToken;
  return data.seller;
}
async function loadEnquiries() {
  const data = await apiRequest("/api/sellers/me/enquiries");
  return data.enquiries;
}
function findSeller(data, sellerId) {
  return data.sellers.find(s => s.id === sellerId) || null;
}
function whatsappNumber(phone) {
  const digits = String(phone).replace(/\D/g, "");
  return digits.startsWith("0") ? "260" + digits.slice(1) : digits;
}
function formatPrice(amount) {
  const value = Number(amount);
  return value === 0 ? "Free" : "K" + value.toLocaleString("en-US", { maximumFractionDigits: 2 });
}
function shortName(listing) {
  return listing.animal.name.replace(/\s*\(.*\)$/, "");
}
function displayName(listing) {
  return listing.quantity > 1
    ? shortName(listing) + " (" + listing.quantity + ")"
    : listing.animal.name;
}
