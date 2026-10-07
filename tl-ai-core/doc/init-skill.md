# The application context: `TL_init`

`TL_init` is the counterpart of `/init` in Claude Code. Where `/init` writes a `CLAUDE.md` that
every later session reads, `TL_init` has Albert (`TL_SingleAgent`) write the **application
context**: one Markdown document saying what the application is for, who works with it and on what,
what the users' terms mean and which rules hold. An administrator installs it in a
`TextPromptContributor`, and from then on it is part of the system prompt of the agents they chose.

It is defined in `tl-ai-core.conf.config.xml` next to the other shipped skills, and it is the
resolution of Gitea #667.

## Why a document, and why this one

The model tells an agent the structure: types, attributes, references, derived markers. It does not
tell it what the application is for, who asks it what, what an in-house abbreviation means, or which
order of steps the business insists on. That knowledge used to live only in the heads of the users
and, if at all, in a hand-written `TextPromptContributor` text (#381). `TL_init` writes that text
from two sources: the model, read in more depth than the outline, and an interview with the user.

## What it does

The skill is a workflow. Albert keeps track of the phases with `TL_taskManagement` - one task per
phase, as an explicit exception to that skill's rule that tasks are for work with several
delegations, because this workflow is long and waits for the user several times.

1. **Existing context** - if Albert's system prompt already carries an application context, the
   skill revises it instead of starting over, like `/init` improving an existing `CLAUDE.md`.
2. **Analyze the model** - `outlineModel`, then `TL_modelExploration`: the calculation rule of every
   derived attribute (`getCurrentAnnotations`, the `expr` of its `storage-algorithm`), quoted
   verbatim; parts and enum values where the outline leaves them out; and a comparison of what each
   type and attribute *says* - label and description - with what it *does*. Every discrepancy is
   reported to the user and **not** fixed. Discrepancies do not go into the document: they are
   defects of the model, not knowledge for the agents. The application's **objects are not
   analyzed** - the context describes what the application is, not what is in it today.
3. **Interview** - at most five `askUser` calls: purpose, the users and their tasks, the users' own
   terms and their meaning, rules and procedures, and what the agents should take care of or avoid.
4. **Draft and review** - the document in fixed sections (Purpose, Users, Terms, Core objects,
   Calculated values, Rules), in the user's language, short, stating only what the model does not
   say and nothing that neither the model nor the user said. Shown in one code block and revised
   until the user is done.
5. **Hand over** - the final document, and how an administrator installs it: *Administration >
   Development > Service Editor*, the *LLM service* (*LLM-Dienst*), under *Prompt contributors*
   (*Prompt-Beiträge*) a *Text prompt contributor* (*Text für den System-Prompt*) with the document
   in the *Text* field of its language and the chosen *Agents* (*Agenten*), then *Save* and
   *Restart* (*Speichern*, *Neu starten*). The steps name every screen element by the label the
   administrator sees, in the language of the user - the skill carries both label sets, taken from
   the shipped resource bundles - because the administrator follows them on the screen, not in the
   configuration file.

## What it does not do

- **It changes nothing in the application.** No model element, no screen, no object.
- **It does not install the document.** Writing the service configuration from an agent run would
  be a second, unguarded way of changing what every user's agent is told; the administrator does
  it, in the place that already exists for it.

## Tests

`TestInitSkill` parses the shipped configuration and pins the shape:

- Albert lists `TL_init`;
- neither the skill nor any skill its instructions name has a tool that changes the application -
  only `READ` and `USER_SCOPED` (Albert's own task list) are allowed;
- every skill its instructions name is one Albert carries.

`TestFrameworkSkillReferences` checks that its description qualifies the reference to
`TL_modelExploration`. Whether a model follows the workflow is not something these tests can say;
that takes a run against a real model.
