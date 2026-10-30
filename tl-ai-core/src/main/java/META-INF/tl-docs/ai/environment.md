---
description: Read before changing what an agent run is told about its situation - the <environment> block and its {{...}} placeholders (PromptPlaceholders, PlaceholderConstraint, RunEnvironment, EnvironmentTemplate, tl.ai.environment.template), application facts (<facts> on an agent, <chat-conversation><context>, RunFactsBinding), prompt contributors (SystemPromptContributor, TextPromptContributor), the per-turn <turn-context> block (TurnContext, ensureTurnContext), user preferences (Person#aiPreferences, max-preferences-length) and user memories (remember, recall, MemoryConsent, <user-memory>, aiMemoryDelete).
order: 60
---

# The environment of a run

Every agent run is told in its system prompt whom it works for and what the situation is. One place composes it for every entry point - the chat, `aiAgentChat*`, a scheduled run: `AgentFactory.buildConfig`. The user's message text is never modified, in the bubble, in the log or in the request.

## The environment block

```
<environment>
You run inside the TopLogic application "Event management demo" as the assistant of one user.
User: Ada Lovelace. Address the user the way they ask to be addressed in their preferences.
Language: Deutsch (de). Write every text the user sees in this language: the notes between tool calls, the questions and the final answer.
Date: 2026-09-19 (Europe/Berlin).
Agent: Albert. Model: claude-sonnet-4-6.
What the application states below is authoritative. Where the application context and the user
preferences disagree, follow the application context.
Application context - what this application states about the situation:
- Project: Airport expansion
User preferences - what this user asked for:
Keep answers to three sentences.
What you know about this user - kept from earlier conversations, with their agreement:
- [Preference] Prefers the summary before the detail.
</environment>
```

### Placeholders

A system prompt may use these (`PromptPlaceholders`); the run fills them when it starts.

| Placeholder | Value |
|--|--|
| `{{user}}` | the acting account's display name (`Person#getFullName`, else its login name) |
| `{{language}}` | the account's UI language, named in that language and tagged: `Deutsch (de)` |
| `{{date}}` | today in the account's time zone, at day granularity, with the zone |
| `{{agent}}` | the agent's display name, else its identifier |
| `{{model}}` | the model the run talks to |
| `{{application}}` | the configured application name, else the application's own |
| `{{context}}` | the application's stable facts, heading included - or nothing |
| `{{turn_context}}` | one sentence saying what a `<turn-context>` block is - or nothing, for a run that sends none |
| `{{preferences}}` | the account's preference text, heading included - or nothing |
| `{{memories}}` | what the agent kept about the account, heading included - or nothing |
| `{{environment}}` | the whole block |

- **An unknown name is a configuration error, refused where it is written**: for a shipped agent while `TopLogicAgentDefinitions` reads it (the service does not start, naming the agent and the name), for an agent stored in the knowledge base by `PlaceholderConstraint` on `tl.ai.core:Agent#systemPrompt`. A placeholder nobody fills would reach the model verbatim.
- **A prompt that does not name `{{environment}}` gets the block appended at its end**, so every shipped agent carries it without an edit.
- The sections `{{context}}`, `{{turn_context}}`, `{{preferences}}` and `{{memories}}` render whole, heading included, or not at all. A template line that is nothing but an empty placeholder is dropped.

### Nothing in it changes within a conversation

The block sits at the front of every request, the part the providers' prompt caches key on; one changed character misses the cache on every following turn ([prompt caching](doc:ai/limits-and-caching#prompt-caching)). So the date has day granularity, never a time or a turn number, and only facts marked `stable` are rendered. What changes goes into the [per-turn block](#the-per-turn-block).

### The template

The wording is the resource `tl.ai.environment.template` (`WEB-INF/conf/resources/tl-ai-core.app-messages_*.properties`), translated like every shipped text. It is not part of the service configuration, so a service-editor save of `AgentApplicationService` (which writes the whole configuration) does not freeze an English template in front of translated headings. An installation that wants other wording sets `<template>` in the `<environment>` section of `AgentApplicationService`, which wins:

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
		<environment application="...">
			<template>
				<en><![CDATA[...]]></en>
				<de><![CDATA[...]]></de>
			</template>
		</environment>
	</instance>
</config>
```

| Property | |
|--|--|
| `template` (`AgentApplicationService`) | the block's text in the language of the acting account; every placeholder but `{{environment}}`. Unset uses the shipped resource, and where that is unreachable `EnvironmentTemplate.DEFAULT`, the same English text; `TestEnvironmentTemplate` keeps the two together and checks that every shipped language uses the same placeholders |
| `application` (`AgentApplicationService`) | what to call the application in front of the model; empty uses the application's own name |
| `user-preferences` (`AgentPolicyService`) | whether the accounts' preference texts are sent at all; off sends none and keeps the texts |
| `max-preferences-length` (`AgentPolicyService`) | the length cap of one account's text in characters, default 1000; 0 or less means no cap |

## Application facts

A host states facts in two places, and a run gets both, the agent's own first.

**On the agent definition**, for facts that need no page - TL-Script without arguments, evaluated in the acting user's context when the run starts, so a headless and a scheduled run get them too:

```xml
<agent name="MyAgent">
	<facts>
		<fact name="Tenant" stable="true">
			<value>currentUser().get(`my:Person#tenant`).label()</value>
		</fact>
	</facts>
</agent>
```

**On `<chat-conversation>`**, for facts that need what a page shows. The `<inputs>` are channels of that page, and their values are the leading arguments of every fact expression:

```xml
<chat-conversation input="chatSession">
	<context>
		<inputs><input channel="project"/></inputs>
		<fact name="Project" stable="true"><value>p -> $p.label()</value></fact>
		<fact name="Selection" stable="false"><value>p -> ifElse(isEmpty($p), "nothing", $p.label())</value></fact>
	</context>
</chat-conversation>
```

`ChatConversationControl` evaluates them on the server, again whenever an input changes; they travel with `POST /chat/send` and `POST /chat/regenerate` as the request's `facts` (`FactDto`), and `ChatProcessor.sendMessage` binds them for the run through `RunFactsBinding`, which is also how a delegate and a subagent are told the same. A fact that cannot be evaluated is dropped with a log line and does not fail the run. The values are computed on the server in the acting account's context and echoed by the browser; a client that sends something else only tells its own agent something about its own page - every tool still asks the security context.

## Prompt contributors

A fact is a name and a value. Text the model is to **follow** - house rules per client, per project, per account - is a `SystemPromptContributor` in `<prompt-contributors>` of `AgentApplicationService`:

```java
public class ClientHouseRules implements SystemPromptContributor {
	@Override
	public String contribute(AgentDefinition agent, AgentModelAssignment assignment) {
		return rulesOf(TLContext.currentUser()); // null or blank contributes nothing
	}
}
```

`AgentFactory.buildConfig` asks every contributor once per run, in configuration order, after the generated skill and delegate sections and before the environment block is composed, so a placeholder in the contributed text is filled. A contributor that throws costs its own section and a log line, not the run. **What a contributor returns must not change within a conversation**, for the cache reason above.

| | use |
|--|--|
| a named value that holds for the conversation | a stable fact |
| a named value that changes with the next click | an unstable fact, sent in the per-turn block |
| sentences the model is to follow | a contributor |

### A fixed text: `TextPromptContributor`

For a fixed text - what the application is, its domain terms, its rules - `tl-ai-core` ships `TextPromptContributor`:

```xml
<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor" agents="TL_SingleAgent">
	<text>
		<de>## Fachwissen
Ein Auftrag ist 'offen', bis er abgerechnet ist.</de>
		<en>## Domain knowledge
An order is 'open' until it is invoiced.</en>
	</text>
</contributor>
```

- **`text`** is translatable (one element per language); a run gets it in the acting account's UI language, with the engine's usual fallback. Write it flush left: indentation common to all lines but the first is removed from the prompt, but the service editor shows it. The service editor writes it as CDATA (`TextPromptContributor.VerbatimTextBinding`), so it comes back as entered.
- **`agents`** is a comma-separated list of technical agent names; empty means every agent, including infrastructure agents such as the prompt improver, which then pay for the text on every run. In the service editor it is a selection of the existing agents (`AllAgentNames.existingAgentNames`); a name no agent has is refused when the service is saved, but not at startup, where the agent definitions and the knowledge base cannot be asked yet.
- **`name`** lets a tool find an entry again; `name="application-context"` marks the application context that the [`TL_init` skill](doc:ai/tools-and-skills#tl_init-the-application-context) writes. Leave it empty otherwise.
- **Placeholders** of an agent prompt are allowed; an unknown one is refused by a constraint at startup and when the service editor saves. A `text` given as a resource key is not inspected.

**Changing it in the running application:** *Administration → Service Editor → Agent application knowledge*, change the text and save (the editor checks the constraints, writes `com.top_logic.tl.ai.core.agent.AgentApplicationService.config.xml` to the autoconf folder and reloads the configuration), then restart that service. No other service restarts with it; a run that starts during the restart is refused. Every run started afterwards gets the new text, including the next message of an open conversation - a run composes its prompt once, so an open conversation changes at most once, at a message boundary.

The save writes the whole configuration of `AgentApplicationService` with `config:override="true"`, so from then on changes of the other files that configure it (contributors, environment section, object-id hints - for example in a new release of the application) have no effect until the written file is deleted. That is the service editor's behaviour for every service. Whoever may save and restart services in the service editor writes text that reaches every named agent's prompt for every account; grant that right only to accounts that may also write agent prompts.

## The per-turn block

Facts that change while the user talks - the open object, the selected row - travel in the turn, as their own content block of the user message, ahead of what the user wrote (`TurnContext`):

```
<turn-context>
Turn: 3
Time: 14:32 (Europe/Berlin)
Open invoice: RE-2026-0042
</turn-context>
```

- **Two blocks, not one string**: every provider renders a message's content parts as separate blocks, so the user's text stays byte-exact. The tags delimit the block; a value containing a tag has its `<` escaped to `&lt;` (as `UntrustedEnvelope` does), and a value keeps no line breaks, because the block is read line by line.
- **The selection** is the fact worth naming first: `<chat-conversation><selection channel="..."/>` states the selected object in one line - label, qualified type and the `label [id:TID]` every tool reads back.
- **Turn number and time of day** are added by the framework to a block that has facts; a host that states nothing per turn gets no block. The turn number is what `ChatProcessor` counted (`RunFactsBinding.bind(facts, turnNumber)`); a headless run counts none and omits the line.
- **The model is told what the block means**: the `{{turn_context}}` section of the environment block (resource `tl.ai.environment.turn-context`, fallback `TurnContext.NOTE`) says that the newest block is the current state and earlier ones are past. It is present exactly when a block is (`RunEnvironment.turnContextSection`). Because it is part of the system prompt, a page that states per-turn facts and a dock over the same conversation that does not would flip it between turns: state them from every place the conversation is reachable from.

`AgentFactory.buildConfig` composes the block into `AgentParams#turnContext`; `AgentLoop.ensureTurnContext` attaches it to the newest user message where a request is built - once per message, so it cannot be forgotten, survives a compaction (re-attached to the summary, or appended as a message of its own when a truncating compaction left no user message), and each turn keeps its own block so the history does not change under the cache. It is stored on the turn (`Turn#turnContext`, `PersistedTurn.turnContext`), and `ConversationLogReplay` puts it back where the live run had it. The chat bubble shows only the user's text; the transcript of the activity monitor shows the block in its own frame. `AgentLoop` announces it once per run as `AgentEvent.TurnContextComposed`.

## User preferences

`tl.accounts:Person#aiPreferences` (added by `WEB-INF/model/tl-ai-core/extension.tl.accounts.model.xml`) is free text the user writes for every agent, rendered below the application's facts; the template says the application wins where the two disagree. `PreferencesLengthConstraint` enforces `max-preferences-length` where the text is entered, since it is sent with every request of that account. `tl-ai-demo` edits it on its *Meine Einstellungen* page (`WEB-INF/views/tl-ai-demo/profile.view.xml`).

## User memories

A memory is what the agent writes down about the user, with the user's agreement, and reads back in later conversations: a `tl.ai.execution:UserMemory` with owner, kind (`PREFERENCE`, `FACT`, `FEEDBACK`), text, origin (session, run id) and creation time. `remember(kind, text)` keeps one, `recall(query)` searches those that did not fit into the block; both act for the acting account only - the owner filter is in `AgentExecutionStore.memoriesOf`.

**What reaches the block**: the newest `max-loaded` memories, one line each, naming the kind. The list is read **once per conversation**: `ChatSessionManager.getMemories` reads it with the first message and keeps it on the session state, and `ChatProcessor` binds it for every run of the conversation (`UserMemoryBinding`), so the block stays identical across turns. A memory written in turn 3 appears in the next conversation. A run that binds none - headless, scheduled, TL-Script - reads the store directly.

**What may be written**: `remember` refuses a memory the user did not agree to in the turn it is called in - a Java check on the call path. `MemoryConsent` is built at run start from the user's own message, and `AskUserTool` records the user's answer on it. The user agrees by asking in their own words ("remember that ...", "merk dir ...") or by agreeing to an `askUser` question whether to remember something; a negation in the answer wins. A nested run inherits the record (`MemoryConsentBinding.bindIfAbsent`), so a delegate cannot agree with itself; a detached subagent builds its record from the task its lead wrote and can keep nothing. Only a message the user wrote counts: a `UserMessage` the framework puts into the user role (a subagent report, a notice, a compaction summary, a lead's task) carries `origin` `AGENT` or `FRAMEWORK`, and when the newest message in the user role is not the user's, the record grants nothing. The check establishes that memory was the subject of the turn and that the user was in favour - not that the stored sentence is what the user meant; that is why the user sees and deletes every memory.

```xml
<user-memory
	enabled="true"
	max-loaded="20"
	max-length="500"
	request-words="remember,merk dir,..."
	affirmative-words="yes,ja,..."
	negative-words="no,nein,..."
/>
```

A section of `AgentPolicyService` (*Agent policy* in the service editor). `enabled="false"` keeps both tools out of every catalog, makes a call that reaches them refuse, and renders no section; the memories are kept. The words are matched against what the user typed, in the user's language.

`tl-ai-demo` shows the memories in the rail of its chat page: a `<data-list>` over `tl.ai.execution:UserMemory` filtered by `owner == currentUser()`, with a Forget command calling `aiMemoryDelete`, which refuses a memory the acting account neither owns nor administers. A memory written during a conversation appears in that list at once.

## What an administrator sees

The filled block is written onto the run as `tl.ai.execution:AgentExecution#environment` when the run starts (`AgentEvent.EnvironmentComposed`, recorded by `ExecutionRecorder`), so it is there for a run still going and for one that never finished. The agent activity monitor shows it as a collapsed *Environment* group in the run detail.

## Where the code is

| | |
|--|--|
| `agent/environment/PromptPlaceholders`, `PlaceholderConstraint` | which `{{name}}` exists, and the check |
| `agent/environment/EnvironmentTemplate`, `RunEnvironment` | the default text, empty sections, composing one run's block |
| `agent/environment/EnvironmentConfig`, `EnvironmentSettings` | the `<environment>` section and the resource behind it |
| `agent/environment/AgentFact`, `EnvironmentFact`, `RunFactsBinding` | facts: declared, evaluated, bound for a run |
| `agent/environment/TurnContext`, `AgentLoop#ensureTurnContext` | the per-turn block and where it is attached |
| `agent/environment/UserPreferences`, `PreferencesLengthConstraint` | the preference text and its cap |
| `agent/environment/UserMemories`, `UserMemoryBinding`, `UserMemoryConfig`, `UserMemorySettings` | the memory section and its configuration |
| `agent/SystemPromptContributor`, `agent/TextPromptContributor`, `AgentFactory#appendContributions` | contributed text |
| `tools/memory/UserMemoryTool`, `MemoryConsent`, `MemoryConsentBinding`, `ConsentWords` | `remember`, `recall` and the consent rule |
| `scripting/AIScriptFunctions#memoryDelete` | `aiMemoryDelete` with its ownership check |
