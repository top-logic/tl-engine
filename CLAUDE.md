# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Custom AI agent framework built on TopLogic, replacing LangChain4j with a purpose-built streaming agent loop. Multi-module Maven build (parent: `tl-parent-core` version `8.0.0-alpha10`; `tl-ai-demo` pins `tl-parent-app` at the same engine version separately).

| Module | Role |
|--------|------|
| **tl-ai-tool-api** | Shared tool contracts (`Tool`, `ToolResult`, `ToolMode`, `@ToolMethod`, `@P`, `ToolSchema`). No implementations — just interfaces and annotations. |
| **tl-ai-core** | Agent loop engine, multi-provider LLM streaming, tool dispatcher & implementations, MCP client, multi-agent delegation, memory/compaction, TL-Script bindings |
| **tl-ai-mcp-server** | MCP protocol server (SDK 1.1.2) exposing TopLogic model/tools to external AI clients over HTTP at `/mcp/*`. **Off by default** - the endpoint authenticates nobody, see `doc/security/mcp-endpoint.md` |
| **tl-chat** | Agentic chat UI - REST API + SSE streaming servlet, React frontend (`ui/`, Vite), session management, human-in-the-loop approval |
| **tl-chat-classic** | The React chat in a classic LayoutServlet tab - the "Albert" tab. A `LayoutComponent` that writes a React view into a classic page; a temporary bridge with documented workarounds (`tl-chat-classic/CLAUDE.md`). Opt-in: a host gets it by depending on this module. |
| **tl-ai-vector-index** | Semantic search via embedding models (Mistral) and vector stores (Qdrant/JVector), background indexing daemon |
| **tl-ai-eval** | Model eval harness: the same business tasks put to several models through our own agent loop, graded against the database they leave behind. Its own runs are gated by `-Dtl.ai.eval=true`; its plumbing runs in every build against a scripted provider. See `tl-ai-eval/README.md` |
| **tl-ai-demo** | Application module bundling all above for deployment |

**Module dependency order:** `tl-ai-tool-api` → `tl-ai-core` → `tl-ai-mcp-server` / `tl-chat` / `tl-ai-vector-index` / `tl-ai-eval`; `tl-chat` → `tl-chat-classic`; all but `tl-ai-eval` → `tl-ai-demo`

## Build Commands

```bash
mvn clean install                      # Build entire project
mvn clean install -pl tl-ai-core       # Build specific module
mvn test -pl tl-ai-core               # Test specific module
mvn clean install -DskipTests          # Skip tests
```

### Running the tests

Tests are **skipped by default** in this build; `-DskipTests=false` is what turns them on. One
command runs everything a machine can run without a key, a browser or an external service:

```bash
mvn test -DskipTests=false                    # every module
mvn test -DskipTests=false -pl tl-ai-core     # one module
```

Two includes decide what that runs, configured once in the aggregator `pom.xml` (and repeated in
`tl-ai-demo/pom.xml`, whose parent is `tl-parent-app` and which therefore inherits nothing):

| Include | Runs |
|---|---|
| `test/TestAll.java` | the engine's generic suite (file names, layout normalization, JSP content, I18N constants, migration-script consistency) **plus** everything below `src/test/java/test/`, recursively |
| `%regex[com/top_logic/(ai\|tl/ai)/...]` | this repository's own tests, which live in packages mirroring the production code |

