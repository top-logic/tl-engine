#!/usr/bin/env bash
#
# Manage a TopLogic application (start, stop, restart).
#
# Usage: tl-app.sh <start|stop|restart> <app-module-path>
# Example: tl-app.sh start com.top_logic.demo
#
# On start success, prints to stdout:
#   url: http://localhost:PORT/context-path/
#   log: <app-module-path>/tmp/tl-app.log
# On failure, prints diagnostics to stderr and exits 1.
#
# The port is recorded in <app-module-path>/tmp/app-port.txt to prevent
# duplicate starts and to allow stop/restart without a URL argument.

set -euo pipefail

usage() {
    echo "Usage: tl-app.sh <start|stop|restart> <app-module-path>" >&2
    exit 1
}

if [[ $# -lt 2 ]]; then
    usage
fi

COMMAND="$1"
APP_MODULE="$2"

# Validate the module path.
if [[ ! -f "$APP_MODULE/pom.xml" ]]; then
    echo "Error: $APP_MODULE/pom.xml not found. Not a valid module path." >&2
    exit 1
fi

PORT_FILE="$APP_MODULE/tmp/app-port.txt"

# --- Helper functions ---

pick_free_port() {
    local port
    for _ in $(seq 1 50); do
        port=$(( RANDOM % 50000 + 10000 ))
        if ! ss -tlnH "sport = :$port" 2>/dev/null | grep -q .; then
            echo "$port"
            return 0
        fi
    done
    echo "Error: Could not find a free port after 50 attempts." >&2
    return 1
}

read_port() {
    if [[ -f "$PORT_FILE" ]]; then
        cat "$PORT_FILE"
    fi
}

write_port() {
    mkdir -p "$(dirname "$PORT_FILE")"
    echo "$1" > "$PORT_FILE"
}

clear_port() {
    rm -f "$PORT_FILE"
}

is_port_in_use() {
    ss -tlnH "sport = :$1" 2>/dev/null | grep -q .
}

do_stop() {
    local port="$1"

    if [[ -z "$port" ]]; then
        echo "Error: No running app known (no port file found)." >&2
        return 1
    fi

    # Send the stop request.
    local stop_url="http://localhost:${port}/admin/stop"
    if ! curl -sf "$stop_url" > /dev/null 2>&1; then
        echo "Warning: Stop request to $stop_url failed (app may not be running)." >&2
    fi

    # Wait for port to be freed.
    local timeout=15 interval=1 elapsed=0
    while (( elapsed < timeout )); do
        if ! is_port_in_use "$port"; then
            # Clean up log file. Keep port file for reuse on next start.
            rm -f "$APP_MODULE/tmp/tl-app.log"
            echo "Stopped."
            return 0
        fi
        sleep "$interval"
        (( elapsed += interval ))
    done

    echo "Error: Port $port still in use after ${timeout}s." >&2
    return 1
}

do_start() {
    # If a previous instance is still running, stop it first.
    local existing_port
    existing_port=$(read_port)
    if [[ -n "$existing_port" ]] && is_port_in_use "$existing_port"; then
        echo "Stopping previous instance on port $existing_port..." >&2
        do_stop "$existing_port"
    fi

    # Reuse previous port if available and free, otherwise pick a new one.
    local port
    if [[ -n "$existing_port" ]] && ! is_port_in_use "$existing_port"; then
        port="$existing_port"
    else
        port=$(pick_free_port) || exit 1
    fi
    mkdir -p "$APP_MODULE/tmp"
    local log
    log="$(cd "$APP_MODULE/tmp" && pwd)/tl-app.log"

    export tl_initial_password='root1234'

    # Use the app module's own tmp/debug as java.io.tmpdir to avoid
    # writing to /tmp (which may be blocked by the sandbox).
    local java_tmpdir
    java_tmpdir="$(cd "$APP_MODULE" && pwd)/tmp/debug"
    mkdir -p "$java_tmpdir"

    # Start Maven in the background.
    cd "$APP_MODULE"
    nohup mvn -B -Dtl.port="$port" -Djava.io.tmpdir="$java_tmpdir" > "$log" 2>&1 &
    local mvn_pid=$!
    disown "$mvn_pid"
    cd - > /dev/null

    # Record port.
    write_port "$port"

    local timeout=120 interval=1 elapsed=0 app_url=""
    local server_started_at=""

    while (( elapsed < timeout )); do
        # Check for BUILD FAILURE first (before process liveness, to avoid race).
        if grep -q "BUILD FAILURE" "$log" 2>/dev/null; then
            echo "Error: Build failed." >&2
            echo "Log: $log" >&2
            grep -A 20 "BUILD FAILURE" "$log" >&2 2>/dev/null || true
            kill "$mvn_pid" 2>/dev/null || true
            exit 1
        fi

        # Check if Maven process is still alive.
        if ! kill -0 "$mvn_pid" 2>/dev/null; then
            echo "Error: Maven process exited unexpectedly." >&2
            echo "Log: $log" >&2
            tail -30 "$log" >&2 2>/dev/null || true
            exit 1
        fi

        # Extract URL from "Server started:" line if we haven't yet.
        if [[ -z "$app_url" ]]; then
            app_url=$(grep -oP 'Server started: \K\S+' "$log" 2>/dev/null || true)
            if [[ -n "$app_url" ]]; then
                server_started_at="$elapsed"
            fi
        fi

        # Check for the fully-ready signal.
        if grep -q "up and running" "$log" 2>/dev/null; then
            if [[ -z "$app_url" ]]; then
                app_url="http://localhost:${port}/"
            fi
            echo "url: $app_url"
            echo "log: $log"
            exit 0
        fi

        # If "Server started:" appeared but "up and running" hasn't followed
        # within 15 seconds, the application failed during initialization.
        if [[ -n "$server_started_at" ]] && (( elapsed - server_started_at > 15 )); then
            echo "Error: Application startup failed (server started but app did not become ready)." >&2
            echo "Log: $log" >&2
            grep "ERROR" "$log" >&2 2>/dev/null || true
            # Stop Jetty (still running despite failed app init).
            curl -sf "http://localhost:${port}/admin/stop" > /dev/null 2>&1 || true
            exit 1
        fi

        sleep "$interval"
        (( elapsed += interval ))
    done

    echo "Error: Timeout (${timeout}s) waiting for app to start." >&2
    echo "Log: $log" >&2
    tail -20 "$log" >&2 2>/dev/null || true
    exit 1
}

# --- Main ---

case "$COMMAND" in
    start)
        do_start
        ;;
    stop)
        port=$(read_port)
        do_stop "$port"
        ;;
    restart)
        port=$(read_port)
        if [[ -n "$port" ]] && is_port_in_use "$port"; then
            do_stop "$port"
        else
            echo "No running app found, starting fresh." >&2
        fi
        do_start
        ;;
    *)
        echo "Error: Unknown command '$COMMAND'. Use start, stop, or restart." >&2
        usage
        ;;
esac
