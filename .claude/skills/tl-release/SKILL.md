---
name: tl-release
description: Use when creating a TopLogic release — especially an alpha / interim release of the tl-engine. Covers the pre-release ticket check, creating and marking the release milestone in Trac, triggering the Jenkins release job, and closing the milestone.
---

# Creating a TopLogic Release

## Overview

A TopLogic release of `tl-engine` is produced by the Jenkins job **`Release_Auto`**,
which runs the `maven-release-plugin` (`release:prepare` + `release:perform`). The
job bumps the POM versions, creates a git tag `TL_<version>`, writes two release
commits onto `master`, and deploys the artifacts.

This skill covers an **alpha release** (a *Zwischen-Release* / interim release),
e.g. `8.0.0-alpha4`. The release worker is responsible for the bookkeeping in
Trac around the job — the job itself only does the Maven/git part.

## When to Use

- Cutting a new alpha/interim release of `tl-engine` (`8.0.0-alphaN`).
- Performing the pre-release ticket hygiene checks.
- Creating and closing the Trac milestone that documents a release.

## Versioning

- The development version on `master` is `X.Y.Z-SNAPSHOT` (root `pom.xml`,
  property `tl.version`).
- Release tags are `TL_<version>`, e.g. `TL_8.0.0-alpha3`.
- The **next** alpha version increments the `alphaN` suffix. Find the current
  highest tag: `git tag --list 'TL_8.0.0-alpha*' --sort=-v:refname | head -1`.

## Scripts

All scripts live in `scripts/` next to this file. The Python scripts need the
project venv (`./.venv/bin/python`) and authenticate to Trac via the OS keyring.

> **Sandbox:** the keyring (OS Secret Service) is only reachable when a command
> runs **outside** the Claude Code command sandbox. Run the Python scripts with
> `dangerouslyDisableSandbox: true`, otherwise they fail with `NoKeyringError`.

| Script | Purpose |
|--------|---------|
| `list-release-tickets.sh [<since-tag>]` | Unique Trac ticket numbers in commits since the last `TL_*` tag. |
| `trac_client.py` | Shared Trac XML-RPC connection (keyring auth). Not run directly. |
| `trac-milestone.py {get\|create\|update} <name> ...` | Inspect/create/update a Trac milestone (the MCP server cannot create milestones). |
| `mark-release-tickets.py <milestone> [--apply]` | Set the `relatedmilestones` field on release tickets. Reads ticket IDs from stdin/args. |

## Process

Run the steps **in this order** — step 3 marks tickets with the milestone, so
the milestone (step 2) must already exist.

### Step 1 — Pre-release ticket check

**Rule: every ticket implemented on `master` since the last release must be
`closed` in Trac.** A release must not ship a half-finished ticket.

1. List the candidate tickets:
   ```bash
   .claude/skills/tl-release/scripts/list-release-tickets.sh
   ```
2. Query their status in Trac in one call (MCP `search_tickets`):
   ```
   id=29242|29243|29244|...&max=0
   ```
3. Evaluate each status. The ticket workflow is
   `new → accepted → testing → approved → closed`.
   - `closed` → fine.
   - `approved` → a PR review was positive, **but that is not enough**. The
     ticket may still have open work. Check that **all PRs of the ticket are
     closed**:
     - List open PRs of the repo: MCP `list_repo_pull_requests`, owner
       `TopLogic`, repo `tl-engine-7`, state `open`.
     - PR titles follow `Ticket #<number>: …`. If **no open PR** references the
       ticket, all its PRs are closed → the release worker closes the ticket:
       MCP `update_ticket` with `{"status": "closed", "resolution": "fixed"}`
       and an explanatory comment.
     - If an open PR **does** reference a release ticket → **blocker**. Do not
       release until it is merged or excluded.
   - `new` / `accepted` / `assigned` / `testing` / `reopened` → not finished →
     blocker.

Do not proceed until all release tickets are `closed`.

### Step 2 — Create the release milestone

The milestone must exist before tickets can be marked with it (step 3).

```bash
./.venv/bin/python .claude/skills/tl-release/scripts/trac-milestone.py \
    create TL_8.0.0-alpha4 --release-description --due 2026-05-21T16:00
```

`--release-description` generates the standard body:

