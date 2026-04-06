#!/usr/bin/env bash
set -euo pipefail

SESSION_NAME="${HYRHYTHM_TMUX_SESSION:-hyrhythm-local}"
HYTALE_ROOT="${HYRHYTHM_HYTALE_ROOT:-/srv/hytale}"
START_SCRIPT="${HYTALE_ROOT}/start.sh"
LISTEN_PORT="${HYRHYTHM_HYTALE_PORT:-5520}"

build_start_env_prefix() {
    local env_parts=()

    if [ -n "${HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS:-}" ]; then
        env_parts+=("HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS=$(printf '%q' "$HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS")")
    fi

    printf '%s ' "${env_parts[@]}"
}

usage() {
    cat <<'USAGE'
Usage: scripts/hytale-local-server.sh <command> [args]

Commands:
  start                     Start the local Hytale server in a tmux session
  stop                      Stop the tmux-backed local Hytale server
  restart                   Restart the tmux-backed local Hytale server
  status                    Print whether the tmux session is running
  send <command...>         Send a server console command into the tmux session
  wait <text> [timeout]     Wait until pane output contains text
  capture [lines]           Print recent tmux pane output (default 200 lines)

Environment:
  HYRHYTHM_TMUX_SESSION     tmux session name (default: hyrhythm-local)
  HYRHYTHM_HYTALE_ROOT      Hytale server root (default: /srv/hytale)
  HYRHYTHM_HYTALE_PORT      Hytale listener port (default: 5520)
  HYRHYTHM_HYTALE_START_ARGS
                            extra args passed to ./start.sh (default: none)
  HYRHYTHM_RESET_LOCAL_WORLD
                            reset default test-world chunk storage before start
  HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS
                            forwarded into the server process for gameplay UI smoke/debug runs
USAGE
}

require_tmux() {
    if ! command -v tmux >/dev/null 2>&1; then
        echo "tmux is required for local server automation." >&2
        exit 1
    fi
}

require_start_script() {
    if [ ! -x "$START_SCRIPT" ]; then
        echo "Missing executable Hytale start script at $START_SCRIPT" >&2
        exit 1
    fi
}

ensure_instance_data_file() {
    local instance_data_file="${HYTALE_ROOT}/Server/universe/worlds/default/resources/InstanceData.json"
    mkdir -p "$(dirname "$instance_data_file")"
    if [ ! -f "$instance_data_file" ]; then
        printf '{}\n' > "$instance_data_file"
        echo "Seeded missing instance data file at $instance_data_file"
    fi
}

session_exists() {
    tmux has-session -t "$SESSION_NAME" 2>/dev/null
}

listener_is_active() {
    ss -ltn 2>/dev/null | awk '{print $4}' | grep -Eq "(^|:)${LISTEN_PORT}$"
}

session_command() {
    tmux list-panes -t "$SESSION_NAME" -F '#{pane_current_command}' 2>/dev/null | head -n 1
}

cleanup_stale_session() {
    if ! session_exists; then
        return
    fi
    local current_command
    current_command="$(session_command)"
    if [ "$current_command" = "bash" ] || [ "$current_command" = "sh" ]; then
        tmux kill-session -t "$SESSION_NAME"
        echo "Removed stale server session '$SESSION_NAME'."
    fi
}

reset_local_world_if_requested() {
    case "${HYRHYTHM_RESET_LOCAL_WORLD:-0}" in
        1|true|TRUE|yes|YES)
            ;;
        *)
            return
            ;;
    esac

    local world_root="${HYTALE_ROOT}/Server/universe/worlds/default"
    local backup_root="${HYTALE_ROOT}/Server/universe/worlds/.hyrhythm-backups"
    local timestamp
    timestamp="$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$backup_root"

    if [ -d "${world_root}/chunks" ]; then
        mv "${world_root}/chunks" "${backup_root}/default-chunks-${timestamp}"
        echo "Backed up default world chunks to ${backup_root}/default-chunks-${timestamp}"
    fi
    mkdir -p "${world_root}/chunks"

    if [ -f "${world_root}/resources/ChunkStorage.json" ]; then
        mv "${world_root}/resources/ChunkStorage.json" "${backup_root}/ChunkStorage-${timestamp}.json"
        printf '{"CurrentProvider":{"Type":"Hytale"}}\n' > "${world_root}/resources/ChunkStorage.json"
        echo "Reset chunk storage metadata for deterministic smoke runs."
    fi
}

