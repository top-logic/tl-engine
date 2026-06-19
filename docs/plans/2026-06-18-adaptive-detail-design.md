# Adaptive Detail (Responsive Master-Detail) Design

## Overview

A meta-UI concept for the React view layer (`com.top_logic.layout.view`) that lets a
single declarative configuration render as **two different presentations depending on the
available display space**, sharing all configuration (model builder, columns, form) between
them:

- **Wide screens** — a selector (table/tree) and a detail form side by side, separated by a
  draggable slider. Efficient, no back-and-forth navigation per selection.
- **Small screens** — the selector full-bleed; selecting an item drills into the detail
  (overlaying the selector), with breadcrumb/back navigation to return. The slider layout is
  unusable on a phone; this drill pattern is.

The two presentations are **not** authored twice. A new `<adaptive-detail>` element holds a
`<selector>` and a `<detail>` once and chooses how to compose them at runtime. This removes
the duplication/inconsistency risk of maintaining two parallel layouts (e.g. configuring the
table model builder in both).

The concept **nests**: an `<adaptive-detail>` may appear inside another's `<detail>`, modelling
several chained selection steps. Nesting yields cascading "Miller" columns on wide screens and a
multi-step drill-in (with a unified breadcrumb) on small screens — from the same config.

## Design decisions (settled)

1. **Where the wide-vs-compact decision lives:** a *server-known* display signal. The client
   measures the breakpoint and reports a display class to the server; the element renders only
   the chosen presentation. Rejected: a pure client-side CSS/JS swap that renders both subtrees
   (would instantiate stateful detail forms twice with competing channel state).
2. **Granularity:** a *single global* app breakpoint (like the existing 768px drawer breakpoint),
   not per-region measurement. One signal, simplest to reason about.
3. **Selection cardinality:** single selection per level.
4. **Compact navigation:** a *unified breadcrumb across all nested levels*. The outermost
   element coordinates the trail by walking the active nested chain; back nulls the deepest
   selection. Rejected: per-level back buttons with no combined trail.

## Part 1 — the global `displayClass` signal

There is currently **no** server-visible notion of available space. Responsiveness today is
ad-hoc and client-local:

- The sidebar→drawer swap is pure CSS at `@media (max-width: 768px)` in
  `com.top_logic.layout.react/.../style/tlReactControls.css`; Java always renders the drawer
  toggle and CSS hides it. The server never learns it is on a phone.
- `TLFormLayout.tsx` flips label position via a `ResizeObserver` against a hardcoded
  `LABEL_SIDE_MIN_WIDTH = 320`, entirely inside one component.
- No `useScreenSize()` hook, no display-class enum, no breakpoint theme token, no channel.

The signal closes that gap:

```
app shell (React)                  server (subsession)            view layer
matchMedia('(max-width:Npx)')  -->  DisplayClassCommand  -->  subsession value  -->  displayClass()
  debounced, fires on band change     writes the band       COMPACT | REGULAR        TL-Script fn
```

- **Client:** one `matchMedia` listener at the app shell (where sidebar/appbar already live),
  debounced, firing a command only on band *change* (not per resize pixel).
- **Breakpoint:** promote the hardcoded `768px` to a **theme token** so the drawer swap and this
  signal stay in lockstep instead of drifting.
- **Scope:** store per **subsession** (per browser tab), not per session — two tabs at different
  widths must differ. Rides on the existing `ViewServlet` / `ensureSubSession` machinery.
- **Exposure:** a `displayClass()` TL-Script function mirroring the existing `currentUser()`, so
  *any* view can branch on it via `<derived-channel>`, not only the new element. (Future cleanup:
  the `TLFormLayout` 320px hack and the CSS drawer swap could both consume this one signal.)

## Part 2 — the `<adaptive-detail>` element

A new `UIElement` with `@TagName("adaptive-detail")` in `com.top_logic.layout.view`. It is a
*composition* element (the documented role of this layer) — it delegates to existing controls,
it is not a new bespoke React component.

```xml
<adaptive-detail selection="selected">
  <selector>
    <table selection="selected" types="...">
      <rows>...</rows>            <!-- model builder: authored ONCE -->
    </table>
  </selector>
  <detail>
    <form input="selected"> ... </form>   <!-- detail: authored ONCE -->
  </detail>
</adaptive-detail>
```

### Config properties (initial)

| Property    | Type                | Required | Description                                              |
|-------------|---------------------|----------|----------------------------------------------------------|
| `selection` | `ChannelRef`        | yes      | Channel the selector writes and the detail reads.        |
| `<selector>`| view content        | yes      | The master control (table/tree) writing `selection`.     |
| `<detail>`  | view content        | yes      | The detail bound to `selection`; may nest `<adaptive-detail>`. |

### Presentation by display class

- **REGULAR** → a split (selector left, detail right), both bound to `selection`. Equivalent to
  today's master-detail (`layout.template.xml` / `FlexibleFlowLayoutControl` split + the
  `selection(...)` channel link), expressed in the view layer.
- **COMPACT** → selector full-bleed; when `selection` becomes non-null the `<detail>` replaces it;
  breadcrumb/back clears `selection`. Equivalent to the existing `ContextTileComponent` semantics
  (two children, toggle on selection).

### The stateless-depth insight

Drill state is **derived from the selection channels — there is no separate navigation state**:

> Compact drill depth = the number of non-null selections in the nested chain.
> Breadcrumb = the label at each level. Back = null out the deepest non-null selection.

Therefore a resize that flips the band is **lossless at any nesting depth**: both modes are pure
functions of the selections plus the children. Wide shows N cascading columns up to the deepest
selection; compact shows the deepest non-null level; the current selection survives the switch.

