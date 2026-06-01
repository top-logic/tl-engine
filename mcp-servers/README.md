# MCP Servers for Claude Code

The Trac (tickets), Jenkins (CI/CD), and Gitea (git hosting) MCP integrations
are provided by the `tl-dev-mcp` package, installed once per machine and
registered globally in Claude Code. Every checkout and worktree picks them up
automatically.

- Package repository: **https://git.top-logic.com/TopLogic/tl-dev-mcp.git**

## One-time setup (per machine)

```bash
bash <(curl -fsSL https://git.top-logic.com/TopLogic/tl-dev-mcp/raw/branch/main/install.sh)
```

The only prerequisite is `curl` (or `wget`); if neither `uv` nor `pipx` is
present, the script installs `uv` automatically. This single step:

1. installs the `tl-trac-mcp` / `tl-jenkins-mcp` / `tl-gitea-mcp` tools (via
   `uv` or `pipx`),
2. prompts for your Trac / Jenkins / Gitea credentials (stored in the OS
   keyring, with a file fallback), and
3. registers the three servers in Claude Code at **user scope**
   (`claude mcp add --scope user`), making them available in **every** project
   and worktree on this machine.

Ensure `~/.local/bin` is on your `PATH`. Then restart Claude Code and run `/mcp`
(or `claude mcp list`) to verify the connections.

The equivalent manual form:

```bash
uv tool install git+https://git.top-logic.com/TopLogic/tl-dev-mcp.git
tl-mcp-setup --register all
```

## Updating

```bash
uv tool upgrade tl-dev-mcp      # or: pipx upgrade tl-dev-mcp
```

The servers are installed and registered once per machine, so an update applies
to every checkout and worktree at once.

See the package repository's `README.md` for full documentation (credential
resolution order, service URLs, project-scoped `.mcp.json` as an alternative to
global registration, troubleshooting, and development setup).
