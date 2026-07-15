# Design: Move Personal Configuration from Controls to UIElement Wiring Layer

**Date:** 2026-03-05
**Ticket:** #29108
**Status:** Approved

## Problem

`ReactSidebarControl` directly accesses `PersonalConfiguration` to persist collapse
state and group expansion states. This couples the control layer to a persistence
mechanism that belongs in the UIElement wiring layer. Controls should be stateless
renderers that fire callbacks on user interaction; the UIElement layer decides what
to do with those state changes (persist, propagate, ignore).

`ReactSplitPanelControl` has a similar gap: it tracks pane sizes and collapse states
in memory but never persists them, so they're lost on page reload.

## Scope

- **ReactSidebarControl**: Remove all `PersonalConfiguration` access, add callbacks
- **ReactSplitPanelControl**: Add nullable persistence callbacks for sizes and collapse
- **SidebarElement**: Wire persistence via callbacks
- **SplitPanelElement**: Wire persistence via callbacks
- **ViewContext**: Add personalization key infrastructure
- **UIElement.Config**: Add optional personalization-key override
- **Demo callers**: Update to pass callbacks

## Approach: Direct Callbacks

Controls receive lambda callbacks via constructor parameters. The UIElement wiring
layer passes lambdas that handle `PersonalConfiguration` operations. This follows
the existing pattern in `ReactBottomBarControl` which takes a `Consumer<String>`.

## Design

### 1. ViewContext Personalization Key

`ViewContext` tracks a hierarchical path for automatic personalization key derivation.

```java
public class ViewContext {
    private final ViewDisplayContext _displayContext;
    private final String _personalizationPath;

    public ViewContext(ViewDisplayContext displayContext) {
        this(displayContext, "view");
    }

    private ViewContext(ViewDisplayContext displayContext, String path) {
        _displayContext = displayContext;
        _personalizationPath = path;
    }

    public ViewContext childContext(String segment) {
        return new ViewContext(_displayContext, _personalizationPath + "." + segment);
    }

    public String getPersonalizationKey() {
        return _personalizationPath;
    }

    public ViewDisplayContext getDisplayContext() {
        return _displayContext;
    }
}
```

### 2. UIElement.Config Override

```java
interface Config extends PolymorphicConfiguration<UIElement> {
    @Name("personalization-key")
    String getPersonalizationKey();
}
```

Each stateful UIElement resolves its key:
```java
String key = config.getPersonalizationKey() != null
    ? config.getPersonalizationKey()
    : context.getPersonalizationKey();
```

### 3. ReactSidebarControl Refactoring

**Remove:**
- `PersonalConfiguration` import
- `PersonalizingExpandable` import
- `_personalizationKey` field
- `loadGroupStates()` method
- `saveGroupState()` method
- All `PersonalConfiguration` calls from constructor and commands

**New constructor signature:**
```java
public ReactSidebarControl(
    List<SidebarItem> items,
    String initialActiveItemId,
    boolean initialCollapsed,
    Map<String, Boolean> initialGroupStates,
    Consumer<Boolean> onCollapseChanged,
    BiConsumer<String, Boolean> onGroupToggled,
    ReactControl headerContent,
    ReactControl headerCollapsedContent,
    ReactControl footerContent,
    ReactControl footerCollapsedContent)
```

**ToggleCollapseCommand:** Calls `_onCollapseChanged.accept(newState)` instead of
`PersonalizingExpandable.saveCollapsed()`.

**ToggleGroupCommand:** Calls `_onGroupToggled.accept(groupId, expanded)` instead of
`saveGroupState()`.

### 4. ReactSplitPanelControl Additions

**New constructor:**
```java
public ReactSplitPanelControl(
    Orientation orientation,
    boolean resizable,
    Consumer<Map<String, Float>> onSizesChanged,
    BiConsumer<Integer, Boolean> onChildCollapsed)
```

Keep existing 2-param convenience constructor (passes `null` for both callbacks).

**UpdateSizesCommand:** After updating internal sizes, invokes `_onSizesChanged`
with a map of controlId to new pixel size.

**childCollapsed():** After updating collapse state, invokes `_onChildCollapsed`
with (childIndex, collapsed).

### 5. SidebarElement Wiring

```java
@Override
public ViewControl createControl(ViewContext context) {
    String key = resolveKey(context, "sidebar");

    boolean collapsed = PersonalizingExpandable.loadCollapsed(
        key + ".collapsed", _collapsed);
    Map<String, Boolean> groupStates = loadGroupStates(key);

    List<SidebarItem> sidebarItems = buildItems(context);

    return new ReactSidebarControl(
        sidebarItems, activeItem,
        collapsed, groupStates,
        c -> PersonalizingExpandable.saveCollapsed(key + ".collapsed", c, _collapsed),
        (gid, exp) -> saveGroupState(key, gid, exp),
        null, null, null, null);
}
```

`loadGroupStates()` and `saveGroupState()` move from `ReactSidebarControl` to
`SidebarElement` as private helper methods.

### 6. SplitPanelElement Wiring

```java
@Override
public ViewControl createControl(ViewContext context) {
    String key = resolveKey(context, "split-panel");

    Map<Integer, Float> persistedSizes = loadPaneSizes(key);
    Map<Integer, Boolean> persistedCollapse = loadCollapseStates(key);

    ReactSplitPanelControl splitPanel = new ReactSplitPanelControl(
        orientation, _resizable,
        sizes -> savePaneSizes(key, sizes),
        (idx, collapsed) -> saveCollapseState(key, idx, collapsed));

    for (int i = 0; i < _panes.size(); i++) {
        PaneEntry pane = _panes.get(i);
        float size = persistedSizes.containsKey(i) ? persistedSizes.get(i) : pane._size;
        DisplayUnit unit = persistedSizes.containsKey(i) ? DisplayUnit.PIXEL : parseUnit(pane._unit);
        boolean collapsed = persistedCollapse.getOrDefault(i, false);
        // create child and add with constraint, applying collapsed state
    }
    return splitPanel;
}
```

### 7. Demo Callers

- **DemoReactSidebarComponent**: Pass `false` for `initialCollapsed`, empty map for
  group states, and persistence callbacks that call `PersonalizingExpandable` with
  key `"demo.sidebar"`.
- **DemoReactAppComponent**: Same pattern with key `"demo.app"`.
- **DemoReactLayoutComponent**: Unchanged (uses 2-param convenience constructor).

### 8. Error Handling

- `PersonalConfiguration.getPersonalConfiguration()` can return `null` (no session).
  All persistence lambdas guard against this.
- Corrupt persisted JSON data (wrong types) is silently ignored; falls back to
  config defaults.

### 9. Testing

- Existing unit tests for controls remain valid (they don't test PC interaction).
- New tests can verify callbacks are invoked by passing mock consumers.

## Files Changed

| Module | File | Change |
|--------|------|--------|
| com.top_logic.layout.view | ViewContext.java | Add personalization path |
| com.top_logic.layout.view | UIElement.java | Add personalization-key to Config |
| com.top_logic.layout.view | SidebarElement.java | Add PC loading/saving, pass callbacks |
| com.top_logic.layout.view | SplitPanelElement.java | Add PC loading/saving, pass callbacks |
| com.top_logic.layout.react | ReactSidebarControl.java | Remove PC, add callbacks |
| com.top_logic.layout.react | ReactSplitPanelControl.java | Add nullable callbacks |
| com.top_logic.demo | DemoReactSidebarComponent.java | Update constructor call |
| com.top_logic.demo | DemoReactAppComponent.java | Update constructor call |
