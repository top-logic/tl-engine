#!/usr/bin/env bash
# Unified MCP setup script for Claude Code integration.
#
# Usage:
#   ./mcp-servers/scripts/setup-mcp.sh          # Interactive: asks which services to set up
#   ./mcp-servers/scripts/setup-mcp.sh all      # Set up everything
#   ./mcp-servers/scripts/setup-mcp.sh trac     # Set up only Trac
#   ./mcp-servers/scripts/setup-mcp.sh jenkins  # Set up only Jenkins
#   ./mcp-servers/scripts/setup-mcp.sh gitea    # Set up only Gitea
#
# This script:
#   - Creates/reuses a project .venv with all Python dependencies
#   - Downloads binary tools (gitea-mcp) to ~/.local/bin and installs mcp-jenkins via pip
#   - Prompts for credentials and stores them in the OS keyring
#   - Validates connections where possible
#
set -euo pipefail

# --- Configuration ---
VENV_DIR="${VENV_DIR:-.venv}"
BIN_DIR="${BIN_DIR:-$HOME/.local/bin}"

# Keyring service names
TRAC_KEYRING_SERVICE="${TRAC_KEYRING_SERVICE:-tl-engine-trac-mcp}"
JENKINS_KEYRING_SERVICE="${JENKINS_KEYRING_SERVICE:-tl-engine-jenkins-mcp}"
GITEA_KEYRING_SERVICE="${GITEA_KEYRING_SERVICE:-tl-engine-gitea-mcp}"

# Service URLs
TRAC_URL="${TRAC_URL:-http://tl/trac/login/xmlrpc}"
JENKINS_URL="${JENKINS_URL:-http://jenkins:8090/}"
GITEA_HOST="${GITEA_HOST:-https://git.top-logic.com}"

# gitea-mcp version to download
GITEA_MCP_VERSION="${GITEA_MCP_VERSION:-v0.7.0}"

# --- Helpers ---
die() { echo "ERROR: $*" >&2; exit 1; }
info() { echo "* $*"; }
warn() { echo "WARNING: $*" >&2; }

# --- Ensure we run from repo root ---
ensure_repo_root() {
    if git rev-parse --show-toplevel >/dev/null 2>&1; then
        REPO_ROOT="$(git rev-parse --show-toplevel)"
        cd "$REPO_ROOT"
    else
        die "Not in a git repository. Run from the tl-engine repository root."
    fi
}

