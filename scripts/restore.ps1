param([Parameter(Mandatory=$true)][string]$Path, [string]$Database = 'tracker_restore', [switch]$ReplaceExisting)
$ErrorActionPreference = 'Stop'
$backupFile = (Resolve-Path -LiteralPath $Path).Path
Set-Location (Split-Path $PSScriptRoot -Parent)
if ($Database -notmatch '^[a-z][a-z0-9_]{0,62}$' -or $Database -in @('postgres','template0','template1')) { throw 'Invalid target database.' }
if ($Database -eq 'tracker' -and -not $ReplaceExisting) { throw 'Restoring over tracker requires -ReplaceExisting.' }
if ($ReplaceExisting) {
    if ($Database -eq 'tracker') { docker compose stop app; if ($LASTEXITCODE -ne 0) { throw 'Could not stop app.' } }
    docker compose exec -T db dropdb -U tracker --if-exists --force $Database
    if ($LASTEXITCODE -ne 0) { throw 'Could not remove target database.' }
}
docker compose exec -T db createdb -U tracker $Database
if ($LASTEXITCODE -ne 0) { throw 'Target database must be new, or explicitly replaced.' }
docker compose cp $backupFile db:/tmp/tracker-restore.dump
if ($LASTEXITCODE -ne 0) { throw 'Could not copy backup.' }
docker compose exec -T db pg_restore --exit-on-error --no-owner --no-acl -U tracker -d $Database /tmp/tracker-restore.dump
if ($LASTEXITCODE -ne 0) { throw 'Restore failed. App remains stopped if replacing tracker.' }
docker compose exec -T db rm /tmp/tracker-restore.dump
if ($Database -eq 'tracker') { docker compose up -d app }
Write-Host "Copia restaurada en: $Database"
