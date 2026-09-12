# MCP Servers for Claude Code

The Trac (tickets), Jenkins (CI/CD), and Gitea (git hosting) MCP integrations
are provided by the **`tl-dev` Claude Code plugin**, installed once per machine.
Every checkout and worktree picks them up automatically.

- Plugin repository: **https://git.top-logic.com/TopLogic/tl-dev-mcp.git**

## One-time setup (per machine)

```bash
bash <(curl -fsSL https://git.top-logic.com/TopLogic/tl-dev-mcp/raw/branch/main/install.sh)
```

The prerequisites are the `claude` CLI and `curl` (or `wget`); `uv` is installed
automatically if missing. This single step:

1. registers the plugin's marketplace and installs the plugin at **user** scope,
   making the three servers available in **every** project and worktree on this
   machine, and
2. prompts for your Trac / Jenkins / Gitea credentials (stored in the OS
   keyring, with a file fallback).

Then restart Claude Code and run `/mcp` (or `claude mcp list`) to verify the
connections.

The equivalent manual form:

```bash
claude plugin marketplace add https://git.top-logic.com/TopLogic/tl-dev-mcp.git
claude plugin install tl-dev@toplogic-internal
uvx --from git+https://git.top-logic.com/TopLogic/tl-dev-mcp.git tl-mcp-setup all
```

## Updating

```bash
claude plugin update tl-dev
```

Restart Claude Code to apply it. The plugin carries the servers, the release
tooling and the skills, so they update together — and a single installation
serves every checkout and worktree.

## Release tooling

The plugin also provides the `tl-release` command and the `tl-release` skill,
which perform the Trac bookkeeping of a TopLogic release (closing the release
tickets, the milestone and its changelog queries, the `relatedmilestones`
marking, the migration keywords). Run them from the engine checkout that is
being released.

See the plugin repository's `README.md` for full documentation (credential
resolution order, service URLs, migrating from the earlier package-based
installation, troubleshooting, and development setup).
