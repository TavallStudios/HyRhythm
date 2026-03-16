#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVER_HELPER="${ROOT_DIR}/scripts/hytale-local-server.sh"
HYTALE_ROOT="${HYRHYTHM_HYTALE_ROOT:-/srv/hytale}"
TARGET_MOD="${HYTALE_ROOT}/Server/mods/HyRhythm.jar"
KEEP_RUNNING=false
SKIP_BUILD=false

usage() {
    cat <<'EOF'
Usage: scripts/hyrhythm-local-smoke.sh [--skip-build] [--keep-running]

Builds and deploys HyRhythm to the local Hytale server, runs the command-driven
playable loop smoke test, and stops the tmux-backed server unless --keep-running
is supplied.
EOF
}

while [ "$#" -gt 0 ]; do
    case "$1" in
        --keep-running)
            KEEP_RUNNING=true
            ;;
        --skip-build)
            SKIP_BUILD=true
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
    shift
done

if ! command -v tmux >/dev/null 2>&1; then
    echo "tmux is required for the local smoke test." >&2
    exit 1
fi

if [ ! -x "$SERVER_HELPER" ]; then
    echo "Server helper script is missing or not executable: $SERVER_HELPER" >&2
    exit 1
fi

if [ ! -d "$(dirname "$TARGET_MOD")" ]; then
    echo "Target mods directory does not exist: $(dirname "$TARGET_MOD")" >&2
    exit 1
fi

cleanup() {
    local exit_code="$1"
    if [ "$KEEP_RUNNING" = false ]; then
        "$SERVER_HELPER" stop >/dev/null 2>&1 || true
    fi
    exit "$exit_code"
}

trap 'cleanup $?' EXIT

if [ "$SKIP_BUILD" = false ]; then
    echo "[smoke] Building HyRhythm package"
    (cd "$ROOT_DIR" && mvn -q -DskipTests package)
fi

if [ ! -f "${ROOT_DIR}/target/HyRhythm.jar" ]; then
    echo "Missing built jar at ${ROOT_DIR}/target/HyRhythm.jar" >&2
    exit 1
fi

echo "[smoke] Deploying jar to $TARGET_MOD"
cp -f "${ROOT_DIR}/target/HyRhythm.jar" "$TARGET_MOD"

echo "[smoke] Restarting tmux-backed local server"
"$SERVER_HELPER" stop >/dev/null 2>&1 || true
"$SERVER_HELPER" start
"$SERVER_HELPER" wait "command_registered aliases=[rhy] command=rhythm" 120
"$SERVER_HELPER" wait "Hytale Server Booted!" 120

run_step() {
    local command="$1"
    local expected="$2"
    local timeout="${3:-20}"
    echo "[smoke] > $command"
    "$SERVER_HELPER" send "$command" >/dev/null
    "$SERVER_HELPER" wait "$expected" "$timeout" >/dev/null
}

run_step "rhythm" "Usage: /rhythm"
run_step "rhy songs" "HyRhythm Debug Track"
run_step "rhythm keybinds show" "1=D 2=F 3=J 4=K"
run_step "rhythm debug on" "Rhythm debug set to on."
run_step "rhythm test" "Prepared debug chart debug/test-4k"
run_step "rhythm input down 1 1000" "Current session: session="
run_step "rhythm start" "Started rhythm session"
run_step "rhythm input down 1 1000" "down lane=1 time=1000ms result=PERFECT"
run_step "rhythm input down 2 1500" "down lane=2 time=1500ms result=PERFECT"
run_step "rhythm input down 3 2000" "down lane=3 time=2000ms result=PERFECT"
run_step "rhythm advance 2600" "Advanced gameplay: gameplay=active time=2600ms"
run_step "rhythm input down 4 3000" "finish=chart_complete" 30
run_step "rhythm state" "phase=ENDED chart=debug/test-4k"
run_step "rhythm stop" "Stopped rhythm session"

echo "[smoke] Local playable loop completed successfully."
echo "[smoke] Final state snapshot:"
"$SERVER_HELPER" capture 120 | tail -n 20
