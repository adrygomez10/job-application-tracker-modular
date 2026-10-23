#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
trap 'docker compose -f compose.test.yaml down --volumes --remove-orphans' EXIT
docker compose -f compose.test.yaml up --build --abort-on-container-exit --exit-code-from tests
