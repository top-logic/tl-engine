#!/bin/bash
# Wrapper script that reads Gitea credentials from OS keyring.
# Credentials are stored securely in the OS keyring (no plaintext files).
#
# Setup credentials with:
#   ./scripts/setup-mcp-credentials.sh gitea

# Keyring config (from environment or defaults)
SERVICE="${GITEA_KEYRING_SERVICE:-tl-engine-gitea-mcp}"
ACC_TOKEN="${GITEA_KEYRING_ACCOUNT_TOKEN:-token}"

# Read credentials from keyring using Python
GITEA_TOKEN=$(python3 -c "import keyring; print(keyring.get_password('$SERVICE', '$ACC_TOKEN') or '')" 2>/dev/null)

if [[ -z "$GITEA_TOKEN" ]]; then
    echo "Error: Missing Gitea credentials in OS keyring." >&2
    echo "  service: $SERVICE" >&2
    echo "  token key: $ACC_TOKEN" >&2
    echo "Run: ./scripts/setup-mcp-credentials.sh gitea" >&2
    exit 1
fi

exec gitea-mcp \
    -host "${GITEA_HOST:-https://git.top-logic.com}" \
    -token "${GITEA_TOKEN}" \
    "$@"
