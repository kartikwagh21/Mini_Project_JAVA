#!/bin/bash
# Compilation Script for Hospital Patient Management System

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BIN_DIR="$PROJECT_ROOT/bin"
SRC_DIR="$PROJECT_ROOT/hospital"

echo "=========================================="
echo " Building Hospital Patient Management App "
echo "=========================================="

mkdir -p "$BIN_DIR"

# Find all Java source files and compile
find "$SRC_DIR" -name "*.java" > "$PROJECT_ROOT/sources.txt"
javac -d "$BIN_DIR" @"$PROJECT_ROOT/sources.txt"
rm "$PROJECT_ROOT/sources.txt"

echo "✓ Compilation Successful! Class files generated in bin/"
