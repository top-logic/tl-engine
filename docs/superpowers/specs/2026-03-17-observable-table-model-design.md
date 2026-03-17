# Observable Table Model Design

## Problem

React-based UIs in the new view layer (UIElement/ReactControl) do not automatically
update when persistent objects change. A table displaying a list of objects should
update rows when an object changes, remove rows when objects are deleted, and insert
new rows when matching objects are created.

The old layout system (LayoutComponent/ModelEventAdapter) handled this through
component invalidation and full re-rendering. The new system needs a lightweight
observer pattern that produces incremental updates delivered via SSE.

## Goals

- Tables update incrementally when displayed objects change (update/delete/create).
- No modifications to the React module's control logic (`ReactTableControl` stays
  unchanged).
- Model dependencies live exclusively in `com.top_logic.layout.view` -- the React
  module remains model-free.
- Leverage existing infrastructure: `ModelScope`, `GlobalModelEventForwarder`,
  `TableModelEvent`, SSE.
- Channel-driven data flow continues to work alongside model observation.

## Non-Goals

- Tree and form controls (future work, same pattern).
- Extracting observed types automatically from TL-Script expressions.
- Push-based real-time updates beyond the SSE heartbeat interval (~30 s).

## Architecture Overview

```
com.top_logic.layout.view                    com.top_logic.layout.react
+-----------------------------------+        +----------------------------+
|                                   |        |                            |
|  TableElement                     |        |  ReactTableControl         |
|    creates ObservableTableModel   |        |    handleModelEvent()      |
|    wraps inner TableModel         |        |    UPDATE -> updateViewport|
|                                   |        |    INSERT/DELETE/INVALIDATE|
|  ObservableTableModel             |        |      -> buildFullState()   |
|    implements TableModel          |        |                            |
|    implements ModelListener       |        |  (NO changes needed)       |
|    implements ChannelListener     |        |                            |
|    delegates to inner TableModel  |        +----------------------------+
|    fires TableModelEvents         |
|                                   |
+-----------------------------------+

ModelScope (per session, shared)     ReactWindowRegistry (per session)
  single GlobalModelEventForwarder     holds shared ModelScope
  shared across all windows            holds per-window SSEUpdateQueue
  accessed via ReactContext            stored as HTTP session attribute
```

### Dependency Direction

- `view` depends on `react` (existing).
- `react` depends on `com.top_logic` (new: for `GlobalModelEventForwarder`,
  `ModelScope`).
- `react` has NO dependency on `view`.
- The interface between view and react is `TableModel` / `TableModelEvent`
  (existing, unchanged).

## Detailed Design

### 1. ModelScope Integration

#### ReactContext

Add `getModelScope()` to the `ReactContext` interface:

```java
public interface ReactContext {
    // existing methods ...
    ModelScope getModelScope();
}
```

`DefaultReactContext` delegates to `ReactWindowRegistry`:

```java
@Override
public ModelScope getModelScope() {
    return _windowRegistry.getOrCreateModelScope();
}
```

#### ReactWindowRegistry

Holds a single shared `GlobalModelEventForwarder` per session. The `UpdateChain`
is a linked-list cursor (`next()` consumes events), so only one forwarder may
exist per session -- otherwise multiple forwarders would race and miss events.

Each window's `ObservableTableModel` registers its own listeners on this shared
scope. Event synthesis dispatches to all registered listeners regardless of window.

```java
public class ReactWindowRegistry {
    private volatile ModelScope _modelScope;

    public synchronized ModelScope getOrCreateModelScope() {
        if (_modelScope == null) {
            _modelScope = GlobalModelEventForwarder.createForSession();
        }
        return _modelScope;
    }

    public void synthesizeModelEvents() {
        if (_modelScope instanceof GlobalModelEventForwarder forwarder) {
            forwarder.synthesizeModelEvents();
        }
    }
}
```

