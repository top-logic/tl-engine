---
name: tl-app
description: Start, stop, or restart a TopLogic application. Use when you need a running app to test changes in the browser. Examples - "start the demo app", "restart the app", "stop the server".
allowed-tools: Bash, Read, Glob, Grep, WebFetch
---

# TopLogic Application Management

Manage the lifecycle of a TopLogic application (start, stop, restart).

## Concepts

- **App module**: A Maven module that produces a runnable WAR (e.g. `com.top_logic.demo`). Identified by having `tl-parent-app` as parent and a `defaultGoal` of `tl:resolve compile exec:java`.
- **Port**: Configured via Maven property `tl.port` (default `8080`). Override with `-Dtl.port=<port>` on the Maven command line.
- **Start command**: `mvn` with no arguments, executed in the app module directory. This blocks (runs Jetty in the foreground, streaming logs to stdout). Must be run as a **background task**.
- **Stop endpoint**: `GET http://localhost:<port>/admin/stop` returns plain text `OK` on success.
- **Ready check**: The app prints `***** up and running (<time> <date>) *****` to stdout when fully started. Watch the background task output for this line.

## Determining the app module

If the user says "start the app" without specifying which module, infer it from context:
1. Check the current working directory - if it's inside an app module, use that.
2. Check recent git changes - the module being worked on likely has a corresponding app.
3. Common app modules: `com.top_logic.demo` (the demo app).

If ambiguous, ask the user which module to start.

## Database / `tmp/` directory

The app's `tmp/` directory contains the H2 database with all application data. **Do NOT delete it** unless there is a specific reason:
- The database is broken or corrupted.
- The schema was changed and no migration is provided.
- The user explicitly asks to reset the data.

A normal restart (stop + start) preserves the database. Deleting `tmp/` forces a full re-initialization which is slow and destroys any test data the user has set up.

## Operations

### Start

1. **Check if already running**: `curl -sf http://localhost:<port>/admin/stop > /dev/null 2>&1` - if this succeeds, an app is already running. Stop it first (see Stop below), then wait 3 seconds.
2. **Build first** (if needed): If the user just made code changes, the app module and its dependencies should be built first. Run `mvn install -DskipTests=true` in each changed module, then in the app module. Use your judgement - if the user explicitly says "just start" or the build is already up to date, skip this.
3. **Start in background**: Run the filtered start command (see below) as a background Bash task with `run_in_background: true`. The command pipes Maven output through grep so only relevant lines (errors and the ready signal) appear in the task output. The full log is saved to `/tmp/tl-app.log` for debugging.
4. **Monitor startup**: Read the background task output every 10 seconds (up to 120 seconds total). Since the output is pre-filtered, every line is meaningful:
   - **Success**: A line containing `up and running` means the app is fully started.
   - **Failure**: Any `ERROR` line or the task exiting means startup failed. Report the filtered output to the user. If more detail is needed, read `/tmp/tl-app.log`.
5. **Report**: Once the "up and running" line appears, tell the user the app is running at `http://localhost:<port>/`.

Start command (filters output to errors + ready signal, saves full log):
```bash
cd <app-module-path> && mvn 2>&1 | tee /tmp/tl-app.log | grep --line-buffered -E '(ERROR|up and running|BUILD FAILURE|Address already in use)'
```

With custom port:
```bash
cd <app-module-path> && mvn -Dtl.port=9090 2>&1 | tee /tmp/tl-app.log | grep --line-buffered -E '(ERROR|up and running|BUILD FAILURE|Address already in use)'
```

### Stop

1. **Send stop request**: `curl -sf http://localhost:<port>/admin/stop`
2. **Verify response**: Should return `OK`.
3. **Wait for shutdown**: Poll until the port is no longer responding (up to 15 seconds).
4. **Report**: Confirm the app has stopped.

If the stop endpoint doesn't respond, the app is likely not running. Report this.

### Restart

1. Stop the running app (see Stop).
2. Wait 3 seconds for port release.
3. Start the app (see Start).

## Port handling

- Default port: `8080`
- If the user specifies a port, use it.
- If a non-default port was used previously in this session, remember and reuse it.
- Pass non-default ports via `-Dtl.port=<port>`.

## Error handling

- **Port already in use**: If `mvn` fails with "Address already in use", another process holds the port. Tell the user.
- **Build failure**: If `mvn` exits with an error during startup, read the last 50 lines of output and report the error.
- **Timeout**: If the app doesn't become ready within 120 seconds, read the background task output and report what's happening.

## Arguments

The skill accepts an optional argument specifying the action and/or module:
- `start` / `start com.top_logic.demo` - Start the app
- `stop` - Stop the running app
- `restart` - Stop and restart
- No argument defaults to `start` if no app is running, `restart` if one is.
