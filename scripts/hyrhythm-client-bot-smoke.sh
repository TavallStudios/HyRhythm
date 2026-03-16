#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVER_HELPER="${ROOT_DIR}/scripts/hytale-local-server.sh"
HYTALE_ROOT="${HYRHYTHM_HYTALE_ROOT:-/srv/hytale}"
TARGET_MOD="${HYTALE_ROOT}/Server/mods/HyRhythm.jar"
SERVER_JAR="/home/ubuntu/.m2/repository/com/hypixel/hytale/Server/2026.02.19-1a311a592/Server-2026.02.19-1a311a592.jar"
START_ARGS="${HYRHYTHM_HYTALE_START_ARGS:---transport TCP --auth-mode INSECURE --allow-op}"
INPUT_MODE="${HYRHYTHM_BOT_INPUT_MODE:-command-input}"
GAMEPLAY_SCENARIO="${HYRHYTHM_BOT_GAMEPLAY_SCENARIO:-play-chart}"
BOT_TIMEOUT_SECONDS="${HYRHYTHM_BOT_TIMEOUT_SECONDS:-70}"
PLAYER_NAME="${HYRHYTHM_BOT_NAME:-HyRhythmBot}"
PLAYER_UUID="${HYRHYTHM_BOT_UUID:-e7a0c3bd-3196-3bdf-a115-7f72c8178eb3}"
RESET_LOCAL_WORLD="${HYRHYTHM_RESET_LOCAL_WORLD:-0}"
BOT_EXTRA_ARGS_RAW="${HYRHYTHM_BOT_EXTRA_ARGS:-}"
GAMEPLAY_UI_REFRESH_INTERVAL_MS="${HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS:-}"
BOT_UI_LOOKAHEAD_MS="${HYRHYTHM_BOT_UI_LOOKAHEAD_MS:-}"
KEEP_RUNNING=false
SKIP_BUILD=false