## Nesting

`<adaptive-detail>` nests inside another's `<detail>`. The same config drives both modes:

```xml
<adaptive-detail selection="selProject">
  <selector><table selection="selProject" types="...">...</table></selector>
  <detail>
    <adaptive-detail selection="selSprint">
      <selector><table selection="selSprint" input="selProject">...</table></selector>
      <detail><form input="selSprint"/></detail>
    </adaptive-detail>
  </detail>
</adaptive-detail>
```

| Mode    | Result                                                                                  |
|---------|-----------------------------------------------------------------------------------------|
| REGULAR | Cascading columns: `projects │ sprints │ sprint-detail`, each split independently draggable. |
| COMPACT | Multi-step drill: projects → sprints → sprint form, with back navigation.               |

A `<detail>` is arbitrary view content, so the "drives the next step **and/or** the detail" case
composes for free: a level may hold a form *and* a nested `<adaptive-detail>` together
(e.g. project form + sprint selector).

### Coordinator role (compact, unified breadcrumb)

The outermost `<adaptive-detail>` coordinates the breadcrumb: it walks the active nested chain and
builds `[root-selector-label] + [label(selProject), label(selSprint), …]`. Tapping a crumb nulls
every selection deeper than that level. This maps onto the existing `<tile-breadcrumb>` truncation
semantics, where the derived path *is* the selection chain. Consequence: the element is
nesting-aware, not purely self-contained per level.

## Open points to settle during build (non-blocking)

- **Empty upstream state (wide only):** before an upstream selection exists, downstream panes have
  no model and need a sensible "nothing selected upstream" placeholder. Compact sidesteps this
  (a pane is only seen after drilling in).
- **Split topology / deep nesting (wide):** default to **unbounded cascading columns** (horizontal
  scroll if very deep). Capping columns and drilling inside the rightmost pane is a later refinement.
- **Reuse for the compact renderer:** `ContextTileComponent`'s two-child toggle is the closest
  match for a fixed per-level drill; prototype with it before considering the full `tile-stack`.
- **Multi-select:** out of scope for the first cut (single selection only).

## Phased plan

1. **Phase 0 — `displayClass` signal.** Client `matchMedia` → `DisplayClassCommand` → subsession
   value → `displayClass()` TL-Script function; promote the 768px breakpoint to a theme token.
   Independently useful.
2. **Phase 1 — `<adaptive-detail>` element.** Delegates to split (REGULAR) and selection-driven
   context-toggle (COMPACT), sharing `<selector>`/`<detail>`; outermost instance coordinates the
   unified breadcrumb.
3. **Phase 2 — demo + verification.** Wire a working example into `com.top_logic.demo`, reachable
   in the browser, that exercises a **multi-step selection path** (at least two nested levels, e.g.
   project → sprint → detail) so both the cascading-column (wide) and multi-step drill (compact)
   behaviours are covered by one shared config. Validate manually with Playwright:
   - **Wide (REGULAR):** the nested levels render as cascading columns; selecting upstream
     populates the downstream selector and ultimately the detail form.
   - **Compact (COMPACT):** the same view drills step-by-step (selector → nested selector → detail)
     with the unified breadcrumb spanning all levels; tapping a crumb / back nulls the deeper
     selections.
   - **Resize flip:** with a selection at depth ≥ 1, flip the viewport across the breakpoint and
     confirm the selection is preserved and the presentation switches losslessly.

## Implementation status (2026-06-19)

Phases 0–2 are implemented and **verified in the browser** (`com.top_logic.demo`, view
`demo/responsive-md-demo.view.xml`, nested ProjectScope → Milestone → detail):

- **REGULAR (wide):** the nested config renders as three cascading columns (scopes │ milestones │
  detail) with draggable splitters; selecting a scope populates the milestone selector, selecting a
  milestone populates the detail form.
- **COMPACT (narrow):** the same config drills step-by-step (scopes → milestones → detail); a back
  bar per level clears that level's selection.
- **Resize flip (both directions):** lossless — the selection lives in the channels, so flipping
  across the breakpoint preserves it (wide→narrow drills to the deepest selection; narrow→wide
  restores the columns with downstream selectors still populated).

**Not yet implemented — the unified breadcrumb (the agreed compact-nav design).** The compact
presentation currently shows a *per-level* back bar, so nested levels stack two bars (`‹ P1`,
`‹ M1`). The agreed design is a single coordinated trail (`Scopes › P1 › M1`) where the outermost
element walks the nested chain and tapping a crumb nulls all deeper selections. Open sub-decision:
what the **root/home crumb** shows (a selector has no intrinsic title).

## Relevant existing code

- Split / slider: `com.top_logic/.../layout.template.xml`, `FlexibleFlowLayoutControl`,
  `FlexibleFlowLayoutRenderer`; channel link via the `selection(...#Component)` TL-Script function.
- Compact toggle: `com.top_logic.mig.html.layout.tiles.ContextTileComponent` (two children, toggle
  on selection); `RootTileComponent` + breadcrumb.
- New-style tiles: `com.top_logic.layout.view.tiles` — `TileStackElement`, `TileBreadcrumbElement`,
  `TileFrame`, `NavigatePushCommand`.
- Responsive precedents: `tlReactControls.css` (`@media (max-width:768px)`),
  `react-src/controls/TLFormLayout.tsx` (`ResizeObserver`, `LABEL_SIDE_MIN_WIDTH`),
  `FormLayoutContext.ts`.
- Model builder shared by tables and tile selectors: `com.top_logic.mig.html.ListModelBuilder`.
