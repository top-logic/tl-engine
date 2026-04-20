---
name: running-scripted-tests
description: Use when needing to run a TopLogic scripted test (.script.xml) from the command line, or when a scripted test fails and needs debugging
---

# Running TopLogic Scripted Tests

## Overview

TopLogic scripted tests are recorded UI action sequences stored as `.script.xml` files. They are executed via the `test.TestAll` JUnit 3 runner with the target script passed as a system property.

## When to Use

- Running a specific `.script.xml` test from the command line
- Debugging a scripted test failure
- Verifying UI-level changes end-to-end

## Quick Reference

```bash
cd <app-module>   # e.g. com.top_logic.demo
mvn test -DskipTests=false \
  -Dtest=test.TestAll \
  -DTestAll.target=<path-to-script.xml> \
  -DTestAll.recursive=true
```

| Parameter | Description |
|-----------|-------------|
| `-Dtest=test.TestAll` | The JUnit runner that loads and executes script files |
| `-DTestAll.target=<path>` | Relative path to the `.script.xml` file from the module root |
| `-DTestAll.recursive=true` | Process sub-actions recursively |

**Do NOT pipe the `mvn` output through `tail`, `head`, `grep`, or any other filter.** The Bash tool already persists the full command output to a file and shows you a preview. Piping through `tail -N` discards everything before the last N lines — and since the application prints thousands of shutdown log lines, truncated output typically contains only shutdown noise, with the actual test output (assertion messages, script-level `log` actions) lost. Run `mvn` unfiltered; afterwards, use `Grep` on the persisted output file to find specific messages.

## Finding Scripts

Script files live under `src/test/` with the naming convention `Test*.script.xml`:

```
<module>/src/test/.../scripted/<feature>/TestFoo.script.xml
```

## Default Workflow: Just Run It

**Do NOT investigate aliases, env files, or config mappings upfront.** Most tests run without any special setup. Start the test directly with the command above. Only if it fails with a recognizable error, follow the recovery steps below.

## Important Notes

- **Sandbox**: Tests write to the filesystem (database, temp files, config). They will fail with "Read-only file system" if sandboxed. Run without sandbox restrictions.
- **metaConf.txt.orig**: If a previous test run crashed, a leftover `src/test/webapp/WEB-INF/conf/metaConf.txt.orig` file blocks startup. Re-running the test once clears it automatically, then run again.
- **Duration**: Scripted tests start a full application instance. Expect 1-5 minutes depending on test complexity.
- **Eclipse equivalent**: Each app module has a launch config named `<App> - Test selection` (e.g. "Demo - Test selection") that does the same thing with `-DTestAll.target="${selected_resource_loc}"`.

## Reactive Recovery: Missing Aliases

**Only trigger this section if the test run fails with `No alias available or value empty` (or a similar `%SOME_NAME%` alias error).** Do not search for aliases before the first run.

Steps once such an error appears:
1. Grep the config XML files under `<module>/src/main/webapp/WEB-INF/conf/` (e.g. `DemoConf.xml`) for the failing alias name to find its `${env:<name>}` mapping — this tells you the expected environment variable name.
2. Check whether `~/.claude/test-env.sh` exists. If so, source it blind and re-run in a single command: `source ~/.claude/test-env.sh && mvn ...`.
3. If the same alias still fails, ask the user to add the missing `export <name>=<value>` line to their env file.

**NEVER read the contents of the env file** (no `cat`, `Read`, `head`, `tail`, `grep` on its contents, no `env | grep` after sourcing, no echoing variable values). The file contains secrets that must not leave the local machine. Source it blind and trust the user to set required variables; let a missing variable surface as a test failure rather than inspecting the file.

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Searching for aliases before running the test | Just run the test first — aliases only matter if the run fails with an alias error |
| Running in sandbox mode | Disable sandbox — tests need filesystem write access |
| Test fails with "metaConf.txt.orig" | Run test again — first run cleans up, second run succeeds |
| Missing alias errors | Follow the Reactive Recovery section above |
| Wrong module directory | `cd` into the application module that contains the test, not the engine root |
