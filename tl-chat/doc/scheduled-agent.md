# Running an agent on a schedule

`com.top_logic.ai.chat.sched.AgentTask` is a scheduler task that puts one prompt to a named agent
and records the run as a chat session. It exists so that "every hour, have agent X do Y, and let me
read afterwards what it did" is a piece of configuration rather than a class in every application
that wants it (Gitea #384).

```xml
<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<services>
		<config service-class="com.top_logic.util.sched.Scheduler">
			<instance class="com.top_logic.util.sched.Scheduler">
				<tasks>
					<task class="com.top_logic.ai.chat.sched.AgentTask"
						name="HourlyHunt"
						agent="Ida"
						run-as="sales-robot"
					>
						<prompt>Scheduled hunt. Load hunt-lead. Hunt exactly one new person.</prompt>
						<schedules>
							<periodically interval="1h" start-time="00:15" stop-time="23:45"/>
						</schedules>
					</task>
				</tasks>
			</instance>
		</config>
	</services>
</application>
```

The file belongs in `WEB-INF/autoconf/com.top_logic.util.sched.Scheduler.config.xml` of the
application. `name`, `enabled`, `schedules` and the rest of the task frame are the
engine's own; see `com.top_logic.util.sched.task.Task.Config`.

## What the task offers

| Property | Default | |
|--|--|--|
| `agent` | the entry-point agent | Which agent answers. The `run-as` account must be allowed to run it, exactly as in the chat. |
| `prompt` | – | The message, as plain text. Write it as an element, not an attribute: a scheduled prompt is an instruction with rules in it. |
| `prompt-expression` | – | TL-Script computing the message, evaluated at the start of **every** run. The alternative to `prompt`; configure exactly one of the two, or the task refuses at boot. |
| `run-as` | `root` | The account the run acts as. Its roles decide which tools and which agents the run may use, its access rights which objects those tools may touch, and the recorded session belongs to it. |
| `approval-mode` | `AUTO_APPROVE` | See "Nobody is there" below. |
| `record-session` | `true` | Whether the sitting is kept as a chat session of that account. |
| `session-title` | the task's `name` | Title of that session. |

## Why it goes through the chat

`AgentRunner.run` executes an agent headlessly with far less plumbing, and it records an
`AgentExecution` row. What it does not produce is a **sitting**: a conversation in the account's chat
history that a person can open, read and carry on. That is the thing the ticket asked for - an
operator wants to see what the nightly agent actually did, in the same list as their own
conversations - so the task goes through `ChatProcessor.sendMessage`, the same entry point a browser
uses.

Everything else follows from that choice:

- a `TaskProgressTracker` stands in for the browser window, holding the approval mode, the session
  and the conversation state;
- `ChatHistoryService.createSession` creates the session first, so the run has something to be
  recorded against, and the thread's read revision is advanced past it
  (`HistoryUtils.updateSessionRevision`) before the tracker is asked to activate it;
- the task's own sub-session is the one the agent's tools act in, and the `run-as` account is bound
  to it for the length of the sitting;
- `ChatProcessor.sendMessage` is the one entry point, so whatever it applies to a chat run - the
  busy check, the conversation memory, the recording - applies to a scheduled one as well, and
  anything added there reaches both.

## Nobody is there

A scheduled run has no browser window. Two things follow, and the task sets both:

1. **`askUser` fails at once.** The tracker is marked
   `TaskProgressTracker#isUnattended()`, `ChatProcessor` carries that into the run's
   `ToolContext#attended()`, and `AskUserTool` answers `error: No user is present in this run …`
   instead of creating a question and parking the agent thread on it. Before this, such a call waited
   out the full interaction timeout - 30 minutes - and then told the model its question had "timed
   out", which invites it to ask again. The refusal names what to do instead: decide and state the
   assumption, or stop and say what would have been needed.
2. **The tool ceiling is the background allowlist.** Approval is the compensating control that makes
   a write-classified tool safe, and a run that cannot show a prompt has lost it. Its security context
   is therefore built as a background run, so what it may do is the intersection of the account's
   grants and the tool-access policy's `<background-allowlist>`. Widen that list, not the account's
   roles, when a scheduled agent needs a tool it is refused.

**Approval prompts do not block in this mode.** With `approval-mode="AUTO_APPROVE"`,
`ChatProcessor` gives the run `ApprovalCallback.AUTO_APPROVE`, which answers "approved" on the calling
thread - no future, no wait, no timeout. So the only thing that could have parked an unattended run
was `askUser`, which is why that is the one tool this change touches.

`approval-mode="MANUAL"` is a deliberate exception rather than an oversight. It declares that a
person *will* answer, and since #393 any window of the `run-as` account can: the request is listed by
`GET /chat/interactions` and answered through `POST /chat/respond`. Such a run is not unattended - it
keeps `askUser`, and it keeps the approval prompt as its bound instead of the allowlist. It also
blocks for up to 30 minutes per call if nobody is in fact watching, so use it only when somebody is.

## Overlap, stopping, and the cluster

- **Overlap.** An agent sitting can take longer than the interval it is scheduled at. A run started
  while the previous one is still working is skipped and reported as a warning in the scheduler's
  task table; it is not queued.
- **Stopping.** "Stop task" reaches the task thread, which is waiting for the run. It cancels the
  run's token - what `POST /chat/cancel` does - and reports the run as cancelled. It never interrupts
  the agent thread: that thread may be reading the database, and an interrupt during a read closes
  H2's database file for the whole application (#901).
- **The cluster.** The task is not node-local (`isNodeLocal()` returns `false`), so one node runs it.
  An agent run writes, sends and costs money; running it once per node would do all of that as often
  as the cluster is wide.

## What a scheduled run has, and what it lacks

| | |
|--|--|
| Has | A principal (`run-as`), its roles and access rights, the full tool catalogue that account may reach within the background allowlist, the environment block with that account's language, time zone and preferences, and a recorded transcript. |
| Lacks | A person. It cannot ask a question, cannot be shown an approval prompt (in the default mode), and has no page to write a channel of - `present` has no window to reach, and the tools that need one report that. |

A prompt written for such a run should therefore end in a decision, not in a question. If the agent
does call `askUser`, the refusal tells it so and the recorded transcript shows exactly where it
asked - which is how a scheduled prompt gets fixed.
