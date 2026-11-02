---
description: Read before writing or changing a tool or a skill of the AI modules - the tool contract of tl-ai-tool-api (Tool, ToolMode, Tool.mode/refusal/unavailableReason, @ToolMethod, ToolExecutionContext.resolveObject, RunInfo, UntrustedEnvelope, ToolRegistryService), the dispatcher (AnnotatedTools, ToolCatalog, DynamicToolCatalog), the data tools (ObjectEditTool, query, present, changeData, TidResolver, QueryLanguage), skills and tool overlays (loadSkill), and the skills TL_init and TL_deepResearch.
order: 61
---

# Tools and skills

## The tool contract

`tl-ai-tool-api` (package `com.top_logic.tl.ai.tool.api`) holds the contract, so a tool is defined without depending on `tl-ai-core`:

- `Tool` - `execute(Map<String, Object> args, ToolExecutionContext context) → ToolResult`.
- `ToolMode` - `READ` (parallel-safe, no approval); `USER_SCOPED`, `DATA_WRITE`, `STRUCTURAL` (serial, need approval, enrol in the turn's transaction); `EXTERNAL` (serial, needs approval, no transaction - the change lands in another system).
- **Per call, not only per tool.** `Tool.mode(args)` says what one call changes: a dry run of a write tool is `READ`, so it needs no approval and stays out of the turn's transaction (`@ToolMethod(dryRun = "param")`). `Tool.refusal(args, ctx)` refuses a call before anybody is asked to approve it (`@ToolMethod(check = "method")`). `ToolCatalog.callMode` is the one answer approval, transaction and log go by. A tool still refuses the same calls in `execute`, because not every caller asks first.
- **Per run.** `Tool.unavailableReason(ctx)` says why a tool can do nothing in this run (`@ToolMethod(unavailable = "method")`) - `present` on a page that displays no channel to show on. `AgentLoop` leaves such a tool out of the schemas of every request (`ToolPermissionFilter.offeredIn`) and answers a call that names it anyway with the reason. This is no permission: nothing is withheld from the user.
- `@ToolMethod` / `@P` - the annotations from which `AnnotatedTools` creates tools by reflection. The modules are compiled with `parameters=true` for it.
- `ToolExecutionContext` - what the framework tells a tool about the call; the concrete `ToolContext` lives in `tl-ai-core`. `resolveObject(id)` turns the `"TypeName:id"` a model wrote into the object, under the run's read rights - which is how a `TLObject` parameter of an `@ToolMethod` is coerced, so a Java tool resolves an identifier exactly as a script tool does and cannot forget the check.
- `RunInfo` - which run the call belongs to (run id, conversation, agent, model, parent run), as plain strings: `ToolExecutionContext.runInfo()` for a tool, `RunInfoBinding.current()` for code beneath one. The run's persistent `AgentExecution` is not readable from a tool - the store writes it at a newer revision; the id is a token to hand back to `AgentExecutionStore`, never an object to read.
- `UntrustedEnvelope` / `AnswerMarking` - the `<untrusted_…>` tags that say a tool answer is data, not instructions, and who puts them on. A `@ToolMethod` answer is marked as `MODEL_DATA` by default; `marking = AnswerMarking.NONE` is for an answer that is the application's own sentence or an instruction the agent is meant to follow, `AnswerMarking.TOOL` for a tool that marks its own content. An answer that already is one complete envelope block is never wrapped twice. See [result filtering](doc:ai/security/result-filtering#marking-untrusted-content).
- `ToolRegistryService` - the service-level registry of tool factories, with qualified names `{factory}.{tool}`.

A tool has three names: the bare `Tool.name()` the model calls, the configured `{factory}.{tool}` name an administrator writes, and the provider-safe `factory__tool` form the registry keys it by. `ToolIdentity.names()` lists them, and every policy entry matches any of the three.

## The dispatcher

- `AnnotatedTools` (in `tl-ai-tool-api`) scans `@ToolMethod` methods into `Tool` instances.
- `ToolCatalog` - the frozen registry of a run, with JSON-Schema validation of the arguments; a call for an absent tool is answered with which absence it is (`ToolCatalog.absenceReason`), see [capability notices](doc:ai/security/capability-notices).
- `DynamicToolCatalog` - the catalog with skill overlays, see below.
- Sub-packages of `com.top_logic.tl.ai.core.tools`: `model/` (model browser, editor, search, the object tools), `script/` (TL-Script execution), `interaction/` (delegation, `askUser`, skills), `task/` (task store), `config/` (schemas, layouts), `integration/` (Gitea), `web/` (web search, web fetch, browser), `memory/`, `registry/` (factories, TL-Script tools), `util/` (serialization, I18N).

### The data tools

- **`ObjectEditTool`** - `object-edit.updateObject` / `createObject` / `deleteObject`, core tools of the entry-point agent: one object the agent knows by its id, changed in one call. Values are converted on the server by the attribute's type (`AttributeValues`), the model's constraints are asked before the commit (`ObjectConstraints`), and rights are checked per object and per attribute before the first write - for a new object's initial values with the engine's create-time question (`isAllowedInitial`), as the create dialogs ask it. A call in a system context is refused: the engine checks no rights there and records no user. `createObject` takes the new object's parts in `values` (a composition's objects, nested up to three levels) and creates the whole tree in one call and one approval, with the constraints asked once it is complete - which is what makes an object with a mandatory composition creatable at all. [Object tools](doc:ai/security/object-tools) has the checks.
- **Reading and changing with TL-Script are two tools.** `query` / `present` (READ, group `data.read`) and `script-exec.changeData` (DATA_WRITE, group `script`; making the change costs `data.write` on top). `changeData` refuses a script that only reads before any approval prompt; its dry run runs the change in an interaction of its own, in a transaction that is always rolled back, and answers what it would change (`ChangeReport`) - only when every effect is a write to the knowledge base. What is a read is decided on the whole expression (`SideEffects`), not by the engine's `HasSideEffects`, which looks at the outermost call only. A set of objects chosen by a condition is changed this way.
- **`QueryLanguage`** is the verified cheat sheet of TL-Script: a query that fails (it does not compile, or fails while it runs) is answered with its reading part, and a skill gets the whole sheet in place of `<<QUERY_LANGUAGE_CHEAT_SHEET>>`.
- **`TidResolver`** turns a model-supplied `TypeName:id` into a `TLObject`, and is the only place that does - every call is a read check. An identifier that does not resolve is classified by `TidDiagnostics` rather than answered with one generic sentence: a uuid, a printed object, an unknown or abstract type, an id of the wrong shape, or a code this application speaks (a pattern and a sentence in `<object-ids>` on `AgentApplicationService`, which is tried first). This runs on the error path only. All of these messages are model-facing English literals, not resources, see [development rules](doc:ai/development#text-for-the-model-and-text-for-people).

## Skills and tool overlays

An agent advertises its own tools plus a catalog of skill names (packages `tools/`, `agent/skills/`); `loadSkill("id")` hands over the skill's instructions and, if it declares any, its tools. A load changes exactly one thing in the request, the tool array: the `### Available Skills` catalog of the system prompt is generated once, when the agent is built, the instructions arrive as a tool result at the end of the conversation, and the tools are appended behind everything already there. `TestSkillOverlayRequestShape` pins the three against a provider that records its requests.

The tool array is in front of the system prompt and of every message in the rendering every provider caches by, so a changed array costs the whole cache: appending leaves every byte before the append point identical, reordering leaves nothing to match. The catalog therefore has three layers and always advertises them in this order, whatever order they were added in:

| Layer | Contents | Stable |
|---|---|---|
| base | the agent's `<tools>` and the tools of its MCP servers | yes |
| framework overlays | `loadSkill`, `listTools` - what every agent carries | yes |
| skill overlays | one per loaded skill, in load order | no |

The first two are the **stable prefix** - `ToolRegistry.stableToolCount`, its size after the run's permission filter. The order is the catalog's, not the call order: `AgentFactory` builds the `SkillTool` first - which restores the session's loaded skills while it is constructed - and adds the framework overlays afterwards, because `listTools` reports the `SkillTool`'s state; in call order a restored skill would sit in front of `loadSkill`, where a freshly loaded one never sits. For the same reason the loaded-skill set is ordered end to end, from `LoadedSkillsBinding` through `ChatProcessor` and `TaskProgressTracker` to the persisted `SessionMetadata`. [Prompt caching](doc:ai/limits-and-caching#prompt-caching) says where the cache breakpoint goes.

Overlays are bounded: at most `DynamicToolCatalog.DEFAULT_MAX_SKILL_OVERLAYS` (8) skills keep their tools loaded, and loading one more drops the overlay whose tools were called longest ago - no timer, no per-turn bookkeeping. The `loadSkill` answer names the skills that left, because the model read their instructions earlier in the conversation, and a call for an evicted tool is answered with "load the skill that has it" rather than "no such tool" (`setReachableBySkill` is about what the agent could reach). There is deliberately no `unloadSkill` tool: it would be advertised in every request of every agent for a saving the ceiling already guarantees.

A compaction changes the messages, never the tool array: the tools of a loaded skill stay. The instructions may be gone, because a summarizing compaction can replace the turn that carried them, so `AgentLoop` tells the registry (`ToolRegistry.historyCompacted()`) and `SkillTool` forgets which skills' text it delivered; the next `loadSkill` delivers the text again without registering the tools twice. `TestSkillTool` and `TestSkillOverlayRequestShape` pin both. `AgentEvent.ContextComposition`, emitted before every provider call, carries `loadedSkills` in load order; it rides the SSE stream (`context_composition`) and is persisted on the turn, so the transcript says which skills each request paid for.

Which skills and tools an agent is offered depends on the acting user's rights: a skill whose every tool is denied is not offered, and `loadSkill` hands over only the permitted tools - see [capability notices](doc:ai/security/capability-notices).

### `TL_init`: the application context

`TL_init` is the counterpart of `/init` in Claude Code: Albert writes the **application context**, one Markdown document saying what the application is for, who works with it and on what, what the users' terms mean, what the calculated values mean and which rules hold - what the model does not tell an agent. The skill is defined in `tl-ai-core.conf.config.xml`.

**Where it lives.** The context is development material, so it lives in the application's source and is deployed with it: a `TextPromptContributor` with `name="application-context"` (`TextPromptContributor.Config.APPLICATION_CONTEXT`) among the prompt contributors of `AgentApplicationService` ([prompt contributors](doc:ai/environment#prompt-contributors)):

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
		<prompt-contributors>
			<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor"
				name="application-context"
			>
				<text>
					<de><![CDATA[# Anwendungskontext: ...]]></de>
					<en><![CDATA[# Anwendungskontext: ...]]></en>
				</text>
			</contributor>
		</prompt-contributors>
	</instance>
</config>
```

- `text` holds at most 8000 characters (about 2000 tokens) with the placeholders of an agent prompt. The save writes **the same text into every language the application supports**, so a context written in German still reaches a user whose language falls back to English; an agent reads it and answers in its user's language. A developer who wants translations edits the file.
- `agents` is written empty, so every agent gets the context, the infrastructure agents included. Agents a developer names are kept: a later save replaces only the text.
- A changed text takes effect when `AgentApplicationService` is restarted (*Agent application knowledge* in the service editor); that restart touches no other service.

**How the save writes the file.** `saveApplicationContext` writes into the `WEB-INF/autoconf` folder of the top-level project (`Workspace.topLevelWebapp()`), never into `tl_autoconf_dir` or the snapshot the service editor saves. `ApplicationContextSource` picks the file:

1. every file that already holds the entry - normally one; a second one exists when the service editor saved `AgentApplicationService` after the entry was written, and both are updated;
2. otherwise the file that configures `AgentApplicationService` and is read last (files are read in name order, and one with `config:override="true"` drops what earlier ones said), where the entry is added to `<prompt-contributors>`;
3. otherwise a new file `ai-application-context.config.xml` that adds the entry **without** `config:override` - the contributors are an unkeyed list, to which a later layer appends. Its name differs from the service editor's `com.top_logic.tl.ai.core.agent.AgentApplicationService.config.xml` and sorts before it, so a later service-editor snapshot (which then contains the entry) replaces the entry instead of adding it twice.

The file is edited in place: only the entry is written, every other character stays (`XmlSpans`). The answer names the file and tells the developer to restart the service and commit the file.

**Only in the development environment**: the save is offered when `tl_operation_mode=development` and the project's `src/main/webapp` exists (`DevelopmentWorkspace`); elsewhere it is not offered (`Tool.unavailableReason`) and Albert hands over the document with that reason. `readApplicationContext` works everywhere; in development it also reads the source file and returns a saved text that is not active yet, saying so.

**What the skill does**, in one run of 15-20 turns (the default turn limit is 60):

1. `readApplicationContext` - an existing context is revised, not started over.
2. The model, read in more depth than the outline (`outlineModel`, `TL_modelExploration`): the calculation rule of every derived attribute (`getCurrentAnnotations`, the `expr` of its `storage-algorithm`), parts and enum values the outline leaves out, and what each type and attribute says (label, description) against what it does. Every discrepancy is reported to the user and neither fixed nor written into the document. The application's objects are not analyzed.
3. An interview of at most five `askUser` calls in the user's language: purpose, users and their tasks, their terms, rules and procedures, what the agents should take care of or avoid.
4. A draft in fixed sections (Purpose, Users, Terms, Core objects, Calculated values, Rules), short, stating only what the model does not say and nothing that neither the model nor the user said; a calculated value gets its meaning, not its expression. It is shown in one code block followed by a choice "save" or "change".
5. `saveApplicationContext` with the whole document: a `STRUCTURAL` call, so the user approves it and sees the text while doing so - that approval is the review. It is refused before any prompt when the account is no administrator, the text is empty or too long, or it names an unknown placeholder. The file is written when the call runs; the turn's transaction does not cover it.

The skill changes nothing but the context - a rule of the skill, not of the run: Albert's write tools stay offered while it is loaded, guarded by their approval.

`TestInitSkill` (`tl-ai-eval`) runs the shipped Albert on a scripted model with a temporary folder as the application's source: the workflow with exactly one approval for the save, the restart after which the next prompt carries the text, a re-run that replaces the entry, the additive file keeping the rest of the service's configuration, the save not offered outside development, and a declined approval writing nothing. `TestApplicationContextSource` (`tl-ai-core`) pins the file edits against the engine's configuration reader and its layering. Whether a real model follows the workflow takes a run against a real model.

### `TL_deepResearch`: research on the public web

`TL_deepResearch` has the assistant research a topic on the public web: it plans sub-questions, spawns one `TL_ResearchWorker` per sub-question in one turn, waits with `wait_for_subagent` and writes one report that links the pages the workers read; it keeps the run with `research.saveReport` and answers with the same report, so a headless `aiResearch()` run keeps one too. The worker carries `webSearch` and `webFetch` only - no application data, no writes - and the lead reads no page itself. An application needs a search engine on `WebToolsService` and a `<background-allowlist>` that admits `egress`. [Web research](doc:ai/deep-research) describes the run, the effort levels, worker models, the configuration and `aiResearch`.
