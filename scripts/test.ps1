$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
try {
    docker compose -f compose.test.yaml up --build --abort-on-container-exit --exit-code-from tests
    $testExitCode = $LASTEXITCODE
} finally { docker compose -f compose.test.yaml down --volumes --remove-orphans }
exit $testExitCode
