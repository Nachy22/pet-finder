#!/usr/bin/env bash
set -euo pipefail
HOST="${1:-http://localhost:7070}"
echo "[1/4] Health endpoint"
curl --fail-with-body -sS "$HOST/api/health"; echo
 echo "[2/4] Public data endpoint"
curl --fail-with-body -sS "$HOST/api/data" | python3 -m json.tool | head -40
 echo "[3/4] Breed profiles endpoint"
curl --fail-with-body -sS "$HOST/api/breeds" | python3 -m json.tool | head -25
 echo "[4/4] Private endpoint should require login (expect HTTP 401)"
response=$(curl -sS -o /dev/null -w '%{http_code}' "$HOST/api/auth/me")
echo "HTTP $response"
test "$response" = '401'
echo 'Public API smoke check complete. Continue with the browser user journey in PHASE4_README.md.'
