---
description: Read before debugging why a React control does not update or does not react, or before writing a ReactControl whose state or commands go beyond putState and @ReactCommandHandler - the transport between browser and server (react-api/events SSE stream of SSEServlet, react-api/poll of PollServlet behind proxies, react-api/command and the other endpoints of ReactServlet), the SSEUpdateQueue of a window, StateEvent / PatchEvent and the other events of sse.proto, the Interaction and DeliveryScope that hold delivery back until a command is done, attach / detach / cleanupTree and which controls receive updates, the FIFO command channel of the client (command-channel.ts), reconnects, stale UI and abandoned windows.
order: 105
---

# Client and server: how state and commands travel

A `ReactControl` lives on the server and holds the state its client component renders. The browser
never computes that state; it receives it, renders it and sends back the gestures of the user as
commands. This article describes the transport under `putState` and `@ReactCommandHandler` (see
[a new UIElement](doc:view-layer/new-ui-element) for writing a control): which requests exist, in
which order things happen, and what a control must observe so that its updates arrive.

All endpoints live below `react-api/` of the application (`web-fragment.xml` of
`com.top_logic.layout.react`) and address a **window** by its name: one browser tab (or a window
opened by the application) has one `SSEUpdateQueue`, kept in the `ReactWindowRegistry` of the HTTP
session.

## The page and its first state

The page of a window (rendered by `ViewServlet` for the view layer) writes the root control: a mount
point and a bootstrap script that calls `TLReact.mount(...)` with the component name and the full
state. Child controls are part of that state as child descriptors (`ChildControl` of `state.proto`:
`controlId`, `module`, `state`), which `TLChild` mounts on the client. Rendering a control -
into the page, or as a child of another control's state - **attaches** it and registers it with the
queue of its window under its ID; only a registered control can be addressed by a command.

Rendering the page starts a new page of the queue (`SSEUpdateQueue.beginPage()`): whatever was
queued or written for the previous page of the same window is dropped, since the rendered output
carries the full state.

## Updates: the event stream `react-api/events`

After mounting, the client opens an `EventSource` on `react-api/events?windowName=<name>`
(`SSEServlet`). The servlet never creates a window: a window the session does not know answers
`410 Gone`. On connecting, the queue sends the full state of every registered control, a first
heartbeat (so the client knows the stream arrives), and the events that were waiting for a
connection.

Every event is a msgbuf message of `sse.proto` (`com.top_logic.layout.react.protocol`), written as
JSON `[typeCode, {fields}]` in one `data:` line, numbered by the SSE `id`:

| Event | Effect on the client |
|---|---|
| `StateEvent` | replaces the state of the control `controlId` |
| `PatchEvent` | merges a JSON Merge Patch (RFC 7396) into the state of the control `controlId` - what `putState` produces |
| `ContentReplacement`, `ElementReplacement`, `PropertyUpdate`, `CssClassUpdate`, `FragmentInsertion`, `RangeReplacement`, `JSSnipplet`, `FunctionCall` | DOM updates of classic (non-React) controls in the same window, see below |
| `RouteChangeEvent`, `RouteResumeEvent` | updates the address bar, resumes a history move refused over unsaved changes |
| `PageNavigationEvent` | leaves the page: reload it, or load another address (login, logout) |
| `WindowOpenEvent`, `WindowCloseEvent`, `WindowFocusEvent` | manage windows the application opened |
| `DownloadEvent` | fetches a file a command prepared (`react-api/download`) |
| `PickEvent` | starts picking an element (UI inspector) |
| `I18NCacheInvalidation` | drops the client's cache of translations |

