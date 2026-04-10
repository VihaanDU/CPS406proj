@echo off
echo =============================================
echo  CSA - Co-op Support Application
echo =============================================

java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java not found. Install JDK 21+.
    exit /b 1
)

if not exist lib\sqlite-jdbc.jar (
    echo ERROR: lib\sqlite-jdbc.jar not found.
    echo Download from: https://github.com/xerial/sqlite-jdbc/releases
    echo Rename it to sqlite-jdbc.jar and put it in the lib\ folder.
    exit /b 1
)

echo Compiling...
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -cp lib\sqlite-jdbc.jar -d out @sources.txt
del sources.txt
echo Done. Launching...

java -cp "out;lib\sqlite-jdbc.jar" csa.App
