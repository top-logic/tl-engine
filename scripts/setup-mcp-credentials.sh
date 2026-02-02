#!/bin/bash
# Setup credentials for MCP servers in the OS keyring.
# This script stores credentials securely - no plaintext files.
#
# Usage: ./scripts/setup-mcp-credentials.sh [trac|jenkins|all]

set -e

setup_trac() {
    echo "=== Trac MCP Credentials ==="
    local service="tl-engine-trac-mcp"

    read -p "Trac username: " username
    read -s -p "Trac password: " password
    echo

    python3 -c "
import keyring
keyring.set_password('$service', 'username', '$username')
keyring.set_password('$service', 'password', '$password')
print('Trac credentials stored in keyring.')
"
}

setup_jenkins() {
    echo "=== Jenkins MCP Credentials ==="
    local service="tl-engine-jenkins-mcp"

    read -p "Jenkins username: " username
    read -s -p "Jenkins API token: " password
    echo

    python3 -c "
import keyring
keyring.set_password('$service', 'username', '$username')
keyring.set_password('$service', 'password', '$password')
print('Jenkins credentials stored in keyring.')
"
}

case "${1:-all}" in
    trac)
        setup_trac
        ;;
    jenkins)
        setup_jenkins
        ;;
    all)
        setup_trac
        echo
        setup_jenkins
        ;;
    *)
        echo "Usage: $0 [trac|jenkins|all]"
        exit 1
        ;;
esac

echo
echo "Done. Restart Claude Code to use the new credentials."
