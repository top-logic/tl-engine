---
name: toplogic-reusable-view-element
description: This skill should be used when building, refactoring, or reviewing a TopLogic React view element (a UIElement + ReactControl pair in the tl-layout-view layer, declared as a <tag> in a .view.xml) to make it generic, reusable across applications, and themeable - i.e. turning a bespoke, app-specific control into a declarative, config-driven element. Use it whenever the work touches: choosing a view element's configuration surface (rows/columns/lines, channels, expressions), deciding between a table/list/tree/grid for a UI, wiring elements together via channels vs nesting, defining or scoping CSS design tokens so any app's own theme can restyle a component, or staging an engine feature as a fork-first-then-upstream change. Reach for it even when the user only says "make this component reusable", "add a config option to this element", "why does my element look unstyled/square outside the chat", "group this list by X", or "how should our design tokens work so a theme can change the look".
version: 1.0.0
---

# Making TopLogic view elements reusable and themeable

This is a playbook for turning a bespoke, hand-rolled TopLogic control into a **generic, declarative,
themeable view element** that other applications and use cases can reuse. It was distilled from
extracting the chat's session rail into a generic `<data-list>`, and it encodes the traps that cost
the most time so you avoid them.

The through-line: a reusable element is one whose **behaviour is fixed but whose data, display,
wiring, and look are all supplied from outside** — TL-Script expressions, channels, typed config, and
theme tokens. Get those four seams right and the same element serves many screens.

## 1. Pick the right primitive — match the widget to the UX, not the data

Before writing anything, name what the thing *is* as a UI, not what data it holds. The engine already
ships primitives (`<table>`, `<tree>`, `<object-list>`, `<sidebar>`, `<form>`, `<button>`, containers);
reusing one, or copying its shape, beats inventing.

- A **data grid** (`<table>`) has column headers, filters, a column-config gear. Right for tabular
  data, wrong for a rail/menu — a grid dropped into a sidebar looks "horrendous".
- A **styled selectable list** (a rail, a picker) is a different primitive: styled rows, grouping
  headers, search, a selection. The engine's navbar (`SidebarElement`) renders that look but is
  static/route-based; `<object-list>` is data-bound but renders per-item *templates* with no
  selection/grouping/search. When no existing element fits the *presentation*, that gap is the real
  thing to build — not a mis-fit of a table.

Cost of getting this wrong: two rebuilds. Spend the thinking up front. If unsure, sketch the DOM/look
you want and ask which existing element renders like that.

## 2. Design the config surface — mirror siblings, put policy in config

An application configures a reusable element; it should feel like the elements it sits beside.