**Adding a test.** Put it next to the code it tests, in the mirrored package
(`com.top_logic.tl.ai.core.agent` for something in `com.top_logic.tl.ai.core.agent`), and name it
`Test<Something>` or `<Something>Test`. Nothing else is needed - no suite to register it in, no
include to extend. `TestSuiteReachability` fails the build if a test class is somewhere no include
reaches, or is named so that no collector recognises it, so a test that would silently never run
says so instead. Do **not** write a stub in the `test` package that forwards to it: that was the old
way round the missing includes (#369) and now runs the class twice, which the same test rejects.

A test that needs a knowledge base is still a unit test here: wrap it in the H2 setups
(`KBSetup.getSingleKBTest`, `ServiceTestSetup`) as the existing ones do, and it runs with the rest.
A test that needs something the build may not have - a key, a browser, an external service - belongs
behind one of the opt-in gates below, so that it reports as **skipped** rather than failing or, worse,
passing without having done anything.

### Browser end-to-end tests

The `tools/web` browser tests launch a real Chromium, so they are off unless
`-Dtl.ai.browser.e2e=true` is given. Without it they report as **skipped** (never as passed) - see
`BrowserE2e` and `AssumptionAwareRunner` in `tl-ai-core/src/test/java`. To run them:

```bash
mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.browser.e2e=true -Dtest='TestBrowser*'
```

Playwright provisions Chromium into `~/.cache/ms-playwright` on first use; it must be the full
Chromium build, not `chrome-headless-shell` (a plain `playwright install chromium` supplies both).
Tests that reach public sites skip themselves separately when DNS does not resolve.

### Visual harnesses

The `Visual*` classes assert nothing: they render a transcript to stdout for a human to read, and
two of them spend a real Anthropic API call doing it. They use the same gate, behind
`-Dtl.ai.visual=true` - see `VisualHarness` next to `AssumptionAwareRunner`. Without it they report
as **skipped**. To run them:

```bash
source env.sh   # for the two that call the API
mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.visual=true -Dtest='Visual*'
```

The two that call the API skip themselves separately when `ANTHROPIC_API_KEY` is not in the
environment.

### Prompt-injection end-to-end tests

Whether the untrusted-content envelope holds is a question about what a model does with a string, so
`TestPromptInjectionBarrier` puts a real model in front of an injected tool result and asserts which
tools ran. Real API calls, so it uses the same gate, behind `-Dtl.ai.injection.e2e=true` - see
`PromptInjectionE2e` next to `AssumptionAwareRunner`. Without it the tests report as **skipped**, and
without `ANTHROPIC_API_KEY` they skip for that reason instead.

```bash
source env.sh
mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.injection.e2e=true -Dtest='TestPromptInjection*'
```

`TestResearchInjection` uses the same gate for a research worker in front of a hostile page and search
result, but against an OpenAI model: it needs `OPENAI_API_KEY`, and `-Dtl.ai.injection.openai.model=...`
chooses the model (default `gpt-5.6-luna`).

### Vector-index end-to-end test

`VectorIndexIntegrationTest` embeds text with a real Mistral model and stores the vectors in a real
JVector index, so it needs an embedding key. Behind `-Dtl.ai.vector.e2e=true` - see `VectorIndexE2e`
next to `AssumptionAwareRunner`. Without it the tests report as **skipped**; the gate is also asked
by the class's `suite()`, because the setup it would otherwise build boots the very service the key
is missing for.

```bash
source env.sh
mvn -pl tl-ai-demo test -DskipTests=false -Dtl.ai.vector.e2e=true -Dtest='VectorIndexIntegrationTest'
```

### The model eval

`tl-ai-eval` puts the same seven business tasks to every configured model and grades the database
each run left behind - or, for a "show me" task, what it presented. Its **plumbing runs in every build**, against a provider whose answers a test
writes, so the scenario file's TL-Script expressions and the graders are exercised at no cost:

```bash
mvn -pl tl-ai-eval test -DskipTests=false
```

The **real models** spend real money and are behind `-Dtl.ai.eval=true` - see `EvalE2e` beside the
other gates. Without it `TestEvalPmMatrix` reports as **skipped**; the gate is also asked by the
class's `suite()`, so a build not running the eval does not boot the agent stack to report a skip.

```bash
source env.sh
mvn -pl tl-ai-eval test -DskipTests=false -Dtl.ai.eval=true -Dtest=TestEvalPmMatrix
```

The CSV lands in `tl-ai-eval/target/eval/`. How to add a scenario, how to read the CSV and - most
importantly - what the numbers may not be used for is in `tl-ai-eval/README.md`. The short version:
with a handful of scenarios this shows that a model is unusable with our harness, never that one
good model is three points better than another.

### The research questions

`research-scenarios.config.xml` in `tl-ai-eval` puts 25 research questions to Albert with
`TL_deepResearch` and grades the answer (#755): `<citations>` deterministically (read sources cited,
hosts, links to unread pages), `<judge>` by a pinned judge model of another family against a rubric.
Real models and the real web, so behind `-Dtl.ai.research.e2e=true` - see `ResearchE2e`; the
graders run in every build against a scripted model and judge (`TestResearchPlumbing`).
An arm can put the workers on another model than the lead (`<agent-models>`, `subagent-workers`
for a server with one request at a time, #784); the CSV's `subagent*` columns count what the workers
spent. `-Dtl.ai.research.arms=` selects arms; `albert-gpt-worker-qwen` needs `tl_qwen_api_key` and
the company network.

```bash
export tl_openai_api_key=... tl_anthropic_api_key=... tl_brave_api_key=...   # each "unencrypted:<key>"
mvn -pl tl-ai-eval test -DskipTests=false -Dtl.ai.research.e2e=true -Dtest=TestResearchEval \
  -Dtl.ai.research.scenarios=fact-nobel-phd -Dtl.ai.research.repeats=1
```

## Architecture

### Tool API (tl-ai-tool-api)

Extracted contract module (package `com.top_logic.tl.ai.tool.api`) enabling tools to be defined without depending on tl-ai-core internals. Key types:

- `Tool` interface — `execute(Map<String, Object> args, ToolExecutionContext context) → ToolResult`
- `ToolMode` enum - READ (parallel-safe, no approval); USER_SCOPED / DATA_WRITE / STRUCTURAL (serial, need approval, enrol in the turn's transaction); EXTERNAL (serial, needs approval, no transaction - the change lands in another system)
- Per call, not only per tool (#630): `Tool.mode(args)` says what one call changes - a dry run of a write tool is READ, so it needs no approval and stays out of the turn's transaction (`@ToolMethod(dryRun = "param")`); `Tool.refusal(args, ctx)` refuses a call before anybody is asked to approve it (`@ToolMethod(check = "method")`). `ToolCatalog.callMode` is the one answer approval, transaction and log go by. A tool still refuses the same calls in `execute`, because not every caller asks first.
- Per run (#649): `Tool.unavailableReason(ctx)` says why a tool can do nothing in this run (`@ToolMethod(unavailable = "method")`) - `present` on a page that displays no channel to show on. `AgentLoop` leaves such a tool out of the schemas of every request (`ToolPermissionFilter.offeredIn`) and answers a call that names it anyway with the reason. Not a permission: nothing is withheld from the user.
- `@ToolMethod` / `@P` — annotation framework for reflection-based tool creation
- `ToolExecutionContext` - what the framework tells a tool about the call; concrete `ToolContext` lives in tl-ai-core. `resolveObject(id)` turns the `"TypeName:id"` a model wrote into the object, under the run's read rights - which is how a `TLObject` parameter of an `@ToolMethod` is coerced, so a Java tool resolves an identifier exactly as a script tool does and cannot forget the check (#381).
- `RunInfo` - which run the call belongs to (run id, conversation, agent, model, parent run), as plain strings. `ToolExecutionContext.runInfo()` for a tool, `RunInfoBinding.current()` for code beneath one. The run's persistent `AgentExecution` is **not** readable from a tool - the store writes it at a newer revision (#405); the wrapper is a token to hand back to `AgentExecutionStore`, never an object to read. See `tl-ai-tool-api/README.md`.
- `UntrustedEnvelope` / `AnswerMarking` - the `<untrusted_…>` tags that say a tool answer is data, not
  instructions, and who puts them on. A `@ToolMethod` answer is marked as `MODEL_DATA` by default
  (#478); `marking = AnswerMarking.NONE` is for an answer that is the application's own sentence or an
  instruction the agent is meant to follow, `AnswerMarking.TOOL` for a tool that marks its own content.
  An answer that already is one complete envelope block is never wrapped twice.
- `ToolRegistryService` — service-level registry with qualified names `{factory}.{tool}`

### Agent Loop (tl-ai-core `agent/`)

`AgentLoop` is the central execution engine: builds `ProviderRequest` → streams LLM response → extracts `ToolCall`s → executes tools → loops until stop condition. `AgentFactory` constructs agents from KB definitions, wiring provider, tools, MCP servers, and skills.

Key flow: `AgentFactory.runAgent()` → `AgentLoop` → `LLMProvider.stream()` → `StreamHandler` → `ToolCatalog.execute()` → repeat.

Stop conditions: `StopReason` — COMPLETE, MAX_TURNS, MAX_TOOL_CALLS, ERROR, CONTEXT_OVERFLOW, REJECTED, ABORTED, BUDGET_EXHAUSTED, NO_ANSWER, OUTPUT_LIMIT.

**A limit ends with an answer (#775).** A run at its `max-turns`, or one that used up its tool-call
budget (`max-tool-calls` in `<agent-overrides>`, narrowed per spawn by `spawn_subagent`'s
`toolCallBudget` through `AgentLoop.limitToolCalls`), gets one last turn that keeps the tool list -
the cached prefix stays the same - but sends `ToolChoice.NONE` and a framework notice to answer with
what it has; calls a provider still returns there are dropped, and a last reply left without text is
asked for once more, the same way (#819). Calls beyond the budget are answered
"Not run"; calls of `interaction` tools spend nothing. The run ends as `MAX_TURNS` or
`MAX_TOOL_CALLS`, the chat appends a notice, and a delivered subagent result says "Stopped at its
limit". A run limited to one turn has no turn to spare and works in it. `TestRunLimitFinalTurn` pins it.

**What a turn reads (#647).** A run holds one interaction for all its turns, and an interaction reads
at the revision it read first, so `AgentLoop` moves the run's view to the newest commit before a
turn's first call is checked, and again after an approval that first call waited for - only while
the thread holds no knowledge-base context, so a run inside a caller's transaction keeps the caller's
view. The calls of one turn read one state; a tool that waits for other threads' commits within its
call catches up with `ToolExecutionContext.catchUpWithCommits()`. A commit made after the turn's view
was taken still fails the turn on a conflict (#305). `TestCommitsOfOtherThreads` pins all of it; the
rule for tool authors is in `tl-ai-tool-api/README.md`.

An installation renames a shipped agent in *Service Editor → TopLogic-Agents and Skills* (`display-name`, `summary`, first in the form); the technical `name` stays, and the change takes effect when that service is restarted - see `tl-chat/CLAUDE.md`.

### Environment block (tl-ai-core `agent/environment/`)

Every run is told, in its system prompt, who it is working for: user, UI language, date in the
user's time zone, agent, model, application, the host's stable facts and the account's own
preference text. `AgentFactory.buildConfig` composes one `<environment>` block and fills the
`{{...}}` placeholders; a prompt that names none gets the block appended, so every shipped agent has
it without an edit. An unknown placeholder is refused where it is written, not at run time.

Nothing in the block may change within a conversation - it sits in front of every request and the
providers' prompt caches key on it: day granularity, and only facts marked `stable`.

A fact that is **not** stable goes into the per-turn `<turn-context>` block instead (`TurnContext`):
its own content block of the turn's user message, ahead of the user's text, which stays exactly what
the user wrote. The framework adds the turn number and the time of day to it, `AgentLoop` attaches it
where the request is built - so it survives a compaction - and it is recorded on the turn
(`Turn#turnContext`, the persisted transcript, the run detail's transcript). The fact a page most
often has to state is **which object the user selected**, so `<chat-conversation><selection
channel="…"/>` states it in one line - label, qualified type and the `label [id:TID]` every tool
reads back (#494) - and the environment block's `{{turn_context}}` section tells the model what a
`<turn-context>` block is, for the runs that get one.

The wording is a shipped, translated resource (`tl.ai.environment.template`) that
`AgentApplicationService`'s `<environment>` section may override, the facts come from the agent
definition and from `<chat-conversation><context>`, the preference text is
`tl.accounts:Person#aiPreferences`, and the filled block is recorded on
`tl.ai.execution:AgentExecution#environment`. See `tl-ai-core/doc/environment-block.md`.

### User memory (tl-ai-core `tools/memory/`, `agent/environment/UserMemories`)

The agent keeps things about the user across conversations: `tl.ai.execution:UserMemory` rows
(owner, kind, text, origin, created), rendered into the environment block's `{{memories}}` section.
`remember(kind, text)` writes one, `recall(query)` searches the rest.

**`remember` refuses a memory the user did not agree to in that turn**, as a Java check on the call
path rather than a line in a prompt: `MemoryConsent` is built from the user's own message at the
start of the run and from what `AskUserTool` recorded, and a nested run inherits it rather than
building its own. Only a `UserMessage` whose `origin` is `USER` counts: a delivered subagent report,
a framework notice and a lead's task are marked `FRAMEWORK` or `AGENT`, so a lead turn the chat
wakes for a subagent's news has no consent (#807). The words it matches are configurable, because
they are matched against what the user typed.

The section is read **once per conversation** (`UserMemoryBinding`, bound by `ChatProcessor` from
the chat session state), because the block has to stay byte-identical across the turns of one.
`<user-memory enabled="false"/>` on `AgentPolicyService` retires tools and section together. The user sees
every memory in a `<data-list>` and deletes one with `aiMemoryDelete`. See
`tl-ai-core/doc/environment-block.md`.

### Skills and tool overlays (tl-ai-core `tools/`, `agent/skills/`)

An agent advertises its own tools plus a catalog of skill names; `loadSkill("id")` hands over the
skill's instructions as a tool result and appends its tools to `DynamicToolCatalog`. The catalog has
three layers and always advertises them in this order, whatever order they were added in: the frozen
base (the agent's `<tools>` and its MCP servers'), the framework overlays every agent carries
(`loadSkill`, `listTools`), and the skill overlays in load order. The first two are the **stable
prefix** - `ToolRegistry.stableToolCount` - and a skill restored from a previous run of the session
lands exactly where loading it would have put it, which is why the loaded-skill set is ordered from
`LoadedSkillsBinding` through to the persisted `SessionMetadata`.

Overlays are bounded: at most `DynamicToolCatalog.DEFAULT_MAX_SKILL_OVERLAYS` (8) skills keep their
tools loaded, and loading one more drops the overlay whose tools were called longest ago. The
eviction is named in the `loadSkill` answer, and a call for an evicted tool is still answered with
"load the skill that has it". There is deliberately no `unloadSkill` tool. A compaction changes
nothing in the tool array; it only makes a skill's *instructions* loadable again
(`ToolRegistry.historyCompacted`), because a summarizing compaction can replace the turn that
carried them. `AgentEvent.ContextComposition#loadedSkills` names the skills each request paid for.
See `tl-ai-core/doc/skill-overlays.md`.

`TL_init` (#667) is the counterpart of `/init` in Claude Code: Albert writes the **application
context** - purpose, users and their tasks, the users' terms, what the calculated values mean, rules.
It is development material, so it lives in the application's **source**: a `TextPromptContributor`
with `name="application-context"` among `AgentApplicationService`'s prompt contributors, in a file of
the app's `WEB-INF/autoconf`. `saveApplicationContext` (STRUCTURAL, approval-gated, administrators only)
writes it there - replacing the entry where it is, adding it to the app's last `AgentApplicationService`
file, or creating `ai-application-context.config.xml` without `config:override` - editing the XML in
place and never touching `tl_autoconf_dir`. It is offered only in the development environment
(`DevelopmentWorkspace`: `tl_operation_mode=development` and a source `src/main/webapp`); elsewhere it
is not offered and Albert hands over the document. The text takes effect when
`AgentApplicationService` ("Agent application knowledge") is restarted; the developer commits the
file. The skill reads the model in depth (the calculation rule of every derived attribute, and every
mismatch between a description and the implementation, which it reports and does not fix) and
interviews the user; it does not look at the application's objects. Albert's own write tools stay
offered while it runs - the approval guards them. `TestInitSkill` (tl-ai-eval) runs it end to end on
a scripted model, including the service restart. See `tl-ai-core/doc/init-skill.md`.

`TL_deepResearch` (#723) has Albert research a topic on the public web: it plans sub-questions,
spawns one `TL_ResearchWorker` per sub-question in one turn, waits with `wait_for_subagent` (the one
route that works with and without a chat) and writes one report that links the pages the workers
read. It keeps the run with `research.saveReport`, opens it with `present` where the page has a
place, and then answers with the same report, so a headless `aiResearch()` run keeps one too (#794). The worker carries `webSearch` and `webFetch` only - no application data, no writes - and the
lead reads no page itself. A spawned worker is unattended, so an application needs a
`<background-allowlist>` that admits `egress` and a search engine on `WebToolsService`; tl-ai-demo
has both in `WEB-INF/conf/research.config.xml`. `TestDeepResearchSkill` (tl-ai-eval) runs it on a
scripted model and a fixture web. See `tl-ai-core/doc/deep-research.md`.

### Prompt caching: three rules

Every provider caches by prefix, over the rendering **tools → system prompt → messages**; Anthropic
and Bedrock need an explicit breakpoint, OpenAI-compatible endpoints match the longest common prefix
by themselves. The repository therefore holds three rules, and a change that breaks one of them is a
cost regression nothing else reports:

1. The `<environment>` block is **stable** - day granularity, only facts marked `stable` (#437),
   and the memories it carries are the ones the conversation was opened with (#439).
2. Facts that are not stable go **in the turn**, as the `<turn-context>` content block of the user
   message, behind the whole cached prefix (#438).
3. A loaded skill's tools go **behind the stable tool prefix**, appended in load order and never
   reordered; `ProviderRequest.stableToolCount` carries that boundary and the Anthropic and Bedrock
   builders put their second breakpoint on it (#346).

### Agent services (tl-ai-core, #691)

What used to be one `LLMService` is four services, cut by who sets them, so that saving one in the
Service Editor (which writes a snapshot of its whole configuration) and restarting it touches only
what its owner tunes:

- `LLMProviderService` (`provider/`, "LLM providers" - the renamed `LLMService`): `<providers>`,
  `<model-catalog>` and `<failover>` (including `rate-limit-wait-seconds`) - which models exist and
  how to reach them.
- `AgentRuntimeService` (`agent/`, "Agent runtime"): `default-model` and `default-provider`,
  `<agent-overrides>`, `max-turns` (the turn limit of an agent whose override names none, 60 when
  unset), `chat-entry-point`, `<capacity>` and `<security-context>`. It depends on
  `LLMProviderService`, and its start refuses a default or override model that no provider exposes,
  that more than one exposes, or that the provider named for it does not.
- `AgentApplicationService` (`agent/`, "Agent application knowledge"): `<environment>`
  (`<application>`, `<template>`), `<object-ids>` and `<prompt-contributors>` - development material,
  kept in the application's source.
- `AgentPolicyService` (`agent/`, "Agent policy"): `<budget>`, `<user-memory>`, `user-preferences`
  and `max-preferences-length` - what a run may cost and what an agent may keep about a person, set
  by administration.

Restarting `LLMProviderService` restarts `AgentRuntimeService`; restarting any of the other three
touches nothing else. `chat-workers` and `chat-queue` still take effect only at an application
restart. `AgentFactory.runAgent` refuses a run while any of `AgentRuntimeService`,
`AgentApplicationService` and `AgentPolicyService` is not running, so no run starts with the default
security context, no limits, no budget or without the application's standing instructions; a test
that starts agent runs starts all three modules. An old file is refused, not read: a configuration
that still names `com.top_logic.tl.ai.core.provider.LLMService` stops the application at the start of
`LLMProviderService`, with a message naming the file and where each section it sets now belongs.
Code reaches the providers through `LLMProviderService.getInstance()`; `resolveModelAssignment` and
`getDefaultModel` are on `AgentRuntimeService`. `TestAgentRuntimeService`,
`TestAgentApplicationService`, `TestAgentPolicyService` and `TestOldLLMServiceConfiguration` pin it.
Where every old property went: `tl-ai-core/doc/llm-service-migration.md`.

### Run limits (tl-ai-core `limits/`)

The ceilings an administrator **may** put on what this installation carries and what one account
may spend, configured in `<capacity>` on `AgentRuntimeService` (the chat pool and the per-account
cap) and `<budget>` on `AgentPolicyService` (the token budget) (#343, #691). **All of them are off by
default** - this product runs in applications with a thousand users, and any figure it picked for
them would refuse work the installation allowed before the upgrade:

- **`chat-workers`** (0 = no ceiling) bounds `ChatRunExecutor`. At 0 it is the cached pool the chat
  always had, a worker per send. With a ceiling, a send that finds every worker busy **waits**:
  `chat-queue` is 0 = a queue of no fixed length, because somebody who asked a question wants the
  answer late rather than not at all. An installation that would rather refuse bounds the queue
  (`chat-queue="10"`) or keeps none (`chat-queue="-1"`); only then is a send refused, *before*
  anything is written down, as HTTP 429 with a sentence the client shows.
- **`concurrent-runs-per-account`** (0 = off, 3-5 recommended for a shared installation),
  **`token-budget`** and **`money-budget`** over `budget-window-hours` (0 = unlimited, per-role
  overrides possible) are
  enforced by `RunLimits.admit` in `AgentFactory.runAgent` - the choke point every entry point
  arrives at. These two refuse rather than queue: the account is already being served.
- **Background subagents** queue rather than refuse (#710): a chat's `SubagentJobRegistry` runs at
  most its cap (4) at once, and **`subagent-workers`** in `<capacity>` (0 = off) bounds them over the
  whole installation (`SubagentSlots`). A job beyond either stays `QUEUED`, starts in registration
  order when one ends, and the spawn answer says "queued, position N"; a job cancelled while queued
  ends `CANCELLED` without running.

A **delegated** run is neither counted nor budgeted separately: it is part of the run that delegated
it, and shares that run's `BudgetAllowance`. The budget is computed from the recorded
`AgentExecution` rows by `AgentExecutionStore.spentSince`, on the store's writer thread; the
headroom rides on `ToolContext.budgetAllowance()`, and a run that spends it stops **between turns**
with `StopReason.BUDGET_EXHAUSTED` (recorded as `ExecutionStatus.REJECTED`).

**Money (#722).** `AgentLoop` prices every request with the model that answered it (`RunCosts`,
`CostCalculator`: input, output, reasoning, cache read and cache write each at its own price, the
over-200k tier per request), converts the dollar price into the one `currency` of `<budget>` at its
fixed `exchange-rate`, and the run records `cost` and `currency` on its `AgentExecution`. Prices are
the models.dev catalog's, or the `*-price` attributes of a `<model-overrides>` entry on
`LLMProviderService`; a model without a price costs 0. The refusal names the amounts in the user's
number format. See `tl-ai-core/doc/run-limits.md`.

### Provider System (tl-ai-core `provider/`)

`LLMProvider` interface with streaming-first design. Four implementations: `AnthropicProvider`, `OpenAIProvider`, `MistralProvider`, `XAIProvider`. All use Java `HttpClient` + msgbuf JSON.

`LLMProviderRegistry` (TopLogic service) maps model names → providers with auto-discovery via `ProviderModelDiscovery`. Supports configurable base URLs for OpenAI-compatible endpoints (local models, custom providers).

### Model health and failover (tl-ai-core `provider/health/`)

A model that could not answer is remembered: `ModelAvailability` (a component of `LLMProviderService`, not a
service of its own) turns the `ProviderException.Kind` the providers already produce into a mark -
`QUOTA` sticky, `CAPACITY` for ~30 s, `TRANSIENT` costing one attempt per turn - and a weaker cause
never replaces a stronger mark that still holds. `INVALID_REQUEST` and `CONTEXT_WINDOW_EXCEEDED`
mark nothing: they would fail the same way on the next model.

`LLMProviderService`'s `<failover>` section says where a run goes instead, per model name. It is **empty by
default and an empty section changes nothing**. Where one is configured, `AgentLoop.callRouted`
moves the run to the next healthy model of the chain **for the rest of the run** - not for the turn,
because every provider caches by prefix - rebuilding the provider, the limits and the sampling
options for the target while the tool schemas and the response format travel unchanged. Every model
passed over is named on the `AgentEvent.ModelSwitched`, in a `MODEL_CHANGED` transcript part and in
the chat's notice line; `AgentExecution.model` is corrected to what answered. `aiModelHealth()` /
`aiModelHealthReset(model)` read and clear the marks. See `tl-ai-core/doc/model-failover.md`.

### Tool System (tl-ai-core `tools/`)

- `AnnotatedTools` — reflection engine scanning `@ToolMethod` methods into `Tool` instances (lives in tl-ai-tool-api)
- `ToolCatalog` — frozen registry with JSON Schema validation
- Subpackages: `model/` (browser/editor/search), `script/` (TL-Script executor), `interaction/` (delegation, ask-user, skill), `task/` (task store, task binding), `config/` (schema, layouts), `integration/` (Gitea), `registry/` (factories), `util/` (serialization, i18n)
- `ObjectEditTool` - `object-edit.updateObject` / `createObject` / `deleteObject` (#625), core tools of Albert: one object the agent knows by its id, changed in one call. Values are converted on the server by the attribute's type (`AttributeValues`), the model's constraints are asked before the commit (`ObjectConstraints`), rights are checked per object and per attribute before the first write - for a new object's initial values with the engine's create-time question (`isAllowedInitial`, #29692), as the create dialogs ask it - and a call in a system context is refused - the engine checks no rights there and records no user. `createObject` takes the new object's parts in `values` (a composition's objects, nested up to three levels) and creates the whole tree in one call and one approval, with the constraints asked once it is complete (#630) - which is what makes an object with a mandatory composition creatable at all. A set chosen by a condition stays one TL-Script, through `changeData`. See `doc/security/agent-security-architecture.md` 3.3.5.
- Reading and changing data with TL-Script are two tools (#630): `query` / `present` (READ, `data.read`) and `script-exec.changeData` (DATA_WRITE, `script`, making the change costs `data.write` on top). `changeData` refuses a script that only reads before any approval prompt; its dry run runs the change in an interaction of its own, in a transaction that is always rolled back, and answers what it would change (`ChangeReport`) - only when every effect is a write to the knowledge base. What is a read is decided on the whole expression (`SideEffects`), not by the engine's `HasSideEffects`, which skips function arguments. The verified cheat sheet of the language is `QueryLanguage`: a query that fails (it does not compile, or fails while it runs) is answered with its reading part, and a skill gets the whole sheet in place of `<<QUERY_LANGUAGE_CHEAT_SHEET>>`.
- `TidResolver` turns a model-supplied `TypeName:id` into a `TLObject`, and is the only place that does - every call is a read check. An identifier that does not resolve is classified by `TidDiagnostics` rather than answered with one generic sentence: a uuid, a printed object, an unknown or abstract type, an id of the wrong shape, or a code this application speaks (a pattern and a sentence in `<object-ids>` on `AgentApplicationService`, which gets first say). Error path only. All of these messages are model-facing English literals, not resources (see the Coding Rules).

### MCP Integration

**Client side** (tl-ai-core `mcp/`): `McpServerRegistry` manages connections to external MCP servers (stdio/HTTP transports). `McpTool` wraps remote tools with sanitized names `mcp__{server}__{tool}`. Agents reference MCP servers by name.

**Server side** (tl-ai-mcp-server): Exposes TopLogic model as MCP resources (`toplogic://instance/{tid}`, `toplogic://model/modules/...`) and publishes the registry tools its `<exposed-tools>` lists (none by default) via `McpToolAdapter`, with `<instructions>` and an `allowed-origins` check. Supports dynamic TL-Script-based resource templates.

### Event System (tl-ai-core `agent/`)

`AgentEvent` sealed interface with 25 record subtypes covering run/turn/tool/approval/transaction/memory lifecycle. `AgentEventListener` enables real-time SSE streaming and structured logging.

### Chat UI (tl-chat)

`ChatServlet` at `/chat/*` handles REST + SSE. `ChatProcessor` orchestrates background agent execution. `ChatSSEHandler` implements `AgentEventListener` for real-time push (50ms flush batching, 30+ event types).

One front end consumes that API, the React chat (`tl-chat/ui/`): TypeScript + Vite, mounted into the React view layer by the composed view elements `<chat-conversation>`, `<chat-rail>` and `<chat-dock>`. See `tl-chat/CLAUDE.md` for the toolchain and the dev loop. It is shown in two kinds of page:

- **React pages** (`/view/`), where the view elements stand in a `.view.xml`.
- **Classic LayoutServlet pages**, through `tl-chat-classic` (#618): `ReactViewComponent` writes a React view into a classic tab, with the classic window as the React window. A host gets the "Albert" tab by depending on `tl-chat-classic`. The bridge is temporary - `LayoutComponent` is to leave tl-core - and works around five engine gaps inside tl-ai, each marked in the code; see `tl-chat-classic/CLAUDE.md`. It replaced the hand-written `chat-client.js` control.

`ChatSessionManager` maintains per-session state (Chat + ConversationMemory + ConversationLog) with LRU cache. `ChatHistoryService` persists to KnowledgeBase.

### Security & Approval

Multi-layer: `SecurityContext.hasToolPermission()` → `ExecutionMode` gate (GLOBAL tools blocked in PRODUCTION) → `PermissionDecision` (Allowed/Denied/NeedsApproval) → `ApprovalCallback` for human-in-the-loop. Session-level approval caching.

**Which `SecurityContext` a run gets is configurable (#381).** Every entry point - chat, TL-Script, MCP, the prompt improver - builds it through `RunSecurity.contextFor(RunPrincipal)`, which asks the `<security-context>` factory on `AgentRuntimeService`. The default is `DefaultSecurityContextFactory`, i.e. `TopLogicSecurityContext.forRun`; an application that must answer approval from the *call* rather than from the tool's `ToolMode` extends `SecurityContextProxy` over the default and overrides `checkApproval` alone. Never build `TopLogicSecurityContext` at a new entry point - go through `RunSecurity`, or the installation's factory is skipped. See `doc/security/agent-security-architecture.md` 4.5.

**Rendering an answer is a security boundary (#385).** The text is model output, so it carries whatever
`webFetch` and the tool results put in front of the model, and it is persisted and re-rendered on every
reopen. Make HTML from it **only** through `Converters.markdownToHtmlString`, which escapes raw HTML and
then filters the rendered result through `HtmlSanitizer`'s allow-list - tags, attributes and URL schemes.
Both layers are needed: escaping cannot see `[text](javascript:...)`, because that is Markdown, not HTML.
The React chat has its own filter (`marked` → `DOMPurify` in `tl-chat/ui/src/markdown.tsx`). Do not widen
either allow-list without a test in `TestHtmlSanitizer` / `markdown.test.ts` saying why, and never write
model text into a page with `writeContent`, `innerHTML` or `dangerouslySetInnerHTML` around them.

**Browsing governance is configuration, not a tool argument (#347).** `<browsing>` on `WebToolsService`
says which origins a run may reach, resolved per security role (`BrowsingPolicyConfig` →
`BrowsingPolicies.forRoles` → `BrowsingPolicy`), with an `access` and a `downloads` axis per origin and
`*.example.com` / exact-host patterns; deny beats allow, and a policy name nobody declared is a boot
error. It is the **ceiling** for all three routes out: the browser (its filtering proxy, now one per
policy rather than one per service), `webFetch` (every hop) and `webSearch` - whose model-supplied
`allowedDomains`/`blockedDomains` can only narrow it, enforced by filtering the hits that come back and
not only by asking the engine. It composes with `BrowserToolsService`'s installation-wide
`allowed-domains`/`blocked-domains` (both must permit) and relaxes nothing in `UrlSafety`, which stays
the unconditional SSRF check. Configure nothing and nothing changes. Administrators are **not** exempt
here, unlike in #392 - this is a statement about the destination, not about the account. See
`doc/security/browsing-governance.md`.

**A worker opens only addresses it was given (#736).** Under `url-provenance` on `WebToolsService`
(default `SUBAGENTS`: every run another run started) `webFetch` and `browserNavigate` refuse an address
that the run's user or lead did not write, that no search returned and that no page it read links to -
so an injected page cannot make a research worker write its task or findings into an address. The
ledger is `SeenUrls` on the run's `ReadingList`: `AgentLoop` adds the addresses of the user's and the
lead's messages at each turn start (not framework notices, subagent reports or summaries), the web tools
add search result addresses and page links (`UrlProvenance.record*`). Never add addresses from a tool
answer that echoes its arguments - the model could launder a composed address through it.
The browser session asks the same ledger for every navigation it makes for such a run (#813): a link or
form must lead to a given address, and after `browserType` no navigation sends a body and no frame loads
from an unseen address; an address in a text the run typed is never taken from a page.
`TestUrlProvenance` pins the rule, `TestBrowserUrlProvenance` (behind `-Dtl.ai.browser.e2e=true`) the
browser's half, `TestResearchInjection` (behind `-Dtl.ai.injection.e2e=true`, OpenAI)
puts a real model in front of it.

### Served binaries: two routes, two rules

`tl-ai-core` serves content on two routes, and they are deliberately not alike:

- `/html/{id}` (`GeneratedHtmlPageServlet`) answers **anonymously**. A page `publishPage` stored is
  meant to be handed around, so its unguessable 128-bit id is the capability, and the answer carries
  a sandbox CSP so LLM-authored HTML cannot act in the app's origin.
- `/screenshot/{id}` (`ScreenshotServlet`, #216) requires a **login** and then asks
  `ScreenshotService.mayRead` - the owner, or an administrator. A screenshot is a picture of a page
  somebody drove a browser to on their own account; the id is defence in depth, not the rule. A
  foreign id and an unknown id both answer 404, so the route says nothing about which ids exist.

`browserScreenshot` returns `ScreenshotService.viewUrl(...)` and tells the model to write it as a
Markdown image, which both rendering paths keep (`HtmlSanitizer`, `markdown.tsx`). The browse/list
view over `tl.ai.screenshots:Screenshot` comes with the React admin views (#452). See
`tl-ai-core/doc/screenshots.md`.

A third route follows the screenshot rule: `/archived-page/{handle}` (`ArchivedPageServlet`, #617)
shows the copy of a web page an agent run read - login, then the reading account or an administrator,
404 for unknown and foreign handles alike, rendered through `Converters.markdownToHtmlString` under a
CSP that loads nothing.

### Citations (tl-ai-core `citation/`)

An answer cites with links in its text, `[Sommerfest](tl:GenericObject:11047)` or
`[Quelle](https://...)`, and calls no tool for it (#617). The **system writes the reading list**: every read tool puts what
it read on the run's `ReadingList` (objects where `ReadFilter` discloses an identity, pages in
`webFetch` and the browser, an application's records through `ToolExecutionContext.readSource`), and
`CitationValidator` keeps a link as a footnote only when it points at an entry of that list, of a kind
declared in `CitationKinds`. A sub-agent's sources join the lead's list when its result is delivered.
Every page that enters a reading list is archived by `PageArchive` (`tl.ai.execution:ArchivedPage`,
bounded and with retention), and its footnote offers the copy. An application adds its own marker
syntax with a `CitationMarkerSyntax` in `<marker-syntaxes>`. See `tl-chat/doc/citation-contract.md`.

A research run also records **findings** on the same list (#752): `research.recordFinding(claim,
source, quote)` refuses a quote that does not occur in the text the run read of that source
(`Finding.occursIn`, white space, Markdown escapes and link addresses aside), the findings join the lead's list with the
sources on every delivery path, and a delivered report is followed by the worker's findings, one line
each. The lead works from them with `research.listFindings` and reads a source's stored text with
`research.readSource` - it never fetches. All three are READ tools of the `interaction` group.

When the lead has written its report it calls **`research.saveReport`** (#764): the report's links are
validated against the run tree's reading list, and `ResearchRuns` keeps a `tl.ai.execution:ResearchRun`
on its own thread - question, effort, plan, status, the Markdown, one self-contained HTML document
(`ResearchReportHtml`: the text with footnotes linked to the cited sources, only tags the HTML safety
check allows), the cited sources, the sources read and not cited (`alsoRead`, `<collapsed/>`), and the
findings split the same way. Owner and administrators read it (role rules in
`tl-ai-core.binarySecurity.config.xml`, as for screenshots). The tool answers the run's id, and `present`
opens it in the chat's side pane, which shows the document's body in place and folds the collapsed
parts. The owner is the account the run works for, so a headless `aiResearch()` run (#767) keeps its
run for the account it runs as, the same way - as soon as the `TL_deepResearch` skill calls the tool.

### Memory & Compaction (tl-ai-core `memory/`)

`ConversationMemory` wraps conversation with hybrid token estimation (API baseline + byte heuristic). `CompactionStrategy` interface: `SummarizingStrategy` (LLM-based) and `TruncatingStrategy`.

**When the budget is checked (#367).** `AgentLoop` checks it at the **start of every turn** -
after the between-turn injections and *before* the turn's request is built - so a conversation
that is already over budget is compacted rather than sent, refused and compacted. The same
position covers the other direction: a turn's tool results are measured at the start of the next
turn, which is the next moment that history is used. Consequences worth knowing:

- a run does **not** compact after its last turn; the next run compacts before its first request,
  so no summarizer call is spent on a conversation nobody continues;
- the `CompactionStarted`/`CompactionCompleted` pair of a proactive compaction precedes the turn's
  `ContextComposition`, not follows it;
- a provider that reports `CONTEXT_WINDOW_EXCEEDED` anyway - the estimator cannot see the system
  prompt or the tool schemas - is still caught by the reactive retry in
  `callProviderWithReactiveCompaction`, which is the fallback, not the normal path.

`TestCompactionOrder` pins all of this.

**A child compacts too (#698).** A subagent or a synchronous delegate brings no session memory, so
`AgentLoop.run(Conversation, CompactionStrategy, ToolContext, AgentEventListener)` wraps its own
conversation in a `ConversationMemory` sized to its model's window, with
`CompactionStrategies.forDefaultModel()` - the strategy the chat's session memory uses. Both
`DelegationTool.executeSync` and `AgentRunner.run(AgentLoop, ...)` (the spawner's path) run that way;
`TestChildCompaction` pins it.

### Multi-Agent Delegation

`DelegationTool` creates isolated sub-agent runs (max depth 3). Parent passes `TransactionScope` and `SecurityContext`. Child events recorded in parent's `ConversationLog`.

`spawn_subagent` is the asynchronous half: fire-and-forget, the turn ends, the child works on in a
`SubagentJobRegistry` bound to the session. The registries belong to the process, not to the chat
(`SubagentRegistries`, one per conversation, and in the chat per window): `AgentRunner` binds the
conversation's for every top-level run that has none bound, so TL-Script, the eval and scheduled runs
can spawn too, and a chat window that drops or reloads a conversation leaves its children running;
only deleting the chat cancels them. After a restart the startup sweep fails the children's runs, and
a reopened conversation ends their parts as failed (`InterruptedTranscript`). Its result reaches the parent on one of two paths, and
both render it through `SubagentResultDelivery` so the parent reads the same block either way:
`wait_for_subagent` when the parent asked, and `AgentLoop.injectSubagentResults` at the start of the
next turn when it did not.

When the parent has no next turn coming, the chat **wakes** it: `SubagentWaker` (tl-chat) starts a
lead turn that opens with the news - a finished child or an `ask_lead` question - instead of with a
user message, drawn as a line in place of the bubble. At most five such turns between two messages of
the user, none after Stop, none while a lead turn runs (that turn reads the news itself). The line
names each subagent and why it reported back - finished, failed, cancelled, or asks and what - and
each name scrolls to that subagent's card: `AgentLoop.injectSubagentNews` says who was delivered,
`AgentEvent.LeadWoken` carries it as `SubagentNews`, and the turn records it as a `LEAD_WOKEN` system
part, which replay skips (#721).

The injection is a **`UserMessage`**, not a `SystemMessage`: no provider's request builder transmits
a mid-conversation system message, so that shape never reached the model (#359). It opens with
`SubagentResultDelivery.NOTICE`, which says in the text what the role cannot - the user did not write
this. The child's own text sits inside `<untrusted_subagent_report>` on every path - injection, wait,
status and the synchronous `delegate_to_*` result - because a worker repeats what it read.
Everything finishing in one gap becomes one message, in the order the parent started the subagents.
Being an ordinary user message, it is compacted like any other turn content.

The same text is recorded on the turn as a `Part.SystemPart` of type `SUBAGENT_RESULT`: an assistant-side
part, so the admin transcript shows it while the chat does not draw it as a user bubble.
`ConversationLogReplay` turns that part back into the identical `UserMessage`, which is the one system
event replay does not skip. See `tl-ai-core/doc/subagent-results.md`.

A background subagent asks the user - `askUser`, or an approval - through **its job's own store**
(`SubagentJob.interactionStore()`), not the store of the turn that started it: that turn ends while
the child works on, and the chat unbinds its store with it. The store names the child
(`InteractionAsker`), and the chat registers it in `PendingInteractionRegistry` while the job runs,
so the SSE poll shows the question with the child's name and `/chat/respond` reaches it from any
window of the account (#695).

What a running child does is kept on its job (#700): the spawner feeds the child's events into a
bounded `SubagentProgress` (turns, tokens, the last 20 tool calls with an argument summary and their
outcome, the end of its last text), sets `childRunId` at `RunStarted` and hangs the child's
`ConversationLog` on the job as its live log. `subagent_status(detail: summary|steps)` reads it, says
`WAITING` and for what when the job's store holds a question or an approval, and puts what the child
wrote inside `<untrusted_subagent_report>`; `GET /chat/subagents/{id}/transcript` serves the live log
until the child ends.

The chat shows each child as a card at the tool call that started it (#702): `SubagentStarted` and
the job carry `parentToolCallId`, the browser keeps a child's live events per subagent id
(`subagentLive`, never in the lead's transcript) and `thread.tsx` draws one `SubagentCard` per child
under the steps of the turn that spawned it. The lead's log keeps one `Part.SubagentPart` per child -
id, agent, task, parent call, run id, end state, a short summary - written at start and ended in place,
never the child's steps (owner decision D5); a reloaded conversation rebuilds the card from it, and
`GET /chat/subagents/{id}/transcript?runId=` opens the recorded run when the server holds no job.
The lead's run has usually written its transcript before the child ends, so the end is written into
that transcript as well (`AgentExecutionStore.replaceTranscriptPart`), before it is announced; the end
frames carry `parentToolCallId` and `childRunId`, and every other window holding a copy of the chat
reads it back with its next request (#720). `TestSubagentEndInEveryWindow` pins it.

The lead steers a running child with `send_to_subagent` and stops it with `cancel_subagent` (#705).
A message goes into the job's `SubagentInbox`, which the spawner binds on the child's thread;
`AgentLoop.injectLeadMessages` drains it at the start of each child turn into one user message opening
with "[Message from the lead agent]", announced as `AgentEvent.LeadMessageDelivered`, which is what
records it as a `LEAD_MESSAGE` part in the child's own log (the thread-bound listener on a child's
thread records the lead's log) and what replay turns back into the same message. A message that arrives
while the child writes its answer gets it one more turn; the inbox closes in the same step as the check
for that, so a later message is refused, and one that found no turn left (limit, failure, cancel) is named
in the result the lead reads (#804). Text only, it changes no tool, right or model.

A child asks its lead with `ask_lead` (#707), a tool only a background subagent's catalog has
(`AgentFactory`, the `includeDelegation=false` branch): the question is parked on the job
(`LeadQuestion`, the job bound by the spawner), the lead is shown it - enveloped - at the start of its
next turn together with any results (`SubagentResultDelivery.deliveryText(finished, asking)`), or at
once when it waits in `wait_for_subagent`, which a question ends early; the lead answers with
`reply_to_subagent`, and the child goes on in the same turn. The question timeout ends the wait with
"decide yourself and state the assumption"; a cancel ends it too. Waking an idle lead for a question is
the wake's job (issue 4 of the series).

## Project Structure

Standard Maven layout. Key package roots:

| Module | Base package |
|--------|-------------|
| tl-ai-tool-api | `com.top_logic.tl.ai.tool.api` (+ `.registry`) |
| tl-ai-core | `com.top_logic.tl.ai.core.*` (agent, provider, tools, message, memory, mcp, session, scripting) |
| tl-chat | `com.top_logic.ai.chat.*` (api, layout, history) |
| tl-ai-mcp-server | `com.top_logic.ai.mcp.server.*` (tools, resources, dynamic, completions) |
| tl-ai-vector-index | `com.top_logic.ai.vector.index.*` |

**Key config files per module:**
- Model definition: `src/main/webapp/WEB-INF/model/tl.ai.<module>.model.xml`
- Application config: `src/main/webapp/WEB-INF/conf/tl-ai-<module>.conf.xml`
- Auto-config: `src/main/webapp/WEB-INF/autoconf/model.tl.ai.<module>.config.xml`

**TopLogic parent POMs:** `tl-parent-core` for library modules, `tl-parent-app` for application modules.

**Important:** Modules use `web-fragment.xml` (in `src/main/resources/META-INF/`) for servlet registration, not `web.xml`. Only top-level applications use `web.xml`.

## Coding Rules

- **DO NOT** manually edit `messages_en.properties` - the build's doclet regenerates it wholesale from the `@en` JavaDoc tags
- **DO** write both `@en` and `@de` on every `ResKey` constant, and copy the `@de` text verbatim into the module's `messages_de.properties`. No generator reads `@de` (Gitea #331): the doclet exports `@en` to `messages_en.properties`, and `tl-maven-plugin:translate` machine-translates from that English alone - silently, and often wrongly ("Stornierung beantragen" for a cancel-run command). It does that for a key with no German yet **and** for a key whose English changed since the last build, so rewording an `@en` text discards the hand-written German of that key without a word. `@de` is the authored source of truth and `messages_de.properties` is maintained by hand; the tag alone ships nothing.
  Adding a resource key is therefore three edits, and the guard checks all three:
  1. `@en` on the constant - the doclet writes `messages_en.properties` from it, never edit that file.
  2. `@de` on the same constant - required. A constant with `@en` and no `@de` fails the build's check, because its German would be an unread machine translation.
  3. The same German text in the module's `src/main/java/META-INF/messages_de.properties`.
  Then run the guard: `mvn -pl tl-ai-core test -DskipTests=false -Dtest=TestGermanResourcesMatchJavaDoc`. It walks every module of the reactor, so run it once from `tl-ai-core` whichever module you changed. It fails when a documented constant has no `@de`, when the shipped German is not the `@de` text, when a `@de` text reaches no bundle, and when a bundle holds a `[missing:...]` generator placeholder.
  Config-property labels and tooltips have no `@en`/`@de` mechanism - the doclet derives them from the JavaDoc prose and the German stays machine-translated, so read it after adding a config option.
- **DO NOT** make a `ResKey` out of a tool's answer to the model - its result text, its refusal, its `@ToolMethod` description. Those are English literals next to the code that writes them (`TidDiagnostics`, `ToolResults`, `PresentTool`, every tool in `tools/`), because they are read by the model rather than by the user: translating them would make what a run does depend on who started it. The exception is text that is *about* the user and is meant to reach them through the model - the environment block's wording (`tl.ai.environment.template`) is a translated resource for exactly that reason. Text a **person** reads - a label, a command, an error in the UI - follows the `@en`/`@de` rule above.
- **DO NOT** add `@author` tags to JavaDoc (use git history)
- **DO NOT** create hand-written POJO DTOs — use msgbuf `.proto` files (see `/create-dto` skill)
- **DO NOT** use Jackson (`ObjectMapper`) — use msgbuf `JsonWriter`/`JsonReader`
- **DO NOT** add Claude signatures or `Co-Authored-By` lines to commits
- **DO** include user prompts as the last section of commit messages (see `/commit` command)
- **DO** include `@see` on config property constants pointing to getter (see `/toplogic-service` skill)
- Use `RuntimeException` for service startup errors, `ConfigurationError` only for user-facing errors (see `/toplogic-service` skill)

## Notes

- UTF-8 encoding throughout
- Test framework: JUnit (via TestCase base class)
- All LLM providers use streaming — no blocking request/response calls
- Services are TopLogic `ConfiguredManagedClass` singletons: `LLMProviderRegistry`, `ToolRegistryService`, `McpServerRegistry`
- Thread-local bindings: `TaskStoreBinding`, `UserInteractionStoreBinding`, `ConversationLogListenerBinding`
- Maven compiler configured with `parameters=true` for reflection-based tool annotation support
