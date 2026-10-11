# Web research: `TL_deepResearch` and `TL_ResearchWorker`

Albert (`TL_SingleAgent`) answers a research question with a report built from web pages. The
skill `TL_deepResearch` tells Albert how to plan the research and how to write the report. Research
workers (`TL_ResearchWorker`) read the web, in parallel. Albert reads no web page itself.

Both are defined in `tl-ai-core.conf.config.xml` next to the other shipped skills and agents.
Gitea #723.

## The run

1. **Scope.** Albert asks one question with `askUser` only when the meaning of the question is
   open. It asks at most once.
2. **Effort.** Albert selects an effort level. The user's own word ("quick", "deep") decides;
   otherwise the level is standard.
3. **Plan.** Albert splits the question into sub-questions, one for each worker, and shows them to
   the user. It does not wait for an approval: the user can stop the run or correct the plan.
4. **Fan out.** Albert calls `spawn_subagent` once for each sub-question, all in one turn, with a
   task that stands alone: the sub-question, its purpose and the scope. It gives each worker the
   tool-call budget of the effort level as `toolCallBudget`.
5. **Wait and check.** Albert calls `wait_for_subagent`; the results arrive in the tool answer. Then
   it calls `listFindings` once, which lists the findings of all workers with their sources and
   quotes. To look at a page a worker read, it calls `readSource`, which fetches nothing.
6. **Another round.** Only for an important gap, and only as often as the effort level permits.
7. **Report.** Albert writes the report once, in the user's language, from the findings. It links
   each statement to the source of its finding, with the finding's quote as link title.
