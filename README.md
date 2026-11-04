# Pet Finder

A Java (Javalin) and MySQL web application connecting animal sellers and buyers.
The frontend is served by Javalin from `src/main/resources/public`.

## Quick start (Ubuntu)

Start MySQL:

```bash
sudo systemctl start mysql
```

From the project directory, set your **MySQL application user** credentials in the current terminal (not your seller login credentials):

```bash
cd ~/Documents/pet-finder
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=pet_finder
export DB_USER=petfinder_app
read -rsp 'MySQL app password: ' DB_PASSWORD; echo
export DB_PASSWORD
mvn clean test
mvn exec:java -Dexec.mainClass=com.petfinder.App
```

Visit `http://localhost:7070`.

**Security:** Do not commit `.env` files, passwords, uploaded identity documents, or session cookies. This project uses salted password hashing; original passwords cannot be retrieved from their hashes.

## Project organization

- `src/main/java/com/petfinder/`: Javalin server, models, repositories, services, API, database and exceptions
- `src/main/resources/public/`: HTML, CSS, JavaScript and static images
- `src/test/java/com/petfinder/`: JUnit tests
- `database/`: schema, migration and read-only verification SQL
- `scripts/`: API and comprehensive test scripts
- `design/`: interface mockups
- `PHASES_TEST.md`: the nine academic requirements, test reports and verification commands

## Current development milestones

- Phase 1: repository setup and inspection — completed
- Phase 2: Java object-oriented models — completed
- Phase 3: MySQL persistence — completed and read/write verified
- Phase 4: REST API and frontend integration — core seller and buyer workflow verified
- Phase 5: automated tests — 21 JUnit tests and 25 API checks passed; MySQL integrity checks found zero violations

**Note:** The original phase notes below are included to preserve implementation details and setup history. Some older instructions describe installing phase ZIP overlays and are not necessary once the current project is set up.

---

## Original project overview

### pet-finder
Platform for pets and animals readily available in Zambia


## Additional original README notes

PET FINDER PHASE 4 - /api/auth/me PATCH

Symptom:
- Dashboard shows "Could not load your dashboard: Unexpected server error"
- Browser network shows GET /api/auth/me -> HTTP 500

Likely cause addressed:
- A signed-in session can have sellerId but no csrf token.
- The old implementation passes ctx.sessionAttribute("csrf") directly to Map.of(...).
- Java Map.of() throws NullPointerException when any map value is null.

Fix:
- In the complete replacement ApiRoutes.java, the /api/auth/me route initializes a missing CSRF token in the existing session before constructing the response.
- All other routes are unchanged.

Installation in ~/Documents/pet-finder:
1. Stop the Javalin server with Ctrl+C.
2. Unzip this archive from the project root:
   unzip -o ~/Downloads/pet-finder-phase4-auth-fix.zip
3. Compile and start with your DB environment variables available:
   mvn clean compile
   mvn exec:java -Dexec.mainClass=com.petfinder.App
4. In the SAME browser in which you logged in, open:
   http://localhost:7070/api/auth/me
   Expect HTTP 200 JSON with seller and csrfToken (never share token publicly).
5. Reload /seller-dashboard.html.

If /api/auth/me still responds with 500, immediately inspect the terminal running Javalin.
The existing App.java prints the full server exception stack trace. Share the first exception
line and the first 5-10 relevant stack frames, removing secrets. Do not share cookies,
session IDs, CSRF tokens or DB passwords.

Unauthenticated /api/auth/me should return HTTP 401: that is expected and correct.


## Frontend design and data notes

### Frontend notes: demo data the backend will replace

The front end is plain HTML, CSS and JavaScript in `src/main/resources/public`. There is no backend yet, so some parts use **mock files** or **fake actions**. This file lists every one of them and what each page needs from the real backend.

Tip: search the code for `DEMO` and `TEMPORARY` to find each spot.

For Javalin to serve these pages, static files must be served from the classpath folder `/public` (for example `config.staticFiles.add("/public", Location.CLASSPATH)`). Image paths in the data, like `/images/doberman-puppy.jpeg`, assume `public` is the site root.

---

#### 1. Where the pages get their data

All data addresses are in one place: [`js/data.js`](src/main/resources/public/js/data.js), at the top.

