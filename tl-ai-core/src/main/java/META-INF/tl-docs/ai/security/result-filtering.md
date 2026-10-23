---
description: Read before changing what a tool result, the model digest or a search hit puts into the context window - layer L3: filtering inside the tool before serialization (ReadFilter), marking untrusted content (UntrustedEnvelope, AnswerMarking), the model digest bounded by accessibleTypes(READ) (ModelDigestGenerator, outlineModel), query-time filtering of the vector index (VectorReadFilter) and the rights inside the hybrid index (IndexSearch, readable-scopes, SearchRefusedException).
order: 40
---

# Result filtering

**Pre-filter, never redact afterwards.** Once a value is in the conversation it has been streamed to the browser, sent to the provider and written to the transcript.

## Tool results

Filtering happens **inside the tool, before serialization**, not in the dispatcher. That is the only place it can work: almost every tool returns an already-serialized JSON string built by a msgbuf writer, so by the time `ToolExecutions.execute` sees the result there is no object graph left to filter. A dispatcher-level `SearchExpression.filterSecurity` would inspect a `String` and pass it through, which is worse than no filter because it looks like one.

So the filter is the `ReadFilter` of [data authorization](doc:ai/security/data-authorization#the-readfilter-chokepoint), applied where the object still is an object, and the design leans on it being *one* filter. Its traversal has the shape of the engine's `filterSecurity` (it recurses through collections and maps, drops non-readable elements and returns the input unchanged when nothing had to go), so a reader who knows one knows the other. What it does not cover is an aggregate computed inside a script (`all(...).size()`).

### Marking untrusted content

**Filtering decides what the model sees; marking says what it is.** Read rights say *which* business data reaches the context, and nothing about *who wrote it*. What reaches it is, by volume, the application's largest untrusted input - the free text in a shared record, typed by an end user who is not the principal this run serves - so every such payload carries the `UntrustedEnvelope` tags, and the system prompt states once per run what a tagged block is: data to read and report, never an instruction (T3). Marking is per tool and never in the dispatcher, for the same reason filtering is.

Who applies it: `ReadFilter` marks everything it discloses, so a TL-Script tool's answer is marked by the executor that runs it; a `@ToolMethod` answer is marked by `AnnotatedTools` unless the method declares `marking = AnswerMarking.NONE` (the answer is the application's own sentence, or is itself an instruction the agent is meant to follow - a loaded skill, the user's own reply) or `AnswerMarking.TOOL` (the tool marks its own content, in the kind that content deserves - a fetched page, a stored document, an external MCP server's answer). The default marks, so a tool cannot silently stop saying what its payload is. A tool that implements `Tool` directly marks its own answer, as `ScriptToolExecutor` and `McpTool` do.

None of this is a control in the sense the other layers are: it is an instruction to a model, and the model is not a boundary. Measured against a small model on a payload aligned with the user's own request, an injected tool call went through in 19 of 24 runs with nothing marked and in 3 of 24 with the marking and the standing rule in place - a reduction of the blast radius, not immunity. `TestPromptInjectionBarrier` measures it ([testing](doc:ai/testing#opt-in-gates)).

## The model digest

The digest is the read surface that reaches the model **in one piece**: one `outlineModel` call hands over every module, type and attribute, so a type nobody filtered out is a type the agent knows the name, the shape and the relationships of (T11). It is therefore scoped by the check its data admits - what it describes is **types**, so the bound is `accessibleTypes(READ)`, not the read filter, which decides about objects.

`ModelDigestGenerator.generate(TLModel, boolean, ModelSecurity, Locale)` is the **only** form; `ModelBrowserTool.outlineModel` takes the run's `ToolExecutionContext` to supply it. A class outside `accessibleTypes(READ)` has no entry; a module whose classes are all inaccessible is absent with its enumerations; a principal who may read no type at all is told there is nothing, the same answer an application with no modules gives. The digest is per principal, so it is generated on every call and never cached globally. Boundaries:

- **An emitted type is written out in full.** Its generalizations and the target types of its parts are *named* even where those types have no entry of their own. Naming is not describing, and a definition with its references cut out would be a false statement about the type rather than a narrower one.
- **Enumerations are not scoped.** A `<security-config>` grants on classes and on parts; there is no grant about an enumeration. So enumerations follow their module, which is scoped.
- **`accessibleTypes` resolves roles at the security root.** An account holding a role only on single objects - a scope rather than a tier - does not get that type advertised, although it may read instances of it. The error is towards disclosing less, and the instance surfaces are filtered per object anyway.
- **Attribute-level restrictions are not reflected**: the digest advertises a type's parts as the model declares them. What an account may read of an instance is decided where the instance is read.

