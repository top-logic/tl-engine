# Citation contract

Assistant answers cite sources with markers of the form `[n]`. The markers are **not** parsed out of
a fence in the answer. The model records each source by calling the `cite.cite` tool, then writes
the number that tool returned immediately after the sentence it supports.

## Wire

`CitationDto` (`index`, `toolName`, `toolCallId`, `kind`, `id`, `label`, `url`) travels:

- on `message_complete` (SSE), so the live answer can chip the markers
- on persist `TextPartDto`, so a reload restores the same chips

The compact `Part.TextPart` constructor is still `(id, timestamp, text)`.

## Tool and prompt

`cite.cite` (`ToolMode.READ`) records `kind`, `id`, `label`, and optional `url` / `toolCallId`. It
returns the next 1-based marker for this run.

Albert (`TL_SingleAgent`) has the tool on its catalog and a `<cite>` fragment in the system prompt:
cite what you read, using the id or URL the producing tool returned; this turn only; one marker per
fact. That fragment is a product rule, not a host prompt. A host agent that should cite needs the
same tool on its catalog (and, if it has its own prompt, the same instruction).

## Validator

`CitationValidator` runs at the end of an `AgentRunner` run, **before** the transcript is completed:

1. Collect this turn's `cite.cite` declarations.
2. Allowed set = every `id` or `url` that appears as a substring in this turn's **other** tool
   results.
3. Drop declarations whose `id`/`url` is missing (DEBUG: invented dropped, valid kept, renumbering,
   none at all).
4. Parse `[n]` from the answer; keep markers whose declared citation survived; **renumber in order
   of first appearance**; strip orphan markers.
5. Rewrite the last assistant `TextPart` and put the cleaned text plus citations on
   `message_complete`.

## Rendering

`[n]` becomes a round accent chip (`data-citation-id`, keyboard-reachable `<button>`). The default
annotation is a popover with kind, label, and a link when `url` is set. A host `messageAnnotations`
renderer may call `openInPane` instead (the demo does this for `kind === 'invoice'`).