#### GlobalModelEventForwarder -- public factory method

The existing `configuredAssociationRelevance()` method is private. Add a public
static factory method:

```java
public static GlobalModelEventForwarder createForSession() {
    KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
    return new GlobalModelEventForwarder(
        kb, kb.getSessionUpdateChain(), configuredAssociationRelevance());
}
```

This requires changing `configuredAssociationRelevance()` from `private` to
`package-private` or `public`, or extracting the logic into the new factory method.

### 2. Event Synthesis Triggers

Model events must be synthesized (polled from the UpdateChain) at two points:

1. **After command execution** in `ReactServlet.handleCommand()`, before flushing
   the SSE queue:

   ```java
   ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
   registry.synthesizeModelEvents();
   queue.flush();
   ```

   This path works because `ReactServlet.handleCommand()` already installs the
   `TLSubSessionContext` on the current thread.

2. **During SSE heartbeat** (every ~30 s), so that changes from other users are
   delivered without user interaction.

   **Important**: `GlobalModelEventForwarder.synthesizeModelEvents()` requires a
   `TLSubSessionContext` on the current thread (it returns `false` if none is
   installed or if the subsession is locked). The heartbeat runs on a
   `SchedulerService` thread that has no subsession context.

   The heartbeat callback must install the subsession before calling
   `synthesizeModelEvents()`:

   ```java
   // In heartbeat timer callback for window "windowName"
   TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
   TLContextManager.inContext(subSession, () -> {
       registry.synthesizeModelEvents();
       queue.flush();
   });
   ```

   If the subsession is locked (another request is being processed), the
   `synthesizeModelEvents()` call returns `false` and the heartbeat becomes a
   no-op -- which is correct, because the concurrent request will synthesize
   events when it completes.

### 3. ObservableTableModel

New class in `com.top_logic.layout.view`. Wraps an inner `TableModel` and
transparently adds model observation.

#### Responsibilities

- Delegates all `TableModel` methods to the inner model.
- Listens on `ModelScope` for object-level changes (updates, deletes) and
  type-level changes (creates).
- Listens on `ViewChannel` for input changes (new filter, new parent object).
- Translates events into `TableModelEvent`s that `ReactTableControl` already
  understands.

#### Internal State

- `_inner`: The wrapped `TableModel`.
- `_observedKeys`: `Set<ObjectKey>` tracking currently observed row objects.
  Using `ObjectKey` rather than `TLObject` references because deleted objects
  may have invalidated wrappers. `ObjectKey` is a stable identifier.
- `_observedTypes`: `Set<TLStructuredType>` from configuration (for creates).
- `_modelScope`: The `ModelScope` from `ReactContext`.
- `_inputChannel`: The `ViewChannel` providing input data.
- `_modelFactory`: `Function<Object, TableModel>` for re-evaluation on
  channel change or create detection.
- `_listeners`: `List<TableModelListener>` for event dispatch.

#### Thread Safety

All mutation paths must hold the `ReactWindowRegistry.getRequestLock()`:

- **Command execution**: `ReactServlet` already acquires this lock.
- **Heartbeat**: Must acquire the lock before calling `synthesizeModelEvents()`.
  If the lock is held (concurrent request), the heartbeat skips this cycle.

This ensures that `notifyChange()`, `channelChanged()`, and any control
interactions are serialized. No additional synchronization is needed inside
`ObservableTableModel`.

#### Lifecycle

```
createControl()
  ObservableTableModel created (passive, no listeners)
  ReactTableControl created, receives ObservableTableModel as its TableModel

onBeforeWrite()                          <- control is rendered
  model.attach(modelScope, channel)
  -> object listeners registered for each row object
  -> type listeners registered for configured observed types
  -> channel listener registered

[control lives]                          <- SSE updates on changes

cleanupTree()                            <- tab closed / navigated away
  model.detach()
  -> all listeners removed
```

#### Two Trigger Paths

