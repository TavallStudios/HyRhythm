#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODULE_DIR="${ROOT_DIR}/hytale-server-patch"
DECOMPILER_JAR="/srv/mcp-servers/jdtls/wrapped.com.jetbrains.intellij.java.java-decompiler-engine_253.29346.240.jar"
SERVER_VERSION="${HYTALE_SERVER_VERSION:-2026.02.19-1a311a592}"
SERVER_JAR="${HYTALE_SERVER_JAR:-${HOME}/.m2/repository/com/hypixel/hytale/Server/${SERVER_VERSION}/Server-${SERVER_VERSION}.jar}"
INSTALL_SOURCES=0

while [ "$#" -gt 0 ]; do
    case "$1" in
        --install-sources)
            INSTALL_SOURCES=1
            shift
            ;;
        --jar)
            SERVER_JAR="$2"
            shift 2
            ;;
        *)
            echo "Unknown argument: $1" >&2
            exit 1
            ;;
    esac
done

if [ ! -f "${SERVER_JAR}" ]; then
    echo "Missing Hytale server jar at ${SERVER_JAR}" >&2
    exit 1
fi

if [ ! -f "${DECOMPILER_JAR}" ]; then
    echo "Missing decompiler jar at ${DECOMPILER_JAR}" >&2
    exit 1
fi

TMP_DIR="$(mktemp -d)"
cleanup() {
    rm -rf "${TMP_DIR}"
}
trap cleanup EXIT

echo "Extracting com/hypixel classes from ${SERVER_JAR}"
(
    cd "${TMP_DIR}"
    jar xf "${SERVER_JAR}" com/hypixel
)

rm -rf "${MODULE_DIR}/decompiled-src"
mkdir -p "${MODULE_DIR}/decompiled-src"

echo "Decompiling into ${MODULE_DIR}/decompiled-src"
java -cp "${DECOMPILER_JAR}" org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler \
    -din=1 \
    -rsy=1 \
    -dgs=1 \
    -lit=1 \
    "${TMP_DIR}" \
    "${MODULE_DIR}/decompiled-src"

if [ "${INSTALL_SOURCES}" -eq 1 ]; then
    mkdir -p "${MODULE_DIR}/target"
    SOURCES_JAR="${MODULE_DIR}/target/Server-${SERVER_VERSION}-sources.jar"
    rm -f "${SOURCES_JAR}"
    jar cf "${SOURCES_JAR}" -C "${MODULE_DIR}/decompiled-src" .

    echo "Installing ${SOURCES_JAR} into the local Maven cache"
    mvn -q install:install-file \
        -Dfile="${SOURCES_JAR}" \
        -DgroupId=com.hypixel.hytale \
        -DartifactId=Server \
        -Dversion="${SERVER_VERSION}" \
        -Dpackaging=jar \
        -Dclassifier=sources \
        -DgeneratePom=false
fi

echo "Reference sources are ready at ${MODULE_DIR}/decompiled-src"
