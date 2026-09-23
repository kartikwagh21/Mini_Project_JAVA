#!/bin/bash
# Launch Script for CarePulse Hospital GUI

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BIN_DIR="$PROJECT_ROOT/bin"

if [ ! -d "$BIN_DIR" ] || [ ! -f "$BIN_DIR/hospital/Main.class" ]; then
    echo "Compiling sources first..."
    "$PROJECT_ROOT/compile.sh"
fi

echo "=========================================="
echo " Launching CarePulse Hospital System GUI  "
echo "=========================================="

java -cp "$BIN_DIR" hospital.Main