**Path 1 -- Channel change (new input):**

```
ViewChannel value changes
  -> channelChanged()
  -> create new inner TableModel from new input
  -> deregister object listeners from old rows
  -> register object listeners for new rows
  -> fire TableModelEvent(INVALIDATE)
  -> ReactTableControl.buildFullState()
```

**Path 2 -- ModelScope event (object changes within current data):**

```
ModelChangeEvent arrives (batched, one per KB commit)
  -> notifyChange(event)

  Updates:
    for each updated object in current rows:
      match by ObjectKey against _observedKeys
      find index in inner model
      fire TableModelEvent(UPDATE, index, index)
    -> ReactTableControl.updateViewport() refreshes visible cells

  Deletes:
    match deleted ObjectKeys against _observedKeys
    collect affected indices (reverse order for stable removal)
    remove from inner model and _observedKeys
    coalesce into single TableModelEvent(INVALIDATE) to avoid
      redundant buildFullState() calls
    -> ReactTableControl.buildFullState() rebuilds viewport once

  Creates (only when observedTypes configured):
    re-evaluate row function ONCE (not per object)
    diff against current rows
    update _inner, _observedKeys, and object listener registrations
    fire single TableModelEvent(INVALIDATE)
    -> ReactTableControl.buildFullState() rebuilds viewport once
```

#### ModelScope Registration

```
attach(ModelScope scope, ViewChannel channel):
  for each row object in inner model:
    _observedKeys.add(object.tHandle().getObjectKey())
    scope.addModelListener(object, this)        // updates + deletes
  for each type in observedTypes:
    scope.addModelListener(type, this)           // creates
  channel.addListener(this)                      // input changes

detach():
  remove all object listeners from scope
  remove all type listeners from scope
  remove channel listener
  clear _observedKeys
```

#### Performance Considerations

- **Object listener count**: For tables with thousands of rows, registering
  individual object listeners has overhead. If this becomes a bottleneck,
  a future optimization could register a single type-level listener and
  filter events by checking `_observedKeys` membership. This trades
  per-event filtering cost for lower registration overhead.

- **Create re-evaluation**: The row function is re-evaluated at most once per
  batched `ModelChangeEvent`. If the row function is expensive (complex
  TL-Script with database queries), the heartbeat path could cause overhead
  every 30 seconds for tables with `observed-types` on frequently-created
  types. Users should configure `observed-types` only when needed.

### 4. TableElement Changes

`TableElement.createControl()` wraps the inner `TableModel` in an
`ObservableTableModel`. The existing direct channel listener wiring in
`TableElement` must be replaced by `ObservableTableModel`'s channel listener
to avoid duplicate observation.

```java
public IReactControl createControl(ViewContext context) {
    ViewChannel inputChannel = context.resolveChannel(getConfig().getInput());

    // Build inner TableModel from current channel value
    TableModel innerModel = buildTableModel(inputChannel.get());

    // Wrap with observation
    ObservableTableModel observableModel = new ObservableTableModel(
        innerModel,
        this::buildTableModel,       // factory for re-evaluation
        getConfig().getObservedTypes() // optional, for create detection
    );

    ReactTableControl control = new ReactTableControl(context, observableModel, columns);

    // Lazy attach on render
    control.addBeforeWriteAction(() -> {
        observableModel.attach(context.getModelScope(), inputChannel);
    });

    // Cleanup on dispose
    control.addCleanupAction(observableModel::detach);

    return control;
}
```

### 5. Prerequisite: addBeforeWriteAction on ReactControl

`ReactControl` currently has `addCleanupAction(Runnable)` but no symmetric
`addBeforeWriteAction(Runnable)`. This method must be added:

