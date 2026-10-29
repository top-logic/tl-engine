---
description: Read before changing conversation memory or citations in tl-ai-core - ConversationMemory and compaction (CompactionStrategy, SummarizingStrategy, TruncatingStrategy, when the budget is checked), citations - the link markers tl:<id> and http(s), the reading list and its validation (ReadingList, ReadingListBinding, CitationValidator, CitationExcerpt, CitationKinds with <kinds> and <marker-syntaxes>, CitationMarkerSyntax, PageArchive with max-chars and retention-days, CitationDto), research findings (research.recordFinding, listFindings, readSource) and kept research runs (research.saveReport, ResearchRuns, tl.ai.execution:ResearchRun).
order: 66
---

# Memory, compaction and citations

## Conversation memory and compaction

`ConversationMemory` (package `com.top_logic.tl.ai.core.memory`) wraps a conversation with a hybrid token estimate: the token count the provider reported last as the baseline, plus a byte heuristic for what was added since. The `CompactionStrategy` interface has two implementations, `SummarizingStrategy` (the model summarizes older turns) and `TruncatingStrategy`.

**When the budget is checked.** `AgentLoop` checks it at the **start of every turn** - after the injections between turns and *before* the turn's request is built - so a conversation that is already over budget is compacted rather than sent, refused and compacted. The same position covers the other direction: a turn's tool results are measured at the start of the next turn, the next moment that history is used. Consequences:

- a run does not compact after its last turn; the next run compacts before its first request, so no summarizer call is spent on a conversation nobody continues;
- the `CompactionStarted` / `CompactionCompleted` pair of a proactive compaction precedes the turn's `ContextComposition` event, it does not follow it;
- a provider that reports `CONTEXT_WINDOW_EXCEEDED` anyway - the estimate cannot see the system prompt or the tool schemas - is still caught by the reactive retry in `callProviderWithReactiveCompaction`, which is the fallback, not the normal path.

`TestCompactionOrder` pins it. A compaction leaves the tool array as it is and only makes a skill's instructions loadable again ([skill overlays](doc:ai/tools-and-skills#skills-and-tool-overlays)); a subagent compacts its own conversation ([delegation](doc:ai/delegation#a-child-compacts-too)).

## Citations

An answer points at its sources **in its text, with a link**, and calls no tool for it (package `citation/`):

```markdown
[Sommerfest](tl:GenericObject:11047) und [Herbstmarkt](tl:GenericObject:11048) sind geplant;
das Fest ist [laut Programm](https://www.example.org/sommerfest "Das Sommerfest findet am 4. Juli statt.").
```

`tl:<id>` points at an object by the id a tool printed for it, an `http(s)` address at a web page; the link text is the part of the sentence the source supports, and an optional link title is the source's sentence the statement rests on, copied verbatim. Albert's `<cite>` prompt section says so; a host agent with its own prompt that should cite needs the same section.

### The reading list

**The system writes the reading list; the model only points.** `ReadingList` holds the `ReadSource`s (`tl-ai-tool-api`) of one run; it lives on the run's `ToolContext` and is shared by every copy of it, so the tools of a parallel batch register into the same list. Code below a tool registers through `ReadingListBinding`, which `ToolExecutions.execute` binds around every call.

| Source | Registered by | Kind |
|---|---|---|
| an object a read tool showed - `query`, `present`, a script tool, `getInstance`, `listInstances` | `ReadFilter` where it discloses an identity (`_tid`/`_display`, `label [id:TID]`); `getInstance`, `listInstances`, `readText`, `viewDocument` explicitly | the first declared kind whose `types` cover the object's type, else `object` |
| a web page | `webFetch` and the browser's full renders (`browserNavigate`, `browserSnapshot`), through `WebSources` | `web` |
| an application's own record | the application's tool, `ToolExecutionContext.readSource(...)` | the application's kind |

A `webSearch` hit is not registered: a result nobody opened was not read. A run keeps at most 5000 sources; the transcript records the first 500 of each turn (`Turn#sources`, `TurnDto.sources`) without the read text. A subagent's list joins the lead's when its result is **delivered** - when a delegation returns, when `wait_for_subagent` hands the result over, or when `AgentLoop.injectSubagentResults` injects it; a `SubagentJob` carries the child's list for that. Merging on delivery rather than writing through to the parent's list is what lets a detached subagent outlive the run that spawned it.

### The validator

`CitationValidator.apply(log, answer, readingList)` runs at the end of an `AgentRunner` run, before the transcript is completed. For every marker of the turn's texts, outside code spans and fenced blocks:

1. A reference that names a source on the reading list, of a **declared kind**, becomes a footnote `[n]` behind the marker's text, numbered per answer in reading order; a source cited twice keeps its number.
2. A `tl:` reference that names nothing read loses its link and keeps its text.
3. An `http(s)` address that names nothing read but occurs in the turn's tool results or in the user's message - a search hit, a page `publishPage` returned - stays an ordinary link; any other address is invented and loses its link.
4. In an answer that cites, a `[n]` the model wrote itself is written `&#91;n&#93;`, so it cannot pass for a footnote. An answer that cites nothing is not touched.

Images, relative links and links of other schemes are no markers. `CitationExcerpt` produces what the popover shows from what the tool read (`ReadSource.text`), never from the raw transport format: a window around the passage, highlighted, when the link title is a passage of at least 12 characters the read text contains and the kind has `passage="true"`; else the opening sentences of a page, an object's own prose field or its `label: value` fields, or the title.

### Kinds

