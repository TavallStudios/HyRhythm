#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVER_HELPER="${ROOT_DIR}/scripts/hytale-local-server.sh"
HYTALE_ROOT="${HYRHYTHM_HYTALE_ROOT:-/srv/hytale}"
TARGET_MOD="${HYTALE_ROOT}/Server/mods/HyRhythm.jar"
BUILT_MOD="${ROOT_DIR}/distribution/mods/HyRhythm.jar"
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
    (cd "$ROOT_DIR" && ./gradlew --no-daemon stageDistribution)
fi

if [ ! -f "$BUILT_MOD" ]; then
    echo "Missing built jar at $BUILT_MOD" >&2
    exit 1
fi

if [ -f "$TARGET_MOD" ]; then
    BACKUP_MOD="${TARGET_MOD}.pre-gradle-$(date -u +%Y%m%dT%H%M%SZ)"
    cp -p "$TARGET_MOD" "$BACKUP_MOD"
    echo "[smoke] Preserved rollback copy at $BACKUP_MOD"
fi

echo "[smoke] Deploying jar to $TARGET_MOD"
cp -f "$BUILT_MOD" "$TARGET_MOD"

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
run_step "rhythm input down 4 3000" "note=note-4"
run_step "rhythm input down 1 3500" "note=note-5"
run_step "rhythm input down 2 4500" "note=note-6"
run_step "rhythm input down 4 5500" "note=note-7"
run_step "rhythm input down 3 6500" "note=note-8"
run_step "rhythm input down 1 7500" "note=note-9"
run_step "rhythm input down 4 8500" "note=note-10"
run_step "rhythm input down 2 9000" "note=note-11"
run_step "rhythm input down 3 9500" "note=note-12"
run_step "rhythm input down 2 10500" "note=note-13"
run_step "rhythm input down 4 11500" "note=note-14"
run_step "rhythm input down 1 12500" "note=note-15"
run_step "rhythm input down 3 13500" "note=note-16"
run_step "rhythm input down 2 14500" "note=note-17"
run_step "rhythm input down 4 15500" "note=note-18"
run_step "rhythm input down 1 16500" "note=note-19"
run_step "rhythm input down 3 17500" "note=note-20"
run_step "rhythm input down 2 18500" "note=note-21"
run_step "rhythm input down 4 19500" "note=note-22"
run_step "rhythm input down 1 20000" "finish=chart_complete" 30
run_step "rhythm state" "phase=ENDED chart=debug/test-4k"
run_step "rhythm stop" "Stopped rhythm session"

echo "[smoke] Local playable loop completed successfully."
echo "[smoke] Final state snapshot:"
"$SERVER_HELPER" capture 120 | tail -n 20
