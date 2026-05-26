# Dynamic Slot Content (Portal Slots)

**Status:** Draft — design only, no implementation
**Ticket:** #29108
**Module:** `com.top_logic.layout.view`

## 1. Problem

Many UI patterns need a descendant view to project content into an ancestor region:

- A drill-down view contributes its breadcrumb to the app bar.
- A form view contributes its "Edit" / "Save" buttons to the app bar's action area.
- A wizard step contributes its "Next" primary-action button to the wizard frame.
- A detail view contributes status indicators to a tab's title bar.

What these have in common is structural: **the content is owned by the descendant
(its lifecycle, its data bindings, its channel references), but rendered by the
ancestor.** The descendant's locally-declared channels and bindings should not have
to escape their scope just to be visible at the rendering site.

TopLogic today has no general primitive for this. It has one ad-hoc mechanism —
`CommandScope` — that handles exactly one case (commands surfacing in the app-bar
toolbar) and only for one artifact type (`CommandModel`). Other cases either don't
exist (breadcrumb) or are handled by widening channel scope, which forces every
contributing module to register channels globally and destroys modularity.

The right primitive is a general one: a pair of regular UIElements — a **slot**
placeholder and **slot-content** contributor — that communicate through a
`ViewContext`-resident scope, named by string, scoped by `ViewContext` nesting.
Everything else (breadcrumb in app-bar, toolbar buttons, wizard primary actions,
status indicators) is an application of this one primitive.

## 2. Prior art in TopLogic

### 2.1 `CommandScope` — the ad-hoc precursor

`AppShellElement` (`com.top_logic.layout.view.element.AppShellElement`) seeds a
shared `CommandScope` into the `ViewContext` for its subtree (lines 122–126, 154):

```java
CommandScope sharedScope = context.getCommandScope();
if (sharedScope == null) {
    sharedScope = new CommandScope(List.of());
}
ViewContext scopedContext = context
    .withErrorSink(errorSink)
    .withCommandScope(sharedScope)
    ...;
```

Descendant elements (form edit commands, dashboard layout edit) call
`scope.addCommand(model)` on attach. `AppBarElement` reads from the same scope
(lines 139–159), renders entries with `TOOLBAR` placement as trailing action
buttons, observes changes via `scope.addListener(...)`, and removes contributed
commands on cleanup.

This is the right shape — descendant contributes, ancestor consumes, scope mediates,
listener observes change — but **narrowed to one type (`CommandModel`) and one
implicit target (the app-bar toolbar, picked by `CommandPlacement` enum).**
Generalizing it removes the special-case nature.

### 2.2 Static slots on `AppShellElement`

`AppShellElement` also uses "slot" in another sense (lines 44–46): the
`<header>` / `<content>` / `<footer>` configuration slots that determine
**structure** — *which* elements are mounted as the shell's three regions. These
are statically declared in XML when the shell is configured. They are not the
subject of this spec. Dynamic slot content is about *runtime* contributions to a
named region, not the structural composition of the shell itself.

## 3. Proposal

### 3.1 Mental model

`<slot>` and `<slot-content>` are **two ordinary UIElements that communicate
through a `SlotScope` resident in the `ViewContext`**, identified by string name.
Neither is privileged: any subtree can declare a slot at any nesting depth, and
any subtree can contribute to it.

```
+-----------------------------------------------------------+
|  view subtree                                             |
|                                                           |
|   ContainerA  (seeds SlotScope into ViewContext)          |
|   |                                                       |
|   +-- ... <slot name="foo"/>     <-- reads from scope     |
|   |                                                       |
|   +-- ... <slot-content slot="foo">   <-- writes to scope |
|              <some-ui-element/>                           |
|           </slot-content>                                 |
|                                                           |
+-----------------------------------------------------------+
```

- The **scope** is a mutable registry keyed by slot name.
- The **slot element** subscribes and renders contributions in registration order.
- The **slot-content element** registers its children on mount and unregisters on
  unmount.
- Mount/unmount is driven by the normal `UIElement` / control lifecycle — no
  separate route-awareness logic is needed.

