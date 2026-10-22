---
description: Read before changing the chat of tl-chat or tl-chat-classic, or a route tl-ai-core serves content on - ChatServlet (/chat/*), ChatProcessor, ChatSSEHandler on the window's React event stream, the React chat (tl-chat/ui, <chat-conversation>, <chat-rail>, <chat-dock>), ReactViewComponent in classic layouts, ChatSessionManager / ChatHistoryService, approval, and the routes /html/{id}, /screenshot/{id} and /archived-page/{handle}.
order: 67
---

# The chat UI and served content

## The chat

`ChatServlet` (package `com.top_logic.ai.chat.api`) at `/chat/*` handles the chat's REST API. `ChatProcessor` runs the agent in the background for a send, on the pool of `ChatRunExecutor`, and builds the run's `ToolContext` for the chat user - tools run inside that user's sub-session. `ChatSSEHandler` implements `AgentEventListener` and pushes the run's events to the browser, with 50 ms flush batching. The chat has no event stream of its own: its frames travel on the window's React event stream (`/react-api/events`), attached with `POST /chat/stream`, so a page needs no second long-lived connection - several tabs with a chat each would otherwise exhaust the browser's six connections to the server.

`ChatSessionManager` keeps the per-session state (chat, `ConversationMemory`, `ConversationLog`) in an LRU cache, and `ChatHistoryService` persists the chat sessions with their per-user history in the knowledge base. `/chat/capabilities` lists the agents, models and MCP servers the account may use, and `/chat/send` and `/chat/regenerate` refuse with 403 what [run access](doc:ai/security/run-access) refuses.

**Approval.** A tool call that needs approval is decided by `SecurityContext.checkApproval` (`Allowed`, `Denied`, `NeedsApproval`); a `NeedsApproval` goes to the `ApprovalCallback`, which in the chat parks the call and asks the user, with session-level caching of an approval. `/chat/respond` answers a pending approval or `askUser` question. A `STRUCTURAL` tool is refused in production mode (`ExecutionMode`, `ToolPermissionFilter`). An application can decide approval from the call rather than from the tool's mode, see [approval from the call](doc:ai/security/administration#approval-decided-from-the-call).

### The front end

One front end consumes that API: the React chat in `tl-chat/ui/` (TypeScript, Vite), mounted into the React view layer by the view elements `<chat-conversation>`, `<chat-rail>` and `<chat-dock>`. `tl-chat/CLAUDE.md` describes its toolchain and development loop. It is shown in two kinds of page:

- **React pages** (`/view/`), where the view elements stand in a `.view.xml`.
- **Classic LayoutServlet pages**, through `tl-chat-classic`: `ReactViewComponent` writes a React view into a classic tab, with the classic window as the React window. A host gets the chat tab by depending on `tl-chat-classic` and chooses the view it shows. The bridge works around gaps of the classic layout, each marked in the code; `tl-chat-classic/CLAUDE.md` lists them.

The answer text is model output and is rendered through an allow-list on both sides - [rendering the answer](doc:ai/security/output-guard-and-audit#rendering-the-answer) is a security boundary.

## Served content

`tl-ai-core` serves content on three routes, and they are deliberately not alike:

- **`/html/{id}`** (`GeneratedHtmlPageServlet`) answers **anonymously**. A page `publishPage` stored is meant to be handed around, so its unguessable 128-bit id is the capability, and the answer carries a sandbox CSP so that HTML a model wrote cannot act in the application's origin.
- **`/screenshot/{id}`** (`ScreenshotServlet`) requires a **login** and then asks `ScreenshotService.mayRead` - the owner, or an administrator. A screenshot is a picture of a page somebody drove a browser to on their own account; the id is defence in depth, not the rule. A foreign id and an unknown id both answer 404, so the route says nothing about which ids exist. `browserScreenshot` returns `ScreenshotService.viewUrl(...)` and tells the model to write it as a Markdown image, which both rendering paths keep. `tl-ai-core/doc/screenshots.md` has the details.
- **`/archived-page/{handle}`** (`ArchivedPageServlet`) follows the screenshot rule: it shows the copy of a web page an agent run read - login, then the reading account or an administrator, 404 for unknown and foreign handles alike - rendered through `Converters.markdownToHtmlString` under a CSP that loads nothing.

The model-level read policy for screenshots and cookie pools is described under [perceivable binaries](doc:ai/security/administration#the-shipped-read-policy-for-perceivable-binaries).
