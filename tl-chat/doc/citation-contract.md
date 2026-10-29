# Citation contract

An answer points at its sources **in its text, with a link**, and calls no tool for it (#617):

```markdown
[Sommerfest](tl:GenericObject:11047) und [Herbstmarkt](tl:GenericObject:11048) sind geplant;
das Fest ist [laut Programm](https://www.example.org/sommerfest "Das Sommerfest findet am 4. Juli statt.").
```

- `tl:<id>` points at an object of the application, by the id a tool printed for it.
- An `http(s)` address points at a web page.
- The link text is the part of the sentence the source supports.
- An optional link title is the sentence of the source the statement rests on, copied verbatim.

**The system writes the reading list; the model only points.** Every tool that reads a source puts
it on the run's reading list - kind, id or address, title. The validator keeps a link as a footnote
only when it points at an entry of that list. A source no tool read does not count, however
plausible its id looks. There used to be a `cite` tool, one call per source; it cost a model round
trip per source, came after the answer text and pushed the answer into the trace. It is gone.

## The reading list

`ReadingList` (tl-ai-core `citation/`) holds the `ReadSource`s (tl-ai-tool-api) of **one run**. It
lives on the run's `ToolContext` and is shared by every copy of it, so the read tools of a concurrent
batch register into the same list.

Who registers what:

| Source | Registered by | Kind |
|---|---|---|
| An object a read tool showed - `query`, `present`, a script tool, `getInstance`, `listInstances` | `ReadFilter`, where an identity is disclosed (`_tid`/`_display`, `label [id:TID]`); `getInstance` / `listInstances` / `readText` / `viewDocument` explicitly | the first declared kind whose `types` cover the object's type, else `object` |
| A web page | `webFetch`, and the browser's full renders (`browserNavigate`, `browserSnapshot`) - through `WebSources` | `web` |
| An application's own record | the application's tool, `ToolExecutionContext.readSource(...)` | the application's kind |

Code below a tool registers through `ReadingListBinding`, which `ToolExecutions.execute` binds around
every call on the thread the tool runs on, next to `RunInfoBinding`.

Not registered: a `webSearch` hit. A search result nobody opened was not read.

A run keeps at most 5 000 sources (a query over ten thousand objects reads each of them). The
transcript records the first 500 of each turn (`Turn#sources`, `TurnDto.sources`), without the read
text, so that a later view - a research report's "also read" - can show what was read beside what
was cited. Nothing shows it yet.

## The run tree

A sub-agent's run has a reading list of its own. Its sources join the lead's list when its result is
**delivered** to the lead:

- when `delegate` returns (`DelegationTool`),
- when `wait_for_subagent` hands the result over,
- when the result is injected at the start of the lead's next turn (`AgentLoop.injectSubagentResults`).

A `SubagentJob` carries the child's list for that. Merging on delivery, rather than writing through
a pointer to the parent's list, is deliberate: a detached sub-agent outlives the run that spawned it,
and the answer that uses its result is checked against the list of the run that received it.

## Validator

`CitationValidator.apply(log, answer, readingList)` runs at the end of an `AgentRunner` run, before
the transcript is completed. For every marker of every text of the turn, outside code spans and
fenced blocks:

1. A reference that names a source on the reading list, of a **declared kind**, becomes a footnote
   `[n]` behind the marker's text. Footnotes are numbered per answer in the order the reader meets
   them; a source cited twice keeps its number.
2. A `tl:` reference that names nothing read loses its link; its text stays.
3. An `http(s)` address that names nothing read, but that the turn's tool results or the user's
   message contain - a search hit, a page `publishPage` returned - stays the ordinary link it was.
   Any other address is invented and loses its link.
4. In an answer that cites, a `[n]` the model wrote itself is written as `&#91;n&#93;`: it renders as
   the same brackets but cannot be mistaken for a footnote. An answer that cites nothing is not
   touched.

Images, relative links and links of other schemes are not markers.

## Passage and excerpt

`CitationExcerpt` produces what the popover shows, from what the tool read (`ReadSource.text`) and
the turn's tool results - never a slice of the raw transport format (#418):

| What the reader gets | When |
|---|---|
| a window of the source around the passage, the passage highlighted | the link title is a passage of at least 12 characters that the read text really contains, and the kind has `passage="true"` |
| the opening sentences of the page | a text source cited without a passage |
| the object's own prose field, or its `label: value` fields | an object |
| the source's title | nothing else is known |

## Kinds are declared

`kind` is a vocabulary the application owns, configured in the `CitationKinds` service. The model no
longer names a kind; the tool that read the source decides it. The framework declares two:

```xml
<config service-class="com.top_logic.tl.ai.core.citation.CitationKinds">
  <instance>
    <kinds>
      <kind name="invoice"
        description="an invoice of this application"
        types="billing:Invoice"
        passage="false"
      >
        <label>
          <de>Rechnung</de>
          <en>Invoice</en>
        </label>
      </kind>
    </kinds>
  </instance>
</config>
```

- `web` - a page `webFetch` or the browser read;
- `object` - an object of a type no other kind claims.

What a kind declaration is for:

- **`types`** - the qualified names of the model types whose objects are sources of this kind (a
  subtype counts as its type). An object is registered with the first kind that claims it.
- **the validator** - a source whose kind nobody declared is not cited (DEBUG log). Outside an
  application (a unit test) nothing is refused for being undeclared.
- **`passage`** - `false` says a source of this kind is a structured object: its popover shows the
  fields the tool read, not a window of text.
- **`label`** - what the `Quellen` rail and the popover show a reader, in the reader's language
  (`CitationDto.kindLabel`). A `web` source shows its host name instead.

## Another marker syntax (application hook)

An application whose agents cite another way registers a `CitationMarkerSyntax` in
`<marker-syntaxes>`:

```xml
<config service-class="com.top_logic.tl.ai.core.citation.CitationKinds">
  <instance>
    <marker-syntaxes>
      <syntax class="com.example.RecordCodeMarkers"/>
    </marker-syntaxes>
  </instance>
</config>
```

A syntax only **finds** markers: positions, the text to keep, the references named. Whether a
reference holds is the reading list's answer, exactly as for a link - so an application's tools
register what they read with `context.readSource(new ReadSource(kind, code, title, ...))`, under the
same reference its markers name. The link syntax (`LinkMarkers`) is always asked first; a marker of a
later syntax that overlaps an earlier one is ignored. `RecordCodeMarkers` in the tests is an example
for codes in parentheses, `(FACT-001, DEC-002)`.

## Web pages are archived

When a page enters a reading list, `PageArchive` stores a copy: the extracted text, the title, the
address and the fetch time, as a `tl.ai.execution:ArchivedPage` (text as a `tl.core:Binary` - the
attribute kind the engine's blob store, Trac #29715, will take over, so the archive moves with it
without a model change).

- **Its own transaction, on its own thread**, so a copy is never rolled back with a turn.
- **What the run read, not more.** A long document is read in windows (`webFetch` with `offset`).
  The copy - and the source's text on the reading list, which a quoted passage is checked against -
  holds every window the run read, in document order, with `[...]` where a part was left out. Each
  further window extends the same copy under the same handle, so one citation shows the whole text
  read; a passage from a part the run never read is not shown as the source's words.
- **Bounded:** `max-chars` per copy (default 1 000 000, about fifty windows - a long report read
  through), the same bound on the reading list's text, `retention-days` (default 90, 0 keeps all;
  purged once a day), `enabled`. The same page with the same text read again by the same account is
  stored once within the life of the process.
