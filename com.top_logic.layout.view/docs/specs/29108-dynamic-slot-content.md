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

`<slot>` and `<slot-content>` are **two ordinary UIElements that find each
other by tree position**. A `<slot-content slot="X">` contribution renders at
the position of the nearest `<slot name="X"/>` in the surrounding tree —
"nearest" measured by lowest-common-ancestor distance. No scope opt-in, no
registry seeded by an ancestor, no `SlotScope` API. The container structure of
the view itself is the routing mechanism.

```
+-----------------------------------------------------------+
|  view subtree                                             |
|                                                           |
|   AnyContainer                                            |
|   |                                                       |
|   +-- ... <slot name="foo"/>                              |
|   |                                                       |
|   +-- ... <slot-content slot="foo">                       |
|              <some-ui-element/>                           |
|           </slot-content>                                 |
|                                                           |
+-----------------------------------------------------------+
```

- The **slot element** is the placeholder. It renders all `<slot-content>`
  contributions whose tree-nearest matching slot is this one.
- The **slot-content element** is the contribution. On mount, it makes its
  children available to the nearest matching `<slot>`; on unmount, it
  withdraws them.
- "Nearest" means: walk up from `<slot-content>` to its parent, the parent's
  parent, … At each ancestor, check for a matching `<slot>` in any of the
  *other* subtrees rooted at that ancestor. The first match wins.
- `AnyContainer` above is *any* UIElement that accepts children — `<stack>`,
  `<view>`, `<app-shell>`, anything. The container does not opt into
  anything; it does not need to be aware that the slot mechanism exists.

The `<slot>` and `<slot-content>` elements still know nothing about each
other directly. The framework finds the match by traversing the live element
tree.

### 3.2 Slot position vs. slot routing

Two independent concerns:

**Position** — where in the rendered UI does a contribution visually appear?
Determined by *where the `<slot>` element is placed in its parent's child
list*. `<slot name="toolbar"/>` placed inside `<app-bar>` between `<title>`
and the right edge renders contributions at that position inside the app bar.

**Routing** — which `<slot-content>` elements get rendered by which `<slot>`?
Determined by tree-distance. A `<slot-content>` is routed to the nearest
`<slot>` with matching name as seen from its position in the tree. The
container that visually contains the slot (e.g. `<app-bar>`) does not need to
do anything — it just renders its children, one of which happens to be a
slot.

### 3.3 No opt-in needed

Because routing is structural, no UIElement opts into anything. There is no
"scope seeder" category, no `SlotHost` interface, no `withSlotScope(...)`
API. Every UIElement is uniformly transparent to the slot mechanism. The only
thing the framework provides is: given the current tree and a `<slot-content
slot="X">`, find the nearest `<slot name="X"/>` and route there.

**Isolation via tree position, not via explicit scope.** If a panel contains
its own `<slot name="primary-action"/>` and child views inside it use
`<slot-content slot="primary-action">`, those contributions go to the panel's
slot — they never escape outward because a closer match exists. A second
panel elsewhere in the tree with its own `<slot name="primary-action"/>`
similarly contains its own contributions. The two panels do not interfere,
without anyone declaring a "scope".

This subsumes everything the explicit-scope design needed:

- **Wizard private slot:** the wizard contains `<slot name="primary-action"/>`;
  step-internal `<slot-content slot="primary-action">` is closer to the
  wizard's slot than to any outer same-named slot, so it goes to the wizard.
  No `withSlotScope` call from `WizardElement`.
- **Multiple simultaneously-mounted wizards:** each wizard's subtree contains
  its own slot and its own steps. Each step's slot-content finds its own
  wizard's slot as nearest. No interference.
- **Modal dialog with its own action region:** same pattern as wizard. Dialog
  contains the slot; its descendants find that slot first.

**Prerequisite: containers must accept arbitrary UIElement children.** For a
`<slot name="X"/>` placeholder to render inside e.g. `<app-bar>`, the
`AppBarElement` config must accept arbitrary UIElement children at the
position where the slot is placed. Most existing layout containers
(`<stack>`, `<split>`, `<view>`) already do. `AppBarElement` today has a
closed schema (`<title>`, `<variant>`, `<commands>`); generalizing it to
accept arbitrary children is a separable refactor, prerequisite to adopting
slots in the app-bar, but **not** part of the slot mechanism itself. The slot
mechanism adds no new container-side requirement beyond "the container
renders its child UIElements".

### 3.4 Java surface

The slot mechanism is implemented as a framework-internal tree-aware
registry. From an application author's perspective there is no API to call —
the only surface is the `<slot>` and `<slot-content>` UIElements in XML.

For the framework's own use, the registry exposes:

```java
public interface SlotRegistry {
    /** Called by <slot> on attach. */
    SlotHandle registerSlot(UIElement slot, String name, SlotPosition position);

    /** Called by <slot-content> on attach. */
    SlotHandle registerContribution(UIElement contributor, String name,
                                    SlotPosition position, List<UIElement> content);
}

public interface SlotHandle extends AutoCloseable {
    @Override void close();
}
```

