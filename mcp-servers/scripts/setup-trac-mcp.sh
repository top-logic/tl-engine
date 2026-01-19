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
