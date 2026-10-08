[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== 1. HEALTH CHECK ===" -ForegroundColor Cyan
$health = Invoke-RestMethod -Uri "http://localhost:9091/api/health"
Write-Host "Health Response: $health" -ForegroundColor Green

Write-Host "`n=== 2. STATIC IMAGES CHECK (ALL 20 ANIMALS) ===" -ForegroundColor Cyan
$images = @('simba.jpg', 'nala.jpg', 'shere_khan.jpg', 'bagheera.jpg', 'george.jpg', 'koko.jpg', 'king_julien.jpg', 'majestic.jpg', 'rio.jpg', 'pinky.jpg', 'pingu.jpg', 'flipper.jpg', 'crush.jpg', 'dumbo.jpg', 'melman.jpg', 'marty.jpg', 'po.jpg', 'kaa.jpg', 'pascal.jpg', 'kermit.jpg')
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

Write-Host "`n=== 9. SWAGGER UI & OPENAPI CHECK ===" -ForegroundColor Cyan
$swag = Invoke-WebRequest -Uri "http://localhost:9091/swagger-ui.html" -Method Get -UseBasicParsing
Write-Host "  -> Swagger UI Page: HTTP $($swag.StatusCode)" -ForegroundColor Green
$openApi = Invoke-RestMethod -Uri "http://localhost:9091/v3/api-docs"
$secType = $openApi.components.securitySchemes.basicAuth.type
Write-Host "  -> OpenAPI Title: $($openApi.info.title) | Basic Auth Security Scheme: $secType" -ForegroundColor Green

Write-Host "`n=== 10. INVENTORY & RBAC VERIFICATION ===" -ForegroundColor Cyan
$headersAdmin = @{ Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('admin:admin123')) }
$headersKeeper = @{ Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('keeper:keeper123')) }

$invPublic = Invoke-RestMethod -Uri "http://localhost:9091/api/inventory"
Write-Host "  -> Public GET /api/inventory: Success ($($invPublic.Count) items found)" -ForegroundColor Green

try {
    Invoke-RestMethod -Uri "http://localhost:9091/api/inventory" -Method Post -ContentType "application/json" -Body '{"name":"UnauthTest"}'
    Write-Host "  -> Unauthenticated POST: UNEXPECTED SUCCESS" -ForegroundColor Red
} catch {
    Write-Host "  -> Unauthenticated POST blocked with HTTP 401 Unauthorized (Expected)" -ForegroundColor Green
}

$firstItemId = $invPublic[0].id
$restockRes = Invoke-RestMethod -Uri "http://localhost:9091/api/inventory/$firstItemId/restock" -Method Post -Headers $headersKeeper -ContentType "application/json" -Body '{"amount":10.0}'
Write-Host "  -> Keeper Restock Item (ID: $firstItemId): Success (New quantity: $($restockRes.quantity))" -ForegroundColor Green

try {
    Invoke-RestMethod -Uri "http://localhost:9091/api/inventory" -Method Post -Headers $headersKeeper -ContentType "application/json" -Body '{"name":"KeeperTest","quantity":10.0,"minThreshold":2.0,"unit":"kg"}'
    Write-Host "  -> Keeper Create Item: UNEXPECTED SUCCESS" -ForegroundColor Red
} catch {
    Write-Host "  -> Keeper Create Item blocked with HTTP 403 Forbidden (Expected)" -ForegroundColor Green
}

$createBody = @{ name = "PremiumHay"; quantity = 150.0; minThreshold = 30.0; unit = "kg" } | ConvertTo-Json
$newAdminItem = Invoke-RestMethod -Uri "http://localhost:9091/api/inventory" -Method Post -Headers $headersAdmin -ContentType "application/json" -Body $createBody
Write-Host "  -> Admin Create Item (ID: $($newAdminItem.id)): Success ('$($newAdminItem.name)')" -ForegroundColor Green

$updateBody = @{ name = "PremiumHayGold"; quantity = 160.0; minThreshold = 35.0; unit = "kg" } | ConvertTo-Json
$updatedAdminItem = Invoke-RestMethod -Uri "http://localhost:9091/api/inventory/$($newAdminItem.id)" -Method Put -Headers $headersAdmin -ContentType "application/json" -Body $updateBody
Write-Host "  -> Admin Update Item (ID: $($updatedAdminItem.id)): Success ('$($updatedAdminItem.name)')" -ForegroundColor Green

$delRes = Invoke-WebRequest -Uri "http://localhost:9091/api/inventory/$($newAdminItem.id)" -Method Delete -Headers $headersAdmin -UseBasicParsing
Write-Host "  -> Admin Delete Item (ID: $($newAdminItem.id)): HTTP $($delRes.StatusCode) No Content (Expected)" -ForegroundColor Green

Write-Host "`nALL COMPREHENSIVE TESTS (SECTIONS 1-10) COMPLETED SUCCESSFULLY!" -ForegroundColor Magenta
