---
description: Read before changing how an agent run executes in tl-ai-core - AgentFactory.runAgent, AgentLoop (turns, tool calls, StopReason, the final turn at max-turns, the knowledge-base view of a turn), where the <environment> block is composed, and the event system (AgentEvent, AgentEventListener, RunObserver, ObserverQueue).
order: 60
---

# The agent loop and its environment

## The loop

`AgentLoop` (package `com.top_logic.tl.ai.core.agent`) is the execution engine of a run: it builds a `ProviderRequest`, streams the model's answer, extracts the `ToolCall`s, executes the tools and loops until a stop condition holds. `AgentFactory` builds agents from their definitions - configured ones (`TopLogicAgentDefinitions`) and records in the knowledge base - and wires provider, tools, MCP servers and skills.

The flow of a run is `AgentFactory.runAgent()` → `AgentLoop` → `LLMProvider.stream()` → `StreamHandler` → `ToolCatalog.execute()` → repeat. `AgentFactory.runAgent(..., RunOverrides)` is the choke point every entry point passes - the chat, `AgentRunner`, the scheduler, TL-Script, MCP and A2A - so the run authorization ([run access](doc:ai/security/run-access)) and the run limits ([run limits](doc:ai/limits-and-caching)) are applied there, before a provider is called. It refuses a run while `AgentRuntimeService`, `AgentApplicationService` or `AgentPolicyService` is not running, so no run starts with the default security context, without limits, without a budget or without the application's standing instructions; a test that starts agent runs starts all three modules.

A run ends with a `StopReason`: `COMPLETE`, `MAX_TURNS`, `ERROR`, `CONTEXT_OVERFLOW`, `REJECTED`, `ABORTED`, `BUDGET_EXHAUSTED`, `NO_ANSWER` or `OUTPUT_LIMIT`.

An installation renames a shipped agent in the service editor of *TopLogic agents and skills* (`display-name`, `summary`); the technical `name` stays, and the change takes effect when that service is restarted.

### A limit ends with an answer

A run at its `max-turns` gets one last turn that keeps the tool list - the cached prefix stays the same - but sends `ToolChoice.NONE` and a framework notice to answer with what it has. Tool calls a provider still returns there are dropped, and a last reply without text is asked for once more in the same way. The run ends as `MAX_TURNS`, the chat appends a notice, and a delivered subagent result says "Stopped at its limit". A run limited to one turn has no turn to spare and works in it. There is no budget of tool calls: how much a research worker reads is guidance in its prompt. `TestRunLimitFinalTurn` pins it.

### What a turn reads

A run holds one interaction for all its turns, and an interaction reads at the revision it read first. So `AgentLoop` moves the run's view to the newest commit before the first call of a turn is checked, and again after an approval that first call waited for - only while the thread holds no knowledge-base context, so a run inside a caller's transaction keeps the caller's view. The calls of one turn read one state. A tool that waits for other threads' commits within its call catches up with `ToolExecutionContext.catchUpWithCommits()`. A commit made after the turn's view was taken still fails the turn on a conflict. `TestCommitsOfOtherThreads` pins it; the rule for tool authors is in `tl-ai-tool-api/README.md`.

### Tool calls and the transaction

READ tools run in parallel and need no approval; `USER_SCOPED`, `DATA_WRITE` and `STRUCTURAL` tools run one after the other, need approval and enrol in the turn's transaction; `EXTERNAL` tools run serially, need approval and take part in no transaction, because their change lands in another system. The mode is decided per call (`ToolCatalog.callMode`): a dry run of a write tool is a read. There is one commit per turn, at the end of the turn, guarded by the [pre-commit guard](doc:ai/security/pre-commit-guard); a failed tool call or a refused commit rolls the whole turn back through `AgentLoop.rollbackTurn` and tells the model with a `[Framework notice]`.

## The environment block

Every run is told in its system prompt whom it works for - user, UI language, date, agent, model, application, the host's stable facts, the account's preference text and its memories. `AgentFactory.buildConfig` composes one `<environment>` block and fills the `{{...}}` placeholders of the prompt; a prompt that names none gets the block appended. Nothing in it may change within a conversation, because the providers' prompt caches key on it; what changes per turn travels in the `<turn-context>` block of the user message (`TurnContext`). The memories are `tl.ai.execution:UserMemory` rows, written by `remember` only with the user's agreement in that turn (`MemoryConsent`). [The environment of a run](doc:ai/environment) describes the block, its placeholders and template, application facts, prompt contributors, the per-turn block, preferences and memories.

## Events and observers

`AgentEvent` (package `agent/event`) is a sealed interface whose record subtypes cover the lifecycle of runs, turns, tool calls, approvals, transactions and memory. An `AgentEventListener` receives them; the chat streams them to the browser (`ChatSSEHandler`), and the conversation log records them.

A `RunObserver` (package `observe`, configured in `<run-observers>` on `AgentRuntimeService`, none by default) receives the events of every run `AgentLoop` starts - chat, script, MCP, A2A, delegation, subagent - together with whom the run is for (`ObservedRun`: agent, account, entry point, conversation, parent run). `ObserverQueue` delivers them on a thread of its own behind a bounded queue, so an observer can neither slow a run down nor break it. An observer sees what the run sees; one that passes it on outside the application must authenticate that itself.

Every run's listener is wrapped in a `DiagnosticLoggingListener`, which writes the run's diagnostic trace to the log - sizes, timings, retries, transactions, never payloads; see [logging](doc:ai/logging).
