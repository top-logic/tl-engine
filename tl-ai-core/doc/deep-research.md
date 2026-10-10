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
   task that stands alone: the sub-question, its purpose, the scope and a budget of tool calls.
5. **Wait and check.** Albert calls `wait_for_subagent`; the results arrive in the tool answer. Then
   it calls `listFindings` once, which lists the findings of all workers with their sources and
   quotes. To look at a page a worker read, it calls `readSource`, which fetches nothing.
6. **Another round.** Only for an important gap, and only as often as the effort level permits.
7. **Report.** Albert writes the report once, in the user's language, from the findings. It links
   each statement to the source of its finding, with the finding's quote as link title.

| Effort | Workers | Tool calls per worker | Further rounds |
|---|---|---|---|
| quick | 1 | 10 | 0 |
| standard | 3 | 15 | 1 |
| deep | 5-8 | 25 | 2 |

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

The worker runs on the installation's default model. To use a cheaper model, assign it in
`<agent-overrides>` of `AgentRuntimeService`.

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

## Tests

`TestDeepResearchSkill` (tl-ai-eval) runs the shipped skill and worker on a scripted model and on a
fixture web (`FixtureWeb`: search results and pages under the reserved domain `.test`). It checks
the shape of the run: the lead loads the skill, starts one worker for each sub-question in one turn,
waits, lists the findings every worker recorded, calls no web tool itself, and writes one report
that cites exactly the pages the workers read.
