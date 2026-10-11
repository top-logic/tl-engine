# The environment block

Every agent run is told, in its system prompt, who it is working for and what the situation is. The
harness composes one `<environment>` block per run and puts it there. It works the same for the
chat, for `aiAgentChat*` and for a scheduled run, because it is composed where every one of them
passes: `AgentFactory.buildConfig`.

Before this existed, applications did it by hand - Consulting prepended a context header to the
message text and stripped it again with a regular expression (#406, #428). The block is the seam
that replaces it: **the user's message text is never modified**, in the bubble, in the log or in the
request.

## What a run is told

```
<environment>
You run inside the TopLogic application "Event management demo" as the assistant of one user.
User: Ada Lovelace. Address the user the way they ask to be addressed in their preferences.
Language: Deutsch (de). Answer only in this language.
Date: 2026-09-19 (Europe/Berlin).
Agent: Albert. Model: claude-opus-5.
What the application states below is authoritative. Where the application context and the user
preferences disagree, follow the application context.
Application context - what this application states about the situation:
- Project: Airport expansion
- Tenant: ACME
User preferences - what this user asked for:
Keep answers to three sentences and address me as Frau Dr. Meier.
What you know about this user - kept from earlier conversations, with their agreement:
- [Preference] Prefers the summary before the detail.
- [Fact] Works in the accounting department.
</environment>
```

## Placeholders

A system prompt may use these; the harness fills them at run start.

| Placeholder | Value |
|--|--|
| `{{user}}` | The acting account's display name (`Person#getFullName`, else its login name). |
| `{{language}}` | The account's UI language, named in that language and tagged: `Deutsch (de)`. |
| `{{date}}` | Today in the account's own time zone, at day granularity, with the zone: `2026-09-19 (Europe/Berlin)`. |
| `{{agent}}` | The agent's display name, else its identifier. |
| `{{model}}` | The model this run talks to. |
| `{{application}}` | The configured application name, else the application's own. |
| `{{context}}` | The application's stable facts, heading included - or nothing. |
| `{{turn_context}}` | One sentence saying what a `<turn-context>` block is - or nothing, for a run that sends none. |
| `{{preferences}}` | The account's preference text, heading included - or nothing. |
| `{{memories}}` | What the agent kept about the account, heading included - or nothing. |
| `{{environment}}` | The whole block. |

Two rules about them:

- **An unknown name is a configuration error, refused where it is written.** For the shipped agents
  that is while `TopLogicAgentDefinitions` reads them (the service refuses to start, naming the
  agent and the typo); for an authored agent it is `PlaceholderConstraint` on
  `tl.ai.core:Agent#systemPrompt`. A placeholder nobody fills would reach the model verbatim and
  read as an instruction the harness failed to honour, and the typo behind it is invisible in the
  answer.
- **A prompt that does not name `{{environment}}` gets the block appended at the end.** That is
  every prompt this product ships, which is the point: an installation gains the block without
  editing a single agent.

## Why nothing in it may change during a conversation

The block sits at the very front of every request of the conversation, which is exactly the part the
providers' prompt caches key on. One changed character there invalidates the cache for every
following turn. So:

- the date is stated at day granularity - never a time of day, never a turn number;
- only facts the host marked `stable` are rendered.

Facts that change go into the [per-turn block](#the-per-turn-block) instead.

## The template

The shipped wording is the resource `tl.ai.environment.template`
(`WEB-INF/conf/resources/tl-ai-core.app-messages_*.properties`), so it is translated like every
other text this product ships. It is **not** in the service configuration, and that is deliberate:
an application that overrides the service wholesale - which is what a Service Editor save does -
would lose a template living there, leaving the English constant in front of headings that are still
in the user's language.

An installation that wants to say something different sets `template` in `AgentApplicationService`'s
`<environment>` section, which wins. The two switches for the accounts' preference texts are
attributes of `AgentPolicyService` (#691):

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
    <environment application="…">
      <template>
        <en><![CDATA[…]]></en>
        <de><![CDATA[…]]></de>
      </template>
    </environment>
  </instance>
</config>

<config service-class="com.top_logic.tl.ai.core.agent.AgentPolicyService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentPolicyService"
    user-preferences="true"
    max-preferences-length="1000"
  />
</config>
```

| Property | |
|--|--|
| `template` | (`AgentApplicationService`) The block's text, in the language of the account the run acts for. It may use every placeholder but `{{environment}}` - the block cannot contain itself. Unset uses the shipped resource, and if that is unreachable `EnvironmentTemplate.DEFAULT`, which is the same English text; `TestEnvironmentTemplate` holds the two together and checks that every shipped language uses the same placeholders. |
| `application` | (`AgentApplicationService`) What to call this application in front of the model. Empty uses the application's own name. |
| `user-preferences` | (`AgentPolicyService`) Whether the accounts' preference texts are sent at all. Off sends none, whatever the accounts wrote; the texts are kept. |
| `max-preferences-length` | (`AgentPolicyService`) How long one account's text may be, in characters. Default 1000. Cleared (0 or less) means no cap. |

`{{context}}`, `{{turn_context}}`, `{{preferences}}` and `{{memories}}` render a **whole section
each**, heading included, or nothing at all. A template line that is nothing but an emptied placeholder is dropped,
so a switched-off preference section leaves no orphaned heading and no gap.

## Application facts

A host states facts in two places, and a run gets both - the agent's own first.

**On the agent definition**, for facts that need no page. TL-Script of no arguments, evaluated in
the acting user's context when the run starts, so a headless and a scheduled run get them too:

```xml
<agent name="MyAgent">
  <facts>
    <fact name="Tenant" stable="true">
      <value>currentUser().get(`my:Person#tenant`).label()</value>
    </fact>
  </facts>
</agent>
```

**On `<chat-conversation>`**, for facts that need what a page is showing. Bound the way
`<data-list>` binds its rows: the `<inputs>` are channels of that page, and their current values are
the leading arguments of every fact expression.

```xml
<chat-conversation input="chatSession">
  <context>
    <inputs><input channel="project"/></inputs>
    <fact name="Project" stable="true"><value>p -> $p.label()</value></fact>
    <fact name="Selection" stable="false"><value>p -> ifElse(isEmpty($p), "nothing", $p.label())</value></fact>
  </context>
</chat-conversation>
```

`ChatConversationControl` evaluates them on the server and re-evaluates them whenever an input
changes; they travel with `POST /chat/send` and `POST /chat/regenerate` as the request's `facts`
(msgbuf `FactDto`), and `ChatProcessor.sendMessage` binds them for the run through
`RunFactsBinding`. The binding is what lets a delegate and a subagent be told the same thing without
the facts being threaded through every signature in between.

A fact that cannot be evaluated is dropped with a log line rather than failing the run: losing one
line of context is a better answer to a mistyped expression than no answer at all.

**What the request carries is not authority.** The values are computed on the server, in the acting
account's context, and the browser echoes them back because the run happens on a background thread
long after those channels were read. A client that sends something else states something about its
own page to its own agent; it gains nothing by it, because every tool still asks the security
context, and it cannot reach another account's conversation.

## Prose the application contributes (#381)

A fact is a name and a value, rendered as one line under a heading that says the application is
*stating* something. Some applications have to *instruct* instead - house rules per client, per
project or per account, in whole sentences. That is a `SystemPromptContributor`, configured on
`AgentApplicationService`:

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
    <prompt-contributors>
      <contributor class="com.acme.agent.ClientHouseRules"/>
    </prompt-contributors>
  </instance>
</config>
```

```java
public class ClientHouseRules implements SystemPromptContributor {
    @Override
    public String contribute(AgentDefinition agent, AgentModelAssignment assignment) {
        return rulesOf(TLContext.currentUser());   // null or blank contributes nothing
    }
}
```

`AgentFactory.buildConfig` asks every configured contributor once per run, in configuration order,
after the generated skill and delegate sections and **before** the `<environment>` block is composed -
so a `{{...}}` placeholder in the contributed text is filled like one in the agent's own prompt. A
contributor that throws costs its own section and a log line, not the run.

Which of the three to use:

| | |
|--|--|
| a named value that holds for the conversation | a **stable fact** - it is rendered where the model already reads the situation |
| a named value that changes with the next click | an **unstable fact**, which travels in the [per-turn block](#the-per-turn-block) |
| sentences the model is to follow | a **contributor** |

The same rule as everything else in the prompt applies, and it is the one thing that can go wrong
here: **what a contributor returns must not change within a conversation.** The text sits in front of
every request, so one changed character misses the providers' prompt cache on every following turn.
That is also the point of putting it here rather than in front of every user message, which is what
an application without this seam had to do: the text is charged once for the conversation instead of
once per turn.

### A fixed text, without Java code (#648)

Most applications only want to give their agents a fixed text: what the application is, its domain
terms, its fixed business rules. For that, tl-ai-core ships `TextPromptContributor`:

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
    <prompt-contributors>
      <contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor"
        agents="TL_SingleAgent"
      >
        <text>
          <de>## Fachwissen
Ein Auftrag ist 'offen', bis er abgerechnet ist.</de>
          <en>## Domain knowledge
An order is 'open' until it is invoiced.</en>
        </text>
      </contributor>
    </prompt-contributors>
  </instance>
</config>
```

- **`text`** is a translatable text (a `ResKey`, written as one element per language, like an
  agent's `display-name`). A run gets the text in the UI language of the account it acts for. When
  there is no text in that language, the engine's usual fallback applies: the fallback chain of the
  resource bundles, then the first supported language that has a text.
  Write the text flush left, as above. Indentation that all lines but the first have in common is
  removed from the prompt, but the service editor shows it to the next administrator.
- **`agents`** is a comma-separated list of technical agent names. Empty means every agent - also
  the infrastructure agents such as the prompt improver, which then pay for the text on every run.
  Name the agents when the text is about the application's data. In the service editor this is a
  selection of the agents that exist: the configured ones and the ones stored in the knowledge base
  (`AllAgentNames.existingAgentNames`). A name that no agent has is refused when the service is
  saved. It is **not** refused at start-up: the engine checks the configuration before the agent
  definitions and the knowledge base can answer, so a name that matches no agent then gets the text
  nowhere.
- **`name`** is optional and lets a tool find an entry again. `name="application-context"` marks the
  application context, which Albert's `TL_init` skill writes into the application's source
  configuration and revises there (#667, see `init-skill.md`). Leave it empty for any other text.
- **Placeholders**: the text may use every `{{...}}` placeholder of an agent's prompt; the tooltip of
  the field lists them with their meaning. An unknown placeholder is a configuration error, refused
  by a `@Constraint` on the property. The engine asks that constraint at two points: when the
  application configuration is read at start-up, and when an administrator saves the service in the
  service editor, before the file is written. Only a literal text is inspected; a `text` that
  references a resource key by name is not.

**Changing the text in the running application.** *Administration → Service Editor → Agent application knowledge*
shows the contributor under *Prompt contributors*, with the text in a multi-line field per language.
The sequence is:

1. Change the text and **save**. The service editor checks the constraints, writes
   `com.top_logic.tl.ai.core.agent.AgentApplicationService.config.xml` to the autoconf folder and reloads
   the application configuration. The running service does not see the change yet.
2. **Restart** the service (*Neu starten* in the same editor). `AgentApplicationService` builds its
   contributors again from the saved configuration. No application restart is necessary, and no
   other service is restarted with it: the model list is not fetched again and the model health
   marks are kept. While the service restarts, a run that starts is refused.

From then on, every run that starts gets the new text: a new conversation, and also the **next
message of a conversation that is already open**. A run that is in progress keeps the text it started
with, because a run composes its system prompt once, in `AgentFactory.buildConfig`, and sends that
same prompt on every turn. So the text of an open conversation changes at most once, at a message
boundary, and that message pays for its prompt once without a cache hit. There is never a mix of old
and new text in one request.

The service editor writes the text as CDATA (`TextPromptContributor.VerbatimTextBinding`), so it
comes back exactly as it was entered. Plain element text would not: the editor's pretty-printer
indents every line of it to the element's depth and strips the indentation the line had, so the text
would come back with tabs on every line and a nested list would lose its nesting.

> ⚠️ **Saving writes the whole `AgentApplicationService` configuration.** As for every service, the
> written file carries `config:override="true"` and replaces the `AgentApplicationService`
> configuration of all other files - contributors, the environment section and the object-id hints.
> From then on, a change to those files (for example in a new release of the application) has no
> effect until the written file is deleted. That is how the engine's service editor behaves for every
> service. Providers, models, agent overrides, limits, budget and user memory are on the other three
> services and are not part of this snapshot; that is the reason for the split (#691, see
> `llm-service-migration.md`).

**Who may change it.** Everybody who may use the service editor's save and restart commands - in a
standard TopLogic application, the administrators. The text then reaches the system prompt of every
named agent, for every account, so it carries the same authority as an agent's own prompt: give the
service editor right only to accounts you would also let write agent prompts. tl-ai adds no own
right on top of it.

## The per-turn block

Some facts change while the user talks to the agent: which object is open, which row was just
selected, which section is being read. They must reach the model, and they must not reach the system
prompt. They travel in the turn instead, as their own content block of the user message, ahead of
what the user wrote:

```
<turn-context>
Turn: 3
Time: 14:32 (Europe/Berlin)
Open invoice: RE-2026-0042
</turn-context>
```

```
Is the payment term okay?
```

**Two blocks, not one string.** Every provider this product speaks to renders a message's content
parts as separate blocks, so the harness sends two and the user's text stays byte-exact - nothing is
prepended to it and nothing has to be stripped off it again (#406, #428). The `<turn-context>` tags
are still there, because nothing guarantees a *visible* separator between two blocks once the model
reads them; self-delimiting text needs no `[user]` marker on the other half, which is what lets the
user's own text stay untouched. A value that contains one of the tags has its `<` escaped to `&lt;`,
the way `UntrustedEnvelope` does it, so no fact can end the block early. A value keeps no line
breaks either: the block is read line by line, and a second line would read as a fact of its own.

**The selection is the fact worth naming first.** The question a user asks a chat beside a page is
almost always about what they have open, and `tl-chat` offers that as one line rather than as three
expressions: `<chat-conversation><selection channel="…"/></chat-conversation>` states the selected
object as a per-turn fact, with the label the user sees, the qualified type name and the
`label [id:TID]` identifier every tool result carries - so the agent can both answer "which one do I
have open?" and act on it. See `tl-chat/CLAUDE.md`.

**Turn number and time of day are added by the framework.** Both change per turn by nature, which is
exactly why the environment block states the date at day granularity and no time at all. The turn
number is what the entry point counted - `ChatProcessor` binds it with the facts
(`RunFactsBinding.bind(facts, turnNumber)`); a headless or a scheduled run counts none and the line
is then omitted.

**A host that states nothing per turn gets no block.** Turn number and time are added *to* a block;
they do not create one.

### The model is told what the block is

A block travelling with the turn says what it holds, but not what it *means* - and in particular not
that the newest one is the current state while the earlier ones, still in the history, describe
moments that have passed. The environment block therefore carries one sentence about it, as the
`{{turn_context}}` section:

```
Live state: each user message carries a <turn-context> block ahead of the user's own text, naming
what this application states about that moment - among it the object the user has selected. The
newest block is what is true now; an earlier one describes a moment that has passed.
```

It is there **exactly when a block is**, by the same predicate `TurnContext` composes by
(`RunEnvironment.turnContextSection`): an installation that states nothing per turn is told nothing
about a block it never sends, the way it gets no `{{context}}` heading for facts it never wrote. Its
wording is the resource `tl.ai.environment.turn-context`, beside the template itself, with
`TurnContext.NOTE` as the fallback a test pins it to.

One thing to know before a host states per-turn facts from two places: this section is part of the
system prompt, so a page that states them and a dock over the same conversation that does not would
flip it between the turns of one chat - and would flip `{{context}}` with it. State them from every
place the conversation is reachable from. `tl-ai-demo` does, on the Chat page and in the dock.

### Where it is attached

`AgentFactory.buildConfig` composes it, from the same facts as the environment block, into
`AgentParams#turnContext`. `AgentLoop.ensureTurnContext` puts it into the conversation - prepended to
the newest user message - at the one point a request is built, which buys three things at once:

- **it cannot be forgotten**, the same argument that puts the untrusted-content standing rule there;
- **it survives a compaction**, including the reactive one between two attempts of the same turn: a
  compaction that squeezed the preserve zone down to nothing takes the user message the block arrived
  on with it, and the next request re-attaches the block to whatever user message is left - the
  summary - or, when a truncating compaction left none at all, appends it as a message of its own at
  the very end, where no tool-call/tool-result pair can be broken;
- **it stays where it was put**, so each turn keeps its own block and the history does not change
  under the providers' prompt caches. Rewriting the history per request would cost the very cache
  this block is kept out of the system prompt to protect.

It is added at most once per message: `ensureTurnContext` checks for the exact block first.

### Where it is stored

On the turn, apart from the user's text: `Turn#turnContext`, `PersistedTurn.turnContext` in the
transcript proto, and `ConversationLogReplay` puts it back in the same position a live run did - a
reloaded conversation has to be the one the model already saw, because the compaction sidecars are
fingerprinted against that projection.

What shows it:

| | |
|--|--|
| the chat bubble | only the user's text - the block is not among the turn's `userParts` at all |
| the transcript / run detail of the activity monitor | the block, above the user's part, in its own frame |

`AgentLoop` announces it once per run as `AgentEvent.TurnContextComposed`, and
`ConversationLogListener` writes it onto the current turn - once, so a delegate told the same thing
does not overwrite the entry point's.

## User preferences

`tl.accounts:Person#aiPreferences`, added to the account model by
`WEB-INF/model/tl-ai-core/extension.tl.accounts.model.xml`. Free text, on purpose: a first version
that lets a user write "call me Frau Dr. Meier and keep answers to three sentences" without anybody
guessing in advance which knobs to offer. Structured preferences are out of scope (#437).

It is rendered below the application's facts, and the template says the application wins where the
two disagree - a preference is a wish, not a statement about the situation.

The cap is enforced by `PreferencesLengthConstraint` where the text is entered, with a message
naming both numbers, rather than truncated where it is used: the text is sent with every single
request of that account.

The demo edits it on its "My settings" page (`WEB-INF/views/tl-ai-demo/profile.view.xml`).

## User memories (#439)

A preference text is what the user writes down for themselves. A **memory** is what the agent
writes down, with the user's agreement, and reads back in the next conversation.

Each one is a `tl.ai.execution:UserMemory`: an owner, a kind (`PREFERENCE` / `FACT` / `FEEDBACK`),
the text, where it came from (session, run id) and when it was kept. A model object rather than a
line in a blob, so the user sees it in a list, sorts it and throws away one at a time, and
TL-Script can query it.

### What reaches the block

The newest `max-loaded` memories of the acting account, one line each, naming their kind - the three
are acted on differently: a preference is honoured, a fact is used, a piece of feedback is a
correction to apply again. A memory is put on one line, because the block is read line by line.

**The list is read once per conversation, not once per run.** The block has to stay byte-identical
across the turns of a conversation (see above), so a memory written in turn 3 must not appear in
turn 4's block - it appears in the next conversation, which is also the only place it is needed,
the agent having just written it. `ChatSessionManager.getMemories` reads it when the first
message of a conversation is sent and keeps it on the session state; `ChatProcessor` binds it for
every run of that conversation through `UserMemoryBinding`. A run that binds none - headless,
scheduled, TL-Script - is a conversation of its own and reads the store directly.

### What may be written

`remember(kind, text)` keeps one, `recall(query)` searches the ones that did not fit. Both act for
the acting account and only for it: the owner filter sits in `AgentExecutionStore.memoriesOf`, so
every reader there is goes through it.

**`remember` refuses a memory the user did not agree to in the turn it is called in**, and that is a
Java check on the call path, not a line in a prompt. `MemoryConsent` is built at the start of a run
from the user's own message; `AskUserTool` records what the user answered onto it. There are two
ways to agree:

- the user asked in their own words - "remember that ...", "merke dir, dass ...";
- the agent asked with `askUser` whether it should remember something, and the user agreed to *that*
  question.

A negation in the answer wins over anything in it that would otherwise read as agreement ("no, that
is fine" is a refusal). The words are configurable, because they are matched against what the
**user** typed, in the user's language.

A nested run inherits the record rather than building one (`MemoryConsentBinding.bindIfAbsent`): a
delegate's "user message" is the instruction its parent wrote, so a run allowed to build its own
could agree with itself by delegating with the words "remember that ...". A detached subagent builds
its record from its task, which its lead wrote, so it can keep nothing.

**Only a message the user wrote counts** (#807). The framework also puts text into the user's role:
a delivered subagent report or question, a notice, a compaction summary, and the task or message a
lead writes for its subagent. Each such `UserMessage` carries its `origin` (`AGENT` or
`FRAMEWORK`, not `USER`), and the replay and the compaction sidecar keep it. When the newest message
in the user's role is not the user's, the record grants nothing. This is the case in a lead turn that
the chat starts for a subagent's news: a subagent's report repeats what it read, and a web page can
ask to be remembered.

**What the check establishes**: memory was the subject of this turn and the user was in favour. It
cannot establish that the stored sentence is the sentence the user meant - no server-side check can
read a sentence the way the user meant it. That is why the user also sees every memory and can
delete any of them.

### Configuration

```xml
<user-memory
  enabled="true"
  max-loaded="20"
  max-length="500"
  request-words="remember,merk dir,…"
  affirmative-words="yes,ja,…"
  negative-words="no,nein,…"
/>
```

A section of `AgentPolicyService` ("Agent policy" in the Service Editor). `enabled="false"` keeps both tools out of every
agent's catalog, makes a call that reaches them anyway refuse, and renders no section - existing
memories are kept, so switching it back on restores them.

### What the user sees

The demo's rail has a "Memory" section: a `<data-list>` over the type filtered by
`owner == currentUser()`, newest first, with a count badge and a Forget command that calls
`aiMemoryDelete`. That function refuses a row the acting account neither owns nor administers -
the check is in the function, because a command's input is whatever a channel happens to hold.

A memory written during the conversation appears in that list at once (the store's writer thread
notifies the open windows) while the agent is told about it only in the next conversation.

## What an administrator sees

The filled block is written onto the run as `tl.ai.execution:AgentExecution#environment`, as the run
starts - so it is there for a run that is still going and for one that never finished, which are the
two cases in which it is most wanted. The agent activity monitor shows it as a collapsed
"Environment" group in the run detail, beside the transcript.

It is a field of its own rather than a slice of the transcript because it is not part of the
conversation: it sits in the system prompt, which nothing else records.

## Where the code is

| | |
|--|--|
| `agent/environment/PromptPlaceholders` | Which `{{name}}` exists, and what filling one means. |
| `agent/environment/EnvironmentTemplate` | The shipped default, and how an empty section disappears. |
| `agent/environment/RunEnvironment` | Composing one run's block and applying it to a prompt. |
| `agent/environment/EnvironmentConfig` / `EnvironmentSettings` | The `<environment>` configuration section, the shipped resource behind it, and how both are read when the service is not up. |
| `agent/environment/AgentFact` / `EnvironmentFact` / `RunFactsBinding` | The facts: declared, evaluated, and bound for a run. |
| `agent/environment/TurnContext` | Composing the per-turn block, and what a fact may not write into it. |
| `agent/AgentLoop#ensureTurnContext` | Putting the per-turn block into the request, once, compaction or not. |
| `session/Turn#turnContext` / `ConversationLogReplay` | Storing it with the turn, and replaying it. |
| `agent/environment/UserPreferences` / `PreferencesLengthConstraint` | The account's own text and its cap. |
| `agent/environment/UserMemories` / `UserMemoryBinding` | Reading the account's memories, rendering the section, and the per-conversation snapshot. |
| `agent/environment/UserMemoryConfig` / `UserMemorySettings` | The `<user-memory>` configuration section and its defaults. |
| `agent/SystemPromptContributor` / `AgentFactory#appendContributions` | Prose the installation adds to every prompt, and where it lands. |
| `agent/TextPromptContributor` | The shipped contributor: a translatable text for picked agents, its constraints and its CDATA binding. |
| `tools/memory/UserMemoryTool` | `remember` and `recall`. |
| `tools/memory/MemoryConsent` / `MemoryConsentBinding` / `ConsentWords` | What the user agreed to in this turn, and the one rule that decides it. |
| `execution/AgentExecutionStore#createMemory` / `memoriesOf` / `deleteMemory` | The write path, the owner filter and the delete. |
| `scripting/AIScriptFunctions#memoryDelete` | `aiMemoryDelete`, with the ownership check. |
| `agent/AgentFactory#buildConfig` | The one place a run's prompt is composed. |
| `agent/AgentLoop` | Emits `AgentEvent.EnvironmentComposed` right after `RunStarted`. |
| `execution/ExecutionRecorder` | Writes it onto the run record. |
