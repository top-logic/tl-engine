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
| `min-similarity` | The lowest cosine to the query that finds a chunk by meaning (default 0: off). Without it, a query that nothing means still gets the nearest, unrelated chunks. Model-dependent: measure related and unrelated texts of the application and set it between. |
| `max-candidates` | How deep a search goes when the reader may not read the best hits (2000). |
| `batch-size`, `revision-slice`, `build-step-millis` | Worker tuning: objects per batch (64), revisions per commit (1000), length of a build step (10 s). |

### Embedders

- `OpenAiCompatibleEmbedder` - `POST <base-url>/v1/embeddings`, one batch per request, retries on
  network errors, 429 and 5xx. vLLM, text-embeddings-inference, llama.cpp's server and Ollama serve this
  route. `send-dimensions="true"` asks the server to cut the vector (OpenAI API).
- `OnnxEmbedder` (module `tl-ai-index-onnx`, ONNX Runtime 1.30.0 MIT + DJL tokenizers 0.38.0
  Apache-2.0, about 73 MB of native libraries) - runs the model in the application's JVM on the CPU.
  It is the default the owner chose (2026-10-14): `snowflake-arctic-embed-m-v2.0` int8; the option for
  small servers is `multilingual-e5-small` int8. See below for the model files.
- Every embedder has `dimensions` (mandatory; fewer than the model's own cuts a Matryoshka-trained
  vector and normalizes it again), `query-prefix`, `passage-prefix` and `batch-size`. The prefixes per
  model: e5 `query: ` / `passage: `; arctic-embed v2 `query: ` / none.

A change of the model, the dimensions or the prefixes builds a new generation of the index (below);
until it is complete the old one serves with words only.

### The in-process model

```xml
<embedder class="com.top_logic.tl.ai.index.onnx.OnnxEmbedder"
	model-dir="%VAR_PATH%/models/snowflake-arctic-embed-m-v2.0"
	model-file="model_int8.onnx"
	pooling="CLS"
	dimensions="768"
	query-prefix="query: "
	threads="4"
/>
```

For `multilingual-e5-small`: `pooling="MEAN"`, `dimensions="384"`, `query-prefix="query: "`,
`passage-prefix="passage: "`. Further settings: `max-tokens` (512), `max-batch-tokens` (8192),
`rows-per-inference` (1, see below), `model` (the stored model name; the directory's name by default).

The model files are not in any jar. Fetch them from the official repositories into the model
directory and compare the checksums:

```bash
d=$VAR_PATH/models/snowflake-arctic-embed-m-v2.0; mkdir -p $d && cd $d
curl -L -o model_int8.onnx https://huggingface.co/Snowflake/snowflake-arctic-embed-m-v2.0/resolve/main/onnx/model_int8.onnx
curl -L -o tokenizer.json  https://huggingface.co/Snowflake/snowflake-arctic-embed-m-v2.0/resolve/main/tokenizer.json
# sha256 (2026-10-14): model_int8.onnx 03d923bb1850ebdccb068e2f3abd8aa43fe81c50d07d037ef103fe3d0fb78e3b
#                      tokenizer.json  f1cc44ad7faaeec47241864835473fd5403f2da94673f3f764a77ebcb0a803ec

d=$VAR_PATH/models/multilingual-e5-small; mkdir -p $d && cd $d
curl -L -o model_int8.onnx https://huggingface.co/intfloat/multilingual-e5-small/resolve/main/onnx/model_qint8_avx512_vnni.onnx
curl -L -o tokenizer.json  https://huggingface.co/intfloat/multilingual-e5-small/resolve/main/tokenizer.json
# sha256 (2026-10-14): model_int8.onnx dd476dd0c2514e9b9be83aeb3853fac0763e0bdf4a71645407587d77c48a2d88
#                      tokenizer.json  0b44a9d7b51c3c62626640cda0e2c2f70fdacdc25bbbd68038369d14ebdf4c39
```

Licences: arctic-embed-m-v2.0 Apache-2.0, multilingual-e5-small MIT.

Operation:

- **Offline.** The embedder sets `ai.djl.offline=true` before the tokenizer loads; without it DJL
  downloads a GPU build of itself when it finds a CUDA GPU and reports telemetry. The tokenizer's
  native library is extracted from the jar into `~/.djl.ai` (or `DJL_CACHE_DIR`), ONNX Runtime's into
  `java.io.tmpdir` (or `onnxruntime.native.path`) - both must be writable. Linux needs glibc 2.28.
  On a musl image (Alpine) the native libraries do not load. The application then starts, logs one
  error, and the index searches words only (see "Embedder not available" below).
- **CPU.** `threads` per inference, no thread spinning, on the index worker's low-priority thread. On a
  laptop CPU (4 threads, int8, passages of about 60 tokens): arctic-embed-m-v2.0 about 47 passages/s,
  e5-small about 140 (230 with `rows-per-inference="16"`); a first load of a million chunks wants a
  night or a GPU endpoint. A query takes about 8 ms (arctic) or 3 ms (e5).
- **One text per inference.** An int8 model quantizes its activations with a scale computed over the
  whole batch, so a text's vector depends on its batch-mates (arctic: cosine 0.976 to 0.985 to the
  vector alone; e5-small: 0.997). `rows-per-inference` is therefore 1 by default.
- **Memory.** 0.5 to 1.3 GB of native memory outside the Java heap.
- **Redeploy.** Restart the JVM: a native library stays loaded until the JVM ends.

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
  embeds nothing. While the new generation is built, the old one follows the commits with words only:
  its changed chunks get no vectors of the new model.
- **Embedder not available.** When the configured embedder cannot be created (model files missing,
  native library does not load), the index that exists serves and follows the commits with words only.
  No new generation is built and nothing is deleted. At the next start with the embedder, a new
  generation is built that embeds only the chunks written without a vector meanwhile.
- **Failing object.** When the text, scope or keywords of one object cannot be computed (a script
  fails), the object keeps its chunks, a warning names it, and the worker goes on with the others.
  An embedder that fails stops the worker for the whole batch; it tries again with a growing pause
  (up to 10 minutes) that commits do not shorten.
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

The real models are behind `-Dtl.ai.index.model.e2e=true` (`OnnxModelE2e`, skipped without it): 30
German sentences must find their English translation as nearest passage at least 28 times (both
models: 30 of 30), and a text's vector must not depend on its batch-mates.

```bash
mvn -pl tl-ai-index,tl-ai-index-onnx test -DskipTests=false -Dtest=TestOnnxEmbedder \
  -Dsurefire.failIfNoSpecifiedTests=false -Dtl.ai.index.model.e2e=true \
  -Dtl.ai.index.model.dir=$HOME/.cache/tl-ai-index-models/snowflake-arctic-embed-m-v2.0
```
