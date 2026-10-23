#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
umask 077
mkdir -p backups
destination="backups/tracker-$(date +%Y%m%d-%H%M%S).dump"
docker compose exec -T db pg_dump -U tracker -d tracker -Fc -f /tmp/tracker-backup.dump
docker compose cp db:/tmp/tracker-backup.dump "$destination"
docker compose exec -T db rm /tmp/tracker-backup.dump
echo "Copia guardada: $destination"
