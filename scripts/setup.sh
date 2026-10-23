#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
[[ ! -e .env ]] || { echo '.env already exists; setup never rotates credentials.' >&2; exit 1; }
read -r -p 'Usuario [adrian]: ' username
username=${username:-adrian}
[[ "$username" =~ ^[A-Za-z0-9._-]{1,64}$ ]] || { echo 'Invalid username.' >&2; exit 1; }
read -r -s -p 'Contraseña (12 caracteres mínimo; 72 bytes UTF-8 máximo): ' password
printf '\n'
docker build --target runtime -t job-application-tracker-modular:local .
hash=$(printf '%s\n' "$password" | docker run --rm -i --entrypoint java job-application-tracker-modular:local -Dloader.main=dev.adriangm.tracker.tools.PasswordHash -cp /app/app.jar org.springframework.boot.loader.launch.PropertiesLauncher)
unset password
[[ "$hash" == '{bcrypt}$2'* ]] || { echo 'Hash generation failed.' >&2; exit 1; }
database_password=$(docker run --rm --entrypoint sh job-application-tracker-modular:local -c 'od -An -N32 -tx1 /dev/urandom | tr -d " \n"')
umask 077
printf "APP_PORT=8091\nTRACKER_USERNAME=%s\nTRACKER_PASSWORD_HASH='%s'\nDB_PASSWORD=%s\n" "$username" "$hash" "$database_password" > .env
unset hash database_password
echo 'Configuración creada. Arranca con: docker compose up -d'
