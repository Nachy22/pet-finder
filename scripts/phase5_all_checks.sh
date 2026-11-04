#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo '========== Phase 5: JUnit tests =========='
mvn clean test

echo '========== Phase 5: Live HTTP API checks =========='
python3 scripts/phase5_api_checks.py --base-url "${PETFINDER_BASE_URL:-http://localhost:7070}"

echo '========== Phase 5: Automated checks passed =========='
echo 'Run: sudo mysql < database/phase5_checks.sql'
echo 'Then complete the authenticated manual checklist in PHASE5_README.md.'
