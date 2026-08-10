Write-Host "=== Attempting to Push to GitHub ===" -ForegroundColor Cyan
Write-Host ""

# Check if we have commits to push
Write-Host "Checking commits to push..." -ForegroundColor Yellow
$commitsAhead = git rev-list --count origin/main..HEAD 2>$null

if ($LASTEXITCODE -eq 0 -and $commitsAhead -gt 0) {
    Write-Host "✓ Found $commitsAhead commit(s) ready to push" -ForegroundColor Green
} else {
    Write-Host "⚠ Checking remote connection..." -ForegroundColor Yellow
    git fetch origin 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Remote connection successful" -ForegroundColor Green
        $commitsAhead = git rev-list --count origin/main..HEAD 2>$null
        Write-Host "  Commits to push: $commitsAhead" -ForegroundColor White
    } else {
        Write-Host "✗ Cannot connect to remote - authentication required" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Attempting push..." -ForegroundColor Yellow
Write-Host "Command: git push -u origin main" -ForegroundColor Gray
Write-Host ""

$pushOutput = git push -u origin main 2>&1
$pushExitCode = $LASTEXITCODE

Write-Host "Push output:" -ForegroundColor Cyan
$pushOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }

if ($pushExitCode -eq 0) {
    Write-Host ""
    Write-Host "✓✓✓ SUCCESS! Your code has been pushed to GitHub! ✓✓✓" -ForegroundColor Green
    Write-Host ""
    Write-Host "View your repository at:" -ForegroundColor Cyan
    Write-Host "https://github.com/dyn stochastic/MotionLab" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "✗✗✗ PUSH FAILED ✗✗✗" -ForegroundColor Red
    Write-Host ""
    
    if ($pushOutput -match "authentication|credential|permission") {
        Write-Host "Authentication is required. You need to:" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "OPTION 1: Use GitHub Desktop (Easiest)" -ForegroundColor Cyan
        Write-Host "  1. Download: https://desktop.github.com/" -ForegroundColor White
        Write-Host "  2. Open GitHub Desktop" -ForegroundColor White
        Write-Host "  3. File → Add Local Repository" -ForegroundColor White
        Write-Host "  4. Select: c:\Users\Christian\AndroidStudioProjects\MotionLab" -ForegroundColor White
        Write-Host "  5. Click 'Push origin' button" -ForegroundColor White
        Write-Host ""
        Write-Host "OPTION 2: Create Personal Access Token" -ForegroundColor Cyan
        Write-Host "  1. Go to: https://github.com/settings/tokens" -ForegroundColor White
        Write-Host "  2. Generate new token (classic)" -ForegroundColor White
        Write-Host "  3. Check 'repo' scope" -ForegroundColor White
        Write-Host "  4. Copy the token" -ForegroundColor White
        Write-Host "  5. Run: git push -u origin main" -ForegroundColor White
        Write-Host "  6. Use token as password when prompted" -ForegroundColor White
    } else {
        Write-Host "Error details:" -ForegroundColor Yellow
        $pushOutput | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    }
}

Write-Host ""
Write-Host "=== Done ===" -ForegroundColor Cyan
