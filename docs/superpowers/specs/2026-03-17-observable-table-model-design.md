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

ModelScope (per window)              ReactWindowRegistry (per session)
  held by ReactWindowRegistry         holds ModelScope + SSEUpdateQueue
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
    return _windowRegistry.getOrCreateModelScope(_windowName);
}
```

#### ReactWindowRegistry

Holds a per-window `GlobalModelEventForwarder` (which extends `DefaultModelScope`):

```java
public class ReactWindowRegistry {
    private final ConcurrentHashMap<String, ModelScope> _windowModelScopes =
        new ConcurrentHashMap<>();

    public ModelScope getOrCreateModelScope(String windowId) {
        return _windowModelScopes.computeIfAbsent(windowId, id -> {
            KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
            return new GlobalModelEventForwarder(
                kb, kb.getSessionUpdateChain(), configuredRelevance());
        });
    }
}
```

Cleanup on window close removes the scope from the map.

### 2. Event Synthesis Triggers

Model events must be synthesized (polled from the UpdateChain) at two points:

1. **After command execution** in `ReactServlet.handleCommand()`, before flushing
   the SSE queue:

   ```java
   ModelScope scope = registry.getOrCreateModelScope(windowName);
   if (scope instanceof GlobalModelEventForwarder forwarder) {
       forwarder.synthesizeModelEvents();
   }
   queue.flush();
   ```

2. **During SSE heartbeat** (every ~30 s), so that changes from other users are
   delivered without user interaction:

   ```java
   // In heartbeat timer callback
   if (scope instanceof GlobalModelEventForwarder forwarder) {
       forwarder.synthesizeModelEvents();
   }
   ```

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
      find index in inner model
      fire TableModelEvent(UPDATE, index, index)
    -> ReactTableControl.updateViewport() refreshes visible cells

  Deletes:
    collect indices of deleted objects (reverse order)
    for each: remove from inner model, fire TableModelEvent(DELETE, index, index)
    -> ReactTableControl.buildFullState() rebuilds viewport

  Creates (only when observedTypes configured):
    re-evaluate row function ONCE (not per object)
    diff against current rows
    insert/remove rows, fire corresponding TableModelEvents
    update object listener registrations
```

#### ModelScope Registration

```
attach(ModelScope scope, ViewChannel channel):
  for each row object in inner model:
    scope.addModelListener(object, this)        // updates + deletes
  for each type in observedTypes:
    scope.addModelListener(type, this)           // creates
  channel.addListener(this)                      // input changes

detach():
  remove all object listeners
  remove all type listeners
  remove channel listener
```

### 4. TableElement Changes

`TableElement.createControl()` wraps the inner `TableModel`:

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

### 5. ReactTableControl -- No Changes

The existing `handleModelEvent()` method already handles all event types:

- `UPDATE` -> `updateViewport()` -- refreshes only visible rows
- `INSERT` / `DELETE` / `INVALIDATE` -> `buildFullState()` -- rebuilds viewport

The virtualization logic ensures only the visible viewport (+ 20-row prefetch
buffer) is sent as SSE patches. Rows outside the viewport are loaded on scroll.

### 6. Client-Side -- No Changes

`TLTableView.tsx` continues to work unchanged. The SSE patches arrive in the same
format. React's VDOM diff with stable `key` props ensures minimal DOM updates.

## Data Flow Summary

```
KB Commit
  -> UpdateChain
  -> GlobalModelEventForwarder.synthesizeModelEvents()
       (triggered by ReactServlet after command, or SSE heartbeat)
  -> ModelChangeEvent dispatched to registered listeners
  -> ObservableTableModel.notifyChange()
       Updates: fire TableModelEvent(UPDATE)
       Deletes: remove row, fire TableModelEvent(DELETE)
       Creates: re-evaluate rows, fire TableModelEvent(INSERT/DELETE)
  -> ReactTableControl.handleModelEvent()
       UPDATE: updateViewport() -- refresh visible cells
       INSERT/DELETE: buildFullState() -- rebuild viewport
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
- `GlobalModelEventForwarder` -- reused as-is

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
