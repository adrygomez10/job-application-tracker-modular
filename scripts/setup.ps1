$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (Test-Path -LiteralPath '.env') { throw '.env already exists. Setup never rotates existing credentials.' }
$username = Read-Host 'Usuario [adrian]'
if ([string]::IsNullOrWhiteSpace($username)) { $username = 'adrian' }
if ($username -notmatch '^[A-Za-z0-9._-]{1,64}$') { throw 'Use 1–64 letters, numbers, dots, underscores or hyphens.' }
$securePassword = Read-Host 'Contraseña (mínimo 12 caracteres; máximo 72 bytes UTF-8)' -AsSecureString
$plainPassword = [System.Net.NetworkCredential]::new('', $securePassword).Password
$previousOutputEncoding = $OutputEncoding
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
try {
    docker build --target runtime -t job-application-tracker-modular:local .
    if ($LASTEXITCODE -ne 0) { throw 'Docker build failed.' }
    $hashOutput = $plainPassword | docker run --rm -i --entrypoint java job-application-tracker-modular:local '-Dloader.main=dev.adriangm.tracker.tools.PasswordHash' -cp /app/app.jar org.springframework.boot.loader.launch.PropertiesLauncher
    if ($LASTEXITCODE -ne 0) { throw 'Password hashing failed.' }
    $hash = ($hashOutput | Where-Object { $_ -match '^\{bcrypt\}\$2[aby]\$' } | Select-Object -Last 1).Trim()
    if (-not $hash) { throw 'No password hash was generated.' }
    $randomBytes = New-Object byte[] 32
    $random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($randomBytes) } finally { $random.Dispose() }
    $databasePassword = [BitConverter]::ToString($randomBytes).Replace('-', '').ToLowerInvariant()
    $content = "APP_PORT=8091`nTRACKER_USERNAME=$username`nTRACKER_PASSWORD_HASH='$hash'`nDB_PASSWORD=$databasePassword`n"
    [System.IO.File]::WriteAllText((Join-Path (Get-Location) '.env'), $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host 'Configuración creada. Arranca con: docker compose up -d'
} finally { $plainPassword = $null; $hashOutput = $null; $hash = $null; $OutputEncoding = $previousOutputEncoding; $securePassword.Dispose() }
