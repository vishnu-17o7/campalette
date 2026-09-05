# Creates the local Play upload keystore and keystore.properties.
# Never commit the JKS or the properties file.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$keytool = Get-Command keytool -ErrorAction SilentlyContinue
if (-not $keytool) {
    throw "keytool was not found on PATH. Install JDK 17 and retry."
}

$releaseDir = Join-Path $root "release"
$keystorePath = Join-Path $releaseDir "campalette-upload.jks"
$propertiesPath = Join-Path $root "keystore.properties"

if (Test-Path $keystorePath) {
    throw "Refusing to overwrite existing $keystorePath"
}
if (Test-Path $propertiesPath) {
    throw "Refusing to overwrite existing $propertiesPath"
}

New-Item -ItemType Directory -Force -Path $releaseDir | Out-Null

Write-Host "You will be asked for a keystore password, key password, and certificate name."
Write-Host "Use a unique password and store it in a password manager."
Write-Host "Play requires the certificate to remain valid after 22 October 2033; this key is valid 10,000 days."
Write-Host ""

& keytool -genkeypair -v `
    -keystore $keystorePath `
    -alias campalette-upload `
    -keyalg RSA `
    -keysize 2048 `
    -validity 10000 `
    -storetype JKS

if ($LASTEXITCODE -ne 0) {
    throw "keytool failed with exit code $LASTEXITCODE"
}

$storePassword = Read-Host -AsSecureString "Re-enter the keystore password to write keystore.properties"
$keyPassword = Read-Host -AsSecureString "Re-enter the key password (same as store password unless you set a different one)"
$storeBstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePassword)
$keyBstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($keyPassword)
try {
    $storePlain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($storeBstr)
    $keyPlain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($keyBstr)
    @"
storeFile=release/campalette-upload.jks
storePassword=$storePlain
keyAlias=campalette-upload
keyPassword=$keyPlain
"@ | Set-Content -Path $propertiesPath -Encoding ascii
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($storeBstr)
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($keyBstr)
}

Write-Host ""
Write-Host "Wrote $keystorePath"
Write-Host "Wrote $propertiesPath"
Write-Host "Back these up. They are gitignored and required for every Play upload."
