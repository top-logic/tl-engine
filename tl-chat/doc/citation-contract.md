# Citation contract

Assistant answers cite sources with markers of the form `[n]`. The markers are **not** parsed out of
a fence in the answer. The model records each source by calling the `cite` tool, then writes the
number that tool returned immediately after the sentence it supports.

The tool has three names and all three occur: `cite` is what the model calls and what a recorded
tool call carries, `cite.cite` is what an agent definition and the access policy write, and
`cite__cite` is the provider-safe registry key. The validator matches all three, because matching
only the configured one made every declaration invisible.

## Wire

`CitationDto` (`index`, `toolName`, `toolCallId`, `kind`, `id`, `label`, `url`, `passage`,
`excerpt`) travels:

- on `message_complete` (SSE), so the live answer can chip the markers
- on persist `TextPartDto`, so a reload restores the same chips

The compact `Part.TextPart` constructor is still `(id, timestamp, text)`.

## Tool and prompt

`cite.cite` (`ToolMode.READ`) records `kind`, `id`, `label`, and optional `url` / `toolCallId` /
`passage`. It returns the next 1-based marker for this run.

`passage` is the sentence of the source the fact rests on, copied verbatim. The validator keeps it
only when the turn's own tool results really contain it, and cuts the excerpt around it out of that
same result - so the popover shows the source's wording in its context rather than the model's
memory of it. A passage shorter than 12 characters is ignored.

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
   Markers whose declared citation survived are renumbered in order of first appearance. **Every
   other `[n]` outside code is deleted**, together with the space in front of it - whether a cite
   call claimed it and lost or the model wrote it out of nowhere: in a turn that cited, `[n]` is
   that turn's own numbering, and a citation-shaped thing that resolves to nothing is worse than no
   marker (#401 §3.2, "no marker survives without a citation").

   The cost is that a prose bracket in a citing turn goes with it. That is why step 1 matters: a
   turn that cited **nothing** is not rewritten at all, so `werte[0]`, footnotes and code samples in
   an ordinary answer are safe. Code spans and fenced blocks are safe in both cases.
5. Rewrite the turn's `TextPart`s, attach the kept citations to the parts their markers occur in,
   and put text plus citations on `message_complete`.

## Rendering

`[n]` becomes a small grey number after the sentence (`data-citation-id`, keyboard-reachable
`<button>`) - quiet, so it says where a fact comes from without taking the eye off the text.

Hovering or focusing it opens a popover with the source's label, its origin (the web host, or the
kind) and the excerpt with the cited passage highlighted. Clicking **opens** the source:
`messageAnnotations.render` decides what that means for a host kind, and a host that returns nothing
for a kind leaves the web default, which opens the page. The same action is what the `Quellen` rail
section and the per-answer `Quellen · n zitiert` pill perform.

A message's own text never carries a `<quote/>`; that element belongs to the composer's reference
tokens (#407) and travels the other way, from the user to the model.
