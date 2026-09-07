# View Designer — "Select View" Picker

**Ticket:** #29108
**Date:** 2026-07-15
**Status:** Design (pending review)
**Related:** `2026-03-27-view-designer-design.md` (original View Designer design)

## Goal

Add a **"Select view"** button to the View Designer toolbar (next to *Apply* /
*Revert*). When pressed, the pointer in the **main window** turns into an
eyedropper cursor; the view under the cursor is highlighted. Clicking a view
selects the corresponding node in the View Designer tree (which in turn drives
the config editor, as today). `ESC` cancels.

Granularity (decided): clicking selects the **enclosing whole view**
(a `.view.xml` boundary: the root app view or a `<view-ref>`), not individual UI
elements.

## Context

- The View Designer opens in a **separate browser window** (`window.open`,
  `ReactWindowRegistry.openWindow`). The main window is its *opener*. Cross-window
  coordination is therefore required.
- The designer's design tree is built from config loaded from disk
  (`DesignTreeBuilder`), and every `DesignTreeNode` already carries its
  `sourceFile` (the `.view.xml` path it came from). `<view-ref>`s are resolved
  eagerly, so referenced views appear as nodes with their own `sourceFile`.
- **There is no existing link** between a rendered `ReactControl` (its DOM id)
  and a design-tree node. This feature introduces a coarse link keyed by the
  **source view file**, which is stable and already tracked on nodes.
- The server can push an SSE event to a specific window's queue
  (`SSEUpdateQueue` per `windowId`, as `WindowOpenEvent` does with
  `targetWindowId`), and the client has a global SSE dispatch (window-manager).
- The `OpenDesignerCommand` already registers an `appContext` channel holding the
  main window's `ReactContext` (currently used only to hot-reload after *Apply*).
  This is reused to target the main window.

## Approach (chosen: server-mediated via SSE)

Rejected alternatives: client-only coordination via `BroadcastChannel`/
`window.opener` (fragile, harder to test, duplicates coordination the framework
already does server-side); a channel/flag observed by main-window controls (no
single global main-window control exists to drive cursor/click capture).

## Components

### 1. Source-path stamping on view root controls (shared building block)

Each view's outermost control receives a DOM attribute
`data-view-source="<loader-path>.view.xml"`.

- View boundaries exist only at (a) the **root app view** and (b) each
  **`<view-ref>`** (`ReferenceElement`). Both know their source path.
- Stamp the attribute on the **existing** outermost control of that view (no
  extra DOM wrapper, to avoid disturbing flex/layout).
- Mechanism: `ReactControl` gains an optional source-view field written as
  `data-view-source` in `write(TagWriter)`. `ReferenceElement.createControl`
  sets it on the control it returns for the referenced view; the root/main app
  view sets it on its root control.
- The client resolves a click by walking up from `event.target` to the
  **nearest** ancestor carrying `data-view-source`; nested views therefore
  resolve to the innermost enclosing view, which matches "the enclosing view".
- Path normalization: the stamped value and `DesignTreeNode.getSourceFile()`
  are both normalized to the `ViewLoader` path form so string comparison is
  exact.

*Open point (confirm in planning):* whether `ReactControl` gets a generic
extra-attributes map or a specific `viewSource` field. Recommendation: a
specific field, since this is the only such attribute today (YAGNI).

### 2. Designer toolbar command `<select-view>` (`SelectViewCommand`)

- `SelectViewCommand implements ViewCommand`, nested `Config` with
  `@TagName("select-view")` and `@ClassDefault`, placed in `designer.view.xml`
  next to `save-design` / `revert-design` with `placement="TOOLBAR"`.
- Config channel refs (via `ChannelRefFormat`): `appContext`, `selectedNode`,
  `design-tree`.
- `execute(ReactContext, input)`:
  1. Resolve the main window `ReactContext` from `appContext` → `windowId`.
  2. Create a short-lived, single-use **pick token**; register a callback in the
     pick registry (§5): `token → (String pickedPath) -> selectNodeForFile(...)`.
  3. Enqueue a `ViewPickEvent(token)` on the main window's `SSEUpdateQueue`.
