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

## 3. Respect the composition seam — both nesting and channels are available

Two ways exist to put elements together, and they answer different questions.

**Nesting**: an app-module element *can* host arbitrary configured child elements. The Java side
builds each child's control (`ContentControls.toControl(elements, context.childContext(…)
.withChildSlotPath(id))`) and puts it into its own state; `ReactControl` serializes an embedded
control as a `{controlId, module, state}` descriptor, propagates attach/detach and disposal to it,
and the browser renders it with `TLChild` — **which `tl-react-bridge` does export** (`import { TLChild
} from 'tl-react-bridge'`, `<TLChild control={state.someChild} />`). `<chat-rail>`'s sections are
built this way; `TabBarElement` and `SwitchElement` are the engine's own examples. Two things to get
right: give each child its own child context, and keep a child **mounted but hidden** rather than
unmounting it when it goes out of sight — `TLChild` drops the child's SSE subscription on unmount and
remounts it from the descriptor's mount-time state, so a re-shown child would be stale.

**Channels**: for elements that are *siblings* in a view, or that must stay independently placeable.
Two elements sharing a `selection` channel (one writes the object, the other reads it) is the idiom —
see `invoices.view.xml` (`<table selection="c">` → `<form input="c">`) and the chat's
`<data-list selection>` → `<chat-conversation input>`. Note that a value derived in the *browser*
(a count the React store computes) reaches another element only by a command to its own control,
which writes the channel — a round trip. Prefer a channel whose value the server can compute.

**Ambient scope**: the third way, for the case where neither fits - several *peer* elements that must
agree on one value, and a channel would be wrong because the value is wiring, not data. The engine's
`ViewContext.withScope(Class, Object)` puts a value in reach of every descendant context
(`getScope(Class)` reads it, `childContext` inherits it, a nested `withScope` of the same type
overrides), and a container element declares it once for everything inside. `<chat chat-id="...">`
is the chat's use of it: `ChatScopeElement` extends the engine's `StackElement`, wraps the child
context in a `ChatScope`, and every `<chat-conversation>`, `<chat-rail>` and rail section inside
resolves *its own `chat-id`, else the scope, else a default*. `ReactTileStackControl` does the same
with `TileStackScope`.

Two rules that make this worth the class:

- **Declare the scope where the layout already is.** `ChatScopeElement` adds a `chat-id` to
  `<stack>`'s config and overrides `createControl` to derive the context - about fifteen lines, no
  control, no React element, and the app writes `<chat>` in place of the `<stack>` it already had.
  Wrapping *extra* nesting around existing elements is the version to avoid: the engine's
  `.tlStack:has(> .tlPanel--fill)` rule looks one level deep, so an added stack silently costs a
  `<panel fill="true">` its height.
- **Make the join say something when it is wrong.** An ambient default means an element that
  disagrees still renders - empty, forever, with no symptom. Tally the joins where they are all
  visible (for the chat, `ChatViewBinding` plus `ChatJoinLog`, keyed on the `InteractionContext`,
  which is one render) and log it: one INFO line per render with the ids and their counts, one WARN
  when a window holds several ids and one of them has a single element. A correct page must stay
  silent, or the log teaches people to ignore it.

Use nesting when one element is the *frame* of the others (a rail and its sections, a tab bar and its
tabs). Use a channel when they are peers and the value is data. Use a scope when they are peers and
the value is which thing they all belong to.
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
  the write→notify→write echo (copy the pattern from `ChatConversationControl`).
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
