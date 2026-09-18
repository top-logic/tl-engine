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

Per-turn facts are a separate block and a separate ticket (#438). Until it exists they are carried
to `RunEnvironment.contextSection` and deliberately dropped.

## The template

Shipped, localized, and changeable without a build: `LLMService`'s `<environment>` section.

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
| `template` | The block's text, in the language of the account the run acts for. It may use every placeholder but `{{environment}}` - the block cannot contain itself. Empty falls back to `EnvironmentTemplate.DEFAULT`, which is the same English text; `TestEnvironmentTemplate` holds the two together. |
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
| `agent/environment/EnvironmentConfig` / `EnvironmentSettings` | The `<environment>` configuration section and how it is read when the service is not up. |
| `agent/environment/AgentFact` / `EnvironmentFact` / `RunFactsBinding` | The facts: declared, evaluated, and bound for a run. |
| `agent/environment/UserPreferences` / `PreferencesLengthConstraint` | The account's own text and its cap. |
| `agent/AgentFactory#buildConfig` | The one place a run's prompt is composed. |
| `agent/AgentLoop` | Emits `AgentEvent.EnvironmentComposed` right after `RunStarted`. |
| `execution/ExecutionRecorder` | Writes it onto the run record. |
