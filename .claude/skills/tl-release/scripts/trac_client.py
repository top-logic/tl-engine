"""
Shared Trac XML-RPC client for the tl-release skill scripts.

Reuses the very same credentials as the `trac` MCP server (`tl-trac-mcp` from the
`tl-dev-mcp` package) and resolves them in the same order:

  1. environment variables `TRAC_USERNAME` / `TRAC_PASSWORD`
  2. the credentials file `$TL_MCP_CRED_FILE`
     (default `~/.config/tl-engine-mcp/credentials.env`), keys
     `<service>__username` / `<service>__password`
  3. the OS keyring (Secret Service), service `tl-engine-trac-mcp`

Run with the project venv (`./.venv/bin/python`), which has the `keyring` module
installed.

NOTE: the OS keyring (Secret Service / DBus) is only reachable when the command
runs OUTSIDE the Claude Code command sandbox, and only while the login collection
is unlocked. Sandboxed runs fail with `keyring.errors.NoKeyringError`, a locked or
absent collection with `keyring.errors.InitError`. The credentials file covers both
cases, so a headless run needs no keyring at all.
"""
import base64
import os
import sys
import xmlrpc.client
from pathlib import Path

TRAC_URL = os.environ.get("TRAC_URL", "http://tl/trac/login/xmlrpc")
SERVICE = os.environ.get("TRAC_KEYRING_SERVICE", "tl-engine-trac-mcp")
ACC_USER = os.environ.get("TRAC_KEYRING_ACCOUNT_USER", "username")
ACC_PASS = os.environ.get("TRAC_KEYRING_ACCOUNT_PASS", "password")

CRED_FILE = Path(os.environ.get(
    "TL_MCP_CRED_FILE", "~/.config/tl-engine-mcp/credentials.env")).expanduser()


def _from_file(key):
    """Value of `<SERVICE>__<key>` in the credentials file, or `None`."""
    if not CRED_FILE.exists():
        return None
    wanted = "%s__%s" % (SERVICE, key)
    for line in CRED_FILE.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        name, value = line.split("=", 1)
        if name.strip() == wanted:
            return value.strip()
    return None


def _from_keyring(key):
    """Value of `<key>` in the keyring service, or `None` if unreachable."""
    try:
        import keyring
        return keyring.get_password(SERVICE, key)
    except Exception:
        return None


def _credential(key, env_var):
    return os.environ.get(env_var) or _from_file(key) or _from_keyring(key)


def connect():
    """Return an authenticated xmlrpc ServerProxy for the Trac instance."""
    username = _credential(ACC_USER, "TRAC_USERNAME")
    password = _credential(ACC_PASS, "TRAC_PASSWORD")
    if not username or not password:
        sys.exit("Error: missing Trac credentials. Checked $TRAC_USERNAME/$TRAC_PASSWORD, "
                 "credentials file '%s' and keyring service '%s'." % (CRED_FILE, SERVICE))

    class BasicAuthTransport(xmlrpc.client.Transport):
        def send_headers(self, connection, headers):
            token = base64.b64encode(("%s:%s" % (username, password)).encode("utf-8")).decode("ascii")
            connection.putheader("Authorization", "Basic " + token)
            super().send_headers(connection, headers)

    return xmlrpc.client.ServerProxy(TRAC_URL, transport=BasicAuthTransport(), allow_none=True)