`kind` is a vocabulary the application owns, in the `CitationKinds` service; the tool that read a source decides its kind, not the model. The framework declares `web` (a page `webFetch` or the browser read) and `object` (an object of a type no other kind claims):

```xml
<config service-class="com.top_logic.tl.ai.core.citation.CitationKinds">
	<instance>
		<kinds>
			<kind name="invoice" description="an invoice of this application" types="billing:Invoice" passage="false">
				<label>
					<de>Rechnung</de>
					<en>Invoice</en>
				</label>
			</kind>
		</kinds>
	</instance>
</config>
```

- **`types`** - the qualified model types whose objects are sources of this kind (a subtype counts); an object is registered with the first kind that claims it.
- A source whose kind nobody declared is not cited (DEBUG log); outside an application (a unit test) nothing is refused for that.
- **`passage="false"`** - a structured object: its popover shows the fields the tool read, not a window of text.
- **`label`** - what the sources rail and the popover show, in the reader's language (`CitationDto.kindLabel`); a `web` source shows its host instead.

An application whose agents cite another way registers a `CitationMarkerSyntax` in `<marker-syntaxes>` of `CitationKinds` (`<syntax class="com.example.RecordCodeMarkers"/>`). A syntax only finds markers - positions, the text to keep, the references named; whether a reference holds is the reading list's answer, so the application's tools register what they read with `context.readSource(new ReadSource(kind, code, title, ...))` under the reference the markers name. The link syntax (`LinkMarkers`) is asked first, and a marker overlapping an earlier one is ignored. `RecordCodeMarkers` in the tests reads codes in parentheses, `(FACT-001, DEC-002)`.

### The page archive

Every page that enters a reading list is archived by `PageArchive` as a `tl.ai.execution:ArchivedPage` - extracted text (a `tl.core:Binary`), title, address, fetch time:

- in its own transaction on its own thread, so a copy is never rolled back with a turn;
- **what the run read, not more**: a long document read in windows (`webFetch` with `offset`) is kept as every window read, in document order, with `[...]` for a part left out - the same text the reading list checks a quoted passage against; each further window extends the same copy under the same handle;
- bounded by `max-chars` per copy (default 1 000 000, also the bound of the reading list's text), `retention-days` (default 90, 0 keeps all, purged daily) and `enabled`; the same page with the same text read again by the same account is stored once per process;
- served at `/archived-page/{handle}` ([served content](doc:ai/chat-ui#served-content)).

### Wire and rendering

`CitationDto` (`index`, `toolName`, `toolCallId`, `kind`, `kindLabel`, `id`, `label`, `url`, `passage`, `excerpt`, `archiveUrl`, `archivedAt`) travels on `message_complete` and on the persisted `TextPartDto`, so a reload restores the same footnotes; `toolName` and `toolCallId` are empty for a citation found through the reading list. The chat renders `[n]` as a small number after the marker's text (a keyboard-reachable `<button>` with `data-citation-id`); hover or focus opens a popover with the kind's icon, the label, the origin, the excerpt or fields, and the archived copy beside the live page. A click opens the source - `messageAnnotations.render` decides for a host kind, else the web default opens the page. The sources rail lists every source of the conversation once, with its marker numbers; its badge counts the sources of the newest answer (`latestAnswerSources`).

The same `ReadingList` holds the `SeenUrls` ledger that [URL provenance](doc:ai/security/browsing-governance#url-provenance) checks.

## Research findings and reports

A research run also records **findings** on the same list: `research.recordFinding(claim, source, quote)` refuses a quote that does not occur in the text the run read of that source (`Finding.occursIn`; white space, Markdown escapes and link addresses aside). The findings join the lead's list with the sources on every delivery path, and a delivered report is followed by the worker's findings, one line each. The lead works from them with `research.listFindings` and reads a source's stored text with `research.readSource` - it never fetches. All three are READ tools of the `interaction` group, because they touch only the run tree's reading list.

When the lead has written its report it calls **`research.saveReport`**: the report's links are validated against the run tree's reading list, and `ResearchRuns` keeps a `tl.ai.execution:ResearchRun` on its own thread, **once per lead run** - a second call replaces what the first kept, and a write that takes longer than the tool waits is answered "still being written, do not call again". The run holds the question, effort and status (enumerations), the plan, and the report Markdown - the one source of truth - and **references, not copies**:

- `execution` - the lead's `AgentExecution`, and through its session the chat;
- one ordered composition of `sources` (cited first, by footnote), whose web sources reference their `ArchivedPage` and whose object sources reference the object - a generic reference: when the object is deleted, the source shows the label it was read with, marked as deleted, and keeps its footnote;
- `findings` that reference their source and the worker run that recorded them.

`citedSources` and `alsoRead` (`<collapsed/>`) are TL-Script selections of `sources`; `document` (`ResearchDocument`) and `tokens` (the execution tree) are computed on every read, never stored. The document links archived copies below the request's context path, never with a host, and links only the footnotes the citation check wrote. The owner and administrators read the run, its parts and the pages they reference (role rules in `tl-ai-core.binarySecurity.config.xml`, see [perceivable binaries](doc:ai/security/administration#the-shipped-read-policy-for-perceivable-binaries)); the computed lists apply the reader's rights. The archive's retention keeps a page a run references; deleting the run keeps its pages.

The tool answers the run's id, and `present` opens it in the chat's side pane, which shows the document's body in place and the two lists and the findings as tables. The owner is the account the run works for, so a headless `aiResearch()` run keeps its run for the account it runs as, the same way, as soon as the `TL_deepResearch` skill calls the tool.
