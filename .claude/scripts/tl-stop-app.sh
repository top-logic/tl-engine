#!/usr/bin/env bash
#
# Stop a running TopLogic application.
#
# Usage: tl-stop-app.sh <base-url>
# Example: tl-stop-app.sh http://localhost:12345/tl-demo/
#
# Sends a stop request to the admin endpoint, waits for the port to free up,
# and cleans up the log file.

set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: tl-stop-app.sh <base-url>" >&2
    exit 1
fi

BASE_URL="$1"

# Parse host and port from the URL.
PORT=$(echo "$BASE_URL" | grep -oP ':\K[0-9]+' | head -1)
if [[ -z "$PORT" ]]; then
    echo "Error: Could not parse port from URL: $BASE_URL" >&2
    exit 1
fi

HOST=$(echo "$BASE_URL" | grep -oP '//\K[^:/]+' | head -1)
HOST="${HOST:-localhost}"

# Send the stop request.
STOP_URL="http://${HOST}:${PORT}/admin/stop"
if ! curl -sf "$STOP_URL" > /dev/null 2>&1; then
    echo "Warning: Stop request to $STOP_URL failed (app may not be running)." >&2
fi

# Wait for port to be freed.
TIMEOUT=15
INTERVAL=1
ELAPSED=0

while (( ELAPSED < TIMEOUT )); do
    if ! ss -tlnH "sport = :$PORT" 2>/dev/null | grep -q .; then
        # Port is free. Clean up log file.
        LOG="/tmp/tl-app-${PORT}.log"
        if [[ -f "$LOG" ]]; then
            rm -f "$LOG"
        fi
        echo "Stopped."
        exit 0
    fi
    sleep "$INTERVAL"
    (( ELAPSED += INTERVAL ))
done

echo "Error: Port $PORT still in use after ${TIMEOUT}s." >&2
exit 1
