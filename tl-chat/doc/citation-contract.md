# Citation contract

Assistant answers cite sources with markers of the form `[n]`. The markers are **not** parsed out of
a fence in the answer. The model records each source by calling the `cite` tool, then writes the
number that tool returned immediately after the sentence it supports.

The tool has three names and all three occur: `cite` is what the model calls and what a recorded
tool call carries, `cite.cite` is what an agent definition and the access policy write, and
`cite__cite` is the provider-safe registry key. The validator matches all three, because matching
only the configured one made every declaration invisible.

## Wire

`CitationDto` (`index`, `toolName`, `toolCallId`, `kind`, `id`, `label`, `url`) travels:

- on `message_complete` (SSE), so the live answer can chip the markers
- on persist `TextPartDto`, so a reload restores the same chips

The compact `Part.TextPart` constructor is still `(id, timestamp, text)`.

## Tool and prompt

`cite.cite` (`ToolMode.READ`) records `kind`, `id`, `label`, and optional `url` / `toolCallId`. It
returns the next 1-based marker for this run.

The numbering belongs to the **run**, not to the calling thread: the tool is parallel-safe, so two
cite calls of one turn run on two pool threads, and a per-thread counter gave both of them `1`. The
counter is `CitationMarkers` on the run's `ToolContext`, shared by every copy of that context and
reset by `AgentRunner` when a run begins. A delegated sub-agent gets its own.

Albert (`TL_SingleAgent`) has the tool on its catalog and a `<cite>` fragment in the system prompt:
cite what you read, using the id or URL the producing tool returned; this turn only; one marker per
fact. That fragment is a product rule, not a host prompt. A host agent that should cite needs the
same tool on its catalog (and, if it has its own prompt, the same instruction).

## Validator

`CitationValidator` runs at the end of an `AgentRunner` run, **before** the transcript is completed:

1. Collect this turn's cite declarations. **No declaration means no rewrite**: the answer is passed
   through untouched, brackets and all.
2. Allowed set = every `id` or `url` that appears as a **whole token** in this turn's other tool
   results - `7` is not evidenced by `700`, and `…/page/1/` is not evidenced by `…/page/12/`.
3. Drop declarations whose `id`/`url` is missing (DEBUG: invented dropped, valid kept, renumbering,
   none at all). Kept declarations are keyed by tool call id, so two that claim the same number are
   both seen instead of one overwriting the other.
4. Parse `[n]` from **every** `TextPart` of the turn, skipping inline code spans and fenced blocks.
   Markers whose declared citation survived are renumbered in order of first appearance; markers
   claimed by a dropped declaration are deleted together with the space in front of them; **every
   other `[n]` stays exactly as the model wrote it** - an array index, a footnote and a code sample
   are prose, not citations.
5. Rewrite the turn's `TextPart`s, attach the kept citations to the parts their markers occur in,
   and put text plus citations on `message_complete`.

## Rendering

`[n]` becomes a round accent chip (`data-citation-id`, keyboard-reachable `<button>`). The default
annotation is a popover with kind, label, and a link when `url` is set. A host `messageAnnotations`
renderer may call `openInPane` instead (the demo does this for `kind === 'invoice'`).