# --- Check Prerequisites ---
check_prereqs() {
    local missing=()

    if ! command -v python3 >/dev/null 2>&1; then
        missing+=("python3")
    fi

    if ! python3 -c "import venv" 2>/dev/null; then
        missing+=("python3-venv module")
    fi

    if ! command -v curl >/dev/null 2>&1 && ! command -v wget >/dev/null 2>&1; then
        missing+=("curl or wget")
    fi

    if [ ${#missing[@]} -gt 0 ]; then
        echo "Missing prerequisites:" >&2
        printf "  - %s\n" "${missing[@]}" >&2
        echo >&2
        echo "Install missing packages:" >&2
        echo "  Ubuntu/Debian:  sudo apt install python3 python3-venv curl" >&2
        echo "  Fedora/RHEL:    sudo dnf install python3 python3-venv curl" >&2
        echo "  macOS:          brew install python3 curl" >&2
        exit 1
    fi

    info "Prerequisites check passed"
}

# --- Create/Reuse Virtual Environment ---
ensure_venv() {
    if [ ! -d "$VENV_DIR" ]; then
        info "Creating virtual environment: $VENV_DIR"
        python3 -m venv "$VENV_DIR"
    else
        info "Using existing virtual environment: $VENV_DIR"
    fi

    PY="$VENV_DIR/bin/python"
    PIP="$VENV_DIR/bin/pip"

    # Ensure pip is installed
    if ! "$PY" -m pip --version >/dev/null 2>&1; then
        info "Installing pip in venv"
        "$PY" -m ensurepip --upgrade --default-pip >/dev/null 2>&1 || {
            info "ensurepip failed, trying get-pip.py"
            local tmp_get_pip
            tmp_get_pip=$(mktemp)
            trap "rm -f $tmp_get_pip" EXIT
            if command -v curl >/dev/null 2>&1; then
                curl -sSLo "$tmp_get_pip" https://bootstrap.pypa.io/get-pip.py
            else
                wget -qO "$tmp_get_pip" https://bootstrap.pypa.io/get-pip.py
            fi
            "$PY" "$tmp_get_pip" >/dev/null || die "Failed to install pip."
        }
    fi

    info "Upgrading pip"
    "$PY" -m pip install -U pip >/dev/null 2>&1

    info "Installing Python dependencies (mcp, keyring, secretstorage)"
    "$PIP" install -U mcp keyring secretstorage >/dev/null 2>&1
}

# --- Store Credentials in Keyring ---
store_in_keyring() {
    local service="$1"
    local key="$2"
    local value="$3"

    "$PY" - <<PYTHON
import keyring
keyring.set_password("${service}", "${key}", """${value}""")
PYTHON
}

get_from_keyring() {
    local service="$1"
    local key="$2"

    "$PY" -c "import keyring; print(keyring.get_password('$service', '$key') or '')" 2>/dev/null
}

# --- Setup Trac ---
setup_trac() {
    info "Setting up Trac MCP..."

    # Check if server script exists
    if [ ! -f "mcp-servers/trac-mcp-server.py" ]; then
        die "Cannot find mcp-servers/trac-mcp-server.py. Run from repo root."
    fi

    # Prompt for credentials
    read -r -p "Trac username: " TRAC_USERNAME
    [ -n "$TRAC_USERNAME" ] || die "Username cannot be empty."

    read -r -s -p "Trac password (stored in OS keyring): " TRAC_PASSWORD
    echo
    [ -n "$TRAC_PASSWORD" ] || die "Password cannot be empty."

    # Store credentials
    info "Storing Trac credentials in keyring (service: $TRAC_KEYRING_SERVICE)"
    store_in_keyring "$TRAC_KEYRING_SERVICE" "username" "$TRAC_USERNAME"
    store_in_keyring "$TRAC_KEYRING_SERVICE" "password" "$TRAC_PASSWORD"

    # Validate connection
    info "Validating Trac connection: $TRAC_URL"
    "$PY" - <<PYTHON || warn "Validation failed. Check credentials and URL."
import base64
import sys
import xmlrpc.client
import keyring

url = "${TRAC_URL}"
service = "${TRAC_KEYRING_SERVICE}"

username = keyring.get_password(service, "username")
password = keyring.get_password(service, "password")

if not username or not password:
    print("Keyring lookup failed.", file=sys.stderr)
    sys.exit(2)

class BasicAuthTransport(xmlrpc.client.Transport):
    def send_headers(self, connection, headers):
        auth = base64.b64encode(f"{username}:{password}".encode("utf-8")).decode("ascii")
        connection.putheader("Authorization", f"Basic {auth}")
        super().send_headers(connection, headers)

proxy = xmlrpc.client.ServerProxy(url, transport=BasicAuthTransport(), allow_none=True)

methods = [
    ("system.getAPIVersion", lambda: proxy.system.getAPIVersion()),
    ("system.listMethods",  lambda: proxy.system.listMethods()),
]

for name, fn in methods:
    try:
        fn()
        print(f"  OK: {name}")
        sys.exit(0)
    except Exception as e:
        pass

print("  Could not validate (this may be OK if Trac requires specific permissions)", file=sys.stderr)
sys.exit(0)  # Non-fatal
PYTHON

    echo "  Trac setup complete."
}

# --- Setup Jenkins ---
setup_jenkins() {
    info "Setting up Jenkins MCP..."

    mkdir -p "$BIN_DIR"

    # Install mcp-jenkins
    if ! command -v mcp-jenkins >/dev/null 2>&1 && [ ! -f "$BIN_DIR/mcp-jenkins" ]; then
        info "Installing mcp-jenkins..."
        if command -v uv >/dev/null 2>&1; then
            uv tool install mcp-jenkins >/dev/null 2>&1
        else
            "$PIP" install mcp-jenkins >/dev/null 2>&1
            # Create wrapper in BIN_DIR
            cat > "$BIN_DIR/mcp-jenkins" <<'WRAPPER'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo "$HOME")"
if [ -f "$REPO_ROOT/.venv/bin/mcp-jenkins" ]; then
    exec "$REPO_ROOT/.venv/bin/mcp-jenkins" "$@"
else
    exec python3 -m mcp_jenkins "$@"
fi
WRAPPER
            chmod +x "$BIN_DIR/mcp-jenkins"
        fi
        info "mcp-jenkins installed."
    else
        info "mcp-jenkins already installed."
    fi

    # Prompt for credentials
    read -r -p "Jenkins username: " JENKINS_USERNAME
    [ -n "$JENKINS_USERNAME" ] || die "Username cannot be empty."

    read -r -s -p "Jenkins API token (stored in OS keyring): " JENKINS_PASSWORD
    echo
    [ -n "$JENKINS_PASSWORD" ] || die "API token cannot be empty."

    # Store credentials
    info "Storing Jenkins credentials in keyring (service: $JENKINS_KEYRING_SERVICE)"
    store_in_keyring "$JENKINS_KEYRING_SERVICE" "username" "$JENKINS_USERNAME"
    store_in_keyring "$JENKINS_KEYRING_SERVICE" "password" "$JENKINS_PASSWORD"

    echo "  Jenkins setup complete."
}

# --- Setup Gitea ---
setup_gitea() {
    info "Setting up Gitea MCP..."

    mkdir -p "$BIN_DIR"

    # Check if gitea-mcp is already available
    if command -v gitea-mcp >/dev/null 2>&1 || [ -f "$BIN_DIR/gitea-mcp" ]; then
        info "gitea-mcp already installed."
    else
        # Try downloading pre-built binary (single attempt)
        info "Downloading gitea-mcp ${GITEA_MCP_VERSION}..."

        local installed=false arch os url tmp_file
        arch=$(uname -m)
        os=$(uname -s)

        case "$arch" in
            x86_64) arch="x86_64" ;;
            aarch64|arm64) arch="arm64" ;;
            i386|i686) arch="i386" ;;
            *) arch="" ;;
        esac

        case "$os" in
            Linux) os="Linux" ;;
            Darwin) os="Darwin" ;;
            *) os="" ;;
        esac

        if [ -n "$arch" ] && [ -n "$os" ]; then
            url="https://gitea.com/gitea/gitea-mcp/releases/download/${GITEA_MCP_VERSION}/gitea-mcp_${os}_${arch}.tar.gz"
            tmp_file=$(mktemp)

            if command -v curl >/dev/null 2>&1; then
                curl -fsSL --http1.1 --connect-timeout 15 -o "$tmp_file" "$url" 2>/dev/null
            else
                wget -q --timeout=15 -O "$tmp_file" "$url" 2>/dev/null
            fi

            if [ -s "$tmp_file" ] && tar -xzf "$tmp_file" -C "$BIN_DIR" gitea-mcp 2>/dev/null; then
                chmod +x "$BIN_DIR/gitea-mcp"
                installed=true
                info "gitea-mcp installed to $BIN_DIR/gitea-mcp"
            fi
            rm -f "$tmp_file"
        fi

        if [ "$installed" = false ]; then
            echo
            warn "Could not install gitea-mcp automatically."
            echo "  Please install gitea-mcp and make sure it is available in your PATH." >&2
            echo "  Download from: https://gitea.com/gitea/gitea-mcp/releases" >&2
            echo "  Place the binary in: $BIN_DIR/ (or anywhere in your PATH)" >&2
            echo
            echo "  Continuing with credential setup (you can install the binary later)..."
            echo
        fi
    fi

    # Prompt for token
    read -r -s -p "Gitea access token (stored in OS keyring): " GITEA_TOKEN
    echo
    [ -n "$GITEA_TOKEN" ] || die "Access token cannot be empty."

    # Store credentials
    info "Storing Gitea token in keyring (service: $GITEA_KEYRING_SERVICE)"
    store_in_keyring "$GITEA_KEYRING_SERVICE" "token" "$GITEA_TOKEN"

    echo "  Gitea setup complete."
}

