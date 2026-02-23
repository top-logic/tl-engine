# MCP Servers for Claude Code

MCP server integrations for Trac (tickets), Jenkins (CI/CD), and Gitea (git hosting).

## Setup

Prerequisites: Python 3.7+ with venv, curl or wget, OS keyring (GNOME Keyring, KWallet, macOS Keychain).

```bash
# Set up all servers (interactive)
./mcp-servers/scripts/setup-mcp.sh

# Or set up a specific server
./mcp-servers/scripts/setup-mcp.sh trac
./mcp-servers/scripts/setup-mcp.sh jenkins
./mcp-servers/scripts/setup-mcp.sh gitea
```

The script creates a Python venv (`.venv`), installs tools, and stores credentials in your OS keyring.

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
| "Missing credentials in OS keyring" | Re-run `./mcp-servers/scripts/setup-mcp.sh <service>` |
| "gitea-mcp: command not found" | Ensure `~/.local/bin` is in your PATH: `export PATH="$PATH:$HOME/.local/bin"` |
| Server not showing in Claude Code | Check `claude mcp list`, verify `.mcp.json` exists, restart Claude Code |

## File Locations

| File | Purpose |
|------|---------|
| `.mcp.json` | Server configuration (shared via git) |
| `mcp-servers/trac-mcp-server.py` | Trac server implementation |
| `mcp-servers/scripts/setup-mcp.sh` | Setup script |
| `mcp-servers/scripts/mcp-*-wrapper.sh` | Credential wrapper scripts |
| `.venv/` | Python venv (local, not in git) |
