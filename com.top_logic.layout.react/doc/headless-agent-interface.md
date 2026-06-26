# Headless Agent / Script Interface for the React View Layer — Plan & Progress

> **Status: PROTOTYPE.** A proof-of-concept exists, is unit-tested, and has been
> exercised live against the demo. None of this is production-ready or merged.
> This document tracks the design questions and the work to turn the prototype
> into a real feature.

- **Branch:** `CWS/CWS_29108_headless_agent_interface` (off `CWS/CWS_29108_integration`)
- **Code:** `com.top_logic.layout.react.headless` (package), `AgentServlet` (HTTP)
- **Owner:** bhu
- **Last updated:** 2026-06-26

## How to use this document

- Each work item is a checkbox. Tick it when done; add a dated note in the
  **Progress log** for anything non-trivial.
- **Open design decisions** are the gating questions — resolve those before
  building the dependent phase. Mark a decision `DECIDED:` with a one-line
  rationale when settled.
- Status legend for milestones: ✅ done · 🚧 in progress · ⬜ not started ·
  ⛔ blocked (on a decision).

---

## Goal

Give the React view layer a non-browser control surface so that a single
mechanism serves three consumers:

1. **Script recorder** — capture a user's interactions as replayable steps
   (regression tests, data-setup scripts), the successor to the legacy
   `ScriptingRecorder` / `ApplicationAction` model.
