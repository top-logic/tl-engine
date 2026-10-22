---
description: Read before changing how an agent run executes in tl-ai-core - AgentFactory.runAgent, AgentLoop (turns, tool calls, StopReason, the final turn at max-turns, the knowledge-base view of a turn), the <environment> block and the <turn-context> block (AgentApplicationService, TurnContext), user memory (remember/recall, MemoryConsent, UserMemoryBinding) and the event system (AgentEvent, AgentEventListener, RunObserver, ObserverQueue).
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

Every run is told in its system prompt whom it works for: the user, the UI language, the date in the user's time zone, the agent, the model, the application, the host's stable facts and the account's own preference text. `AgentFactory.buildConfig` composes one `<environment>` block and fills the `{{...}}` placeholders of the prompt; a prompt that names none gets the block appended, so every shipped agent has it without an edit. An unknown placeholder is refused where it is written, not at run time.

Nothing in the block may change within a conversation - it sits in front of every request, and the providers' prompt caches key on it: the date has day granularity, and only facts marked `stable` go into it. The wording is a shipped, translated resource (`tl.ai.environment.template`) that the `<environment>` section of `AgentApplicationService` may override; the facts come from the agent definition and from `<chat-conversation><context>`, the preference text is `tl.accounts:Person#aiPreferences`, and the filled block is recorded on `tl.ai.execution:AgentExecution#environment`. `tl-ai-core/doc/environment-block.md` has the details.

### Turn context

A fact that is not stable goes into the per-turn `<turn-context>` block instead (`TurnContext`): its own content block of the turn's user message, ahead of the user's text, which stays exactly what the user wrote. The framework adds the turn number and the time of day; `AgentLoop` attaches the block where the request is built, so it survives a compaction, and it is recorded on the turn (`Turn#turnContext`, the persisted transcript). The fact a page most often has to state is which object the user selected, so `<chat-conversation><selection channel="…"/>` states it in one line - label, qualified type and the `label [id:TID]` every tool reads back - and the `{{turn_context}}` section of the environment block tells the model what a `<turn-context>` block is, for the runs that get one.

## User memory

The agent keeps things about the user across conversations (`tools/memory/`, `agent/environment/UserMemories`): `tl.ai.execution:UserMemory` rows (owner, kind, text, origin, created), rendered into the `{{memories}}` section of the environment block. `remember(kind, text)` writes one, `recall(query)` searches the rest.

`remember` refuses a memory the user did not agree to in that turn - a Java check on the call path, not a line in a prompt. `MemoryConsent` is built from the user's own message at the start of the run and from what `AskUserTool` recorded, and a nested run inherits it rather than building its own. Only a `UserMessage` whose `origin` is `USER` counts: a delivered subagent report, a framework notice and a lead's task are marked `FRAMEWORK` or `AGENT`, so a lead turn the chat wakes for a subagent's news has no consent. The words it matches are configurable, because they are matched against what the user typed.

The section is read once per conversation (`UserMemoryBinding`, bound by `ChatProcessor` from the chat session state), because the block has to stay byte-identical across the turns of one conversation. `<user-memory enabled="false"/>` on `AgentPolicyService` retires the tools and the section together. The user sees every memory in a `<data-list>` and deletes one with `aiMemoryDelete`.

## Events and observers

`AgentEvent` (package `agent/event`) is a sealed interface whose record subtypes cover the lifecycle of runs, turns, tool calls, approvals, transactions and memory. An `AgentEventListener` receives them; the chat streams them to the browser (`ChatSSEHandler`), and the conversation log records them.

A `RunObserver` (package `observe`, configured in `<run-observers>` on `AgentRuntimeService`, none by default) receives the events of every run `AgentLoop` starts - chat, script, MCP, A2A, delegation, subagent - together with whom the run is for (`ObservedRun`: agent, account, entry point, conversation, parent run). `ObserverQueue` delivers them on a thread of its own behind a bounded queue, so an observer can neither slow a run down nor break it. An observer sees what the run sees; one that passes it on outside the application must authenticate that itself.
