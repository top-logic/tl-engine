# tl-chat

Real-time agentic chat interface for TopLogic applications. Connects users to the multi-agent orchestrator (from `tl-ai-core`) and displays tool calls, agent handoffs, and AI responses as they happen.

**Parent:** `tl-parent-core` | **Dependencies:** `tl-core`, `tl-ai-core`

## Architecture

### Commands and push

The browser component reaches the server only through the chat controls of its window - their state, their React commands, their data route and their upload - and receives the frames of its conversation on the window's event stream. The module maps no servlet of its own: a request to `/chat/*` is answered 404.

1. **The run's gestures** are React commands of the chat controls (`ChatControl`), so they travel the window's FIFO command channel like every other command. Every chat control answers them - the store sends through whichever of its elements joined last - and each names its arguments, reply fields and status values with constants (`CMD_*`, `ARG_*`, `FIELD_*`, `STATUS_*`; the browser's copies in `api.ts` say "Must match ..."). What reaches a run of the account rather than of the window lives in `AccountRuns` (see "a pending interaction belongs to the account" below).
   - **`respond`** (`CMD_RESPOND`): `requestId`, `response` (`APPROVED`, `APPROVED_FOR_SESSION`, `REJECTED` or the answer), `toolName`, `comment`, `ownWords`; answered `status` `ok`, or `ignored` when no run of the account waits for the request - which a request of another account is answered with too. The approval and question cards (`prompts.tsx`) and the store's auto-approve paths send it.
   - **`cancel`** (`CMD_CANCEL`): `sessionId`; answered `cancelled`. Found among the account's runs (#465, below).
   - **`setApprovalMode`** (`CMD_SET_APPROVAL_MODE`): `mode`, `sessionId` (absent for a conversation that has no chat yet); answered `status` `ok`, `forbidden` for a chat the account may read but not change, `notFound` for a chat it may not read, `error` for an unknown mode. The browser puts the switch back on anything else.
   - **`rate`** (`CMD_RATE`): `sessionId`, `turnIndex`, `rating` (`up`, `down`, empty to withdraw), `note`; stored in the rated chat's metadata without switching the conversation to it; answered `status` `ok` / `notFound`. The browser takes an optimistic rating back on anything else.
   - **`messageSubagent`** (`CMD_MESSAGE_SUBAGENT`): `subagentId`, `sessionId`, `parentToolCallId`, `text`; answered `status` `sent` / `ended` / `unknown` / `empty` (`AccountRuns.SubagentMessageStatus`) and `messageId`, which the card shows as sent until the read receipt names it. **`cancelSubagent`** (`CMD_CANCEL_SUBAGENT`): the same without the text, answered `cancelled`.
   - **`improvePrompt`** (`CMD_IMPROVE_PROMPT`): `draft`, `requestId`. An improvement is a call to a model and takes seconds, so the command must not hold the window's command channel: `PromptImprovementProcessor.start` checks the request, registers it on the tracker and hands the run to a chat worker (`ChatRunExecutor`), in the window's sub-session, and the command is answered `status` `started` at once (`error` for a request or configuration that cannot run, `rejected` with `reason` when there is no room). The result arrives as a **`prompt_improved`** frame (`ChatSSEHandler.PROMPT_IMPROVED`, `PromptImprovedEvent`: `requestId`, `improvedText`, `status` `completed` / `cancelled` / `stale` / `error` / `forbidden`, `reason`) on the stream the conversation has when the run ends (`ChatViewBinding.sendPromptImproved`). The store keeps a promise per request id (`ChatActions.improvePrompt`) and settles it with the frame; **`cancelPromptImprovement`** (`CMD_CANCEL_PROMPT_IMPROVEMENT`, `requestId`, answered `cancelled`) settles it as `cancelled` at once.
   - **Recording.** These commands are user intent and stay in the headless interface like `send` and `selectSession`. The script recorder captures a `Map`-argument command by its name only (`ReactControl.recordCommand` binds no argument type for it), and their arguments are session-bound ids - a request id, a chat id, a subagent id - that a replay in a later session could not resolve anyway; a replay of one of them reaches nothing and is answered like a request for something that does not exist.
   - `TestChatCommandOwnership`, `TestChatCancelOwnership`, `TestChatPendingInteraction`, `TestApprovalModeSwitch`, `TestSubagentMessageCommands` and `TestChatConversationRouting` drive these commands through `ChatCommandDriver` (test sources).

2. **Sending** is a set of React commands of the chat controls (`ChatControl`) as well, so it travels the window's FIFO command channel like every other command:
   - **`send`** (`ChatControl.CMD_SEND`): arguments `text`, `sessionId`, `title`, and the turn settings `agent`, `model`, `mcpOverride`, `mcpServers`, `effort`, `approvalMode` (`ARG_*`). It checks that the account may write the chat (see "Access to a chat" below), switches the conversation's tracker to it, applies the approval mode the switch shows (`ApprovalChoice.applyToChat`, `ApprovalChoice.save`), asks `SendRefusal` (or, for a chat a `ConversationBackend` answers, what the backend supports) and starts the run (`ChatProcessor.sendMessage`). The answer is `status` - one of `processing`, `busy`, `forbidden` (the policy or the backend refuses, or the account may read the chat but not write it; `reason` is the sentence the composer shows), `rejected` (no room for the run right now, `reason`), `notFound` (a chat the account may not read answered exactly like one that does not exist, an agent no agent is called), `error` (`STATUS_*`) - and for a message that went `sessionId` and `features` (`ConversationFeaturesDto`). The browser shows `forbidden` as a sticky notice, `rejected` as one that goes by itself (`ForbiddenError` / `RefusedError` in `api.ts`), `busy` as the busy hint, everything else as "could not be sent".
   - **A send without `sessionId` creates its chat**, titled `title` (the browser's `autoTitle` of the message), with the approval mode the switch shows and about the object the page offers as its `subject` (`ChatViewBinding.subject()`, see "`subject`" below), and answers with its id: one command where a create and an open used to be two. What refuses a first message without its chat - the agent policy or the backend's features, the room for the run - is asked before the chat is created (`ChatControl.firstSendRefusal`); a chat the send created and could not send into afterwards is deleted again in the same command (as `ChatStart` does), so a refused first message leaves no empty row in the list. A chat whose subject the send created (`subject-factory`, see "`subject`" below) is kept instead, and the answer names it.
   - **What the page states about the situation** (`<chat-conversation><context>` and `<selection/>`) is evaluated on the server when the message is sent: the displayed conversation offers its facts to its chat (`ChatViewBinding.offerFacts`), and `send` and `regenerate` read them there (`ChatViewBinding.facts()`). The browser carries none of it; the harness renders the stable facts into the run's system prompt and the message text stays untouched.
   - **`editSend`** (`CMD_EDIT_SEND`, plus `fromTurn`): drops the turn and everything after it and sends the edited message, in **one** command, so no other command of the window can come between the two. The busy check, the backend's `editMessages`, the send's refusal checks and the installation's room are all asked before anything is dropped, so a refused edit removes nothing; the browser puts the transcript back as it was. Answered like `send`, plus `removedTurns`. An edited message carries no files.
   - **`regenerate`** (`CMD_REGENERATE`): busy check, refusal (`SendRefusal` / the backend's `regenerate`), `ChatProcessor.noRoomReason`, then the last turn is dropped and its message sent again; answered with `status`, `reason`, `removedTurns` and the message sent again as `text`, which the browser puts back as the user bubble.
   - **Attachments** are uploads to a chat control (`ChatControl` is an `UploadHandler`; the bridge's `useTLUpload()` posts to `/react-api/upload`, where the engine's size limit and upload guard apply). The files belong to the chat session they were attached in, not to the place that shows it (#30552): they wait on the server in one `ChatAttachments` per chat session of the window's conversation (`ChatViewBinding.attachments(sessionId)`), the chat the next send creates having its own under `ChatViewBinding.NEW_CONVERSATION` (`""`). Switching the place to another chat therefore shows that chat's files, and switching back shows the first chat's again. Every displayed chat control of the conversation states them as `draftAttachments`, a map from session id (`""` for the chat not yet created) to the list of `DraftAttachmentDto` (id, name, media type, size) attached in it - a session without files has no entry -, and the composer shows the entry of the session on screen. The browser names the chat with every gesture - the form part `sessionId` of the upload (`ChatControl.UPLOAD_SESSION_PART`), `sessionId` of `removeAttachment` and of `send` -, because the server has no notion of which chat a place shows: "new chat" happens in the browser alone and leaves the tracker's active session where it was, so the key comes from the request, as `send` always took it. An upload or a removal in an existing chat needs the right to write it (`writeRefusal`, like `send`); in the chat not yet created it is always allowed. A type other than an image or PDF, a file over 10 MB and a sixth file are refused with a sentence (`I18NConstants.ERROR_*`), answered as `reason` of the upload; the browser checks the same first, so the user is told without a round trip. `removeAttachment` (`attachmentId`, `sessionId`) takes one off; `send` carries the files of its chat that are there when it starts and takes exactly those off once the message went - a refused send keeps them, and a first send that is refused (the chat it created is deleted again) leaves them with the chat not yet created. The files of a deleted chat are dropped in every window (`ChatWindowService.onSessionDeleted`). The browser's `send` waits for uploads still on their way.
   - A command's answer is what tells the window's event stream that frames are due (`expectEvents` in the bridge), so a send needs nothing of its own behind a buffering proxy. A send's command that is never answered because the bridge reloads a stale page leaves its text in `sessionStorage` (`keepUnsent`) for the reloaded composer, as before (#574).
   - `TestChatCommandOwnership`, `TestChatRunAccess`, `TestChatBackendFeatures`, `TestApprovalModeSwitch` and `TestChatDraftCommands` drive these commands through `ChatCommandDriver` (test sources).

3. **Push** (real-time frames, on the window's event stream): `ChatSSEHandler` receives `AgentEvent`s and emits structured frames: `connected`, `run_state`, `run_started`, `turn_started`, `text_delta`, `tool_started`, `tool_completed`, `approval_required`, `approval_resolved`, `interaction_resolved`, `message_complete`, `heartbeat`, etc.
   - **Attach** is the technical React command `attachStream` (`ChatControl.CMD_ATTACH_STREAM`, argument `stream` = the token, answered with `attached`), which every chat control answers - `ChatConversationControl`, `ChatDockControl`, `ChatSectionControl` all extend `ChatControl`. The store sends it through the command function of whichever of its elements joined last (`useChat` hands the element's `useTLCommand()` to `ChatStore.subscribe`), so it travels the window's FIFO command channel like every other command, and its answer is what tells the window's stream that the first frames are due (`expectEvents`). The frames travel on the event stream the browser window already holds for the React layer (`/react-api/events`), as one `PatchEvent` per 50 ms flush to the listener id `tl-chat-stream:<token>`, which the browser subscribes with the bridge's `subscribe` before it asks (`chat-stream.ts`). The token is new for every attach, so the frames of a replaced handler reach nobody. The handler registers that id with the window's queue (`SSEUpdateQueue.registerListener`): the first frames are written inside the command's interaction, and an interaction drops a patch to an id that names no displayed control.
   - **Detach** is `detachStream` with the same token, sent when the last chat element of the conversation leaves a page that stays open, or when the store attaches again after a silence.
   - **Lifetime.** The stream belongs to the conversation of the window, not to the control it was attached through: `ChatViewBinding` keeps it and the set of the conversation's chat controls displayed in the window, and stops it when the last of them is detached or disposed - the server half of the store's "first element opens, last one closes". That is also what ends the stream of a page that is left (reloaded, closed): `command-channel.ts` sends no command once the page is leaving, while the window's unload detaches its controls and `windowClosed` disposes them. The handler further stops itself once its window is no longer displayed (`isWindowShown`). It lives, therefore, while its window shows the chat, and that is what "a window shows this chat" means for waking a lead and marking a result seen.
   - **The chats of the account** are state, commands and data of the chat controls (`ChatControl`), so they travel the window's command channel and event stream like everything else:
     - **The list** (id, title, `lastUpdated` of every chat of the account - no paging, the list is small) is the `sessions` state entry of `<chat-conversation>` (`SessionListWatch.SESSIONS`, a list of `ChatSessionDto`), written on every render and again whenever the account's chats change - see "the browser store keeps the chat list" below. The agents, models and MCP servers the account may choose from are its `capabilities` entry (`ChatConversationControl.STATE_CAPABILITIES`, `ChatCapabilities.build`), computed on every render, in the session's language. A visitor gets neither. The store takes both whenever the element's state brings new ones (`ChatStore.takeLists`); the first capabilities choose the agent.
     - **Opening a chat** is the command `selectSession` (`ChatControl.CMD_SELECT_SESSION`, argument `sessionId`): it checks that the account may read the chat, switches the conversation's tracker to it (`ChatSessionManager.switchSession`) and answers `opened` plus the chat's `runState` (`RunStateFrame.of`). The run state comes in the answer, not on the stream, because the store applies it once the transcript is on screen - a frame arriving while it opens a chat is dropped. The transcript is read afterwards, as data of the same control: `ChatControl` is a `DataProvider`, and `/react-api/data?controlId=…&windowName=…&key=session/<id>` answers the `ChatSessionDetailResponse` (`ChatTranscripts.sessionDetail`: the run in flight, else the window's live log, else the recorded runs; ratings, effort and approval mode only for the chat the conversation has open). The store builds the URL from the element's `useTLDataUrl()`, handed to `ChatStore.subscribe` beside its command function and its `useTLUpload()`. A chat a first send creates is opened by that send; the store sends no `selectSession` for it.
     - **A subagent's transcript** is `key=subagent/<id>[/<runId>]` (`ChatTranscripts.subagentTranscript`): the live log while the subagent works, its recorded run afterwards, found by the run id its card names when the window holds no job for it any more. Every id in a key is URL-encoded (`ChatControl.DATA_KEY_SEPARATOR`).
     - **Deleting a chat** is the command `deleteSession` (`ChatControl.CMD_DELETE_SESSION`, answered `deleted`); `ChatHistoryService.deleteSession` cascades over recorded executions, tasks and interactions, and `ChatWindowService.onSessionDeleted` drops the chat out of the owner's windows.
     - Every one of them answers a chat the account may not read exactly like an id that names nothing (`opened`/`deleted` false, data not found), so the answer says nothing about which ids exist (`TestChatSessionCommands`).
   - **Run state in one frame.** The stream replays nothing, so `ChatSSEHandler.start()` writes a `run_state` frame (`RunStateEvent`, built by `RunStateFrame.of(tracker, account)`) right after `connected`: the chat it is about, whether it is processing (in this window or another window of the account), its task list, the approvals and questions of that chat waiting for the account (with the shape and asker only the waiting run knows), and the subagents with their costs and budget. `refreshRunState` (`ChatControl.CMD_REFRESH_RUN_STATE`) sends it again (`ChatViewBinding.sendRunState()`); the store sends it when the subagent panel opens, and the answer to `selectSession` carries it for the chat just opened - the live `subagent_*` frames keep the statuses current, not the costs and the budget. Building the frame also runs `ChatProcessor.applyPollUpdates`, which forgets a run that ended with an error. The store applies a frame only to the chat it shows, once it is open (`applyRunState`).

   The chat therefore works only in a window of the React layer - a React page or a classic tab through `tl-chat-classic`; there is no stand-alone event stream.

   **Behind a buffering proxy (#940).** A content-inspecting proxy (Zscaler, Netskope) can hold the window's stream back until it ends, forward it in blocks of some kilobytes, or refuse it. Every result of a React command arrives on that stream, so without a fallback the page would not react at all: no login dialog, no navigation, no chat answer. The engine's own polling takes over there (`react-api/poll`, `sse-client.ts` in `com.top_logic.layout.react`): the bridge changes to polling when its stream brings nothing, is refused, or does not bring the events it was told are due. A React command - the attach, a send, a regenerate among them - tells it that by itself.

   `tl-chat/proxy-rig/` reproduces such proxies in docker and runs the app through each of them.

   Ids the browser makes (stream tokens, message ids) come from `randomId()` (`ids.ts`): `crypto.randomUUID` exists only in a secure context, and an installation served over plain HTTP under a host name other than localhost could not send a message without the fallback.

   **Why no `EventSource` of its own (#867).** A browser opens at most six connections to one host over HTTP/1.1, for all its tabs together, and every React page holds one for `/react-api/events`. A chat stream of its own made it two per page: three tabs held all six, the browser queued every other request of every tab, and no click, navigation or message reached the server any more. A page now holds one connection. Six tabs still exhaust the pool with the engine's stream alone; HTTP/2 (which needs TLS in browsers) lifts the limit.

Every command of the chat controls refuses a window nobody is signed in to (`status` `forbidden`,
nothing done), because an application may hand a visitor a session without a login. The chat has
no anonymous mode: the chat controls pass `authenticated` to the
browser component, which shows a sign-in note rather than a composer. The note is the
conversation's alone - a `<chat-dock>` renders nothing at all, so a page states
it once (#643).

**Signing in within the window.** A login through the page's login dialog keeps the window's
sub-session and changes its person (`SessionService.loginUserInWindow`), and the window's tree is
built anew. Nothing of the chat that depends on the person is therefore held across renders:
`authenticated`, `userName` and `welcomeName` are written by `ChatViewBinding.writeRenderState`
(`STATE_*`) on every render, beside the language. And a conversation's binding and
tracker serve one account: `ChatViewBinding.bind` replaces a binding made for another person, and
`ChatSessionRegistry.tracker(subSession, conversation, account)` closes and replaces a tracker that
serves another account (`TaskProgressTracker.claim`), so the open chat, the run and the attached
files of the person before are never the next one's (`TestChatLoginInWindow`). The replacement
retires the trackers of every conversation of the window that serve another account and stops
what they run as the stop button does (`TaskProgressTracker.cancelRuns`: subagents, the run's token,
which ends its approval and question waits, the prompt improvement); a render for the same person
stops nothing. The run's frames go only to streams of its own account, and `present` writes only
channels of chats of the run's account (`ChatViewBinding.presentable(scope, name, account)`).

### Access to a chat (#30546)

Who may do what with a chat session is decided by the model-based security alone, never by comparing its owner: `ChatHistoryService.findAccessibleSession(id, person, op)` / `isAllowed(person, session, op)` ask `ModelAccessRights.isAllowed`.

- **Read** opens a chat (`selectSession`, the `input` channel, `publishSession`), delivers its transcript and the transcripts of its subagents over the data route, builds its `run_state`, and puts a window among the stream targets of its runs (`ChatEventHandlerRegistry.findViewers`: a window whose tracker has the chat active, for the account the switch checked).
- **Write** is asked by every command that changes the chat - `send`, `editSend`, `regenerate`, `rate`, `respond`, `cancel`, `setApprovalMode`, the subagent commands, `improvePrompt`, uploads, `deleteSession` - and by every store of the chat's data: the metadata persisted on a switch, the seen mark, the approval mode, the transcript of a run (`ChatProcessor.resolveAgentSession`). A window of an account that may only read stores nothing on the chat and is not registered to answer its subagents.
- `<chat-conversation writable="...">` narrows Write for reasons that are no question of rights (an archived subject): `ChatControl.isWritable`, overridden by `ChatConversationControl`. A command answers `forbidden` for a chat the account may read but not write here, `notFound` for one it may not read (`ChatControl.writeRefusal`). A new conversation, without a chat yet, is always writable.
- The state `readOnly` (`ChatConversationControl.STATE_READ_ONLY`) tells the browser: `chat-conversation.tsx` shows the transcript with its copy action and, in place of the composer, the note "Read only" / "Nur lesen" - no approval or question cards, no stop, no editing, regenerating or rating.
- What a run asks (`approval_required`, `approval_resolved`, questions, `interaction_resolved`) reaches only the windows of the run's account, which answers it (`ChatEventHandlerRegistry.findAnswerTargets`); everything else of a run reaches every window showing the chat.
- The shipped policy `tl-ai-core.chatSecurity.config.xml` gives the owner Read and Write; an application grants further roles (developer article `ai/security/administration`).
- `TestChatReadAccess` and `TestReaderWatchesTheRun` pin it.

### Processing Flow

1. User sends a message through the `send` command of a chat control (`ChatControl.CMD_SEND`), or a run is started on the server (`ChatStart`, the scheduler)
2. `ChatProcessor.sendMessage()` checks that the installation has room for the run (pool, per-account cap - both unlimited unless configured), adds the message to `Chat`, creates a `ConversationLog` turn, submits `AIBackgroundTask` to `ChatRunExecutor`
3. Background task resolves entry point agent from `AgentRegistry.getEntryPointAgentName()` and invokes it with `ConversationLogListener` bound for structured event recording
4. Agent events (tool calls, thinking, text deltas) are delivered to the SSE handler in real-time and recorded in the `ConversationLog`
5. `ChatProcessor.applyPollUpdates()` flushes pending messages into Chat
6. On completion, agent response added as `ChatMessage` and `ConversationLog` persisted

### Who decides that a call needs a person (#381)

`ChatProcessor.buildToolContext` no longer builds the run's `SecurityContext` itself. It asks
`RunSecurity.contextFor(new RunPrincipal(account, RunEntryPoint.CHAT, false, sessionId))`, and what
answers is the installation's configured `SecurityContextFactory` - by default the one backed by
TopLogic's roles and model access rights, which is what this line did before. A run whose tracker
says it has no window (#384) is announced as `SCHEDULED` and as a background run instead; everything
else on this path is the same.

An application substitutes that factory when its answer depends on **what** is being changed rather
than only on the tool's `ToolMode`: overwriting an empty section a machine wrote, overwriting what a
colleague wrote, and releasing a document to a customer are all `DATA_WRITE`, and only some of them
deserve an approval card. `ApprovalMode` cannot express that - it decides who answers a prompt, not
whether one is raised. The factory is asked once per run and returns a context whose
`checkApproval(Tool, ToolCall)` reads the call's arguments; extend `SecurityContextProxy` over
`DefaultSecurityContextFactory.defaultContext(run)` so that every other question keeps its answer.
Every other entry point - the TL-Script functions, the MCP endpoint, the prompt improver - goes
through the same call, so the substitution is one class and not five call sites. See
the developer article `ai/security/administration` (approval decided from the call).

### Human-in-the-loop: a pending interaction belongs to the account (#393)

A run that needs a tool approval, or that asks a question through `askUser`, parks its agent thread
on a future in the `UserInteractionStore` of its own `AIProcessingState`. That state hangs on the
`TaskProgressTracker` of **one** conversation of one browser window, which the chat transport
addresses one at a time (the chat control's window and `chat-id` → tracker). The request itself, though, is addressed to an
**account**: `InteractionRecorder` persists it as a `tl.ai.execution:InteractionRequest` with a
`responder`, and that is the rule this module follows everywhere.

| | |
|--|--|
| Listing what is pending | The `run_state` frame (`RunStateFrame`) – `AgentExecutionStore.pendingInteractionsFor(person)`, those of the chat the window shows, whatever window asks. The client replays an approval **and a question** from it after a reload (`applyRunState`). What the persisted row does not record - an approval's `toolMode`, a question's `multiSelect` and its parsed options - is read off the still-waiting run through `PendingInteractionRegistry`, so a rebuilt card asks what the agent asked (#466); nothing about the shape is persisted. A question row whose run is gone is therefore left out of the feed: its shape is unknown and an answer would reach nothing. |
| Answering | The `respond` command (`ChatControl.CMD_RESPOND`, `AccountRuns.respond`) – the request id is looked up in `PendingInteractionRegistry`, among **this account's** running runs, not in the asking window's tracker. A second tab, a reloaded page and a new login therefore all reach the parked run. |
| Who may answer | The account named as the run's responder, and nobody else. Another account finds nothing and is told exactly what a caller naming a request that does not exist is told (`status` `ignored`), so the answer says nothing about which ids exist. |
| Order of effects | The decision is recorded (`ConversationLogListener.resolveApproval`, "approve for this session" onto the **run's own** tracker) *before* the future is completed. Completing it unparks the agent mid-turn, and it may call the next tool before this request has returned. |
| Telling the other windows | `interaction_resolved`, pushed to the account's other live chat UIs (`ChatEventHandlerRegistry.findOthers`). The run's own window is left out: it hears `approval_resolved` from the run itself. A chat UI says which account it is (`ChatEventHandler.getAccount()`) and takes the frame (`sendInteractionResolved`); both have no-op defaults, so a UI that never lists another window's requests is unaffected. |
| Timeouts | Unchanged, and still the backstop: `InteractionTimeoutService` bounds both waits (30 min by default), and `AgentExecutionStore` cancels every still-pending row at startup, because no run survives a restart. |

`PendingInteractionRegistry` is a process-wide register of the chat runs in flight, keyed by task id
and named by the account each run belongs to. `ChatProcessor` puts a run in when it creates the
run's interaction store and takes it out in the same `finally` that unbinds the store, so its
entries are bounded by the runs in flight. It is **not** what keeps a window alive - the run's own
background task already holds its tracker while it works. Its name is older than its second use
(#465, below), which is stopping a run rather than answering it.

### Stopping a run belongs to the account too (#465)

The same window barrier held for the stop, which started from the asking window's own
tracker: the stop button of a second tab, and of the page after a reload, silently did nothing while
the run went on spending the account's tokens to the end of its turns.

The client names the chat it is stopping (`cancelRun(command, sessionId)` → the `cancel` command with `sessionId`), and
the run is looked up by that chat among **this account's** runs
(`PendingInteractionRegistry.findBySession`). Three consequences:

- any window of the account stops the run, and the run's own state - its cancellation token, its
  thread, its log listener - is reached through the register rather than through the window;
- the stop hits the chat the user is looking at, not whatever else the asking window happens to be
  running;
- a chat of another account, and a chat with no run, answer the same `cancelled` false, so the
  answer says nothing about which chats exist or which of them are running.

A client that names no chat is answered exactly as before - whatever the asking window is running -
and so is a client whose body cannot be read: the stop button is the way out of a run that is going
wrong, and must not start refusing requests. The subagents of the run are cancelled through the run
window's `SubagentJobRegistry`, which a tracker only hands out for its *active* chat, so that step is
skipped when the run's window has moved on to another chat.

### Running an agent on a schedule (#384)

`com.top_logic.ai.chat.sched.AgentTask` is a configured `Task` of the engine's `Scheduler` that puts
one prompt to a named agent and records the run as a chat session of the `run-as` account. The prompt
is plain text or a TL-Script expression evaluated at the start of every run; the rest of the frame -
`name`, `enabled`, `schedules` - is the engine's own.

It goes through `ChatProcessor.sendMessage`, not through `AgentRunner`, and that is the whole point:
what an operator wants from a nightly agent is a **sitting** they can open in their chat history, not
an execution row. The task therefore does what a browser window would - a `TaskProgressTracker`, a
session created by `ChatHistoryService.createSession`, the `run-as` account bound to its sub-session -
and then waits for the run, skipping a start while the previous one is still working and cancelling
the run's token when the scheduler stops it. It is not node-local: one cluster node runs it, because
an agent run writes and costs money.

**A scheduled run has nobody to ask,** and two things follow from that, set together by the task and
carried by `ChatProcessor.buildToolContext`:

| | |
|--|--|
| `TaskProgressTracker#isUnattended()` → `ToolContext#attended()` | `askUser` answers `AskUserTool.NOBODY_TO_ASK` at once instead of parking the agent thread until the 30-minute interaction timeout. Keyed on the context the entry point sets, never on what the prompt says. |
| `RunSecurity.contextFor(new RunPrincipal(account, SCHEDULED, background = true, ...))` | Approval is the compensating control for a write-classified tool, and a run that cannot show a prompt has lost it, so the run is additionally bounded by the policy's `<background-allowlist>`. The entry point the factory is told about is `RunEntryPoint.SCHEDULED` rather than `CHAT` (#381), because nobody typed this message. |

`isUnattended()` is deliberately **not** derived from the approval mode inside `ChatProcessor`: a user
who switches their own chat to `AUTO_APPROVE` has not left the room. Only a caller that knows there is
no window sets it - today, the task, and it sets it for `AUTO_APPROVE` only. A task configured
`approval-mode="MANUAL"` declares that a person will answer, which since #393 any window of that
account can, and is therefore an ordinary attended run started by a clock.

Approval prompts never blocked in `AUTO_APPROVE` - the callback answers on the calling thread - so
`askUser` was the one thing that could park an unattended run. See the developer article `ai/script-and-scheduled-runs` (agents on a schedule).

### The switch reaches the running turn, and no answer is lost (#943, #944)

`ChatApprovalCallback` is the approval callback of every chat run. It reads the chat's
`approvalMode()` at **every** call, not when the run starts: a person who switches auto-approve on
during a turn is not asked for the next write of that turn, one who switches it off is.
The switch (`setApprovalMode`) therefore sets the mode on the window's copy of the chat **and** on the
copies the account's running runs and subagents hold (`PendingInteractionRegistry.chatsOf`), because
the run may have been started in another window. A card already open when the switch goes on is
answered by the browser, as before.

The callback registers its wait in `ApprovalCallback.open`, which `AgentLoop` calls **before** it
emits `ApprovalRequired`: an answer can come the moment the card is shown, and an answer that found
no wait was dropped ("no run of this account is waiting for it") while the run waited 30 minutes.
`TestChatApprovalDuringTurn` and `TestApprovalModeSwitch` pin both.

### Key Classes

- **`ChatProcessor`** – Static utility: orchestrator execution, state sync, and the two pre-checks a send passes before anything is written down (pool room, per-account run cap)
- **`ConversationBackend`** (`…chat.backend`) – What answers a conversation instead of the framework's agent (an external process); chosen per run from the conversation's subject by the `ConversationBackendResolver`s of `ConversationBackendService` (`backendFor(TLObject subject)`; none configured here, a session without subject is always the framework's agent's). What a conversation about a subject of a type offers is answered per type (`featuresFor(TLStructuredType)`), so an empty chat knows it before the first message: `ChatConversationControl` writes it as state `subjectFeatures` for the type of the object in its `subject` channel, and `chat-store` gives it to every new conversation. `ChatProcessor.executeOrchestrator` is the run frame both share, `runInternal` the framework agent's part, `BackendRun` the backend's (recording through `RunRecording`, events only on the worker thread). Its `BackendFeatures` reach the browser as `features` of a chat's transcript (`ChatTranscripts.sessionDetail`) and of the answer to `send`, where `ui/src/features.ts` gates the controls; `ChatControl` refuses an unsupported regenerate, edit or attachment and skips the agent policy for such a chat. See the developer article `ai/chat-ui`
- **`ChatRunExecutor`** – The pool the chat's agent runs execute on. Unbounded unless `<capacity>` on `AgentRuntimeService` (tl-ai-core) names a `chat-workers` ceiling, next to the per-account cap; with a ceiling a send that finds every worker busy waits, and it is refused only where `chat-queue` bounds the waiting room. See the developer article `ai/limits-and-caching` (run limits)
- **`TaskProgressTracker`** – One window's chat: its processing states, and accessors that read and write the `ChatState` of the chat the window shows
- **`ChatEventHandler`** – The contract a chat UI implements to observe a run: the `AgentEvent` stream plus the
  final-message and provider-error notifications `ChatProcessor` produces itself
- **`ChatEventHandlerRegistry`** – Interface-typed lookup of the chat UI observing a `TaskProgressTracker`;
  `ChatProcessor` resolves the handler here per event, so a UI other than `ChatSSEHandler` can register.
  `findOthers` is the account-wide direction: every *other* window of one account, for news that is
  the account's rather than one run's
- **`PendingInteractionRegistry`** – The chat runs in flight, keyed by task id and named by the account
  they belong to. What lets any window of that account answer a request the window that started the run
  is parked on, and stop the run itself - see "a pending interaction belongs to the account" and
  "stopping a run belongs to the account too"
- **`ChatSSEHandler`** – Push-based SSE implementation of `ChatEventHandler`; started and stopped by `ChatViewBinding` on the commands of `ChatControl`, see "Push" above
- **`AccountRuns`** – What an account does to its runs while they work, found among the account's runs rather than the window's: answering an approval or a question, stopping a run, writing to a subagent and stopping one
- **`RunStateFrame`** – Builds the `run_state` frame of a window's conversation: processing, tasks, the waiting approvals and questions of the chat, the subagents with their budget
- **`ChatControl`** – Base class of the controls whose browser elements join a conversation's store (`<chat-conversation>`, `<chat-dock>`, `<chat-sources>`): binds the window's conversation, writes the bootstrap and the render state, and answers the stream commands `attachStream`, `detachStream` and `refreshRunState`, the chat commands `selectSession` and `deleteSession`, the sending commands `send`, `editSend`, `regenerate` and `removeAttachment`, the run's gestures `respond`, `cancel`, `setApprovalMode`, `rate`, `improvePrompt`, `cancelPromptImprovement`, `messageSubagent` and `cancelSubagent`, and the upload of attachments. Every one of them answers all of these, because the store sends its commands through whichever element joined last
- **`Chat`** – Thread-safe in-memory container using `CopyOnWriteArrayList<ChatMessage>` (max 500)
- **`ChatSessionManager`** – Holds a `ChatState` per chat with LRU caching (max 5 sessions). Handles session switching with persist/load/rebuild cycle.
- **`ChatState`** – Everything one chat holds in a window's memory: Chat, ConversationMemory, ConversationLog, the agent's plan (`TaskStore`) and tool catalog, and the settings of its metadata bag (approval mode, approved tools, loaded skills, ratings, effort, token usage). They belong to the chat, not to the window: a window that comes back to a chat it holds finds them as it left them. A send captures the state of its chat under the send lock, and the run reads and writes only that state, also when the window has switched to another chat by the time the run starts or ends (#844)
- **`ChatHistoryService`** – Static access to persistent `AgentSession` objects (kind CHAT) via `AgentExecutionStore` (tl-ai-core). Conversations are stored as one transcript per `AgentExecution` (written by `AgentRunner` at run end) and merged in run order on load.
- **`AgentTask`** (`…chat.sched`) – The scheduler task above. Not to be confused with
  `tl.ai.execution:AgentTask`, which is one entry of the plan an agent writes while it works
- **`ChatSessionRegistry`** – The trackers of a window's conversations, keyed by `(sub-session, conversation)`: a window showing a page's chat beside a `<chat-dock>`'s has a `TaskProgressTracker` for each (#502). A caller that names no conversation registers and resolves the window's unnamed one. A tracker serves one account: `tracker(subSession, conversation, account)` replaces one that serves another, after a login in the window. Published API: `ChatStart` and an application's own control register here from outside the chat controls, and `ChatWindowService` reaches every tracker of a window through `trackers`.
- **`ChatViewBinding`** – Once per (sub-session, conversation) and account: tracker, stream, the files attached in each of its chats, and the channels the agent may write there. The split elements (`<chat-conversation>`, `<chat-dock>`, `<chat-sources>`) delegate to it so several controls do not bind the window several times. It also holds the conversation's stream and the chat controls of the conversation the window displays, and stops the stream when the last of them goes. The conversation is the `chat-id`; it is read as a key of its own because a chat that shared its conversation with nobody would key its tracker differently - the one-element `<agent-chat>` did, until #487 removed it. What depends on the person or the session - `authenticated`, the user's names, the language, the auto-approve choice - is not held: it is read whenever `writeRenderState` is asked for it, which every chat control does from its constructor and from `onBeforeWrite` - see "where the chat's state lives". It is also where every join is tallied for `ChatJoinLog`.
- **`ChatWindowService`** – Connects agent runs to the browser windows the chat is displayed in. It installs the `RunWindow` of tl-ai-core over the engine's `ReactWindowReplay.inWindow(subSession, action)`; it offers `inWindow` / `hasWindow`, through which a tool writes into the window of its run and tells "this run is headless" from "this page allows nothing" (a write waits at most `write-timeout-millis`, 5 s, for a browser command in progress and answers `BUSY` otherwise); and on `AgentExecutionStore.ChangeListener.onSessionDeleted` it drops the deleted chat out of every live window of its owner (`ReactWindowRegistry.windowSubSessions()`, filtered by the sub-session's person). Following commits is not its job: the engine's `WindowModelEventService` delivers commits of other threads, such as an agent run's, to every browser window whose model listeners they affect.
- **`ChatPresentTool`** – The `present` tool of a chat page (`com.top_logic.ai.chat.tools`), over the base `PresentTool` in tl-ai-core: the agent shows objects - found by a TL-Script query or named by id - as a table, a chart or a form on a channel of the page it is on. Registered from this module's own configuration, so every application that depends on `tl-chat` has the tool; **which** channel it may write is the page's decision (`<chat-conversation present="…">`, deny by default), and a run whose page displays no such channel is not offered the tool at all (#649). See "the agent writes a channel" below.
- **`PresentationPane` / `PresentationPaneElement` / `PresentationPaneControl`** – `<presentation-pane input="…">` (client `TLPresentationPane`, `ui/src/presentation-pane.tsx`): renders what stands on that channel for any type, and keeps the history "back" walks. See "the presentation pane" below.
- **`ChatScope` / `ChatScopeElement`** – The `<chat>` scope around exactly one element, and the ambient value it puts into that element's `ViewContext`. An element's conversation is its own `chat-id`, else the enclosing `<chat>`, else `default`.
- **`ChatJoinLog`** – Per render, which element joined which chat id; logged when the interaction ends, with a WARN for an element that joined nobody while another id has several.

## Thread Safety

- `Chat` uses `CopyOnWriteArrayList` for thread-safe message access
- `AIProcessingState` uses `synchronized` blocks and `volatile` fields
- `ChatSSEHandler` uses `AtomicBoolean` for close detection; its frame buffer is written only by its flush task (and by `start()` before that task runs). `SSEUpdateQueue.enqueue` is thread-safe, so writing a patch needs no window interaction

## Persistence Model

- **Module**: `tl.ai.execution` (tl-ai-core), **Types**: `AgentSession` (kind CHAT) + `AgentExecution` per run
- Session: `title`, `kind`, `owner`, `metadata` (approved tools, approval mode, tasks, skills), timestamps
- The approval mode in `metadata` is the chat's "auto-approve" switch, and only the switch writes it: `setApprovalMode` names the chat and stores the mode at once, and every other save of the bag keeps the stored mode, so no window writes back the mode it opened the chat with (#658). A chat's transcript (`ChatTranscripts.sessionDetail`) answers it as `approvalMode`, which the switch shows. A chat that stores no mode runs `MANUAL` (#808). The switch also belongs to the person (#948): every `setApprovalMode`, also one without a chat, stores the position as the person's last choice (`ApprovalChoice`, `PersonalConfiguration` key `tl-chat.approvalMode`); the chat controls boot with it (`approvalMode` in the state), and a new chat starts with it. The browser names the mode with every `send` (`approvalMode`), so the chat a first send creates stores it, as a scheduled task's mode is, and every turn runs with the position the switch shows.
- Execution: `runId`, `agentName`, `model`, `provider`, `status`, `stopReason`, token columns, `transcript` (CLOB)
- Transcript format: `ConversationLog` turns serialized via `ConversationLogSerializer` (tl-ai-core, `PersistedConversationV2`)
- Human-in-the-loop requests persist as `InteractionRequest` rows (approval/question/choice with resolution)
- The agent's task plan persists as `AgentTask` rows of the session. `TaskStore` (tl-ai-core) stays the truth of the running turn - it also carries `activeForm`, `owner` and the blocks/blocked-by sets, which the model does not - and `AgentTaskMirror` writes what it says onto the rows after every mutation, through `AgentExecutionStore.syncTasks` (one transaction, on the writer thread). The store is still serialized into the session metadata as well, because that is what restores it when a session is re-opened and what the SSE `task_update` frame reads. So: the blob is the store's own persistence, the rows are the model a view binds to.
- Legacy `tl.ai.chat:ChatSession` single-blob sessions were migrated to this model eagerly at startup by a conversion service; both the legacy model and that service have since been removed (#487)

## Engine dependencies

The engine modules this one uses must be plain `compile` dependencies - never `optional`, never
`provided`. `tl-chat.conf.config.xml` registers services of this module unconditionally, and every
dependent that loads that configuration has to resolve their classes: `ChatWindowService`
alone pulls in `ReactWindowRegistry` from `tl-layout-react`. An `optional` dependency does not reach
dependents, so the missing jar surfaces only when a downstream module boots the configuration, as a
`NoClassDefFoundError` out of the config reader (#415).

`tl-chat-classic` is that downstream module, and its own suite is what catches it, so after adding
or rescoping an engine dependency here run both suites:

```bash
mvn -o -pl tl-chat install -DskipTests=false
mvn -o -pl tl-chat-classic test -DskipTests=false
```

The second one needs the first: it resolves `tl-chat` from the local repository, not from the
reactor.

## Front end

The chat UI is a React application in `ui/`. It talks to the chat controls of its window through
the React bridge - their state, their commands and their data (see "Push" and
"the chats of the account" above) - and to nothing else: no TopLogic AJAX framework.

This React chat is the only chat front end. A LayoutServlet application shows it in a classic tab
through the separate module `tl-chat-classic` (#618), which writes these same view elements into a
classic page; it replaced the hand-written `chat-client.js` control that lived there since #398. A
host gets the "Albert" tab by depending on `tl-chat-classic`; depending on `tl-chat` alone gives the
API and the React chat in React pages.

### Toolchain and build output

| | |
|--|--|
| Sources | `ui/src` (TypeScript, `.tsx`, `jsx: "react"` - the classic runtime, because React comes from the host) |
| Bundler | Vite library build, entries `ui/src/main.tsx` (`tl-chat-ui.js`) and `ui/src/index.ts` (`tl-chat-seams.js`) |
| Output | `src/main/webapp/script/tl-chat-ui.js`, `tl-chat-ui.css`, `tl-chat-seams.js` - **git-ignored build output, never commit it** |
| Node | installed by `frontend-maven-plugin` into `ui/node/` (v20.20.2 - 20.19 or newer, because jsdom 29 reaches `@exodus/bytes` through a `require()` of an ES module); a local Node 20.19+ works too. After a version bump, delete `ui/node/` once: the plugin swaps the binary but keeps the old bundled npm, which then fails with "Class extends value undefined is not a constructor or null". |
| Dependencies | `marked` (MIT) + `dompurify` (Apache-2.0 or MPL-2.0) for markdown, `highlight.js` (BSD-3-Clause) for code blocks - core plus 13 registered grammars. All three are bundled into `tl-chat-ui.js`, so their licences travel with the WAR. |

`mvn -pl tl-chat install` runs, in `generate-resources`: `npm install`, then `npm run typecheck`
(`tsc --noEmit`), then `npm run build`. The type check is a build step of its own because the Vite
library build strips types without checking them. `vitest run` comes later, in the `test` phase -
see below.

`tl-react-bridge` is the host's module: the build keeps it external and the browser resolves it
through a `<module-script specifier=…>`, so there is nothing in `node_modules` for a test run to
import. `vite.config.ts` therefore aliases it, for the vitest run only, to `ui/test/tl-react-bridge.ts`.
The stub hands out the **real** React (a dev dependency), so a test may render a component with
`react-dom/server` and assert on the markup - `chat-sources.test.tsx` does. Everything else in it is the
smallest stand-in that behaves: the control state a test sets, a command sink it reads back, and a
marker element in place of a mounted child control. React must still never be imported from `react`
in `ui/src` - it is the host's instance, and a second copy breaks every hook.

The install, the type check and the bundle sit in `generate-resources`, because the bundle that goes
into the WAR must not be built from code that does not compile. **The `vitest run` does not** - it
is bound to the `test` phase, behind surefire (#450). Bound before `compile`, one broken frontend
test aborted the module, and the state of its ~190 Java tests was then not merely unreported but
unproduced: no `target/surefire-reports` directory existed at all. Behind surefire, a build always
has both answers, and a failing JS test still fails the build before `package`, so nothing that its
own tests reject is ever packaged or installed.

`-DskipTests` still runs the JS tests. The execution pins `skipTests` to `false` for that: the
frontend plugin honours `${skipTests}` for an execution in a testing phase, so without the pin the
move would have switched the JS tests off in every build that does not say `-DskipTests=false`,
which in this repository is the default. The flag is for the Java suites, whose KB setup is what
makes a build slow, while the whole vitest run is a second or two. What switches the frontend steps
off is `-Dskip.npm=true`.

`composer.test.tsx` is the one suite that mounts a component into a live DOM - `jsdom`, asked for by
that file's `@vitest-environment` docblock and by nothing else. It has to: what it guards only
exists between a real commit and the passive effects that follow it, and `react-dom/server` performs
neither. That is what the dependency buys, and it is why the run now takes ~3 s rather than ~2 s.

**The composer writes no state per keystroke.** A keystroke is a discrete event, so React commits it
on the microtask that follows; a `setState` that runs *after* that commit - from a passive effect,
typically - is a nested update, and fifty in a row with no idle frame in between is React's
"maximum update depth exceeded" (the minified error #185). A driver typing at zero delay, a paste,
an IME burst and a held key all produce exactly that cadence, so an effect of the composer's that
writes state on every `text` change makes the chat throw at the fifty-first character (#419). State
that follows the draft is therefore **derived** at render time, not stored and re-synchronised by an
effect; an effect may arm a timer, and the timer's own write is fine because it lands on an idle
frame. `composer.test.tsx` holds that line for typing, paste, composition and the "@" picker.

### Dev loop

```bash
cd tl-chat/ui
npm install
npm run typecheck          # tsc --noEmit
npm run build              # writes the bundle into src/main/webapp/script/
```

The bundle is served out of the `tl-chat` web-fragment WAR, so a demo application picks up a change
only after `mvn -pl tl-chat -am -DskipTests install` and a restart of the application.

### Embedding in the React view layer

The chat is composed from view elements: `<chat-conversation>` (transcript, composer, pickers),
`<chat-dock>`, `<chat-sources>` and `<presentation-pane>`, all but the last joined by their
`chat-id`, placed beside the conversation in the engine's `<side-rail>`. Each control registers the
per-window `TaskProgressTracker` through `ChatViewBinding` and pushes the bootstrap state
(`authenticated`, `userName`, `welcomeName`, `traceMode`, `promptImprovementEnabled`, `placeholder`) the
browser component reads. The one-element `<agent-chat/>` form was removed with #487: no application
used it, and the empty chat-id it needed was a special case in three live code paths.
The browser components are registered in `ui/src/main.tsx`: `TLChatConversation`, `TLChatDock`,
`TLChatSources`, `TLPresentationPane`.

The session list is no element of this module's own: it is a `<data-list>` of
`tl.ai.execution:AgentSession` whose `selection` channel feeds `<chat-conversation input>`, which
is what the shipped page `tl-chat/albert.view.xml` configures.

**The shipped page.** `src/main/webapp/WEB-INF/views/tl-chat/albert.view.xml` is the chat page every
React application gets with this module: the account's chats (grouped by day, new / rename / delete,
rename through `tl-chat/rename-chat.view.xml`), the conversation with `present="paneObject"`, and a
`<side-rail id="rail">` with Progress, Sources and Memory, open on the first visit. While `paneObject`
holds something, the rail's section `presented` (visibility `!isEmpty($paneObject)`, `reveal="true"`,
`header="false"`: the pane's own title bar is its header) shows it in a `<presentation-pane>`, and the other sections, which carry `isEmpty($paneObject)`, give
way to it. The button at the head of the transcript is a command of `<chat-conversation><commands>`
that toggles the rail's `open-channel` `railOpen` and is pressed while it is open
(`active-expr="$railOpen"`); its badge is the rail's `badge-channel` `railBadge`. The derived channel `paneItem` (the
one object in the pane, empty for a table or a chart) is the conversation's `subject`, and the chat
list is scoped by it: the account's chats about that object plus the open one while there are such,
else all of the account's chats. The composer channels `chatDraft`, `chatReference` and `chatQuote`
are declared on the page and wired as `draft`, `reference` and `quote`, so an embedding view binds
its own over them (`<view-ref><bind/>`); the prompt improver is `TL_PromptImprover`. It knows nothing
of an application; an application shows it with one
`<nav-item><view-ref view="tl-chat/albert.view.xml"/></nav-item>` and changes it with a same-path
overlay addressing the ids `albert`, `chats`, `conversation` and `rail` (never a copy). The rail's
sections are keyed by `name` (`@Key` on `SideRailElement.Config.getSections()`), so an overlay places
a section with `config:position` / `config:reference` and updates or removes a shipped one by its
name; two sections of one rail must not share a name (`TestAlbertRailOverlay`). It uses the default
chat id, so a `<chat-dock>` without `chat-id` joins its conversation. The classic
tab of `tl-chat-classic` keeps a view of its own (`chat-id="classic"`). The developer article
`ai/adding-albert` is the how-to around it. (The former `<chat-sessions/>` element was
deleted with #415 - `<data-list>` does the same job with no chat-specific code.) The agent's task
plan went the same way: the run mirrors it onto `tl.ai.execution:AgentTask` objects, so the
Fortschritt section is a `<data-list>` over those, and `<chat-progress/>` is gone. `useChat` is
**not** exported from `index.ts`: the store is internal to the bundle, so a host cannot write its
own element against it. New chat pieces live in `ui/src` and are registered in `main.tsx`.

### Configuring `<data-list>`

The engine's element (`com.top_logic.layout.view.list.DataListElement` / `DataListControl`, client
`TLDataList` in `com.top_logic.layout.react`, design system `tl-list`); the view-layer articles
describe it. What the chat adds is its own look of the session list: the views mark it
`css-class="tl-chat-sessions"`, which gives it the rail's width and surface (`ui/src/styles.css`), and
the row-state classes below.

| Option | What it is |
|--|--|
| `rows` | TL-Script producing the row objects, the way `<table>` binds its rows. Mandatory. |
| `<inputs><input channel="…"/>` | Channel values passed to `rows` as positional arguments. |
| `observed-types` | Types whose create/update/delete re-runs `rows`, so the list follows the model. |
| `<lines><line attribute=…\|<value>…` | What a row shows, top to bottom - a table's columns, stacked. An attribute holding a point in time is written in the format a form field and a table cell show it in (`FieldControlService.dateFormat`), so a `tl.core:DateTime` shows its time of day. `emphasis` is `primary` / `secondary`. |
| `group-by` | TL-Script of one row giving its group header; unset means a flat list. |
| `row-css-class` | TL-Script of one row giving extra classes on the row element (`tl-list__item`) - its *state* (`is-done`, `is-running`, `is-pending`, `is-working`, `is-waiting`, `has-news` are the ones this module's stylesheet defines). A row function written as `css-class` is refused at startup. |
| `css-class` | A class on the list's root element, as on every view element of the engine. |
| `selection` | Two-way channel holding the picked row object. |
| `count` | Channel receiving the number of rows, written with every serialisation - what a rail section's badge reads without asking the browser. |
| `search` / `title` / `empty-text` | The list's own chrome. |
| `<leading name image>` / `<trailing attribute\|value display>` | An avatar or icon in front of the lines, metadata text or a badge after them. |
| `<commands><command>` | Header buttons above the list. Ordinary `<generic-command>`s, each rendered as the engine's command button (`ReactButtonControl`, a child control in the `buttons` state): hidden while its rules hide it (`<access>`, `<visible-if>`), disabled with the reason as tooltip while they disable it (`<disabled-if>`), and following every change of its state. |
| `<row-commands><command>` | Commands offered on a single row - see below. |
| `row-channel` | Name of the local channel a row command reads its row from. Default `row`. |
| `row-actions` | `hover` (default) / `always` / `none` - when a row shows its trigger. |

#### Row commands

A `<row-commands>` entry is the same `<generic-command>` a header command is: label, image, clique,
tooltip, `<executability>` and any action chain. The row the menu was opened on reaches the command
through the **row channel** the list registers - which is what the command names in its `inputs`, and
what an `<executability>` rule therefore sees (as `$session` below), re-evaluated each time the menu opens.

```xml
<data-list selection="chatSession" row-channel="session" row-actions="hover" …>
    <row-commands>
        <generic-command image="css:bi bi-pencil" inputs="session">
            <label><en>Rename</en><de>Umbenennen</de></label>
            <open-dialog dialog-view="tl-chat/rename-chat.view.xml" bind-input-to="model"/>
        </generic-command>
        <generic-command image="css:bi bi-trash" inputs="session" clique="delete">
            <label><en>Delete</en><de>Löschen</de></label>
            <confirm><expr>…names $session…</expr></confirm>
            <execute-script function="aiSessionDelete($session)"/>
            <write-channel name="chatSession" clear="true"/>
        </generic-command>
    </row-commands>
</data-list>
```

What the element owns is the trigger; the **menu is the application's** (`ContextMenuOpener` /
`ReactMenuControl`), so its entries carry the app's typography, its cliques become separators, and
arrow keys, Enter and Escape already work. Two consequences worth knowing:

- A command's `css-classes` does not reach the menu - the opener emits id, label, icon and disabled -
  so a red "Delete" entry is not available without an engine change.
- Outside an `<app-shell>` there is no opener. The list then renders **no trigger** (one DEBUG line,
  no exception): a trigger that opens nothing is worse than none.
- The opener dispatches a menu command with the **frame's** `ReactContext`, which is not a
  `ViewContext` and does not hold this list's row channel. A `ViewCommandModel` ignores that context
  and runs in the one it was created in - here the list's, which holds the row channel - so a row
  command reaches its row whoever dispatches it.
- A channel that already holds a value notifies nobody, so reopening the same row's menu would show
  what its `<executability>` decided the first time. The control re-evaluates the rules explicitly
  before every open.

`row-actions` has the same meaning and the same touch caveat as `<panel hover-actions>`: `hover`
reveals the trigger through `tl-reveal-on-hover` of the design system, on hovering the row and on
keyboard focus in it, and permanently where there is no hover.

When the row whose menu was open disappears - what a Delete in that menu does - focus moves to the
neighbouring row's trigger (previous, else next, else the list), not to `<body>`.

#### A list in a narrow host (#548)

A `<data-list>` stands in a row beside columns that do not give way - beside the conversation with
its 20 rem floor (#511) and the rail's strip. Below the
width they need together (688 px in the demo) the row overflowed into the engine's scrolling
`.tlSidebar__content`: the composer started off-screen and the chat scrolled in two dimensions.

The list has no collapse of its own: the chat's session lists stand in a `<side-rail>` of one
section (`header="false"`, the list's own title is the head; `width="264"`, `drawer-below="44"`;
`dismiss-on="chatSession"`, so that a picked chat closes the drawer).
The rail collapses the list to its strip, and below 44 rem of the area it stands in it keeps only the
strip and opens the list as the rail's modal drawer over the conversation - see the side-rail section
of `view-layer/layout`. Whether the rail is open is remembered per user. In the shipped views the
rails stand in panes of a `<split-panel>`: a rail that is a strip - closed, or in drawer mode -
shrinks its pane to the strip, and the conversation's pane takes the room.

- The conversation's floor gives way by `--chat-conversation-floor-reserve` (two strips and two
  gaps, 10 rem) while a side rail beside it is a strip - closed, or in drawer mode - so a 320 px
  window reflows instead of scrolling. "Beside it" is a sibling of the conversation's ancestor, or
  a rail in another pane of the split panel the conversation stands in (`styles.css`). Beside a rail that is a column the #511 floor holds unchanged.

The chat's 44 rem is the demo row: list 16.5 + floor 20 + rail strip 3.5 + two gaps 3 = 43 rem.
It is an attribute rather than a constant because only the view knows what else stands in the row;
a host with less beside the list sets less.

Writing a session: a row command calls `aiSessionRename(session, title)` / `aiSessionDelete(session)`
(tl-ai-core `AIScriptFunctions`), never the `title` attribute. Those go through
`AgentExecutionStore`, which commits on its writer thread, bumps `lastUpdated` (the list's own
grouping) and tells the other windows; `aiSessionDelete` also asks a live run to stop and drops the
chat out of every window that holds it. Both refuse a session the acting account neither owns nor
administers.

The browser store keeps the chat list, and the conversation's header reads titles
from it. The list is the `sessions` state entry of `<chat-conversation>`, which `SessionListWatch`
writes on every render and again when the model events of such a write reach it: it reads the list
again for a created chat of the account, a chat whose title is not the one it saw last, and any
deletion, and sends it when it differs from what the browser was told. Not every update counts - a
run writes its chat's bookkeeping several times per send, and counting those made one send cost four
list reads instead of one. A delete through the conversation's own `deleteSession` and the chat a
first send creates are in the store's list at once; the server's list follows with the command's
model events. Opening a chat also takes the title its transcript answers with, so a window that
missed a change shows the current title as soon as the chat is opened (#596).

This module registers the bundle on `ClientResources` itself (`tl-chat.conf.config.xml`, #642), so
an application that depends on `tl-chat` needs no entry of its own:

```xml
<module-script name="tl-react-agent-chat" resource="/script/tl-chat-ui.js" requires="tl-react-bridge"/>
<stylesheet name="tl-react-agent-chat-css" resource="/script/tl-chat-ui.css" requires="tl-design-system-components"/>
```

The `resources` list is keyed by `name`, so an application that still declares one of these names
overrides the entry rather than loading the bundle twice; it can delete its copy. Only a host that
registers an element or a seam of its own (see "Extending the chat") adds the seams module:

```xml
<module-script name="tl-chat-seams" resource="/script/tl-chat-seams.js" specifier="tl-chat-seams" requires="tl-react-bridge"/>
```

### Styling

`ui/src/styles.css` uses only the TopLogic React design tokens (`--background`, `--layer`,
`--text-primary`, `--border-subtle`, `--corner-radius`, `--shadow-menu`, ...), so the chat follows
the theme the app-bar's `<set-theme>` buttons select. Tokens the shipped themes do not define yet
are aliased once at the top of the file with a fallback that is correct in both themes; add new ones
there rather than hard-coding a colour.

### An element measures itself, not the window

Every responsive rule in `ui/src/styles.css` that is about *how much room the chat has* is a
`@container` query, not a `@media` one (Issue #427). A chat placed by an application almost never
owns the window - a shell's navigation and the page gutters take their share first - so a viewport
query asks a box the chat does not have: it stayed wide while the conversation was 234 px, and the
narrow layout arrived hundreds of pixels late or never.

One container carries all of it:

| Container | On | What it decides |
|--|--|--|
| `chat-stage` | `.stage` - the transcript plus the composer of a `<chat-conversation/>` | Thread gutters (≤ 1080 px), Enter hint long/short (≤ 35 rem), the auto-approve label (≤ 19 rem), the model name on its picker (≤ 18 rem), and whether the composer's controls wrap (≤ 16 rem). |

Rules to know when touching this:

- **The stage is what the composer measures.** The session list and the side rail beside the
  conversation take their columns first, so a rule about the composer's room asks `.stage`, never a
  box around the chat.
- **The session list and `.tl-chat-dock__body` are deliberately not containers.** Neither has
  anything to ask: a list is as wide as the column it was placed in, and what the dock's card size
  has to reach is the conversation inside it, which measures itself through `.stage`.
- **No JS half asks the window for a width.** Nothing in the bundle reads `window.matchMedia` for a
  width. Keep it that way - a JS half that asks the window and a CSS half that asks the element
  disagree at every width where it matters.
- **What stays a media query, and why.** `@media (hover: none)` and
  `@media (prefers-reduced-motion: reduce)` are about the person's input device and their
  preference, not about a box. `@media (max-width: 1100px)` on `.plus-popover` / `.model-popover` /
  `.context-popover` / `.activity-panel` makes them full-bleed sheets on the bottom edge, which is a
  phone idiom and so genuinely the screen's business - note only that `position: fixed` there now
  resolves against `.stage`, because a query container is the containing block of its fixed
  descendants, which is what puts the popover over the chat rather than over the application.
- **Sizing inside a container query uses `cqi`, not `vw`**, so a size is a share of the chat, not of
  the window.

**Browser support.** `container-type`, `@container` and the container query units (`cqi`) are
Baseline **Widely available**, across browsers since **February 2023** (MDN). They raise no floor
this product did not already have: the engine's React layer and this application's own shell are
written with `:has()` - `.tl-item:has(> .tlFill)`, `button:has(.bi-moon-stars)` - which is
Baseline Widely available only since **December 2023**, ten months later. The repo's own browser
tests run current Chromium (`tools/web`, Playwright); there is no listed minimum browser beyond
that.

### What the agent picker shows, and what it filters

Two fields of the agent definition are written for a person and are the only ones the UI shows:
`display-name` (what the agent is called - `TL_SingleAgent` is "Albert") and `summary` (one line
saying what it is for). Both are localizable, both live on the definition - in
`tl-ai-core.conf.config.xml` for the agents this product ships, or in the agent record for one an
installation authors - and both fall back to nothing: an agent with no display name is shown by its
technical name, one with no summary by its name alone. The agent's `description` is a delegation
instruction addressed to a model and never reaches the browser.

The summary is rendered **as authored** and **whole**: under the row's label, wrapping onto as many
lines as it needs (#635). The same holds for every hint of the "+" menu and the model picker. A hint
stands under the label and the value rather than beside the value, so how much room it has never
depends on the value - a longer model name used to cut the sentence off again. Each row is named by
its label and value and described by its hint (`aria-labelledby` / `aria-describedby`), so a screen
reader hears the whole sentence too. A summary is still best kept to one line where it is written,
because the menu is easier to scan that way; nothing breaks when it is not.

**Where an installation changes them (#403).** For the agents this product ships, both texts are
configuration, and the place to change them is *Administration → Development → Service Editor →
TopLogic-Agents and Skills*: each agent's entry now begins with **Display name** and **Summary**,
above the model-facing description, with a field per language and the shipped text already in it.
The technical `name` above them is not editable there, and that is the point of having a display
name at all: other configuration, the skills, the security policy and every persisted session name
the agent by its identifier, so renaming that is a migration while renaming the display name is a
setting. Two things about the mechanism are worth knowing before using it. A saved change reaches
users only when the service is restarted - *Administration → System → Services → Restart*, or the
next application start; saving alone changes nothing a running agent does. And the editor saves the
configuration of **every** agent as it currently reads, into
`WEB-INF/autoconf/com.top_logic.ai.core.agent.TopLogicAgentDefinitions.config.xml`, so from then
on the installation carries its own copy of the shipped prompts and tool lists too; *Reset
configuration* in the same editor deletes that file and gives the shipped texts back at the next
restart. An installation that wants to change two texts and keep receiving the rest of every release
writes that overlay file by hand instead, naming only the two properties it replaces - which is what
the configuration overlay merge is for, and what `TestShippedAgentOverride` pins.

The `capabilities` state of `<chat-conversation>` is filtered by the acting account (#392): the agents
and the MCP servers it lists are exactly what a run of that account accepts. The **models** are the
one thing a single payload cannot make exact - it is written per render and the agent changes without
another round trip - so
it lists what the account may choose with *some* agent it may run and marks each with
`grantedToRole`; `modelsFor()` in `models.ts` narrows that to `granted ∪ {selected agent's model}`
before the picker sees it. A request that names something the account may not use is answered
`forbidden` with a sentence in the user's language, which the composer shows in the notice strip in the neutral
tone.

`entryPointAgent` is resolved for the caller, not read from the configuration: a client that names no
agent runs the agent the payload reports. The prompt improver is exempt from the agent allowlist -
it rewrites a draft and never answers as a persona.

### Module map

| File | What it owns |
|--|--|
| `chat-conversation.tsx` | `<chat-conversation/>` as `TLChatConversation`: transcript plus prompt field over the chat's store, and the toolbar of its `<commands>` in the head of the transcript |
| `chat-dock.tsx` | `<chat-dock/>` as `TLChatDock`: the floating launcher and the panel it opens over the page |
| `presentation-pane.tsx` | `<presentation-pane/>` as `TLPresentationPane`: the history of what stands on its channel, the top step shown, with back and close |
| `chat-sources.tsx` | `<chat-sources/>` as `TLChatSources`: the session-wide list of cited sources, for a section of a `<side-rail>` |
| `chat-store.ts` | The chat's state, one store per `chat-id`: bootstrap, capabilities, sessions, turn settings, transcript, run and the attach to the window's event stream |
| `composer.tsx` | Input, plus menu, model picker, attachments, drag and drop, improve-prompt |
| `thread.tsx` | Transcript: user bubbles, answers, run meta line, trace panels |
| `subagent-card.tsx` | One card per background subagent at the tool call that started it, from the live events (`subagentLive`) or, after a reload, from the lead's recorded `SubagentPart`; `cardsForTurn` decides which cards a turn shows |
| `context-usage.tsx` | The context ring and its popover, plus how each number is derived |
| `prompts.tsx` | Approval and question cards above the composer, with their countdowns |
| `markdown.tsx` / `highlight.ts` | Markdown rendering, sanitizing, code blocks |
| `api.ts` | The commands and data keys of the chat controls for opening, deleting and reading a chat (`openSession`, `deleteSession`, `loadSession`, `loadSubagentTranscript`), for sending (`sendMessage`, `editSend`, `regenerate`, `uploadAttachments`, `removeAttachment`) and for the run's gestures (`respond`, `cancelRun`, `setApprovalMode`, `rateAnswer`, `startPromptImprovement`, `cancelPromptImprovement`, `sendSubagentMessage`, `cancelSubagent`, the `prompt_improved` frame), reading the capabilities, the chat list and the attached files out of the control state, plus the persisted-transcript to message mapping |
| `attachments.ts` | Which dropped, pasted or picked files the server would take (type, size, count), checked before the upload |
| `unsent.ts` | The `sessionStorage` slot in which the text of a send the page was reloaded over waits for the reloaded composer (#574). The reload itself is the bridge's: its command channel loads a page whose UI the server no longer knows again (`reloadForStaleUi` in `command-channel.ts`) |
| `reducer.ts` / `store.ts` / `types.ts` | Transcript reducer, the `runStore` snapshot a host may subscribe to, shared types |
| `extensions.ts` / `registry.ts` | The types of the host seams and the registries a host fills them through |
| `progress-model.ts` | The agent's task list as the run state carries it |
| `theme-icon.tsx` | A `ThemeImage` (`css:bi bi-…`) as an element, for configured icons |
| `working-indicator.tsx` | What stands in the answer's place while the agent works: the dots, or the element's `working-indicator` |
| `token-input.tsx` / `reference.ts` | The composer's inline reference tokens and how they travel |
| `sources.tsx` | The cited-source model, the `Quellen` section and the per-answer pill |

## Extending the chat

The browser components are registered in `ui/src/main.tsx`; `ui/src/index.ts` is the seams bundle
`tl-chat-seams.js`, which exports them, the building blocks of the conversation (`Composer`,
`Thread`, `PromptStack`, `Markdown`, ...) and the registries below. A host that renders the
conversation itself registers its own element and points `<chat-conversation react-element="MyChat"/>`
at it. The control is still the only binder of session, tracker and prompt-improver; the browser
component still talks to the chat controls alone.

### Seams

A host fills a seam by registering a value under a name, and the page names it on
`<chat-conversation>`:

```js
import { registerMenuItem } from 'tl-chat-seams'

registerMenuItem('my-item', { id: 'my-item', label: '…', action: (composer) => composer.setDraft('…') })
```

```xml
<chat-conversation menu-items="my-item"/>
```

| Registry (`registry.ts`) | Named by | What it adds |
|--|--|--|
| `registerReferenceSource` | `reference-source` | What typing **@** offers (`ReferenceCandidate`). A `<mention>` wins over it. |
| `registerMenuItem` | `menu-items` | Rows of the `+` menu; their `action` gets the `ComposerApi`. |
| `registerAnnotationRenderer` | `citation-renderer` | The popover of a citation chip, a `CitationRender` `(marker, citation, message) => ReactNode`; the default shows kind, label and a link. |
| `registerMessageAction` | `<actions host="…"/>` | Buttons under a finished answer, beside copy / rate / regenerate. |

What the agent presents is not a seam: it stands on a channel of the page, and a
`<presentation-pane>` shows it (see "the presentation pane" below).

### The composer's invitation

The empty field says "Frag mich etwas …", which is informal; an application whose every other
surface addresses the user with *Sie* then has one "du" on its most-read control (#420). Both paths
into the chat state their own sentence, and both feed one `Composer` prop:

| Path | How |
|--|--|
| `<chat-conversation>` | `<placeholder><en>…</en><de>…</de></placeholder>`, or the key of a text in the application's bundle. The control resolves it in the acting account's language (`ChatViewBinding.resolveText`) and pushes the words, never the key. |
| `<chat-conversation>` on several pages | `<placeholder-by>` with `<inputs>` and a `<value>` TL-Script over them, like `<prompts>`. The control evaluates it again when an input changes; an empty result leaves `<placeholder>` in force. For one chat element whose subject the page decides - "this project" while one is open, everything else while none is. |

Two rules about it:

- **It is the idle invitation only.** While the agent works the field keeps saying "Nächste
  Nachricht schreiben …", because that sentence is about the run rather than about the application,
  and a host that replaced it would be inviting the user to start what is already under way.
- **It replaces both framework variants**, the one that offers `@` included. A host that wants its
  sentence to mention `@` writes that into its own text.

Before this the only way in was CSS - a host put its own text in front of
`.token-input.is-empty::before { content: attr(data-placeholder) }` with a custom property. That was
a host reaching into a framework pseudo-element; there is no reason to do it any more.

### The working indicator

While the agent works and has written nothing yet, the answer's place holds three pulsing dots
(`PendingAnswer` in `thread.tsx`, #429). An application puts its own animation there - a logo, say -
with one attribute:

```xml
<chat-conversation working-indicator="/images/app-working.svg"/>
```

It is a `ThemeImage`, named like every other icon of the chat, in one of three forms:

| Form | Rendered as | Light / dark | Reduced motion |
|--|--|--|--|
| `/images/….svg` | **Inline** SVG, fetched once per page (on mount, not on the first send) and passed through DOMPurify's SVG profile - scripts and event handlers go, `<style>` and SMIL stay | Draws in `currentColor` (the secondary text colour) and the theme tokens (`var(--button-primary)`), so one file fits both themes | The chat's reduced-motion rule reaches into it; SMIL is paused on its first frame |
| `css:app-working` | `<i class="app-working">` | The application's stylesheet draws it and may pick an image per `[data-theme]` | The chat's reduced-motion rule |
| any other image | `<img>` | As it is | Cannot be stopped from outside - prefer the other two |

- **Size**: the height of the first text line, `--chat-working-indicator-size` (1.5rem). The token is
  read with a fallback, not declared on the chat's roots, so `:root { --chat-working-indicator-size: … }`
  works.
- **An image travels with the context path** (`ChatConversationControl.workingIndicator`). Its encoded
  form is the path inside the application, which a browser resolves against the server root - wrong
  for every application not deployed there, which is also why `ThemeIcon`'s image branch is not used.
- **An inline SVG's `<style>` applies to the whole page**: prefix its classes. A file that fails to
  load, or is not one `<svg>` (a login page answering the request), keeps the dots with one console
  warning.
- **Decorative**, like the dots (`aria-hidden`): the "Arbeitet …" line and the thread's live region say
  what a screen reader needs, whatever is configured.
- **Only the answer's place.** The "Arbeitet …" / "… gearbeitet" line, the streaming caret and the
  dock launcher's busy mark are unchanged.
- The option is on `<chat-conversation>`, so a `<chat-dock>` gets it on the conversation nested in it.

`tl-ai-demo`'s Chat page shows `images/tl-ai-demo-working.svg`, a neutral example; its dock embeds the
same page and shows it too. `working-indicator.test.tsx` and `TestChatWorkingIndicator` pin both.

While the latest step of a running run is thinking, the collapsed run line says what the model is
thinking about - "Nachdenken: " and the start of the newest paragraph of the thinking summary - in
place of "Arbeitet …" (#602). A model can think for minutes before its next step: Claude Opus 5.5
thought for almost four on an intake of five documents, and a line that only said "Arbeitet …" for
that long read as a hang. The summary exists only because `AnthropicRequestBuilder` asks for
`display: "summarized"`; the API default on the models since Opus 4.7 streams thinking blocks with no
text. While the latest step is a running tool call, the line says `<tool>: <preview>` instead (#30143) -
the preview is the call's `description` argument (Claude Code's Bash sends one), else its first
argument, cut to the same one line; a collapsed tool entry of the trace shows the same preview.

Every model request of a run opens an empty answer message (`turn_started`). While the run is going,
that empty message is not the answer: `lastAnswer` keeps the newest text that has content as the
answer until a newer one has some (#30143), so a run of many requests keeps what the agent last said on
screen, and the working indicator stands only while the run has written nothing at all.
`thread.test.ts` and `thread-trace.test.tsx` pin both.

### The composer's reference tokens

A host's quoted passage is an **inline token inside the text**, not a chip above it: "shorten
[this] and expand [that]" needs two references at two positions in one message, and a chip cannot
say which is which. The token is `{kind, id, label, location, text}`; the caret steps over it,
Backspace takes it whole, and a screen reader reads it as one named thing.

On send it serialises **at its position** to `<quote label="…" ref="…" location="…" kind="…">the
quoted text</quote>`, so the model reads the reference inside the sentence that is about it.
`location` and `kind` are written only when the host set them. The thread renders a sent message
through the same parser, so the reference reads back as the token the user inserted, and the inline
editor edits it as one.

Two ways in: the `reference` and `quote` channels of `<chat-conversation>`, which a command of
another page writes - the token is inserted at the caret, restoring where the user last stood,
because by then the editor has lost the selection - and typing **@** in the composer, which offers
the `<mention>` rows or what the registered `reference-source` returns. There is no
prefix form and no chip list.

**/** types a slash and opens nothing. It is reserved for skills, the way it is in every chat
product; the agent picker lives in the **+** menu, which is where it was reachable all along
(#416). The skill picker itself comes with the skills work.

**Is `ref` resolvable?** For a model object - what `@` and the `reference`/`quote` channels insert
from an object - yes (#616): `ref` is the `TypeName:id` every tool reads (`ReadFilter.idOf`) and
`kind` the qualified type, so the agent answers from label and kind where they suffice and reads the
object with `getInstance(ref)`, a core tool of Albert, where they do not. The `<reference>` prompt
fragment (`tl-ai-core.conf.config.xml`) says so. A host that writes an id of its own as text is the
other case: tl-ai ships no tool that takes it, so **such a host must put everything the model needs
into the element's text**, or give its agent a tool that accepts that id.

### What an answer's citations look like

The answer cites with links in its text - `[Sommerfest](tl:GenericObject:11047)`,
`[laut Programm](https://...)` - and calls no tool for it (#617). The system writes the reading
list: every read tool registers what it read on the run's `ReadingList`, and the validator turns a
link to a source on that list into a footnote and drops the link of anything else. A sub-agent's
sources join the lead's list when its result is delivered.

A footnote is a small grey number after the link's text. Hovering (or focusing) it shows the
source's kind icon and label, where it comes from and which one it is (the page's path, or the
record's id), and what the source says: prose with the cited passage highlighted - the link's title,
kept only when what the tool read really contains it - or, for a kind declared `passage="false"`,
the record's fields as a field list. A web page also offers its **archived copy**
(`/archived-page/{handle}`), kept when the agent read it, so the citation still shows what the page
said after it changed. Clicking opens the source: the renderer named by `citation-renderer` decides that for a
host kind (returning nothing leaves the web default, which opens the page).

**Kinds are declared, not invented.** The framework knows `web` and `object`; a host declares its
own kinds once through the `CitationKinds` service (name, `types` its objects belong to, label,
whether a passage applies). The model never names a kind - the reading tool decides it - and the
validator drops a citation of an undeclared kind at DEBUG. A host that cites another way than with
links registers a `CitationMarkerSyntax`. See the developer article `ai/memory-and-citations` (citations).

Under an answer sits a collapsed `Quellen · n zitiert` pill with that answer's sources. The rail's
`Quellen` section is the session-wide list: every source cited in this chat, with the marker numbers
that point at it - a number scrolls to its answer, the row opens the source, and a filter appears
once the list is long.

Its **badge** is not the length of that list: it counts what the *newest answer* cited
(`latestAnswerSources`). A badge over a list that only grows sat on the Quellen icon for the rest of
the conversation and could not be dismissed (#495); an answer that cited nothing now carries none.

Every badge around the chat - beside a section title, on a strip icon, on the transcript's rail
command and on the dock's launcher - is drawn through the part `Badge` (`StateBadge`,
`labelWithBadge` of the bridge). A `<section>` of the `<side-rail>` is declared `WithBadge` (`badge`,
`badge-role`, `badge-label`), and the rail writes what its sections count beyond what the user has
seen to its `badge-channel` (see [the side rail](../com.top_logic.layout.view/src/main/java/META-INF/tl-docs/view-layer/side-rail.md)).
In `albert.view.xml` that channel is `railBadge`: the rail command of `<chat-conversation><commands>`
shows it with `badge="railBadge"`, and a `<chat-dock badge="…">` around the page shows it on its
launcher (`ChatDockControl.STATE_BADGE`, state `badge`) when the shell binds its own channel over
`railBadge`. It binds `chatShown`, the rail's `shown`, to the dock's `open-channel`, so nothing
counts as seen while the dock is closed
(`tl-ai-demo/src/main/webapp/WEB-INF/views/app.view.xml`).

### Where the chat's state lives

Everything the conversation *is* lives in `chat-store.ts`, not in a component: one `ChatStore` per
`chat-id`, held in a module-scoped map and subscribed with `useSyncExternalStore` through
`useChat(chatId, boot)`. The whole chat bundle is a single ES module instance in the browser
(`tl-chat-ui.js` and `tl-chat-seams.js` are shims over one `tl-chat-shared.js`), so that map is one
object for every chat element on the page - which is what lets several view elements show one
conversation.

| | |
|--|--|
| What the store owns | The bootstrap, the capabilities, the session list and the open session, the turn settings, the transcript (through `chatReducer`), the run, and the chat's one attach to the window's event stream |
| What a chat element keeps | Its own geometry and the UI state only it shows, such as an open popover |
| Lifetime | The first element to subscribe opens the stream, the last to leave closes it and drops the chat, so a fresh mount starts fresh |

#### What survives a reload, and who decides it

A browser reload does **not** rebuild the control tree: `ViewServlet` re-renders the tree the window
already holds, deliberately, so that a table keeps its selection. A control constructor therefore
runs once per window, not once per page load. Everything the chat wants a reload to restore has to
be read at **render** time, and every chat control reads who is signed in, the language and the
auto-approve choice in `onBeforeWrite` (`ChatViewBinding.writeRenderState`).

The layout around the chat is not the chat's: whether the side rail is open, which of its sections
are expanded and the width the user dragged are stored by the engine's `<side-rail>` per user under
its personalization key, not per conversation, and the sizes of a split panel likewise. The
configured `open` of the rail and of a section applies only where the user has decided nothing.

Run state is recovered over the stream rather than the control state: the
chat's stream replays nothing, so the server sends a `run_state` frame when the stream is attached,
and again on `refreshRunState`, which the store sends once it has restored or selected a session -
the tasks of the open run, whether it is still processing, the approvals and questions waiting for
this user and the subagents. A reload in mid-run therefore comes back with the
Fortschritt list, a busy composer and the open card - the approval, or the question the agent asked,
with the options and the single/multi shape it was asked in.

`chat-id` is how elements find each other, and **`<chat>` is where it is written**. A page puts its
chat elements inside one `<chat chat-id="...">`; each of them inherits that id through the view
context (`ChatScope`, `ChatScopeElement`) and carries no `chat-id` of its own. That is the normal
form, because the alternative - the same string repeated on every element - makes the strongest
coupling on the page the least visible thing about it: a typo in one attribute opens a second, empty
conversation and nothing in the product says so.

```xml
<chat chat-id="chat">
    <split-panel id="chatSplit" orientation="horizontal" resizable="true">
        <panes>
            <pane id="chatListPane" size="264" unit="px">
                <side-rail id="chatList" drawer-below="44" width="264">
                    <section name="chats" header="false" dismiss-on="chatSession" …>
                        <data-list selection="chatSession" …/>  <!-- joins by channel, not by chat-id -->
                    </section>
                </side-rail>
            </pane>
            <pane id="conversationPane" size="100" unit="%">
                <panel fill="true"><chat-conversation input="chatSession"/></panel>
            </pane>
            <pane id="railPane" size="300" unit="px">
                <side-rail id="rail" side="right" open-channel="railOpen" badge-channel="railBadge">…</side-rail>
            </pane>
        </panes>
    </split-panel>
</chat>
```

`<chat>` is a scope and nothing else: it holds **exactly one** element and returns that element's
control unchanged, created in the scoped context - no control, no DOM node, no layout options. A
`<chat>` with several children is a configuration error that names a layout element to put them in.
The layout is the layout element's: the shipped views use a `<split-panel>`, whose widths the user
drags and which remembers them per user; a `<stack>` does where nothing is to be dragged. Because
the `<chat>` has no element of its own, the fill contract (`useFillHost`) passes through it: a
`<panel fill="true">` inside fills as if the `<chat>` were not there.

That panel is there for the height alone and carries no title. Its header, which the engine draws
anyway (padding, a tinted band and a border line), is hidden by a rule in `styles.css` for as long
as it holds no title, no title content and no command (#589). A host that gives the panel a title
or a command keeps its header.

`<chat-dock>` is the one element that **repeats** the id rather than inheriting it, because it
*contains* the `<chat>` instead of sitting inside one and a scope only reaches descendants:

```xml
<chat-dock chat-id="assistant" icon="css:bi bi-stars">
    <label><en>Assistant</en><de>Assistent</de></label>
    <chat chat-id="assistant"><split-panel …>…</split-panel></chat>
</chat-dock>
```

The two must agree. A mismatch is not silent: `ChatJoinLog` warns about a window whose chat ids do
not line up, and a dock joining a chat nobody else is on is exactly that shape - the dock's launcher
would then count news from a conversation it does not contain.

`chat-id` on a child element is the **override**, for the rare page that holds two conversations;
it is nullable, and left empty an element outside every `<chat>` joins `default`. A browser
element whose state names no chat id falls back to its control id (`chatIdOf`), so two chats in one
window stay two chats - a fixed key would silently merge their transcripts (`chat-store.test.ts`,
"gives two conversations in one window two transcripts").

**The join is logged.** `ChatJoinLog` tallies, per render, which chat id each element joined and
writes one INFO line for it. When a window holds more than one chat id and one of them has a single
element, it adds a WARN naming that element - the shape a typo produces. A page with one chat id
never warns, however many elements are on it.

`<chat-conversation>` is the transcript and the prompt field as one element - the transcript and
the input almost always sit together, so they are one placeable box, while the session list and the
rail stay separate because those genuinely want independent placement. It honours its configuration
flags as render guards on the existing `Thread` and `Composer` props.

#### The nine channels of `<chat-conversation>`

| Channel | Direction | Value |
|--|--|--|
| `input` | two-way | The open `tl.ai.execution:AgentSession`, as the object. Empty means a fresh conversation, so "new chat" is the list clearing it. |
| `subject` | in | The object a conversation *created here* is about, written onto it as `AgentSession#subject` - see below. |
| `busy` | out | `Boolean`, `true` while the agent works. |
| `agent` | two-way | The agent's **name** (`String`). |
| `model` | two-way | The model's **name** (`String`). |
| `effort` | two-way | The effort level (`String`), one of the levels the selected model offers; empty for none. |
| `mcp-servers` | two-way | The attached servers' **names**, a `Collection<String>` (one name on its own is read as a set of one). |
| `draft` | two-way | The text standing unsent in the composer (`String`, or a `ResKey` on the way in). |
| `reference` | in | A model object, inserted into the draft as one atomic reference token. |
| `quote` | in | A marked passage *of* an object, as the token itself: `{id, text}` and optionally `{label, location, kind}`. |

`present` is not an eleventh: it is a *list* of channels this element opens to the agent rather than
a channel the element itself reads or writes - see "the agent writes a channel" below.

##### `subject` - what a conversation is about (#508)

A host whose page is about one object at a time - a project, an invoice, a contract - wants one
conversation per object, and before #508 it had to remember which conversation that was somewhere
of its own. `tl.ai.execution:AgentSession#subject` is where that now stands, and this channel is
what writes it:

```xml
<chat-conversation input="chatSession" subject="project"/>
```

| | |
|--|--|
| When | Once, by the command that brings the conversation into being - the first `send`, which creates the chat before its message goes. The element offers its channel to its chat (`ChatViewBinding.offerSubject`) while it is displayed, and the send reads it there (`ChatViewBinding.subject()`), so the subject stands before the first run asks the `ConversationBackendResolver`s. A conversation started on the server - `chatStart(chatId, subject, text)`, `ChatStart` - is created with its subject the same way. |
| Never | On the way in. Which conversation is shown stays `input`'s business, and which ones the user may pick stays the `<data-list>`'s. A conversation opened beside another object is not moved there, and one started while the channel was empty is not given a subject later (other than the one a `subject-factory` creates with its first message, below) - `AgentExecutionStore.setSubject` refuses a session that already has one, and no path backfills the rest. |
| What counts | One model object, or a one-row selection. Several objects name no single subject, and nothing on the channel means the page was showing nothing; both stamp nothing (`ChatViewBinding.singleObject`). |
| Scoping the list | The `<data-list>`'s own `rows`, by navigating back - which is the point of a reference rather than a remembered id (see below). `referers` is not a security boundary, so the owner filter stays. |
| A deleted subject | Clears the reference (the default deletion policy): the conversation outlives the object it was about, and it drops out of that object's list. **But navigating back from the deleted object itself throws** - `TLObject.tReferers`, which TL-Script's `referers` calls, refuses a lookup from a deleted object, and TL-Script has no "is this still there" to guard with. So the expression below answers while the channel is empty and while it holds a live object, and fails in the window where it still holds one that was deleted meanwhile. That window is not this feature's: a `<form>` or a `<table>` over the same channel is in the same position, and a page that must survive it asks the sessions (`all(...).filter(s -> $s.get(...) == $x)`) instead of the object. `TestSessionSubject` pins both halves. |

```xml
<rows>project -> ifElse(isEmpty($project), list(),
    $project.referers(`tl.ai.execution:AgentSession#subject`)
        .filter(s -> $s.get(`tl.ai.execution:AgentSession#owner`) == currentUser())
        .sort((s -> $s.get(`tl.ai.execution:AgentSession#lastUpdated`)).comparator().desc()))</rows>
```

**A subject that comes into being with the first message** - `subject-factory` and `subject-type`
(`ChatConversationElement.Config`; `ChatSubjectFactory`, offered to the chat by
`ChatViewBinding.offerSubjectFactory` while the element is displayed):

```xml
<chat-conversation input="chatSession" subject="task"
    subject-type="my.module:Task"
    subject-factory="text -> attachments -> {
        task = new(`my.module:Task`);
        $task.set(`my.module:Task#name`, $text);
        $task;
    }"/>
```

| | |
|--|--|
| When | Only in the `send` that creates a conversation, and only while `subject` holds no object (an object there wins). The function gets the text and the files of the message as a list of `BinaryData` and returns the subject - the created object as its last expression, since `set` returns nothing. `subject-type` is required with it (`ChatConversationElement.missingSubjectType`). |
| Before it | Everything decidable without a session (`ChatControl.firstSendRefusal`), on the features of `subject-type` (`ConversationBackendService.backendFeaturesFor`): a resolver answers the type - the files are checked against its features; none does - the agent policy (`SendRefusal.forRun`). Then the room for the run. A refusal creates nothing. |
| It | Its own transaction, as the signed-in account (the model-based security decides what it may create). A `TopLogicException` refuses the message with its text (`forbidden`); `null` or an object of another type refuses it with `ERROR_SUBJECT_NOT_OF_TYPE__TYPE`. Rolled back either way; the text stays in the composer. |
| After it | The session is created about the object, `subject` shows it (`ChatSubjectFactory.show`). Should the session not come into being, the object is deleted again (`discard`). A send that fails afterwards keeps the session: the answer names it (`sessionId` beside a status other than `processing`), the files move to its draft (`ChatAttachments.moveTo`), and `chat-store` opens it with the text in the composer (`openKeptChat`). |
| Empty chat | `subjectFeatures` are those of `subject-type` while `subject` is empty. |

The shipped page `tl-chat/albert.view.xml` configures both halves: the conversation stamps what it
creates with the object the presentation pane is showing, and the session list on the left offers that
object's chats while one is open, and every chat of the account while none is.

The four turn settings carry **names**, not model objects, and that is a fact about this product
rather than a shortcut: only an agent an installation authored in the knowledge base is a
`tl.ai.core:Agent` at all - the agents tl-ai ships, every model and every MCP server are
configuration - while a name is what the `send` command, `AgentFactory` and the security policy are
written in. On the way *in* a `tl.ai.core:Agent` record is read for its `name`, so a `<data-list>`
of authored agents feeds the `agent` channel; on the way *out* the name is written, so such a list
is not highlighted back. `ChatTurnChannels` is where that translation and the refusal below live.

Two-way means the same thing for all five: the composer writes the user's choice and **follows a
write** from outside. It follows a write, not the value standing on the channel - the composer
changes these itself (restoring a session brings back its effort, picking an agent brings its model
and its servers) and the channel learns of that one round trip later, so acting on the standing
value would undo every such change. A host that pre-selects an agent for its page therefore writes
the channel once and the composer keeps it, including across the capabilities that arrive with the
conversation's state afterwards.

A channel is a second way past the composer's own filtering, so a write is refused exactly where a
send naming the same value is: `ChatTurnChannels` asks `AgentFactory.runRefusal`, which is the
question the `send` command pre-checks a send with (`SendRefusal`), plus "does this application have such an agent at
all" (the send's `notFound`). A refused write is logged at DEBUG, is *not* applied, and the composer's
own setting is written back onto the channel - so the channel never claims a setting the composer is
not showing, and the next send cannot be answered 403 for something the user never chose.
`TestChatTurnChannels` pins it against the shipped defaults, beside `TestChatRunAccess`.

#### The agent writes a channel: `present`

`present` is the only way an agent reaches the page it is on, and the page decides how much of
itself to offer:

```xml
<chat-conversation input="chatSession" present="paneObject"/>
```

| | |
|--|--|
| The tool | `present(query, ids, as, chart, title, channel)` - `PresentTool` in tl-ai-core holds the logic, `com.top_logic.ai.chat.tools.ChatPresentTool` delivers to the page; registered as `present.present` from `tl-chat.conf.config.xml`, so every host of this module has it. Classified READ / `interaction`: it reads business data and changes page state, not the knowledge base, so there is nothing to approve - and the approval card would sit in the very page it is filling. |
| What | A TL-Script `query` evaluating to the objects - "show me all events as a table" is `present(query: all(`demo.events:Event`), as: TABLE)`, one call, with no listing and fetching first (#616) - or the `ids` of objects the model already holds. `as` is `TABLE`, `CHART` (with `chart: {kind, label, value}`, two TL-Script functions of one object) or `FORM`; omitted, one object is a form and several are a table. The answer lists the objects as JSON identities `{"_tid", "_type", "_display"}` (a chart's points as `{"object": <identity>, "label", "value"}`), cut at 50 as `{"items", "total"}`, so the model can talk about them without reading them again. |
| Data (#655) | A query may compute data instead of objects, in the format of a chart component's `data` script: a whole Chart.js configuration `{'type', 'data': {'labels', 'datasets'}, 'options'}` - stacked, a line over bars, two axes - or its short forms, `{label: number}` (what `groupBy(key, g -> $g.size())` returns), `{label: {series: number}}` and a list of records. `ChartData` (tl-ai-core) turns every form into the one configuration, checks it (a known type, numbers only, objects only in `metadata` and only readable ones) and cuts it to 200 points per series and 20 series, saying so in the answer. Shown as `CHART` (the default for data) or as `TABLE` - a label column and a column per series, `ComputedColumn`s over plain rows. A point of data has no object, so a click opens nothing. |
| The value | A form's object stands on the channel as itself - what a `<form>`, a subject or a context fact reading "the open object" understands. A table or a chart stands there as the `Presentation`, which a `<presentation-pane>` renders and `ChatSelection` states to the agent. |
| The rights | A query is evaluated without being able to modify anything, and its objects are reduced to the ones the **acting user** may read (`ReadFilter`), the way `filterSecurity` would. Every id resolves through `TidResolver` with the same rights, all or nothing: an object they may not read is refused exactly like one that does not exist. A chart's functions are tried on every object before anything is shown, so a failing function is the model's error to correct, not an empty chart. |
| The allow list | **Deny by default.** `ChatConversationControl` resolves each `present="…"` entry and registers it on the `ChatViewBinding` of its (window, chat-id); the tool looks the name up across the bindings of that *window* - which is what a run knows about itself, the chat id being something the tool layer is never told. **So a window's allow list is the union of its chats' lists.** A window that offers exactly one channel is written without the model naming it; one that offers several makes the model choose and names them. **A channel counts while its element is displayed** (#649): the registration follows the control's attach and detach, so a page of the shell the user left, a classic tab that is not selected and the React page a browser tab showed before the classic UI offer nothing - before, a run presented into such a page and told the user they saw a table they could not see. |
| Offered or not | The run is offered `present` only while its window displays such a channel (`ChatPresentTool.noPlaceToShow`, through the generic `Tool.unavailableReason` that `AgentLoop` asks before every request). A run without one - a page that names none, no window at all - is not sent the tool, so it answers "show me" in text instead of with a call that is refused. Albert's prompt states the rule conditionally ("you have `present` only where the page has a place ... without it, `query` and a Markdown table"), so the page's `present` attribute stays the one switch: a second, page-dependent prompt section would be a second source of truth, and a second reason for the prompt to change between turns. A call that names the tool anyway is answered with the same sentence. |
| The window | The value is written in the window's sub-session, **within its interaction**, through `ChatWindowService.inWindow` - a `ReactControl`'s state map is a plain `HashMap`. A run with no window (headless, TL-Script, the scheduler) says "no page is attached" and does nothing; that is a fact about the run, and is deliberately a different sentence from "this page allows nothing". |

**What a page needs, all three or none:** the channel, `present="…"` on the `<chat-conversation>`, and
an element that renders the channel - usually `<presentation-pane input="…">` in a section of a
`<side-rail>`, which shows any type.
Without `present` the agent is not offered the tool; without the rendering element it shows a table on
a channel nothing displays and says the user sees it. The classic Albert tab's default view
(`tl-chat-classic/chat.view.xml`) carries all three, plus `<selection channel="…">` so the agent knows
which object the pane shows; an application's own copy of that view has to keep them.

`TestPresentTool` drives the real path - a live browser window, a registered channel, a listener
standing in for the page - and builds a real `ChatConversationControl` to pin that its channel counts
only while it is attached. The eval has the same tool without a page (`EvalPresentTool`), and grades
what a run showed (`<presented>`, tl-ai-eval).

#### The agent runs a page command: `runPageCommand`

The second way an agent reaches its page, deny by default like `present`: `<chat-conversation><agent-commands>`
lists `<agent-command name="…" description="…">` entries, each an ordinary `<generic-command>` whose chain opens a
dialog or shows a view. `ChatConversationControl` builds each in its view context (`ViewCommandModel.forCommand`)
and offers it as a `PageCommand` on its `ChatViewBinding` while attached; `ChatCommandTool`
(`page-commands.pageCommands`, `page-commands.runPageCommand`, READ / `interaction`) finds it by name across the
window's bindings of the run's account and runs `ViewCommandModel.execute(input, parameterValues)` in
`ChatWindowService.inWindow`, so the command's executability and access decide exactly as for a click. A command's
`<parameters>` (a `ConfigurationDescriptorConfig`, built once as `DeclaredParameters` of `tl-ai-core`) are listed as
JSON schema and checked by `DeclaredParameters.bind` before the command runs; the chain sees them as arguments of
their names. The article `ai/chat-ui` ("Page commands an agent
may run") has the syntax; `TestPageCommandTool` drives a live window with a dialog manager standing in for the
browser.

#### The draft as a channel (#440)

A page button could not put text into the chat, so an application built a view action that found
the chat control, reached the browser and typed into the composer with a DOM hack (issue #414). One element changing another element's editable state is what a channel is for, and
`draft` is that channel:

```xml
<generic-command image="css:bi bi-chat-left-text" inputs="invoice">
    <label><en>Ask about this</en><de>Dazu fragen</de></label>
    <execute-script>
        <function><![CDATA[{"en": "Please explain …", "de": "Bitte erkläre …"}.internationalize()]]></function>
        <with-result>
            <write-channel name="chatDraft"/>
        </with-result>
    </execute-script>
</generic-command>
```

Two existing engine actions, no new class. What to know about it:

- **Out** the composer reports its text on a typing pause (300 ms, `createDraftReporter` in
  `ui/src/draft.ts`), when the field loses the focus, and immediately before a send. A channel write
  is a round trip and a keystroke is not worth one; blur and send do not wait, because those are the
  moments a host acts on what it reads.
- **In** a write replaces the whole draft, focuses the field and puts the caret behind the text. The
  user still presses send - there is no `send` mode, deliberately: reading the sentence before it
  goes is worth one keystroke.
- **The draft belongs to the chat it was written in** (#30552), not to the place that shows the
  chat. The store keeps one draft per chat (`ChatStore.drafts`, keyed by the session id, the chat the
  next send creates under `NEW_CONVERSATION`), filled from what the composer reports (`noteDraft`).
  When the place shows another chat - a pick in the list, a selection written to `input`, "new
  chat", the chat on screen deleted - the store puts that chat's draft into the composer with
  `ComposerApi.restoreDraft`, which unlike `setDraft` takes no focus: a switch the user made in the
  list or a host made by a command must not pull the caret into the field. The composer reports the
  restored text like typed text, so `draft` always carries the draft of the chat on screen, and a
  write to `draft` applies to that chat alone. The first send of a new chat hands its draft to the
  chat it creates; a first send that is refused leaves it with the new chat. A composer that mounts
  again (a dock) is given the draft of the chat on screen. The drafts are not stored - not on the
  server, not in `sessionStorage` -: they live as long as the store, i.e. as long as the chat is on
  the page; the text of a send the page is reloaded over is what `keepUnsent` (`unsent.ts`) keeps.
  Attachments follow the same rule on the server, see "Attachments" above.
- A write may be a `ResKey`, because `{"en": …, "de": …}.internationalize()` is the obvious way to
  build a sentence in TL-Script; the control resolves it in the user's language
  (`ChatQuotes.text`). `null` - what an unwritten channel holds - says nothing and leaves the
  composer alone; the empty string clears the draft.
- It follows a **write**, not the standing value, the same way the turn settings do, and for the
  same reason. The echo is recognised in the browser: what this element last published is
  remembered, so a report of "Was" arriving back two keystrokes later does not put the composer back
  two keystrokes. The one place the standing value *is* applied is `onAttach` - a host may have
  written the draft while this element was on another page of the shell and its browser component
  was gone.
- `reference` is the "quote this" half: a model object written to it becomes one atomic reference
  token at the caret, the same token the `@` picker inserts, so it serialises to the same
  `<quote/>` on send. The token carries the object's label and its stable id, and the quoted text a
  nested `<mention>`'s `text` expression computes - without a `<mention>` the label is the quoted
  text too, because the element has nothing else to say about the object. It is read only: the
  tokens are part of the draft `draft` already carries.
- `quote` is the half `reference` cannot serve (#509): **a passage of an object**, which only the
  viewer that showed it can name. `reference` takes the object, whose quoted text is everything the
  element knows about it - the section, when what the reader marked was one paragraph of it. So
  `quote` takes the token itself, as a map:

  ```xml
  <chat-conversation quote="chatQuote"/>
  ```

  ```
  {"id": $section, "location": "02 Kontext", "text": $marked}
  ```

  Only `id` and `text` have to be there - there is nothing to quote without the passage, and nothing
  for a host tool to resolve without the object. `id` is normally the **model object itself**, not a
  printed identifier: an application has no way of spelling the stable id the `@` menu uses, so the
  element fills the kind, the id and the label from the object (a `<mention>` having its say in the
  label) and the write only names the passage. Whatever the map does say wins. Fields may be
  `ResKey`s, resolved like a draft's text. A map with no `text` inserts nothing, and so does a bare
  object: that is `reference`'s job, and quoting the whole of a section back is the one thing the
  agent's instructions tell it not to do. The rule is `ChatQuotes.token`; the inserted token is the
  same one either way, so it serialises to the same `<quote/>`.
- A channel notifies nobody when it is written the value it already holds, so writing the **same**
  object to `reference` twice in a row inserts one token. (A, B, A does insert three: the control
  numbers its writes so the browser can tell the third from the first.) Two *passages* of one object
  are two different maps, so they are two writes and both insert - which is the case `quote` exists
  for.
- Two channels, two state slots (`referenceWrite`, `quoteWrite`) and two counters, deliberately: one
  slot fed by both would lose a token whenever a page cleared one channel in the same request in
  which it wrote the other. The browser parses both with the one `tokenWrite` in
  `chat-conversation.tsx` and hands both to the same `insertReference`.

`TestChatConversationConfig` holds the options and the `ChatQuotes` rule, `draft.test.ts` the
debounce, the `chat-store.test.ts` "composer seam" block the two calls a write turns into, its "a
draft per chat" block the drafts per chat, and `chat-conversation-draft.test.tsx` the switch in a
mounted element (editor text, channel reports, focus).

#### Writing a channel across a view boundary

The dock case of #440 - a button on one page writing the draft of a chat that lives in another view -
comes down to two questions about the engine. Both are answered, with the code:

1. **Can a nested `<view-ref>` view write a channel of the enclosing view?** Yes, but only through
   an explicit `<bind>`. `ReferenceElement.createControl` calls `parentContext.withIsolatedChannels()`
   (`ReferenceElement.java:118`), which is a `DefaultViewContext` with a **fresh** channel map
   (`DefaultViewContext.java:207-211`), so `resolveChannel` in the child cannot find a parent channel
   by name and throws "Unknown channel" (`DefaultViewContext.java:228-236`). Each `<bind channel="x"
   to="y"/>` then registers the *parent's* channel instance under the child's name
   (`ReferenceElement.java:121-124`), and `ViewElement.createControl` skips creating a local one for
   a name that is already there (`ViewElement.java:139-142`) - so the child view may declare the
   channel for its standalone case and still get the parent's instance inside the shell. Both sides
   hold the same object, so the write travels in either direction.
2. **Can `<app-shell>`'s view declare channels that its pages see?** Directly, yes: `AppShellElement`
   never isolates - its header, content and footer contexts come from `withErrorSink` / `withScope` /
   `withContextMenuOpener` / `withChildSlotPath` (`AppShellElement.java:144-163`), each of which
   passes the **same** `_channels` map on, as does `childContext` (`DefaultViewContext.java:104-109`).
   `<sidebar>` is no different (`SidebarElement.java:441`). So an element written straight into the
   shell's own view - the `<chat-dock>` in the footer, for instance - shares the shell's channels with
   everything else in that file. What does *not* see them is a page, because a page is reached through
   `<view-ref>`, and that is question 1 again.

The demo shows it on one channel. `app.view.xml` declares `chatDraft`, and the chat page (in the
sidebar and in the dock, which embeds the same page) and the Events page each get it through one
`<bind>` on their `<view-ref>`. The Events page's "Dazu fragen" then reaches both: open the dock on that page and the
question is in its composer; switch to the Chat page instead and it is in that one's.

Two conversations on one draft channel work here only because the two are **never mounted at once** -
a dock unmounts its content while a `<chat-conversation/>` of its chat id is on the page (see
`chat-dock.tsx`). A composer holds its own text and reports it, so two that are mounted together
would overwrite each other's drafts; that is a property of the channel, not of the dock. The one that
mounts applies what stands on the channel (`onAttach`), which is what makes the draft follow the user
from the dock to the page and back.

So there is no engine gap for the dock case: a dock in `<app-shell><footer>` reaches the shell's
channels with no binding at all, and a page reaches them with one `<bind>` per `<view-ref>`. What the
engine has no answer for is *opening* the dock from the page - that is a second channel and a dock
element that follows one, not a channel-scope question.

#### Configuring `<chat-conversation>` - easy vs advanced

An application configures this element in the design view. Its options fall into three tiers:

- **Plain toggles / channels a designer sets** - `trace` (`none`/`summary`/`detailed`, one knob that
  replaces the former trace-mode + run-summary; the user can still change it live with the composer's
  trace picker), `citations` (markers *and* the per-answer sources pill - off removes the whole
  citation surface from the transcript), the nested `<actions>` (`copy`/`rate`/`regenerate`/
  `timestamp`), the nested `<controls>` pickers, `placeholder`, `working-indicator`, `prompt-improver-agent` (an agent
  dropdown), `chat-id`, the ten channels (`input`, `subject`, `busy`, `agent`, `model`, `effort`,
  `mcp-servers`, `draft`, `reference`, `quote`), and `present` - the channels the agent may write,
  deny by default. The nested `<commands>` are ordinary view commands, drawn as a toolbar in the
  head of the transcript (`ChatConversationControl.HEADER_COMMANDS`, state `headerCommands`) - the
  shipped page's button that opens and closes the side rail is one of them, a `<generic-command>`
  that toggles the rail's `open-channel` and shows its `badge-channel` as its `badge`.
- **Points at code (name a component registered in TypeScript; do nothing unless a developer set that
  name up)** - `react-element`, `citation-renderer` (the renamed `annotations`), `<actions host>`,
  `menu-items`, and `reference-source`. Each such tooltip says so, and each carries `@Hidden`, so the
  layout editor does not offer it at all - a name a designer can only get wrong is not a designer
  option. The XML still accepts them. The engine-inherited `class` and
  `personalization-key` are hidden for the same reason (`TestHiddenElementOptions` pins the set).
- **Needs data modelling (TL-Script)** - the nested `<mention>` binding: `rows` (the objects `@` may
  mention), optional `label`/`text` expressions, `observed-types`, `limit`. Same skill as
  `<data-list>`'s `rows`; the control compiles them with `QueryExecutor`, pushes a bounded candidate
  set shaped as the browser's `ReferenceCandidate`, and the existing `@` picker filters it client-side.
  A `<mention>` wins over `reference-source`. Each mention carries the object's `TypeName:id`, which
  `getInstance` resolves; a host that wants an id of its own to mean more gives its agent such a tool
  (see the `<reference>` prompt fragment). The nested `<context>` and `<prompts>` bindings are the
  same skill again - see below.

#### What a page states about the situation: `<context>`

```xml
<chat-conversation input="chatSession">
    <context>
        <inputs><input channel="project"/></inputs>
        <fact name="Project" stable="true"><value>p -> $p.label()</value></fact>
        <fact name="Selection" stable="false"><value>p -> ifElse(isEmpty($p), "nothing", $p.label())</value></fact>
    </context>
</chat-conversation>
```

Bound the way `<data-list>` binds its rows: the `<inputs>` are channels of this page and their
current values are the **leading arguments** of every fact expression. `ChatConversationControl`
evaluates them on the server, in the acting user's context, when a message is sent or an answer
regenerated (`ChatViewBinding.facts()`); the browser carries none of it, and
`ChatProcessor.sendMessage` binds them for the run (`RunFactsBinding`), which is what also reaches a
delegate and a subagent.

**This is the seam that replaces a context header on the message text** (#406, #428): the user's
text stays exactly what the user wrote, in the bubble, in the log and in the request.

**`stable` decides where a fact is rendered, and nothing else does.**

`stable="true"` (the default) means the value holds for the whole conversation: it goes into the
`{{context}}` section of the run's environment block, in the system prompt. That block sits in front
of every request of the conversation and has to stay byte-identical, or the provider's prompt cache
misses on every turn - which is why nothing that changes may be in it.

`stable="false"` means the value changes while the user talks - the open object, the selected row,
the section being read. Such a fact goes into the per-turn `<turn-context>` block, which travels in
the turn itself as its own content block of the user message, ahead of the user's text:

```
<turn-context>
Turn: 3
Time: 14:32 (Europe/Berlin)
Open invoice: RE-2026-0042
</turn-context>
```

The turn number and the time of day are added by the framework; they change per turn by nature,
which is exactly why the environment block states the date at day granularity and no time at all.
The block is composed per run (`TurnContext`, from the same facts), attached by `AgentLoop` where the
request is built - so a compaction cannot lose the newest one - and stored on the turn
(`Turn#turnContext`), apart from the user's text: the bubble shows what the user wrote, the
transcript and the run detail show both. A page that states no per-turn fact gets no block at all.
See the developer article `ai/environment`.

A fact that throws is sent empty with a WARN rather than taking the page down - the user is in the
middle of a sentence.

#### The one line for the selection: `<selection/>`

The question a user asks a chat beside a page is almost always about what they have open, and
expressing that as `<fact>`s means knowing how this product spells an object identifier. It is one
line instead (#494):

```xml
<chat-conversation input="chatSession">
    <selection channel="selectedEvent"/>
</chat-conversation>
```

which states, as a per-turn fact of the `<turn-context>` block:

```
Selected object: {"_tid":"GenericObject:12345","_type":"demo.events:Event","_display":"KI im Mittelstand 2026"}
```

| | |
|--|--|
| `channel` | Mandatory. The channel holding what the user has selected - one object, several, or nothing. A channel of *this* view; a page of a shell hands its selection to a dock in the footer with a `<bind>` on the `<view-ref>`, exactly the way it hands over the draft. |
| `name` | What the fact is called in front of the model. Default `Selected object`; name what the page actually holds where that says more. |
| `<label>` | TL-Script naming one object. Empty uses the application's own label - which is what the user sees on their screen, and therefore the right answer to a question about it. |

**Three things, not one**, and each is load-bearing (`ChatSelection`): the label answers "which one
do I have open?", the qualified type name says what kind of thing it is, and the identifier makes it
actionable. The three form the same JSON identity every tool result writes, so the model passes its
`_tid` straight into the next tool call and `TidResolver` resolves it. A label alone leaves the agent searching for the object
the user is looking at, which is the state #494 was filed about.

Never stable, and it cannot be configured to be: a selection is the value that changes most often in
a conversation. Nothing selected says `none` rather than nothing at all, and a selection of more than
`ChatSelection.MAX_NAMED` (20) objects names that many and counts the rest - a stray Ctrl+A on a
table must not put ten thousand identifiers in front of the model. The selection is stated **before**
the `<context>` facts, because the block is read top down.

**A dock states it too, and should.** `<chat-dock>` has no `<selection>` of its own - it frames a
`<chat-conversation>`, and that is where the line goes. The shipped page `tl-chat/albert.view.xml`
states a derived channel `selected`: the object in the presentation pane, else its channel
`pageSelection`, which is empty on its own and which a dock embedding the page binds to the shell's. The dock is where a user asks about the page
*behind* it, so the page has to give its selection to a channel of the shell. There are two ways:

| | `<bind>` on the page's `<view-ref>` | `<page-selection>` in the page (#928) |
|--|--|--|
| Reaches | a page that is a `<view-ref>` of the shell view | every page of the browser window: a `<view-ref>`, a tile-stack frame, a page that a control builds |
| Direction | two-way: the page's table and the shell share one channel | one-way: the page writes, nothing writes back into the page |
| When the page leaves the screen | the value stays | the value goes, the conversation states `none` |
| Shell side | the channel | the channel, and `<chat-channels channels="..."/>` |

Use `<page-selection>`. Use `<bind>` only for a shell whose pages are all `<view-ref>`s and that
accepts a value that stays after the user leaves the page.

```xml
<!-- app.view.xml -->
<channels><channel name="pageSelection"/></channels>
...
<footer>
    <chat-channels channels="pageSelection"/>
    <chat-dock ...>
        <view-ref view="tl-chat/albert.view.xml">
            <bind channel="pageSelection" to="pageSelection"/>
        </view-ref>
    </chat-dock>
</footer>

<!-- a page, also a tile-stack frame -->
<page-selection to="pageSelection">
    <inputs>
        <input channel="selectedSection"/>
        <input channel="document"/>
    </inputs>
</page-selection>
```

- **`<chat-channels channels="a,b"/>`** goes into the shell view, once. It draws nothing. It puts the
  named shell channels into a register of the browser window (`ChatChannels`), a property of the
  window's sub-session. A new browser window has its own register. Java code of the same window gets
  a channel with `ChatChannels.channel(name)`.
- **`<page-selection to="...">`** goes into the view of a page. It draws nothing. While the page is on
  screen (attached), it writes the first input that holds one object into the shell channel `to`. A
  set of exactly one object counts as that object; nothing and several objects are skipped. When the
  page leaves the screen (another nav item, a frame over it) or is disposed, it removes its value.
- **Two pages on screen** in one window: the most recent selection wins. When one page leaves, the
  channel gets the value of the other one again, not `null`.
- **Keep the shell channel for `<page-selection>` only.** Do not bind a table's selection of a page
  to it: the table would get the objects of other pages as its selection. An application measured
  this: a list in a shared two-way channel gave a `ClassCastException`, a foreign object in a table's
  selection gave `NullPointerException`s. This is why `<page-selection>` writes one object, never a
  list.
- A dock and a page over the same conversation read the same shell channel, so both state the same
  object.

`tl-ai-demo` shows it: `app.view.xml` publishes `pageSelection`, the detail views of Events,
Invoices and Venues write it, and Venues is a tile stack whose venue frame states the venue.

**State them from every place one conversation is reachable from.** A page that declares a
`<context>` and a dock over the same conversation that declares none flips the environment block
between the turns of one chat: the stable facts *and* the `{{turn_context}}` sentence are part of
the system prompt, and a prompt that changes mid-conversation misses every provider's prompt cache.
The demo's dock embeds the page itself, so it states the same facts by construction.

**What the agent remembers about the user** (#439) rides in the same block, under its own heading,
and is read **once per conversation** for the same prompt-cache reason: `ChatProcessor.sendMessage`
asks `ChatSessionManager.getMemories` when the first message of a conversation is sent, keeps
the answer on the session state and binds it for every run of that conversation
(`UserMemoryBinding`). A memory the agent writes in turn 3 therefore shows up in the user's memory
list at once, and in the agent's block in the *next* conversation. The rail section that lists them
is an ordinary `<data-list>` over `tl.ai.execution:UserMemory` filtered by `owner == currentUser()`,
with a Forget command that calls `aiMemoryDelete`. See the developer article `ai/environment`.

#### The first questions a page offers: `<prompts>` (#529)

A new chat is the one screen with nothing to react to, and the page knows better than the chat what
is worth asking on it. Cards of `<prompts>` say so as configuration, with no hand-built buttons
or channels of their own:

```xml
<chat-conversation draft="chatDraft" input="chatSession">
    <prompts>
        <inputs><input channel="paneInvoice"/></inputs>
        <prompt icon="css:bi bi-receipt">
            <label><en>Summarise the newest invoices</en><de>Die neuesten Belege zusammenfassen</de></label>
            <value>open -> "Please summarise the three newest invoices."</value>
        </prompt>
        <prompt icon="css:bi bi-question-circle">
            <label><en>Explain the open invoice</en><de>Den geöffneten Beleg erklären</de></label>
            <value>open -> ifElse(isEmpty($open), "…", "Please explain " + $open.label() + ".")</value>
        </prompt>
    </prompts>
</chat-conversation>
```

Bound exactly the way `<context>` is: the `<inputs>` are channels of this page and their current
values are the **leading arguments** of every card's `value`, evaluated on the server in the acting
user's context and re-evaluated whenever an input changes. So a card names what the page is showing
rather than only a fixed sentence - and, because the text is already evaluated when it reaches the
browser, clicking one costs no round trip.

| | |
|--|--|
| `<label>` | Mandatory. What the card says, in the user's own language - the short form of the question. One line, elided where it does not fit; the whole sentence is the button's tooltip. |
| `icon` | A `ThemeImage`, named the way a `<side-rail>` section names its icon. A card without one shows its label alone. |
| `<value>` | Mandatory. TL-Script producing the text the click puts into the composer. |

**Where they stand, and when.** Between the greeting and the composer - under "Womit kann ich
helfen?", over the field - which is the position the former classic chat gave its own example prompts and
the order the eye reads them in: what this is, what you could ask, where you type. In the empty
stage's grid they are a row of their own (`.stage.is-empty .prompt-suggestions`), zero-height for a
conversation that offers none.

**On a new chat, and nowhere else.** Not "whenever the conversation is empty": a chat the user
opened from the session list is not short of a first question, whatever it holds, so the rule is the
session channel - `promptCardsVisible(chat.sessionId, chat.messages.length)` in
`chat-conversation.tsx`. That is the element's rule and not a page's option; there is nothing to
switch off. (The `empty-only` flag of the first cut is gone.)

**A click fills the composer; it does not send.** The same rule `draft`, `reference` and `quote`
follow, and the reason a card may offer a longer and more precise question than anyone would type:
the user reads the sentence and presses send. It goes in through the same `setDraft` a host writing
the `draft` channel reaches, so the field takes the focus, the caret lands behind the text, and the
composer reports the draft back onto that channel the way a typed sentence does. A card therefore
**replaces** whatever stood in the composer - which on a new chat is a field the user has not typed
into yet.

A card whose expression throws is pushed with an empty text and the browser leaves it out, with a
WARN - the same trade a `<fact>` makes. One card fewer, not a broken page.

`TestChatPromptsConfig` holds the configuration shape; `readPromptCards` / `promptCardsVisible` in
`chat-conversation.test.ts` hold what the browser does with it.

Coupling to know: elements share one conversation only when they agree on a chat id - which is what
the enclosing `<chat>` is for - and a session list feeds the conversation over a **two-way** channel — `<data-list selection>` ↔
`<chat-conversation input>`. The list writes the picked session; the conversation follows it and
writes its own session back (so a chat created on first send gets highlighted). An **empty** selection
means a fresh, empty conversation, so "new chat" is just the list clearing the channel — a
`<data-list create-action="clear-selection">`, no separate binding element. (This replaced the former
`<chat-session-binding>`.)

**The channel is the selection; what the conversation writes back is a proposal** (#576). A host may
bind the channel to something else on its page - one conversation per project, say - and
answer a write-back from its own channel listener by refusing the session and writing its own, or
none. `SessionSelection` therefore tells the browser what the channel holds *after* every listener
has answered, never what the browser reported, and counts each refusal in `selectedCorrections`:
a refusal that leaves the channel empty changes nothing in `selected`, and the count is what makes
the browser leave the refused chat (`followSelection` in `chat-conversation.tsx`). The browser does
not report an empty chat on mount either - it would clear the channel the host just set while the
chat it names is still loading. `TestSessionSelection` and `chat-conversation-input.test.tsx` pin
both halves.

Two rules keep the browser from arguing with the channel, and breaking either one makes the
transcript switch between two conversations every few milliseconds, for good. **The element follows
the channel when the channel moves, never when the store does**: the store opens chats itself (a
first send) and reports them, and until the channel answers, `selected` still names the
chat that was left. **Only the latest choice of chat takes effect**: `selectSession` ignores the chat
the store already shows or is opening, and an opening that another choice overtook is dropped when
its transcript arrives. The tab's remembered chat (`sessionStorage`) is never restored where an
element wires an `input` channel - there the channel is the whole selection, and empty means a fresh
conversation - and elsewhere only into a store nothing has chosen a chat for yet (#577).

The column beside the conversation is the engine's `<side-rail>` (`SideRailElement`, control
`ReactSideRailControl`, see [the side rail](../com.top_logic.layout.view/src/main/java/META-INF/tl-docs/view-layer/side-rail.md)):
a strip of section icons and a column of sections, each section's body a view element the
application configured there.

```xml
<side-rail open-channel="railOpen" badge-channel="railBadge" shown="chatShown" drawer-below="70" side="right">
    <section name="presented" header="false" icon="css:bi bi-easel" reveal="true" width="420">
        <label><en>Presented</en><de>Angezeigt</de></label>
        <visibility inputs="paneObject" expr="!isEmpty($paneObject)"/>
        <presentation-pane input="paneObject"/>
    </section>
    <section name="progress" icon="css:bi bi-list-check" badge="progressBadge">
        <label><en>Progress</en><de>Fortschritt</de></label>
        <visibility inputs="paneObject" expr="isEmpty($paneObject)"/>
        <data-list count="progressBadge" observed-types="tl.ai.execution:AgentTask" …/>
    </section>
</side-rail>
```

Any element is a section - `<chat-sources>`, a `<data-list>` of the agent's tasks or of the
application's own objects, an element a host wrote - so the rail can show a proposed plan, a document
the agent produced or a live view of its browser without the chat learning about any of them. `@`
sources, `+` menu items, citation renderers and message actions are registered from TypeScript and
referenced by name from XML.

Whether the rail is open, which sections are expanded and the width the user dragged are the
rail's, stored per user under its personalization key. Below `drawer-below` rem of the area it
stands in, the rail keeps only its strip and opens as the engine's modal drawer over the
conversation (scrim, focus inside, Escape); the user's column choice is left alone meanwhile. The
shipped page's 70 rem is about what the row needs with the session list as a column, the
conversation at its floor and the presented section open at its 420 px; with the list a drawer the
row needs less, still above the list's own 44 rem, so one threshold covers both.

#### The presentation pane (#616)

`<presentation-pane input="…">` (`PresentationPaneElement`, `PresentationPaneControl`, client
`TLPresentationPane`) shows what stands on its channel. In a side rail it is the body of a section
that is visible while the channel holds something, with `reveal="true"`, so that a new value opens
the rail with that section; the other sections carry the opposite condition and give way to it.
**Nothing about it is configured but the channel** - `PresentationPane` renders any type:

| On the channel | The pane shows |
|--|--|
| one object | its form: every attribute not hidden from display, inherited ones first (so `name` leads), each composition and each computed list of objects (a derived multi-valued reference, such as a research run's `citedSources`) as a `<composition-table>` below the fields - the engine's `<field>` prints a composition as text (`PaneLayouts.isTable`). Such a table annotated `<collapsed/>` (`CollapsedDisplay`, tl-ai-core) is in a closed `<group>` of its own, and the body of a binary value of type `text/html` is shown in place (`<html display="inline">`) below its field, which keeps the download - without the elements of class `download-only`, which say what the fields already show (#764, #827) |
| several objects | a `<table>` typed to their most specific common type: the type's `<main-properties>`, or else up to six single-valued attributes that are not long text; the rest of the type is offered in the column selection. One personalization per type (`chat-pane:<type>`), so the columns a user picked for events are those of the next table of events |
| a `Presentation` (what `present` writes) | the table, chart (the engine's React `<chart>`, `tl-layout-react-chartjs`) or form it names, titled with the agent's `title`. Every chart is the one Chart.js configuration `Presentation.chartData()` computes; `PaneCharts` adds the pane's look only where the configuration says nothing itself |

**History.** Every value that reaches the channel is a step on top of the ones before, and so is a
row clicked in a table or a bar in a chart, which opens that object's form. The back button (command
`back`) drops the top step and writes the value of the one below back onto the channel, so whatever
else reads it - a scoped chat list, a context fact - sees what the user sees. A value equal to the
top step's is not a new step. At most 20 steps; the oldest goes first. The close button (command
`close`) empties the channel, and with it the history, and the rail's other sections come back.

**Every step stays mounted.** The control state `steps` lists the history, bottom first; the browser
keeps all steps in the DOM, the top one shown and the ones below hidden and `inert`, each its own
scroller - so the table the back button returns to has its data, its sort, its filter and its scroll
position, and no agent call is involved. A chart is recomputed from its objects when the user
returns to it. Closing the rail keeps the pane as it is, since a section's content stays mounted
while the rail is closed.

#### `<chat-dock>` - the chat over the page

`<chat-dock>` is a **frame**: a fixed launcher in the corner of the
window, and a card over the page holding whatever the application nested inside it. It brings no
conversation of its own - normally the shipped page `<view-ref view="tl-chat/albert.view.xml">` with
its session list, its `<chat-conversation>` and its `<side-rail>`, which is what
`tl-ai-demo/app.view.xml` puts in the shell's `<footer>`. The footer is
a mounting point, nothing more: the dock is `position: fixed`, so it takes no room in the layout.

| Surface | Kind | |
|--|--|--|
| *(content)* | nested elements | Normally one `<chat>`. The dock body ends the view layer's fill chain (a `FillBarrier`) and its stylesheet stretches a `<split-panel>` (the content of a `<chat>`), a `<stack>` or a `<panel>` over the panel, so the conversation's `<panel fill="true">` takes the panel's height and scrolls inside it. Several children are combined into one stack (`ContentControls`), which is stretched alike; the filling ones among them share the height the others leave. |
| `chat-id` | String, `@Nullable` | **Repeated**, not inherited - see above. Needed so the launcher can read `busy` from the browser store and so the dock can stand down for a page that shows its conversation. |
| `icon` | ThemeImage | The launcher's glyph, default `css:bi bi-chat-dots`. An open dock shows a close cross instead. |
| `<label>` | ResKey | The launcher's tooltip and accessible name; empty falls back to the chat's own wording. |
| `open` | boolean, default `false` | First-load state of the panel. Not remembered. |
| `open-channel` | channel, `@Nullable` | Follows whether the panel is open, and opens or closes it when written. Bound into the content as the `shown` of the `<side-rail>`, so the rail counts nothing as seen while the dock is closed. |
| `badge` / `badge-role` / `badge-label` | `WithBadge` | The launcher's badge (`ChatDockControl.STATE_BADGE`, state `badge`), typically the `badge-channel` of the side rail inside, bound out through the embedded view. |

**A window carries as many conversations as it shows (#502).** Every command of the chat goes to a
chat control of one conversation (the browser's own chat id) of its window, and
`ChatSessionRegistry` holds a `TaskProgressTracker` - one active session, one processing state - for
each of them; `ChatEventHandlerRegistry` is keyed by tracker, so each conversation gets its own SSE
handler by construction. Until #502 the tracker was one per window: two chat ids that each mounted a
`<chat-conversation>` attached to the same run, each send switched the window's active session, the
second conversation to send took the first one's stream, and the first waited for an answer that
never arrived. `TestChatConversationRouting` pins the new shape: a command acts on the conversation
of the control it is sent to, and on no other. A command to a control the window does not hold is
the engine's stale UI, which the command channel answers by loading the page again.

**A conversation in a closed dock offers the agent nothing.** Opening and closing is the state of
`ChatDockControl` (`CMD_SET_OPEN`, `ARG_OPEN`, state `dockOpen`), handed to the elements inside
through their `ChatDockScope` (`isOpen`, a listener). The content stays displayed while the panel is
closed - see below - but a `ChatConversationControl` in a dock registers on the `ChatViewBinding`
only while it is attached **and** its dock is open (`offersToAgent`): the channels `present` may
write, the page commands, the subject and the facts. One conversation has one set of these, and the
allow list of `present` is the union of the window's displayed channels: a closed dock that offered
its pane would take the tables the page's conversation presents into its hidden pane, last attach
winning. The switch runs outside any channel notification
(`ChannelNotificationScope.afterNotification`). `TestChatDockContent` pins it.

**The dock still stands down while a page shows its conversation.** The demo writes no `chat-id`
on the dock, nor does the shipped page it embeds, so the two are the **same** conversation in two
places; showing both at once would give one `draft` channel two composers. `<chat-conversation>` registers itself with the store (`registerConversation`,
`insideDock` from `ChatDockScope`), and a dock whose `conversationsOutsideDock > 0` renders nothing
at all - no panel, no launcher - and an open one sends `setOpen` false, so its conversation stops
offering the agent anything on the server too. A dock on a chat id of its own needs none of that.

Two guards sit beside it: `eventBelongsToChat` drops a run-scoped SSE frame that names a session
other than the open one - a run this conversation has since moved away from (inert while the server
stamps none, which is most of them today) - and both the remembered session
(`tl-chat-session:<chat-id>` in `sessionStorage`) and the run snapshot the context ring reads
(`ChatHandle.runs`) are per chat id. The module-level `runStore` export is still written, as the
single-chat host seam it has always been; nothing in this bundle renders from it.

Three things are load-bearing:

- **The panel is hidden when closed, never unmounted.** The store drops a chat when its last element
  leaves, so a dock that unmounted its panel would hand back a new, empty conversation on every open
  and would stop hearing about the run it started; and the rail inside is what counts the unseen
  badge the launcher shows, through the dock's `badge` channel. `.tl-chat-dock__panel[hidden] { display: none }` is a rule of its own
  because the `display: flex` beside it would otherwise beat the attribute.
- **Opening it lets the rail count as seen, and only that.** The dock's `open-channel` is the rail's
  `shown`: while the dock is open, the open rail marks its shown sections seen, and the dock does not
  open the rail itself.
- **The breakpoints are the dock's, not the window's** (Gitea #427, #548). The conversation inside
  measures its own `.stage`, and a side rail - the session list's and the other one - measures the
  area it stands in, so a dock sized for the desktop layout (`min(68.75rem, …)`) keeps a column in
  a narrow window as long as the panel has room for it, and a panel below a rail's `drawer-below`
  opens that rail as a drawer over the conversation instead of squeezing it. Inside a dock the conversation's floor is measured on `--chat-dock-width`
  rather than on the window, because the panel stands its offset in from both edges.
- **The content mounts from the second commit on.** On the first one the dock cannot know yet
  whether the page shows its conversation - a `<chat-conversation/>` says so from an effect - and
  mounting the content meanwhile would build a second copy of the page's conversation only to
  unmount it a moment later (#552).
- **Escape is the innermost layer's.** The panel is a keyboard scope of its own on the dispatcher's
  stack (`KeyboardScopeProvider` with `EscapeToClose`, active while the focus is in the panel), so
  a layer opened inside - the rail's drawer, a popover - answers Escape first, and closing it does
  not close the dock with it. The panel is positioned and clips, so such a drawer stays inside it.

Everything a theme would restyle is a token on `.tl-chat-dock` - `--chat-dock-width`, `-height`,
`-offset`, `-launcher`, `-gap`, `-radius`, `-shadow`, `-z`.


### Stability

The exports of `index.ts` (`tl-chat-seams`) - the components, the registries and the types of `extensions.ts` - are the contract. File splits inside `ui/src` are not.

Citations: see the developer article `ai/memory-and-citations`. Albert points at its sources with links; the server keeps a
link as a footnote only when a tool of the run read that source, and drops the link of any other id
or address.

## Notes

- Active chat state lives in session memory; completed sessions persisted to KnowledgeBase
- A chat handler lives until the `detachStream` command, until no chat control of its conversation is displayed in the window any more, until its window's page is unloaded, or until a newer attach of the same conversation replaces it
- Background AI tasks run in `ChatRunExecutor`, which carries as many as arrive unless `<capacity chat-workers>` on `AgentRuntimeService` bounds it and queues behind a ceiling it has; a send with no room - only reachable with a bounded `chat-queue` or a per-account cap - is answered `status` `rejected` with a `reason`, which the React client throws as a `RefusedError` and the composer shows in the notice strip
