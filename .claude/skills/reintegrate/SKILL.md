---
name: reintegrate
description: Use when a worktree agent's work is finished and should be folded back into the shared feature/integration branch. Replays only this branch's own commits onto the integration branch, fast-forwards it, and renames the local branch to the worktree folder name to mark the work done. Examples - "re-integrate this work", "bring my changes back to the integration branch and mark done", "reintegrate to CWS/CWS_29108_integration".
allowed-tools: Bash, Read
---

# Re-integrate Worktree Work

Brings a worktree agent's finished work back into the shared feature/integration
branch and marks the worktree's work as done.

Two things happen:

1. **Replay only this branch's own commits** onto the integration branch and
   **fast-forward** the integration branch to the result. Fast-forward (not a
   merge commit) keeps the integration branch linear.
2. **Rename the local branch** to the worktree's folder name (e.g. `agent-2`) so
   it is obvious the branch has been integrated and should not be committed to
   further.

## When to use

- An agent in a worktree (`/home/claude/devel/agent-N`) has finished a unit of
  work, it is committed, and it should land on the shared integration branch.
- The integration branch must stay linear (fast-forward only).

Do **not** use this to push, open a PR, or merge into `master` — it operates on
local branches only. Pushing is a separate, explicitly-requested step.

## Core idea: replay *only your own* commits

A worktree branch is created off the integration branch (see
`create-feature-branch`). In the clean case its commits sit directly on top of
the integration tip, and a plain rebase replays exactly them.

The hazard is a **drifted base**: the branch was created off a *sibling* branch
that has since diverged, so `<integration>..HEAD` contains not only your commits
but also unrelated commits inherited from that sibling. Replaying that whole
range would drag another agent's work onto the integration branch. So always
identify **the parent of your own first commit** as the fork point and rebase
`--onto` the integration branch from there — never assume the merge-base is the
fork point.

## Workflow

### 1. Establish the facts

```bash
cd "<this-worktree>"
git status --porcelain        # MUST be clean; stop and surface anything uncommitted
SELF=$(git rev-parse --abbrev-ref HEAD)            # current branch
DONE=$(basename "$(git rev-parse --show-toplevel)") # worktree folder → "done" branch name
git worktree list             # see every worktree and the branch each holds
```

The working tree must be clean. If not, stop and report — never stash or discard
another turn's work to force a reintegration.

### 2. Determine the integration target

The target is the **shared integration branch**, normally the branch checked out
in the *main* worktree (first line of `git worktree list`). **Verify it**, do not
blindly trust it: a main worktree can sit on the wrong branch (a sibling agent
"running in the wrong workspace"). If the user named a target explicitly, use
that. Confirm the resolved target name before any history rewrite.

```bash
TARGET="CWS/CWS_29108_integration"   # explicit, or the verified main-worktree branch
git rev-parse --verify "$TARGET"     # must exist
```

### 3. Identify your own commits and the fork point

```bash
git log --oneline --left-right "$TARGET"...HEAD   # < = on target only, > = on your branch only
git log --oneline "$TARGET"..HEAD                 # everything that would replay by default
```

Read the `>` list. The commits that are **yours** (your ticket, your work) are
what should land. If the list is *only* your commits, the fork point is the
merge-base and a plain rebase is safe. If it also contains **foreign** commits
(drifted base), find your **first** commit and take its parent:

```bash
FORK=<parent-sha-of-your-first-commit>   # e.g. 3b06129cdd in the #29343 PDF reintegration
```

When the boundary is ambiguous, **stop and confirm** which commits are yours
before rewriting history — this is the one decision the skill must get right.

### 4. Back up before rewriting

```bash
git branch "backup/${DONE}_prerebase" HEAD
```

A cheap, named escape hatch. Keep it until the integration is verified.

### 5. Rebase only your commits onto the target

```bash
# Clean case (no drift): fork point == merge-base
git rebase "$TARGET"

# Drifted base: replay only your commits, excluding the foreign ones
git rebase --onto "$TARGET" "$FORK"
```

**Conflicts in generated artifacts** — regenerate, do not hand-merge. The vite
React bundle (`com.top_logic.layout.react/.../tl-react-controls*.js`) and
generated `messages_en.properties` are build outputs. On a conflict there, take
either side to unblock (`git checkout --theirs <file>` / `--ours`), finish the
rebase, then regenerate from the now-correct sources and commit the regeneration
as its own commit:

```bash
git checkout --theirs <generated-bundle>
git add <generated-bundle> && git rebase --continue
# after the rebase finishes, regenerate against the new base:
mvn -B generate-resources -pl com.top_logic.layout.react \
  2>&1 | tee com.top_logic.layout.react/target/regen.log
git add <generated-bundle> && git commit -m "Ticket #<n>: Regenerate React JS bundle for integration base."
```

After the rebase, sanity-check the result reflects the integration base, not the
drifted one (e.g. controls present/absent in the regenerated bundle match the
integration source's `controls-entry.ts`).

### 6. Fast-forward the integration branch

The rebased `HEAD` is now `TARGET` plus your commits, so the move is a true
fast-forward.

```bash
# If TARGET is NOT checked out in any worktree:
git branch -f "$TARGET" HEAD

# If TARGET IS checked out in another worktree, -f is refused; fast-forward there:
git -C "<worktree-holding-TARGET>" merge --ff-only "$SELF"
```

Verify it really fast-forwarded (no new merge commit, no rewritten target
history):

```bash
git log --oneline -n 6 "$TARGET"
```

### 7. Mark the work done — rename the local branch

Rename your feature branch to the worktree folder name. `-M` overwrites any
existing same-named branch (a stale placeholder from worktree creation).

```bash
git branch -M "$SELF" "$DONE"   # e.g. CWS/CWS_29108_integrate_document_viewers → agent-2
```

### 8. Report

Tell the user:
- Target branch and its old → new tip (short hashes), and that it fast-forwarded.
- The exact commits that landed (count + subjects).
- Any foreign commits deliberately **excluded**, and how.
- Any generated artifact that was regenerated rather than merged.
- The local branch rename (`<SELF>` → `<DONE>`).
- The backup branch name (retained).
- That nothing was pushed (local only).

## Pitfalls

- **Never replay `<target>..HEAD` blindly.** With a drifted base it carries other
  agents' commits onto the integration branch. Anchor on the parent of *your*
  first commit.
- **Do not hand-merge generated bundles / generated `messages_en`.** Regenerate
  from source after the rebase; commit the regeneration separately.
- **The main worktree's branch is a hint, not the truth.** Verify the target;
  a sibling worktree may be on the wrong branch.
- **Fast-forward only.** If the target cannot fast-forward to your rebased tip,
  the rebase used the wrong base — re-examine the fork point; do not create a
  merge commit to paper over it.
- **Never reword/amend/reset a commit already published on the integration
  branch.** Once your commit has fast-forwarded onto the integration branch, its
  SHA is shared: a sibling worktree may have rebased onto the integration branch
  and now carry that exact commit. Rewriting it in place (to fix a message,
  ticket number, etc.) orphans the old SHA and leaves every sibling branch
  holding a stale duplicate with the old content/message — a mess that surfaces
  at *their* next reintegration. If a fix is genuinely needed, do it **before**
  the fast-forward (on your own branch only), or coordinate with the sibling
  worktrees. To redo a botched integration cleanly, roll the integration branch
  back to the pre-commit base, re-rebase your branch, and fast-forward again —
  but understand that is itself another rewrite and only safe because nothing is
  pushed.
- **Don't push.** Reintegration is local; pushing is a separate explicit request.
