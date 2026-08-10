# PowerShell script to help push MotionLab to GitHub
# Run this script: .\push-to-github.ps1

Write-Host "=== MotionLab GitHub Push Helper ===" -ForegroundColor Cyan
Write-Host ""

# Check if we're in a git repository
try {
    $gitCheck = git rev-parse --git-dir 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Not a git repository. Initializing..." -ForegroundColor Yellow
        git init
        Write-Host "✅ Git repository initialized!" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Error checking git status" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Step 1: Checking current git status..." -ForegroundColor Cyan
git status --short | Select-Object -First 20
Write-Host ""

Write-Host "Step 2: Files that will be added (respecting .gitignore):" -ForegroundColor Cyan
Write-Host ""

# Show what would be added
$untracked = git ls-files --others --exclude-standard
$modified = git diff --name-only
$staged = git diff --cached --name-only

if ($untracked.Count -gt 0) {
    Write-Host "📁 New files to add: $($untracked.Count)" -ForegroundColor Yellow
    $untracked | Select-Object -First 10 | ForEach-Object { Write-Host "  + $_" -ForegroundColor Gray }
    if ($untracked.Count -gt 10) {
        Write-Host "  ... and $($untracked.Count - 10) more" -ForegroundColor Gray
    }
}

if ($modified.Count -gt 0) {
    Write-Host "📝 Modified files: $($modified.Count)" -ForegroundColor Yellow
    $modified | Select-Object -First 10 | ForEach-Object { Write-Host "  ~ $_" -ForegroundColor Gray }
}

if ($staged.Count -gt 0) {
    Write-Host "✅ Already staged: $($staged.Count)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 3: Ready to add all files? (Y/N)" -ForegroundColor Cyan
$response = Read-Host

if ($response -eq 'Y' -or $response -eq 'y') {
    Write-Host ""
    Write-Host "Adding all files..." -ForegroundColor Cyan
    git add .
    Write-Host "✅ Files added!" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "Step 4: Enter commit message (or press Enter for default):" -ForegroundColor Cyan
    $commitMsg = Read-Host
    if ([string]::IsNullOrWhiteSpace($commitMsg)) {
        $commitMsg = "Update MotionLab project - latest changes"
    }
    
    Write-Host ""
    Write-Host "Committing changes..." -ForegroundColor Cyan
    git commit -m $commitMsg
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Changes committed!" -ForegroundColor Green
        
        # Check for remote
        Write-Host ""
        Write-Host "Step 5: Checking remote repository..." -ForegroundColor Cyan
        $remote = git config --get remote.origin.url
        
        if ($remote) {
            Write-Host "✅ Remote found: $remote" -ForegroundColor Green
            Write-Host ""
            Write-Host "Ready to push? (Y/N)" -ForegroundColor Cyan
            $pushResponse = Read-Host
            
            if ($pushResponse -eq 'Y' -or $pushResponse -eq 'y') {
                Write-Host ""
                Write-Host "Pushing to GitHub..." -ForegroundColor Cyan
                
                $branch = git branch --show-current
                if (!$branch) {
                    $branch = "main"
                }
                
                git push -u origin $branch
                
                if ($LASTEXITCODE -eq 0) {
                    Write-Host ""
                    Write-Host "🎉 Successfully pushed to GitHub!" -ForegroundColor Green
                } else {
                    Write-Host ""
                    Write-Host "❌ Push failed. You may need to:" -ForegroundColor Red
                    Write-Host "   1. Set remote: git remote add origin <your-repo-url>" -ForegroundColor Yellow
                    Write-Host "   2. Or pull first if repo is outdated: git pull origin $branch --allow-unrelated-histories" -ForegroundColor Yellow
                }
            }
        } else {
            Write-Host ""
            Write-Host "⚠️  No remote repository configured." -ForegroundColor Yellow
            Write-Host "To add a remote repository, run:" -ForegroundColor Yellow
            Write-Host "   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git" -ForegroundColor Cyan
        }
    } else {
        Write-Host ""
        Write-Host "⚠️  Commit failed. You may have no changes to commit." -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "Cancelled. No files were added." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
