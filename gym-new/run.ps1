Write-Host "Compiling Java files..." -ForegroundColor Green
if (!(Test-Path "backend/bin")) {
    New-Item -ItemType Directory -Force -Path "backend/bin" | Out-Null
}
javac -cp "lib/mysql-connector-java-8.0.30.jar" -d backend/bin (Get-ChildItem -Recurse backend/src -Filter *.java).FullName

if ($LASTEXITCODE -eq 0) {
    Write-Host "Starting Gym Management System server on http://localhost:5000..." -ForegroundColor Green
    java -cp "backend/bin;lib/mysql-connector-java-8.0.30.jar" com.gym.Main
} else {
    Write-Host "Compilation failed." -ForegroundColor Red
}
