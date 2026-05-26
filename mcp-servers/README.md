# MCP Servers for Claude Code

MCP server integrations for Trac (tickets), Jenkins (CI/CD), and Gitea (git hosting).

## Setup

Prerequisites: Python 3.7+ with venv, curl or wget. An OS keyring (GNOME Keyring, KWallet, macOS Keychain) is preferred; if none is available the script automatically falls back to a file-based store.

```bash
# Set up all servers (interactive)
./mcp-servers/scripts/setup-mcp.sh

# Or set up a specific server
./mcp-servers/scripts/setup-mcp.sh trac
./mcp-servers/scripts/setup-mcp.sh jenkins
./mcp-servers/scripts/setup-mcp.sh gitea
```

The script creates a Python venv (`.venv`), installs tools, and stores credentials.

### Credential resolution order

Every component (setup script, wrapper scripts, Trac server) reads credentials in this order:

1. **Environment variable** — `TRAC_USERNAME` / `TRAC_PASSWORD`, `JENKINS_USERNAME` / `JENKINS_PASSWORD`, `GITEA_TOKEN`. Useful for CI or scripted/headless setups.
2. **Credentials file** — `~/.config/tl-engine-mcp/credentials.env` (mode 0600). Override path with `TL_MCP_CRED_FILE`.
3. **OS keyring** — service names `tl-engine-trac-mcp`, `tl-engine-jenkins-mcp`, `tl-engine-gitea-mcp`.

The setup script probes the keyring on startup. If it's usable, secrets go to the keyring; otherwise they're written to the credentials file with 0600 permissions. The mode is announced at the top of each run.

Credentials persist across workspaces, so the script detects existing credentials and skips the prompts. To re-enter credentials (e.g., after a password change), use `--force`:

```bash
./mcp-servers/scripts/setup-mcp.sh --force trac
```

After setup, restart Claude Code and run `/mcp` to verify. You can also check with `claude mcp list`.

### Credentials

- **Trac**: username + password
- **Jenkins**: username + API token (generate in Jenkins: User > Configure > API Token)
- **Gitea**: access token (generate in Gitea: Settings > Applications > Generate New Token)

To update credentials, re-run the setup script for that service.

### Service URLs

Override defaults via environment variables:

| Variable | Default |
|----------|---------|
| `TRAC_URL` | `http://tl/trac/login/xmlrpc` |
| `JENKINS_URL` | `http://jenkins:8090/` |
| `GITEA_HOST` | `https://git.top-logic.com` |

## Usage Examples

Once connected, ask Claude Code things like:

- "Show me ticket #29053" (Trac: `get_ticket`)
- "Find open tickets in milestone 7.10.0" (Trac: `search_tickets`)
- "Show the build status of the last CI run" (Jenkins)
- "List open pull requests" (Gitea)

**Note**: Trac uses WikiFormatting (not Markdown) for descriptions/comments: headings `= Title =`, bold `'''text'''`, code blocks `{{{ }}}`.

## Troubleshooting

| Error | Fix |
|-------|-----|
| "Virtual environment not found" | Run `./mcp-servers/scripts/setup-mcp.sh all` |
| "Missing <service> credentials" (error lists env / file / keyring) | Run `./mcp-servers/scripts/setup-mcp.sh --force <service>`, or set the env vars listed in the error |
| `WARN: keyring ... failed: Prompt dismissed` (or similar) | Keyring backend is unusable; setup will fall back to `~/.config/tl-engine-mcp/credentials.env`. To force the file path: `TL_MCP_CRED_FILE=/path/file ./mcp-servers/scripts/setup-mcp.sh <service>` |
| "gitea-mcp: command not found" | Ensure `~/.local/bin` is in your PATH: `export PATH="$PATH:$HOME/.local/bin"` |
| Server not showing in Claude Code | Check `claude mcp list`, verify `.mcp.json` exists, restart Claude Code |

Inspect the active storage backend at any time with:

```bash
.venv/bin/python mcp-servers/credentials.py location   # prints "keyring" or "file:<path>"
.venv/bin/python mcp-servers/credentials.py probe      # prints "yes" or "no"
```

## File Locations

| File | Purpose |
|------|---------|
| `.mcp.json` | Server configuration (shared via git) |
| `mcp-servers/trac-mcp-server.py` | Trac server implementation |
| `mcp-servers/credentials.py` | Credential resolver (env > file > keyring) |
| `mcp-servers/scripts/setup-mcp.sh` | Setup script |
| `mcp-servers/scripts/mcp-*-wrapper.sh` | Credential wrapper scripts |
| `~/.config/tl-engine-mcp/credentials.env` | File-based credential store (mode 0600, fallback when keyring is unusable) |
| `.venv/` | Python venv (local, not in git) |
