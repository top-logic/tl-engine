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
You run inside the TopLogic application "Invoice management demo" as the assistant of one user.
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
| `{{preferences}}` | The account's preference text, heading included - or nothing. |
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
an application that names its own providers overrides `LLMService` wholesale, and a template living
there would be lost with it - leaving the English constant in front of headings that are still in
the user's language.

An installation that wants to say something different sets `template` in `LLMService`'s
`<environment>` section, which wins:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMService">
  <instance class="com.top_logic.tl.ai.core.provider.LLMService">
    <environment
      application="…"
      user-preferences="true"
      max-preferences-length="1000"
    >
      <template>
        <en><![CDATA[…]]></en>
        <de><![CDATA[…]]></de>
      </template>
    </environment>
  </instance>
</config>
```

| Property | |
|--|--|
| `template` | The block's text, in the language of the account the run acts for. It may use every placeholder but `{{environment}}` - the block cannot contain itself. Unset uses the shipped resource, and if that is unreachable `EnvironmentTemplate.DEFAULT`, which is the same English text; `TestEnvironmentTemplate` holds the two together and checks that every shipped language uses the same placeholders. |
| `application` | What to call this application in front of the model. Empty uses the application's own name. |
| `user-preferences` | Whether the accounts' preference texts are sent at all. Off sends none, whatever the accounts wrote; the texts are kept. |
| `max-preferences-length` | How long one account's text may be, in characters. Default 1000. Cleared (0 or less) means no cap. |

`{{context}}` and `{{preferences}}` render a **whole section each**, heading included, or nothing at
all. A template line that is nothing but an emptied placeholder is dropped, so a switched-off
preference section leaves no orphaned heading and no gap.

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

**Turn number and time of day are added by the framework.** Both change per turn by nature, which is
exactly why the environment block states the date at day granularity and no time at all. The turn
number is what the entry point counted - `ChatProcessor` binds it with the facts
(`RunFactsBinding.bind(facts, turnNumber)`); a headless or a scheduled run counts none and the line
is then omitted.

**A host that states nothing per turn gets no block.** Turn number and time are added *to* a block;
they do not create one.

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
| `agent/AgentFactory#buildConfig` | The one place a run's prompt is composed. |
| `agent/AgentLoop` | Emits `AgentEvent.EnvironmentComposed` right after `RunStarted`. |
| `execution/ExecutionRecorder` | Writes it onto the run record. |