2. **Replay** — run recorded steps deterministically.
3. **AI agent** — observe application state, decide, and act to drive a session
   autonomously (the "headless interface for an agent controlling an app
   session" idea).

The key insight: the view layer **already** exposes a server-side state tree and
accepts commands back, so the substrate exists. The missing piece is a **stable
semantic addressing layer** plus a thin facade over the existing command
dispatch.

## Architecture at a glance

Three primitives (`AgentSession`):

- `observe()` → project the live control tree into an addressable `AgentNodeView`
  (role, name, state, advertised actions, children).
- `resolve(address)` → map a semantic address back to the live control.
- `act(address, command, args)` → dispatch through the **same**
  `ReactControl.executeCommand` the browser uses (one source of truth for
  behavior).

Addressing (`AgentTreeProjector`): a path of `role[name]` segments derived from
semantic properties + tree shape (e.g. `…/card[Aktive_Aufgaben]/counter[Aufgaben]`),
**not** the opaque per-session control IDs (`v17`). Controls may implement
`AgentNode` to refine role/name/state and advertise an action space with argument
schemas.

---

## Milestones

### Phase 0 — Prototype ✅ (done, 2026-06-23/24)

- [x] Generic tree introspection over the existing state tree
      (`ReactControl.agentChildren/agentScalarState/commandNames`,
      `SSEUpdateQueue.getRootControls`, `ReactCommandMap.commandIds`).
- [x] `AgentSession` facade (`observe`/`resolve`/`act`) reusing `executeCommand`.
- [x] `AgentTreeProjector` semantic addressing with sibling disambiguation.
- [x] `AgentNode` opt-in enrichment + `AgentAction`/`AgentParam` schemas.
- [x] Model-derived naming via `MetaLabelProvider` (pluggable; table rows / list
      items get business-object labels).
- [x] `AgentServlet` HTTP endpoint (`/agent-api/{windows,observe,act}`) +
      window discovery + quiesced-observation responses.
- [x] Self-contained `TestAgentSession` (5 tests): observe→act→observe,
      recorded-step replay, model-derived addressing, unknown-address failure.
- [x] **Live verification** against the demo dashboard: observed the real tree,
      acted on a counter (0→2), browser UI updated via SSE.

### Phase 1 — Addressing hardening 🚧 (D1 decided; cross-session test remains)

The load-bearing phase. Index-disambiguated labels are fine for a demo but
fragile for recorded scripts (labels duplicate, reorder, localize, change).

- [x] Decide the addressing scheme (see **D1**) — **DECIDED**. Both walls closed:
      resolution context is `ReactActionContext`; naming-scheme stability resolved in
      favour of the *by-label* default (the label typically **is** the business
      identifier; KB ids are worse — unstable across DB reset and broken in tests).
- [x] Introduce a stable identity for **data** nodes (rows, list items) — a
      model/business-key reference (`AgentModelKey` = JSON `ModelName`), not a
      positional index. Keys on table rows and dropdown options; build **and**
      resolve (`selectByKey`) verified live.
- [x] Keep structural paths for **chrome** (buttons, tabs, panels).
- [x] Define behavior when an address no longer resolves (drift): **DECIDED + done**.
      A control that resolves recorded business keys never silently substitutes or
      drops. Structural address drift already hard-fails (`AgentSession.resolve`
      throws → `act`/`replay` report `success:false`). Key-level drift now does too:
      `selectByKey` on the dropdown (any unresolved key) and the table (no matching
      row) returns `HandlerResult.error` instead of a silent partial/empty selection,
      surfaced as `success:false` + a localized `error` message. `/replay` adds a
      top-level `success` (the all-steps-passed regression verdict). No fuzzy
      best-match in the control — re-planning is an agent-side strategy on top of the
      loud failure. Verified live: a valid row key selects (`success:true`); a bogus
      key fails (`success:false`, "…Geschäftsschlüssel lässt sich nicht mehr einer
      Zeile … zuordnen…") — the same call silently succeeded on the pre-fix build.
- [ ] Round-trip tests across re-render and across a fresh session — scheme-level
      round-trip unit-tested (`TestReactOptionByLabelNaming`); a cross-session
      end-to-end automated test still to write.

### Phase 2 — Recorder side 🚧

- [x] **Intercept the browser command stream and translate to `(address, command,
      arguments)`.** `ReactServlet.handleCommand` feeds each dispatched command into the
      window's `ScriptRecorder` while recording, turning the target control into its
      stable semantic address via `AgentSession.addressOf` (the inverse of `resolve`,
      same segment algorithm → guaranteed to resolve back). Verified live: two browser
      clicks on the dashboard counter captured as
      `/appShell/sidebar/grid/card[Aktive_Aufgaben]/counter[Aufgaben] :: increment` —
      no session ids.
- [x] **Recording format + endpoints.** `POST /agent-api/record/{start,stop}` and
      `GET /agent-api/record/steps` return `{recording, steps:[{address,command,
      arguments}]}`. The same shape feeds replay. (D3 — whether this is also the agent's
      action log — still open; the format is deliberately the `act` triple so they can
      converge.)
- [x] **Replay a recorded script headlessly.** `POST /agent-api/replay {steps}` runs
      each step through `AgentSession.act`, settling derived state between steps, and
      returns per-step `results` + final `observation`. Verified live end-to-end: counter
      0 →(2 recorded clicks)→ 2 →(replay)→ 4.
- [x] **Replay-stable arguments.** A control rewrites session-bound arguments at record
      time via `ReactControl.recordCommand` (default: verbatim). The dropdown overrides it
      to translate `valueChanged {value:[ids]}` → `selectByKey {keys:[businessKeys]}`.
      Verified live: a real browser option click on the members dropdown was captured as
      `selectByKey {keys:["…ReactOptionByLabelNaming$Name…{label:securityOwner}"]}`, and
      replaying that recorded script against a fresh (empty) form set the members to
      `securityOwner` — session-independent identity, not an option id.
- [x] **Assertions/observation steps.** `POST /agent-api/record/assert {address, expect?}`
      appends an assertion step (capturing the node's current state, or an explicit `expect`).
      On replay it is *verified* not dispatched: a reserved `assertState` pseudo-command
      compares the expected entries against the live node state (subset match, canonical-JSON
      so numeric representation is irrelevant) and reports per-key `mismatches`. Verified live
      on the group form: `select user` + `assertState {value:"user"}` passes; the same with
      `{value:"superuser"}` fails with `mismatches:[{key:value,expected:superuser,actual:user}]`.
      The compare logic (`RecordedStep.mismatchingKeys`) is unit-tested.
- [x] **Replay-stable table selection.** `TableViewControl` gains a `selectByKey {key}`
      command (resolves the row's business object globally via `ReactActionContext` and
      selects it) and a `recordCommand` override translating a plain `select {rowIndex}`
      into `selectByKey {key}` (modifier/range selections stay index-based). Verified live:
      a recorded group-row click captured as `selectByKey`, and replaying it **after
      re-sorting the table** still selected the right group (`securityOwner`) — index-
      independent. Both major interactive controls (select + table) now record stably.
- [x] **User-facing recorder side-window (first cut).** An app-bar `<open-window>` button
      (the new reusable `OpenViewWindowCommand`) pops out a recorder window with Start/Stop
      and a steps table (`com.top_logic.layout.view.recorder`, all in the view layer; demo
      only wires the button). The recorder lives on the recorded (main) window's queue; the
      side-window drives it through its opener, so the side-window's own clicks are not
      captured.
- [ ] **Recorder side-window — reach legacy parity.** Built as a faithful analog of the
      reusable SQL-monitor view (dual `recording`/`steps` channels, one `RecorderAction`
      with `Mode {START, REFRESH, STOP}` returning `recorder.steps()`, a `RecordedStepsTable`
      `UIElement`). Reuses the generic `VisibleIf` rule and `<execute-script>`+`<write-channel>`
      — no bespoke executability rule. Status of the review gaps:
  - [x] **Buttons sized properly** — compact `placement="TOOLBAR"` icon buttons (Start /
        Refresh / Stop), not full-width content buttons.
  - [x] **Start/Stop mutually exclusive** — driven by the boolean `recording` channel +
        `<visible-if expr="running -> $running == true|!= true">`; Start shows while idle,
        Refresh/Stop while recording. Verified live: clicking Start flips the toolbar; Stop
        flips it back. (Replaced the would-be bespoke `RecordingStateRule` with this reuse.)
  - [x] **Actions shown** — `RecordedStepsTable` lists captured steps (#, address, command,
        arguments). Verified live: two main-window counter clicks show as two `increment`
        rows with their semantic address.
  - [x] **Live pop-in (push, not pull)** — done. `ScriptRecorder` is now observable
        (`addListener`, fired on start/stop/record); the recorder window's `RecordedStepsTable`
        subscribes to the **opener's** recorder (reached via the opener-tracks-children
        registry, `RecorderAccess`) and refreshes on each event. The capture happens on the
        main window's request thread, but `SSEUpdateQueue.enqueue()`+`flush()` is synchronized,
        so the refresh pushes straight to the recorder window's own SSE connection — no
        cross-window subsession machinery, no Refresh button (removed). Verified live: three
        main-window counter clicks (increment, increment, decrement) appeared in the recorder
        window as they happened, in order, zero console errors.
  - [x] **Step-debugger from the UI** — done. Selecting a step and pressing **Step**
        (`StepReplayAction`) replays that single step on the opener window and advances the
        selection to the next step. Cross-window replay is the reusable `ReactWindowReplay.act`
        (installs the opener's subsession, runs under the shared request lock, settles, restores
        the caller's subsession — the same wrapping `AgentServlet` uses, now extracted and shared;
        both servlets' duplicated `installSubSession` delegate to it). Selection is the reused
        `TableElement` pattern (table selection ↔ a `selection` channel, guarded against feedback)
        plus a new programmatic `TableViewControl.selectRow(key)`. Replay goes through
        `AgentSession.act`, not `ReactServlet`, so a replayed step is **not** itself recorded.
        Verified live: two recorded increments stepped one at a time drove the main-window counter
        2→3→4, selection advancing 1→2, effect shown in the main UI, zero console errors.
  - [ ] **Export / clear from the UI** — copy the step script out and a clear button (the
        remaining recorder-UI conveniences).
- [x] **Explore the legacy `ScriptingRecorder` capabilities** and map them to this design.
      Done 2026-06-25 — see **[Legacy parity gap analysis](#legacy-scriptingrecorder--parity-gap-analysis-2026-06-25)**
      below. Headline: the new stack already reuses the legacy `ModelName`/`ModelResolver`
      object-naming infrastructure (strongest parity point), has the observe/act/record/
      replay loop and a generic value assertion, but is MISSING the breadth of legacy
      capabilities — typed assertion library, global variables/parameterization, script
      control-flow (chain/conditional/include), an on-disk script format, and the
      inspector-driven assertion-recording UX. The parity backlog below is driven from it.
- [ ] Migration story / coexistence with legacy `ScriptingRecorder`.
- [ ] Label-based assertion for session-id-bearing state (e.g. a dropdown's `value`
      descriptors carry option ids); assert by label/key instead.

### Phase 3 — Action space & affordances 🚧

- [x] **Structural pruning (modular).** Layout-only wrappers elide themselves from
      the projection via a single polymorphic `ReactControl.agentTransparent()`
      override (ReactStackControl, ReactInsetControl, ReactSplitPanelControl,
      ReloadableControl, SlotPlaceholderControl) — the projector asks each control
      and never switches
      on concrete types (no `instanceof` cascade). Result (live): addresses dropped
      from `/stack/appShell/sidebar/stack/inset/stack/grid/card[…]/counter[Aufgaben]`
      to `/appShell/sidebar/grid/card[Aktive_Aufgaben]/counter[Aufgaben]`; payload
      ~27 KB → ~20 KB. Shorter addresses are also more stable against layout
      refactors (advances **D1**).
- [x] **Strip rendering-only state and chrome commands (modular).** `agentScalarState`
      drops `null` values generically; each control declares its own presentation
      state keys (`agentPresentationKeys()`) and chrome commands
      (`agentHiddenCommands()`) — same polymorphic seam as `agentTransparent()`, no
      central list. Applied to button/card/grid/appBar/text/snackbar (padding,
      variant, css class, size, …) and sidebar/appShell (`toggleCollapse`,
      `toggleDrawer`, `toggleGroup`, `reportDisplayClass`). Live: payload
      ~20 KB → ~17.7 KB (~35% off the original 27 KB); sidebar actions reduced to
      `[executeCommand, selectItem]`; button state to `[disabled, hidden, label]`.
- [x] **Affordance-first `mode=actions` view.** `GET …/observe?mode=actions` returns
      a flat list of only the actionable nodes (`{address, role, name, state, actions}`,
      no children). Live: 16 nodes, ~10.95 KB vs 17.7 KB tree (~60% off the original
      27 KB); the read→act→read loop works by the same addresses.
- [ ] `executeCommand` on the sidebar is a remaining chrome-command candidate to
      assess (one-line `agentHiddenCommands` entry if internal).
- [x] **Argument schemas on `@ReactCommand` (D5 first step) — DONE, now superseded.** New
      `@ReactParam` (name/type/required/description) declared in `@ReactCommand.params()`,
      captured by `ReactCommandMap`, exposed via `ReactControl.agentCommandParams`, and
      surfaced by the projector in each action's `params`. Annotated the commands I had
      to guess: `selectItem {itemId}`, `valueChanged {value:string[]}`,
      `select {rowIndex,ctrlKey,shiftKey}`, `selectTab {tabId}`. Verified live: the
      sidebar advertises `selectItem(itemId, required)` — the exact key I'd guessed
      wrong. The schema lives next to the handler (can't drift) and needs no headless
      dependency on the control. **`@ReactParam` is a hand-rolled restatement of what a
      `ConfigurationItem` descriptor already knows; the typed-argument work below replaces
      it (see D5).**
- [x] **Typed command arguments as `ConfigurationItem` (the D5 core) — first slice done,
      verified live.** A `@ReactCommand` handler may declare a
      `ConfigurationItem` subtype as its argument parameter (third allowed param type
      alongside `ReactContext` and the legacy raw `Map`). `ReactCommandMap` captures the
      argument `ConfigurationDescriptor`; at dispatch `ReactCommandInvoker` re-serializes
      the client argument `Map` and binds it through `JsonConfigurationReader`, so the
      handler receives the typed instance and reads getters. Un-migrated commands keep the
      raw `Map` path untouched. `TestAgentSession` (12 green) proves the binding and the
      descriptor capture; a control's `setNote(NoteArgs)` receives `{note:"hello"}` typed.
  - [x] **JSON schema for free — verified live.** `JsonConfigSchemaBuilder.buildConfigSchema`
        projects the arg descriptor to a JSON Schema, serialized via `JsonSchemaWriter` and
        emitted as the action's `argsSchema` (replacing `params` for typed commands).
        Fault-tolerant: if the schema can't build (e.g. no `ResourcesModule`), the action
        still projects with a `null` schema. Live on the demo: `/agent-api/observe?mode=actions`
        advertises `selectItem` with `argsSchema` carrying `required:["itemId"]` and the
        property's generated `title:"Item ID"` + description — `@Mandatory` and the I18N label
        flowing into the schema with no hand-written `@ReactParam`.
  - [x] **Human-readable step rendering — done, verified live.** `ReactControl.describeCommand`
        binds a step's arguments to the typed config and renders it via `ConfigLabelProvider`;
        each arg interface's `@Label` *is* the template (e.g. `Navigate to '{itemId}'`, kept in
        the generated bundle, DE hand-authored). The recorder side-window's `RecordedStepsTable`
        gained a **Description** column that resolves the step's address against the opener
        window and calls `describeCommand` (falling back to the raw command + JSON for assertion
        steps, untyped commands, or drifted addresses). Verified live (German session): captured
        steps read *"Navigiere zu 'input-controls'"*, *"Navigiere zu 'tabs'"*,
        *"Tab 'details' aktivieren"* beside the technical address/command/arguments. Values are
        the raw ids for now; resolving them to friendly labels is the deferred follow-up.
  - [ ] **Persisted step format** — write the bound config instance as JSON/XML (feeds
        parity item #12; per-step `comment` rides along, parity #16).
  - [x] Lean React-side action-config **base** — `ReactCommandArguments` (plain
        `ConfigurationItem`). **Not** legacy `ApplicationAction`.
  - [x] Migrate the structural-navigation commands — **done, verified live**: sidebar
        `selectItem` → `SelectItemArguments`, tab bar `selectTab` → `SelectTabArguments`,
        table `select` → `SelectRowArguments`. Live `/agent-api/observe` advertises each
        with its derived schema; `select` confirms non-string types
        (`rowIndex:"integer"` required, `ctrlKey`/`shiftKey:"boolean"` optional) and a
        table-row click still selects (detail panel populated). `@ReactParam` removed from
        all three; arg-key literals unified onto the typed interfaces.
  - [ ] Migrate the value-bearing commands (`valueChanged`/`selectByKey`). Entangled with
        the value→label-resolution decision below (their values are option/business keys),
        so deferred with the rendering work. Consider a conformance check that every
        recordable `@ReactCommand` declares a config arg type.
- [ ] **Sidebar size hotspot via the enum schema** — once `selectItem` carries a typed
      arg, advertise `itemId` as a constrained enum (`∈ {dashboard, administration, …}`)
      from the schema instead of shipping the raw 24-entry `items` array (folds the
      former "remaining size hotspot" item into the typed-argument work).
- [ ] Surface enabled/disabled and visibility per action (don't advertise
      actions that would be rejected).
- [ ] Decide the default projection of state: raw control state vs. a curated
      semantic view (see **D4**).

### Phase 4 — Quiescence, security, projection 🚧

> **Known bug found 2026-06-24 (live):** `observe`/`act` reuse the **same** live
> per-window control tree as the browser session (by design), but share it
> naively. Two concrete defects:
> 1. **UI stalls / clicks rejected while inspecting.** The request lock is
>    *session-wide*, and `observe` holds it across the **whole** projection,
>    including `MetaLabelProvider.getLabel` on every node. Real clicks
>    (`/react-api/command`) queue behind it; slow/blocking label resolution stalls
>    the session. Cross-tab inspection serializes the whole session.
> 2. **Flaky reads (only the root node).** The tree is read while it is being
>    rebuilt: the SSE heartbeat runs `synthesizeModelEvents` **without** the
>    request lock, and control register-then-embed has a race window, so
>    `getRootControls()`/`agentChildren()` can see a partial tree.

- [x] **Project from the window's authoritative root**, not the registration
      heuristic. `SSEUpdateQueue.setRootControl` is set by `ViewServlet`; the
      heuristic (`getRootControls`) is **removed** — no silent fallback. Fixed the
      orphan over-reporting (was 5 `TLStack` roots after visiting 5 views; now a
      stable single `/stack` reflecting the current view). Verified live.
- [x] **Diagnostic logging** (window-named): command-target-not-found now logs
      window + registered count + attached count + IDs; SSE connection
      *replacement* logs at WARN; unknown-window commands log the known windows.
      `SSEUpdateQueue` window name populated at creation so logs identify it.
- [ ] **Investigate queue recreation (likely the real "dead controls" cause).**
      The new logging already shows one window (`vd6…`) with **two different
      `SSEUpdateQueue` identities** — the per-window queue is being recreated.
      A recreated (empty) queue would receive commands while the controls live in
      the old queue → `NOT FOUND, 0 registered`, i.e. clicks do nothing. Find what
      evicts/recreates the queue and why.
- [ ] **Snapshot-under-lock, project-off-lock.** Still open: `observe` holds the
      session-wide request lock across the projection incl. `MetaLabelProvider`.
      Capture a tiny structural snapshot under the lock, build the view off-lock.
- [ ] Flag/fix the latent React-layer issue: heartbeat `synthesizeModelEvents`
      mutates the control tree without the request lock.
- [ ] Real quiescence signal beyond the synchronous case (await pending model
      events / async work before returning an observation).
- [ ] Security: confirm the endpoint enforces the same permission checks as the
      UI for every `act`; decide whether it is enabled per-environment (dev vs.
      prod) and how it is authenticated for a non-browser client.
- [ ] Decide raw-tree vs. curated projection and whether nodes carry semantic
      role metadata at the control level (see **D4**).
- [ ] Bound the observation size for very large trees (paging / subtree
      addressing).

### Phase 5 — MCP wrapper / external agent ⬜

- [ ] Thin MCP server mapping `observe` / `list_actions` / `act` /
      `wait_for_settled` onto the HTTP endpoint.
- [ ] Auth/session handling for the MCP client.
- [ ] End-to-end: an external agent drives a live TL session through MCP.

### Phase 6 — Productionization ⬜

- [ ] Servlet-level integration test (boots the container, not just unit tests).
- [ ] Performance check on large views (projection cost, lock contention).
- [ ] Developer documentation (how to make a control well-addressable /
      well-described for agents).
- [ ] Decide final package/module placement and public API surface.

---

## Legacy ScriptingRecorder — parity gap analysis (2026-06-25)

Survey of `com.top_logic.layout.scripting` (the `ScriptingRecorder` / `ApplicationAction`
stack) mapped against the React headless interface. Legend: ✅ have · 🟡 partial ·
❌ missing · ⚪ N/A (legacy mechanism not applicable; the React layer solves it
differently). The point is to scope **what a "real test scenario" needs** before
building recorder-side parity.

| # | Capability | Legacy (key class) | React headless | State |
|---|------------|--------------------|----------------|-------|
| 1 | **Object/model references** | `ModelName` + `ModelNamingScheme`/`ModelResolver` (150+ schemes) | **Same infra reused** — `AgentModelKey` = JSON `ModelName`; `ReactActionContext` drives `ModelResolver.locateModel`; React-context schemes (`ReactOptionByLabelNaming`) | ✅ |
| 2 | **Component/field addressing** | Fuzzy label/name/path (`FuzzyComponentNaming`, `*FieldRef`) | Semantic `role[name]` path (`AgentTreeProjector`) — different mechanism, same intent | ⚪→✅ |
| 3 | **Command/button execution** | `CommandAction` (by id / fuzzy label) | Browser command captured at the control's semantic address | ✅ |
| 4 | **Tab/route navigation** | `FuzzyGotoActionOp` (tab-path), `TabSwitch` | `selectTab` recorded; `tabBar[active]` addressing; `/navigate` route primitive | ✅ |
| 5 | **Form field input** | `FormInput` (CANONICAL/INTERACTIVE/RAW modes), typed `*FieldRef` | Browser field command via `recordCommand`; dropdown→`selectByKey`. Generic typed inputs (text/number/date) recorded verbatim — replay-stability not yet proven per field type | 🟡 |
| 6 | **Selection (single)** | `SelectAction` (ABSOLUTE) + `SelectionRef` | Table `selectByKey` (index→business key), replay-stable across sort | ✅ |
| 7 | **Selection (multi / range / tree subtree)** | `SelectAction` INCREMENTAL/SUBTREE | Modifier/range selections stay **index-based** (not key-stable); tree selection unproven | ❌ |
| 8 | **Value assertions** | `ValueAssertion`/`CheckAction` + `ValueCheck` family | Generic `assertState` pseudo-command, subset compare, per-key `mismatches` | 🟡 |
| 9 | **Typed assertion library** | 40+ plugins: field error/mode/validity/label, `ModelNotExists`, table-contents, visibility | Only the one generic state-subset assert | ❌ |
| 10 | **Global variables / parameterization** | `GlobalVariableStore`, `SetGlobalVariableAction`, `GlobalVariableRef` | none — cannot capture a created object's identity and reuse it downstream | ❌ |
| 11 | **Script control flow** | `ActionChain`, `ConditionalAction`/`IfAction`, `IncludeScriptAction`, `DynamicAction` | flat ordered step list only | ❌ |
| 12 | **Persisted script format** | Polymorphic-config XML via `ActionWriter`/`ActionReader` (`.script.xml`) | in-memory `RecordedStep` + JSON over HTTP — no on-disk format, no reader/writer | ❌ |
| 13 | **Replay runner & failure report** | `ScriptedTest`/`ApplicationSession.process`, stop-on-first `ApplicationAssertion` | `/replay`: per-step `success`+`error`, top-level verdict; continues through steps | ✅ (differs: no stop-on-first) |
| 14 | **Recording on/off + noise filter** | `setRecordingActive`, `annotateAsDontRecord`/`mustNotRecord` | `ScriptRecorder` start/stop; per-control `nonRecordableCommands()` | 🟡 (no pause/resume) |
| 15 | **Insert-assertion UX (inspector)** | `GuiInspectorControl` + `AssertionPlugin` (right-click → pick check) | `/record/assert` endpoint only — no in-UI element inspector | ❌ |
| 16 | **Comments / metadata on steps** | `getComment`/`getFailureMessage`/`getUserID` per action | none | ❌ |

**What "real test scenarios" minimally need (parity backlog, priority order):**

1. **Global variables (#10)** — capture an object's `ModelName` into a named var and
   reference it later. Without it a create→use→assert scenario can't be recorded. Reuses
   `GlobalVariableStore` (already used by `ReactActionContext.getGlobalVariableStore`).
2. **Typed assertions beyond value (#9)** — at least field-error, field-mode/validity,
   visibility, and object-not-exists; these are what regression scripts actually check.
3. **On-disk script format + reader/writer (#12)** — a persisted artifact (decide:
   reuse `ApplicationAction` XML vs. the flat JSON step list) so scripts live as test
   resources, not just a session buffer.
4. **Stable multi/range/tree selection (#7)** — extend the key-based selection to
   modifier/range and tree rows.
5. **Insert-assertion inspector UX (#15)** and **recorder side-window parity** (the
   existing Phase 2 UI gaps) — the authoring surface.
6. **Script control flow + comments (#11, #16)** — chain/conditional/include and
   per-step comments, once the linear case is solid.

Items #1–#6 (addressing, navigation, command, single-select) are already at parity by
reusing the legacy naming infrastructure — the gap is concentrated in **authoring
breadth** (assertions, variables, control flow, format, inspector), not in the
core observe/act/resolve substrate.

## Open design decisions (decision log)

### D1 — Addressing scheme `DECIDED`

Decided so far (with the user): two consumers with different needs — the live agent
re-observes each step (cheap path is fine), the recorder needs identity that survives
data/locale/layout/session. Direction: **path stays the human handle; the recorded
identity is a locator (semantic criteria + stable keys)**; data nodes carry a stable
**business key = a `ModelName` serialized as JSON** (reusing the script recorder's
object naming + `ModelResolver`, via `JsonConfigurationWriter`).

Done (`AgentModelKey`): table rows and dropdown options now carry a `key` — the
JSON `ModelName` of the row/option business object. Verified live on the green-field
table: e.g. `{"model-name":["…TLObjectByLabelNaming…",{"class-name":"DemoTypes:A","object-label":"Part 1"}]}`
— a real, KB-resolvable global name independent of index/sort/session.

**Walls:**
1. **Resolution context. `DECIDED` — option (a): `ReactActionContext`.** A thin
   `AbstractActionContext` subclass carrying only the `DisplayContext` + `HttpSession`
   the React view layer actually has; `getMainLayout()` throws. It drives
   `ModelResolver.locateModel` for both global schemes (KB-backed, context-light) and
   the React layer's own context-relative schemes. Verified live round-trip (a group by
   its context-relative `{"label":…}` key **and** a person by its global key, resolved
   in one `selectByKey` call). Component-relative *legacy* schemes (which need a
   `MainLayout`) remain out of scope — correctly, since the React layer defines its own
   context-relative schemes instead (see `ReactOptionByLabelNaming`).
2. **Naming-scheme stability. `DECIDED` — keep the by-label default.** The default
   scheme is *by label* (`TLObjectByLabelName`), and that is the right choice: in this
   model the label typically **is** the business identifier, so a by-label key is both
   stable and human-readable. The tempting alternative — a KB id / internal business
   key — is actually *worse*: internal ids are not stable across a DB reset and break
   in test cases (a recorded script keyed on an id would not replay against a freshly
   initialized database). The `buildName`-declines-on-collision behavior already
   prevents identity loss when a label is non-unique (it falls back to the global
   scheme), so by-label does not sacrifice correctness. No scheme-priority tuning
   needed.

### (superseded) D1 — Addressing scheme `OPEN` (gates Phase 1)

How does a node get a stable, replayable address?
- (a) Purely structural paths (current prototype) — simplest, fragile for data.
- (b) Model-reference based (legacy `ModelName` style) — stable across runs,
      heavier.
- (c) **Hybrid** (recommended): structural for chrome, model-key for data.

Recommendation leans (c). Decide before building Phase 1/2.

### D2 — Headless transport `TENTATIVE`

Server-side session API (reuse `executeCommand`) vs. client-mediated (drive a
real browser/SSE client). Prototype uses the **server-side** path.
`TENTATIVE: server-side`, unless byte-identical browser behavior is required.

### D3 — One artifact or two `OPEN`

Is the recorded format also the agent's action log (one replayable artifact), or
are scripted tests and agent traces separate things sharing only the addressing
layer?

### D4 — Raw tree vs. curated projection `OPEN`

Does the agent see the raw control state tree (presentation-shaped) or a curated
semantic projection? Prototype exposes the raw tree with opt-in `AgentNode`
refinement. Decide whether controls should contribute semantic descriptors at
source.

### D5 — Action-space exposure `DECIDED` (typed config args; build in progress)

Untyped command names vs. advertised argument schemas. **First step shipped:**
`@ReactCommand.params()` + `@ReactParam`. **Now decided** (2026-06-26, with the user):
a command's arguments are modelled as a **`ConfigurationItem` that is also the actual
argument carrier** — the handler receives the typed config (bound from the client JSON
via `JsonConfigurationReader`), not a raw `Map`. One interface is the single source for
the call arguments, the advertised schema, the human-readable rendering, and the
persisted form — no parallel descriptor, so nothing can drift.

This **supersedes `@ReactParam`** (a hand-rolled restatement of a config descriptor) and
keeps `executeCommand` the single behavior path (only the *type* the handler receives
changes). Rationale for choosing this over the two alternatives considered:
- *Descriptor beside the raw triple* — rejected: two representations of the same
  arguments that can disagree.
- *Full legacy `ApplicationAction` model* (typed action replayed via an
  `ApplicationActionOp.process()`) — rejected: reintroduces a second behavior
  implementation per action, the very thing this stack avoids.

Three capabilities fall out of the one interface, all by reuse:
- **Schema** — `JsonConfigSchemaBuilder` (`com.top_logic.basic.config.json`) → JSON Schema
  with types/`@Mandatory`/constraints/enums; also yields the `selectItem(itemId ∈ {…})`
  enum that retires the sidebar `items` size hotspot.
- **Human-readable display** — a per-arg-type `ResKey` interpolating the action's own
  values, *label-resolved* via `MetaLabelProvider`/`ModelResolver` (modelled on legacy
  `ApplicationAction` I18N). This is the recorder side-window's human-compatible step text.
- **Persistence** — the config instance written as JSON/XML (parity #12), `comment`
  riding along (parity #16).

Base type: a **lean React-side action-config base**, not legacy `ApplicationAction`
(avoids its `ComponentName`/`WindowScope`/`MainLayout` baggage). Remaining work tracked
under Phase 3; migrate recordable commands, leave chrome commands on the raw `Map`,
consider a conformance check that recordable `@ReactCommand`s declare a config arg type.

### D6 — Read concurrency model `OPEN` (gates Phase 4)

The headless read shares one live, concurrently-mutating tree with the browser.
How is a consistent, non-disruptive observation produced? Options:
- (a) **Snapshot-under-lock, project-off-lock** (recommended) — tiny critical
      section captures structure+state; labels/roles derived after unlock.
- (b) Lock-free best-effort read with retry-on-inconsistency.
- (c) Maintain the projection incrementally as part of the render pipeline.
Also decide whether `observe` should ever block user commands at all.

---

## Risks & caveats

- **Address drift** is the central risk; D1 must address it or recorded scripts
  rot (the exact problem the legacy recorder solved with `ModelName`).
- **Security**: a headless `act` surface is powerful; it must not bypass any UI
  permission check and should be gated per environment.
- **No servlet integration test yet** — only unit tests + a manual live run.
- The prototype validated the **happy path**; error/veto/dialog flows
  (e.g. `ChannelVetoException` dirty-check) are unexplored headlessly.

---

## Findings to fix (surfaced 2026-06-24 driving a real form end-to-end)

- [x] **Field-name instability across view/edit — FIXED.** Root cause: the chrome
      control was named from its display `label`, which is the technical attribute
      name in the placeholder form but the localized label once an object is loaded.
      `ReactFormFieldChromeControl` now carries a stable `agentName` (the technical
      attribute name, set by `AttributeFieldControl`), so the field address is the
      technical name in both states. Verified live: `formField[members]` in
      placeholder and loaded-edit; `…/formField[members]/dropdownSelect` resolves and
      `loadOptions` returns 9 options.
- [x] **Routed nav items — NOT a bug (resolved).** `selectItem` *does* drive routed
      items; the earlier "no-op" was my own wrong argument key (`{id}` instead of the
      real `{itemId}`). Confirmed live: `selectItem {itemId:"administration"}` →
      lands on the access-control area. The actual lesson is **D5**: the command's
      argument is not advertised, so a consumer must guess it. Separately added a
      route primitive `POST /agent-api/navigate {url}` (validated navigating
      access-control accounts↔groups) for areas addressed by URL/back-forward — a
      bonus, not required for routed sidebar items.
- [x] **Table rows projected — FIXED.** The table's real `rows` state holds cell
      *controls* (stripped from the agent projection). `TableViewControl` now also
      projects a text `rows` list — `{rowIndex, selected, cells:{column→text}}` for
      the current viewport (capped at 100; `totalRowCount` signals more, reachable via
      `scroll`). Verified live: an agent read the green-field table, found the row
      whose `name` cell is "Tool 2" (rowIndex 3), and `select {rowIndex:3}` selected
      it — choosing by content, not a blind index.

## Progress log

- **2026-06-26** — **Recorder fidelity: technical-command classification, control-derived
  descriptions, and a checkbox-binding fix**, verified live.
  - **Checkbox bug fixed.** A checkbox's `valueChanged` sends a JSON `boolean`, which failed to bind
    into the base field's `String` value (*"Error when reading the content"*). The field value is
    polymorphic, so — rather than fall back to a schema-less `Map` — the checkbox declares its own
    `CheckboxValueArguments` (`boolean`); the echo-suppressed apply was factored to a shared
    `applyClientValue` the subclass reuses. Verified: toggling records `{value:false}`, no error.
  - **Technical/chrome commands are no longer recorded.** New co-located `@ReactCommand(technical =
    true)` flag (captured by `ReactCommandMap`, unioned into `nonRecordableCommands()`/the agent
    action space) replaces the drift-prone per-control `agentHiddenCommands()` sets (migrated
    sidebar/appShell; marked snackbar `dismiss`, panel toggle/popOut, drawer/menu `close`, form-group
    collapse). Verified: a snackbar dismiss no longer appears as a step.
  - **Button/field steps read for humans.** A button click and a field value need the *control's*
    identity, not its args — and a table-cell field has no chrome parent, so the identity comes from
    the step **address** (its last `[name]` segment), passed into `describeCommand(command, args,
    targetName)`. The button renders *"Button 'Neu' pressed"* (its label state), the field *"Set
    'name' to 'value'"*. Verified live: *Button 'Bearbeiten' gedrückt*, *'boolean' auf 'false'
    setzen*, *'name' auf '…' setzen*.
  - *Follow-up noted:* the table `selectByKey` step still renders the raw `ModelName` JSON business
    key (*Zeile '…huge json…' auswählen*) — the same value→label resolution deferred earlier; a
    `selectByKey` should describe the row by its label, not its key.
- **2026-06-26** — **Typed arguments rolled out to (almost) all commands.** Migrated every
  scalar-argument `@ReactCommand` handler across the control library to a typed
  `ConfigurationItem` (table: sort/scroll/select/columnResize/columnReorder/expand/openFilter/
  moveSelection/selectAll/setFrozenColumnCount/selectByKey; tree: expand/collapse/select/
  contextMenu/dragOver/drop/contextMenuAction; nav/layout/overlay/sidebar: navigate/selectItem/
  executeCommand/toggleGroup/selectChild/resize/dismiss/reportDisplayClass; form fields:
  valueChanged for text/number/date via a shared `FieldValueArguments`). Each carries a `@Label`
  recorder template; `@ReactParam` is gone from these. Parallelized the mechanical bulk across
  three subagents (table / tree / structural), kept the entangled form-field family by hand, and
  integrated centrally. Verified live: every migrated command projects its JSON schema, and
  `sort`/`scroll`/`setFrozenColumnCount` dispatch via `/agent-api/act` (`success:true`, table
  re-sorts).
  - **Two documented exceptions stay on `Map`** (a flat typed config can't model them):
    1. **List-of-primitive arguments** — `valueChanged`/`selectByKey` (dropdown, `string[]`),
       `paletteChanged` (color), `reorder` (dashboard). The config-JSON layer does **not** support a
       `List<String>` property: `JsonConfigurationReader.nextElement` reads every list element as a
       config *item* (object), and `JsonConfigSchemaBuilder.buildListPropertySchema` throws — so a
       typed `List<String>` arg fails to bind (caught live: *"Error when reading the content"*).
       These keep a raw `Map` + a lightweight `@ReactParam(type = "string[]")` schema. Fixing it
       properly means teaching the shared config reader/schema-builder to handle primitive-element
       lists — a core-config change for a separate, carefully-tested task.
    2. **Dynamically-keyed map** — split-panel `updateSizes` (`controlId → pixel`): arbitrary runtime
       keys, not named properties; stays a `Map`.
  - **Follow-up noted:** the browser-typing echo-suppression also suppresses the value-state echo on
    the **headless** `act(valueChanged)` path, so a re-`observe` of a base field shows the prior
    value (the field *model* is correct; only the projected `value` state is stale). The agent has
    no optimistic client copy, so for the headless path the value should still be pushed — a small
    targeted fix worth doing before agents drive form input heavily.
- **2026-06-26** — **Field input fixed + recorded as one step** (recorder quality / parity #5),
  verified live. Two coupled fixes to text-like field input:
  1. *No more mangled typing / per-keystroke round-trips.* The client (`useTLFieldValue`) kept
     transmitting `valueChanged` on every keystroke while the server echoed the value back
     (`putState(VALUE)`); a late echo of an earlier keystroke clobbered the newer optimistic local
     value (dropped characters), and recording widened the latency that exposed it. Fix:
     `ReactFormFieldControl` suppresses only the *redundant* self-echo (a coercion or dependent
     change still echoes), and the client debounces the send (300 ms) and flushes on blur
     (`TLTextInput`/`TLNumberInput`/`TLPasswordInput`). Verified live: char-by-char input stays
     intact, ~3 requests for 10 keystrokes, login (text+password) still authenticates (flush-on-blur).
  2. *Consecutive field edits coalesce into one recorded step* (the legacy `FormInput` semantics).
     `RecordedCommand` gained a `coalescing` flag; `ReactFormFieldControl.recordCommand` marks its
     `valueChanged` coalescing; `ScriptRecorder.record(step, coalescing)` supersedes the previous
     step when it shares the same `address` + `command`. **No control-type knowledge in the recorder
     loop** — it merges by data equality; the control declares intent through the `recordCommand`
     seam it already owns. `TestScriptRecorder` (6 cases) covers the merge semantics; verified live:
     typing three characters into a table-row Name field recorded a *single* `valueChanged` step with
     the final value, not three.
- **2026-06-26** — **Human-readable recorded-step rendering**, verified live — the headline of
  the typed-argument work. `ReactControl.describeCommand(command, args)` binds the arguments to
  the typed config and renders via `ConfigLabelProvider`; each arg interface's `@Label` doubles
  as the render template (`Navigate to '{itemId}'`, `Activate tab '{tabId}'`, `Select row
  {rowIndex}`), staying in the generated EN bundle (DE hand-authored). The bind logic was extracted
  to a shared `ReactControl.bindArguments` (the dispatch invoker now delegates to it). The recorder
  side-window's `RecordedStepsTable` gained a **Description** column: it resolves a step's address
  against the opener window (`RecorderAccess.openerRoot`) and calls `describeCommand`, falling back
  to the raw command + JSON for assertions / untyped commands / drifted addresses. Verified live in
  a German session: recording three main-window gestures showed *"Navigiere zu 'input-controls'"*,
  *"Navigiere zu 'tabs'"*, *"Tab 'details' aktivieren"* — the exact "descriptive text mixed with
  the action's values" the legacy `ApplicationAction` I18N gave, now from the same interface that
  yields the JSON schema. Raw values for now; friendly-label resolution deferred.
- **2026-06-26** — **Typed arguments extended to `selectTab` + table `select`**, verified
  live. Same pattern as `selectItem`: `SelectTabArguments` (`tabId`) and `SelectRowArguments`
  (`rowIndex` `@Mandatory` `int`, `ctrlKey`/`shiftKey` `boolean`); `@ReactParam` removed,
  arg-key literals unified onto the typed interfaces, the table's `recordCommand` translation
  still reads the same keys. Live on the demo: `/agent-api/observe` advertises `selectTab`
  (`required:["tabId"]`) and `select` with correct JSON-schema *types* — `rowIndex:"integer"`
  (required), `ctrlKey`/`shiftKey:"boolean"` (optional) plus generated titles/descriptions —
  and clicking table row "Part 7" selected it (detail panel populated), confirming the typed
  `int`/`boolean` binding dispatches. `TestAgentSession` still 12 green.
- **2026-06-26** — **Typed command arguments — first slice** (D5 core), unit-verified,
  live pending. A `@ReactCommand` handler may now declare a `ConfigurationItem` argument
  instead of a raw `Map`: `ReactCommandMap` captures the argument descriptor and
  `ReactCommandInvoker` binds the client argument `Map` into the typed instance through
  `JsonConfigurationReader` (re-serialized JSON → config), exactly as the user framed it
  ("read via its JSON serialization instead of a plain map"). The projector emits the
  command's JSON schema (`JsonConfigSchemaBuilder` → `JsonSchemaWriter`) as `argsSchema`,
  replacing the hand-rolled `@ReactParam` for typed commands; schema-building is
  fault-tolerant (no `ResourcesModule` → `null` schema, projection proceeds). New lean
  base `ReactCommandArguments` (plain `ConfigurationItem`); sidebar `selectItem` migrated
  to `SelectItemArguments`. `TestAgentSession` grew to 12 (binding + descriptor capture);
  generated EN labels feed both the schema and the planned `ConfigLabelProvider` rendering,
  DE hand-corrected (`Element-ID`). **Verified live:** clicking a sidebar item navigated
  (`/view/input-controls`, SSE `activeItemId` patch, no errors), and
  `/agent-api/observe?mode=actions` advertises `selectItem` with a derived JSON schema
  (`required:["itemId"]`, `title:"Item ID"`). Remaining: recorder-side human-readable
  rendering via `ConfigLabelProvider`, and migrating the other interactive commands.
- **2026-06-25** — **Recorder step-debugger**, verified live. Selecting a captured step in the
  recorder side-window and pressing Step replays that one step on the recorded (opener) window —
  the effect appears in the main browser window — and advances the selection to the next step.
  New reusable `ReactWindowReplay.act` runs a headless `AgentSession.act` against a sibling window
  (installs that window's subsession, runs under the session request lock, settles derived state,
  restores the caller's subsession); the duplicated `installSubSession` in `AgentServlet`/
  `ReactServlet` now delegates to it. Selection ↔ a `selection` channel reuses the `TableElement`
  pattern (guarded against feedback) plus a new `TableViewControl.selectRow(key)`. Replay bypasses
  `ReactServlet`, so it is not re-recorded. Live: two recorded increments stepped one at a time
  drove the counter 2→3→4, selection 1→2, no console errors.
- **2026-06-25** — **Recorder live pop-in via SSE**, verified live. Made `ScriptRecorder`
  observable; the recorder window's `RecordedStepsTable` listens to the **opener's** recorder
  and refreshes on each capture. Because the per-window `SSEUpdateQueue.enqueue()`/`flush()`
  is synchronized, the refresh (run on the main window's recording thread) pushes straight to
  the recorder window's own SSE connection — steps appear *as captured* with no Refresh
  button (removed) and no cross-window subsession machinery. Confirmed the design premise by
  measurement first: a recorder side-window is a distinct browser tab and therefore has its
  own `window.name` → own subsession → own `SSEUpdateQueue` (ViewServlet: "each tab gets its
  own independent subsession"; `/agent-api/windows` went 1→2 on opening the recorder). The
  windows are not shared, but the opener *tracks* the windows it opened (`WindowEntry`
  `openerWindowId` + reachable `getQueue`), which is exactly the push path.
- **2026-06-25** — **Legacy `ScriptingRecorder` parity survey done.** Mapped the
  `com.top_logic.layout.scripting` stack (action types, assertions, `ModelName` refs,
  variables, control flow, script XML format, replay runner, inspector UX) against the
  React headless design — see the [parity gap analysis](#legacy-scriptingrecorder--parity-gap-analysis-2026-06-25).
  Finding: the core substrate (addressing, navigation, command, single-select, replay)
  is already at parity because it **reuses the same `ModelName`/`ModelResolver`
  infrastructure**; the gap is concentrated in *authoring breadth* — global variables,
  the typed assertion library, an on-disk script format, stable multi/tree selection,
  and the inspector-driven assertion-recording UX. Defined a 6-item parity backlog in
  priority order (global variables first — without them a create→use→assert scenario
  can't be recorded).
- **2026-06-25** — **D1 fully DECIDED + Phase 1 drift contract done**, verified live.
  Naming-scheme stability resolved (with the user) in favour of the **by-label**
  default: the label typically *is* the business identifier, and KB ids are worse
  (unstable across DB reset, broken in tests). With D1 closed, implemented the
  remaining Phase 1 drift behavior: a control resolving recorded business keys now
  **fails loudly** instead of silently substituting. `ReactDropdownSelectControl`
  collects unresolved keys and returns `ERROR_OPTION_KEYS_UNRESOLVED__KEYS`; the
  table's `selectByKey` (was `void` → always success) now returns `HandlerResult`,
  failing with `ERROR_ROW_KEY_UNRESOLVED__KEY` when no row matches. `/agent-api/act`
  and `/replay` surface the failed `HandlerResult`'s localized message as an `error`
  field; `/replay` gained a top-level `success` (all-steps-passed verdict). Verified
  live on the accounts table: a valid row key selects (`success:true`), a bogus key
  fails (`success:false` + German message) — the *identical* call returned
  `success:true` on the pre-fix build (clean before/after across an app restart).
  Remaining Phase 1 item: an automated cross-session round-trip test (record in one
  session, replay in a fresh one) asserting this contract.
- **2026-06-24** — **Phase 2 replay-stable table selection**, verified live. `TableViewControl`
  now has a `selectByKey {key}` command (resolves the row business object globally through
  `ReactActionContext`, selects it) and a `recordCommand` override turning a plain
  `select {rowIndex}` into `selectByKey {key}`. Proof: a recorded group-row click captured as
  `selectByKey`, and replaying it *after re-sorting the table* still selected `securityOwner` —
  the row index changed but identity held. Select + table are now both replay-stable.
- **2026-06-24** — **Phase 2 assertion/observation steps**, verified live. A recording can
  now embed assertions, turning replay into a self-checking regression run.
  `POST /agent-api/record/assert {address, expect?}` appends an `assertState` step; on
  replay it verifies (not dispatches) the node's state against the expectation — subset
  match by canonical JSON, per-key `mismatches` on failure. Live proof on the group form:
  `assertState {value:"user"}` passes after selecting `user`; `{value:"superuser"}` fails
  reporting `expected:superuser, actual:user`. Compare logic factored to
  `RecordedStep.mismatchingKeys` and unit-tested. Reserved pseudo-command keeps the step
  format and `ScriptRecorder` unchanged.
- **2026-06-24** — **Phase 2 replay-stable arguments**, verified live. New
  `ReactControl.recordCommand(command, args)` hook (default verbatim) lets a control emit
  a replay-stable form at record time; `ReactDropdownSelectControl` overrides it to turn
  `valueChanged {value:[session ids]}` into `selectByKey {keys:[business keys]}` via the
  option scope + `AgentModelKey`. Proof: a real browser option click recorded as
  `selectByKey {keys:[…{label:securityOwner}]}` (not an id), and replaying that script
  against a fresh empty form reproduced the `securityOwner` selection. This unifies the
  recorder with the business-key/`selectByKey` resolve path — recordings are now
  session-independent for select fields.
- **2026-06-24** — **Phase 2 recorder/capture first slice** (capture + replay), verified
  live. `ScriptRecorder` (per window, on `SSEUpdateQueue`) captures each browser command
  as a `RecordedStep(address, command, arguments)`; the address is the control's stable
  semantic path via the new `AgentSession.addressOf` (inverse of `resolve`,
  unit-tested to round-trip). `/agent-api/record/{start,stop,steps}` drive it;
  `/agent-api/replay` runs a step list back through `act`. Live proof: 2 recorded counter
  clicks (`…/counter[Aufgaben] :: increment`) → replayed → counter 0→2→4. The agent `act`
  path is intentionally *not* captured (it bypasses `ReactServlet`); the recorder captures
  genuine user interaction. Open next: replay-stable arguments (translate session-id args
  like dropdown `valueChanged {value:[ids]}` to `selectByKey {keys}` at record time).
- **2026-06-24** — `selectByKey` hardened for **replay** and verified live: it now
  resolves against the model's authoritative option list (`SelectFieldModel.getOptions`)
  rather than only the options already streamed to the client, so a recorded
  `selectByKey` replays without first opening the dropdown (`loadOptions`). Proven live
  on group `superuser` (no members, `optionsLoaded=false`): a hardcoded recorded key
  set the value to `securityOwner` directly. Also unit-tested the scheme round-trip
  (`TestReactOptionByLabelNaming`, 4 cases: unique→resolve, ambiguous→decline,
  foreign→decline, unknown→fail-loud).
  - **`/agent-api/navigate` characterized + made honest.** In-area route navigation
    works (`access-control/accounts` → `access-control/roles` returns `success:true`,
    reached). The limitation is deep-linking into an area whose routing participants are
    not registered yet: `navigateToRoute` resolves only against already-registered
    participants, and an unloaded area's leading segment is loaded by a sidebar
    `selectItem`, not by route matching — so the route can't trigger the load. That is a
    routing-architecture question (route ≠ loader), deferred rather than guessed. The
    servlet no longer lies about it: `navigate` reports `success:false` with a message
    when it does not reach the requested URL (verified live), instead of a false OK.
- **2026-06-24** — D1 **round-trip resolve verified live** end-to-end through
  `/agent-api`, closing the build↔resolve loop. New `selectByKey` command on the
  dropdown takes the same business `key`s the projection emits and sets the selection
  by object identity. Driven headless (group `user` members): `selectByKey` with a
  group key `["…ReactOptionByLabelNaming$Name",{"label":"securityOwner"}]` (context-
  relative) **and** a person key (`IndexedObjectNaming`, global) in one call changed
  the value from `[anonymous,anonymous,root,root]` to exactly `[root,securityOwner]`.
  Both resolution paths proven: `AgentModelKey.fromJson` → `ModelResolver.locateModel`,
  routed by the `ContextDependent` marker (scope for context-relative names, global
  otherwise) through a new `ReactActionContext` (`AbstractActionContext` carrying only
  the `DisplayContext` + `HttpSession`; `getMainLayout()` throws — the resolution-context
  abstraction the React layer owns, with no `MainLayout`). Keys are now serialized
  polymorphically (`write(ModelName.class, …)` → `["ConcreteName$Type",{…}]`) so they
  are self-describing and the reader can pick the scheme.
  - The earlier-suspected "routed-act gap" was **a test-harness bug, not a code
    defect**: the harness sent `args` while the servlet reads `arguments`, so
    argument-bearing commands silently no-op'd (`success:false`). With the correct key,
    `selectTab` (routed nested tab bar) and table `select` both apply (`success:true`).
    Hardened `/agent-api/act` to accept `args` as an alias for `arguments` so the trap
    cannot recur.
- **2026-06-24** — D1 context-relative keys **verified live** (build direction):
  the dropdown emits `ReactOptionByLabelName` for uniquely-labeled options and degrades
  to the global key only on label collisions. See the detailed entry below.
- **2026-06-24** — D1 context-relative keys (first slice): the React layer defines its
  own naming scheme `ReactOptionByLabelNaming` (context type `ReactOptionScope`), so a
  select option is named/resolved by label *relative to its control* — no global
  uniqueness, no `MainLayout`. Confirms the design: React registers schemes keyed on
  React context types; the framework auto-selects by `C`; legacy schemes (bound to
  legacy `C` types) never fire here. Scheme registered (verified in boot log), dropdown
  wired to build option keys via the scope. **Verified live** (group `user` members
  dropdown, driven via `/agent-api`): the 7 uniquely-labeled options key as bare
  `{"label":…}` (the React context-relative `ReactOptionByLabelName`), while the 2
  options whose label collides between a Person *and* a Group of the same name
  (`anonymous`, `root`) fall back to the global `TLModelPartNaming` key — because
  `buildName` declines on a non-unique label, so identity is never lost. Same object,
  context-sensitive: group `securityAdministrators` keys as a global `StringNaming`/`Group`
  name in the *table* but as `{"label":"securityAdministrators"}` in the *dropdown*. No
  `priority` tuning needed — the global scheme only wins where the context-relative one
  declines. Still open: round-trip resolve (`locateModel`) is unit-level only; it is not
  yet wired into a live act command (e.g. selecting an option by its `{"label":…}` key),
  which is the next slice.
- **2026-06-24** — D1 business keys: `AgentModelKey` projects a stable `ModelName`
  (JSON) key onto table rows and dropdown options. Verified live — real KB-resolvable
  global names. Hit two decision walls: how to provide an `ActionContext` for *replay*
  resolution in the React view layer (no `LayoutComponent`), and whether to prefer a
  stable id naming scheme over the default by-label. Build side complete; replay
  blocked on decision 1.
- **2026-06-24** — D5 first step: commands now advertise argument schemas via
  `@ReactParam` on `@ReactCommand`, surfaced in the projection. Annotated
  selectItem/valueChanged/select/selectTab; verified live (sidebar advertises
  `selectItem(itemId)`). An agent no longer has to guess argument keys.
- **2026-06-24** — Worked the three live-found findings. (1) Field-name stability:
  FIXED (technical attribute name in both view/edit). (2) Table rows: FIXED (text
  `rows` projection; agent selects by content). (3) Routed nav: **not a bug** — my
  wrong arg key (`id` vs `itemId`); `selectItem {itemId}` drives it. Added a
  `/agent-api/navigate {url}` route primitive (works for registered routes). The
  recurring theme — guessing argument shapes — elevates **D5** (advertise action
  arg schemas) to the top priority.

- **2026-06-24** — Drove a real multi-step task entirely through `/agent-api`
  (login → `selectTab` Gruppen → `select {rowIndex:0}` → edit → `loadOptions {}` →
  `valueChanged {value:[ids]}`): set a group's members from the headless interface,
  options loaded (9) and selection applied. Confirms the interface is agent-operable
  for non-trivial forms. Surfaced three gaps (see *Findings to fix*): view/edit
  field-name instability, routed nav items not drivable, table rows not projected.

- **2026-06-24** — Address-quality fixes (advances **D1**): (1) reject default
  `Object.toString()` model labels (`Class@hashcode`) as names — they were unstable
  (hashcode per run) and ugly (`dropdownSelect[comtop_logic…@2c24205d]`); now such a
  node falls back to a role-only address. (2) Tab bars name themselves by their
  active tab, so nested tab bars read `tabBar[Outer]/tabBar[Inner]` instead of
  `tabBar/tabBar` (the doubling was legitimate nested tabs, not a bug — a tab's
  content is itself a tab bar). Also consolidated the naming seam: `agentName()` /
  `agentRole()` moved from the headless `AgentNode` interface onto `ReactControl`
  (alongside `agentTransparent()`), so controls name themselves without depending on
  the headless package. Verified live: `tabBar[Übersicht]`; a 958-node form view has
  no `Class@hash` names.

- **2026-06-23** — Phase 0 core landed (commit `90400a4561`): addressing +
  `AgentSession` + introspection hooks + demonstration test.
- **2026-06-23** — Model-derived naming (commit `c94f211b86`).
- **2026-06-24** — HTTP endpoint `AgentServlet` (commit `8a6b44f12c`);
  live-verified against the demo dashboard (observe real tree; act counter 0→2;
  browser updated via SSE).
- **2026-06-24** — Found concurrency bug while inspecting a live session: the
  headless interface shares the real control tree and projects it under the
  session-wide request lock, stalling the UI and producing flaky partial reads.
  Recorded under Phase 4 + decision **D6**. Fix direction:
  snapshot-under-lock / project-off-lock.
- **2026-06-24** — Added the affordance-first `mode=actions` view (flat list of
  actionable nodes, no hierarchy): 16 nodes, ~10.95 KB vs 17.7 KB tree. Read→act→read
  loop validated live. Next size lever is expressing the sidebar nav as a
  parameterized action (D5), since its `items` array now dominates the payload.
- **2026-06-24** — Stripped rendering-only state (`null` generically;
  per-control `agentPresentationKeys()`) and chrome commands
  (`agentHiddenCommands()`), same modular seam. Live payload ~20 KB → ~17.7 KB;
  sidebar actions `[executeCommand, selectItem]`, button state
  `[disabled, hidden, label]`; act through addressing still drives the UI.
- **2026-06-24** — Structural pruning landed, modularly (one polymorphic
  `agentTransparent()` per control, no type cascade). Live: addresses lose the
  stack/inset/slot scaffolding, payload ~27 KB → ~20 KB; act through the pruned
  address drives the real UI (counter 0→1 via SSE in a clean tab). Next size lever
  is the affordance-first `mode=actions` view (payload still dominated by raw
  state).
- **2026-06-24** — Could **not** reproduce "controls don't react" via automation
  (view tab + observe tab + sidebar switching + 2nd tab); server log clean. The
  lock theory was wrong (a completed request releases it). Removed the
  `getRootControls` heuristic and switched `observe` to project from the window's
  authoritative root (`SSEUpdateQueue.setRootControl`); verified the tree is now a
  single root reflecting the current view. Added window-named diagnostic logging,
  which immediately surfaced a new lead: the per-window queue is being **recreated**
  (same window, two queue identities) — the probable real cause to chase next.