| Constant | Reads now (mock file) | What the backend needs to provide |
|---|---|---|
| `DATA_URL` | `mock/seed_data.json` (a copy of `src/main/resources/seed_data.json`) | All sellers and listings, in the same shape (see section 3) |
| `BREEDS_URL` | `mock/breeds.json` (sample breed profiles we wrote) | Breed profiles, keyed by breed name. **`seed_data.json` has no breed info at all**, so the team needs to decide where this lives |
| `ENQUIRIES_URL` | `mock/enquiries.json` (3 made-up enquiries, buyer names end in "(demo)") | The logged-in seller's enquiries |

When these point at real endpoints, **delete the `public/mock` folder**.

---

#### 2. Page by page

##### Browse (`browse.html`)
- **Now:** loads every listing from the mock file. Search and filters run in the browser.
- **Needs:** a list of listings with their seller's name, town and verified status. Filtering in the browser is fine for now. If the list gets big, the backend may want to filter instead.

##### Animal details (`animal.html?id=3`)
- **Now:** finds the listing by `id` in the mock file and its seller by `sellerId`. Breed info comes from `mock/breeds.json`.
- **Needs:** one listing by id, plus its seller's **business name, phone, email, town and verified status**, which are shown to the public, plus the breed profile.
- **Send enquiry (fake):** after the scam warning, the enquiry is **saved in the browser's localStorage** (key `petFinder.demoEnquiries`) instead of being sent. The page then says **"Enquiry sent (demo)…"**. See `js/animal.js`, the `DEMO` comment in `setUpEnquiryForm`.
  - **Needs:** an endpoint to save an enquiry: listing id, buyer name, buyer phone, message. Then remove the localStorage code in `js/data.js` (`readSavedEnquiries`, `saveDemoEnquiry`, `DEMO_ENQUIRIES_KEY`).
- **Report this listing (fake):** only shows "Thank you. We'll check this listing (demo)." (`js/animal.js`, `DEMO` comment).
  - **Needs:** an endpoint to report a listing.
- **Placeholders:** the map is a drawing labelled "Map placeholder". The health table says "Sample record." The footer says "Sample data".

##### Seller sign-up (`seller-signup.html`)
- **Now:** checks the fields, then shows **"Account created (demo)"**. Nothing is saved or uploaded (`js/seller-signup.js`, `DEMO` comment).
- **Needs:** an endpoint to create a seller with **business name, seller type (Breeder / Farmer / Individual), contact person, phone, email, town, area, password** and **one verification document** (PDF, JPG or PNG, up to 5 MB, so it must be a file upload). New sellers start as not verified.
  - Note: **`area` is not in `seed_data.json`** yet.
  - Please check the same rules on the server: required fields, password of at least 8 characters with a number, file type and size.

##### Seller login (`seller-login.html`)
- **Now: a FAKE login.** Any email or phone works with the password **`demo123`**, then the page goes to the dashboard (`js/seller-login.js`, the big `TEMPORARY FAKE LOGIN` comment).
- **Needs:** a login endpoint that accepts **email or phone + password**, and keeps the seller logged in (session or token). On failure the page shows one general message: "Email or phone, or password is incorrect. Please try again." Please don't say which part was wrong.
- **Not built yet:** "Forgot password?" only shows "Password reset isn't available yet (demo)."

##### Seller dashboard (`seller-dashboard.html`)
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

#### 3. Data shapes the pages expect

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

#### 4. Other placeholders (not backend work, but good to know)

- Every footer says "Sample design" or "Sample data: … placeholders".
- `scam-safety.html` is linked from the landing page, the details page and the footers, but **doesn't exist yet.**


## Phase 3 — Development and setup notes

### Pet Finder — Phase 3: MySQL Persistence

#### Files

This package is an **overlay** for your existing working `pet-finder` project. It contains a full `pom.xml`, `.gitignore`, `database/schema.sql`, `Database.java`, three repositories and `Phase3Demo.java`. It does not overwrite your Java OOP models or `App.java`.

#### Prerequisites

- Ubuntu MySQL Server 8+, Java 17+, Maven.
- Existing Phase 2 code must already compile.

#### Installation

From your existing pet-finder repository folder:

```bash
unzip -o ~/Downloads/pet-finder-phase3-complete.zip
```

No existing model classes, static frontend files or App.java are replaced.

#### Create database tables

Check whether MySQL is running:

```bash
sudo systemctl status mysql
```

If not running:

```bash
sudo systemctl start mysql
```

Create the schema (safe to rerun, no DROP or deletion):

```bash
sudo mysql < database/schema.sql
```

Then open an interactive MySQL admin prompt:

```bash
sudo mysql
```

