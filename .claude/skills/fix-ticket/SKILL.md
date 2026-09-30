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

### 2. Make the ticket changelog-ready

The release changelog is generated from ticket summaries and descriptions, so both must be understandable to a reader who knows neither the code nor the internal discussion. Review the ticket and rewrite it if needed:

- **Summary**: Must state the user-visible problem on its own. A class or method name as summary (e.g. "SelectSubtree bricht zu früh ab") is useless in a changelog — name the feature and the misbehavior instead (e.g. "Teilbaum-Auswahl endet zu früh, wenn die unterste Ebene vom Ebenen-Filter nicht mitgezählt wird").
- **Description**: Must begin with the *context* — which feature/command is affected and what it does, from the user's perspective — followed by the observable misbehavior and its consequence. Technical details (affected classes, root cause, solution proposal) come after that, not first.
- **Preserve** the reporter's technical content (analysis, code proposals) — restructure around it, don't drop it.
- Keep the ticket's language (typically German) and Trac WikiFormatting.

Update via `mcp__trac__update_ticket` with the full new text in `attributes` (`{"summary": ..., "description": ...}` — top-level fields are silently ignored), then verify with `get_ticket`. Skip this step only if summary and description already meet the bar.

### 3. Prepare the branch

```
git fetch origin master
git checkout -b CWS/CWS_<number>_<short_description> origin/master
```

Use the convention `CWS/CWS_<ticket-number>_<short_description>` for the branch name, where `<short_description>` is a brief snake_case summary derived from the ticket title (e.g., `CWS/CWS_29053_fix_null_pointer`). Use the bare ticket number (no `#`).

### 4. Plan and implement the fix

- Explore the codebase to understand the relevant code
- Plan the implementation approach and present it to the user for approval before writing code
- Implement the fix following the project coding conventions:
  - Member variables prefixed with `_`
  - Source encoding is ISO-8859-1
  - Test classes use `test.` package prefix
  - Use `TopLogicException` with I18N for user-visible errors
  - Do NOT edit `messages.properties` files directly (they are generated)

### 5. Commit the changes

Create a commit with the message format:
```
Ticket #<number>: <description of the fix>
```

Do NOT include any AI attribution lines.

### 6. Accept the ticket

Before pushing, the ticket must pass the server hook validation. Use `mcp__gitea__get_my_user_info` to determine the current username, then use `mcp__trac__update_ticket` to ensure:
- `status` is `accepted` (or `testing`/`approved`) — the hook rejects `new` tickets
- `owner` is set to the current user's login name
- `component` is set to a valid value matching: `tl`, `tl-addons`, `tl-bpe`, `tl-demo`, `tl-contact`, `tl-help`, `tl-mail`, `tl-migrate`, `tl-reporting`, `tl-search`, `tl-sync`, or `tl-themes`

Skip fields that are already correctly set. If the component is missing or invalid (e.g., `---`), set it to `tl` as the default.

### 7. Push the branch

```
git push -u origin <branch-name>
```

### 8. Create a Pull Request

Use the Gitea MCP server to create a PR:
- **Owner**: `TopLogic`
- **Repo**: `tl-engine-7`
- **Title**: `Ticket #<number>: <description>` (same as commit message)
- **Base**: `master`
- **Head**: the branch name
- **Body**: Include a summary of changes and reference the ticket

### 9. Report back

Provide the user with:
- The branch name
- The PR URL/number
- A summary of what was changed
