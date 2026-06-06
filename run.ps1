Write-Host "Compiling Java files..." -ForegroundColor Green
if (!(Test-Path "gym-new/backend/bin")) {
    New-Item -ItemType Directory -Force -Path "gym-new/backend/bin" | Out-Null
}
javac -cp "gym-new/lib/mysql-connector-java-8.0.30.jar" -d gym-new/backend/bin (Get-ChildItem -Recurse gym-new/backend/src -Filter *.java).FullName

if ($LASTEXITCODE -eq 0) {
    Write-Host "Starting Gym Management System server on http://localhost:5000..." -ForegroundColor Green
    java -cp "gym-new/backend/bin;gym-new/lib/mysql-connector-java-8.0.30.jar" com.gym.Main
} else {
    Write-Host "Compilation failed." -ForegroundColor Red
}