Create a restricted application account. Replace `CHOOSE_A_PRIVATE_PASSWORD` with a new strong password; do not paste your real password into chat or commit it to Git.

```sql
CREATE USER IF NOT EXISTS 'petfinder_app'@'localhost'
IDENTIFIED BY 'CHOOSE_A_PRIVATE_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON pet_finder.*
TO 'petfinder_app'@'localhost';
EXIT;
```

If the account existed with a different password, use `ALTER USER 'petfinder_app'@'localhost' IDENTIFIED BY '...'` in the admin prompt.

#### Environment variables (terminal session)

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=pet_finder
export DB_USER=petfinder_app
read -rsp 'Enter petfinder_app MySQL password: ' DB_PASSWORD; echo
export DB_PASSWORD
```

All the variables last only in this terminal session. Do not type your password into this chat.

#### Compile and test (two separate Java processes)

```bash
mvn clean compile
mvn -q exec:java -Dexec.mainClass=com.petfinder.Phase3Demo -Dexec.args=write
mvn -q exec:java -Dexec.mainClass=com.petfinder.Phase3Demo -Dexec.args=read
```

Expected last line after `read`:

```text
PHASE 3 PERSISTENCE CHECK PASSED
```

The `write` process creates only one idempotent demo seller, listing, animal, health record and enquiry; it also sets the listing status to `Reserved`. The separate `read` process confirms they were reloaded from MySQL. **Rerun `read` after restarting MySQL** if you want to demonstrate persistence even after a server restart.

If the Maven exec plugin runs `App` despite `-Dexec.mainClass`, make sure you installed this ZIP's `pom.xml` and that the property `<mainClass>${exec.mainClass}</mainClass>` is present.

#### Important scope

- Phase 3 is database persistence only, not a new API. The UI still reads its mock JSON until Phase 4.
- `ListingService` remains the working in-memory OOP demonstration from Phase 2. `ListingRepository` is the persistent implementation to use in Phase 4.
- Demo records use reserved identifiers 900001. No real customer data or authentication credentials are stored in this example.
- Schema does not store account passwords yet; Phase 4 authentication must add a password hash and verification document support, with appropriate authorization, validation and file-storage rules.
- Deleting a listing via `ListingRepository.delete` removes its animal, health records and enquiries due to foreign key cascades. Use carefully. Later, consider soft deletion to preserve audit history.
- For local student development, database password lives in an environment variable, not source control. A production deployment should use a secret manager and TLS.

#### Commit

```bash
git status
git add pom.xml .gitignore .env.example database src/main/java/com/petfinder/db src/main/java/com/petfinder/repository src/main/java/com/petfinder/Phase3Demo.java
git commit -m "Complete Phase 3 MySQL persistence"
```


## Phase 4 — Development and setup notes

### Pet Finder — Phase 4: Complete Javalin REST API + Frontend

This ZIP is a complete **Phase 4 overlay** for the working repository after Phases 1–3. Its Java and JavaScript files are full replacements or full new files, not partial snippets. It **does not overwrite** the Phase 2 models, Phase 3 repositories, `pom.xml`, or HTML/CSS. It adds an incremental SQL migration; data already stored in Phase 3 is preserved.

#### What's included

- `src/main/java/com/petfinder/App.java`: complete Javalin launcher, error handlers, static website, API registration and 5 MB-file upload limits
- `src/main/java/com/petfinder/api/ApiRoutes.java`: HTTP routes, validation, seller authorization, csrf checking, DTO handling
- `src/main/java/com/petfinder/api/ApiException.java`: API-specific error class
- `src/main/java/com/petfinder/api/Passwords.java`: PBKDF2 salted password hashing
- `src/main/java/com/petfinder/api/UploadStore.java`: private verification files, public random-named photos
- `src/main/java/com/petfinder/repository/AccountRepository.java`: atomic signup + stored hashed passwords
- `src/main/java/com/petfinder/repository/ApiWriteRepository.java`: transaction-based database inserts with generated IDs; photo, enquiry and report storage
- `database/phase4_migration.sql`: adds seller credentials, listing images and reports tables
- Full replacement frontend scripts: `data.js`, `seller-signup.js`, `seller-login.js`, `seller-dashboard.js`, `add-animal.js`, `animal.js`

#### 1. Preserve the Phase 3 milestone

Use VS Code terminal **in the project root**:

```bash
cd ~/Documents/pet-finder
git status
```

Commit Phase 3 first if necessary (do not commit credentials):

```bash
git add pom.xml .gitignore .env.example database src/main/java
# Only commit when you have reviewed git status for unintended files.
git commit -m "Complete Phase 3 MySQL persistence"
```

If you already committed Phase 3, skip this commit. Keep your `feature/backend-development` branch.

#### 2. Install the Phase 4 overlay

Download the ZIP, then run:

```bash
cd ~/Documents/pet-finder
unzip -o ~/Downloads/pet-finder-phase4-complete.zip
```

Do not delete the existing `public/mock/breeds.json` file: the `/api/breeds` route currently reads those sample breed profiles. Other mock files are unused by the replaced scripts.

#### 3. Add the three new MySQL tables

```bash
sudo mysql < database/phase4_migration.sql
sudo mysql -e "USE pet_finder; SHOW TABLES;"
```

You should see these **8** tables:

- `sellers`, `animals`, `health_records`, `listings`, `enquiries` (Phase 3)
- `seller_credentials`, `listing_images`, `reports` (Phase 4)

This migration creates missing tables without deleting any earlier data. `petfinder_app` needs only SELECT, INSERT, UPDATE and DELETE on `pet_finder.*`; the existing grants from Phase 3 should include new tables.

#### 4. Configure MySQL credentials for this terminal

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=pet_finder
export DB_USER=petfinder_app
read -rsp 'MySQL password: ' DB_PASSWORD; echo
export DB_PASSWORD
```