```java
public class ReactControl {
    private List<Runnable> _beforeWriteActions;

    public void addBeforeWriteAction(Runnable action) {
        if (_beforeWriteActions == null) {
            _beforeWriteActions = new ArrayList<>();
        }
        _beforeWriteActions.add(action);
    }

    @Override
    protected void onBeforeWrite() {
        super.onBeforeWrite();
        if (_beforeWriteActions != null) {
            _beforeWriteActions.forEach(Runnable::run);
            _beforeWriteActions = null; // run once
        }
    }
}
```

This is a small, safe addition to the React module that follows the existing
`addCleanupAction` pattern.

### 6. ReactTableControl -- No Changes

The existing `handleModelEvent()` method already handles all event types:

- `UPDATE` -> `updateViewport()` -- refreshes only visible rows
- `INSERT` / `DELETE` / `INVALIDATE` -> `buildFullState()` -- rebuilds viewport

The virtualization logic ensures only the visible viewport (+ 20-row prefetch
buffer) is sent as SSE patches. Rows outside the viewport are loaded on scroll.

### 7. Client-Side -- No Changes

`TLTableView.tsx` continues to work unchanged. The SSE patches arrive in the same
format. React's VDOM diff with stable `key` props ensures minimal DOM updates.

## Data Flow Summary

```
KB Commit
  -> UpdateChain
  -> GlobalModelEventForwarder.synthesizeModelEvents()
       (triggered by ReactServlet after command, or SSE heartbeat)
  -> ModelChangeEvent dispatched to all registered listeners on shared scope
  -> ObservableTableModel.notifyChange()
       Updates: fire TableModelEvent(UPDATE) per row
       Deletes: coalesce into single TableModelEvent(INVALIDATE)
       Creates: re-evaluate, fire single TableModelEvent(INVALIDATE)
  -> ReactTableControl.handleModelEvent()
       UPDATE: updateViewport() -- refresh visible cells
       INVALIDATE: buildFullState() -- rebuild viewport
  -> putState() / patchReactState()
  -> SSE PatchEvent to browser
  -> React VDOM diff, minimal DOM updates
```

## What Does NOT Change

- `ReactTableControl` -- unchanged
- `TLTableView.tsx` -- unchanged
- `TableModel` / `TableModelEvent` interfaces -- unchanged
- SSE infrastructure -- unchanged (heartbeat gets additional trigger)
- `TLSubSessionContext` -- unchanged

## Required Changes Summary

| Component | Module | Change |
|---|---|---|
| `ReactContext` | react | Add `getModelScope()` |
| `DefaultReactContext` | react | Delegate to registry |
| `ReactWindowRegistry` | react | Hold shared `ModelScope`, add `synthesizeModelEvents()` |
| `ReactControl` | react | Add `addBeforeWriteAction(Runnable)` |
| `GlobalModelEventForwarder` | core | Add public `createForSession()` factory |
| `ReactServlet` | react | Call `synthesizeModelEvents()` after command |
| `SSEUpdateQueue` | react | Call `synthesizeModelEvents()` in heartbeat (with subsession) |
| `ObservableTableModel` | view | **New class** |
| `TableElement` | view | Wrap model, replace channel listener wiring |

## Extensibility

The pattern transfers directly to other control types:

- **Tree**: `ObservableTreeModel` wraps `TreeModel`, fires `TreeModelEvent`s
- **Form**: `ObservableFormModel` observes displayed object, updates field values

Each observable model follows the same structure: wrap inner model, listen on
ModelScope + Channel, fire existing model events.

## Configuration

`TableElement.Config` gets an optional `observed-types` property:

```xml
<table input="channel:items">
  <observed-types>
    <type name="my.module:MyType"/>
  </observed-types>
  <!-- columns... -->
</table>
```

When `observed-types` is empty (default), only updates and deletes of currently
displayed rows are observed. No create detection occurs. This is correct for
tables whose rows come directly from a channel (the channel itself handles new
data).

When `observed-types` is set, the `ObservableTableModel` additionally registers
type-level listeners and re-evaluates the row function when objects of those types
are created.
