"""
Shared Trac XML-RPC client for the tl-release skill scripts.

Reuses the very same OS-keyring credentials as mcp-servers/trac-mcp-server.py
(keyring service `tl-engine-trac-mcp`). Must be run with the project venv
(./.venv/bin/python), which has the `keyring` module installed.

NOTE: the OS keyring (Secret Service / DBus) is only reachable when the command
runs OUTSIDE the Claude Code command sandbox. Sandboxed runs fail with
`keyring.errors.NoKeyringError`.
"""
import base64
import os
import sys
import xmlrpc.client

import keyring

TRAC_URL = os.environ.get("TRAC_URL", "http://tl/trac/login/xmlrpc")
SERVICE = os.environ.get("TRAC_KEYRING_SERVICE", "tl-engine-trac-mcp")
ACC_USER = os.environ.get("TRAC_KEYRING_ACCOUNT_USER", "username")
ACC_PASS = os.environ.get("TRAC_KEYRING_ACCOUNT_PASS", "password")


def connect():
    """Return an authenticated xmlrpc ServerProxy for the Trac instance."""
    username = keyring.get_password(SERVICE, ACC_USER)
    password = keyring.get_password(SERVICE, ACC_PASS)
    if not username or not password:
        sys.exit("Error: missing Trac credentials in OS keyring service '%s'." % SERVICE)

    class BasicAuthTransport(xmlrpc.client.Transport):
        def send_headers(self, connection, headers):
            token = base64.b64encode(("%s:%s" % (username, password)).encode("utf-8")).decode("ascii")
            connection.putheader("Authorization", "Basic " + token)
            super().send_headers(connection, headers)

    return xmlrpc.client.ServerProxy(TRAC_URL, transport=BasicAuthTransport(), allow_none=True)
