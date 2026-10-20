---
description: Read before changing conversation memory or citations in tl-ai-core - ConversationMemory and compaction (CompactionStrategy, SummarizingStrategy, TruncatingStrategy, when the budget is checked), the reading list and citation validation (ReadingList, CitationValidator, CitationKinds, PageArchive, CitationMarkerSyntax), research findings (research.recordFinding, listFindings, readSource) and kept research runs (research.saveReport, ResearchRuns, tl.ai.execution:ResearchRun).
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

An answer cites with links in its text, `[Summer party](tl:GenericObject:11047)` or `[Source](https://...)`, and calls no tool for it (package `citation/`). The **system writes the reading list**: every read tool puts what it read on the run's `ReadingList` - objects where `ReadFilter` discloses an identity, pages in `webFetch` and the browser, an application's records through `ToolExecutionContext.readSource` - and `CitationValidator` keeps a link as a footnote only when it points at an entry of that list, of a kind declared in `CitationKinds`. A subagent's sources join the lead's list when its result is delivered.

Every page that enters a reading list is archived by `PageArchive` (`tl.ai.execution:ArchivedPage`, bounded and with retention), and its footnote offers the copy, served by `/archived-page/{handle}` ([served content](doc:ai/chat-ui#served-content)). An application adds its own marker syntax with a `CitationMarkerSyntax` in `<marker-syntaxes>`. `tl-chat/doc/citation-contract.md` is the contract between the server and the chat.

The same `ReadingList` holds the `SeenUrls` ledger that [URL provenance](doc:ai/security/browsing-governance#url-provenance) checks.

## Research findings and reports

A research run also records **findings** on the same list: `research.recordFinding(claim, source, quote)` refuses a quote that does not occur in the text the run read of that source (`Finding.occursIn`; white space, Markdown escapes and link addresses aside). The findings join the lead's list with the sources on every delivery path, and a delivered report is followed by the worker's findings, one line each. The lead works from them with `research.listFindings` and reads a source's stored text with `research.readSource` - it never fetches. All three are READ tools of the `interaction` group, because they touch only the run tree's reading list.

When the lead has written its report it calls **`research.saveReport`**: the report's links are validated against the run tree's reading list, and `ResearchRuns` keeps a `tl.ai.execution:ResearchRun` on its own thread, **once per lead run** - a second call replaces what the first kept, and a write that takes longer than the tool waits is answered "still being written, do not call again". The run holds the question, effort and status (enumerations), the plan, and the report Markdown - the one source of truth - and **references, not copies**:

- `execution` - the lead's `AgentExecution`, and through its session the chat;
- one ordered composition of `sources` (cited first, by footnote), whose web sources reference their `ArchivedPage` and whose object sources reference the object - a generic reference: when the object is deleted, the source shows the label it was read with, marked as deleted, and keeps its footnote;
- `findings` that reference their source and the worker run that recorded them.

`citedSources` and `alsoRead` (`<collapsed/>`) are TL-Script selections of `sources`; `document` (`ResearchDocument`) and `tokens` (the execution tree) are computed on every read, never stored. The document links archived copies below the request's context path, never with a host, and links only the footnotes the citation check wrote. The owner and administrators read the run, its parts and the pages they reference (role rules in `tl-ai-core.binarySecurity.config.xml`, see [perceivable binaries](doc:ai/security/administration#the-shipped-read-policy-for-perceivable-binaries)); the computed lists apply the reader's rights. The archive's retention keeps a page a run references; deleting the run keeps its pages.

The tool answers the run's id, and `present` opens it in the chat's side pane, which shows the document's body in place and the two lists and the findings as tables. The owner is the account the run works for, so a headless `aiResearch()` run keeps its run for the account it runs as, the same way, as soon as the `TL_deepResearch` skill calls the tool.
