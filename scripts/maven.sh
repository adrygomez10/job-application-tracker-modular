#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
trap 'docker compose -f compose.quality.yaml down --remove-orphans' EXIT
docker compose -f compose.quality.yaml run --rm --build maven "$@"