The `<slot>` and `<slot-content>` elements know nothing about each other directly.
They both know about the `SlotScope`. Scope is the *only* shared state.

### 3.2 Slot position vs. slot scope

Two independent concerns, easy to confuse:

**Position** — where in the rendered UI does a contribution appear?
Determined by *where the `<slot>` element is positioned in its parent's child
list*. `<slot name="toolbar"/>` placed inside `<app-bar>` renders contributions
at that position inside the app bar. The slot's positional parent (`app-bar`)
is the **visual host**.

**Scope** — which `<slot-content>` elements can target which `<slot>` element?
Determined by which **ancestor container** has opted in by seeding a
`SlotScope` into its derived `ViewContext`. The slot's positional parent is
**not** required to be the scope owner. Both `<slot>` and `<slot-content>`
walk up their respective `ViewContext` chains to find the nearest seeded
`SlotScope`; they meet at the lowest common ancestor that opted in.

In the typical app, the only opt-in is `AppShellElement`. The app-bar contains
the `<slot>`, deep descendants contain `<slot-content>`, and they connect
through the single shell-level scope. The app-bar itself does **not** seed
anything; intermediate containers (`<stack>`, `<split>`, `<view>`, …) do not
seed anything; they pass `ViewContext` through unchanged.

### 3.3 Who opts in

Three categories of UIElement in practice:

1. **Scope seeders** — rare, deliberate. Containers that want to be a slot
   communication boundary. `AppShellElement` is the default seeder for the
   common case. Additional seeders only exist when an element wants its
   internal slot-content *isolated* from an outer shell scope: e.g. a wizard
   frame whose `primary-action` slot must not be filled by random contributions
   from elsewhere in the shell, or a modal dialog with its own action region.
   These are ~3–5 elements in the whole codebase, not a routine pattern.

2. **Slot hosts** — elements that render a `<slot>` placeholder somewhere in
   their content. The host renders the placeholder at the chosen position; it
   does **not** manage the registry. `AppBarElement` would be one such host;
   a status-bar element another. The host needs zero knowledge of which
   container above it owns the scope.

3. **Everything else** — transparent. Containers like `<stack>`, `<split>`,
   `<view>`, `<form>`, `<tab>`, `<grid>` do not seed scopes and do not host
   slots. They forward `ViewContext` to their children unchanged. They do not
   need to know the slot mechanism exists.

A `<slot-content>` element only requires that *some* ancestor in its chain has
opted in. The contributor is fully decoupled from which specific element owns
the scope and where the matching `<slot>` is rendered.

### 3.4 Where the scope API lives

```java
ViewContext scoped = context.withSlotScope(new SlotScope());
```

