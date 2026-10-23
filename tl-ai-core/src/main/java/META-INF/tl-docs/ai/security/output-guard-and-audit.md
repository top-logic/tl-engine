---
description: Read before changing how an agent's answer is rendered or what is logged about denials - layer L5: why free answer text is not scanned, the citation check of links (CitationValidator), the HTML allow-list (Converters.markdownToHtmlString, HtmlSanitizer, DOMPurify in tl-chat/ui/src/markdown.tsx); layer L6: the WARN / ERROR log records of ToolAuthorization and ModelSecurityPreCommitGuard.
order: 60
---

# Output guard and audit

## What the output guard does not do

The output guard is scoped narrowly and deliberately. It does **not** scan free answer text for "secrets": the data has already been streamed by the time a full answer exists (`ChatSSEHandler` pushes `text_delta` frames live), so a scan afterwards protects nothing and creates a false sense of assurance. If a value must not appear in an answer, it must not have reached the context - that is L2 and L3.

The residual risk is explicit: an agent that legitimately read an object (L2 passed) can paraphrase it in prose, and prose is not checked. That is inherent to the product.

## Links in the answer

What the guard does check deterministically are structured references. A link the answer cites - `[Summer party](tl:GenericObject:11047)` or a web address - is kept as a footnote only when it points at an entry of the run's reading list (`CitationValidator`), and the reading list holds an object only where `ReadFilter` disclosed its identity to this run. A link to anything the run did not read is not turned into a citation. See [citations](doc:ai/memory-and-citations#citations).

## Rendering the answer

The answer is rendered as Markdown, which is where the *other* direction of the output guard sits: not what the agent may say, but what a browser may do with what it said. The text is model output, and a model writes down what `webFetch` and the tool results put in front of it, so a page carrying `<script>`, an `<img onerror>`, a `javascript:` link or an `<iframe>` reaches the renderer as soon as the model quotes it. The text is also persisted with the conversation and rendered again on every reopen, so anything active in it is stored, not reflected.

Both front ends filter with an allow-list, and neither trusts the other:

- **Server side** - `Converters.markdownToHtmlString` is the single boundary where HTML is made from model text, and every consumer goes through it: the `contentHtml` the chat API hands out and the TL-Script function `convert.markdownToHtml`. Two layers act there. The renderer escapes raw HTML in the Markdown (`HtmlRenderer.ESCAPE_HTML`), so a quoted `<script>` becomes text. `HtmlSanitizer` then filters what the renderer itself produced - the layer escaping cannot replace: `[text](javascript:...)` is Markdown, not HTML, and the renderer writes that target into an `href` of its own accord.
- **React side** - `tl-chat/ui/src/markdown.tsx` parses with `marked` and filters with `DOMPurify` before the result reaches `dangerouslySetInnerHTML`, widened only by the copy button and the data attributes that the file's own code-block and citation markup carry.

The allow-list is the one a Markdown answer needs and nothing beyond it: headings, lists, tables with their column alignment, code with its language marker, block quotes, `<del>`, `<ins>`, `<sup>`, `<hr>`, and links and images whose scheme is `http`, `https` or `mailto`. A URL in any other scheme - `javascript:`, `vbscript:`, `data:`, `file:`, and the same in mixed case, which a browser matches and a naive check does not - loses its attribute; the text around it stays, so the reader sees what the model wrote without being able to act on it. A relative URL is kept as written, because `publishPage` hands the model a root-relative link when the deployment declares no `%HOST%`.

**Rules for code:** make HTML from model text only through `Converters.markdownToHtmlString`; never write model text into a page with `writeContent`, `innerHTML` or `dangerouslySetInnerHTML` around the filters; do not widen either allow-list without a test in `TestHtmlSanitizer` / `markdown.test.ts` saying why.

What this is not: a defence against the agent *choosing* to write a link to a host it may reach. An `https://` link to any site is allowed, as it must be for an agent that cites its sources. The guard is against markup becoming behaviour, not against the destination of a link the reader clicks knowingly.

## Audit

Denials and rollbacks are logged to the application log, with run, turn, account, tool and reason, and **never with a withheld value** - type names, operations, group names and role names only:

| Occasion | Record |
|---|---|
| A tool or capability refused by the tool policy (L1) | `WARN` under `com.top_logic.tl.ai.core.security.ToolAuthorization`, naming the tool, its group, the account, the roles it holds and the deciding rule - see [diagnosing a denial](doc:ai/security/tool-access-recipes#why-can-this-account-not-use-this-tool) |
| A `ToolSecurityException` raised by a tool (L2) | a denied tool result and a log record naming operation and type |
| A pre-commit rollback (L4) | one `ERROR` per violation by `ModelSecurityPreCommitGuard`, in the field shape of a `ToolAuthorization` denial |
| A refused commit | one `ERROR` line by `AgentLoop.commitTransaction` naming run, turn and cause |
| A refused browsing origin | `INFO` - see [browsing governance](doc:ai/security/browsing-governance#what-the-operator-sees) |
| A skill or delegate not offered to the user | `INFO` with the deciding reason ([capability notices](doc:ai/security/capability-notices)) |
| A run that cannot name a principal | refused where its security context is built ([identity](doc:ai/security/identity)) |

Denials are logged at `WARN` and rollback violations at `ERROR`; a denial that is a configured state rather than an operator's problem - an account without a read right on a declared type, a browsing refusal - is logged at `INFO`.
