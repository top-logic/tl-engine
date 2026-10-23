---
description: Read before deciding what an application indexes for its agents - why semantic search complements the structured tools, the three levels it works on (platform, model, data), and which module provides it (tl-ai-vector-index with searchVectorIndex, tl-ai-index with index.search / index.similar and aiIndexSearch).
order: 30
---

# Semantic memory

The tools give the agent access to the application. Access alone is not enough - the agent needs understanding as well. When a user asks "how do I set up an approval step?", the agent has to know not only which tools it has, but how the platform works, how the concrete data model is built and which business relationships exist.

That understanding comes from a semantic index: a knowledge base that is searched by meaning rather than by keyword. The agent finds relevant information even when the wording does not match - "delivery" also finds "shipment", "freight order" and "goods issue". Two modules provide it:

- **`tl-ai-vector-index`** - embeddings of the indexed objects in a vector store (Qdrant or JVector), filled by a background daemon; the tool `searchVectorIndex` and the TL-Script functions with the prefix `vectorIndex`.
- **`tl-ai-index`** - hybrid search in a Lucene index of its own: words (BM25, German and English analysis) and meaning (vectors) per chunk of an object's text, fused by Reciprocal Rank Fusion, so a German question finds an English text. The tools `index.search` and `index.similar`, the TL-Script functions `aiIndexSearch` and `aiIndexSimilar`; the embedder runs remotely (an OpenAI-compatible endpoint) or in process (`tl-ai-index-onnx`). `tl-ai-index/README.md` describes the configuration of the indexed types.

Both apply the reader's rights to every hit - an index is written under system rights, so its contents are unrestricted, and what a search returns is decided per object at query time. [Result filtering](doc:ai/security/result-filtering#the-vector-index) describes the checks and what they do not claim.

The index works on three levels.

## Understanding the platform

The training data of a language model holds hardly any examples of how one specific platform is configured. Platform documentation, best practices, configuration examples and help texts an application indexes let the agent find the relevant documentation before it calls a tool at all.

*Example: "How do I configure a table with grouped rows?" - the index finds the documentation of the table component and its grouping options, even when the user does not know the name of the configuration option.*

This closes the gap in the model's prior knowledge on purpose: what is not in the training data is in the index.

## Understanding the model

The tools deliver structured information about the data model: types, properties, references. An index over model descriptions, labels and annotations adds semantic search on top: "I need something to do with invoicing" finds types such as "Invoice", "Invoice item", "Billing" and "Payment receipt" - even when the search term occurs nowhere literally.

On a model-based platform the indexed text is enriched with structural knowledge - type membership, references, the meaning of properties. A customer record is not just a block of text but an object with a known type, known references and a known role in the business process. The model *is* the context that plain text search would have to reconstruct laboriously.

## Understanding the data

The most effective level concerns the concrete business data of an application. When the index covers the application's contents, not only its model, the agent answers questions that go beyond structured queries:

- *Similarity search*: "find customers with a profile similar to Müller Ltd." - based on the whole of the customer data, not on single fields (`index.similar`).
- *Document search*: many business applications manage documents - contracts, minutes, specifications. An index over their content makes them searchable: "do our contracts contain clauses limiting liability?"
- *Pattern recognition*: "which projects had problems similar to project X?" - the index finds similarities in free-text fields, comments and descriptions that a structured query would not cover.

## Interplay with the tools

The index does not replace the tools, it complements them. The tools give precise, structured answers: "the type Customer has the properties name, address, revenue." The index gives context and background: "customers with high revenue are listed separately in the quarterly report, see the documentation of key account management." Together they give an agent that not only has access to the application but understands it - the platform, the model and the data.
