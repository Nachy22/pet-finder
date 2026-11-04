#!/usr/bin/env python3
"""Phase 5 live HTTP checks for Pet Finder.

Uses only the Python standard library. No account credentials are needed.
No valid record creation, update or deletion requests are sent.
Start the Javalin server before running this script.
"""

import argparse
import json
import sys
import urllib.error
import urllib.request
from urllib.parse import urljoin


def request(base, path, method="GET", body=None, timeout=8):
    headers = {"Accept": "application/json"}
    data = None
    if body is not None:
        data = body.encode("utf-8") if isinstance(body, str) else json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(
        urljoin(base.rstrip("/") + "/", path.lstrip("/")),
        data=data, headers=headers, method=method
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response:
            return response.status, response.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode("utf-8", "replace")


def parsed_json(body):
    return json.loads(body)


def main():
    parser = argparse.ArgumentParser(description="Test running Pet Finder Javalin API")
    parser.add_argument("--base-url", default="http://localhost:7070")
    parser.add_argument("--timeout", type=int, default=8)
    args = parser.parse_args()
    base = args.base_url
    timeout = args.timeout
    passed = failed = skipped = 0

    def check(name, path, expected, method="GET", body=None, validate=None):
        nonlocal passed, failed
        try:
            status, response = request(base, path, method, body, timeout)
            if status != expected:
                raise AssertionError(f"HTTP {status}; expected {expected}; response: {response[:180]!r}")
            value = parsed_json(response) if validate else response
            if validate:
                validate(value)
            print(f"PASS  {name}  [{method} {path} -> {status}]")
            passed += 1
            return value
        except (AssertionError, OSError, ValueError, TypeError, KeyError) as error:
            print(f"FAIL  {name}: {error}")
            failed += 1
            return None

    def require_keys(keys, value):
        if not isinstance(value, dict) or not all(k in value for k in keys):
            raise AssertionError(f"Expected JSON object with keys: {keys}")

    print(f"\nPet Finder Phase 5 API checks: {base}")
    print("These tests use no seller passwords and do not intentionally change database records.\n")

    check("Server health", "/api/health", 200,
        validate=lambda data: (
            require_keys(["status"], data),
            data["status"] == "ok" or (_ for _ in ()).throw(AssertionError("status must be ok"))
        ))
    check("Javalin welcome route", "/hello", 200)
    check("Home page is served", "/", 200)
    check("Seller login page is served", "/seller-login.html", 200)
    check("Breed data loads", "/api/breeds", 200, validate=lambda data: None)

    data = check("Public sellers and listings", "/api/data", 200,
        validate=lambda obj: require_keys(["sellers", "listings"], obj))
    listings_body = check("Public listing route", "/api/listings", 200,
        validate=lambda obj: require_keys(["listings"], obj))

    if isinstance(data, dict) and isinstance(listings_body, dict):
        try:
            sellers = data["sellers"]
            listings = data["listings"]
            other_listings = listings_body["listings"]
            if not isinstance(sellers, list) or not isinstance(listings, list) or not isinstance(other_listings, list):
                raise AssertionError("Expected lists of sellers and listings")
            if any(seller.get("verified") is not True for seller in sellers):
                raise AssertionError("Unverified seller exposed publicly")
            visible_ids = {seller.get("id") for seller in sellers}
            if any(listing.get("sellerId") not in visible_ids for listing in listings):
                raise AssertionError("Listing owner missing from visible seller list")
            ids1 = {listing.get("id") for listing in listings}
            ids2 = {listing.get("id") for listing in other_listings}
            if ids1 != ids2:
                raise AssertionError("Public listing endpoints disagree")
            print("PASS  Public data is restricted to verified sellers and endpoint results agree")
            passed += 1
            if listings:
                public_id = listings[0]["id"]
                check("Public listing details", f"/api/listings/{public_id}", 200,
                    validate=lambda obj: require_keys(["listing", "seller", "imageUrls"], obj))
            else:
                print("SKIP  Public listing details: no verified public listings currently exist")
                skipped += 1
        except (AssertionError, AttributeError, KeyError, TypeError) as error:
            print(f"FAIL  Public listing visibility: {error}")
            failed += 1

    check("Unauthenticated current seller blocked", "/api/auth/me", 401)
    check("Unauthenticated seller listings blocked", "/api/sellers/me/listings", 401)
    check("Unauthenticated seller enquiries blocked", "/api/sellers/me/enquiries", 401)
    check("Invalid login rejected", "/api/auth/login", 401, "POST",
        {"emailOrPhone": "nonexistent@example.invalid", "password": "InvalidPassword123!"})
    check("Missing login fields rejected", "/api/auth/login", 400, "POST", {})
    check("Malformed JSON rejected", "/api/auth/login", 400, "POST", "{")
    check("Invalid listing ID rejected", "/api/listings/0", 400)
    check("Missing listing returns 404", "/api/listings/2147483647", 404)
    check("Anonymous listing creation blocked", "/api/listings", 401, "POST", {})
    check("Anonymous listing update blocked", "/api/listings/1", 401, "PUT", {"price": 1})
    check("Anonymous listing patch blocked", "/api/listings/1", 401, "PATCH", {"price": 1})
    check("Anonymous listing delete blocked", "/api/listings/1", 401, "DELETE")
    check("Anonymous logout blocked", "/api/auth/logout", 401, "POST")
    check("Invalid enquiry rejected", "/api/enquiries", 400, "POST", {})
    check("Invalid report rejected", "/api/reports", 400, "POST", {})
    check("Incomplete seller registration rejected", "/api/auth/register", 400, "POST", {})

    print(f"\nRESULT: {passed} passed, {failed} failed, {skipped} skipped")
    if failed:
        print("Do not mark Phase 5 complete. Investigate the failing route(s) first.")
        return 1
    print("Automated anonymous API checks passed. Continue authenticated manual checks in README.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
