#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
for tool in actionlint shellcheck shfmt; do
  command -v "$tool" >/dev/null || {
    echo "Missing tool: $tool" >&2
    exit 1
  }
done
for script in scripts/cloud/*.sh; do
  bash -n "$script"
done
shellcheck --external-sources scripts/cloud/*.sh
shfmt -d -i 2 -ci -bn scripts/cloud/*.sh
actionlint
bash scripts/cloud/validate.sh
