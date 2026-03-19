# Setup and Run Skibidi Music Backend

Write-Host "--- Skibidi Music Backend Setup ---" -ForegroundColor Cyan

# 1. Check for Python
if (!(Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Error "Python not found! Please install Python from python.org"
    exit
}

# 2. Install dependencies
Write-Host "Installing dependencies..." -ForegroundColor Yellow
python -m pip install fastapi uvicorn yt-dlp requests

# 3. Find Local IP
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -notlike "*Loopback*" -and $_.InterfaceAlias -notlike "*Virtual*" } | Select-Object -First 1).IPAddress
Write-Host "`nYour Local IP Address is: $ip" -ForegroundColor Green
Write-Host "IMPORTANT: Update 'BACKEND_URL' in NetworkModule.kt to: http://$ip:8080" -ForegroundColor White

# 4. Start Server
Write-Host "`nStarting Server..." -ForegroundColor Cyan
python extractor_server.py
