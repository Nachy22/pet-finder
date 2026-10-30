# Frontend notes: demo data the backend will replace

The front end is plain HTML, CSS and JavaScript in `src/main/resources/public`. There is no backend yet, so some parts use **mock files** or **fake actions**. This file lists every one of them and what each page needs from the real backend.

Tip: search the code for `DEMO` and `TEMPORARY` to find each spot.

For Javalin to serve these pages, static files must be served from the classpath folder `/public` (for example `config.staticFiles.add("/public", Location.CLASSPATH)`). Image paths in the data, like `/images/doberman-puppy.jpeg`, assume `public` is the site root.

---

## 1. Where the pages get their data

All data addresses are in one place: [`js/data.js`](src/main/resources/public/js/data.js), at the top.

| Constant | Reads now (mock file) | What the backend needs to provide |
|---|---|---|
| `DATA_URL` | `mock/seed_data.json` (a copy of `src/main/resources/seed_data.json`) | All sellers and listings, in the same shape (see section 3) |
| `BREEDS_URL` | `mock/breeds.json` (sample breed profiles we wrote) | Breed profiles, keyed by breed name. **`seed_data.json` has no breed info at all**, so the team needs to decide where this lives |
| `ENQUIRIES_URL` | `mock/enquiries.json` (3 made-up enquiries, buyer names end in "(demo)") | The logged-in seller's enquiries |

When these point at real endpoints, **delete the `public/mock` folder**.

---

## 2. Page by page

### Browse (`browse.html`)
- **Now:** loads every listing from the mock file. Search and filters run in the browser.
- **Needs:** a list of listings with their seller's name, town and verified status. Filtering in the browser is fine for now. If the list gets big, the backend may want to filter instead.

### Animal details (`animal.html?id=3`)
- **Now:** finds the listing by `id` in the mock file and its seller by `sellerId`. Breed info comes from `mock/breeds.json`.
- **Needs:** one listing by id, plus its seller's **business name, phone, email, town and verified status**, which are shown to the public, plus the breed profile.
- **Send enquiry (fake):** after the scam warning, the enquiry is **saved in the browser's localStorage** (key `petFinder.demoEnquiries`) instead of being sent. The page then says **"Enquiry sent (demo)…"**. See `js/animal.js`, the `DEMO` comment in `setUpEnquiryForm`.
  - **Needs:** an endpoint to save an enquiry: listing id, buyer name, buyer phone, message. Then remove the localStorage code in `js/data.js` (`readSavedEnquiries`, `saveDemoEnquiry`, `DEMO_ENQUIRIES_KEY`).
- **Report this listing (fake):** only shows "Thank you. We'll check this listing (demo)." (`js/animal.js`, `DEMO` comment).
  - **Needs:** an endpoint to report a listing.
- **Placeholders:** the map is a drawing labelled "Map placeholder". The health table says "Sample record." The footer says "Sample data".

### Seller sign-up (`seller-signup.html`)
- **Now:** checks the fields, then shows **"Account created (demo)"**. Nothing is saved or uploaded (`js/seller-signup.js`, `DEMO` comment).
- **Needs:** an endpoint to create a seller with **business name, seller type (Breeder / Farmer / Individual), contact person, phone, email, town, area, password** and **one verification document** (PDF, JPG or PNG, up to 5 MB, so it must be a file upload). New sellers start as not verified.
  - Note: **`area` is not in `seed_data.json`** yet.
  - Please check the same rules on the server: required fields, password of at least 8 characters with a number, file type and size.

### Seller login (`seller-login.html`)
- **Now: a FAKE login.** Any email or phone works with the password **`demo123`**, then the page goes to the dashboard (`js/seller-login.js`, the big `TEMPORARY FAKE LOGIN` comment).
- **Needs:** a login endpoint that accepts **email or phone + password**, and keeps the seller logged in (session or token). On failure the page shows one general message: "Email or phone, or password is incorrect. Please try again." Please don't say which part was wrong.
- **Not built yet:** "Forgot password?" only shows "Password reset isn't available yet (demo)."

### Seller dashboard (`seller-dashboard.html`)
- **Now: pretends the logged-in seller is id 1** (`DEMO_SELLER_ID` in `js/seller-dashboard.js`). Anyone can open the page; there is no login check.
- **Needs:**
  - **Who is logged in:** their name and `verified` status, for the banner. If no one is logged in, the page should send them to the login page.
  - **Log out:** the button only links back to the login page. It needs a real log-out endpoint.
  - **My listings:** only this seller's listings.
  - **Add animal (fake):** a valid form only adds a row to the table on the page and shows **"Listing published (demo)"**. The row disappears on reload, and photos are not uploaded (`js/add-animal.js`, `DEMO` comments).
    - Needs an endpoint to create a listing, with photo upload (up to 8 images). The form sends: name, type (Pet / Livestock), species, breed, colour, age group, sex, price (0 means Free), temperament (pets) or purpose (livestock), and **at least one health record** (vaccine + date). Please enforce "at least one health record" on the server too.
    - Open questions for the team: the page sets new listings to **"Available"**, even for unverified sellers. Should they wait for verification? Quantity isn't asked, so it is always 1.
  - **Edit** and **Mark rehomed** buttons: **do nothing yet.** They need update endpoints.
  - **Enquiries:** shows `mock/enquiries.json` plus anything saved in this browser by the animal page (see above). Only enquiries about this seller's animals are shown.
    - Needs: the logged-in seller's enquiries, each with buyer name, buyer phone, message, which listing it is about, and when it was sent.

---

## 3. Data shapes the pages expect

These match `seed_data.json`, plus the extra fields marked **new**.

**Listing**
```
id, sellerId, status ("Available" | "Reserved" | "Rehomed"), price (kwacha, 0 = Free), quantity, imageUrl,
animal: { type ("Pet" | "Livestock"), name, species, breed, lifeStage, sex, colour,
          temperament (pets) or purpose (livestock),
          healthRecords: [ { vaccine, date "YYYY-MM-DD" } ] }
```

**Seller:** `id, businessName, sellerType, contactPerson, phone, email, town, verified`, plus **`area` (new)**

**Breed profile (new):** keyed by breed name → `summary, adultSize, lifespan, needs, goodToKnow`

**Enquiry (new):** `id, listingId, buyerName, buyerPhone, message, sentAt` (date and time, e.g. `2026-10-29T09:14:00`)

If the real API uses different names, only `js/data.js` and the few places noted above need to change.

---

## 4. Other placeholders (not backend work, but good to know)

- Every footer says "Sample design" or "Sample data: … placeholders".
- `scam-safety.html` is linked from the landing page, the details page and the footers, but **doesn't exist yet.**
