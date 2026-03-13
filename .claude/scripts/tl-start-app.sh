#!/usr/bin/env bash
#
# Start a TopLogic application in the background and wait until it is ready.
#
# Usage: tl-start-app.sh <app-module-path>
# Example: tl-start-app.sh com.top_logic.demo
#
# On success, prints to stdout:
#   url: http://localhost:PORT/context-path/
#   log: /tmp/tl-app-PORT.log
# On failure, prints diagnostics to stderr and exits 1.
#
# The Maven/Jetty process keeps running after this script exits.

set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: tl-start-app.sh <app-module-path>" >&2
    exit 1
fi

APP_MODULE="$1"

# Validate the module path.
if [[ ! -f "$APP_MODULE/pom.xml" ]]; then
    echo "Error: $APP_MODULE/pom.xml not found. Not a valid module path." >&2
    exit 1
fi

# Pick a random free port in 10000-60000.
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

PORT=$(pick_free_port) || exit 1
LOG="/tmp/tl-app-${PORT}.log"

export tl_initial_password='root1234'

# Start Maven in the background. Redirect all output to the log file.
cd "$APP_MODULE"
nohup mvn -Dtl.port="$PORT" > "$LOG" 2>&1 &
MVN_PID=$!
disown "$MVN_PID"
cd - > /dev/null

TIMEOUT=120
INTERVAL=1
ELAPSED=0
APP_URL=""

while (( ELAPSED < TIMEOUT )); do
    # Check if Maven process is still alive.
    if ! kill -0 "$MVN_PID" 2>/dev/null; then
        echo "Error: Maven process exited unexpectedly." >&2
        echo "Log: $LOG" >&2
        # Show last 30 lines for diagnostics.
        tail -30 "$LOG" >&2 2>/dev/null || true
        exit 1
    fi

    # Check for BUILD FAILURE.
    if grep -q "BUILD FAILURE" "$LOG" 2>/dev/null; then
        echo "Error: Build failed." >&2
        echo "Log: $LOG" >&2
        grep -A 20 "BUILD FAILURE" "$LOG" >&2 2>/dev/null || true
        # Kill the Maven process since it failed.
        kill "$MVN_PID" 2>/dev/null || true
        exit 1
    fi

    # Extract URL from "Server started:" line if we haven't yet.
    if [[ -z "$APP_URL" ]]; then
        APP_URL=$(grep -oP 'Server started: \K\S+' "$LOG" 2>/dev/null || true)
    fi

    # Check for the fully-ready signal.
    if grep -q "up and running" "$LOG" 2>/dev/null; then
        if [[ -z "$APP_URL" ]]; then
            # Fallback: construct URL from port (shouldn't normally happen).
            APP_URL="http://localhost:${PORT}/"
        fi
        echo "url: $APP_URL"
        echo "log: $LOG"
        exit 0
    fi

    sleep "$INTERVAL"
    (( ELAPSED += INTERVAL ))
done

echo "Error: Timeout (${TIMEOUT}s) waiting for app to start." >&2
echo "Log: $LOG" >&2
tail -20 "$LOG" >&2 2>/dev/null || true
exit 1