`SlotPosition` is whatever the framework needs to compute LCA distance —
typically a path from the root of the rendered tree to this element.
There is exactly one `SlotRegistry` per rendered view tree, established
implicitly by the framework. Application code does not see it.

### 3.5 XML surface

`<slot>` and `<slot-content>` are regular `UIElement` configurations. They can
appear anywhere a `UIElement` can — inside `<app-bar>`, inside `<stack>`, inside a
custom container, inside another `<slot-content>`. There is no positional or
container-type restriction.

**Slot placeholder:**

```xml
<slot name="secondary"/>
```

Renders all `<slot-content slot="secondary">` contributions whose tree-nearest
matching slot is this one. Optionally accepts a fallback for the empty case:

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

### 3.6 Lifecycle

| Event | Effect |
|-------|--------|
| `<slot-content>` element creates its child controls | Children registered with the slot registry; framework recomputes the nearest matching `<slot>` and notifies it; that slot re-renders. |
| Containing view detaches (route change, conditional unmount, exception in teardown) | `SlotHandle.close()` called via standard `UIElement` cleanup hook; contributions removed; affected slot re-renders. |
| Multiple `<slot-content>` elements target the same slot | All contributions render, in registration order, wrapped in a `ReactStackControl` (same pattern `AppShellElement.createSlotControl` uses today for static slots). |
| `<slot>` has zero contributions | Renders the configured `<empty>` fallback, or nothing if none configured. Visual collapsing (zero height, hidden border) is a styling concern for the containing element, not this spec. |
| `<slot-content>` declared but no matching `<slot>` exists anywhere in the tree at mount time | Configurable: error (default, surfaces typos and accidental orphaning) or silent (for opt-in tolerance to absent hosts). |
| A new `<slot>` mounts after some `<slot-content>` is already active | Framework recomputes nearest-match for every active contribution. Any contribution whose nearest slot is now the new one migrates to it; the previous slot re-renders without those contributions. |
| A `<slot>` unmounts while it has contributions | Framework recomputes nearest-match for those contributions. They migrate to the next-nearest matching slot, or become orphaned (per the rule above) if none remains. |

### 3.7 Conflict resolution

All contributions render, in registration order. The simplest rule, matches the
existing static-slot multi-element behaviour in `AppShellElement`, and has no
hidden precedence.

If a use case appears for "single-winner" or "highest-priority-wins", it goes on
`<slot-content>` as an explicit attribute (`replace="true"`, `priority="N"`) — not
as implicit behaviour and not in this spec.

### 3.8 Nested same-name slots

When two `<slot>`s with the same name exist in nested containers, each
`<slot-content>` is routed to the one nearest to it by tree-distance. The
inner slot is reached by contributions inside the inner container; the outer
slot is reached by contributions outside the inner container.

```
<outer>
    <slot name="toolbar"/>                  [A]
    <inner>
        <slot name="toolbar"/>              [B]
        <view-1>
            <slot-content slot="toolbar">   --> routed to [B]
                <button-x/>
            </slot-content>
        </view-1>
    </inner>
    <view-2>
        <slot-content slot="toolbar">       --> routed to [A]
            <button-y/>
        </slot-content>
    </view-2>
</outer>
```

The outer slot `[A]` shows `button-y`. The inner slot `[B]` shows `button-x`.
Neither shows the other. This is the entire isolation mechanism. There is no
"scope" object, no opt-in by `<inner>`, no `withSlotScope` call. The mere
presence of `<slot name="toolbar"/>` inside `<inner>` is what isolates
contributions originating from inside `<inner>`.

To explicitly target an outer slot from inside an inner same-named one, an
`<slot-content slot="X" target="outer">` (or similar) attribute could be
added later if a real use case appears — see open question 7.1.

## 4. Generalization: `CommandScope` becomes redundant

The existing `CommandScope` is a narrow precursor of the slot mechanism:

- the artifact type is `CommandModel` instead of arbitrary `UIElement` children,
- the routing key is `CommandPlacement` (an enum) instead of a string slot name,
- the consumer is hard-coded inside `AppBarElement` instead of expressed as a
  `<slot>` UIElement at an arbitrary position,
- the contribution is via `ViewCommandModel.attach()` against an implicit scope
  pulled from `ViewContext`, instead of via a `<slot-content>` UIElement at an
  arbitrary position in the tree.

With the slot mechanism in place, the app-bar's toolbar is just an inline slot:

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

### 5.5 Wizard with a private primary-action slot

A wizard frame contains its own `<slot name="primary-action"/>`. Step-internal
contributions are automatically routed to the wizard's slot rather than to any
same-named slot outside the wizard — because the wizard's slot is closer:

