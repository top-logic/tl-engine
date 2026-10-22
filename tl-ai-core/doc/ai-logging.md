# AI subsystem logging

tl-ai emits a dedicated, structured diagnostic trace for every agent run, routed to its own log
target and controlled independently of the main application log.

## TL;DR

- All AI logging uses the `com.top_logic.tl.ai.*` category (core + tool API) and
  `com.top_logic.ai.*` (chat, MCP server, vector index).
- In the demo app it is routed to `top-logic.tl-ai.log` with `additivity="false"`, so it never
  reaches `top-logic.main.log`.
- At `info` you get only rare operational events. Raise the category to `debug` for the full
  per-run trace. Everything is guarded by `Logger.isDebugEnabled(...)`, so `debug` off means zero
  cost.
- **The log never contains prompts, tool arguments or tool results.** That content is persisted in
  the `tl.ai.execution` model (`AgentExecution` and friends). The log only carries
  diagnostics - sizes, timings, counts, stop reasons - correlated to those records by `runId`.

## What is and is not logged

The persistent execution store (`AgentExecutionStore`, written via `ExecutionRecorder`) already
records, per run: `runId`, agent name, model, provider, status, stop reason, error message, total
token usage and the full transcript. The diagnostic log deliberately does **not** repeat that
content. It adds the operational signal that is *not* in the database:

| Logged (diagnostic, not persisted)                                   | Not logged (already persisted / payload) |
|----------------------------------------------------------------------|-------------------------------------------|
| Per-turn latency and per-tool timing                                 | Full prompts / system prompt              |
| Per-turn token split (input/output/thinking/cache)                   | Full tool arguments                       |
| Tool result **sizes** (byte counts)                                  | Full tool results                         |
| Provider retries and backoff (`PROVIDER_RETRY`)                      | Run status / stop reason as the record    |
| Provider call latency (`PROVIDER_MS`)                                | Total token usage (run total)             |
| Context compaction (`COMPACT_START` / `COMPACT_DONE`)                | The conversation transcript               |
| Loop-guard blocks - duplicate / oscillation (`LOOP_BLOCK`)           |                                           |
| Transaction outcomes (`TX_BEGIN` / `TX_COMMIT` / `TX_ROLLBACK`)      |                                           |
| A refused commit, with its cause - at ERROR, see below               |                                           |
| Result truncation (`TOOL_TRUNCATED`)                                 |                                           |

Every line carries `runId` so it can be joined back to the persisted `AgentExecution` row for the
full, authoritative picture (including the payloads that are intentionally absent from the log).

## Levels

There is no TRACE level in the TopLogic `Logger` facade; the three levels in use are:

- **ERROR** - a run that lost data, whatever the log level. Today that is the turn whose commit was
  refused: `AgentLoop.commitTransaction` writes one line per failure, naming run, turn and cause,
  because nothing else does. `TransactionScope` wraps the engine's exception without logging it and
  the rollback that follows is only traced at DEBUG, so a deployment that hit this used to find
  nothing at all (issue #410). Format:
  `Agent run <runId> turn <n>: the transaction of the turn could not be committed.`
- **INFO** - rare, important operational events that are *not* persisted and are worth seeing even
  with debug off: provider retries (`PROVIDER_RETRY`) and reactive, context-overflow compaction
  (`COMPACT_START trigger=reactive`).
- **DEBUG** - the full per-run / per-turn / per-tool trace (run and turn boundaries, every tool
  call with timing and result size, transactions, proactive compaction, provider request/latency,
  provider errors, loop-guard blocks). Off by default.

## Enabling and reading the logs

The demo app ships two logging configurations under
`tl-ai-demo/src/main/webapp/WEB-INF/conf/logging/`:

- `default.xml` - AI category at `info` (production-like; main log stays clean).
- `debug.xml` - AI category at `debug` (full trace in `top-logic.tl-ai.log`).

Select the debug variant the same way as any other TopLogic logging config (e.g. developer mode /
the app's log-config alias). The trace then lands in `<LOG_PATH>/top-logic.tl-ai.log`.

Line format - `run=<runId> turn=<n> <EVENT> key=value ...`, for example:

```
run=8f3c1e… turn=1 RUN_START model=claude-opus-4-8
run=8f3c1e… turn=1 PROVIDER_REQUEST tools=12 messages=3 toolNames=[...]
run=8f3c1e… turn=1 PROVIDER_MS ms=1840
run=8f3c1e… turn=1 TURN_END in=1203 out=88 think=0 cacheR=0 cacheW=0 parts=2 ms=1902
run=8f3c1e… turn=2 TOOL_START name=search_model argsBytes=64
run=8f3c1e… turn=2 TOOL_OK name=search_model ms=142 resultBytes=2481
run=8f3c1e… turn=2 PROVIDER_RETRY kind=OVERLOADED attempt=1 waitMs=2000
run=8f3c1e… turn=2 TX_COMMIT rev=4711
run=8f3c1e… turn=3 RUN_END reason=COMPLETE turns=3 ms=8123
```

Useful greps: by run (`grep 'run=8f3c1e'`), by event (`grep ' TOOL_FAIL '`,
`grep ' PROVIDER_RETRY '`, `grep ' LOOP_BLOCK '`).

## Adding the routing to another application

A TopLogic application assembles a single log4j2 configuration from the webapp overlay - there is
no fragment merge - so a library module cannot inject this route automatically. To get the same
dedicated target in your own app, copy your `WEB-INF/conf/logging/default.xml` (and `debug.xml`)
and add:

An appender:

```xml
<RollingFile name="TL_AI_APPENDER"
	fileName="${log.directory}/${filename.prefix}.tl-ai.log"
	filePattern="${log.directory}/${filename.prefix}.tl-ai.%i.log"
>
	<PatternLayout pattern="${pattern.long}" charset="UTF-8"/>
	<DefaultRolloverStrategy fileIndex="min" max="10"/>
	<SizeBasedTriggeringPolicy size="1 MB"/>
</RollingFile>
```

And the routes (use `level="debug"` to capture the full trace; `additivity="false"` keeps it out
of the main log):

```xml
<Logger name="com.top_logic.tl.ai" additivity="false" level="info">
	<AppenderRef ref="TL_AI_APPENDER"/>
</Logger>
<Logger name="com.top_logic.ai" additivity="false" level="info">
	<AppenderRef ref="TL_AI_APPENDER"/>
</Logger>
```

Note: even without any config change, the main log stays free of AI debug noise out of the box -
the root logger is at `info`, and all detailed AI tracing is at `debug`, so nothing is emitted
until you deliberately raise the AI category.