Do not share your password or add it to your Git repository. Re-export these variables when using a new terminal session.

#### 5. Compile and start server

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=com.petfinder.App
```

Expected startup: `Pet Finder started: http://localhost:7070`.

Open:

- http://localhost:7070 — existing frontend
- http://localhost:7070/api/health — `{ "status": "ok" }`
- http://localhost:7070/api/data — JSON `sellers` and `listings` arrays (may be empty until verified)
- http://localhost:7070/api/breeds — breed metadata from existing seed file

##### Why browse might initially be empty

**Only verified sellers' listings are public**, matching the frontend's verification promise. The Phase 3 demo seller was created as *unverified*, and sellers registered through Phase 4 start unverified. Their listings appear in their own dashboard but not in the public browse page until verified.

For local classroom testing, an **administrator** can approve an account from MySQL:

```bash
sudo mysql -e "SELECT id, business_name, verified FROM pet_finder.sellers ORDER BY id DESC LIMIT 10;"
```

After choosing an actual seller ID (replace 123 in the next command):

```bash
sudo mysql -e "UPDATE pet_finder.sellers SET verified=1 WHERE id=123;"
```

**Do not add a publicly accessible verify endpoint.** A production system would need an authorized admin workflow.

#### 6. Test the complete user journey

1. Open `/seller-signup.html`; register a seller with a PDF/JPG/PNG verification document (max 5 MB) and a password with at least eight characters including a number.
2. Open `/seller-login.html`; log in with your email or phone and password. `demo123` is no longer an automatic password.
3. On `/seller-dashboard.html`, click **Add animal**; complete the form, add at least one vaccination/treatment record, and optionally upload up to eight JPG/PNG/WebP photos (max 5 MB each).
4. Submit the form. The listing is saved to MySQL, not just the browser. Reload the page and confirm it is still there.
5. Use the **Edit** button to change the price or **Mark rehomed** to change status. Refresh and verify the new status persists.
6. From your MySQL administrator session, verify this seller as shown above.
7. Open `/browse.html` in another browser and view the public listing; open its details page and send an enquiry after accepting the scam warning.
8. Log in as the seller again; see the enquiry in the dashboard.
9. Click **Report this listing** on the animal page and enter a reason; confirm the report appears in `pet_finder.reports`.
10. Log out; opening `/seller-dashboard.html` should redirect to login.

Note: Photos are saved under `~/.petfinder-uploads/public-images`, verification documents under `~/.petfinder-uploads/private-documents`. These directories are outside your Git checkout. The app never provides a URL to serve verification documents.

#### 7. HTTP endpoints implemented

