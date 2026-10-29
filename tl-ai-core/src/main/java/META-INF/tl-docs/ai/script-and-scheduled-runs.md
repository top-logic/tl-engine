---
description: Read before running models or agents from TL-Script or on a schedule - aiChat with its responseFormat (text, json, a JSON schema) and how each provider sends it, how a failed run is reported (TopLogicException, aiAgentRunStatus, aiAgentRunResult, aiAgentRunError), the asynchronous handle of aiAgentChatAsync, and the scheduler task com.top_logic.ai.chat.sched.AgentTask of tl-chat (agent, prompt, prompt-expression, run-as, approval-mode, record-session, session-title) with what an unattended run has and lacks.
order: 69
---

# Agents from TL-Script and the scheduler

## `aiChat`: one exchange with a model

`aiChat(messages, model, responseFormat, tools)` runs one exchange with a model. A call without tools is one turn; a call that passes tools gets the default turn limit, because the answer comes after the tool results.

`responseFormat` takes four shapes:

| Argument | Meaning |
|---|---|
| `null` | nothing is asked for; the provider answers as it likes |
| `"text"` | the provider default, stated explicitly |
| `"json"` | some JSON object, with nothing saying which |
| `{name: ..., schema: ..., strict: ...}` | a JSON object satisfying `schema` |

The schema map is the shape worth using: a plain JSON mode still lets the model choose its field names. `name` is what the provider registers the schema under (default `response`); `strict` (default `false`) asks the provider to reject its own output when it does not validate, and needs a schema that closes every object with `additionalProperties: false` and lists every property in `required`. Any other argument - `"yaml"`, a map without `schema` - is refused where it is written, because a caller that asked for JSON and received prose could not tell that its request was dropped.

The value travels as `AgentParams#responseFormat` into `ProviderRequest#responseFormat`, and each request builder spells it in its API's dialect:

| Provider | `"json"` | a schema |
|---|---|---|
| Anthropic Messages | refused - no untyped JSON mode | `output_config.format` = `{type: "json_schema", schema}` |
| OpenAI Responses | `text.format` = `{type: "json_object"}` | `text.format` = `{type, name, strict, schema}` |
| chat completions (OpenAI-compatible, Mistral, xAI, local) | `response_format` = `{type: "json_object"}` | `response_format` = `{type: "json_schema", json_schema: {name, strict, schema}}` |
| Bedrock Converse | refused | refused - the API has no response format |

A refusal is a `ProviderException` of kind `INVALID_REQUEST`, raised while the request is built, so nothing is sent. `TestResponseFormatInRequests` pins every row.

## A failed run is a failure

`aiChat` and `aiAgentChat` throw a `TopLogicException` carrying the provider's error kind and message for every stop reason but `COMPLETE` - a rejected key, a stream error, an exhausted turn limit, a model that answered nothing - after the run has been recorded. The failed run is a row an administrator can look at and an error the caller cannot mistake for an answer.

### Asynchronous runs

`aiAgentChatAsync(agent, message)` starts a run on a background thread and returns a handle at once. The run re-enters the caller's sub-session, so it acts as the calling account, marked as a background run: nobody answers an approval prompt for it, and the `<background-allowlist>` bounds its tools ([tool authorization](doc:ai/security/tool-authorization#background-and-delegated-runs)). It refuses to start from a system context.

- `aiAgentRunStatus($handle)` - `RUNNING`, `COMPLETE`, `ERROR`, `CANCELLED`, `REJECTED`;
- `aiAgentRunResult($handle)` - the answer of a `COMPLETE` run, `null` while it is not finished, and the same exception as `aiChat` for a run that ended otherwise;
- `aiAgentRunError($handle)` - the recorded message without raising it, for a script that stores or shows the failure;
- `aiAgentRunCancel($handle)` - stops the run.

The polling shape never reaches the exception:

```
aiAgentRunStatus($handle) == "COMPLETE" ? aiAgentRunResult($handle) : null
```

Read a handle in a later request than the one that started the run: the run's session is committed at a revision newer than the one the starting request read, so within that request `aiAgentRunStatus` answers `null`, however long the script waits. In the script console these are two executions in the same session, the handle carried over with `storeInSession` / `loadFromSession`. `aiAgentChat`, which runs inline, cannot be used from the script console for the same reason: its run commits past the revision the console request read, and the request fails rendering the result. `aiResearch` starts a [web research](doc:ai/deep-research#research-without-a-chat-window-airesearch) with the same kind of handle.

## Agents on a schedule