`AppShellElement` calls this once. Other seeders call it when they want
isolation. Inner seeded scopes shadow outer ones for descendants inside them,
exactly as `CommandScope` nesting works today. If no scope is seeded in any
ancestor, `<slot>` and `<slot-content>` both error at startup ("no enclosing
slot scope") — explicit failure rather than silent no-op.

### 3.5 Java surface

```java
public final class SlotScope {
    public SlotHandle contribute(String slotName, UIElement content);
    public List<UIElement> get(String slotName);
    public void addListener(SlotScopeListener listener);
}

public interface SlotHandle extends AutoCloseable {
    @Override void close();   // removes the contribution
}

public interface SlotScopeListener {
    void slotsChanged(Set<String> changedSlotNames);
}
```

`ViewContext` gains `getSlotScope()` / `withSlotScope(...)`, mirroring the
existing `CommandScope` pair.

### 3.6 XML surface

`<slot>` and `<slot-content>` are regular `UIElement` configurations. They can
appear anywhere a `UIElement` can — inside `<app-bar>`, inside `<stack>`, inside a
custom container, inside another `<slot-content>`. There is no positional or
container-type restriction.

**Slot placeholder:**

```xml
<slot name="secondary"/>
```

Renders the current contributions to `secondary` from the nearest enclosing
`SlotScope`. Optionally accepts a fallback for the empty case:

```xml
<slot name="secondary">
    <empty>
        <text>no breadcrumb</text>
    </empty>
</slot>
```

**Slot contribution:**

```xml
<slot-content slot="secondary">
    <tile-breadcrumb path="navPath">
        <home-label><en>Accounts</en></home-label>
    </tile-breadcrumb>
</slot-content>
```

The contribution's children are constructed in the *declaring* view's context —
so the `navPath` channel reference resolves against the view that owns it, not
against the slot's host. The slot host only adopts already-constructed controls
for rendering.

### 3.7 Lifecycle

| Event | Effect |
|-------|--------|
| `<slot-content>` element creates its child controls | Children registered into nearest `SlotScope`, listener notified, slot host re-renders. |
| Containing view detaches (route change, conditional unmount, exception in teardown) | `SlotHandle.close()` called via standard `UIElement` cleanup hook; contributions removed; slot host re-renders. |
| Multiple `<slot-content>` elements target the same slot | All contributions are rendered, in registration order, wrapped in a `ReactStackControl` (same pattern `AppShellElement.createSlotControl` uses today for static slots). |
| `<slot>` host has zero contributions | Renders the configured `<empty>` fallback, or nothing if none configured. Visual collapsing (zero height, hidden border) is a styling concern for the host element, not this spec. |
| `<slot-content>` declared but no enclosing scope seeds a matching scope | Startup error. |
| `<slot>` declared but no enclosing scope | Startup error. |

### 3.8 Conflict resolution

All contributions render, in registration order. The simplest rule, matches the
existing static-slot multi-element behaviour in `AppShellElement`, and has no
hidden precedence.

If a use case appears for "single-winner" or "highest-priority-wins", it goes on
`<slot-content>` as an explicit attribute (`replace="true"`, `priority="N"`) — not
as implicit behaviour and not in this spec.

### 3.9 Scope boundaries and nesting

Slot names are local to the seeding `SlotScope`. Two unrelated subtrees that each
seed their own `SlotScope` and each use `<slot name="primary"/>` and
`<slot-content slot="primary">` do not interfere — each pair communicates within
its own scope.

A `<slot-content>` finds its target by walking up the `ViewContext` chain to the
nearest `SlotScope`. To project content "past" an inner scope into an outer one,
the inner scope's seeding container would have to expose it (not part of this
spec; can be added later if needed via something like
`<slot-content slot="..." scope="outer">`).

## 4. Generalization: `CommandScope` becomes redundant

The existing `CommandScope` is a special case of `SlotScope` where:

- the artifact type is `CommandModel` instead of `UIElement`,
- the routing key is `CommandPlacement` (an enum) instead of a string slot name,
- the consumer is hard-coded inside `AppBarElement` instead of expressed as a
  `<slot>` UIElement,
- the contribution is via `ViewCommandModel.attach()` against an implicit scope
  pulled from `ViewContext`, instead of via a `<slot-content>` UIElement.

With the slot mechanism in place, the app-bar's toolbar is just another slot host:

```xml
<!-- before: app-bar has special <commands> child + hidden CommandScope logic -->
<app-bar variant="flat">
    <commands>
        <open-designer placement="TOOLBAR">...</open-designer>
    </commands>
    <title>Task Manager</title>
</app-bar>

<!-- after: app-bar has a regular slot; commands are slot-content contributing buttons -->
<app-bar variant="flat">
    <title>Task Manager</title>
    <slot name="toolbar"/>
</app-bar>

<!-- declared near the app-shell, or inside any descendant view: -->
<slot-content slot="toolbar">
    <button command="open-designer" placement="TOOLBAR">
        <label><en>Designer</en></label>
        <tooltip><en>Open the view designer.</en></tooltip>
    </button>
</slot-content>

<!-- and a form's edit command, declared inside the form view: -->
<slot-content slot="toolbar">
    <button command="form-edit"><label><en>Edit</en></label></button>
    <button command="form-save"><label><en>Save</en></label></button>
</slot-content>
```