# --- Interactive Menu ---
interactive_menu() {
    echo "MCP Server Setup"
    echo "================"
    echo
    echo "Which MCP servers would you like to set up?"
    echo
    echo "  1) All (Trac, Jenkins, Gitea)"
    echo "  2) Trac only"
    echo "  3) Jenkins only"
    echo "  4) Gitea only"
    echo "  5) Exit"
    echo

    read -r -p "Choice [1-5]: " choice

    case "$choice" in
        1) setup_all ;;
        2) setup_trac ;;
        3) setup_jenkins ;;
        4) setup_gitea ;;
        5) exit 0 ;;
        *) die "Invalid choice." ;;
    esac
}

setup_all() {
    setup_trac
    echo
    setup_jenkins
    echo
    setup_gitea
}

# --- Main ---
main() {
    ensure_repo_root
    check_prereqs
    ensure_venv

    # Ensure BIN_DIR is in PATH
    case ":$PATH:" in
        *":$BIN_DIR:"*) ;;
        *) warn "$BIN_DIR is not in your PATH. Add it with: export PATH=\"\$PATH:$BIN_DIR\"" ;;
    esac

    case "${1:-}" in
        all)
            setup_all
            ;;
        trac)
            setup_trac
            ;;
        jenkins)
            setup_jenkins
            ;;
        gitea)
            setup_gitea
            ;;
        "")
            interactive_menu
            ;;
        *)
            echo "Usage: $0 [all|trac|jenkins|gitea]"
            exit 1
            ;;
    esac

    echo
    echo "Setup complete!"
    echo
    echo "Next steps:"
    echo "  1. Restart Claude Code to pick up the new credentials"
    echo "  2. Type /mcp in Claude Code to verify server connections"
}

main "$@"
