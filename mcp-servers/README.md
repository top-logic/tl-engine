# MCP Servers for Claude Code

The Trac (tickets), Jenkins (CI/CD), and Gitea (git hosting) MCP integrations
are **no longer embedded in this repository**. They now live in a separate,
installable package so they can be installed **once per machine** and shared by
every checkout and worktree — instead of requiring a per-checkout `.venv` and a
setup run in each clone.

- Package repository: **http://tl.bos.local:3000/TopLogic/tl-dev-mcp.git**

The project's [`.mcp.json`](../.mcp.json) (committed) references the installed
commands by name (`tl-trac-mcp`, `tl-jenkins-mcp`, `tl-gitea-mcp`), so it no
longer depends on any checkout-local path.

## One-time setup (per machine)

```bash
# Install the servers (puts tl-trac-mcp / tl-jenkins-mcp / tl-gitea-mcp on PATH)
uv tool install git+http://tl.bos.local:3000/TopLogic/tl-dev-mcp.git
# or: pipx install git+http://tl.bos.local:3000/TopLogic/tl-dev-mcp.git

# Store your Trac / Jenkins / Gitea credentials (keyring, with file fallback)
tl-mcp-setup
```

Ensure `~/.local/bin` is on your `PATH`. Then restart Claude Code and run `/mcp`
(or `claude mcp list`) to verify the connections.

## Updating

```bash
uv tool upgrade tl-dev-mcp      # or: pipx upgrade tl-dev-mcp
```

Because the servers are installed once per machine, the update applies to every
checkout and worktree at once — there is nothing to port into individual clones.

See the package repository's `README.md` for full documentation (credential
resolution order, service URLs, troubleshooting, development setup).
