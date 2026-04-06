#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SESSION_NAME="${HYRHYTHM_UI_DEBUG_BOT_SESSION:-hyrhythm-ui-debug-bot}"
SERVER_JAR="${HYRHYTHM_UI_DEBUG_BOT_SERVER_JAR:-/home/ubuntu/.m2/repository/com/hypixel/hytale/Server/2026.02.19-1a311a592/Server-2026.02.19-1a311a592.jar}"
HOST="${HYRHYTHM_UI_DEBUG_BOT_HOST:-127.0.0.1}"
PORT="${HYRHYTHM_UI_DEBUG_BOT_PORT:-5520}"
PLAYER_NAME="${HYRHYTHM_UI_DEBUG_BOT_NAME:-HyRhythmUiBot}"
TIMEOUT_SECONDS="${HYRHYTHM_UI_DEBUG_BOT_TIMEOUT_SECONDS:-600}"
MODE="${HYRHYTHM_UI_DEBUG_BOT_MODE:-listen-only}"
TRACE_UI="${HYRHYTHM_UI_DEBUG_BOT_TRACE_UI:-1}"
ASSUME_OP="${HYRHYTHM_UI_DEBUG_BOT_ASSUME_OP:-1}"
SKIP_BUILD="${HYRHYTHM_UI_DEBUG_BOT_SKIP_BUILD:-0}"
UI_LOOKAHEAD_MS="${HYRHYTHM_UI_DEBUG_BOT_UI_LOOKAHEAD_MS:-260}"

usage() {
    cat <<'EOF'
Usage: scripts/hyrhythm-ui-debug-bot.sh <start|stop|restart|status|capture>

Commands:
  start         Build if needed and launch the debug bot in tmux
  stop          Stop the tmux-backed debug bot session
  restart       Restart the debug bot session
  status        Print whether the debug bot session is running
  capture       Print recent tmux pane output (default 200 lines, set HYRHYTHM_UI_DEBUG_BOT_CAPTURE_LINES)

Environment:
  HYRHYTHM_UI_DEBUG_BOT_MODE          listen-only (default), command-input, or ui-packet
  HYRHYTHM_UI_DEBUG_BOT_NAME          bot player name (default: HyRhythmUiBot)
  HYRHYTHM_UI_DEBUG_BOT_TIMEOUT_SECONDS
                                      bot runtime timeout in seconds (default: 600)
  HYRHYTHM_UI_DEBUG_BOT_TRACE_UI      1 to enable --trace-ui (default: 1)
  HYRHYTHM_UI_DEBUG_BOT_ASSUME_OP     1 to enable --assume-op (default: 1)
  HYRHYTHM_UI_DEBUG_BOT_SKIP_BUILD    1 to skip mvn test-compile before launch
  HYRHYTHM_UI_DEBUG_BOT_UI_LOOKAHEAD_MS
                                      ui-packet mode lookahead (default: 260)
  HYRHYTHM_UI_DEBUG_BOT_CAPTURE_LINES capture line count (default: 200)
EOF
}

require_tmux() {
    if ! command -v tmux >/dev/null 2>&1; then
        echo "tmux is required for debug bot automation." >&2
        exit 1
    fi
}

session_exists() {
    tmux has-session -t "$SESSION_NAME" 2>/dev/null
}

build_if_needed() {
    if [ "$SKIP_BUILD" = "1" ]; then
        return
    fi
    (cd "$ROOT_DIR" && mvn -q -DskipTests test-compile)
}

bot_command() {
    local -a args
    args=(
        java -cp "target/test-classes:target/classes:${SERVER_JAR}"
        com.hyrhythm.protocolbot.RhythmProtocolBotMain
        --host "$HOST"
        --port "$PORT"
        --timeout-seconds "$TIMEOUT_SECONDS"
        --name "$PLAYER_NAME"
    )

    case "$MODE" in
        listen-only)
            args+=(--input-mode command-input --listen-only)
            ;;
        command-input)
            args+=(--input-mode command-input)
            ;;
        ui-packet)
            args+=(--input-mode ui-packet --ui-lookahead-ms "$UI_LOOKAHEAD_MS")
            ;;
        *)
            echo "Unsupported debug bot mode: $MODE" >&2
            exit 1
            ;;
    esac

    if [ "$ASSUME_OP" = "1" ]; then
        args+=(--assume-op)
    fi
    if [ "$TRACE_UI" = "1" ]; then
        args+=(--trace-ui)
    fi

    printf 'cd %q && exec' "$ROOT_DIR"
    for arg in "${args[@]}"; do
        printf ' %q' "$arg"
    done
    printf '\n'
}

start_bot() {
    require_tmux
    if session_exists; then
        echo "running:$SESSION_NAME"
        return 0
    fi
    build_if_needed
    tmux new-session -d -s "$SESSION_NAME" "$(bot_command)"
    echo "started:$SESSION_NAME:mode=$MODE:name=$PLAYER_NAME"
}

stop_bot() {
    require_tmux
    if ! session_exists; then
        echo "stopped:$SESSION_NAME"
        return 0
    fi
    tmux kill-session -t "$SESSION_NAME"
    echo "stopped:$SESSION_NAME"
}

status_bot() {
    require_tmux
    if session_exists; then
        echo "running:$SESSION_NAME"
    else
        echo "stopped:$SESSION_NAME"
    fi
}

capture_bot() {
    require_tmux
    if ! session_exists; then
        echo "Debug bot session '$SESSION_NAME' is not running." >&2
        exit 1
    fi
    local lines="${HYRHYTHM_UI_DEBUG_BOT_CAPTURE_LINES:-200}"
    tmux capture-pane -J -pt "$SESSION_NAME" -S "-$lines"
}

main() {
    case "${1:-}" in
        start)
            start_bot
            ;;
        stop)
            stop_bot
            ;;
        restart)
            stop_bot >/dev/null 2>&1 || true
            start_bot
            ;;
        status)
            status_bot
            ;;
        capture)
            capture_bot
            ;;
        ""|-h|--help|help)
            usage
            ;;
        *)
            echo "Unknown command: ${1:-}" >&2
            usage >&2
            exit 1
            ;;
    esac
}

main "$@"
