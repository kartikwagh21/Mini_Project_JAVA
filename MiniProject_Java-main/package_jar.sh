#!/bin/bash
# Script to build executable standalone JAR file

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BIN_DIR="$PROJECT_ROOT/bin"
JAR_NAME="CarePulse-Hospital-System.jar"

echo "=========================================="
echo " Packaging Standalone Executable JAR      "
echo "=========================================="

"$PROJECT_ROOT/compile.sh"

# Create Manifest
echo "Main-Class: hospital.Main" > "$PROJECT_ROOT/manifest.txt"

# Build JAR
jar cfm "$PROJECT_ROOT/$JAR_NAME" "$PROJECT_ROOT/manifest.txt" -C "$BIN_DIR" .
rm "$PROJECT_ROOT/manifest.txt"

echo "✓ Successfully created executable: $PROJECT_ROOT/$JAR_NAME"
echo "To run anywhere: java -jar $JAR_NAME"
