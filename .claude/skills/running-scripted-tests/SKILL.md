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

## Important Notes

- **Sandbox**: Tests write to the filesystem (database, temp files, config). They will fail with "Read-only file system" if sandboxed. Run without sandbox restrictions.
- **Aliases**: Some tests require aliases (e.g. `%TEST_OPEN_API_URL%`). A missing alias causes `No alias available or value empty` errors. Aliases are mapped to system properties or environment variables via `${env:<name>}` expressions in a config XML file under `WEB-INF/conf/` (e.g. `DemoConf.xml`). Grep for the alias name to find the mapping and the expected property name. If values are not available locally, ask the user to provide a shell file with `export <name>=<value>` lines (e.g. `~/.claude/test-env.sh`) and source it before running the test.
  - **NEVER read the contents of the env file** (no `cat`, `Read`, `head`, `tail`, `grep` on its contents, no `env | grep` after sourcing, no echoing variable values). The file contains secrets that must not leave the local machine — any tool output could be transmitted over the network. Source it blind (`source ~/.claude/test-env.sh && mvn ...` in a single command) and trust that the user set the required variables. If a variable is missing, the test will fail with a clear alias error — only then ask the user to add it, without inspecting what is already there.
- **metaConf.txt.orig**: If a previous test run crashed, a leftover `src/test/webapp/WEB-INF/conf/metaConf.txt.orig` file blocks startup. Re-running the test once clears it automatically, then run again.
- **Duration**: Scripted tests start a full application instance. Expect 1-5 minutes depending on test complexity.
- **Eclipse equivalent**: Each app module has a launch config named `<App> - Test selection` (e.g. "Demo - Test selection") that does the same thing with `-DTestAll.target="${selected_resource_loc}"`.

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Running in sandbox mode | Disable sandbox — tests need filesystem write access |
| Test fails with "metaConf.txt.orig" | Run test again — first run cleans up, second run succeeds |
| Missing alias errors | Pass as `-Dtl_test_<name>=<value>` system property (see notes above) |
| Wrong module directory | `cd` into the application module that contains the test, not the engine root |
