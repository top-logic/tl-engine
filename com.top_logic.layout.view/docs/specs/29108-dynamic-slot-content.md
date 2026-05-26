# Dynamic Slot Content (Portal Slots)

**Status:** Draft — design only, no implementation
**Ticket:** #29108
**Module:** `com.top_logic.layout.view`

## 1. Problem

The tile-stack demo currently renders its breadcrumb as a "secondary header" inside the
tab body. In real applications a breadcrumb belongs in the app bar, not under it. The
naive fix — moving the breadcrumb into `<app-bar>` — fails for two reasons:

1. **Scope.** The breadcrumb binds to a `navPath` channel declared on the tile-stack
   view. Channels are visible only within their declaring view's subtree; the app bar
   is an ancestor and cannot see them. Forcing every breadcrumb-using feature to
   declare its channel at app-shell scope destroys module independence — every nav
   feature would have to register its channels globally.
2. **Tab independence.** Different tabs may host different tile-stacks with their own
   independent navigation paths. The app-bar breadcrumb has to *follow the active tab*.
   No data-flow channel models this cleanly: the value the app bar should display is
   not a path, it is a UI fragment whose identity depends on which subtree is mounted.

The right primitive is not a wider channel. It is a **named portal slot**: an ancestor
declares a region; a descendant declares content for that region; the framework wires
the two together at render time, scoped to lifecycle (mount/unmount of the descendant).

## 2. Prior art in TopLogic

The framework already implements this pattern — for commands.

`AppShellElement` (`com.top_logic.layout.view.element.AppShellElement`) seeds a shared
`CommandScope` into the `ViewContext` (lines 122–126, 154):

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

Descendant elements (form edit commands, dashboard layout edit) register their
`ViewCommandModel`s into this scope on attach. `AppBarElement` reads from the same
scope (lines 139–159), renders the commands with `TOOLBAR` placement as trailing
action buttons, observes changes via `scope.addListener(...)`, and removes contributed
commands on cleanup.

This is exactly the contribution model needed for breadcrumbs — except the contributed
artifact is a UI fragment (a `ReactControl`), not a `CommandModel`, and the routing is
by named slot, not by `CommandPlacement` enum.

## 3. Proposal: `ContentScope` and named slots

### 3.1 Java surface

A new shared scope, analogous to `CommandScope`:

```java
public final class ContentScope {

    /** Add a contribution under a named slot. */
    public ContentHandle contribute(String slotName, UIElement content);

    /** All current contributions for a slot, in registration order. */
    public List<UIElement> get(String slotName);

    /** Observe add/remove for any slot. */
    public void addListener(ContentScopeListener listener);
}

public interface ContentHandle extends AutoCloseable {
    @Override void close();   // removes the contribution
}
```

`ViewContext` gains `getContentScope()` / `withContentScope(...)`, matching the
`CommandScope` pair. `AppShellElement` seeds a single `ContentScope` for the whole
shell, just as it does for `CommandScope`.

### 3.2 XML surface

Two new elements:

**`<slot name="...">`** — declares a named slot on an ancestor. Used inside containers
that can host dynamic content (initially: `<app-bar>`):

```xml
<app-bar variant="flat">
    <commands>...</commands>
    <title>Task Manager</title>
    <slot name="secondary"/>
</app-bar>
```

**`<slot-content slot="...">`** — descendant contribution. Its children are registered
into the named slot when the surrounding view is mounted, and unregistered when it
unmounts:

```xml
<view>
    <channels><channel name="navPath"/></channels>

    <slot-content slot="secondary">
        <tile-breadcrumb path="navPath">
            <home-label><en>Accounts</en></home-label>
        </tile-breadcrumb>
    </slot-content>

    <tile-stack path="navPath" initial="..."/>
</view>
```

The breadcrumb's channel reference (`path="navPath"`) is still resolved in the view
that declared `<slot-content>`, not in the app bar. The app bar only adopts the
already-instantiated control. The channel never has to leave its home scope.

### 3.3 Lifecycle

| Event | Effect |
|-------|--------|
| `<slot-content>` element creates its control | Contribution registered into ancestor `ContentScope`, scope listeners notified, app bar re-renders the slot. |
| Containing view detaches (tab change, route change, conditional render) | Contribution removed via the `ContentHandle.close()` returned at registration, app bar re-renders the slot. |
| Multiple `<slot-content>` elements target the same slot | All contributions are rendered, in registration order. Same as `AppShellElement`'s `createSlotControl` wrapping multiple elements in a `ReactStackControl`. |
| App bar declares `<slot name="X"/>` but no contributions exist | Slot renders as empty. App bar layout collapses the empty region (CSS `:empty` or explicit visibility logic). |

The mount/unmount tie-in falls out naturally because nav-item routing already
controls which view subtree is currently materialized — only the active tab's
`<slot-content>` is attached, so only its contribution is in the scope.

### 3.4 Conflict resolution

For the breadcrumb case there is exactly one active tile-stack tab at a time, so
conflicts do not arise. The general rule: **all contributions render in registration
order**, wrapped in a `ReactStackControl` as `AppShellElement` already does.
Consumers that want "single-winner" semantics can declare the slot with a maximum
count or a `replace` policy if a use case appears; we do not pre-build that.

### 3.5 Scope boundaries