```
[[TicketQuery(relatedmilestones~=milestone:TL_8.0.0-alpha4)]]

== Migration ==
[[TicketQuery(relatedmilestones~=milestone:TL_8.0.0-alpha4,keywords~=RequiresCodeMigration)]]
```

> **Query operator:** the TicketQuery uses **`~=` (contains)**, not `=`
> (exact). A ticket that shipped in several releases has a multi-valued
> `relatedmilestones` field; `=` only matches a single-valued field and would
> silently drop such tickets. (Older milestones used `=` — fix them with
> `trac-milestone.py update <name> --release-description`.)

`completed` is set in step 5, after the build succeeds.

### Step 3 — Mark the release tickets with the milestone

TopLogic documents which tickets shipped in a release via the **custom Trac
field `relatedmilestones`** — a comma-separated list of `milestone:<name>`
entries, queried by the milestone description. The standard `milestone` field
is the *roadmap* milestone and is left untouched.

Setting `relatedmilestones` is done by **whoever runs the release**:

```bash
.claude/skills/tl-release/scripts/list-release-tickets.sh \
  | ./.venv/bin/python .claude/skills/tl-release/scripts/mark-release-tickets.py \
        TL_8.0.0-alpha4 --apply
```

The script **appends** `milestone:TL_8.0.0-alpha4` and preserves any existing
value — a ticket can belong to several releases (e.g.
`milestone:TL_8.0.0-alpha3, milestone:TL_8.0.0-alpha4`). Run without `--apply`
first for a dry-run.

### Step 4 — Trigger the release build

Trigger the Jenkins job `Release_Auto` with `buildWithParameters`:

| Parameter | Value | Notes |
|-----------|-------|-------|
| `VERSION` | `8.0.0-alpha4` | **Must be explicit.** Empty → the job derives `8.0.0` from the SNAPSHOT version and would cut a *final* release. |
| `BRANCH` | `master` | |
| `REPO` | `https://git.top-logic.com/TopLogic/tl-engine-7.git` | Job default; used for the engine. |
| `BUILD_MODULE` | `.` | |
| `NEXT_VERSION` | `8.0.0-SNAPSHOT` | **Must be set for an alpha.** Otherwise the release plugin sets the next dev version to `8.0.0-alpha5-SNAPSHOT`; an alpha must keep `master` at `8.0.0-SNAPSHOT`. |

MCP call: `build_item`, `fullname=Release_Auto`, `build_type=buildWithParameters`.

The build runs `clean install` + `release:perform`, takes **~15 minutes**, and
on success creates the tag `TL_8.0.0-alpha4`, two commits on `master`
(`prepare release` / `prepare for next development iteration`) and deploys the
artifacts.

> **Jenkins auth gotcha:** triggering a build (a POST) requires a Jenkins
> **API token** in the keyring, not a password. With a password, Jenkins
> rejects the POST with `403 No valid crumb` (CSRF). If you hit this, store an
> API token: generate one at `http://jenkins:8090/user/<user>/configure` →
> *API Token*, then run `./mcp-servers/scripts/setup-mcp.sh --update jenkins`
> and reload the MCP server.

### Step 5 — Close the milestone

After the build has finished successfully, mark the milestone completed with
the build's finish time:

```bash
./.venv/bin/python .claude/skills/tl-release/scripts/trac-milestone.py \
    update TL_8.0.0-alpha4 --completed 2026-05-21T11:00
```

## Real release vs. interim (alpha) release

| Aspect | Interim release (alpha) | Real release |
|--------|-------------------------|--------------|
| Milestone | Created reactively for this release (steps 2–5); not a roadmap item | A planned roadmap milestone that exists long in advance |
| `NEXT_VERSION` | Stays `X.Y.Z-SNAPSHOT` | Bumped to the next dev version |
| `VERSION` | Explicit `X.Y.Z-alphaN` | The `X.Y.Z` the SNAPSHOT points to |

## Key facts

- The Trac MCP server can list milestones but **cannot create them** and does
  not surface `relatedmilestones` specially — hence the `trac-milestone.py` /
  `mark-release-tickets.py` helpers calling Trac XML-RPC directly.
- "Issue comments" / ticket data come from **Trac**, not Gitea. PRs come from
  **Gitea** (`TopLogic/tl-engine-7`).
- The release commits on `master` are authored by `Jenkins` and reference no
  ticket — they do not affect the release ticket set.
