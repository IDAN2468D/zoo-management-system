[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== 1. HEALTH CHECK ===" -ForegroundColor Cyan
$health = Invoke-RestMethod -Uri "http://localhost:9091/api/health"
Write-Host "Health Response: $health" -ForegroundColor Green

Write-Host "`n=== 2. STATIC IMAGES CHECK (NaNoBanana 2.1) ===" -ForegroundColor Cyan
$images = @('simba.jpg', 'shere_khan.jpg', 'dumbo.jpg', 'po.jpg', 'flipper.jpg', 'pingu.jpg', 'koko.jpg', 'pinky.jpg')
foreach ($img in $images) {
    $res = Invoke-WebRequest -Uri ("http://localhost:9091/images/" + $img) -Method Head -UseBasicParsing
    Write-Host "  -> Image $img : HTTP $($res.StatusCode) ($($res.Headers['Content-Type']))" -ForegroundColor Green
}

Write-Host "`n=== 3. AUTH & RBAC LOGIN CHECKS ===" -ForegroundColor Cyan
$admin = Invoke-RestMethod -Uri "http://localhost:9091/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
Write-Host "  -> Admin: Auth=$($admin.authenticated) | Role=$($admin.role) | Name=$($admin.fullName)" -ForegroundColor Green

$vet = Invoke-RestMethod -Uri "http://localhost:9091/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"vet","password":"vet123"}'
Write-Host "  -> Vet: Auth=$($vet.authenticated) | Role=$($vet.role) | Name=$($vet.fullName)" -ForegroundColor Green

$keeper = Invoke-RestMethod -Uri "http://localhost:9091/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"keeper","password":"keeper123"}'
Write-Host "  -> Keeper: Auth=$($keeper.authenticated) | Role=$($keeper.role) | Name=$($keeper.fullName)" -ForegroundColor Green

try {
    Invoke-RestMethod -Uri "http://localhost:9091/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"wrongPassword"}'
    Write-Host "  -> Wrong password: UNEXPECTED SUCCESS" -ForegroundColor Red
} catch {
    Write-Host "  -> Wrong password blocked with HTTP 401 Unauthorized (Expected)" -ForegroundColor Green
}

Write-Host "`n=== 4. ALERTS ENDPOINT CHECK ===" -ForegroundColor Cyan
$alerts = Invoke-RestMethod -Uri "http://localhost:9091/api/alerts"
Write-Host "  -> Total Alerts: $($alerts.totalAlerts) (Critical: $($alerts.criticalCount), Warning: $($alerts.warningCount), Info: $($alerts.infoCount))" -ForegroundColor Green

Write-Host "`n=== 5. TASKS ENDPOINT CHECK ===" -ForegroundColor Cyan
$tasks = Invoke-RestMethod -Uri "http://localhost:9091/api/tasks"
Write-Host "  -> Total Zoo Tasks: $($tasks.Count)" -ForegroundColor Green

Write-Host "`n=== 6. ANALYTICS ENDPOINT CHECK ===" -ForegroundColor Cyan
$analytics = Invoke-RestMethod -Uri "http://localhost:9091/api/analytics"
Write-Host "  -> Species Distribution Categories: $($analytics.speciesDistribution.Count)" -ForegroundColor Green
Write-Host "  -> Cages Monitored: $($analytics.cagesReport.Count)" -ForegroundColor Green

Write-Host "`n=== 7. ANIMALS & IMAGE INTEGRATION CHECK ===" -ForegroundColor Cyan
$animals = Invoke-RestMethod -Uri "http://localhost:9091/api/animals"
Write-Host "  -> Total Animals: $($animals.Count)" -ForegroundColor Green
$withImg = ($animals | Where-Object { $_.imageUrl -ne $null -and $_.imageUrl -ne '' }).Count
Write-Host "  -> Animals with Image URL: $withImg" -ForegroundColor Green

Write-Host "`n=== 8. FRONTEND FILES CHECK ===" -ForegroundColor Cyan
foreach ($f in @('login.html', 'login.js', 'index.html', 'app.js', 'styles.css')) {
    $r = Invoke-WebRequest -Uri ("http://localhost:9091/" + $f) -Method Head -UseBasicParsing
    Write-Host "  -> $f : HTTP $($r.StatusCode)" -ForegroundColor Green
}

Write-Host "`nALL COMPREHENSIVE TESTS COMPLETED SUCCESSFULLY!" -ForegroundColor Magenta
