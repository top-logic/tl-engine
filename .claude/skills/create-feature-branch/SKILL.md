---
name: create-feature-branch
description: Use when starting new work in a worktree agent that needs its own branch. Creates a local branch from the main workspace's current feature branch. Examples - "create a branch for icon popup work", "start a new branch for this task".
allowed-tools: Bash
---

# Create Feature Branch in Worktree

Creates a local branch for an agent working in a worktree, branching off the main workspace's current feature branch.

## When to Use

- Starting a new piece of work in a worktree (`.worktrees/agent-*`)
- The agent needs its own branch to commit to
- The branch should be based on the main workspace's current development branch

## Workflow

### 1. Find the main workspace branch

The main worktree is the one whose path does NOT contain `.worktrees/`:

```bash
git worktree list | grep -v '\.worktrees/' | head -1
```

Extract the branch name from the output (in brackets at end of line).

### 2. Extract ticket number

Parse the ticket number from the main branch name. The branch follows `CWS/CWS_<number>_...`:

```bash
# Example: CWS/CWS_29108_ui_flexibility → 29108
echo "$MAIN_BRANCH" | grep -oP 'CWS_\K[0-9]+'
```

If the main branch doesn't follow CWS naming, ask the user for the ticket number.

### 3. Create and switch to new branch

The description comes from $ARGUMENTS or from the task context. Convert it to snake_case.

```bash
git checkout -b "CWS/CWS_${TICKET}_${description}" "${MAIN_BRANCH}"
```

This works because all worktrees share the same git refs — the main branch is visible even though it's checked out in a different worktree.

### 4. Confirm

Report:
- New branch name
- Parent branch it was created from
- The commit it points to (short hash)

## Example

```
Main workspace: CWS/CWS_29108_ui_flexibility
Task: "Fix icon popup positioning"
→ Creates: CWS/CWS_29108_fix_icon_popup_positioning
  from: CWS/CWS_29108_ui_flexibility @ abc1234
```
