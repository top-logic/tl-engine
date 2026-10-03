# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Custom AI agent framework built on TopLogic, replacing LangChain4j with a purpose-built streaming agent loop. Multi-module Maven build (parent: `tl-parent-core` version `8.0.0-alpha9`; `tl-ai-demo` pins `tl-parent-app` at the same engine version separately).

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

## Architecture

### Tool API (tl-ai-tool-api)

Extracted contract module (package `com.top_logic.tl.ai.tool.api`) enabling tools to be defined without depending on tl-ai-core internals. Key types:

- `Tool` interface — `execute(Map<String, Object> args, ToolExecutionContext context) → ToolResult`
- `ToolMode` enum - READ (parallel-safe, no approval); USER_SCOPED / DATA_WRITE / STRUCTURAL (serial, need approval, enrol in the turn's transaction); EXTERNAL (serial, needs approval, no transaction - the change lands in another system)
- Per call, not only per tool (#630): `Tool.mode(args)` says what one call changes - a dry run of a write tool is READ, so it needs no approval and stays out of the turn's transaction (`@ToolMethod(dryRun = "param")`); `Tool.refusal(args, ctx)` refuses a call before anybody is asked to approve it (`@ToolMethod(check = "method")`). `ToolCatalog.callMode` is the one answer approval, transaction and log go by. A tool still refuses the same calls in `execute`, because not every caller asks first.
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

Stop conditions: `StopReason` — COMPLETE, MAX_TURNS, ERROR, CONTEXT_OVERFLOW, REJECTED, ABORTED, BUDGET_EXHAUSTED.

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

The wording is a shipped, translated resource (`tl.ai.environment.template`) that `LLMService`'s
`<environment>` section may override, the facts come from the agent
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
building its own. The words it matches are configurable, because they are matched against what the
user typed.

The section is read **once per conversation** (`UserMemoryBinding`, bound by `ChatProcessor` from
the chat session state), because the block has to stay byte-identical across the turns of one.
`<user-memory enabled="false"/>` on `LLMService` retires tools and section together. The user sees
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

### Run limits (tl-ai-core `limits/`)

Three ceilings an administrator **may** put on what this installation carries and what one account
may spend, configured in one `<run-limits>` section beside `<environment>` and `<user-memory>` on
`LLMService` (#343). **All three are off by default** - this product runs in applications with a
thousand users, and any figure it picked for them would refuse work the installation allowed before
the upgrade:

- **`chat-workers`** (0 = no ceiling) bounds `ChatRunExecutor`. At 0 it is the cached pool the chat
  always had, a worker per send. With a ceiling, a send that finds every worker busy **waits**:
  `chat-queue` is 0 = a queue of no fixed length, because somebody who asked a question wants the
  answer late rather than not at all. An installation that would rather refuse bounds the queue
  (`chat-queue="10"`) or keeps none (`chat-queue="-1"`); only then is a send refused, *before*
  anything is written down, as HTTP 429 with a sentence the client shows.
- **`concurrent-runs-per-account`** (0 = off, 3-5 recommended for a shared installation) and
  **`token-budget` over `budget-window-hours`** (0 = unlimited, per-role overrides possible) are
  enforced by `RunLimits.admit` in `AgentFactory.runAgent` - the choke point every entry point
  arrives at. These two refuse rather than queue: the account is already being served.

A **delegated** run is neither counted nor budgeted separately: it is part of the run that delegated
it, and shares that run's `TokenAllowance`. The budget is computed from the recorded
`AgentExecution` rows by `AgentExecutionStore.tokensSpentSince`, on the store's writer thread; the
headroom rides on `ToolContext.tokenAllowance()`, and a run that spends it stops **between turns**
with `StopReason.BUDGET_EXHAUSTED` (recorded as `ExecutionStatus.REJECTED`). See
`tl-ai-core/doc/run-limits.md`.

### Provider System (tl-ai-core `provider/`)

`LLMProvider` interface with streaming-first design. Four implementations: `AnthropicProvider`, `OpenAIProvider`, `MistralProvider`, `XAIProvider`. All use Java `HttpClient` + msgbuf JSON.

`LLMProviderRegistry` (TopLogic service) maps model names → providers with auto-discovery via `ProviderModelDiscovery`. Supports configurable base URLs for OpenAI-compatible endpoints (local models, custom providers).

### Model health and failover (tl-ai-core `provider/health/`)

A model that could not answer is remembered: `ModelAvailability` (a component of `LLMService`, not a
service of its own) turns the `ProviderException.Kind` the providers already produce into a mark -
`QUOTA` sticky, `CAPACITY` for ~30 s, `TRANSIENT` costing one attempt per turn - and a weaker cause
never replaces a stronger mark that still holds. `INVALID_REQUEST` and `CONTEXT_WINDOW_EXCEEDED`
mark nothing: they would fail the same way on the next model.

`LLMService`'s `<failover>` section says where a run goes instead, per model name. It is **empty by
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
- `ObjectEditTool` - `object-edit.updateObject` / `createObject` / `deleteObject` (#625), core tools of Albert: one object the agent knows by its id, changed in one call. Values are converted on the server by the attribute's type (`AttributeValues`), the model's constraints are asked before the commit (`ObjectConstraints`), rights are checked per object and per attribute before the first write, and a call in a system context is refused - the engine checks no rights there and records no user. `createObject` takes the new object's parts in `values` (a composition's objects, nested up to three levels) and creates the whole tree in one call and one approval, with the constraints asked once it is complete (#630) - which is what makes an object with a mandatory composition creatable at all. A set chosen by a condition stays one TL-Script, through `changeData`. See `doc/security/agent-security-architecture.md` 3.3.5.
- Reading and changing data with TL-Script are two tools (#630): `query` / `present` (READ, `data.read`) and `script-exec.changeData` (DATA_WRITE, `script`, making the change costs `data.write` on top). `changeData` refuses a script that only reads before any approval prompt; its dry run runs the change in an interaction of its own, in a transaction that is always rolled back, and answers what it would change (`ChangeReport`) - only when every effect is a write to the knowledge base. What is a read is decided on the whole expression (`SideEffects`), not by the engine's `HasSideEffects`, which skips function arguments. The verified cheat sheet of the language is `QueryLanguage`: a query that fails (it does not compile, or fails while it runs) is answered with its reading part, and a skill gets the whole sheet in place of `<<QUERY_LANGUAGE_CHEAT_SHEET>>`.
- `TidResolver` turns a model-supplied `TypeName:id` into a `TLObject`, and is the only place that does - every call is a read check. An identifier that does not resolve is classified by `TidDiagnostics` rather than answered with one generic sentence: a uuid, a printed object, an unknown or abstract type, an id of the wrong shape, or a code this application speaks (a pattern and a sentence in `<object-ids>` on `LLMService`, which gets first say). Error path only. All of these messages are model-facing English literals, not resources (see the Coding Rules).

### MCP Integration

**Client side** (tl-ai-core `mcp/`): `McpServerRegistry` manages connections to external MCP servers (stdio/HTTP transports). `McpTool` wraps remote tools with sanitized names `mcp__{server}__{tool}`. Agents reference MCP servers by name.

**Server side** (tl-ai-mcp-server): Exposes TopLogic model as MCP resources (`toplogic://instance/{tid}`, `toplogic://model/modules/...`) and adapts all registered tools via `McpToolAdapter`. Supports dynamic TL-Script-based resource templates.

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

**Which `SecurityContext` a run gets is configurable (#381).** Every entry point - chat, TL-Script, MCP, the prompt improver - builds it through `RunSecurity.contextFor(RunPrincipal)`, which asks the `<security-context>` factory on `LLMService`. The default is `DefaultSecurityContextFactory`, i.e. `TopLogicSecurityContext.forRun`; an application that must answer approval from the *call* rather than from the tool's `ToolMode` extends `SecurityContextProxy` over the default and overrides `checkApproval` alone. Never build `TopLogicSecurityContext` at a new entry point - go through `RunSecurity`, or the installation's factory is skipped. See `doc/security/agent-security-architecture.md` 4.5.

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

### Multi-Agent Delegation

`DelegationTool` creates isolated sub-agent runs (max depth 3). Parent passes `TransactionScope` and `SecurityContext`. Child events recorded in parent's `ConversationLog`.

`spawn_subagent` is the asynchronous half: fire-and-forget, the turn ends, the child works on in a
`SubagentJobRegistry` bound to the session. Its result reaches the parent on one of two paths, and
both render it through `SubagentResultDelivery` so the parent reads the same block either way:
`wait_for_subagent` when the parent asked, and `AgentLoop.injectSubagentResults` at the start of the
next turn when it did not.

The injection is a **`UserMessage`**, not a `SystemMessage`: no provider's request builder transmits
a mid-conversation system message, so that shape never reached the model (#359). It opens with
`SubagentResultDelivery.NOTICE`, which says in the text what the role cannot - the user did not write
this. Everything finishing in one gap becomes one message, in the order the parent started the
subagents. Being an ordinary user message, it is compacted like any other turn content.

The same text is recorded on the turn as a `Part.SystemPart` of type `SUBAGENT_RESULT`: an assistant-side
part, so the admin transcript shows it while the chat does not draw it as a user bubble.
`ConversationLogReplay` turns that part back into the identical `UserMessage`, which is the one system
event replay does not skip. See `tl-ai-core/doc/subagent-results.md`.

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
