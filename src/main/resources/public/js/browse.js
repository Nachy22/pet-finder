// Browse page: loads the listings, then lets the visitor search and filter them.

// TEMPORARY: reads the mock copy of seed_data.json.
// When the backend is ready, change this to its API address.
const DATA_URL = "mock/seed_data.json";

const searchForm = document.getElementById("search-form");
const searchInput = document.getElementById("search-input");
const filterSelects = document.querySelectorAll(".filter-select");
const clearButton = document.getElementById("clear-filters");
const resultsCount = document.getElementById("results-count");
const cardGrid = document.getElementById("card-grid");
const cardTemplate = document.getElementById("card-template");

let animals = [];
let searchText = "";

// Turn the raw data (sellers + listings) into one simple object per animal card.
function toAnimals(data) {
  const sellersById = new Map(data.sellers.map((s) => [s.id, s]));

  return data.listings.map((listing) => {
    const seller = sellersById.get(listing.sellerId) || {};
    return {
      id: listing.id,
      status: listing.status,
      price: listing.price,
      quantity: listing.quantity,
      imageUrl: listing.imageUrl,
      name: listing.animal.name,
      species: listing.animal.species,
      breed: listing.animal.breed,
      age: listing.animal.lifeStage,
      colour: listing.animal.colour,
      hasHealthRecord: (listing.animal.healthRecords || []).length > 0,
      seller: seller.businessName || "Unknown seller",
      sellerTown: seller.town || "",
      sellerVerified: Boolean(seller.verified),
    };
  });
}

function formatPrice(amount) {
  return "K" + amount.toLocaleString("en-US");
}

// "Village chicks (per chick)" with 10 available becomes "Village chicks (10)".
function displayName(animal) {
  if (animal.quantity > 1) {
    return animal.name.replace(/\s*\(.*\)$/, "") + " (" + animal.quantity + ")";
  }
  return animal.name;
}

// Fill a dropdown with every distinct value of one field, sorted A to Z.
function fillSelect(select, field) {
  const values = [...new Set(animals.map((a) => a[field]))].sort();
  for (const value of values) {
    select.add(new Option(value, value));
  }
}

function matchesSearch(animal) {
  if (!searchText) return true;
  const haystack = [animal.name, animal.breed, animal.seller, animal.colour, animal.species]
    .join(" ")
    .toLowerCase();
  return haystack.includes(searchText);
}

function matchesFilters(animal) {
  for (const select of filterSelects) {
    if (select.value && animal[select.dataset.field] !== select.value) return false;
  }
  return true;
}

function buildCard(animal) {
  const card = cardTemplate.content.firstElementChild.cloneNode(true);

  const status = card.querySelector(".status-badge");
  status.textContent = animal.status;
  status.className = "status-badge tag " + (animal.status === "Available" ? "tag-green" : "tag-amber");

  const media = card.querySelector(".card-media");
  const img = card.querySelector(".card-img");
  if (animal.imageUrl) {
    img.src = animal.imageUrl;
    img.alt = animal.name + ", " + animal.breed;
    img.addEventListener("error", () => media.classList.add("no-image"));
  } else {
    media.classList.add("no-image");
  }

  card.querySelector(".card-name").textContent = displayName(animal);
  card.querySelector(".card-price").textContent =
    formatPrice(animal.price) + (animal.quantity > 1 ? " each" : "");

  card.querySelector('[data-fact="breed"]').textContent = animal.breed;
  card.querySelector('[data-fact="age"]').textContent = animal.age;
  card.querySelector('[data-fact="colour"]').textContent = animal.colour;
  card.querySelector('[data-fact="seller"]').textContent =
    animal.seller + (animal.sellerTown ? ", " + animal.sellerTown : "");

  const sellerTag = card.querySelector(".seller-tag");
  sellerTag.textContent = animal.sellerVerified ? "Verified seller" : "Verification pending";
  sellerTag.classList.add(animal.sellerVerified ? "tag-green" : "tag-amber");

  if (!animal.hasHealthRecord) {
    card.querySelector(".health-tag").remove();
  }

  const link = card.querySelector(".card-link");
  link.href = "details.html?id=" + encodeURIComponent(animal.id);
  link.setAttribute("aria-label", "View details for " + displayName(animal));

  return card;
}

function render() {
  const visible = animals.filter((a) => matchesSearch(a) && matchesFilters(a));

  resultsCount.textContent =
    visible.length + (visible.length === 1 ? " animal found" : " animals found");

  cardGrid.replaceChildren();

  if (visible.length === 0) {
    const empty = document.createElement("p");
    empty.className = "empty-state";
    empty.textContent = "No animals match your search. Try clearing the filters.";
    cardGrid.append(empty);
    return;
  }

  for (const animal of visible) {
    cardGrid.append(buildCard(animal));
  }
}

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  searchText = searchInput.value.trim().toLowerCase();
  render();
});

// Clearing the search box (e.g. with its little x) shows everything again.
searchInput.addEventListener("input", () => {
  if (searchInput.value === "") {
    searchText = "";
    render();
  }
});

for (const select of filterSelects) {
  select.addEventListener("change", render);
}

clearButton.addEventListener("click", () => {
  for (const select of filterSelects) select.value = "";
  searchInput.value = "";
  searchText = "";
  render();
});

async function init() {
  try {
    const response = await fetch(DATA_URL);
    if (!response.ok) throw new Error("HTTP " + response.status);
    animals = toAnimals(await response.json());
  } catch (error) {
    console.error("Could not load animals:", error);
    resultsCount.textContent =
      "Could not load the animals. Open this page through a local web server, not by double-clicking the file.";
    return;
  }

  for (const select of filterSelects) {
    fillSelect(select, select.dataset.field);
  }
  render();
}

init();
