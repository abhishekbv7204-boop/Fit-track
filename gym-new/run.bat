@echo off
echo Compiling Java files...
if not exist "backend\bin" (
    mkdir "backend\bin"
)
dir /s /b backend\src\*.java > sources.txt
javac -cp "lib\mysql-connector-java-8.0.30.jar" -d backend\bin @sources.txt
del sources.txt
if %ERRORLEVEL% EQU 0 (
    echo Starting Gym Management System server on http://localhost:5000...
    java -cp "backend\bin;lib\mysql-connector-java-8.0.30.jar" com.gym.Main
) else (
    echo Compilation failed.
    pause
)
