---
name: fix-ticket
description: Fix a Trac ticket end-to-end. Reads the ticket, creates a branch from master, implements the fix, pushes, and creates a PR.
disable-model-invocation: true
allowed-tools: Bash, Read, Write, Edit, Glob, Grep, Task, mcp__trac__get_ticket, mcp__trac__get_ticket_changelog, mcp__trac__update_ticket, mcp__gitea__create_pull_request, mcp__gitea__list_branches, mcp__gitea__get_my_user_info
---

# Fix Trac Ticket

You are fixing Trac ticket $ARGUMENTS.

**Important**: The user may pass the ticket number as `29053` or `#29053`. Strip any leading `#` to get the bare number. Use the bare number for API calls (e.g., `get_ticket(29053)`) and the `#`-prefixed form for display text (e.g., `Ticket #29053`).

## Workflow

Follow these steps in order:

### 1. Read the ticket

Use the Trac MCP server to fetch the ticket details and changelog. Understand:
- What the issue is (summary, description)
- Priority and component
- Any discussion or prior work in the changelog

Present a brief summary of the ticket to the user before proceeding.

### 2. Prepare the branch

```
git fetch origin master
git checkout -b CWS/CWS_<number>_<short_description> origin/master
```

Use the convention `CWS/CWS_<ticket-number>_<short_description>` for the branch name, where `<short_description>` is a brief snake_case summary derived from the ticket title (e.g., `CWS/CWS_29053_fix_null_pointer`). Use the bare ticket number (no `#`).

### 3. Plan and implement the fix

- Explore the codebase to understand the relevant code
- Plan the implementation approach and present it to the user for approval before writing code
- Implement the fix following the project coding conventions:
  - Member variables prefixed with `_`
  - Source encoding is ISO-8859-1
  - Test classes use `test.` package prefix
  - Use `TopLogicException` with I18N for user-visible errors
  - Do NOT edit `messages.properties` files directly (they are generated)

### 4. Commit the changes

Create a commit with the message format:
```
Ticket #<number>: <description of the fix>
```

Do NOT include any AI attribution lines.

### 5. Accept the ticket

Before pushing, the ticket must pass the server hook validation. Use `mcp__gitea__get_my_user_info` to determine the current username, then use `mcp__trac__update_ticket` to ensure:
- `status` is `accepted` (or `testing`/`approved`) — the hook rejects `new` tickets
- `owner` is set to the current user's login name
- `component` is set to a valid value matching: `tl`, `tl-addons`, `tl-bpe`, `tl-demo`, `tl-contact`, `tl-help`, `tl-mail`, `tl-migrate`, `tl-reporting`, `tl-search`, `tl-sync`, or `tl-themes`

Skip fields that are already correctly set. If the component is missing or invalid (e.g., `---`), set it to `tl` as the default.

### 6. Push the branch

```
git push -u origin <branch-name>
```

### 7. Create a Pull Request

Use the Gitea MCP server to create a PR:
- **Owner**: `TopLogic`
- **Repo**: `tl-engine-7`
- **Title**: `Ticket #<number>: <description>` (same as commit message)
- **Base**: `master`
- **Head**: the branch name
- **Body**: Include a summary of changes and reference the ticket

### 8. Report back

Provide the user with:
- The branch name
- The PR URL/number
- A summary of what was changed
