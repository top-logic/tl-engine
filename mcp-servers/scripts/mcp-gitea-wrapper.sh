#!/bin/bash
# Wrapper script that reads Gitea credentials from OS keyring.
# Credentials are stored securely in the OS keyring (no plaintext files).
#
# Setup credentials with:
#   ./mcp-servers/scripts/setup-mcp.sh gitea

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Use venv Python (with keyring installed)
VENV_PYTHON="$REPO_ROOT/.venv/bin/python"
if [ ! -x "$VENV_PYTHON" ]; then
    echo "Error: Virtual environment not found at $REPO_ROOT/.venv" >&2
    echo "Run: ./mcp-servers/scripts/setup-mcp.sh gitea" >&2
    exit 1
fi

# Keyring config (from environment or defaults)
SERVICE="${GITEA_KEYRING_SERVICE:-tl-engine-gitea-mcp}"
ACC_TOKEN="${GITEA_KEYRING_ACCOUNT_TOKEN:-token}"

# Resolution order: env var (GITEA_TOKEN), credentials file, then OS keyring.
# Handled by mcp-servers/credentials.py.
CRED_HELPER="$REPO_ROOT/mcp-servers/credentials.py"
GITEA_TOKEN=$("$VENV_PYTHON" "$CRED_HELPER" get "$SERVICE" "$ACC_TOKEN" --env GITEA_TOKEN)

if [[ -z "$GITEA_TOKEN" ]]; then
    echo "Error: Missing Gitea credentials." >&2
    echo "  Checked env: GITEA_TOKEN" >&2
    echo "  Checked file: \${TL_MCP_CRED_FILE:-~/.config/tl-engine-mcp/credentials.env} (key: ${SERVICE}__${ACC_TOKEN})" >&2
    echo "  Checked keyring service: $SERVICE" >&2
    echo "Run: ./mcp-servers/scripts/setup-mcp.sh gitea" >&2
    exit 1
fi

if ! command -v gitea-mcp >/dev/null 2>&1; then
    echo "Error: gitea-mcp not found in PATH." >&2
    echo "Please install gitea-mcp and make sure it is available in your PATH." >&2
    echo "Download from: https://gitea.com/gitea/gitea-mcp/releases" >&2
    exit 1
fi

exec gitea-mcp \
    -host "${GITEA_HOST:-https://git.top-logic.com}" \
    -token "${GITEA_TOKEN}" \
    "$@"