The form's "Edit" / "Save" buttons appear when the form view is mounted, disappear
when it unmounts, render in the order they were declared. No `CommandScope`, no
`addCommand` / `removeCommand` API, no `CommandPlacement` enum, no cleanup hook
inside `AppBarElement`. The same lifecycle hooks that handle the breadcrumb
contribution handle these buttons.

**This spec does not commit to migrating `CommandScope` now.** It commits to making
the new mechanism general enough that such a migration is a pure refactor, not a
re-design — and that any *new* "contribute to ancestor" use case (breadcrumb,
status badge, wizard primary action, contextual filter chip) goes through the
slot mechanism directly without inventing a parallel narrow scope.

## 5. Worked examples

### 5.1 Breadcrumb in app bar

Drill-down view contributes its breadcrumb into the app bar's `secondary` slot:

```xml
<!-- app.view.xml -->
<app-shell>
    <header>
        <app-bar variant="flat">
            <title>Task Manager</title>
            <slot name="secondary"/>
        </app-bar>
    </header>
    <content>...</content>
</app-shell>
```

```xml
<!-- tiles-demo.view.xml -->
<view>
    <channels><channel name="navPath"/></channels>

    <slot-content slot="secondary">
        <tile-breadcrumb path="navPath">
            <home-label><en>Accounts</en></home-label>
        </tile-breadcrumb>
    </slot-content>

    <tile-stack path="navPath" initial="demo/tiles-demo/overview.view.xml"/>
</view>
```

The `navPath` channel stays local. The breadcrumb's path resolution happens
where the channel exists. Only the rendered control is adopted by the slot host.

### 5.2 Multiple tile-stacks sharing one breadcrumb (channel pattern)

This needs no slot mechanism — channel semantics already cover it. Included to
answer the natural question "how do I synchronize two stacks":

```xml
<view>
    <channels><channel name="navPath"/></channels>

    <slot-content slot="secondary">
        <tile-breadcrumb path="navPath"/>
    </slot-content>

    <split direction="horizontal">
        <tile-stack path="navPath" initial="left-overview.view.xml"/>
        <tile-stack path="navPath" initial="right-overview.view.xml"/>
    </split>
</view>
```

Two stacks bound to the same channel name in the same scope. Drill into either:
the channel updates, both stacks re-render, the breadcrumb re-renders.

### 5.3 Independent tile-stack tabs

Tab A and Tab B each declare their own local `navPath` channel and their own
`<slot-content slot="secondary">`. Only the active tab is mounted (nav-item
routing handles this). Only the active tab's slot-content is in the scope.
Switching tabs implicitly switches the visible breadcrumb. No app-level
multiplexer needed; no global channel needed.

### 5.4 Toolbar buttons from a form (CommandScope replacement)

Form view contributes its action buttons to the toolbar, no `CommandScope`:

```xml
<!-- form view -->
<view>
    <slot-content slot="toolbar">
        <button command="edit"><label><en>Edit</en></label></button>
        <button command="save" disabled-while-clean="true">
            <label><en>Save</en></label>
        </button>
    </slot-content>

    <form-component .../>
</view>
```

When the form unmounts, both buttons disappear from the toolbar automatically.

### 5.5 Wizard with its own private slot scope

A wizard frame seeds its own `SlotScope` and exposes a `primary-action` slot:

```xml
<wizard>  <!-- this element calls context.withSlotScope(new SlotScope()) -->
    <header>
        <stepper steps="..."/>
        <slot name="primary-action"/>
    </header>

    <step id="account">
        <slot-content slot="primary-action">
            <button command="next">Next</button>
        </slot-content>
        <form .../>
    </step>

    <step id="review">
        <slot-content slot="primary-action">
            <button command="submit" variant="primary">Submit</button>
        </slot-content>
        <review-panel/>
    </step>
</wizard>
```

The wizard's private scope shadows the app-shell's. `<slot-content slot="toolbar">`
declared inside a step would still bubble up to the wizard's scope first; if the
wizard's scope doesn't know `toolbar`, an explicit decision is needed (current
proposal: error; alternative: chain through to outer scope — open question 6.1).

## 6. Non-goals

