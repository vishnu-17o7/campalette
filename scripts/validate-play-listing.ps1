# Checks Play listing files for size, pixel format, and copy limits.
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

function Assert-Png($path, $width, $height, $label) {
    if (-not (Test-Path $path)) { throw "$label missing: $path" }
    $img = [System.Drawing.Image]::FromFile((Resolve-Path $path))
    try {
        if ($img.Width -ne $width -or $img.Height -ne $height) {
            throw "$label is $($img.Width)x$($img.Height), expected ${width}x${height}"
        }
        if ($img.PixelFormat.ToString() -notmatch "24bpp") {
            throw "$label pixel format $($img.PixelFormat) must be 24-bit RGB (no alpha) for Play"
        }
        Write-Host "OK $label $($img.Width)x$($img.Height)"
    } finally {
        $img.Dispose()
    }
}

function Assert-Screenshot($path, $label) {
    if (-not (Test-Path $path)) { throw "$label missing: $path" }
    $img = [System.Drawing.Image]::FromFile((Resolve-Path $path))
    try {
        $min = [Math]::Min($img.Width, $img.Height)
        $max = [Math]::Max($img.Width, $img.Height)
        if ($min -lt 320 -or $max -gt 3840) {
            throw "$label $($img.Width)x$($img.Height) is outside Play's 320-3840px range"
        }
        $longOverShort = $max / [double]$min
        if ($longOverShort -lt 1.5 -or $longOverShort -gt 2.0) {
            Write-Warning "$label aspect $longOverShort is outside the usual 16:9-9:16 store range"
        }
        if ($img.PixelFormat.ToString() -notmatch "24bpp") {
            throw "$label pixel format $($img.PixelFormat) must be 24-bit RGB (no alpha) for Play"
        }
        Write-Host "OK $label $($img.Width)x$($img.Height)"
    } finally {
        $img.Dispose()
    }
}

function Assert-Copy($path, $max, $label) {
    if (-not (Test-Path $path)) { throw "$label missing: $path" }
    $text = (Get-Content $path -Raw).Trim()
    if ($text.Length -eq 0) { throw "$label is empty" }
    if ($text.Length -gt $max) { throw "$label is $($text.Length) chars, max $max" }
    Write-Host "OK $label $($text.Length)/$max"
}

Assert-Png "fastlane/metadata/android/en-US/images/icon.png" 512 512 "Hi-res icon"
Assert-Png "fastlane/metadata/android/en-US/images/featureGraphic.png" 1024 500 "Feature graphic"
Get-ChildItem "fastlane/metadata/android/en-US/images/phoneScreenshots/*.png" | ForEach-Object {
    Assert-Screenshot $_.FullName "Phone $($_.Name)"
}
Get-ChildItem "fastlane/metadata/android/en-US/images/tenInchScreenshots/*.png" | ForEach-Object {
    Assert-Screenshot $_.FullName "Tablet $($_.Name)"
}
Assert-Copy "fastlane/metadata/android/en-US/title.txt" 30 "Title"
Assert-Copy "fastlane/metadata/android/en-US/short_description.txt" 80 "Short description"
Assert-Copy "fastlane/metadata/android/en-US/full_description.txt" 4000 "Full description"
Assert-Copy "fastlane/metadata/android/en-US/changelogs/1.txt" 500 "Changelog 1"

if (-not (Test-Path "PRIVACY.md")) { throw "PRIVACY.md missing" }
Write-Host "OK privacy policy file"
Write-Host "Listing files are Play-sized. Recapture screenshots from the signed build before production."
