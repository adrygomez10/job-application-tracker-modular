$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
New-Item -ItemType Directory -Force backups | Out-Null
$destination = 'backups/tracker-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.dump'
docker compose exec -T db pg_dump -U tracker -d tracker -Fc -f /tmp/tracker-backup.dump
if ($LASTEXITCODE -ne 0) { throw 'Backup failed.' }
docker compose cp db:/tmp/tracker-backup.dump $destination
if ($LASTEXITCODE -ne 0) { throw 'Copy failed.' }
docker compose exec -T db rm /tmp/tracker-backup.dump
Write-Host "Copia guardada: $destination"
