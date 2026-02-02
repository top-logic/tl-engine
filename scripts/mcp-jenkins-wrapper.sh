#!/bin/bash
# Wrapper script that reads Jenkins credentials from OS keyring.
# Credentials are stored securely in the OS keyring (no plaintext files).
#
# Setup credentials with:
#   ./scripts/setup-mcp-credentials.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Keyring config (from environment or defaults)
SERVICE="${JENKINS_KEYRING_SERVICE:-tl-engine-jenkins-mcp}"
ACC_USER="${JENKINS_KEYRING_ACCOUNT_USER:-username}"
ACC_PASS="${JENKINS_KEYRING_ACCOUNT_PASS:-password}"

# Read credentials from keyring using Python
JENKINS_USERNAME=$(python3 -c "import keyring; print(keyring.get_password('$SERVICE', '$ACC_USER') or '')" 2>/dev/null)
JENKINS_PASSWORD=$(python3 -c "import keyring; print(keyring.get_password('$SERVICE', '$ACC_PASS') or '')" 2>/dev/null)

if [[ -z "$JENKINS_USERNAME" || -z "$JENKINS_PASSWORD" ]]; then
    echo "Error: Missing Jenkins credentials in OS keyring." >&2
    echo "  service: $SERVICE" >&2
    echo "  username key: $ACC_USER" >&2
    echo "  password key: $ACC_PASS" >&2
    echo "Run: ./scripts/setup-mcp-credentials.sh" >&2
    exit 1
fi

exec mcp-jenkins \
    --jenkins-url "${JENKINS_URL:-http://jenkins:8090/}" \
    --jenkins-username "${JENKINS_USERNAME}" \
    --jenkins-password "${JENKINS_PASSWORD}" \
    "$@"
