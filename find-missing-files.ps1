Write-Host "=== Checking for Missing Files ===" -ForegroundColor Cyan
Write-Host ""

# Check for untracked files
Write-Host "Untracked files:" -ForegroundColor Yellow
$untracked = git ls-files --others --exclude-standard
$untrackedCount = ($untracked | Measure-Object).Count
Write-Host "Total untracked files: $untrackedCount" -ForegroundColor White

# Check images specifically
Write-Host ""
Write-Host "Untracked image files:" -ForegroundColor Yellow
$images = $untracked | Where-Object { $_ -match '\.(png|jpg|jpeg|PNG|JPG|JPEG|webp|gif)$' }
$images | Select-Object -First 30 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
if ($images.Count -gt 30) {
    Write-Host "  ... and $($images.Count - 30) more images" -ForegroundColor Gray
}

# Check videos
Write-Host ""
Write-Host "Untracked video files:" -ForegroundColor Yellow
$videos = $untracked | Where-Object { $_ -match '\.(mp4|mov|avi)$' }
$videos | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

# Check JSON files
Write-Host ""
Write-Host "Untracked JSON files:" -ForegroundColor Yellow
$json = $untracked | Where-Object { $_ -match '\.json$' }
$json | Select-Object -First 20 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

# Check what's in res and assets
Write-Host ""
Write-Host "Checking app/src/main/res and assets..." -ForegroundColor Yellow
$resFiles = Get-ChildItem -Path "app\src\main\res" -Recurse -File | Select-Object -ExpandProperty FullName | ForEach-Object { $_.Replace((Get-Location).Path + '\', '').Replace('\', '/') }
$assetFiles = Get-ChildItem -Path "app\src\main\assets" -Recurse -File | Select-Object -ExpandProperty FullName | ForEach-Object { $_.Replace((Get-Location).Path + '\', '').Replace('\', '/') }

$tracked = git ls-files
$missingRes = $resFiles | Where-Object { $_ -notin $tracked }
$missingAssets = $assetFiles | Where-Object { $_ -notin $tracked }

Write-Host ""
Write-Host "Missing from res/: $($missingRes.Count)" -ForegroundColor $(if ($missingRes.Count -gt 0) { "Red" } else { "Green" })
$missingRes | Select-Object -First 20 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

Write-Host ""
Write-Host "Missing from assets/: $($missingAssets.Count)" -ForegroundColor $(if ($missingAssets.Count -gt 0) { "Red" } else { "Green" })
$missingAssets | Select-Object -First 20 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

Write-Host ""
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host "Total untracked: $untrackedCount" -ForegroundColor White
Write-Host "Untracked images: $($images.Count)" -ForegroundColor White
Write-Host "Untracked videos: $($videos.Count)" -ForegroundColor White
Write-Host "Untracked JSON: $($json.Count)" -ForegroundColor White
Write-Host "Missing from res/: $($missingRes.Count)" -ForegroundColor White
Write-Host "Missing from assets/: $($missingAssets.Count)" -ForegroundColor White

if ($untrackedCount -gt 0) {
    Write-Host ""
    Write-Host "Ready to add all missing files? (Y/N)" -ForegroundColor Cyan
}
