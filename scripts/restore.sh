#!/usr/bin/env bash
set -euo pipefail
backup=${1:?Usage: bash scripts/restore.sh BACKUP [DATABASE] [--replace-existing]}
backup=$(cd "$(dirname "$backup")" && pwd)/$(basename "$backup")
database=${2:-tracker_restore}
replace=${3:-}
cd "$(dirname "$0")/.."
[[ "$database" =~ ^[a-z][a-z0-9_]{0,62}$ && "$database" != postgres && "$database" != template0 && "$database" != template1 ]] || { echo 'Invalid target database.' >&2; exit 1; }
[[ "$database" != tracker || "$replace" == --replace-existing ]] || { echo 'Replacing tracker requires --replace-existing.' >&2; exit 1; }
if [[ "$replace" == --replace-existing ]]; then
    if [[ "$database" == tracker ]]; then docker compose stop app; fi
    docker compose exec -T db dropdb -U tracker --if-exists --force "$database"
fi
docker compose exec -T db createdb -U tracker "$database"
docker compose cp "$backup" db:/tmp/tracker-restore.dump
docker compose exec -T db pg_restore --exit-on-error --no-owner --no-acl -U tracker -d "$database" /tmp/tracker-restore.dump
docker compose exec -T db rm /tmp/tracker-restore.dump
if [[ "$database" == tracker ]]; then docker compose up -d app; fi
echo "Copia restaurada en: $database"
