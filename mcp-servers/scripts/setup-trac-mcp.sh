#!/usr/bin/env bash
set -euo pipefail

# --- config (can be overridden via env) ---
TRAC_URL="${TRAC_URL:-http://tl/trac/login/xmlrpc}"
KEYRING_SERVICE="${KEYRING_SERVICE:-tl-engine-trac-mcp}"
VENV_DIR="${VENV_DIR:-.venv}"
SERVER_SCRIPT="${SERVER_SCRIPT:-mcp-servers/trac-mcp-server.py}"

# We store BOTH username and password in keyring under fixed account names.
KEYRING_ACCOUNT_USER="${KEYRING_ACCOUNT_USER:-username}"
KEYRING_ACCOUNT_PASS="${KEYRING_ACCOUNT_PASS:-password}"

# --- helpers ---
die() { echo "ERROR: $*" >&2; exit 1; }
info() { echo "• $*"; }

# --- check prerequisites ---
check_prereqs() {
  local missing=()
  local has_curl_or_wget=false

  # Check python3
  if ! command -v python3 >/dev/null 2>&1; then
    missing+=("python3")
  fi

  # Check python3-venv module
  if ! python3 -c "import venv" 2>/dev/null; then
    missing+=("python3-venv module")
  fi

  # Check curl or wget (optional, for fallback pip installation)
  if command -v curl >/dev/null 2>&1 || command -v wget >/dev/null 2>&1; then
    has_curl_or_wget=true
  fi

  if [ ${#missing[@]} -gt 0 ]; then
    echo "❌ Missing prerequisites:" >&2
    printf "  - %s\n" "${missing[@]}" >&2
    echo >&2
    echo "Install missing packages:" >&2
    echo "  Ubuntu/Debian:  sudo apt install python3 python3-venv" >&2
    echo "  Fedora/RHEL:    sudo dnf install python3 python3-venv" >&2
    echo "  macOS:          brew install python3" >&2
    echo "  Windows (WSL):  sudo apt install python3 python3-venv" >&2
    echo "  Windows (Git Bash): Install Python from python.org" >&2
    exit 1
  fi

  info "Prerequisites check passed"

  # Optional: warn about curl/wget for fallback
  if [ "$has_curl_or_wget" = false ]; then
    echo "⚠️  Note: curl or wget not found. Fallback pip installation may fail." >&2
    echo "   If ensurepip fails, install curl or wget and re-run." >&2
  fi
}

check_prereqs

# Ensure we run from repo root (best effort)
if git rev-parse --show-toplevel >/dev/null 2>&1; then
  REPO_ROOT="$(git rev-parse --show-toplevel)"
  cd "$REPO_ROOT"
fi

[ -f "$SERVER_SCRIPT" ] || die "Cannot find server script at: $SERVER_SCRIPT (run from repo root?)"

# 1) Create venv and install deps
if [ ! -d "$VENV_DIR" ]; then
  info "Creating venv: $VENV_DIR"
  python3 -m venv "$VENV_DIR"
else
  info "Using existing venv: $VENV_DIR"
fi

PY="$VENV_DIR/bin/python"
PIP="$VENV_DIR/bin/pip"

# Ensure pip is installed (venv might have been created with --without-pip)
if ! "$PY" -m pip --version >/dev/null 2>&1; then
  info "pip not found in venv, installing via ensurepip"
  "$PY" -m ensurepip --upgrade --default-pip >/dev/null 2>&1 || {
    # Fallback: download and install get-pip.py
    info "ensurepip failed, trying get-pip.py"
    tmp_get_pip=$(mktemp)
    trap "rm -f $tmp_get_pip" EXIT
    curl -sSLo "$tmp_get_pip" https://bootstrap.pypa.io/get-pip.py || \
      wget -qO "$tmp_get_pip" https://bootstrap.pypa.io/get-pip.py || \
      die "Failed to download get-pip.py. Please install pip manually or delete $VENV_DIR and re-run."
    "$PY" "$tmp_get_pip" >/dev/null || die "Failed to install pip. Delete $VENV_DIR and re-run, or install pip manually."
  }
fi

info "Upgrading pip"
"$PY" -m pip install -U pip >/dev/null

info "Installing MCP + keyring deps"
# secretstorage helps keyring on many Linux desktops; harmless if not used.
"$PIP" install -U mcp keyring secretstorage >/dev/null

# 2) Ask for credentials once (no [jhu] default shown)
read -r -p "Trac username: " TRAC_USERNAME
[ -n "$TRAC_USERNAME" ] || die "Username cannot be empty."

# Prompt for password (silent)
read -r -s -p "Trac password (will be stored in OS keyring): " TRAC_PASSWORD
echo
[ -n "$TRAC_PASSWORD" ] || die "Password cannot be empty."

# 3) Store username + password in keyring (no plaintext files)
info "Storing credentials in OS keyring (service: $KEYRING_SERVICE)"
"$PY" - <<PY
import keyring
keyring.set_password("${KEYRING_SERVICE}", "${KEYRING_ACCOUNT_USER}", """${TRAC_USERNAME}""")
keyring.set_password("${KEYRING_SERVICE}", "${KEYRING_ACCOUNT_PASS}", """${TRAC_PASSWORD}""")
print("ok")
PY

# 4) Validate via a small XML-RPC call (tries common methods)
info "Validating credentials against: $TRAC_URL"
"$PY" - <<PY
import base64
import sys
import xmlrpc.client
import keyring

url = "${TRAC_URL}"
service = "${KEYRING_SERVICE}"
acc_user = "${KEYRING_ACCOUNT_USER}"
acc_pass = "${KEYRING_ACCOUNT_PASS}"

username = keyring.get_password(service, acc_user)
password = keyring.get_password(service, acc_pass)

if not username or not password:
    print("Keyring lookup failed (missing username/password).", file=sys.stderr)
    sys.exit(2)

class BasicAuthTransport(xmlrpc.client.Transport):
    def send_headers(self, connection, headers):
        auth = base64.b64encode(f"{username}:{password}".encode("utf-8")).decode("ascii")
        connection.putheader("Authorization", f"Basic {auth}")
        super().send_headers(connection, headers)

proxy = xmlrpc.client.ServerProxy(url, transport=BasicAuthTransport(), allow_none=True)

methods_to_try = [
    ("system.getAPIVersion", lambda: proxy.system.getAPIVersion()),
    ("system.listMethods",  lambda: proxy.system.listMethods()),
    ("ticket.getActions",   lambda: proxy.ticket.getActions(1)),  # may fail if ticket 1 doesn't exist / permission
]

last_err = None
for name, fn in methods_to_try:
    try:
        fn()
        print(f"OK: {name}")
        sys.exit(0)
    except Exception as e:
        last_err = f"{name}: {e}"

print("Validation failed. Last error:", last_err, file=sys.stderr)
sys.exit(3)
PY

echo
echo "✅ ready"
echo
echo "Next step:"
echo "Run Claude from repo root."
