@echo off
echo Compiling Java files...
if not exist "gym-new\backend\bin" (
    mkdir "gym-new\backend\bin"
)
dir /s /b gym-new\backend\src\*.java > sources.txt
javac -cp "gym-new\lib\mysql-connector-java-8.0.30.jar" -d gym-new\backend\bin @sources.txt
del sources.txt
if %ERRORLEVEL% EQU 0 (
    echo Starting Gym Management System server on http://localhost:5000...
    java -cp "gym-new\backend\bin;gym-new\lib\mysql-connector-java-8.0.30.jar" com.gym.Main
) else (
    echo Compilation failed.
    pause
)
