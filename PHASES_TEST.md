# Pet Finder — Phases, Tests & Academic Verification

This document consolidates academic requirements, test procedures, observed results, and the original Phase 5 testing report.

## Verified results (4 November 2026)

| Verification | Result | Evidence |
|---|---|---|
| Java unit tests | **21 passed, 0 failed, 0 errors, 0 skipped** | `mvn clean test` reported `BUILD SUCCESS` |
| Live API checker | **25 passed, 0 failed, 0 skipped** | Output reported by project developer |
| MySQL integrity checks | **0 violations** across 10 checks | `database/phase5_checks.sql` output |
| Persistence | **Passed** | Phase 3 write and read processes returned the same saved records |
| Browser workflow | **Passed** | Registration, login, create listing, refresh, approval, public listing, enquiry, dashboard receipt |

These are the recorded results, not a claim that every possible edge case or security scenario has been tested.

## Run tests again

```bash
cd ~/Documents/pet-finder
mvn clean test
python3 scripts/phase5_api_checks.py  # requires Javalin + MySQL to be running
sudo mysql < database/phase5_checks.sql
```

The API checks avoid intentional modification of live records. Authenticated cross-account isolation and manual input-validation cases should be checked separately before final sign-off.

## Phases overview

| Phase | Scope | Status |
|---|---|---|
| 1 | Repository setup and inspection | Complete |
| 2 | Java OOP models and demonstrations | Complete |
| 3 | JDBC + MySQL persistent repositories | Complete |
| 4 | REST API and frontend integration | Core workflow complete |
| 5 | Testing and academic verification | Automated checks passed; manual security review recommended |

---

## Academic requirements — 9 mandatory criteria

### Pet Finder — Evidence map for the nine fixed requirements

Status below describes **implemented code and designed tests**, not a claim that the tests have been run on your machine. Record your actual results in `PHASES_TEST.md`.

| # | Academic requirement | Where it is satisfied | Evidence / test |
|---|---|---|---|
| 1 | Real classes | `model/Animal.java`, `Pet.java`, `Livestock.java`, `Seller.java`, `Listing.java`, `HealthRecord.java`, `Enquiry.java` | Different responsibilities; `AnimalModelTest`, `ListingTest`, `SellerAndEnquiryTest` |
| 2 | Encapsulation | Private fields + getters + validation in `Animal`, `Listing`, `Seller`; protective health-record list copy | `AnimalModelTest.animalFieldsArePrivateAndNameCanBeChangedThroughMethod`, `healthRecordsCanOnlyBeAddedThroughMethod`, listing validation tests |
| 3 | Inheritance or interface | `Pet extends Animal` and `Livestock extends Animal` | `AnimalModelTest.petAndLivestockShareAnimalParentAndUsePolymorphism` |
| 4 | Polymorphism | `Animal.describe()` / `getType()` overridden; `List<Animal>` containing both subclasses | Same test and `Phase2Demo.java` console output |
| 5 | Custom exception | `exception/ListingNotFoundException.java` thrown by `ListingService`; handled by Phase 4 `App.java` with HTTP 404 | `ListingServiceTest.duplicateIdsAndMissingListingsThrowExpectedExceptions`; API 404 test |
| 6 | Collections | `HashMap<Integer,Listing>` in `ListingService`, `List<HealthRecord>` in `Animal`, `List<Animal>` in demo | `ListingServiceTest.collectionBackedCrudWorks`, health records test, Phase 2 demo |
| 7 | At least 5 REST endpoints, GET, POST, PUT/DELETE | `api/ApiRoutes.java`: GET `/api/listings`, GET `/api/listings/{id}`, POST `/api/listings`, PUT `/api/listings/{id}`, DELETE `/api/listings/{id}`, more | `scripts/phase5_api_checks.py` and live browser testing; code inspection of route definitions |
| 8 | Saving data across restart | `db/Database.java`, MySQL and Phase 3/4 repository classes; SQL schema | Earlier successful `Phase3Demo write` then separate `Phase3Demo read`; repeat listing query after Javalin restart |
| 9 | Frontend served by Javalin using endpoints | `App.java` static files; `public/js/` fetch calls to `/api/*` | `GET /seller-login.html` and `/`; register, login, create listing, refresh, verify, enquire browser scenario |

#### API routes to demonstrate

- **GET** `/api/health`, `/api/data`, `/api/listings`, `/api/listings/{id}`, `/api/auth/me` (authenticated)
- **POST** `/api/auth/register`, `/api/auth/login`, `/api/listings`, `/api/enquiries`, `/api/reports`
- **PUT/PATCH** `/api/listings/{id}` (authenticated owner, with CSRF token)
- **DELETE** `/api/listings/{id}` (authenticated owner, with CSRF token)

Do not expose a publicly accessible seller-approval endpoint for a classroom demonstration. Approve a test seller via local administrator SQL only.

#### Demonstration walkthrough

Start with the class hierarchy and its polymorphic `describe()` behavior, show the custom exception from `ListingService`, then explain the MySQL repository classes. In the browser, show a verified seller's public listing; log in as that seller and display listings/enquiries. Finally show how the same listing remains after refreshing or restarting the server, and display the JUnit and API test summaries.


## Detailed Phase 5 test report and manual checklist

### Pet Finder — Phase 5 Test Report

**Date:** [fill in]
**Tester:** [fill in]
**Git branch:** `feature/backend-development`
**Commit:** [fill in]
**Runtime:** Java [version], MySQL [version], Ubuntu [version]

#### Automated checks

| Command | Expected | Actual result | Pass/Fail |
|---|---|---|---|
| `mvn clean test` | 21 unit tests, 0 failures/errors, BUILD SUCCESS | [fill in] | [ ] |
| `python3 scripts/phase5_api_checks.py` | 0 failures; skipped detail test is allowed if no public listings | [fill in] | [ ] |
| `sudo mysql < database/phase5_checks.sql` | All integrity violations = 0 | [fill in] | [ ] |

#### Manual browser tests

| Scenario | Expected | Actual result | Pass/Fail |
|---|---|---|---|
| Registration | New seller saved as unverified | [fill in] | [ ] |
| Correct credentials | Dashboard loads | [fill in] | [ ] |
| Wrong credentials | Login rejected safely | [fill in] | [ ] |
| Create listing | Seller listing saved | [fill in] | [ ] |
| Reload/restart | Listing unchanged after restart | [fill in] | [ ] |
| Update price/status | Changes persist | [fill in] | [ ] |
| Invalid price/status | Rejected without data corruption | [fill in] | [ ] |
| Seller authorization | Another seller cannot edit/delete | [fill in] | [ ] |
| Approval workflow | Verified listing becomes public | [fill in] | [ ] |
| Buyer enquiry | Seller can view it | [fill in] | [ ] |
| Upload restrictions | Invalid files rejected | [fill in] | [ ] |
| Logout/CSRF | Protected routes reject invalid sessions/tokens | [fill in] | [ ] |

#### Nine academic requirements

See `PHASES_TEST.md`. Record screenshots/code locations demonstrating each requirement here:

1. Real classes: [ ]
2. Encapsulation: [ ]
3. Inheritance/interface: [ ]
4. Polymorphism: [ ]
5. Custom exceptions: [ ]
6. Collections: [ ]
7. REST endpoints: [ ]
8. Saving data: [ ]
9. Javalin-served frontend: [ ]

#### Known issues / limitations

[Fill in any failing tests and how they were resolved. Do not report passing results for unrun tests.]

#### Conclusion

[Complete only after reviewing all test results.]

