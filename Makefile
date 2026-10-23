# Optional GNU Make + Bash shortcuts. Docker Compose owns the environment.
SHELL := bash
.SHELLFLAGS := -eu -o pipefail -c
.DEFAULT_GOAL := help
.NOTPARALLEL:

# Pass restore paths through the environment rather than interpolating shell code.
export FILE

.PHONY: help doctor setup build compile verify up down rebuild ps logs quality format test backup restore check-env

help:
	@printf '%s\n' \
	  'Job Application Tracker' \
	  '' \
	  'Primer arranque: make setup && make up' \
	  '' \
	  '  make doctor       Comprueba Docker, Compose y el motor' \
	  '  make setup        Construye la imagen y pide las credenciales (interactivo)' \
	  '  make up           Arranca y espera a que los servicios estén saludables' \
	  '  make down         Detiene los servicios conservando los datos' \
	  '  make rebuild      Reconstruye y arranca tras modificar el código' \
	  '  make build        Construye la imagen sin arrancar servicios' \
	  '  make compile      Compila Java con Maven en Docker' \
	  '  make verify       Ejecuta calidad y tests sin PostgreSQL ni navegador' \
	  '  make ps           Muestra el estado de los servicios' \
	  '  make logs         Sigue los registros de la aplicación (salir: Ctrl+C)' \
	  '  make test         Ejecuta la suite aislada y retira su entorno' \
	  '  make quality      Comprueba formato, estilo, complejidad, duplicados y arquitectura' \
	  '  make format       Aplica el formato compartido al código Java y al POM' \
	  '  make backup       Guarda una copia en backups/' \
	  '  make restore FILE="backups/FILE.dump"' \
	  '                    Restaura en tracker_restore conservando la base de la aplicación'

doctor:
	@command -v docker >/dev/null 2>&1 || { printf '%s\n' 'Docker CLI not found. Docker with Compose v2 is required.' >&2; exit 1; }
	@docker compose version >/dev/null
	@docker info >/dev/null
	@printf '%s\n' 'Docker and Compose are ready.'

check-env: doctor
	@test -f .env || { printf '%s\n' 'Missing .env. Run make setup first.' >&2; exit 1; }

setup: doctor
	bash scripts/setup.sh

build: doctor
	docker build --target runtime -t job-application-tracker-modular:local .

compile: doctor
	bash scripts/maven.sh compile

verify: doctor
	bash scripts/maven.sh -DskipITs=true -Djacoco.skip=true verify

up: check-env
	docker compose up -d --wait

down: check-env
	docker compose down

rebuild: check-env
	docker compose up -d --build --wait

ps: check-env
	docker compose ps

logs: check-env
	docker compose logs --follow --tail=100 app

test: doctor
	bash scripts/test.sh

quality: doctor
	bash scripts/quality.sh

format: doctor
	docker compose -f compose.quality.yaml run --rm --build format

backup: check-env
	bash scripts/backup.sh

restore: check-env
	@test -n "$$FILE" || { printf '%s\n' 'Usage: make restore FILE="backups/FILE.dump"' >&2; exit 1; }
	@test -f "$$FILE" || { printf '%s\n' 'Backup file not found.' >&2; exit 1; }
	bash scripts/restore.sh "$$FILE"