`com.top_logic.ai.chat.sched.AgentTask` (module `tl-chat`) is a scheduler task that puts one prompt to a named agent and records the run as a chat session of the account it runs as - "every hour, have agent X do Y, and let me read afterwards what it did" as configuration. In `WEB-INF/autoconf/com.top_logic.util.sched.Scheduler.config.xml`:

```xml
<config service-class="com.top_logic.util.sched.Scheduler">
	<instance class="com.top_logic.util.sched.Scheduler">
		<tasks>
			<task class="com.top_logic.ai.chat.sched.AgentTask"
				name="DailyInvoiceSummary"
				agent="Albert"
				run-as="sales-robot"
			>
				<prompt>Summarise the invoices recorded today.</prompt>
				<schedules>
					<daily time-of-day="18:00"/>
				</schedules>
			</task>
		</tasks>
	</instance>
</config>
```

`name`, `enabled`, `schedules` and the rest of the task frame are the engine's (`com.top_logic.util.sched.task.Task.Config`).

| Property | Default | |
|--|--|--|
| `agent` | the entry-point agent | which agent answers; the `run-as` account must be allowed to run it, as in the chat |
| `prompt` | - | the message as plain text, written as an element |
| `prompt-expression` | - | TL-Script computing the message, evaluated at the start of every run; configure exactly one of `prompt` and `prompt-expression`, or the task refuses at boot |
| `run-as` | `root` | the account the run acts as: its roles decide the tools and agents, its access rights the objects, and the recorded session belongs to it |
| `approval-mode` | `AUTO_APPROVE` | see below |
| `record-session` | `true` | whether the sitting is kept as a chat session of that account |
| `session-title` | the task's `name` | the title of that session |

The task goes through the chat, not through `AgentRunner`, because what it leaves behind is a **sitting** - a conversation in the account's chat history a person can open, read and continue. A `TaskProgressTracker` stands in for the browser window (approval mode, session, conversation state); `ChatHistoryService.createSession` creates the session first and the thread's read revision is advanced past it (`HistoryUtils.updateSessionRevision`); the task's sub-session is the one the agent's tools act in, with the `run-as` account bound to it; and `ChatProcessor.sendMessage` is the entry point, so the busy check, the conversation memory and the recording of a chat run apply to a scheduled one as well.

### Nobody is there

With `approval-mode="AUTO_APPROVE"` the task marks its tracker unattended (`TaskProgressTracker#isUnattended()`), which `ChatProcessor` carries into the run's `ToolContext#attended()`:

1. **`askUser` fails at once**: `AskUserTool` answers `error: No user is present in this run ...` instead of parking the agent thread until the interaction timeout, and tells the model to decide and state its assumption, or to stop and say what would have been needed.
2. **The tool ceiling is the background allowlist.** Approval is what makes a write tool safe, and a run that cannot show a prompt has lost it; its security context is built as a background run, so it may do the intersection of the account's grants and the `<background-allowlist>`. Widen that list, not the account's roles, when a scheduled agent is refused a tool it needs.

Approval itself does not block: the run's `ChatApprovalCallback` reads the chat's mode at every call and answers "approved" on the calling thread.

`approval-mode="MANUAL"` declares that a person will answer: any window of the `run-as` account sees the request through `GET /chat/interactions` and answers it through `POST /chat/respond`. Such a run is attended - it keeps `askUser` and the approval prompt instead of the allowlist - and blocks up to the interaction timeout (30 minutes) per call if nobody is watching, so use it only when somebody is.

### Overlap, stopping, cluster

- A run started while the previous one of the task is still working is **skipped** and reported as a warning in the scheduler's task table; it is not queued.
- **Stop task** cancels the run's token, as `POST /chat/cancel` does, and reports the run as cancelled. It never interrupts the agent thread: an interrupt during a database read closes H2's database file for the whole application.
- The task is not node-local (`isNodeLocal()` is `false`), so one node of a cluster runs it - a run writes, sends and costs money.

| A scheduled run | |
|--|--|
| has | a principal (`run-as`) with its roles and rights, the tools that account reaches within the background allowlist, the [environment block](doc:ai/environment) with that account's language, time zone and preferences, and a recorded transcript |
| lacks | a person: it cannot ask, cannot show an approval prompt (in the default mode), and has no page - `present` has no window to reach, and the tools that need one say so |

A prompt for such a run should end in a decision, not in a question. If the agent calls `askUser` anyway, the transcript shows where - which is how a scheduled prompt gets fixed. `tl-ai-demo` ships a disabled `AgentTask` in its `Scheduler` configuration (`TestScheduledAgentDemoConfig`).
