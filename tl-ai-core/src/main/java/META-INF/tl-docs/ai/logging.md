---
description: Read before diagnosing agent runs from the log or routing the AI log of an application - the log categories com.top_logic.tl.ai and com.top_logic.ai, the line format of DiagnosticLoggingListener (run=<runId> turn=<n> <EVENT>, RUN_START, TOOL_OK, PROVIDER_RETRY, TX_ROLLBACK, LOOP_BLOCK, ...), what is logged at ERROR, INFO and DEBUG, and the TL_AI_APPENDER route of tl-ai-demo's logging configuration (default.xml, debug.xml, LoggerAdminBean).
order: 68
---

# Logging of agent runs

Every agent run writes a structured diagnostic trace, correlated by run id with the persisted run record. The log carries the operational signal the database does not hold; the database holds the content.

## What is and is not logged

The execution store (`AgentExecutionStore`, written through `ExecutionRecorder`) records per run the run id, agent, model, provider, status, stop reason, error message, token usage and the full transcript. The log does **not** repeat that content: it never contains prompts, tool arguments or tool results, which may hold customer data. It adds what is not in the database:

| Logged (diagnostic, not persisted) | Not logged (persisted, or payload) |
|---|---|
| latency per turn and timing per tool call | prompts and the system prompt |
| token split per turn (input, output, thinking, cache) | tool arguments |
| tool argument and result **sizes** in bytes | tool results |
| provider retries and backoff (`PROVIDER_RETRY`), provider latency (`PROVIDER_MS`) | run status and stop reason as a record |
| context compaction (`COMPACT_START`, `COMPACT_DONE`) | total token usage of the run |
| loop-guard blocks (`LOOP_BLOCK`), denied calls (`TOOL_DENIED`) | the conversation |
| transaction outcomes (`TX_BEGIN`, `TX_COMMIT`, `TX_ROLLBACK`) | |
| result truncation (`TOOL_TRUNCATED`) | |

Every line carries the run id, so it joins the persisted `AgentExecution` for the full picture.

## Levels

- **ERROR** - a run that lost data, whatever the level: a turn whose commit was refused. `AgentLoop.commitTransaction` writes one line per failure, `Agent run <runId> turn <n>: the transaction of the turn could not be committed.`, with the cause. Nothing else logs this case: `TransactionScope` wraps the engine's exception without logging it, and the rollback that follows is traced at DEBUG only.
- **INFO** - rare operational events that are not persisted: provider retries (`PROVIDER_RETRY`) and reactive compaction after a context overflow (`COMPACT_START trigger=reactive`).
- **DEBUG** - the full trace: run and turn boundaries, every tool call with timing and result size, transactions, proactive compaction, provider requests, latency and errors, loop-guard blocks, approvals. Off by default; guarded by `Logger.isDebugEnabled`, so it costs nothing while off.

The `Logger` facade has no TRACE level.

## The line format

`DiagnosticLoggingListener` (package `agent/event`) wraps the listener of every run `AgentLoop` starts and renders the events; `AgentLoop` itself writes `PROVIDER_REQUEST` and `PROVIDER_MS`. Every line starts with `run=<runId> turn=<n> <EVENT>`, followed by `key=value` pairs:

```
run=8f3c1e… turn=1 RUN_START model=claude-sonnet-4-6
run=8f3c1e… turn=1 PROVIDER_REQUEST tools=12 messages=3 toolNames=[...]
run=8f3c1e… turn=1 PROVIDER_MS ms=1840
run=8f3c1e… turn=1 TURN_END in=1203 out=88 think=0 cacheR=0 cacheW=0 parts=2 ms=1902
run=8f3c1e… turn=2 TOOL_START name=search_model argsBytes=64
run=8f3c1e… turn=2 TOOL_OK name=search_model ms=142 resultBytes=2481
run=8f3c1e… turn=2 PROVIDER_RETRY kind=OVERLOADED attempt=1 waitMs=2000
run=8f3c1e… turn=2 TX_COMMIT rev=4711
run=8f3c1e… turn=3 RUN_END reason=COMPLETE turns=3 ms=8123
```

Search by run (`grep 'run=8f3c1e'`) or by event (`grep ' TOOL_FAIL '`, `grep ' PROVIDER_RETRY '`, `grep ' LOOP_BLOCK '`). A delegated child run logs under its own run id.

## Routing the AI log to a file of its own

The AI modules log under two categories: `com.top_logic.tl.ai` (`tl-ai-core`, `tl-ai-tool-api`, `tl-ai-index`) and `com.top_logic.ai` (`tl-chat`, `tl-ai-mcp-server`, `tl-ai-vector-index`, `tl-ai-a2a`, `tl-ai-views`). Without any configuration the main log stays free of the trace, because the root logger is at `info` and the trace is at `debug`.

An application assembles one log4j2 configuration from its webapp overlay; a library module cannot add an appender to it. `tl-ai-demo` therefore overrides `WEB-INF/conf/logging/default.xml` (AI categories at `info`) and `debug.xml` (AI categories at `debug`), both copies of the engine's base configuration with an appender and two loggers added. `LoggerAdminBean` selects the file with `<logging-config>debug.xml</logging-config>`; the trace then lands in `<LOG_PATH>/top-logic.tl-ai.log`.

An application that wants the same route adds to its own copy of the logging configuration:

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

and the two loggers, `additivity="false"` keeping the lines out of the main log, `level="debug"` for the full trace:

```xml
<Logger name="com.top_logic.tl.ai" additivity="false" level="info">
	<AppenderRef ref="TL_AI_APPENDER"/>
</Logger>
<Logger name="com.top_logic.ai" additivity="false" level="info">
	<AppenderRef ref="TL_AI_APPENDER"/>
</Logger>
```

Such a copy has to follow changes of the engine's base files `com.top_logic/src/main/webapp/WEB-INF/conf/logging/default.xml` and `debug.xml`.
