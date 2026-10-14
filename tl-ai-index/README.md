# tl-ai-index - hybrid search by words and meaning

Finds an application's objects by what their text says: by its words (BM25, German and English
analysis) and by its meaning (vectors of an embedding model) in one query, fused by Reciprocal Rank
Fusion. A German question finds an English text. Every search runs under the reader's rights, filtered
inside the index, so a reader of one project of a thousand gets a full page from that project.

The index is derived data in a Lucene directory of its own (Lucene 9.12.3, the engine's version). It
does not touch the engine's word index (`DefaultIndexingService`) and does not depend on
`tl-ai-vector-index`.

## Switching it on

Depend on `tl-ai-index`, switch the module on and configure the types:

```xml
<config service-class="com.top_logic.basic.module.ModuleSystem">
	<instance>
		<modules>
			<module key="com.top_logic.tl.ai.index.AiIndexService$Module" value="true"/>
		</modules>
	</instance>
</config>

<config service-class="com.top_logic.tl.ai.index.AiIndexService">
	<instance class="com.top_logic.tl.ai.index.AiIndexService"
		readable-scopes="all(`my:Project`)"
	>
		<embedder class="com.top_logic.tl.ai.index.embed.OpenAiCompatibleEmbedder"
			base-url="https://embed.example.eu"
			model="Snowflake/snowflake-arctic-embed-m-v2.0"
			api-key="%EMBED_API_KEY%"
			dimensions="768"
			query-prefix="query: "
		/>
		<chunking max-chars="1200" overlap-sentences="1" page-marker="^\[p\.(\d+)\]$"/>
		<types>
			<type name="my:Statement" scope="s -> $s.get(`my:Statement#project`)">
				<text attribute="text"/>
				<keywords>
					<keyword name="kind" script="s -> $s.get(`my:Statement#kind`)"/>
				</keywords>
			</type>
			<type name="my:Document" scope="d -> $d.get(`my:Document#project`)">
				<text attribute="title"/>
				<text attribute="extractedText"/>
			</type>
		</types>
	</instance>
</config>
```

Then give an agent the tools `index.search` and `index.similar`.

| Setting | Meaning |
|---|---|
| `location` | Directory of the index, default `%VAR_PATH%/tl-ai-index`. Holds the text of every indexed object: protect it like the database files. |
| `embedder` | How texts become vectors. None: the index searches words only. |
| `chunking` | `max-chars` (default 1200), `overlap-sentences` (1), `page-marker` (a regex for a line that starts a page, the page number as group 1), `max-text-chars` (2,000,000 per text). |
| `types/type` | `name` (qualified class; applies to sub classes without their own entry), `<text attribute=".."/>` or `<text name=".." script=".." reads="a,b"/>`, `scope` (script: object -> scope objects), `<keywords>`, `label-prefix` (default true: the object's label goes in front of each chunk when it is embedded). |
| `readable-scopes` | Script without arguments: every scope object a reader may search in. Run as the reader, filtered by the reader's rights. Without it, scoped types are filtered by type only. |
| `max-candidates` | How deep a search goes when the reader may not read the best hits (2000). |
| `batch-size`, `revision-slice`, `build-step-millis` | Worker tuning: objects per batch (64), revisions per commit (1000), length of a build step (10 s). |

### Embedders

- `OpenAiCompatibleEmbedder` - `POST <base-url>/v1/embeddings`, one batch per request, retries on
  network errors, 429 and 5xx. vLLM, text-embeddings-inference, llama.cpp's server and Ollama serve this
  route. `send-dimensions="true"` asks the server to cut the vector (OpenAI API).
- Every embedder has `dimensions` (mandatory; fewer than the model's own cuts a Matryoshka-trained
  vector and normalizes it again), `query-prefix`, `passage-prefix` and `batch-size`. The prefixes per
  model: e5 `query: ` / `passage: `; arctic-embed v2 `query: ` / none.

A change of the model, the dimensions or the prefixes builds a new generation of the index (below);
until it is complete the old one serves with words only.

## What it does

### Sync: the history is the queue

A commit only wakes the index worker. The worker reads every change after its watermark from the
database history (`ChangeSetReader`): rows of the indexed types' tables and of their link tables, mapped
to the owning object. It re-indexes the changed objects in batches - one embedder call per batch - and
commits the new watermark in the same Lucene commit as their chunks. A crash, a restart and a long
downtime are the same loop as normal work. The worker runs in a system interaction with low thread
priority.

- **Generations.** Types, chunking and embedder have a fingerprint. A configuration with another
  fingerprint builds a new generation beside the serving one (`<location>/gen-<n>`), from a key file of
  all objects, in batches, resumable after a stop; it then catches up with the history and replaces the
  old one. Vectors of texts that did not change are reused, so a change of a keyword or of the analysis
  embeds nothing.
- **History compaction.** A watermark the history no longer reaches builds a new generation.
- **Freshness.** A search does not see the commits of the last seconds, nor the running transaction.
  The answers say how many commits the index is behind; the Java API can wait for a revision
  (`SearchRequest.awaiting`).
- **Cluster.** Each node keeps its own index and reads the shared history itself; nothing is locked or
  shared. Each node embeds each chunk once.
- **Not followed:** text of another object (only the object's own attributes and the declared script
  parts are indexed), unversioned types after the engine's `HistoryCleanup` removed their old rows.

### Search

`index.search(query, types?, scopes?, filters?, mode?, limit?)` and `index.similar(id, ...)`:

1. Refused in a system context, where the engine allows every read.
2. The reader's scopes (`readable-scopes`, filtered by the reader's rights) filter both the word and the
   vector search inside Lucene, with the type and keyword filters.
3. Per hit, best first: the object must be readable, and so must every attribute the matched chunk's
   text comes from. A failing hit is dropped with its text.
4. A page that is not full searches deeper, up to `max-candidates`.
5. Each hit is registered on the run's reading list with the text shown, so the answer can cite and
   quote it.

The word side asks a German and an English field in one disjunction; German compounds are not split
(no compound data with a permissive licence exists), the meaning side covers many of them. The vector
side asks the HNSW graph (7-bit quantized) for four times the depth and orders the candidates by the
exact dot product of the raw vectors.

### TL-Script and Java

- `aiIndexSearch(query, {"types": [...], "scopes": [...], "filters": {...}, "mode": "hybrid", "limit": 10})`
  and `aiIndexSimilar(object, {...})` return objects, best first, under the session user's rights.
  `aiIndexStatus()` tells where the index stands.
- `AiIndexService.getInstance().index().search(SearchRequest, ModelSecurity)` and `.similar(...)`
  return `Hits` with the chunk text, its locator (attribute, offsets, page) and the ranks.

## Operations

- Java 20 to 23 with `--add-modules jdk.incubator.vector` lets Lucene use SIMD vector code; Java 17
  works, slower.
- About 1 GB of page cache per million 768-dimensional chunks for the vectors; the stored text and the
  word fields need about as much disk again.

## Tests

`mvn -pl tl-ai-index test -DskipTests=false` - recall on planted vectors, the hybrid query, the HTTP
embedder against a fake server, the chunker, the sync against an H2 knowledge base (create, update,
link change, delete, catch-up after a stop and after a crash, a new generation without embedding, a
bounded build), the rights and the bounded search (the same work for 200 and 2,000 unreadable objects),
and the tools in a scripted agent run.