- **Served at `/archived-page/{handle}`** (`ArchivedPageServlet`): a login is required, then the
  account whose run read the page or an administrator - the `/screenshot` rule, because a page
  fetched under somebody's browsing rights can be an intranet page. Unknown and foreign handles both
  answer 404. The text is rendered through `Converters.markdownToHtmlString` under a content security
  policy that loads nothing.

The citation carries `archiveUrl` and `archivedAt`; the popover and the source's row in the rail offer
"Archivierte Kopie vom …" beside the live page.

## Wire

`CitationDto` (`index`, `toolName`, `toolCallId`, `kind`, `kindLabel`, `id`, `label`, `url`,
`passage`, `excerpt`, `archiveUrl`, `archivedAt`) travels on `message_complete` (SSE) and on the
persisted `TextPartDto`, so a reload restores the same footnotes. `toolName` and `toolCallId` are
empty for a citation found through the reading list.

## Rendering

`[n]` becomes a small grey number after the marker's text (`data-citation-id`, keyboard-reachable
`<button>`). Hovering or focusing it opens a popover: the kind's icon, the source's label, its
origin (the web host, or the kind's label) and which one it is (the page's path, the record's id),
then what the source says - an excerpt with the passage marked, or a record's fields. Clicking opens
the source: `messageAnnotations.render` decides that for a host kind, and a host that returns
nothing leaves the web default, which opens the page.

## The rail section, and what its badge counts

The `Quellen` section lists every source of the conversation, a source cited three times being one
row with three marker numbers. Its badge counts the sources of the newest answer
(`latestAnswerSources`), not the whole list (#495).

## Prompt

Albert's `<cite>` section says how to point: the two link shapes, the link text, link only what a
tool of this run read, open a search result with `webFetch` first, the optional passage title, no
source list of its own. A host agent with its own prompt that should cite needs the same section.