The queue sends a heartbeat every 15 seconds. After a connection error the `EventSource`
reconnects by itself, and a reconnect sends the full state again; a stream that stays silent for 45
seconds makes the client poll instead (see [below](#when-the-stream-does-not-arrive-react-apipoll)).

**What a control sends.** `putState(key, value)` of a control that has been rendered and is attached
sends a `PatchEvent`; before the first rendering it only fills the initial state.
`beginUpdate()` / `commitUpdate(token)` collect several `putState` calls into one patch.
`updateStateSilently(...)` records a change without an event, for a value the client sent itself
(echoing a typed value back would overwrite newer keystrokes); when the same handler runs from a
script replay, the framework resends the state after the command.

## Commands: `react-api/command`

A command is a `POST` of JSON `{controlId, command, windowName, arguments}` to
`react-api/command` (`ReactServlet`). The servlet resolves the window and its sub-session (the
acting user belongs to the window), looks up the control in the window's queue and calls its
`@ReactCommandHandler` method by the command name. It then forwards side effects - messages of the
`InfoService`, repaints of classic controls whose model the command changed - and answers
`{"success": true}`; a failing command is shown in the snackbar of the window (`CommandErrors`)
instead of failing the request.

**One command at a time, in order.** On the client, every command goes through one FIFO chain
(`command-channel.ts`): a command is sent only after the answer to the previous one has arrived. A
field holding a debounced value registers a pending flush, and every action command flushes those
values first, so the action always sees the complete input (pressing Enter in a login dialog right
after typing the password). On the server, every command runs in an `Interaction`
(`ReactWindowRegistry.beginInteraction()`), which holds the session's request lock, so a second
request waits until the control trees are consistent again.

**The answer comes before the events.** An interaction opens a `DeliveryScope`: events enqueued
within it are only queued, and delivered when the outermost interaction closes. Before delivery,
`SSEUpdateQueue.settle()` drops `StateEvent`s and `PatchEvent`s addressed to controls the
interaction stopped displaying - a control patched by one listener of a channel while a container
listening later on the same channel replaces it. Nothing is lost by that: a control that becomes
displayed again is serialized with its full state. Events enqueued outside an interaction (a
reconnect, background activity) are written at once.

**A command for a control that is gone.** A control not registered in the window answers
`404`. A `valueChanged` of a field whose control left the display is dropped silently (the edit was
abandoned, e.g. the dialog was cancelled). A window the session does not know, or a window without
any control (the session was replaced by a login or logout underneath the open page), answers with
the error code `stale-ui`, upon which the client reloads the page.

## The other endpoints of `ReactServlet`

| Request | Purpose |
|---|---|
| `POST react-api/state?controlId=…&windowName=…` | the current full state of one control as JSON |
| `POST react-api/upload` | multipart upload to a control implementing `UploadHandler`; a file over its `maxUploadSize()` or refused by a content checker is answered with an error and a message in the window |
| `GET react-api/data?controlId=…` | binary data served by a control implementing `DataProvider` (images, audio) - the window-bound way to deliver user-dependent content |
| `GET react-api/download?key=…` | a file a command handed out with `SSEUpdateQueue.deliverDownload(BinaryData)`; served once |
| `GET react-api/tooltip`, `GET react-api/i18n` | lazy tooltips, translations of the window's locale |

## When the stream does not arrive: `react-api/poll`

A proxy that inspects content may hold a `text/event-stream` response back, forward it in large
blocks, or refuse it. The queue therefore keeps what it wrote (up to `MAX_RETAINED_CHARS`) until the
client has read it, and the client switches to polling (`PollServlet`) when the stream brings
nothing within three seconds, is refused, or falls behind: every command answer carries the number
of the last event written (header `X-TL-Last-Event`), and a stream that has not delivered it shortly
after makes the client poll. A tab that had to poll keeps polling, also after a reload.

`GET react-api/poll?after=<n>` answers the events after number `n`, waiting up to
`POLL_HOLD_MILLIS` for one. The first poll sends the session cookie and asks for the full state
(`full=true`); later polls carry the window's token instead of the cookie, so that polling does not
keep the session alive. `401` and `410` mean the session or window is gone (the client reloads),
`409` that events were dropped before they were read (the client asks for the full state again).

## Attached, detached, disposed

Whether a control's update reaches the client is decided by its lifecycle, not by the caller:

- **Attached** - rendered and displayed. Registered with the queue, addressable by commands, its
  `putState` sends patches. Attaching is implicit: rendering a control attaches it, and the attach
  listeners (`addAttachListener`) are the place to register model listeners.
- **Detached** - in memory, not displayed: the inactive child of a tab bar, a deck, a sidebar.
  Detaching is explicit: the container that stops displaying a child detaches it (`detach()`, which
  propagates to its children). A detached control records `putState` without an event and is
  serialized with its full state when it is displayed again.
- **Disposed** - `cleanupTree()`: detached for good, never registered again. A handler that still
  holds the control within the running interaction may call `putState`; it is dropped, not an
  error.

A control that renders controls it does not own - a slot placeholder showing the controls of a
`<slot-content>` - declares the state property with `borrowedState(name)`, so that its disposal does
not dispose them.

## Windows that go away

Closing a tab reports `windowClosed` (with `unload=true` on `beforeunload`, which fires for a reload
as well): the window keeps its state for a grace period, so that a reload finds it. A client that
vanishes without a report - a killed tab, a lost network - is noticed by the heartbeat, and the
registry tears the window down once its client has been gone for
`ReactWindowRegistry.DISCONNECT_GRACE_MILLIS`. A request of a session restarts its inactivity
timeout (`SessionActivityFilter`); a poll by token does not.

## Classic controls in the same window

`ReactServlet` runs a command inside the window's classic layout engine as well, when the window has
one (`SubsessionHandler`). A classic control whose model the command changed is repainted, and the
repaint reaches the browser as one of the DOM events above, on the same stream - no separate AJAX
round trip.

## Pitfalls

- **Two tabs, one window name** share one queue: the second event stream replaces the first, and
  the first tab stops receiving updates although its commands still arrive (logged as "SSE
  connection REPLACED").
- **State changed outside an interaction is written at once.** A background thread that changes a
  displayed control enters an interaction (`beginInteraction()` or `tryBeginInteraction(timeout)`)
  so that its updates are consistent with the display.
- **A control the client never received cannot be addressed.** IDs are known to the client only
  through serialization; a control created but not put into any rendered state is not registered.
