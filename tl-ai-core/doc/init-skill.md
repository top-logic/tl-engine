# The application context: `TL_init`

`TL_init` is the counterpart of `/init` in Claude Code. Where `/init` writes a `CLAUDE.md` that
every later session reads, `TL_init` has Albert (`TL_SingleAgent`) write the **application
context**: one Markdown document saying what the application is for, who works with it and on what,
what the users' terms mean, what the calculated values mean and which rules hold. Albert saves it
with the developer's approval into the application's source configuration, and once the LLM service
is restarted the agents are given it in their system prompt.

The skill is defined in `tl-ai-core.conf.config.xml` next to the other shipped skills. It resolves
Gitea #667.

## Why a document, and why this one

The model tells an agent the structure: types, attributes, references, derived markers. It does not
tell it what the application is for, who asks it what, what an in-house abbreviation means, or which
order of steps the business insists on. That knowledge lives in the heads of the users. `TL_init`
writes it down from two sources: the model, read in more depth than the outline, and an interview
with the user.

## Where the context lives

The application context is **development material**, not business content: a developer builds the
application and sets up its agents, and writing down what the application is for is part of that. So
it lives where the rest of that work lives - in the application's source, in git, deployed with the
application. Concretely it is a `TextPromptContributor` (#648) among the prompt contributors of
`LLMService`, marked by its `name`:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMService">
  <instance class="com.top_logic.tl.ai.core.provider.LLMService">
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

- **`name="application-context"`** is how the tools find the entry among the others
  (`TextPromptContributor.Config.APPLICATION_CONTEXT`). Every other contributor leaves `name` empty.
- **`text`**: the Markdown, at most 8000 characters (about 2,000 tokens), with the `{{...}}`
  placeholders of an agent prompt. The save writes **the same text into every language the
  application supports**, in a CDATA section. That avoids the language trap of a per-language text:
  a context written only in German would silently not reach a user whose language falls back to
  English. An agent reads a German context and still answers in its user's language. A developer who
  wants a translated text edits the file.
- **`agents`**: written empty, so every agent gets the context - also the infrastructure agents such
  as the prompt improver, which then pay for it on every run. A developer who names agents here keeps
  them: a later save replaces the text and leaves `agents` alone.

A changed text takes effect when the LLM service is restarted, like any `TextPromptContributor`.

## How the save writes the file

`saveApplicationContext` writes into the application's **source**, never into the runtime
configuration: the `WEB-INF/autoconf` folder of the top level project (`Workspace.topLevelWebapp()`),
not `tl_autoconf_dir` and not the snapshot the service editor saves. Which file
(`ApplicationContextSource`):

1. **A file that already holds the entry**: its text is replaced. Normally that is one file. A second
   one appears when the service editor saved `LLMService` after the entry was written, because its
   snapshot carries the entry too; both are then updated.
2. **Otherwise the file that configures `LLMService` and is read last**: the entry is added to its
   `<prompt-contributors>`. Files of the folder are read in the order of their names, and a file with
   `config:override="true"` drops what the earlier ones said, so the last one is the only safe place.
3. **Otherwise a new file, `ai-application-context.config.xml`**, that adds the entry **without**
   `config:override`. The prompt contributors are a list without a key, and the engine appends the
   entries of a later configuration layer to such a list, so the file adds one entry and everything
   tl-ai and the application configured stays. Two choices in that name:
   - It is not `com.top_logic.tl.ai.core.provider.LLMService.config.xml`, the name the service editor
     uses. An application file of that name hides tl-ai-core's file of the same name, and with it
     tl-ai's own settings of the service (`default-model` and the rest).
   - It sorts before that name. If the service editor later saves `LLMService`, its snapshot - which
     then contains the entry - replaces this file's entry instead of adding it a second time.

The file is edited in place: only the entry is written anew, every other character - comments,
attribute layout, blank lines - stays as the developer left it (`XmlSpans`). The answer names the
file and tells the developer to restart the LLM service and to commit the file.

**Only in the development environment.** The save is offered only when the engine reports the
development environment (`tl_operation_mode=development`) and the project's `src/main/webapp` folder
exists (`DevelopmentWorkspace`) - the same folder the engine's own in-app tools write back to, such as
exporting a model module into the workspace. In a test or production installation there is no source
to write: the save is not offered at all (`Tool.unavailableReason`), and Albert hands over the
document with that reason instead. `readApplicationContext` works everywhere.

**Why not the database** (the previous version of this PR): the context is part of how the
application is built, so it belongs in its source and travels with it through git, review and
deployment, like the model. A database object would have to be carried from the development
installation to every other one by hand.

## What the skill does

1. **Existing context**: `readApplicationContext`. If there is one, the skill revises it instead of
   starting over, like `/init` improving an existing `CLAUDE.md`.
2. **Analyze the model**: `outlineModel`, then `TL_modelExploration`:
   - The calculation rule of every derived attribute (`getCurrentAnnotations`, the `expr` of its
     `storage-algorithm`), read so that the document can say what the value **means**.
   - Parts and enum values where the outline leaves them out.
   - A comparison of what each type and attribute *says* (label and description) with what it
     *does*.

   Every discrepancy is reported to the user and **not** fixed. Discrepancies stay out of the
   document: they are defects of the model, not knowledge for the agents. The application's
   **objects are not analyzed**: the context describes what the application is, not what is in it
   today.
3. **Interview**: at most five `askUser` calls, in the user's language:
   - purpose;
   - the users and their tasks;
   - the users' own terms and their meaning;
   - rules and procedures;
   - what the agents should take care of or avoid.
4. **Draft and review**: the document in fixed sections (Purpose, Users, Terms, Core objects,
   Calculated values, Rules), in the user's language, short.
   - It states only what the model does not say, and nothing that neither the model nor the user
     said.
   - A calculated value gets its meaning, not its expression. An agent that needs the expression
     reads it in one call, and a 10-line rule in every system prompt would cost every run.
   - The document is shown in one code block, followed by a choice in the user's language, "save"
     or "change". An answer in the user's own words counts as a change request.
5. **Save**: `saveApplicationContext` with the complete document.
   - **Approval:** it is a `STRUCTURAL` call - it changes the application's configuration, which is
     what the approval card says, and the engine's ban on structural changes in production is a second
     guard - so the user approves it and sees the text while doing so. That approval is the review
     step. The file is written when the call runs; the turn's transaction does not cover it.
   - **Refusals before the prompt:** the save is refused when the account is not an administrator
     (the people who may change a service's configuration), when the text is empty or too long, or
     when it names an unknown placeholder. So nobody is asked to approve a save that cannot go
     through.
   - **Hand-over:** "saved into `<file>`; restart the LLM service; commit the file". Where the save
     is not offered or is refused, the user gets the final document in one code block and the reason.

The whole workflow runs in one run, with no task bookkeeping. Its 15-20 turns fit in the default
turn limit of 60.

## Re-running it

`readApplicationContext` returns the active text. In the development environment it also reads the
source file, and when the file holds a text that is not active yet - saved, but the service not
restarted - it returns that one and says so. The skill revises what it reads rather than starting
over. Editing by hand is editing the file.

## What it does not do

- **It changes nothing in the application but the context.** No model element, no screen, no
  object. That is a rule of the skill, not a restriction of the run: Albert's own write tools stay
  offered while the skill is loaded. What stands between such a call and a write is the approval
  every one of them needs, as everywhere else in Albert.
- **It does not read the application's data.**

## Tests

- `TestInitSkill` (tl-ai-eval) runs the shipped Albert through `AgentRunner` against a scripted
  model, with a temporary folder as the application's source:
  - **The workflow:** Albert loads `TL_init`, reads the empty context, loads `TL_modelExploration`,
    reads a derived attribute's rule (the answer must carry the expression) and saves. The approval
    is asked exactly once, for the save, with the saved text. The application's `LLMService` file
    then holds one entry with the text, and a revision diff shows that the run changed none of the
    application's data.
  - **The restart:** the next run's prompt does not carry the text yet; after the configuration is
    reloaded with the written file and `LLMService` restarted - what the service editor does - it
    does.
  - **A re-run** reads the context and replaces it: still one entry.
  - **No file of its own:** the save creates the additive file, and after the restart the eval's own
    providers and default model are still there.
  - **Outside the development environment** the save is not among the offered tools, a call is
    answered with the reason, and nobody is asked to approve anything.
  - **A declined approval** writes nothing.
- `TestApplicationContextSource` (tl-ai-core): the file edits, read back with the engine's
  configuration reader and its layering - the entry added with every other character unchanged, a
  second save replacing it and keeping the named agents, a new file on top of the shipped settings,
  and the entry going into the file read last.

Whether a real model follows the workflow is not something these tests can say. That takes a run
against a real model.
