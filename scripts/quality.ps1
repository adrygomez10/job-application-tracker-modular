$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
try {
    docker compose -f compose.quality.yaml run --rm --build quality
    $qualityExitCode = $LASTEXITCODE
} finally { docker compose -f compose.quality.yaml down --remove-orphans }
exit $qualityExitCode
