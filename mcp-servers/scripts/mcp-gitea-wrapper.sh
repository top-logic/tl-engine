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

# Read credentials from keyring using venv Python
GITEA_TOKEN=$("$VENV_PYTHON" -c "import keyring; print(keyring.get_password('$SERVICE', '$ACC_TOKEN') or '')" 2>/dev/null)

if [[ -z "$GITEA_TOKEN" ]]; then
    echo "Error: Missing Gitea credentials in OS keyring." >&2
    echo "  service: $SERVICE" >&2
    echo "  token key: $ACC_TOKEN" >&2
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