`SecDemoModelDigestTest` asserts it against the test bed's real accounts: a ticket reader is told about `pm:Ticket` and not about `pm:Project`, and the module nobody holds a grant in is absent.

## The vector index

The index of `tl-ai-vector-index` is populated by a background daemon under system rights, so its contents are unrestricted by construction. Filtering therefore happens **at query time**, in `VectorReadFilter`, and `VectorIndex.searchVectorIndex` has no form that skips it - a search names the principal it serves or does not happen:

1. Each chunk carries the source object's identifier: a `TopLogic-id` metadata entry, with the `objectId` field of the indexed payload as the fallback.
2. The identifier is resolved through `TidResolver.resolve`, which **is** the read check, so "no such object" and "not yours" are one answer here as everywhere else.
3. **Over-fetch, then filter**: the store is asked for `k * f` candidates and the readable hits are truncated to `k` afterwards (`VectorReadFilter.overfetch`), so an account with narrow access still gets a full page rather than the two survivors of the top ten. A fixed multiplier is a heuristic and not a guarantee, which is why the result says when the candidate pool ran out.
4. Chunks whose source cannot be resolved are **dropped**, not passed through.

`VectorIndexSearchTool` goes one step further: what it writes for a surviving hit is the live object through `ReadFilter` - its identity and the attributes the principal may read - and **not** the indexed chunk. The chunk is a serialization of every attribute taken at indexing time, so handing it over would disclose an attribute the principal may not read and a value that has since changed. The TL-Script function `vectorIndexSearch` returns the chunk, because its caller is the script author's own session and the object it hands back is read-checked by the engine on every attribute access; the per-object drop applies to it identically.

**What this does not claim.** The embedding vector was computed from unfiltered text. Dropping a hit keeps the text out of the answer; it does not undo that the vector exists, and a caller who can observe scores can still learn that *something* similar is in the index. Not indexing unreadable content in the first place would be the stronger guarantee, at the price of an index per rights profile - multiplying embedding cost and storage and turning a rights change into a re-indexing job.

`SecDemoVectorIndexTest` asserts it against the test bed's real accounts; the store round trip is behind `-Dtl.ai.vector.e2e=true` in `VectorIndexIntegrationTest`, because it needs an embedding key.

## The hybrid index

`tl-ai-index` keeps its own Lucene index of chosen types: words and meaning per chunk of an object's text. Like the vector index it is written by a worker in a system interaction, so its contents are unrestricted; unlike it, the rights are applied **inside** the query and then once more per hit, in `IndexSearch`:

1. **No reader, no search.** A call in a system context, or with no user, throws `SearchRefusedException`. The engine answers every read check with "allowed" there, so a search would disclose every indexed text.
2. **Scope in the query.** Each chunk carries the ids of its scope objects (the type's `scope` script, e.g. the project). At query time the application's `readable-scopes` script runs in the reader's own interaction, and its result is filtered by the run's `ModelSecurity`; the remaining ids filter both the word and the vector search. A reader of one project of a thousand gets a full page from that project instead of the survivors of a global top-k. A scope id does not change when a membership changes, so the index holds no rights, only ids the rights are asked about.
3. **Per hit, in rank order:** `TidResolver.resolve` (the object read check) and `canRead(object, attribute)` for **every attribute the chunk's text comes from** - an attribute text part names its attribute, and a script text part must declare what it `reads`, or it is shown to nobody. A hit that fails is dropped with its text. A surviving hit shows its chunk: the chunk is the text of named attributes, all just checked, not a dump of the object. A search that filters by a keyword also checks `canRead` for every attribute the keyword `reads`, and a keyword must declare them - otherwise a filter would tell a reader the value of an attribute the reader may not read, for example `salaryBand=HIGH` over readable objects.
4. **Deeper, not wider.** An unfilled page searches again with twice the depth, up to `max-candidates` chunks and as many checks; the answer says whether more is available, never how many hits were dropped.
5. `similar` starts only from the chunks of the start object the reader may read, so a hidden attribute cannot steer what is found.

What the vector index section says about the vector holds here too. The stored chunk text is on disk in the index directory, which needs the protection of the database files. `TestIndexSearchRights` and `TestBoundedSearch` in `tl-ai-index` assert it.