The `ContentScope` is seeded at `AppShellElement` and inherited via `ViewContext`
into every descendant. Slot names are global within the app shell — `name="secondary"`
on an `<app-bar>` matches any descendant's `<slot-content slot="secondary">`,
regardless of which feature module declared either side.

This is the same global-name model `CommandScope` uses with `CommandPlacement`. If
later we need scoped/local slots (e.g. a slot owned by a sub-view, not the app
shell), we add a second seeding point — `ContentScope` is hierarchical the same way
`CommandScope` is.

## 4. Worked examples

### 4.1 Breadcrumb in app bar

Today (tile demo only):

```xml
<view>
    <channels><channel name="navPath"/></channels>
    <stack direction="column" gap="default">
        <tile-breadcrumb path="navPath">...</tile-breadcrumb>
        <tile-stack path="navPath" initial="..."/>
    </stack>
</view>
```

With dynamic slot content:

```xml
<view>
    <channels><channel name="navPath"/></channels>

    <slot-content slot="secondary">
        <tile-breadcrumb path="navPath">...</tile-breadcrumb>
    </slot-content>

    <tile-stack path="navPath" initial="..."/>
</view>
```

The app bar declares `<slot name="secondary"/>` once. Any tile-stack tab fills it
when active; the `navPath` channel stays local.

### 4.2 Multiple tile-stacks sharing one breadcrumb

Two stacks in one view, one breadcrumb, all bound to the same channel. No new
framework feature needed — channel scoping already handles this:

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

Drill into either stack: the channel updates, both stacks re-render, the breadcrumb
re-renders. This is a property of channels, not of slots. Worth including in the
demo because the question comes up naturally.

### 4.3 Independent tile-stack tabs

Tab A and Tab B each declare their own `navPath` channel locally. Each contributes
its own breadcrumb to `slot="secondary"` on mount. Only the active tab is mounted, so
only its breadcrumb is in the scope, so the app bar shows the right one. Switching
tabs implicitly switches the breadcrumb. No app-level channel multiplexer needed.

## 5. Non-goals

- **Cross-window / cross-app-shell portals.** The scope is bounded by `AppShellElement`.
- **Replacing the static `<header>/<content>/<footer>` slots on `AppShellElement`.**
  Those configure the *shell structure* (which elements exist), not dynamic content
  into one of them. They stay.
- **Reordering / priority across contributions.** Registration order is the contract.
  If precedence is ever needed, it goes on `<slot-content>` as an explicit `priority`
  attribute — not implicit and not part of this spec.
- **Content-aware empty-state rendering inside the slot.** The slot is "empty" iff
  no contributions are registered. The app bar's visual treatment of an empty slot
  (collapse vs reserve space) is a styling concern for `ReactAppBarControl`, not a
  framework concern.

## 6. Open questions

1. **Slot declaration site.** Initial proposal puts `<slot>` on `<app-bar>` only. Should
   it be more general — e.g. allowed on any `UIElement` that opts in by implementing
   a `SlotHost` interface? Generalizing now is cheap; deferring is also cheap.
2. **Slot identity vs. position.** If `<slot name="secondary"/>` appears in multiple
   places in the shell (unlikely, but possible if `<app-bar>` is duplicated), which
   one receives contributions? Proposal: error at startup. Slot names are unique
   within an `AppShellElement`.
3. **Stale references on hot-reload.** `ContentHandle` is `AutoCloseable`; if a view
   is detached without `close()` being called (e.g. an exception in tear-down), the
   contribution leaks. Mitigation: weak references, or a periodic GC sweep tied to
   shell render. Same risk exists today in `CommandScope`; out of scope.
4. **Naming.** "Slot" matches existing TopLogic terminology (`AppShellElement` already
   uses it for header/content/footer). "Slot-content" is verbose; alternatives:
   `<projects-to slot="...">`, `<header-content slot="...">`, `<contribute slot="...">`.
   Bikeshed.

## 7. Migration

This feature is purely additive. No existing view XML breaks. The tile-stack demo
opts into the new pattern by:

1. Adding `<slot name="secondary"/>` to the demo's `<app-bar>` in `app.view.xml`.
2. Wrapping the breadcrumb in `tiles-demo.view.xml` in `<slot-content slot="secondary">`
   and removing the surrounding `<stack direction="column">` (no longer needed since
   the breadcrumb and stack live in different regions).

Other consumers can adopt incrementally; nothing forces them.

## 8. Implementation sketch (for sizing only)

| Component | Estimated effort |
|-----------|------------------|
| `ContentScope` + `ContentScopeListener` + `ContentHandle` | small, mirror of `CommandScope` |
| `ViewContext.getContentScope()` / `withContentScope(...)` | small, mirror of command pair |
| `AppShellElement` seeds `ContentScope` | trivial (3 lines) |
| `<slot name="...">` element + reads from scope, observes changes | small |
| `<slot-content slot="...">` element + register/unregister lifecycle | small |
| `AppBarElement` accepts nested `<slot>` declarations | small |
| `ReactAppBarControl` renders slot region | small to medium (depending on layout) |
| Tile-stack demo migration | trivial |
| Multi-tab demo (linked stacks + independent tab) | small |

Total: probably one ticket's worth of work, well under the cost of trying to make
this work with channels alone.
