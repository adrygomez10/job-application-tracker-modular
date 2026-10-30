#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
terraform -chdir=infra fmt -check -recursive
terraform -chdir=infra init -backend=false -input=false -lockfile=readonly
terraform -chdir=infra validate
terraform -chdir=infra test
helm lint deploy/helm/tracker -f deploy/validation-values.yaml --strict
work=$(mktemp -d)
rendered="$work/rendered.yaml"
trap 'rm -f "$rendered"; rmdir "$work"' EXIT
helm template tracker deploy/helm/tracker --namespace tracker \
  -f deploy/validation-values.yaml >"$rendered"
[[ -s "$rendered" ]]
kubeconform -strict -summary -kubernetes-version 1.35.0 "$rendered" deploy/platform.yaml
# The chart must reject malformed digests rather than falling back to mutable tags.
if helm template tracker deploy/helm/tracker -f deploy/validation-values.yaml \
  --set-string image.digest=latest >/dev/null 2>&1; then
  echo "Chart accepted a mutable image reference." >&2
  exit 1
fi
echo "Cloud configuration validated offline; no AWS resources were created."
