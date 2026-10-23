#!/usr/bin/env bash
set -euo pipefail
trap 'bash scripts/collect-reports.sh' EXIT
mvn -B -ntp verify