usage() {
    cat <<'EOF'
Usage: scripts/hyrhythm-client-bot-smoke.sh [--skip-build] [--keep-running]

Builds and deploys HyRhythm, restarts the local Hytale server in TCP/insecure
mode, connects the protocol bot, drives the real command/UI flow, and verifies
that the session ends with authoritative judged gameplay.

Environment:
  HYRHYTHM_BOT_INPUT_MODE      command-input (default) or ui-packet
  HYRHYTHM_BOT_TIMEOUT_SECONDS bot timeout in seconds (default: 70)
  HYRHYTHM_BOT_GAMEPLAY_SCENARIO
                               play-chart (default), click-stop, or click-close
  HYRHYTHM_BOT_NAME            bot player name (default: HyRhythmBot)
  HYRHYTHM_BOT_UUID            bot UUID to pre-op from console
  HYRHYTHM_BOT_EXTRA_ARGS      extra args passed to RhythmProtocolBotMain
  HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS
                               override gameplay UI refresh interval for debug smoke runs
  HYRHYTHM_BOT_UI_LOOKAHEAD_MS ui-packet note lookahead in milliseconds
  HYRHYTHM_HYTALE_START_ARGS   server start args (default: --transport TCP --auth-mode INSECURE --allow-op)
  HYRHYTHM_RESET_LOCAL_WORLD   reset default smoke world before start (default: 0, opt-in)
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

if [ ! -x "$SERVER_HELPER" ]; then
    echo "Server helper script is missing or not executable: $SERVER_HELPER" >&2
    exit 1
fi

if [ ! -f "$SERVER_JAR" ]; then
    echo "Missing Hytale server API jar at $SERVER_JAR" >&2
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
    echo "[bot-smoke] Building HyRhythm package and test classes"
    (cd "$ROOT_DIR" && mvn -q -DskipTests package)
fi

if [ ! -f "${ROOT_DIR}/target/HyRhythm.jar" ]; then
    echo "Missing built jar at ${ROOT_DIR}/target/HyRhythm.jar" >&2
    exit 1
fi

echo "[bot-smoke] Deploying jar to $TARGET_MOD"
cp -f "${ROOT_DIR}/target/HyRhythm.jar" "$TARGET_MOD"

if [ "$INPUT_MODE" = "ui-packet" ]; then
    if [ -z "$GAMEPLAY_UI_REFRESH_INTERVAL_MS" ]; then
        GAMEPLAY_UI_REFRESH_INTERVAL_MS=0
    fi
    if [ -z "$BOT_UI_LOOKAHEAD_MS" ]; then
        BOT_UI_LOOKAHEAD_MS=260
    fi
fi

echo "[bot-smoke] Restarting local Hytale server with args: $START_ARGS"
"$SERVER_HELPER" stop >/dev/null 2>&1 || true
HYRHYTHM_HYTALE_START_ARGS="$START_ARGS" HYRHYTHM_RESET_LOCAL_WORLD="$RESET_LOCAL_WORLD" HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS="$GAMEPLAY_UI_REFRESH_INTERVAL_MS" "$SERVER_HELPER" start
HYRHYTHM_HYTALE_START_ARGS="$START_ARGS" "$SERVER_HELPER" wait "command_registered aliases=[rhy] command=rhythm" 120 >/dev/null
HYRHYTHM_HYTALE_START_ARGS="$START_ARGS" "$SERVER_HELPER" wait "Hytale Server Booted!" 120 >/dev/null

for _ in $(seq 1 30); do
    if ss -ltn 2>/dev/null | awk '{print $4}' | grep -Eq '(^|:)5520$'; then
        break
    fi
    sleep 1
done

if ! ss -ltn 2>/dev/null | awk '{print $4}' | grep -Eq '(^|:)5520$'; then
    echo "[bot-smoke] Hytale listener on 5520 did not come up after boot." >&2
    exit 1
fi

echo "[bot-smoke] Granting operator permissions to $PLAYER_UUID"
"$SERVER_HELPER" send "op add $PLAYER_UUID" >/dev/null
"$SERVER_HELPER" wait "Console executed command: op add $PLAYER_UUID" 20 >/dev/null

BOT_LOG="$(mktemp)"
declare -a BOT_EXTRA_ARGS=()
if [ -n "$BOT_EXTRA_ARGS_RAW" ]; then
    read -r -a BOT_EXTRA_ARGS <<< "$BOT_EXTRA_ARGS_RAW"
fi
if [ "$INPUT_MODE" = "ui-packet" ] && [ -n "$BOT_UI_LOOKAHEAD_MS" ]; then
    BOT_EXTRA_ARGS+=(--ui-lookahead-ms "$BOT_UI_LOOKAHEAD_MS")
fi

echo "[bot-smoke] Running protocol bot as $PLAYER_NAME using input mode $INPUT_MODE and scenario $GAMEPLAY_SCENARIO"
(
    cd "$ROOT_DIR"
    java -cp "target/test-classes:target/classes:${SERVER_JAR}" \
        com.hyrhythm.protocolbot.RhythmProtocolBotMain \
        --host 127.0.0.1 \
        --port 5520 \
        --timeout-seconds "$BOT_TIMEOUT_SECONDS" \
        --name "$PLAYER_NAME" \
        --input-mode "$INPUT_MODE" \
        --gameplay-scenario "$GAMEPLAY_SCENARIO" \
        --assume-op \
        "${BOT_EXTRA_ARGS[@]}"
) | tee "$BOT_LOG"

if ! grep -Fq "[bot] success=true" "$BOT_LOG"; then
    echo "[bot-smoke] Protocol bot did not report success." >&2
    exit 1
fi

require_server_log() {
    local pattern="$1"
    local description="$2"
    if ! "$SERVER_HELPER" capture 1200 | grep -Fq "$pattern"; then
        echo "[bot-smoke] Missing server log marker for ${description}: $pattern" >&2
        exit 1
    fi
}

require_server_log "song_selection_ui_opened" "song selection UI"
require_server_log "chart_confirmed" "chart confirmation"
require_server_log "gameplay_ui_opened" "gameplay UI"

case "$GAMEPLAY_SCENARIO" in
    play-chart)
        if ! grep -Fq "phase=ENDED chart=debug/test-4k" "$BOT_LOG"; then
            echo "[bot-smoke] Final bot state did not reach the expected chart completion." >&2
            exit 1
        fi
        if ! grep -Fq "score=1530" "$BOT_LOG"; then
            echo "[bot-smoke] Expected authoritative score=1530 in final bot state." >&2
            exit 1
        fi
        if ! grep -Fq "maxCombo=5" "$BOT_LOG"; then
            echo "[bot-smoke] Expected authoritative maxCombo=5 in final bot state." >&2
            exit 1
        fi
        require_server_log "session_completed phase=ENDED" "session completion"
        if [ "$INPUT_MODE" = "command-input" ]; then
            require_server_log "lane_judged accuracyAfter=100.0" "authoritative lane judgment"
            require_server_log "gameplay_completed accuracy=100.0" "authoritative gameplay completion"
        fi
        ;;
    click-stop)
        if ! grep -Fq "phase=ENDED chart=debug/test-4k" "$BOT_LOG"; then
            echo "[bot-smoke] Stop scenario did not end the session." >&2
            exit 1
        fi
        if ! grep -Fq "finish=ui_stop" "$BOT_LOG"; then
            echo "[bot-smoke] Stop scenario did not produce finish=ui_stop." >&2
            exit 1
        fi
        require_server_log "gameplay_stop_requested" "gameplay stop request"
        require_server_log "session_stopped" "session stop"
        ;;
    click-close)
        if ! grep -Fq "phase=PLAYING chart=debug/test-4k" "$BOT_LOG"; then
            echo "[bot-smoke] Close scenario did not keep the session in PLAYING." >&2
            exit 1
        fi
        if ! grep -Eq "gameplay=(active|idle)" "$BOT_LOG"; then
            echo "[bot-smoke] Close scenario did not report active gameplay state." >&2
            exit 1
        fi
        require_server_log "gameplay_close_requested" "gameplay close request"
        ;;
    *)
        echo "[bot-smoke] Unknown gameplay scenario: $GAMEPLAY_SCENARIO" >&2
        exit 1
        ;;
esac

echo "[bot-smoke] Client bot gameplay scenario completed successfully."
echo "[bot-smoke] Final bot state:"
tail -n 5 "$BOT_LOG"