| HTTP | Endpoint | Behavior |
|---|---|---|
| GET | `/api/health` | server check |
| GET | `/api/data` | public verified sellers and listings in existing frontend shape |
| GET | `/api/listings` | public listings |
| GET | `/api/listings/{id}` | listing details and seller |
| GET | `/api/breeds` | profile metadata |
| POST | `/api/auth/register` | multipart sign-up and private verification document |
| POST | `/api/auth/login` | credential check and session login |
| GET | `/api/auth/me` | authenticated seller and CSRF token |
| POST | `/api/auth/logout` | end session |
| GET | `/api/sellers/me/listings` | authenticated seller's own listings |
| GET | `/api/sellers/me/enquiries` | authenticated seller's own enquiries |
| POST | `/api/listings` | save animal, health records, listing and 0-8 photos |
| PUT/PATCH | `/api/listings/{id}` | owner-only price/status updates |
| DELETE | `/api/listings/{id}` | owner-only deletion (cascades records/enquiries: demo only) |
| POST | `/api/enquiries` | save buyer enquiry against public available/reserved listing |
| POST | `/api/reports` | record listing report |
| GET | `/media/{filename}` | serve publicly uploaded photos; never verification documents |

For a real deployment, add a formal admin verification UI, password recovery, email confirmation, login rate limits, anti-spam controls, storage cleanups on deletion, transactional status/price updates, managed sessions across restarts, HTTPS and secure cookie settings, CSRF protection for all state-changing cookie-auth routes, audit logs, and a privacy policy. This phase is designed for **local academic development**, not an internet-facing production deployment.

#### 8. Commit milestone only after tests pass

```bash
git status
git add database/phase4_migration.sql src/main/java/com/petfinder src/main/resources/public/js
git commit -m "Implement Phase 4 REST API and frontend integration"
```

#### If a test fails

Send the complete error text from `mvn clean compile` or the browser Network tab (request path, status, response **excluding passwords**). Since phase 4 was assembled outside your laptop, a successful local compile and full end-to-end test are still required to mark it complete.


## Phase 5 — Development and setup notes

### Pet Finder — Phase 5: Final Testing & Academic Requirements

This is a **Phase 5 overlay** for your *already working* Phase 4 Pet Finder repository.

It adds **21 JUnit 5 unit tests**, a read-only/invalid-request API smoke test, a read-only MySQL integrity report, and academic evidence/checklists. **It does not replace App.java, the REST routes, model classes, frontend, or database schema.** Do not delete your current database or uploaded files.

#### A. Files in this package

- `pom.xml` — **complete** Maven project file with existing Javalin/Jackson/MySQL dependencies, plus JUnit Jupiter 5 and Surefire 3
- `src/test/java/com/petfinder/model/AnimalModelTest.java` — inheritance, polymorphism, encapsulation, health records, validation
- `src/test/java/com/petfinder/model/ListingTest.java` — valid/invalid listing behavior
- `src/test/java/com/petfinder/model/SellerAndEnquiryTest.java` — seller and enquiry models
- `src/test/java/com/petfinder/service/ListingServiceTest.java` — collections, sorting, owner filtering, CRUD, custom exceptions
- `src/test/java/com/petfinder/api/PasswordsTest.java` — PBKDF2 hashing, verification, invalid passwords and API errors
- `scripts/phase5_api_checks.py` — live API checks; uses Python 3 standard library; no real records are created or deleted
- `scripts/phase5_all_checks.sh` — convenience wrapper for Maven unit tests and the API checker
- `database/phase5_checks.sql` — **read-only** database counts, relationship checks and constraints
- `PHASES_TEST.md` — evidence map for each of the nine requirements
- `PHASES_TEST.md` — fill-in test results report

#### B. Before installing

Your repository should already be on `feature/backend-development` and Phase 4 should be working, as you verified.

```bash
cd ~/Documents/pet-finder
git branch --show-current
git status
cp pom.xml ~/Documents/pet-finder-pom-before-phase5.xml
```

Review any existing uncommitted work before overwriting files. The Phase 5 ZIP updates only `pom.xml` and adds new test/report files. It does not contain passwords or private files. If you previously customized `pom.xml` beyond the Phase 3 version, compare it to the backup after installation.

#### C. Install the package

Download the Phase 5 ZIP into `~/Downloads`, then run:

```bash
cd ~/Documents/pet-finder
unzip -o ~/Downloads/pet-finder-phase5-complete.zip
git diff -- pom.xml
```

If you made changes to the dependencies in your original `pom.xml`, review the diff and preserve those custom dependencies before continuing.

#### D. Test Java classes — run this first

```bash
mvn clean test
```

This compiles the application and **executes** the 21 JUnit tests. Maven may download the testing dependencies on first run.

Expected success: a summary reporting 21 executed tests, zero failures and zero errors, followed by `BUILD SUCCESS`. If the count differs, inspect the test discovery output; do not assume tests ran just because Maven compiled successfully.