```xml
<wizard>
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

`WizardElement` does not opt into anything, does not call any framework API.
It just contains a slot. The framework routes each step's `<slot-content
slot="primary-action">` to the wizard's slot because it is the nearest match
by tree distance.

If something further out (e.g. the app-shell) also defines `<slot
name="primary-action"/>`, the wizard's slot wins for everything inside the
wizard; the outer slot still receives contributions originating from outside
the wizard.

A different question is what happens when a step contributes to a slot that
the wizard does *not* define locally (e.g. `<slot-content slot="toolbar">`).
That contribution walks past the wizard, finds the outer `<slot
name="toolbar"/>` (e.g. in the app-bar), and is routed there. The wizard does
not isolate names it does not itself host.

## 6. Non-goals

- **Migrating `CommandScope` to slot-content as part of this work.** The design
  enables it as a future refactor; the migration is not in scope here.
- **Replacing the static `<header>` / `<content>` / `<footer>` slots on
  `AppShellElement`.** Those configure shell *structure* (which elements exist).
  They are unrelated to the dynamic mechanism here.
- **Reordering or priority across contributions.** Registration order is the
  contract.
- **Cross-window portals.** Routing is bounded by the rendered view tree.

## 7. Open questions

1. **Explicit outer-slot targeting.** When an inner same-named `<slot>`
   exists, all inner `<slot-content>` is captured by it. If a deeply-nested
   view legitimately needs to project content to an *outer* same-named slot,
   the design currently has no syntax for that. Proposal: defer; add
   `<slot-content slot="X" target="outer">` (or a path expression) only when a
   real use case appears.
2. **Naming.** Element names `<slot>` and `<slot-content>` chosen for
   symmetry, but conflict-rich at the reading level (the static configuration
   "slots" on `AppShellElement` and the dynamic slots here share the word).
   Possible alternatives: `<outlet>` / `<contribute>`, `<region>` /
   `<region-content>`, `<projects-to>`. Bikeshed; will not block the design.
3. **Multiple `<slot>`s with the same name at the same tree-distance.** If a
   contribution has two equally-near matching slots (e.g. tied LCA distance
   via different sibling branches), do contributions fan out to all, or is
   this a configuration error? Proposal: error (forces unique resolution).
   Alternative: fan out (cheaper, but harder to reason about). The vast
   majority of real configurations will never trigger this.
4. **Orphan policy.** When a `<slot-content>` mounts but no matching `<slot>`
   exists anywhere in the tree, is that an error or silently dropped?
   Proposal: configurable per-contribution via `required="true|false"`,
   defaulting to `true` (error) because typos and accidentally orphaned
   contributions are bugs we want surfaced.
5. **Empty-state contract.** Should `<slot>` always allocate space (and the
   containing element styles it as collapsed when empty), or should it
   actually not render anything when empty? Proposal: don't render when
   empty; the containing element can wrap it in its own container if it
   wants reserved space.
6. **Leak resistance.** If a view unmounts without `SlotHandle.close()` being
   reached (exception in teardown), the contribution leaks. Mitigation
   deferred — possibly weak references, possibly periodic sweep.
7. **Tree-traversal cost.** The naive routing algorithm is O(tree-size) per
   contribution. For typical app shells this is trivial; for very large
   dynamic trees it might warrant indexing slots by name globally and
   computing LCA-distance lazily. Implementation concern, not design.

## 8. Migration of existing code

Purely additive. No existing view XML breaks.

- The slot routing infrastructure is added; no element opts in.
- `AppBarElement` is extended to accept nested arbitrary UIElement children
  (in addition to its current `<commands>` block). The existing `<commands>`
  / `CommandScope` path remains untouched; the new `<slot>`-child path is
  opt-in.
- The tile-stack demo migrates: breadcrumb moved into a `<slot-content>` in
  `tiles-demo.view.xml`; `<slot name="secondary"/>` added to the demo
  `app-bar`.
- Optional follow-up ticket (not this one): port `<open-designer>` and all
  `placement="TOOLBAR"` commands to `<slot-content slot="toolbar">`-wrapped
  buttons. Then deprecate `CommandScope`, `ViewCommandModel`, the
  `<commands>` block on `AppBarElement`, and the `CommandPlacement` enum.

## 9. Implementation sketch (for sizing only)

| Component | Estimated effort |
|-----------|------------------|
| Tree-aware slot registry: maintain `(name, position)` index, compute nearest-match | small to medium (the only genuinely new code) |
| Parent-pointer / position tracking on rendered UIElements (if not already present) | small if existing infrastructure can be reused, else medium |
| `SlotHandle` and registration lifecycle hooks | small |
| `<slot>` element + control that renders contributions in registration order | small |
| `<slot-content>` element + register/unregister lifecycle | small |
| `AppBarElement` accepts arbitrary UIElement children (prerequisite for inline `<slot>`) | small |
| `ReactAppBarControl` renders the slot region | small to medium |
| Tile-stack demo migration | trivial |
| Multi-tab demo views | small |
| **(Follow-up, separate ticket)** CommandScope deprecation | medium (touches every existing toolbar consumer) |

This ticket: probably one to two days of focused work. The novel piece is the
tree-aware nearest-match routing; everything else is plumbing.