- **Copy the data-binding vocabulary of the closest sibling.** `<table>` binds data with
  `types` + `rows` (a TL-Script `Expr`) + `inputs` (channels) + `observed-types` + a `selection`
  channel. A new list/grid element should use the *same* names so an admin binds "which objects" the
  same way. Differ only where the element genuinely differs (e.g. a list's display is vertical
  `<lines>` where a table's is horizontal `<columns>`). Copy a name only when your element *reads*
  it: `<data-list>` took `types` along with the rest and nothing ever looked at it - attributes
  resolve against the row object's own type - so it shipped an option that did nothing.
- **Policy belongs in configuration, not in a bespoke server function.** A `chatSessions()` method
  that returns "the current user's CHAT sessions" bakes in a policy and is ambiguous. Prefer a generic
  `rows` expression the application writes:
  `all(\`m:Type\`).filter(x -> $x.get(\`m:Type#owner\`) == currentUser()).sort(...)`. Now the app
  decides which objects, whose, and in what order — no one-off method to maintain.
- **Type config properties so the design editor gives the right editor** (this is free and matters):
  - an expression → `com.top_logic.model.search.expr.config.dom.Expr` (renders as a TL-Script field,
    not a plain string)
  - a channel → `ChannelRef` with `@Format(ChannelRefFormat.class)`
  - a model type → `TLModelPartRef` (typed picker)
  - a label/text → `ResKey` (localizable field)
  - an attribute name → plain `String` (like `<column attribute="...">`); offer an `Expr` *value*
    alternative for a computed cell/line, since attributes can't compute.
- **Every expression is one-argument-per-row where it applies.** A per-row expression is a lambda
  `row -> ...`; run it with `QueryExecutor.compile(expr)` then `execute(row)`.

## 3. Respect the composition seam — channels link elements, nesting does not

The single biggest constraint on decomposition in `tl-ai`: **`tl-react-bridge` exposes no way for a
tl-ai React component to host engine child view-elements** (only `React`, `register`, `useTLState`,
`useTLCommand`). Consequences:

- You **cannot** build a `<container>` element in `tl-ai` that wraps *separate* `<button>`/`<list>`
  child elements and renders them in your own React tree. A reusable composite must be **one element**
  that renders its own DOM (composing plain React sub-components inside one bundle).
- **Separate view elements communicate by channels, not by nesting.** Two elements sharing a
  `selection` channel (one writes the object, the other reads it) is the idiom — see
  `invoices.view.xml` (`<table selection="c">` → `<form input="c">`) and the chat's
  `<data-list selection>` → `<chat-conversation input>`.
- **Decomposition trades cohesion for placement.** Pulling a control out into its own channel-wired
  element (e.g. a standalone collapse toggle) makes it placeable anywhere but visually *detached* — it
  becomes its own box outside the panel. If the control belongs *inside* a panel's chrome, keep it in
  the element; only split out what genuinely needs independent placement. When a split doesn't earn
  its keep, delete it — don't leave dead code.
- **Isolate the irreducible app-specific glue at the seam.** When an element must trigger app state a
  channel can't express (e.g. a chat "new conversation" resets a React store), put that in a tiny
  headless binding element that owns the coupling, and keep the generic element generic.

## 4. The Java↔React recipe for a data-bound element

When you do build the element, this is the mechanical spine (all public API, usable from an app
module — see `references/mechanics.md` for signatures and a worked skeleton):

- Compile expressions once in the control constructor (`QueryExecutor.compile`).
- Serialize each row to a small `Map<String,Object>` and push the list with
  `putState("rows", List<Map>)`; React reads `useTLState().rows` as an array.
- Keep it live with `com.top_logic.layout.view.model.RowSourceObserver` — its **`Consumer<List>` sink
  constructor** (not just the table one) re-runs the query on model/channel change; wire it with
  `addAttachListener(() -> observer.attach(context.getModelScope()))` / `addDetachListener`.
- React→Java actions use `@ReactCommandHandler` / `@ReactParam`, invoked with `useTLCommand()`.
- Two-way channels use `ViewChannel` `get/set/addListener` with a re-entrancy guard boolean to break
  the write→notify→write echo (copy the pattern from `ChatSessionsControl`).
- Give each row a stable id from `obj.tHandle().getObjectName().toExternalForm()` and keep a
  `Map<id,object>` so a `select` command can resolve the id back to the object.

## 5. Design tokens and theming — scope is the feature

An element is only themeable if it consumes tokens the app's theme can set, at a scope the element can
actually see. The traps here are about **scope**, not values:

- **Component tokens must live where every consumer inherits them.** The chat defined `--chat-sidebar`,
  `--r-*`, `--chat-small` on `.tl-cockpit` (the App root). A reusable element renders *outside* that
  root, so `var(--chat-sidebar)` resolved to nothing → broken geometry (square corners, wrong widths).
  Put component tokens at a shared root (`:root`, or a class the element carries), never on one
  component instance's root.
- **Base CSS a component relies on has the same trap.** The `button` reset and focus ring were
  `:where(.tl-cockpit button)`. Outside `.tl-cockpit`, buttons fell back to browser chrome (grey,
  square, bordered) — the classic "elements are always a different colour and square" symptom. A
  reusable element must carry its own reset/focus base, scoped to its own root.
- **Media queries scoped to the host leak too.** The chat's mobile-drawer rules target bare `.rail`,
  so a reused rail turned into a drawer at narrow widths. Override them under your element's root
  (`.my-el .rail { ... }`) with higher specificity to keep your element behaving as itself.
- **Layer the tokens so a theme can adapt look & feel:** theme *primitives* (`--background`, `--layer`,
  `--layer-hover`, `--layer-selected`, `--text-primary`, `--border-subtle`, `--button-primary`,
  `--corner-radius`, `--shadow-menu`) are owned by the app/theme; the element consumes only those (plus
  *aliases with fallbacks* for tokens a theme hasn't defined yet). Then an app restyles the element
  purely through its theme, touching no component CSS. Publish the element's token list as its
  contract.
- Default row/interaction styling that reads well: **flat and transparent by default, background on
  `:hover` (`--layer-hover`), a distinct background when selected (`--layer-selected`), rounded with
  the same radius as sibling controls** — never a persistent box.

## 6. Stage it fork-first, upstream later

The engine release cycle is slow; iterate in the app module and upstream once proven — exactly how
`<grouped-table>` and `<data-list>` were built.

- Build the element in the app module (e.g. `tl-chat`) on the engine's **public** API. If the engine
  can't be subclassed cleanly (private helpers), copy the minimal read path and mark the additions.
- Put a clear class-doc header: this is a temporary fork, the upstream target, and the removal
  condition ("delete when a `tl-engine` release ships X").
- Keep the fork self-contained and single-purpose so upstreaming is a clean diff and removal is
  deleting a package + swapping a tag.

## 7. Verify, and leave nothing dead

- Build the module (`mvn -o -pl <module> install -DskipTests` runs the JS typecheck + vitest + bundle
  even with `-DskipTests`, because those are `generate-resources`).
- Add a booted config-defaults test (plain `TestCase` can't instantiate config — `TypeIndex` isn't
  started; wrap with `ModuleTestSetup.setupModule(new TestSuite(Test.class))`).
- Walk it live in the real app, **both themes and a narrow width**, since the token/media-query traps
  above only show at runtime.
- Remove experiments that don't pan out — element, control, React, registration, CSS, and the config
  properties added only for them. Dead code is a maintenance tax and hides the real contract.

## Worked example

The chat session rail as a generic element lives in `tl-chat`:
`com.top_logic.ai.chat.view.list.DataListElement` / `DataListControl` +
`tl-chat/ui/src/data-list.tsx`, composed in
`tl-ai-demo/.../views/tl-ai-demo/chat-grouped.view.xml`. Read those alongside this playbook to see
each section applied. `references/mechanics.md` has the copy-paste Java↔React skeleton.