- **Migrating `CommandScope` to slot-content as part of this work.** The design
  enables it as a future refactor; the migration is not in scope here.
- **Replacing the static `<header>` / `<content>` / `<footer>` slots on
  `AppShellElement`.** Those configure shell *structure* (which elements exist).
  They are unrelated to the dynamic mechanism here.
- **Reordering or priority across contributions.** Registration order is the
  contract.
- **Cross-window or cross-app-shell portals.** Scope is bounded by `ViewContext`
  inheritance.

## 7. Open questions

1. **Inner-scope passthrough.** When a `<slot-content slot="X">` is declared inside
   an inner scope that does not host `X`, should it bubble to the outer scope or
   error? Symmetric question for `<slot>`. Proposal: error (explicit is better).
   Alternative: implicit bubble (`scope="outer"` attribute to opt in).
2. **Naming.** Element names `<slot>` and `<slot-content>` chosen for symmetry and
   to match existing `AppShellElement` "slot" vocabulary, but conflict-rich at the
   reading level (the static slots and the dynamic slots share the word). Possible
   alternatives: `<outlet>` / `<contribute>`, `<region>` / `<region-content>`,
   `<projects-to>`. Bikeshed; will not block the design.
3. **Slot uniqueness within a scope.** If two `<slot name="X"/>` elements appear
   in the same scope (e.g. two app-bars), do contributions fan out to both, or is
   the second a configuration error? Proposal: fan out — contributions render in
   *all* matching slot hosts. Cheap to implement, matches the listener model.
4. **Empty-state contract.** Should `<slot>` always allocate space (and the host
   styles it as collapsed when empty), or should it actually not render anything
   when empty? Proposal: don't render when empty; host can wrap in its own
   container if it wants reserved space.
5. **Leak resistance.** If a view unmounts without `SlotHandle.close()` being
   reached (exception in teardown), the contribution leaks. Same risk in
   `CommandScope` today. Mitigation deferred — possibly weak references on
   contributions, possibly periodic sweep.
6. **Scope discovery API.** Should `<slot-content>` allow an explicit scope
   selector (`scope="app"`, `scope="wizard"`) for cases where the nearest scope
   is not the intended target? Adds complexity; defer until a real need appears.

## 8. Migration of existing code

Purely additive. No existing view XML breaks.

- `AppShellElement` gains a `SlotScope` seed alongside its existing `CommandScope`
  seed. Both coexist.
- `AppBarElement` is extended to accept nested `<slot>` children (in addition to
  its current `<commands>` block). The existing `<commands>` / `CommandScope`
  path remains untouched; the new `<slot>` path is opt-in.
- The tile-stack demo migrates: breadcrumb moved into a `<slot-content>` in
  `tiles-demo.view.xml`; `<slot name="secondary"/>` added to the demo `app-bar`.
- Optional follow-up ticket (not this one): port `<open-designer>` and all
  `placement="TOOLBAR"` commands to `<slot-content slot="toolbar">`-wrapped
  buttons. Then deprecate `CommandScope`, `ViewCommandModel`, the `<commands>`
  block on `AppBarElement`, and the `CommandPlacement` enum.

## 9. Implementation sketch (for sizing only)

| Component | Estimated effort |
|-----------|------------------|
| `SlotScope`, `SlotScopeListener`, `SlotHandle` | small (mirror of `CommandScope`) |
| `ViewContext.getSlotScope()` / `withSlotScope(...)` | small |
| `AppShellElement` seeds default `SlotScope` | trivial (3 lines) |
| `<slot>` element + control that subscribes/renders | small |
| `<slot-content>` element + register/unregister lifecycle | small |
| `AppBarElement` accepts nested `<slot>` children | small |
| `ReactAppBarControl` renders slot region | small to medium |
| Tile-stack demo migration | trivial |
| Multi-tab demo views | small |
| **(Follow-up, separate ticket)** CommandScope deprecation | medium (touches every existing toolbar consumer) |

This ticket: probably one to two days of focused work. Follow-up ticket:
separate, larger, well-scoped.
