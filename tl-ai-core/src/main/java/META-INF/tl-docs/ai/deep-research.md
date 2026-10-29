---
description: Read before configuring or changing web research of the agents - the skill TL_deepResearch and the worker agent TL_ResearchWorker (spawn_subagent, wait_for_subagent, recordFinding, listFindings, readSource, saveReport), the effort levels quick, standard and deep, worker model profiles in <agent-overrides> (max-tokens, subagent-workers), the search engine and <background-allowlist> it needs, and research without a chat window with aiResearch, from a ScriptTask on a schedule.
order: 69
---

# Web research

The assistant Albert (`TL_SingleAgent`) answers a research question with a report built from web pages. The skill `TL_deepResearch` tells it how to plan the research and how to write the report; research workers (`TL_ResearchWorker`) read the web in parallel. Albert reads no web page itself. Both are defined in `tl-ai-core.conf.config.xml` next to the other shipped skills and agents.

## The run

1. **Scope.** Albert asks one question with `askUser` only when the meaning of the question is open, at most once.
2. **Effort.** Albert selects an effort level; the user's own word ("quick", "deep") decides, otherwise it is `standard`.
3. **Plan.** Albert splits the question into sub-questions, one per worker, and shows them. It does not wait for an approval: the user can stop the run or correct the plan.
4. **Fan out.** Albert calls `spawn_subagent` once per sub-question, all in one turn, each task standing alone: the sub-question, its purpose, the scope and the effort.
5. **Wait and check.** Albert calls `wait_for_subagent`, then `research.listFindings` once for the findings of all workers with their sources and quotes; `research.readSource` shows the stored text of a page a worker read, without fetching it.
6. **Another round**, only for an important gap and only as often as the effort permits.
7. **Report.** Albert writes the report once, in the user's UI language, from the findings, linking each statement to the source of its finding with the finding's quote as link title.
8. **Keep and answer.** Albert calls `research.saveReport` with the question, the report, the effort, the plan and the status, which keeps the run as a `tl.ai.execution:ResearchRun` ([kept research runs](doc:ai/memory-and-citations#research-findings-and-reports)); where the page has a place for it, `present` opens the kept run beside the chat. Then it answers with the same report and calls no tool after it, so a headless run records the report as its result too.

| Effort | Workers | How much each worker reads | Further rounds |
|---|---|---|---|
| `quick` | 1 | a few sources | 0 |
| `standard` | 3 | enough to answer its sub-question | 1 |
| `deep` | 5-8 | thoroughly: several independent sources | 2 |

The effort is guidance in the prompts; no code counts the tool calls of a worker. A worker stops when its sub-question is answered or when two searches in a row brought no new useful page. One answer of a worker is at most 16384 tokens (`max-tokens` of its shipped agent override), which keeps a model that loops inside the arguments of a tool call from writing until the model's own output limit while the lead waits. A worker at its `max-turns` ends with an answer, and the lead reads "Stopped at its limit" and names the worker under the open points of the report ([a limit ends with an answer](doc:ai/agent-loop#a-limit-ends-with-an-answer)).

**The lead waits** with `wait_for_subagent` instead of ending its turn and letting the chat wake it, as other spawning agents do: a run without a chat (TL-Script, a scheduled task, the eval) has nobody who wakes it, and the chat wakes a lead at most five times per user message, fewer than the results of a deep run with a second round ([waking the lead](doc:ai/delegation#waking-the-lead)).

## The worker

`TL_ResearchWorker` carries `webSearch`, `webFetch` and `research.recordFinding` - no tool that reads application data, none that changes data. It selects the two to four most promising pages of a search, prefers primary sources, and reads only addresses from its task, from a search result or from a page it read ([URL provenance](doc:ai/security/browsing-governance#url-provenance)). Each statement it finds is a `recordFinding` with the claim, the URL and a quote, refused when the page text the worker read does not contain the quote. It reports in at most 800 words - an answer, its findings by id with their pages, conflicts and gaps; the findings travel with the report.

### Worker model profiles

The workers spend most of the tokens of a research run, so an installation can put them on another model than the lead - an entry of `<agent-overrides>` on `AgentRuntimeService`, which merges with the shipped entry that sets `max-tokens`:

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
		<agent-overrides>
			<agent name="TL_ResearchWorker" model="gpt-5.4-mini" provider="openai"/>
		</agent-overrides>
	</instance>
</config>
```

A local model behind an OpenAI-compatible server is declared as a provider of `LLMProviderService`, with its context window in the model catalog, because no public catalog knows it:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMProviderService">
	<instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
		<providers>
			<provider class="com.top_logic.tl.ai.core.provider.openai.OpenAICompatibleProviderConfig"
				base-url="http://localhost:8000"
				catalog-provider-id=""
				models="qwen3-32b"
				name="local"
			>
				<api-key name="tl_local_api_key"/>
			</provider>
		</providers>
		<model-catalog>
			<model-overrides>
				<model name="qwen3-32b" context-window="128000" max-output="16000"
					reasoning="false" temperature="true" tool-call="true"/>
			</model-overrides>
		</model-catalog>
	</instance>
</config>
```

and assigned to the worker. A server that answers one request at a time gets one background subagent at a time; the other workers wait in the queue. `subagent-workers` bounds every background subagent of the installation, not only research workers ([run limits](doc:ai/limits-and-caching#run-limits)):

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
		<capacity subagent-workers="1"/>
		<agent-overrides>
			<agent name="TL_ResearchWorker" model="qwen3-32b" provider="local"/>
		</agent-overrides>
	</instance>
</config>
```

The eval of `tl-ai-eval` compares such arms on research questions ([testing](doc:ai/testing#the-research-questions)).

## Security

| | Untrusted web content | Sends requests out | Application data | Changes data |
|---|---|---|---|---|
| worker | yes | yes | no | no |
| lead, in this skill | only the workers' reports | no | yes | with approval |

A worker report reaches the lead as data, inside `<untrusted_subagent_report>`. Albert may carry `webFetch` from `TL_webAccess`; the skill tells it to use no web tool in a research run, and the eval checks that. A search query goes to the search provider, so Albert puts application data into a worker task only when the question cannot be researched without it.

What a worker reads goes on its own reading list, which joins the lead's list when `wait_for_subagent` delivers the result; `CitationValidator` makes footnotes of the links to pages the workers read, and a link to a page no worker read loses its address and keeps its text ([citations](doc:ai/memory-and-citations#citations)).

## What an application configures

- **A search engine** on `WebToolsService` (`<search-engine>`); without one, `webSearch` answers with an error.
- **A background allowlist that admits `egress`.** A spawned worker is an unattended run; without a declared `<background-allowlist>` on `AiToolAccessService` it reaches only interaction and introspection, and `spawn_subagent` refuses to start it ([tool authorization](doc:ai/security/tool-authorization#background-and-delegated-runs)).

`tl-ai-demo` has both in `WEB-INF/conf/research.config.xml`: Brave, with its key in the variable `tl_brave_api_key`, and an allowlist with `model.read`, `data.read` and `egress`.

## Research without a chat window: `aiResearch`

`aiResearch(question, effort, owner)` starts a research run on a background thread and returns at once, from a script or a scheduled task:

```
aiResearch("What did our three main competitors announce this month?", "deep")
```

| Parameter | Default | Meaning |
|---|---|---|
| `question` | - | what to research |
| `effort` | `standard` | `quick`, `standard` or `deep` |
| `owner` | the calling account | the account the run acts as - its rights, its budget, its chat list; an account or a login name. Only a call without an account, such as a `ScriptTask`, may name another one |

The function creates a chat of the owner titled "Research:" and the start of the question, and returns the chat's persistent id as the handle. On a background thread Albert gets the question, the instruction to use `TL_deepResearch`, the effort and the information that nobody can answer: the run is unattended, `askUser` answers at once that nobody is there, and the `<background-allowlist>` bounds its tools. The report is the answer of the run, recorded in the chat with footnotes; the owner finds it in the chat list. The engine has no notification for an account that is not logged in; an application that wants a mail reads the report with `aiAgentRunResult` and sends it. The handle works with `aiAgentRunStatus`, `aiAgentRunResult`, `aiAgentRunError` and `aiAgentRunCancel` - read it in a later request than the one that started the run ([agents from TL-Script](doc:ai/script-and-scheduled-runs#asynchronous-runs)).

The owner's token and money budget bound the run like a chat run ([run limits](doc:ai/limits-and-caching#run-limits)). With the budget already spent the run calls no model; the chat holds the question and the refusal as the answer, with status `REJECTED`.

A `ScriptTask` runs TL-Script in a system context, without an account, so it names the owner - in `WEB-INF/autoconf/com.top_logic.util.sched.Scheduler.config.xml`:

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

The task returns once it started the run; the run continues on its own thread. The scheduler task `com.top_logic.ai.chat.sched.AgentTask` of `tl-chat` can put a research question to Albert as well, through the chat, and waits for the end of the run ([scheduled agents](doc:ai/script-and-scheduled-runs#agents-on-a-schedule)); `aiResearch` names the skill and the effort for the model and needs no `tl-chat`.

## Tests

- `TestDeepResearchSkill` (`tl-ai-eval`) runs the shipped skill and worker on a scripted model and a fixture web (`FixtureWeb`: search results and pages under the reserved domain `.test`): the lead loads the skill, starts one worker per sub-question in one turn, waits, lists the findings, calls no web tool and writes one report that cites exactly the pages the workers read.
- `TestHeadlessResearch` (`tl-ai-eval`) starts `aiResearch` in a system context for a named owner: the report lands in a chat of the owner with a footnote for the page the worker read, and a spent budget asks no model and records the refusal.
- `TestHeadlessResearchArguments` (`tl-ai-core`) checks whom the run acts as and the effort levels.
- The research eval with real models and the real web is described under [testing](doc:ai/testing#the-research-questions).