start_server() {
    require_tmux
    require_start_script
    cleanup_stale_session
    ensure_instance_data_file
    reset_local_world_if_requested
    local start_args="${HYRHYTHM_HYTALE_START_ARGS:-}"
    local start_env_prefix
    start_env_prefix="$(build_start_env_prefix)"
    if session_exists; then
        echo "Server session '$SESSION_NAME' is already running."
        return 0
    fi
    tmux new-session -d -s "$SESSION_NAME" "cd '$HYTALE_ROOT' && exec env ${start_env_prefix}./start.sh $start_args"
    if [ -n "$start_args" ]; then
        echo "Started local Hytale server in tmux session '$SESSION_NAME' with args: $start_args"
    else
        echo "Started local Hytale server in tmux session '$SESSION_NAME'."
    fi
}

stop_server() {
    require_tmux
    if ! session_exists; then
        echo "Server session '$SESSION_NAME' is not running."
        return 0
    fi
    tmux send-keys -t "$SESSION_NAME" "stop" C-m
    for _ in $(seq 1 45); do
        if ! session_exists; then
            echo "Stopped local Hytale server session '$SESSION_NAME'."
            return 0
        fi
        sleep 1
    done
    echo "Timed out waiting for server session '$SESSION_NAME' to stop." >&2
    return 1
}

send_command() {
    require_tmux
    if ! session_exists; then
        echo "Server session '$SESSION_NAME' is not running." >&2
        exit 1
    fi
    if [ "$#" -eq 0 ]; then
        echo "No command provided." >&2
        exit 1
    fi
    tmux send-keys -t "$SESSION_NAME" "$*" C-m
    echo "sent: $*"
}

wait_for_text() {
    require_tmux
    if ! session_exists; then
        echo "Server session '$SESSION_NAME' is not running." >&2
        exit 1
    fi
    if [ "$#" -lt 1 ]; then
        echo "Missing text to wait for." >&2
        exit 1
    fi
    local expected="$1"
    local timeout="${2:-60}"
    local deadline=$((SECONDS + timeout))
    while [ "$SECONDS" -lt "$deadline" ]; do
        if tmux capture-pane -J -pt "$SESSION_NAME" -S -4000 | grep -Fq "$expected"; then
            echo "matched: $expected"
            return 0
        fi
        sleep 1
    done
    echo "Timed out after ${timeout}s waiting for: $expected" >&2
    return 1
}

capture_output() {
    require_tmux
    if ! session_exists; then
        echo "Server session '$SESSION_NAME' is not running." >&2
        exit 1
    fi
    local lines="${1:-200}"
    tmux capture-pane -J -pt "$SESSION_NAME" -S "-$lines"
}

status_server() {
    require_tmux
    if session_exists; then
        if listener_is_active; then
            echo "running:$SESSION_NAME:listening"
        else
            echo "running:$SESSION_NAME:stale"
        fi
    else
        echo "stopped:$SESSION_NAME"
    fi
}

main() {
    local command="${1:-}"
    case "$command" in
        start)
            start_server
            ;;
        stop)
            stop_server
            ;;
        restart)
            stop_server || true
            start_server
            ;;
        status)
            status_server
            ;;
        send)
            shift
            send_command "$@"
            ;;
        wait)
            shift
            wait_for_text "$@"
            ;;
        capture)
            shift
            capture_output "${1:-200}"
            ;;
        ""|-h|--help|help)
            usage
            ;;
        *)
            echo "Unknown command: $command" >&2
            usage >&2
            exit 1
            ;;
    esac
}

main "$@"
