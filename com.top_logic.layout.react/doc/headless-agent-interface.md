# Headless Agent / Script Interface for the React View Layer — Plan & Progress

> **Status: PROTOTYPE.** A proof-of-concept exists, is unit-tested, and has been
> exercised live against the demo. None of this is production-ready or merged.
> This document tracks the design questions and the work to turn the prototype
> into a real feature.

- **Branch:** `CWS/CWS_29108_headless_agent_interface` (off `CWS/CWS_29108_integration`)
- **Code:** `com.top_logic.layout.react.headless` (package), `AgentServlet` (HTTP)
- **Owner:** bhu
- **Last updated:** 2026-06-24

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

### Phase 1 — Addressing hardening ⛔ (blocked on decision D1)

The load-bearing phase. Index-disambiguated labels are fine for a demo but
fragile for recorded scripts (labels duplicate, reorder, localize, change).

- [ ] Decide the addressing scheme (see **D1**).
- [ ] Introduce a stable identity for **data** nodes (rows, list items) — a
      model/business-key reference, the `ModelName`/`SelectionRef` analogue, not
      a positional index.
- [ ] Keep structural paths for **chrome** (buttons, tabs, panels).
- [ ] Define behavior when an address no longer resolves (drift): hard-fail for
      replay vs. best-match/re-plan for an agent.
- [ ] Round-trip tests across re-render and across a fresh session.

### Phase 2 — Recorder side ⬜

We built `act`; we have **not** built capture.

- [ ] Intercept the browser command stream (`/react-api/command`) and translate
      each into an `(address, command, arguments)` step using the live control's
      semantic address.
- [ ] Define the recording file format (and whether it is also the agent's
      action log — see **D3**).
- [ ] Replay a recorded file through `AgentSession` headlessly.
- [ ] Assertions/observation steps (record "expected state at this point").
- [ ] Migration story / coexistence with legacy `ScriptingRecorder`.

### Phase 3 — Action space & affordances ⬜

- [ ] Optional argument schema on `@ReactCommand` (names/types/required) so the
      action space is introspectable without each control implementing
      `AgentNode` by hand.
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

- [ ] **Snapshot-under-lock, project-off-lock.** Capture an immutable structural
      snapshot (ids, module, copied state, child-id lists, model refs) inside a
      *tiny* critical section, then build the `AgentNodeView` (roles, labels)
      after releasing the lock. Fixes both defects above.
- [ ] Remove `MetaLabelProvider` (and any KB access) from the critical section.
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

## Open design decisions (decision log)

### D1 — Addressing scheme `OPEN` (gates Phase 1)

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

### D5 — Action-space exposure `OPEN`

Untyped command names (current default) vs. mandatory argument schemas. Affects
how reliably an agent can construct valid `arguments`.

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

## Progress log

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
