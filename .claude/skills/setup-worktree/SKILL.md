---
name: setup-worktree
description: Prepare a fresh git worktree for building TopLogic. Configures an isolated Maven repository with a read-only tail to the global repo so no full rebuild or re-download is needed. Examples - "set up this worktree", "prepare the worktree build environment", "initialize the worktree".
allowed-tools: Bash, Read
---

# Initial Worktree Setup

Prepares a freshly created git worktree so it can build TopLogic immediately,
**without a full build and without re-downloading dependencies**.

## How it works

Each worktree gets its own Maven local repository for build isolation:

- **Writes** go to the worktree-local repo (`<worktree>/.m2/repository`).
  Branch-specific SNAPSHOT builds never pollute the shared global repo, and
  parallel worktrees never clash.
- **Reads** fall through to the global repo (`~/.m2/repository`) as a
  **read-only tail** (`maven.repo.local.tail`). All external dependencies and
  every unchanged TopLogic module resolve from there.

Result: the worktree repo starts empty; only modules you actually rebuild here
land in it and shadow the global copy. No full build, no network.

The read-only tail requires **Maven 3.9.0+**. Older Maven ignores the tail line
and falls back to a fully isolated (empty) repo that needs a full build.

## When to use

- A new worktree was just created and has no `.mvn/maven.config` yet.
- Build commands in the worktree fail to find artifacts, or you want to verify
  the isolated-repo setup.

Branch creation is a separate concern — see the `create-feature-branch` skill.

## Workflow

### 1. Verify this is a worktree

```bash
git rev-parse --git-dir
```

A linked worktree reports a path containing `/worktrees/`; the main checkout
does not. Run this skill only in a worktree — the main checkout must use the
global repo directly.

### 2. Write the Maven configuration

```bash
.claude/scripts/local-m2.sh
```

This writes `.mvn/maven.config` with the worktree-local `maven.repo.local` and
the read-only `maven.repo.local.tail` pointing at `~/.m2/repository`. Override
the tail location with the `TL_GLOBAL_M2` environment variable if needed.

Read the script output: it warns if the active Maven is older than 3.9.0 or if
the global repo is missing. Resolve both before continuing.

### 3. Verify Maven and the tail resolve offline

```bash
mvn --version            # must report 3.9.0 or newer
mvn -B -o -q validate    # offline reactor validation against the tail
```

If `-o validate` succeeds, every parent POM and build plugin resolves from the
global repo through the tail — the worktree is build-ready.

If Maven is too old, activate a 3.9.x install (in this environment:
`~/tools/apache-maven-3.9.10`, wired into `~/.bashrc`).

### 4. Report

Tell the user:
- The `maven.repo.local` and `maven.repo.local.tail` paths in use.
- The active Maven version.
- That the worktree is build-ready (no full build performed).

## Building in the worktree

- A fresh worktree branched off the main feature branch at the same commit
  needs **no build** — the tail serves every artifact.
- As you change modules, rebuild only those:
  `mvn install -pl <module-dir>` (add `-am` to also rebuild changed dependencies).
  The rebuilt artifacts land in the worktree-local repo and shadow the tail.
- **Do not run a full `mvn install`** during setup, and **do not run
  `rebuild-stale.sh`** on a fresh worktree: `git worktree add` sets every file
  mtime to "now", so mtime-based staleness detection would flag every module
  and trigger a full build. `rebuild-stale.sh` is for in-place branch switches
  within an existing worktree, not for initial setup.
