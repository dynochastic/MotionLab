# Diagnostic and Push Script
Write-Host "=== MotionLab GitHub Push Diagnostic ===" -ForegroundColor Cyan
Write-Host ""

# Check if we're in a git repo
Write-Host "1. Checking Git Repository..." -ForegroundColor Yellow
if (Test-Path .git) {
    Write-Host "   ✓ Git repository found" -ForegroundColor Green
} else {
    Write-Host "   ✗ Not a git repository!" -ForegroundColor Red
    exit
}

# Check remote
Write-Host ""
Write-Host "2. Checking Remote Repository..." -ForegroundColor Yellow
$remote = git config --get remote.origin.url
if ($remote) {
    Write-Host "   ✓ Remote: $remote" -ForegroundColor Green
} else {
    Write-Host "   ✗ No remote configured!" -ForegroundColor Red
    exit
}

# Check current branch
Write-Host ""
Write-Host "3. Checking Current Branch..." -ForegroundColor Yellow
$branch = git branch --show-current
if ($branch) {
    Write-Host "   ✓ Current branch: $branch" -ForegroundColor Green
} else {
    Write-Host "   ⚠ No commits yet - initializing..." -ForegroundColor Yellow
    $branch = "main"
}

# Check for uncommitted changes
Write-Host ""
Write-Host "4. Checking for Uncommitted Changes..." -ForegroundColor Yellow
$status = git status --porcelain
if ($status) {
    Write-Host "   ⚠ Found uncommitted changes:" -ForegroundColor Yellow
    $status | Select-Object -First 10 | ForEach-Object { Write-Host "      $_" -ForegroundColor Gray }
    Write-Host ""
    Write-Host "   Adding all files..." -ForegroundColor Yellow
    git add .
    Write-Host "   Committing changes..." -ForegroundColor Yellow
    git commit -m "Update MotionLab project - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
    Write-Host "   ✓ Changes committed" -ForegroundColor Green
} else {
    Write-Host "   ✓ Working directory clean" -ForegroundColor Green
}

# Check commits ahead
Write-Host ""
Write-Host "5. Checking Commits to Push..." -ForegroundColor Yellow
try {
    $commitsAhead = git rev-list --count origin/$branch..HEAD 2>$null
    if ($LASTEXITCODE -eq 0) {
        if ($commitsAhead -gt 0) {
            Write-Host "   ✓ Found $commitsAhead commit(s) to push" -ForegroundColor Green
        } else {
            Write-Host "   ⚠ No commits ahead of remote" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ⚠ Remote branch might not exist yet" -ForegroundColor Yellow
        $commitsAhead = 1
    }
} catch {
    Write-Host "   ⚠ Could not check remote status" -ForegroundColor Yellow
    $commitsAhead = 1
}

# Check authentication
Write-Host ""
Write-Host "6. Testing Connection to GitHub..." -ForegroundColor Yellow
try {
    git ls-remote origin HEAD | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Connection successful" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Connection failed - may need authentication" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ Connection failed - may need authentication" -ForegroundColor Red
}

# Try to push
Write-Host ""
Write-Host "7. Attempting to Push..." -ForegroundColor Yellow
Write-Host "   Command: git push -u origin $branch" -ForegroundColor Gray
Write-Host ""

$pushOutput = git push -u origin $branch 2>&1
$pushExitCode = $LASTEXITCODE

if ($pushExitCode -eq 0) {
    Write-Host ""
    Write-Host "   ✓✓✓ SUCCESS! Push completed! ✓✓✓" -ForegroundColor Green
    Write-Host ""
    Write-Host "   Your code is now on GitHub at:" -ForegroundColor Cyan
    Write-Host "   $remote" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "   ✗✗✗ PUSH FAILED ✗✗✗" -ForegroundColor Red
    Write-Host ""
    Write-Host "   Error output:" -ForegroundColor Yellow
    $pushOutput | ForEach-Object { Write-Host "   $_" -ForegroundColor Red }
    Write-Host ""
    Write-Host "   Common issues:" -ForegroundColor Yellow
    Write-Host "   1. Authentication required - you may need to:" -ForegroundColor White
    Write-Host "      - Set up a Personal Access Token (PAT)" -ForegroundColor Gray
    Write-Host "      - Or use GitHub Desktop/VS Code Git integration" -ForegroundColor Gray
    Write-Host "   2. Branch name mismatch - try: git push -u origin main" -ForegroundColor White
    Write-Host "   3. Remote is ahead - pull first: git pull origin $branch --allow-unrelated-histories" -ForegroundColor White
}

Write-Host ""
Write-Host "=== Done ===" -ForegroundColor Cyan
