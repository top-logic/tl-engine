#!/usr/bin/env python3
"""Credential storage for TopLogic MCP servers.

Resolution order when reading a credential:
  1. Environment variable (when an --env / env_var name is passed)
  2. Credentials file at $TL_MCP_CRED_FILE (default: ~/.config/tl-engine-mcp/credentials.env)
  3. OS keyring (via the `keyring` package)

The credentials file is mode 0600. It uses `service__key=value` lines, one per
secret. This is the fallback path for headless/container environments where the
OS keyring is unavailable.

Usage from shell:
    python mcp-servers/credentials.py get  <service> <key> [--env VAR]
    python mcp-servers/credentials.py set  <service> <key> <value> [--prefer-file]
    python mcp-servers/credentials.py probe       # prints "yes" / "no"
    python mcp-servers/credentials.py location    # prints "keyring" or "file:<path>"
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path


CRED_FILE = Path(os.environ.get(
    "TL_MCP_CRED_FILE",
    str(Path.home() / ".config" / "tl-engine-mcp" / "credentials.env"),
))


def _read_file() -> dict[str, str]:
    if not CRED_FILE.exists():
        return {}
    creds: dict[str, str] = {}
    for line in CRED_FILE.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, _, v = line.partition("=")
        creds[k.strip()] = v
    return creds


def _write_file(creds: dict[str, str]) -> None:
    CRED_FILE.parent.mkdir(parents=True, exist_ok=True)
    try:
        CRED_FILE.parent.chmod(0o700)
    except OSError:
        pass
    CRED_FILE.write_text("".join(f"{k}={v}\n" for k, v in sorted(creds.items())))
    CRED_FILE.chmod(0o600)


def _kr_get(service: str, key: str) -> str:
    try:
        import keyring
        return keyring.get_password(service, key) or ""
    except Exception as e:
        print(f"WARN: keyring read failed ({service}/{key}): {e}", file=sys.stderr)
        return ""


def _kr_set(service: str, key: str, value: str) -> bool:
    try:
        import keyring
        keyring.set_password(service, key, value)
        return True
    except Exception as e:
        print(f"WARN: keyring write failed ({service}/{key}): {e}", file=sys.stderr)
        return False


def _file_key(service: str, key: str) -> str:
    return f"{service}__{key}"


def get(service: str, key: str, env_var: str | None = None) -> str:
    """Resolve credential value or empty string if not found."""
    if env_var:
        v = os.environ.get(env_var)
        if v:
            return v
    v = _read_file().get(_file_key(service, key))
    if v:
        return v
    return _kr_get(service, key)


def set_(service: str, key: str, value: str, prefer_file: bool = False) -> str:
    """Store credential and return a human-readable location string."""
    if not prefer_file and _kr_set(service, key, value):
        return "keyring"
    creds = _read_file()
    creds[_file_key(service, key)] = value
    _write_file(creds)
    return f"file:{CRED_FILE}"


def keyring_works() -> bool:
    """Probe whether the OS keyring can write+read."""
    try:
        import keyring
        sentinel = "tl-engine-mcp-probe"
        keyring.set_password(sentinel, "probe", "ok")
        ok = keyring.get_password(sentinel, "probe") == "ok"
        try:
            keyring.delete_password(sentinel, "probe")
        except Exception:
            pass
        return ok
    except Exception:
        return False


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    sub = p.add_subparsers(dest="cmd", required=True)

    g = sub.add_parser("get", help="Print credential value (empty if missing).")
    g.add_argument("service")
    g.add_argument("key")
    g.add_argument("--env", dest="env_var", default=None,
                   help="Environment variable to check first.")

    s = sub.add_parser("set", help="Store credential. Tries keyring first, falls back to file.")
    s.add_argument("service")
    s.add_argument("key")
    s.add_argument("value")
    s.add_argument("--prefer-file", action="store_true",
                   help="Skip keyring and write to the credentials file.")

    sub.add_parser("probe", help="Print 'yes' if keyring is usable, else 'no'.")
    sub.add_parser("location", help="Print where credentials would be stored ('keyring' or 'file:<path>').")

    args = p.parse_args()
    if args.cmd == "get":
        print(get(args.service, args.key, args.env_var))
    elif args.cmd == "set":
        where = set_(args.service, args.key, args.value, prefer_file=args.prefer_file)
        print(where)
    elif args.cmd == "probe":
        print("yes" if keyring_works() else "no")
    elif args.cmd == "location":
        print("keyring" if keyring_works() else f"file:{CRED_FILE}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
