---
name: tl-app
description: Start, stop, or restart a TopLogic application. Use when you need a running app to test changes in the browser. Examples - "start the demo app", "restart the app", "stop the server".
allowed-tools: Bash, Read, Glob, Grep, WebFetch
---

# TopLogic Application Management

## Start

```bash
.claude/scripts/tl-start-app.sh <app-module-path>
```

On success, prints two lines:
```
url: http://localhost:PORT/context-path/
log: /tmp/tl-app-PORT.log
```

Report the URL and credentials (user `root`, password `root1234`) to the user.

Build first if needed: `mvn install -DskipTests=true` in changed modules, then the app module. Skip if user says "just start".

## Stop

```bash
.claude/scripts/tl-stop-app.sh <base-url>
```

Pass the URL that was reported on start.

## Restart

Stop the running app, then start it again.

## Determining the app module

If not specified: check current directory, check recent git changes, default to `com.top_logic.demo`. Ask if ambiguous.

## Database / `tmp/` directory

**Do NOT delete `tmp/`** unless an incompatible model change was made without a data migration (e.g. back-and-forth model changes during development), or the user explicitly asks. Adding new modules does NOT require deletion. Deleting forces slow full re-initialization and destroys test data.