- Executability: disabled when no `appContext` channel / no main window.

### 3. New SSE event `ViewPickEvent`

- In `com.top_logic.layout.react/.../protocol/sse.proto`:
  `message ViewPickEvent extends SSEEvent { string token; string targetWindowId; }`.
- Regenerate msgbuf (runs during `mvn compile`).
- Extend the client SSE dispatch to route `ViewPickEvent` to the picker (§4).

### 4. Client pick mode (main window) — `react-src/bridge/view-picker.ts`

Registered in the bridge init. On `ViewPickEvent(token)`:

- **Cursor:** eyedropper/crosshair on `document.body` (CSS `cursor`; custom
  eyedropper as a data-URI if a plain `crosshair` is not distinctive enough).
- **Hover highlight:** a `position: fixed; pointer-events: none` overlay div that
  tracks the bounding rect of the hovered `[data-view-source]` element
  (capture-phase `pointermove`).
- **ESC:** cancel — remove cursor/overlay/listeners, no selection, no server call.
- **Capture-phase click:** always `preventDefault()` + `stopPropagation()`.
  - Hit (nearest `[data-view-source]` found): read its path, POST `{token, path}`
    to the server (§5), then end pick mode.
  - No hit: **end pick mode**, no selection; the click is discarded.
- Only one pick session active at a time; a new `ViewPickEvent` supersedes any
  active session.

### 5. Result channel + node selection (server)

- Client POSTs `{token, path}` to a new `react-api` action `view-pick`.
- **Pick registry** (session-scoped, in `com.top_logic.layout.react`):
  `token → Consumer<String path>`. Module boundaries: the endpoint (layout.react)
  does not know design-tree types; it only invokes the callback. The callback is
  supplied by `SelectViewCommand` (layout.view) and performs the layout.view
  work: from the `design-tree` root node, find the node whose `sourceFile`
  equals `path` and which is that file's **view-root** node, then push it onto
  the `selectedNode` channel. The design tree and config editor then react as
  they already do on selection.
- The token is single-use and expires; removed after use.

### 6. Error handling

- No `appContext` / main window not found → command disabled (executability), or
  a user-facing message if reached.
- Clicked view not present in the design tree → snackbar "View is not part of
  the current design" and end pick mode.
- Unknown / expired token → ignore.
- Stale event (popup already closed) → harmless; token lookup fails → ignored.

### 7. Data flow

```
Designer button
  -> SelectViewCommand (server): register pending(token) + enqueue ViewPickEvent(token) to main window SSE queue
  -> main window client: enter pick mode (cursor + highlight + ESC + capture click)
  -> user clicks a view
  -> client POST {token, path} to react-api/view-pick
  -> server endpoint: lookup callback by token -> callback resolves sourceFile==path node
  -> set designer selectedNode channel
  -> design tree + config editor update (existing behavior)
```

## Testing

- **Server (JUnit, `com.top_logic.layout.view`):**
  - `sourceFile → node` lookup over a built design tree (assert the correct
    view-root node is found; assert miss when the file is absent).
  - `SelectViewCommand` registers a pending callback and enqueues a
    `ViewPickEvent` (fake `SSEUpdateQueue` / window registry).
- **Client:** the pick DOM logic (nearest `[data-view-source]`, ESC, highlight,
  click hit/miss) via a headless check (as used previously), plus manual
  Playwright verification.
- **End-to-end (manual, per CLAUDE.md):** in `tl-demo` / `tl-demo-react`, open the
  designer, press *Select view*, pick a view in the main window, assert the
  corresponding tree node is selected and the config editor updates; verify ESC
  and click-miss both end the mode without selecting.

## Out of scope (YAGNI)

- Per-element (sub-view) selection granularity.
- Selecting views not reachable from the currently designed view tree.
- Persisting or multi-selecting picks.

## Open points to confirm during planning

- Exact location/servlet for the `react-api/view-pick` endpoint.
- `ReactControl` generic extra-attributes map vs. a specific `viewSource` field
  (recommendation: specific field).
