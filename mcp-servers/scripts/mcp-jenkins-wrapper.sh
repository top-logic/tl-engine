#!/bin/bash
# Wrapper script that reads Jenkins credentials from OS keyring.
# Credentials are stored securely in the OS keyring (no plaintext files).
#
# Setup credentials with:
#   ./mcp-servers/scripts/setup-mcp.sh jenkins

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Use venv Python (with keyring installed)
VENV_PYTHON="$REPO_ROOT/.venv/bin/python"
if [ ! -x "$VENV_PYTHON" ]; then
    echo "Error: Virtual environment not found at $REPO_ROOT/.venv" >&2
    echo "Run: ./mcp-servers/scripts/setup-mcp.sh jenkins" >&2
    exit 1
fi

# Keyring config (from environment or defaults)
SERVICE="${JENKINS_KEYRING_SERVICE:-tl-engine-jenkins-mcp}"
ACC_USER="${JENKINS_KEYRING_ACCOUNT_USER:-username}"
ACC_PASS="${JENKINS_KEYRING_ACCOUNT_PASS:-password}"

# Resolution order: env vars (JENKINS_USERNAME / JENKINS_PASSWORD), credentials
# file, then OS keyring. Handled by mcp-servers/credentials.py.
CRED_HELPER="$REPO_ROOT/mcp-servers/credentials.py"
JENKINS_USERNAME=$("$VENV_PYTHON" "$CRED_HELPER" get "$SERVICE" "$ACC_USER" --env JENKINS_USERNAME)
JENKINS_PASSWORD=$("$VENV_PYTHON" "$CRED_HELPER" get "$SERVICE" "$ACC_PASS" --env JENKINS_PASSWORD)

if [[ -z "$JENKINS_USERNAME" || -z "$JENKINS_PASSWORD" ]]; then
    echo "Error: Missing Jenkins credentials." >&2
    echo "  Checked env: JENKINS_USERNAME, JENKINS_PASSWORD" >&2
    echo "  Checked file: \${TL_MCP_CRED_FILE:-~/.config/tl-engine-mcp/credentials.env} (keys: ${SERVICE}__${ACC_USER}, ${SERVICE}__${ACC_PASS})" >&2
    echo "  Checked keyring service: $SERVICE" >&2
    echo "Run: ./mcp-servers/scripts/setup-mcp.sh jenkins" >&2
    exit 1
fi

exec mcp-jenkins \
    --jenkins-url "${JENKINS_URL:-http://jenkins:8090/}" \
    --jenkins-username "${JENKINS_USERNAME}" \
    --jenkins-password "${JENKINS_PASSWORD}" \
    "$@"
