#!/usr/bin/env bash
set -e

echo "============================================="
echo " CSA - Co-op Support Application"
echo "============================================="

if ! command -v java &>/dev/null; then
    echo "ERROR: Java not found. Please install JDK 21+."
    exit 1
fi

if [ ! -f lib/sqlite-jdbc.jar ]; then
    echo "ERROR: lib/sqlite-jdbc.jar not found."
    echo "Download from: https://github.com/xerial/sqlite-jdbc/releases"
    echo "Rename the jar to sqlite-jdbc.jar and put it in the lib/ folder."
    exit 1
fi

echo "Compiling..."
mkdir -p out
find src -name "*.java" > sources.txt
javac -cp lib/sqlite-jdbc.jar -d out @sources.txt
rm sources.txt
echo "Done. Launching..."

java -cp "out:lib/sqlite-jdbc.jar" csa.App
