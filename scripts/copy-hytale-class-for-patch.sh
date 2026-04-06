#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <fully.qualified.ClassName>" >&2
    exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODULE_DIR="${ROOT_DIR}/hytale-server-patch"
CLASS_NAME="$1"
RELATIVE_PATH="${CLASS_NAME//./\/}.java"
SOURCE_FILE="${MODULE_DIR}/decompiled-src/${RELATIVE_PATH}"
TARGET_FILE="${MODULE_DIR}/src/main/java/${RELATIVE_PATH}"

if [ ! -f "${SOURCE_FILE}" ]; then
    echo "Missing decompiled source at ${SOURCE_FILE}" >&2
    echo "Run scripts/decompile-hytale-server.sh first." >&2
    exit 1
fi

mkdir -p "$(dirname "${TARGET_FILE}")"
cp "${SOURCE_FILE}" "${TARGET_FILE}"
echo "Copied ${SOURCE_FILE} -> ${TARGET_FILE}"
