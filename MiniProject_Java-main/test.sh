#!/bin/bash
# Test Execution Script for Hospital System

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BIN_DIR="$PROJECT_ROOT/bin"

"$PROJECT_ROOT/compile.sh"

echo "=========================================="
echo " Running Automated Unit & DSA Tests       "
echo "=========================================="

java -enableassertions -cp "$BIN_DIR" hospital.HospitalSystemTest
