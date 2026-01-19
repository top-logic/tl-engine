# Trac MCP Server Setup

This MCP server allows Claude Code to interact with the Trac ticket system at `http://tl/trac/`.

## Prerequisites

1. **Python 3.7+** with the MCP SDK installed
2. **Credentials in ~/.netrc** for the 'tl.bos.local' machine

## Installation

### Step 1: Install Python Dependencies

```bash
pip install mcp
```

### Step 2: Verify ~/.netrc Configuration

Your `~/.netrc` file should contain credentials for the 'tl.bos.local' machine:

```
machine tl.bos.local
  login your-username
  password your-password
```

Make sure the file has proper permissions:

```bash
chmod 600 ~/.netrc
```

### Step 3: Add to Claude Code (Team-Shared)

The MCP server script is located in `mcp-servers/trac-mcp-server.py` in the repository.

Add it with **project scope** so it's shared with the whole team via `.mcp.json`:

```bash
cd /path/to/tl-engine
claude mcp add --scope project trac -- python3 mcp-servers/trac-mcp-server.py
```

This creates a `.mcp.json` file in the repository root that team members will automatically use when they work in this repository.

**Alternative: Personal installation only (not shared)**

If you prefer to use it only locally without sharing:

```bash
claude mcp add trac -- python3 mcp-servers/trac-mcp-server.py
```

### Step 4: Test the Script (Optional)

You can test the script manually to ensure it connects properly:

```bash
python3 mcp-servers/trac-mcp-server.py
```

Press Ctrl+C to exit after verifying it starts without errors.

### Step 5: Verify Installation

```bash
claude mcp list
```

You should see 'trac' in the list of configured MCP servers.

In Claude Code, type `/mcp` to check the server status.

## Available Tools

Once installed, Claude Code will have access to these Trac tools:

- **get_ticket** - Get complete details of a ticket by number
  - Example: "Show me ticket #29053"

- **search_tickets** - Search for tickets using Trac query syntax
  - Example: "Find all tickets in milestone 7.10.0 with status=new"
  - Example: "Search for tickets with component=BPE"

- **get_ticket_changelog** - Get the complete change history for a ticket
  - Example: "Show me the changelog for ticket #29053"

- **create_ticket** - Create a new Trac ticket
  - Example: "Create a ticket for fixing null formatting in PDFs"

- **update_ticket** - Update an existing ticket (change status, add comment)
  - Example: "Close ticket #29053 with resolution fixed"

- **get_milestones** - List all milestones
  - Example: "What milestones do we have?"

- **get_components** - List all components
  - Example: "List all Trac components"

## Usage Examples

Once configured, you can ask Claude Code:

- "What's ticket #29053 about?"
- "Find all open tickets assigned to me"
- "Show me all tickets in the BPE component"
- "Create a ticket for the bug I just found"
- "Add a comment to ticket #29053 saying the fix is ready"
- "What milestones are we tracking?"

## Troubleshooting

### Error: "No credentials found for 'tl.bos.local' in ~/.netrc"

Add the credentials to your `~/.netrc` file:

```bash
echo "machine tl.bos.local" >> ~/.netrc
echo "  login your-username" >> ~/.netrc
echo "  password your-password" >> ~/.netrc
chmod 600 ~/.netrc
```

### Error: "MCP SDK not installed"

Install the MCP Python package:

```bash
pip install mcp
```

### Error: "XML-RPC Error: TICKET_VIEW permission required"

Check that:
1. Your credentials in ~/.netrc are correct
2. Your Trac user has the necessary permissions
3. The Trac XML-RPC plugin is enabled at http://tl/trac/login/xmlrpc

### MCP server not showing up in Claude Code

1. Verify installation: `claude mcp list`
2. Check server status in Claude Code: `/mcp`
3. Try removing and re-adding:
   ```bash
   claude mcp remove trac
   claude mcp add --scope project trac -- python3 mcp-servers/trac-mcp-server.py
   ```

## Removing the MCP Server

```bash
claude mcp remove trac
```