Test reports can be found in `target/surefire-reports/`.

To rerun a single test class:

```bash
mvn -Dtest=ListingServiceTest test
```

#### E. Run the live API tests

Start MySQL and Javalin in **Terminal 1** (if they are not already running):

```bash
sudo systemctl start mysql
cd ~/Documents/pet-finder
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=pet_finder
export DB_USER=petfinder_app
read -rsp 'MySQL application password: ' DB_PASSWORD; echo
export DB_PASSWORD
mvn exec:java -Dexec.mainClass=com.petfinder.App
```

Keep Terminal 1 open. In **Terminal 2** run:

```bash
cd ~/Documents/pet-finder
python3 scripts/phase5_api_checks.py
```

The checker validates public GET routes, JSON payload structure, page serving, authenticated route protection, missing/invalid inputs and error codes. It **never supplies valid seller credentials or sends a valid record-creation, update or delete payload**.

Expected: `RESULT: ... passed, 0 failed, ... skipped`. The exact number of skips depends on whether verified public listings exist.

If using a nonstandard port:

```bash
python3 scripts/phase5_api_checks.py --base-url http://localhost:7070
```

The convenient combined command (with the server already running) is:

```bash
bash scripts/phase5_all_checks.sh
```

#### F. Database integrity verification

Run this with MySQL available:

```bash
sudo mysql < database/phase5_checks.sql
```

The query produces table counts, a `violations` column for orphan records / invalid prices / zero quantity / missing health records, and simple status counts. The `violations` counts should normally all be **0**. It does not display private seller details or mutate records.

#### G. Manual authenticated / browser tests (required)

The automated tests deliberately do not log in or change real records. Use a **dedicated test seller account** and safe test listings, not someone else's production data. In the browser, confirm:

- [ ] Seller registration succeeds with valid input; duplicate email/phone is rejected without creating another account
- [ ] Wrong password returns an appropriate error; correct password creates a session
- [ ] Seller dashboard loads only after logging in
- [ ] Each seller sees only their own listings and enquiries
- [ ] Creating a pet with health details persists after page reload and Javalin restart
- [ ] Creating livestock demonstrates the alternate `Livestock` class
- [ ] Listing price and status updates persist; invalid price/status is rejected and does not change saved data
- [ ] Uploading a permitted animal photo works, and the file is displayed
- [ ] Invalid file extension / oversize upload is rejected
- [ ] Unverified seller listings stay off the public browse page
- [ ] After local-admin approval, listings appear publicly
- [ ] Buyer sends an enquiry and seller sees it
- [ ] Buyer report is saved (verify count with the SQL report)
- [ ] Logging out invalidates the session; protected routes return 401
- [ ] State-changing API requests without a valid CSRF token are refused
- [ ] PUT / PATCH / DELETE from a different logged-in seller cannot modify someone else's listing (expect 403)

**Important:** If an invalid update returns `500` instead of `400`, or changes other fields before failing, record it as a bug. Never mark a failure as passed just because the happy path works.

#### H. Evidence for your school demonstration

Keep screenshots/recordings of:

1. `mvn clean test` output showing test count and `BUILD SUCCESS`
2. `python3 scripts/phase5_api_checks.py` with 0 failures
3. `sudo mysql < database/phase5_checks.sql` showing integrity violations = 0
4. Java `Animal`, `Pet`, `Livestock`, `ListingService`, `ListingNotFoundException`
5. Public browse page + authenticated seller dashboard and an enquiry
6. A listing after Javalin restart showing data persisted

Use `PHASES_TEST.md` to explain each requirement during presentation.

#### I. Commit only after verification

```bash
cd ~/Documents/pet-finder
git status
git add pom.xml src/test scripts/phase5_all_checks.sh scripts/phase5_api_checks.py database/phase5_checks.sql PHASES_TEST.md PHASES_TEST.md README.md
git diff --cached --stat
git commit -m "Add Phase 5 automated tests and academic verification"
```

Do **not** commit `.env`, passwords, credentials, real private documents, uploaded photos, `target/` or `__pycache__/`.

#### Limitations

The provided unit tests can run without MySQL; the API and SQL checks require your running Phase 4 application and database. This is an academic test baseline, **not** a penetration test, load test or production certification. Additional security protections are required before public deployment (e.g. account recovery, rate limits, secure HTTPS cookies, admin workflows, file retention rules, abuse monitoring).

