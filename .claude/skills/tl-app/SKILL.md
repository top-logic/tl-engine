---
name: tl-app
description: Start, stop, or restart a TopLogic application. Use when you need a running app to test changes in the browser. Examples - "start the demo app", "restart the app", "stop the server".
allowed-tools: Bash, Read, Glob, Grep, WebFetch
---

# TopLogic Application Management

## Quick Reference

- **App module**: Maven module with `tl-parent-app` parent (e.g. `com.top_logic.demo`)
- **Default port**: `8080` (override: `-Dtl.port=<port>`)
- **Login**: user `root`, password from `tl_initial_password` env var (default: `root1234`)
- **Stop endpoint**: `GET http://localhost:<port>/admin/stop` → returns `OK`
- **Ready signal**: stdout line `***** up and running (<time> <date>) *****`

## Start

1. Check if running: `curl -sf http://localhost:<port>/admin/stop > /dev/null 2>&1` — if success, stop first, wait 3s.
2. Build if needed: `mvn install -DskipTests=true` in changed modules, then app module. Skip if user says "just start".
3. Run as background task (`run_in_background: true`):
```bash
export tl_initial_password='root1234' && cd <app-module-path> && mvn 2>&1 | tee /tmp/tl-app.log | grep --line-buffered -E '(ERROR|up and running|BUILD FAILURE|Address already in use)'
```
4. Wait: single `TaskOutput` call with `block: true`, `timeout: 40000`. Returns immediately on first grep match.
   - `up and running` → success, report URL to user.
   - `ERROR` / `BUILD FAILURE` → failed, report output. Read `/tmp/tl-app.log` for details.

**CRITICAL**: `mvn` MUST run inside the app module directory. Never omit the `cd <app-module-path>`.

## Stop

1. `curl -sf http://localhost:<port>/admin/stop` → should return `OK`.
2. Poll until port stops responding (up to 15s).

## Restart

Stop → wait 3s → Start.

## Database / `tmp/` directory

**Do NOT delete `tmp/`** unless an incompatible model change was made without a data migration (e.g. back-and-forth model changes during development), or the user explicitly asks. Adding new modules does NOT require deletion. Deleting forces slow full re-initialization and destroys test data.

## Arguments

- `start` / `start com.top_logic.demo` — start the app
- `stop` — stop the running app
- `restart` — stop and restart
- No argument: `start` if not running, `restart` if running

## Determining the app module

If not specified: check current directory, check recent git changes, default to `com.top_logic.demo`. Ask if ambiguous.
