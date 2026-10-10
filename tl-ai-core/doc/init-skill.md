# The application context: `TL_init`

`TL_init` is the counterpart of `/init` in Claude Code. Where `/init` writes a `CLAUDE.md` that
every later session reads, `TL_init` has Albert (`TL_SingleAgent`) write the **application
context**: one Markdown document saying what the application is for, who works with it and on what,
what the users' terms mean, what the calculated values mean and which rules hold. Albert saves it
with the user's approval, and from the next message on every agent a user talks to is given it in
its system prompt.

The skill is defined in `tl-ai-core.conf.config.xml` next to the other shipped skills. It resolves
Gitea #667.

## Why a document, and why this one

The model tells an agent the structure: types, attributes, references, derived markers. It does not
tell it what the application is for, who asks it what, what an in-house abbreviation means, or which
order of steps the business insists on. That knowledge lives in the heads of the users. `TL_init`
writes it down from two sources: the model, read in more depth than the outline, and an interview
with the user.

## Where the context lives

The context is the singleton `APPLICATION_CONTEXT` of the module `tl.ai.core`, an object of type
`tl.ai.core:ApplicationContext`:

- **`text`**: the Markdown, at most 8000 characters (about 2,000 tokens), with the
  `{{...}}` placeholders of an agent prompt.
- **`agents`**: optional. Empty means every agent a user talks to.

`ApplicationContextContributor` puts the text into the system prompt. `AgentFactory` asks it for
every run, **after** the contributors the installation configures. It is not one of those
configured contributors, because applications override the `LLMService` configuration as a whole
(`config:override="true"`): a contributor shipped in that list would silently never reach them.

- An **infrastructure** agent, such as the prompt improver, never gets the context.
- An application that has written none gets nothing added.

**Why not a `TextPromptContributor`** (#648), which is where the first version of this skill had the
administrator paste the document? Three reasons:

1. **The save freezes `LLMService`.** Saving `LLMService` in the service editor writes a snapshot of
   its whole configuration that shadows every later change of the shipped one (see the warning in
   `environment-block.md`).
2. **It needs a restart.** The change only applies after a service restart.
3. **Language fallback can lose the text silently.** The contributor's text is per language: a
   context written only in German silently does not reach an English user whose fallback language
   is English.

The application context needs none of this. It is one text for every language: an agent reads a
German context and still answers in its user's language. A save applies from the next message on,
also in open conversations. Such a conversation misses the provider's prompt cache once, as after a
restart of the contributor. The knowledge base keeps every earlier version.

`TextPromptContributor` stays for a host that ships a fixed text in its configuration.

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
   - **Approval:** it is a `DATA_WRITE` call, so the user approves it and sees the text while doing
     so. That approval is the review step.
   - **Refusals before the prompt:** the save is refused when the account may not write the
     context, when the text is too long, or when it names an unknown placeholder. So nobody is asked
     to approve a save that cannot go through.
   - **Without the right:** an account that lacks it gets the final document, and the information
     that an administrator can save it.

The whole workflow runs in one run, with no task bookkeeping. Its 15-20 turns fit in the default
turn limit of 60.

## Editing it by hand

*Administration > Development > Agent Editor*, tab *Application context* (*Anwendungskontext*): a
form over the singleton, with the text and the agents. Who may change it is decided by the model's
access rights on `tl.ai.core:ApplicationContext`. By default that is only an administrator, which
is also who may edit services. The same rights decide whether `saveApplicationContext` is refused.

## What it does not do

- **It changes nothing in the application but the context.** No model element, no screen, no
  object. That is a rule of the skill, not a restriction of the run: Albert's own write tools stay
  offered while the skill is loaded. What stands between such a call and a write is the approval
  every one of them needs, as everywhere else in Albert.
- **It does not read the application's data.**

## Tests

- `TestInitSkill` (tl-ai-eval) runs the shipped Albert through `AgentRunner` against a scripted
  model. Albert loads `TL_init` and reads the empty context. It loads `TL_modelExploration` and reads
  a derived attribute's rule (the answer must carry the expression). Then it saves the context.
  - **The approval:** asked exactly once, for the save, with the saved text.
  - **The knowledge base:** a revision diff shows that the run changed the context and, apart from
    the execution store's record of the run, nothing else.
  - **The next run:** Albert's next run is given the text. The eval's `LLMService` configuration
    lists no contributor, so this is also the proof that none is needed.
  - **A declined approval:** writes nothing.
- `TestApplicationContextContributor`: the text reaches an agent's prompt after the configured
  contributors, with no contributor configured. It does not reach an infrastructure agent, and
  named agents narrow it.
- `TestApplicationContextTool`: the refusals that come before the approval (rights, length,
  placeholders, empty text), and a save replacing the text.

Whether a real model follows the workflow is not something these tests can say. That takes a run
against a real model.
