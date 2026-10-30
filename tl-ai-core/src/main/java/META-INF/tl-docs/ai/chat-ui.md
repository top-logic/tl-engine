---
description: Read before changing the chat of tl-chat or tl-chat-classic, or a route tl-ai-core serves content on - ChatServlet (/chat/*), ChatProcessor, ChatSSEHandler on the window's React event stream, the React chat (tl-chat/ui, <chat-conversation>, <chat-rail>, <chat-dock>), ReactViewComponent in classic layouts, ChatSessionManager / ChatHistoryService, approval, a ConversationBackend answering a conversation in place of the framework's agent, and the routes /html/{id}, /screenshot/{id} and /archived-page/{handle}.
order: 67
---

# The chat UI and served content

## The chat

`ChatServlet` (package `com.top_logic.ai.chat.api`) at `/chat/*` handles the chat's REST API. `ChatProcessor` runs the agent in the background for a send, on the pool of `ChatRunExecutor`, and builds the run's `ToolContext` for the chat user - tools run inside that user's sub-session. `ChatSSEHandler` implements `AgentEventListener` and pushes the run's events to the browser, with 50 ms flush batching. The chat has no event stream of its own: its frames travel on the window's React event stream (`/react-api/events`), attached with `POST /chat/stream`, so a page needs no second long-lived connection - several tabs with a chat each would otherwise exhaust the browser's six connections to the server.

`ChatSessionManager` keeps the per-session state (chat, `ConversationMemory`, `ConversationLog`) in an LRU cache, and `ChatHistoryService` persists the chat sessions with their per-user history in the knowledge base. `/chat/capabilities` lists the agents, models and MCP servers the account may use, and `/chat/send` and `/chat/regenerate` refuse with 403 what [run access](doc:ai/security/run-access) refuses.

**Approval.** A tool call that needs approval is decided by `SecurityContext.checkApproval` (`Allowed`, `Denied`, `NeedsApproval`); a `NeedsApproval` goes to the `ApprovalCallback`, which in the chat parks the call and asks the user, with session-level caching of an approval. `/chat/respond` answers a pending approval or `askUser` question. A `STRUCTURAL` tool is refused in production mode (`ExecutionMode`, `ToolPermissionFilter`). An application can decide approval from the call rather than from the tool's mode, see [approval from the call](doc:ai/security/administration#approval-decided-from-the-call).

### A conversation answered from outside

A conversation can be answered by a process outside of the agent loop - a coding agent in a container, a remote agent - while the chat keeps everything around the run: send lock and busy refusal, the user's message persisted first, the delivery to every window showing the conversation, stop, history and cost. Such a process is a `ConversationBackend` (package `com.top_logic.ai.chat.backend` in `tl-chat`); which conversations it answers is decided by the `ConversationBackendResolver`s of the `ConversationBackendService`, asked in their configured order before every run, the first that names a backend winning. `tl-chat` configures none; an application adds its own:

```xml
<config service-class="com.top_logic.ai.chat.backend.ConversationBackendService">
    <instance>
        <resolvers>
            <resolver class="my.app.WorkspaceBackendResolver"/>
        </resolvers>
    </instance>
</config>
```

A resolver decides from what the conversation is - typically `AgentSession#subject` - and never from the session's `metadata`, which the chat rewrites from its own state.

The subject is there before the first run asks: `<chat-conversation subject="…">` offers its channel to its chat while it is displayed, and `POST /chat/sessions`, which the browser sends before the first message, creates the session with what stands there. A conversation can also be started on the server, from a command - `chatStart(chatId, subject, text)` (`ChatStart`) creates the session about `subject` and sends `text` through the chat `chatId` of the window, as if typed there; written to that chat's `input` channel, the session shows the run while it is written.

- `send(turn, sink, cancel)` runs on the chat's worker thread and blocks until the run has ended. The backend reports its work as `AgentEvent`s into `sink`, from `RunStarted` to `RunCompleted`, and `RunRecording` records them as the conversation's execution with its transcript, tokens and cost, recorded under `agentName()`.
- **Events only on that thread.** The thread carries the run's frame - the user's sub-session, the bound `ConversationLogListener`, the processing state - and the recording takes its events one at a time in run order. A backend that reads its process on a thread of its own queues what it reads and drains the queue inside `send`; the sink refuses an event from another thread, one after `send` returned and every event after a refused one, and the run then ends in an error.
- **Stop** cancels `cancel` on the thread of the stop request (`CancellationToken#onCancel`); the backend ends its work, emits `RunCompleted` with `ABORTED` and returns what it has. A backend that throws, or returns without `RunCompleted`, ends the run in an error: recorded as `ERROR`, reported to the windows as `APPLICATION_ERROR`, and the chat is free for the next message.
- `features()` (`BackendFeatures`) states which functions beyond sending the backend supports - regenerate, editing, the agent / model / effort / MCP pickers, attachments, approvals. The browser receives them as `features` with `GET /chat/sessions/{id}` and with every `POST /chat/send` (`ConversationFeaturesDto`) and hides what is not supported (`ui/src/features.ts`: one object gates the transcript's regenerate and edit, the composer's pickers, the attachment button and the auto-approve switch; absent means everything). The server refuses an unsupported regenerate (`/chat/regenerate`), edit (`/chat/truncate`) and a message with files (`/chat/send`) with 403 and a sentence, like a policy refusal.
- **No agent policy for such a conversation.** `/chat/send` and `/chat/regenerate` do not ask the agent security policy about the account's agents, model or MCP choice there - no agent of this application runs, and an account that may run none can still talk to the backend. Ownership, the busy refusal, the send lock and the installation's room and budget are checked as for every chat.
- A lead turn for subagents' news is never started for such a conversation: only the framework's agent spawns subagents.
- **Context and cost.** The context gauge of such a conversation measures against `contextWindow()` of the backend - the window its process reported, zero while unknown, and then no gauge is shown -, never the window of the framework's agent. The cost a run reports in `RunCompleted` is shown after the run's duration line, live from the `run_completed` frame and after a reload from the recorded execution (`runs` of `GET /chat/sessions/{id}`), the same as for a run of the framework's agent. The duration line after a reload is measured from the recorded execution as well - from its start, which is the run's `RunStarted`, to its end - so the time the backend took to start its process before the run is left out, as it is live.

### The front end

One front end consumes that API: the React chat in `tl-chat/ui/` (TypeScript, Vite), mounted into the React view layer by the view elements `<chat-conversation>`, `<chat-rail>` and `<chat-dock>`. `tl-chat/CLAUDE.md` describes its toolchain and development loop. It is shown in two kinds of page:

- **React pages** (`/view/`), where the view elements stand in a `.view.xml`.
- **Classic LayoutServlet pages**, through `tl-chat-classic`: `ReactViewComponent` writes a React view into a classic tab, with the classic window as the React window. A host gets the chat tab by depending on `tl-chat-classic` and chooses the view it shows. The bridge works around gaps of the classic layout, each marked in the code; `tl-chat-classic/CLAUDE.md` lists them.

The answer text is model output and is rendered through an allow-list on both sides - [rendering the answer](doc:ai/security/output-guard-and-audit#rendering-the-answer) is a security boundary.

## Served content

`tl-ai-core` serves content on three routes, and they are deliberately not alike:

- **`/html/{id}`** (`GeneratedHtmlPageServlet`) answers **anonymously**. A page `publishPage` stored is meant to be handed around, so its unguessable 128-bit id is the capability, and the answer carries a sandbox CSP so that HTML a model wrote cannot act in the application's origin.
- **`/screenshot/{id}`** (`ScreenshotServlet`) requires a **login** and then asks `ScreenshotService.mayRead` - the owner, or an administrator. A screenshot is a picture of a page somebody drove a browser to on their own account; the id is defence in depth, not the rule. A foreign id and an unknown id both answer 404, so the route says nothing about which ids exist. `browserScreenshot` returns `ScreenshotService.viewUrl(...)` and tells the model to write it as a Markdown image, which both rendering paths keep; see [screenshots](#screenshots).
- **`/archived-page/{handle}`** (`ArchivedPageServlet`) follows the screenshot rule: it shows the copy of a web page an agent run read - login, then the reading account or an administrator, 404 for unknown and foreign handles alike - rendered through `Converters.markdownToHtmlString` under a CSP that loads nothing.

The model-level read policy for screenshots and cookie pools is described under [perceivable binaries](doc:ai/security/administration#the-shipped-read-policy-for-perceivable-binaries).

### Screenshots

`browserScreenshot` drives the session's headless Chromium, captures a PNG and stores it as a `tl.ai.screenshots:Screenshot`. `ScreenshotServlet`, mapped to `/screenshot/*` by the `web-fragment.xml` of `tl-ai-core`, answers `GET /screenshot/{id}` with the image:

- **`{id}` is the screenshot's own handle**, not its TID: 16 bytes from `SecureRandom`, base64url, assigned by `ScreenshotService.createScreenshot`. `ScreenshotService.viewUrl(screenshot)` builds the URL, absolute when the deployment declares `%HOST%`, root-relative otherwise (`ApplicationUrl`).
- The **content type** comes from `Screenshot#contentType`, but only within `image/*`; anything else is served as `application/octet-stream`, with `X-Content-Type-Options: nosniff`, so a stored non-image is never rendered as a document in the application's origin.
- **Caching** is `private, max-age=31536000, immutable`: the bytes never change for the life of an id, and `private` keeps the answer out of shared caches.

| Request | Answer |
|---|---|
| an anonymous session | 401 - a session an application gives a visitor is no login |
| no cookie at all | 302 to the start page (the cookie probe of `TopLogicServlet`, as on every route of that base); a request that comes back still without a session gets 401 |
| signed in, own screenshot (or an administrator) | 200 with the image |
| signed in, somebody else's screenshot, or an unknown id | 404 |

Neither refusal delivers the image or a login page in its place. A screenshot captured by an unattended run has no owner and is readable by administrators only. `TestScreenshotServlet` pins the answers, `TestScreenshotService` the rule. An installation that widens the model-level policy (another role on the `AiBinaryOwner` grants) widens `viewDocument`, not `/screenshot/{id}`, which keeps asking `ScreenshotService.mayRead`.

The tool result tells the model to write the image into its answer:

```
Saved screenshot "example.com-20260919-104500.png" (48213 bytes) from https://example.com/.
View it at: /screenshot/a7f3b2... - show it to the user by writing exactly this markdown image
into your answer: ![example.com-20260919-104500.png](/screenshot/a7f3b2...).
```

Both rendering paths keep it: the React chat parses with `marked` and filters with `DOMPurify` (`tl-chat/ui/src/markdown.tsx`; `.md img` in `styles.css` holds the picture to the column width), and the Java path renders with flexmark and filters with `HtmlSanitizer`, whose allow-list keeps `img` with an `http`, `https` or relative `src` and drops `javascript:` and `data:`.