8. **Keep and answer.** Albert calls `saveReport` with the question, the report, the effort, the
   plan and the status (#794). The tool keeps the run as a `tl.ai.execution:ResearchRun` for the
   user the run works for (#764). Where the run has `present`, Albert opens the kept run beside the
   chat. Then it gives the same report as its answer, and calls no tool after it. The answer stays
   the report, so a headless run (`aiResearch()`) records the report as its result, and keeps a
   `ResearchRun` for its owner too.

| Effort | Workers | Tool calls per worker | Further rounds |
|---|---|---|---|
| quick | 1 | 10 | 0 |
| standard | 3 | 15 | 1 |
| deep | 5-8 | 25 | 2 |

### The budget is enforced

The tool-call budget is not a request in the prompt; the agent loop enforces it (#775):

- `spawn_subagent(..., toolCallBudget)` limits the worker's run. It narrows the worker's own
  `max-tool-calls` (an agent override, 25 for `TL_ResearchWorker`) and never raises it.
- A call beyond the budget is answered "Not run" and is not run. Calls of interaction tools
  (`recordFinding`, `ask_lead`) do not count.
- The worker then gets one last turn: the tools stay in the request, but the model may call none,
  and a notice tells it to write its report from what it has. The run ends as `MAX_TOOL_CALLS`.
- The lead receives the report with the line "Stopped at its limit", and names that worker under
  Open points.

A run at its `max-turns` limit ends the same way, as `MAX_TURNS`: its last turn is an answer, not a
tool call. This is true for every agent, not only for the worker.

### Why the lead waits

`TL_spawnSubagents` tells an agent to end its turn after a spawn and to let the chat wake it. A
research run waits with `wait_for_subagent` instead, for two reasons:

- The wait works for every entry point. The chat wakes the lead, but a run without a chat (the
  eval, TL-Script, a scheduled task) has no wake-up.
- The chat wakes the lead at most five times for each user message. A deep run with a second round
  can have more results than that.

The user can stop the run with Stop at any time.

## The worker

`TL_ResearchWorker` has three tools: `webSearch`, `webFetch` and `recordFinding` (#760). It has no
tool that reads application data, and no tool that changes data. It does this:

- It searches, selects the 2-4 most promising pages, prefers primary sources and reads them with
  `webFetch`.
- It stops when the sub-question is answered, when it used its budget, or when two searches in
  sequence found no new useful page.
- It reads only URLs from the task, from a search result or from a link on a page it read.
- For each statement it found, it calls `recordFinding` with the claim, the URL and a quote. The
  tool refuses a quote that the page text the worker read does not contain.
- It reports in at most 800 words: an answer, its findings by id with their pages, and conflicts and
  gaps. The findings travel with the report, so the report does not repeat the quotes.

The worker runs on the installation's default model unless an installation assigns it another one.

### Worker model profiles

The workers spend most of the tokens of a research run, so an installation can give them another
model than the lead (#784). No code is needed: an entry in `<agent-overrides>` of
`AgentRuntimeService` assigns a model and its provider to `TL_ResearchWorker`. The entry merges with
the shipped one that sets `max-tool-calls="25"`; the lead stays on the default model.

**Hosted** - the workers on a hosted mid-tier model (decision D7):

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
		<agent-overrides>
			<agent name="TL_ResearchWorker" model="gpt-5.6-luna" provider="openai"/>
		</agent-overrides>
	</instance>
</config>
```

**Local** - the workers on a local model behind an OpenAI-compatible server (here vLLM serving
`qwen3.8-27b`). Declare the server as a provider in `LLMProviderService`, with its context window
in the model catalog, because no public catalog knows a local model:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMProviderService">
	<instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
		<providers>
			<provider class="com.top_logic.tl.ai.core.provider.openai.OpenAICompatibleProviderConfig"
				api-key="%QWEN_API_KEY%"
				base-url="http://ki.bos.local:8080"
				catalog-provider-id=""
				models="qwen3.8-27b"
				name="qwen"
			/>
		</providers>
		<model-catalog>
			<model-overrides>
				<model name="qwen3.8-27b" context-window="230000" max-output="16000"
					reasoning="false" temperature="true" tool-call="true"/>
			</model-overrides>
		</model-catalog>
	</instance>
</config>
```

and assign it to the worker. The server answers one request at a time per key, so let only one
background subagent run at a time - the other workers wait in the queue:

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
		<capacity subagent-workers="1"/>
		<agent-overrides>
			<agent name="TL_ResearchWorker" model="qwen3.8-27b" provider="qwen"/>
		</agent-overrides>
	</instance>
</config>
```

`subagent-workers` applies to every background subagent of the installation, not only to research
workers. `%QWEN_API_KEY%` is an alias the application declares for the environment variable that
holds the key (prefix `unencrypted:`).

How the two compare on the research questions is measured with the eval (`tl-ai-eval`, arms
`albert-gpt` and `albert-gpt-worker-qwen`); the results are in #784.

## Security

The run separates two kinds of risk:

| | Untrusted web content | Sends requests out | Application data | Changes data |
|---|---|---|---|---|
| Worker | yes | yes | no | no |
| Lead, in this skill | only the workers' reports | no | yes | with approval |

A worker report is data for the lead, inside `<untrusted_subagent_report>`. Albert can carry
`webFetch` from `TL_webAccess`; the skill tells it not to use web tools in a research run, and the
eval checks that it does not.

A search query goes to the search provider. Albert puts application data into a worker task only
when the question cannot be researched without it.

## Sources and citations

What a worker reads goes on its own reading list. When `wait_for_subagent` delivers the result, the
worker's list joins the lead's list. The lead links the URLs the workers read, and
`CitationValidator` makes footnotes of them (#696). A link to a URL that no worker read loses its
address and keeps its text.

## Configuration an application needs

- **A search engine** on `WebToolsService` (`<search-engine>`). Without one, `webSearch` answers
  with an error.
- **A background allowlist that admits `egress`.** A spawned worker is an unattended run. Without a
  declared `<background-allowlist>` on `AiToolAccessService`, an unattended run reaches only
  interaction and introspection, and `spawn_subagent` refuses to start the worker.

tl-ai-demo has both in `WEB-INF/conf/research.config.xml`: Brave, with the key in the environment
variable `tl_brave_api_key`, and an allowlist with `model.read`, `data.read` and `egress`.

## Research without a chat window: `aiResearch`

The TL-Script function `aiResearch(question, effort, owner)` starts a research run on a background
thread and returns at once (#767). Use it from a script or from a scheduled task.

```
aiResearch("What did our three main competitors announce this month?", "deep")
```

| Parameter | Default | Meaning |
|---|---|---|
| `question` | - | What to research. |
| `effort` | `standard` | `quick`, `standard` or `deep`, as in the table above. |
| `owner` | the calling account | The account the run acts as. The run spends the budget of this account, uses its rights, and the report goes into its chat list. An account or a login name. |

A user can start a research only for themselves. Only a call without an account, such as a
scheduled `ScriptTask`, can name a different owner.

### What happens

1. The function makes a chat of the owner, with the title "Research:" and the start of the
   question, and returns the persistent id of the chat as the handle.
2. On a background thread, Albert gets the question, the instruction to use `TL_deepResearch`, the
   effort, and the information that nobody can answer.
3. The run is unattended. `askUser` answers at once that nobody is there. The policy's
   `<background-allowlist>` limits the tools, as for every run without a person.
4. The run records its turns in the chat. The report is the answer of the run, with footnotes for
   the pages that the workers read.

The owner finds the report in the chat list and opens it like any chat. The new chat in the list
tells the owner that the report is there. The engine has no notification for an account that is
not logged in, and mail is a module that tl-ai does not depend on. An application that wants a mail
reads the report with `aiAgentRunResult` and sends it.

The handle works with the functions of `aiAgentChatAsync`: `aiAgentRunStatus`, `aiAgentRunResult`,
`aiAgentRunError`, `aiAgentRunCancel`. Read it in a later request than the one that started the run.

### Budget

The owner's token and money budget limit the run, like a chat run (`<budget>` on
`AgentPolicyService`, see `run-limits.md`). When the budget is already spent, the run calls no
model. The chat then holds the question and the refusal as the answer, so the owner reads why there
is no report. Its status is `REJECTED`.

### Every Monday: a scheduled research

A `ScriptTask` of the engine runs TL-Script in a system context, without an account. Name the owner:

```xml
<config service-class="com.top_logic.util.sched.Scheduler">
	<instance class="com.top_logic.util.sched.Scheduler">
		<tasks>
			<task class="com.top_logic.model.search.providers.ScriptTask"
				name="WeeklyFundingResearch"
			>
				<script>aiResearch("Which new funding programmes for small companies were announced in the last seven days?", "standard", "jdoe")</script>
				<schedules>
					<weekly day-of-week="MONDAY" time-of-day="07:00"/>
				</schedules>
			</task>
		</tasks>
	</instance>
</config>
```

The file goes in `WEB-INF/autoconf/com.top_logic.util.sched.Scheduler.config.xml`. The task returns
after it started the run. The run continues on its own thread.

`com.top_logic.ai.chat.sched.AgentTask` (tl-chat, `scheduled-agent.md`) can also put a research
question to Albert. It runs through the chat and waits for the end of the run. `aiResearch` names
the skill and the effort for the model, and it needs no tl-chat.

### Not yet

The report is the answer of the run. A stored research object with its own view (`ResearchRun`) is
a separate change. When it exists, `aiResearch` stores the report there too.

## Tests

`TestDeepResearchSkill` (tl-ai-eval) runs the shipped skill and worker on a scripted model and on a
fixture web (`FixtureWeb`: search results and pages under the reserved domain `.test`). It checks
the shape of the run: the lead loads the skill, starts one worker for each sub-question in one turn,
waits, lists the findings every worker recorded, calls no web tool itself, and writes one report
that cites exactly the pages the workers read.

`TestHeadlessResearch` (tl-ai-eval) starts `aiResearch` in a system context for a named owner, on
the same scripted model and fixture web. It checks that the report goes into a chat of the owner,
with a footnote for the page that the worker read, and that a spent budget asks no model and records
the refusal. `TestHeadlessResearchArguments` (tl-ai-core) checks who the run acts as and the effort
levels.
