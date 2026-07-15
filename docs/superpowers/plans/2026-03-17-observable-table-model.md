# Observable Table Model Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Forward persistent object changes to React table controls so that rows update, appear, or disappear automatically.

**Architecture:** A per-window `GlobalModelEventForwarder` (held by `ReactWindowRegistry`) synthesizes KB events into `ModelChangeEvent`s. `ObservableTableModel` (new, in view module) wraps the inner `ObjectTableModel`, listens on `ModelScope` + `ViewChannel`, and fires `TableModelEvent`s. The existing `ReactTableControl` handles these events unchanged.

**Tech Stack:** Java 17, TopLogic KB/Model layer, React SSE controls, Maven multi-module build.

**Spec:** `docs/superpowers/specs/2026-03-17-observable-table-model-design.md`

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `com.top_logic/src/main/java/com/top_logic/mig/html/layout/GlobalModelEventForwarder.java` | Add public `createForWindow()` factory |
| Modify | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactContext.java` | Add `getModelScope()` method |
| Modify | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultReactContext.java` | Implement `getModelScope()` |
| Modify | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DisplayContextAdapter.java` | Implement `getModelScope()` |
| Modify | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java` | Add per-window ModelScope management |
| Modify | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java` | Add `addBeforeWriteAction(Runnable)` |
| Modify | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java` | Call `synthesizeModelEvents()` after command |
| Modify | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/SSEUpdateQueue.java` | Synthesize events in heartbeat |
| Create | `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/model/ObservableTableModel.java` | New: wraps TableModel, observes ModelScope + Channel |
| Modify | `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TableElement.java` | Wrap model in ObservableTableModel |

---

## Chunk 1: Infrastructure (React + Core modules)

### Task 1: Add `createForWindow()` factory to GlobalModelEventForwarder

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/mig/html/layout/GlobalModelEventForwarder.java:126-134`

- [ ] **Step 1: Add public static factory method**

The existing `configuredAssociationRelevance()` is private (line 126). Add a
public factory that calls it:

```java
/**
 * Creates a new {@link GlobalModelEventForwarder} for a single browser window.
 *
 * <p>
 * Each window gets its own {@link UpdateChain} cursor via
 * {@link KnowledgeBase#getSessionUpdateChain()}, which returns an independent cursor
 * per call. Multiple forwarders can coexist per session without racing.
 * </p>
 */
public static GlobalModelEventForwarder createForWindow() {
    KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
    return new GlobalModelEventForwarder(
        kb, kb.getSessionUpdateChain(), configuredAssociationRelevance());
}
```

Insert this after the existing `configuredAssociationRelevance()` method (after line 134).

- [ ] **Step 2: Build core module**

Run: `mvn install -DskipTests=true -pl com.top_logic`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
Ticket #29108: Add public createForWindow() factory to GlobalModelEventForwarder.
```

---

### Task 2: Add `getModelScope()` to ReactContext and DefaultReactContext

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactContext.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultReactContext.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DisplayContextAdapter.java`

- [ ] **Step 1: Add method to ReactContext interface**

Add import and method at the end of the interface (before the closing `}`),
after the `fromDisplayContext` method:

```java
import com.top_logic.model.listen.ModelScope;
```

```java
/**
 * The {@link ModelScope} for observing persistent object changes in this window.
 *
 * <p>
 * Delegates to the per-window scope held by
 * {@link ReactWindowRegistry#getOrCreateModelScope(String)}.
 * </p>
 */
ModelScope getModelScope();
```

- [ ] **Step 2: Implement in DefaultReactContext**

Add at the end of the class (before closing `}`):

```java
@Override
public ModelScope getModelScope() {
    return _windowRegistry.getOrCreateModelScope(_windowName);
}
```

Add import: `import com.top_logic.model.listen.ModelScope;`

- [ ] **Step 3: Implement in DisplayContextAdapter**

Find `DisplayContextAdapter` (same package as ReactContext). Add a
`getModelScope()` implementation. The old-world display context has a
`ModelScope` accessible via `FrameScope`:

```java
@Override
public ModelScope getModelScope() {
    return getWindowRegistry().getOrCreateModelScope(getWindowName());
}
```

Add import: `import com.top_logic.model.listen.ModelScope;`

- [ ] **Step 4: Verify compilation**

These changes won't compile yet because `ReactWindowRegistry.getOrCreateModelScope()`
does not exist. That comes in Task 3. Move on.

---

### Task 3: Add per-window ModelScope to ReactWindowRegistry

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java`

- [ ] **Step 1: Add ModelScope field and methods**

Add import:

```java
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.listen.ModelScope;
```

Add field after the existing `_windowQueues` field (line 40):

```java
private final ConcurrentHashMap<String, GlobalModelEventForwarder> _windowModelScopes =
    new ConcurrentHashMap<>();
```

Add methods after `getQueue()` (after line 80):

```java
/**
 * Gets or creates the {@link ModelScope} for the given window.
 *
 * <p>
 * Each window gets its own {@link GlobalModelEventForwarder} with an independent
 * {@link com.top_logic.knowledge.service.UpdateChain} cursor. Events synthesized on one
 * scope are dispatched only to listeners registered on that scope.
 * </p>
 */
public ModelScope getOrCreateModelScope(String windowId) {
    return _windowModelScopes.computeIfAbsent(windowId,
        id -> GlobalModelEventForwarder.createForWindow());
}

/**
 * Synthesizes pending model events for the given window.
 *
 * <p>
 * Polls the window's {@link com.top_logic.knowledge.service.UpdateChain} and dispatches
 * {@link com.top_logic.model.listen.ModelChangeEvent}s to all registered listeners.
 * </p>
 *
 * @param windowId
 *        The window to synthesize events for.
 */
public void synthesizeModelEvents(String windowId) {
    GlobalModelEventForwarder forwarder = _windowModelScopes.get(windowId);
    if (forwarder != null) {
        forwarder.synthesizeModelEvents();
    }
}
```

- [ ] **Step 2: Clean up ModelScope on window close**

In the `windowClosed(String windowId)` method (line 190), add cleanup of the
model scope. Find the line that removes the queue and add model scope removal
next to it:

```java
_windowModelScopes.remove(windowId);
```

- [ ] **Step 3: Add ModelScope dependency to pom.xml**

Check if `com.top_logic.layout.react` already depends on `com.top_logic` (tl-core).
If not, add the dependency.

Run: `grep -q 'tl-core' com.top_logic.layout.react/pom.xml && echo "already depends" || echo "need to add"`

- [ ] **Step 4: Build react module**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```
Ticket #29108: Add per-window ModelScope to ReactContext and ReactWindowRegistry.
```

---

### Task 4: Add `addBeforeWriteAction()` to ReactControl

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java`

- [ ] **Step 1: Add field and method**

Add a field near the existing `_cleanupActions` field:

```java
private List<Runnable> _beforeWriteActions;
```

Add method after `addCleanupAction()` (after line 243):

```java
/**
 * Registers an action to run once before this control is first rendered.
 *
 * <p>
 * Use this to defer resource-intensive setup (e.g. registering model listeners) until the
 * control is actually displayed. Actions run during {@link #onBeforeWrite()} and are
 * discarded afterwards.
 * </p>
 *
 * @param action
 *        The action to run before first render.
 */
public void addBeforeWriteAction(Runnable action) {
    if (_beforeWriteActions == null) {
        _beforeWriteActions = new ArrayList<>();
    }
    _beforeWriteActions.add(action);
}
```

- [ ] **Step 2: Call actions in onBeforeWrite()**

Replace the `onBeforeWrite()` method (lines 198-200):

```java
protected void onBeforeWrite() {
    super.onBeforeWrite();
    if (_beforeWriteActions != null) {
        _beforeWriteActions.forEach(Runnable::run);
        _beforeWriteActions = null;
    }
}
```

- [ ] **Step 3: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Add addBeforeWriteAction() to ReactControl for lazy initialization.
```

---

### Task 5: Trigger event synthesis in ReactServlet after commands

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java:301-324`

- [ ] **Step 1: Add synthesizeModelEvents call**

In `handleCommand()`, after the command execution and `forwardPendingUpdates()`
call (line 315), but still inside the `requestLock` block (before `if (result.isSuccess())`
on line 317), add:

```java
// Synthesize model events so that observable models receive changes
// from this command before the SSE queue is flushed.
ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);
```

- [ ] **Step 2: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
Ticket #29108: Synthesize model events after command execution in ReactServlet.
```

---

### Task 6: Trigger event synthesis in SSE heartbeat

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/SSEUpdateQueue.java`

- [ ] **Step 1: Add window name and session context fields**

The heartbeat needs to know which window it serves. Add fields:

```java
private volatile String _windowName;
private volatile TLSessionContext _sessionContext;
private volatile ReactWindowRegistry _windowRegistry;
```

Add imports:

```java
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.window.ReactWindowRegistry;
```

- [ ] **Step 2: Set context in setConnection()**

In `setConnection(AsyncContext asyncContext)`, add parameters or a separate
setter. The simplest approach: add a method that is called right after
`setConnection()` in `SSEServlet`:

```java
/**
 * Sets the window context for model event synthesis during heartbeat.
 *
 * @param windowName
 *        The window name this queue serves.
 * @param sessionContext
 *        The session context for subsession lookup.
 * @param registry
 *        The window registry for event synthesis.
 */
public void setWindowContext(String windowName, TLSessionContext sessionContext,
        ReactWindowRegistry registry) {
    _windowName = windowName;
    _sessionContext = sessionContext;
    _windowRegistry = registry;
}
```

- [ ] **Step 3: Extend sendHeartbeat() to synthesize events**

Replace `sendHeartbeat()` (line 285-291):

```java
private void sendHeartbeat() {
    SSEConnection conn = _connection;
    if (conn != null) {
        synthesizeModelEventsIfPossible();
        writeOrDisconnect(conn, HEARTBEAT_MESSAGE);
        flush();
    }
    cancelHeartbeatIfEmpty();
}

private void synthesizeModelEventsIfPossible() {
    ReactWindowRegistry registry = _windowRegistry;
    TLSessionContext sessionCtx = _sessionContext;
    String windowName = _windowName;
    if (registry == null || sessionCtx == null || windowName == null) {
        return;
    }
    TLSubSessionContext subSession = sessionCtx.getSubSession(windowName);
    if (subSession == null) {
        return;
    }
    ThreadContextManager.inContext(subSession, () -> {
        registry.synthesizeModelEvents(windowName);
    });
}
```

- [ ] **Step 4: Wire setWindowContext() call in SSEServlet**

Find `SSEServlet` (in the same package). In the method that handles new SSE
connections, after calling `queue.setConnection(asyncContext)`, add:

```java
queue.setWindowContext(windowName, TLContextManager.getSession(), registry);
```

- [ ] **Step 5: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```
Ticket #29108: Synthesize model events during SSE heartbeat for near-realtime updates.
```

---

## Chunk 2: ObservableTableModel + TableElement Integration (View module)

### Task 7: Create ObservableTableModel

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/model/ObservableTableModel.java`

- [ ] **Step 1: Create package directory**

Run: `mkdir -p com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/model`

- [ ] **Step 2: Write ObservableTableModel**

Create the file with the full implementation. This is the core of the design.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Companion to an {@link ObjectTableModel} that observes persistent object changes via
 * {@link ModelScope} and input changes via {@link ViewChannel}, translating them into
 * {@link TableModelEvent}s on the inner model.
 *
 * <p>
 * This class lives in the view module and bridges the gap between the TopLogic model layer
 * and the model-free React control layer. The wrapped {@link ObjectTableModel} and the
 * {@link com.top_logic.layout.react.control.table.ReactTableControl} remain unchanged.
 * </p>
 *
 * <h3>Two trigger paths:</h3>
 * <ol>
 * <li><b>Channel change</b> (new input data): Re-evaluates the row function with the new
 * input, calls {@link ObjectTableModel#setRowObjects(List)} on the inner model (which fires
 * {@link TableModelEvent#INVALIDATE}), and re-registers object listeners.</li>
 * <li><b>ModelScope event</b> (object changes): Removes deleted rows via
 * {@link ObjectTableModel#removeRowObject(Object)}, and re-evaluates the row function for
 * updates and creates (firing {@link TableModelEvent#INVALIDATE} via
 * {@link ObjectTableModel#setRowObjects(List)}).</li>
 * </ol>
 *
 * <h3>Lifecycle:</h3>
 * <ul>
 * <li>{@link #attach(ModelScope)} - registers listeners (called on first render)</li>
 * <li>{@link #detach()} - removes all listeners (called on cleanup)</li>
 * </ul>
 */
public class ObservableTableModel implements ModelListener, ViewChannel.ChannelListener {

	private final ObjectTableModel _inner;

	private final Function<Object[], Collection<?>> _rowFunction;

	private final Set<TLStructuredType> _observedTypes;

	private final List<ViewChannel> _inputChannels;

	private Set<ObjectKey> _observedKeys = new HashSet<>();

	private ModelScope _modelScope;

	private boolean _attached;

	/**
	 * Creates a new {@link ObservableTableModel}.
	 *
	 * @param inner
	 *        The wrapped table model.
	 * @param rowFunction
	 *        Function that takes channel values and returns row objects. Used for
	 *        re-evaluation on channel change and create detection.
	 * @param observedTypes
	 *        Types to observe for create events. Empty means no create detection.
	 * @param inputChannels
	 *        The input channels whose values are passed to the row function.
	 */
	public ObservableTableModel(ObjectTableModel inner,
			Function<Object[], Collection<?>> rowFunction,
			Set<TLStructuredType> observedTypes,
			List<ViewChannel> inputChannels) {
		_inner = inner;
		_rowFunction = rowFunction;
		_observedTypes = observedTypes;
		_inputChannels = inputChannels;
	}

	/**
	 * The wrapped {@link ObjectTableModel}.
	 *
	 * <p>
	 * The {@link com.top_logic.layout.react.control.table.ReactTableControl} uses this as
	 * its table model and registers its {@link com.top_logic.layout.table.model.TableModelListener}
	 * on it directly.
	 * </p>
	 */
	public ObjectTableModel getTableModel() {
		return _inner;
	}

	/**
	 * Registers listeners on the given {@link ModelScope} and input channels.
	 *
	 * <p>
	 * Called lazily on first render (via {@code addBeforeWriteAction}).
	 * </p>
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		_modelScope = scope;

		registerObjectListeners();
		registerTypeListeners();
		registerChannelListeners();
	}

	/**
	 * Removes all listeners and releases references.
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;

		deregisterObjectListeners();
		deregisterTypeListeners();
		deregisterChannelListeners();

		_modelScope = null;
		_observedKeys.clear();
	}

	// --- ModelListener ---

	@Override
	public void notifyChange(ModelChangeEvent event) {
		boolean hasDeletes = handleDeletes(event);
		boolean hasUpdates = hasUpdatesForCurrentRows(event);
		boolean hasCreates = hasRelevantCreates(event);

		if (hasUpdates || hasCreates) {
			// Re-evaluate row function and update inner model. This fires INVALIDATE
			// via setRowObjects(), causing ReactTableControl.buildFullState().
			reEvaluateRows();
		}
	}

	private boolean handleDeletes(ModelChangeEvent event) {
		List<TLObject> toRemove = new ArrayList<>();
		event.getDeleted().forEach(obj -> {
			ObjectKey key = key(obj);
			if (key != null && _observedKeys.contains(key)) {
				toRemove.add(obj);
				_observedKeys.remove(key);
				_modelScope.removeModelListener(obj, this);
			}
		});
		for (TLObject obj : toRemove) {
			_inner.removeRowObject(obj);
		}
		return !toRemove.isEmpty();
	}

	private boolean hasUpdatesForCurrentRows(ModelChangeEvent event) {
		return event.getUpdated().anyMatch(obj -> {
			ObjectKey key = key(obj);
			return key != null && _observedKeys.contains(key);
		});
	}

	private boolean hasRelevantCreates(ModelChangeEvent event) {
		if (_observedTypes.isEmpty()) {
			return false;
		}
		for (TLStructuredType type : _observedTypes) {
			if (event.getCreated(type).findAny().isPresent()) {
				return true;
			}
		}
		return false;
	}

	// --- ChannelListener ---

	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		reEvaluateRows();
	}

	// --- Re-evaluation ---

	private void reEvaluateRows() {
		deregisterObjectListeners();
		Object[] channelValues = readChannelValues();
		Collection<?> newRows = _rowFunction.apply(channelValues);
		_inner.setRowObjects(new ArrayList<>(newRows));
		registerObjectListeners();
	}

	// --- Listener registration helpers ---

	@SuppressWarnings("unchecked")
	private void registerObjectListeners() {
		_observedKeys.clear();
		for (Object row : (Collection<Object>) _inner.getAllRows()) {
			if (row instanceof TLObject tlObj) {
				ObjectKey key = key(tlObj);
				if (key != null) {
					_observedKeys.add(key);
					_modelScope.addModelListener(tlObj, this);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void deregisterObjectListeners() {
		for (Object row : (Collection<Object>) _inner.getAllRows()) {
			if (row instanceof TLObject tlObj) {
				_modelScope.removeModelListener(tlObj, this);
			}
		}
		_observedKeys.clear();
	}

	private void registerTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_modelScope.addModelListener(type, this);
		}
	}

	private void deregisterTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_modelScope.removeModelListener(type, this);
		}
	}

	private void registerChannelListeners() {
		for (ViewChannel channel : _inputChannels) {
			channel.addListener(this);
		}
	}

	private void deregisterChannelListeners() {
		for (ViewChannel channel : _inputChannels) {
			channel.removeListener(this);
		}
	}

	// --- Utility ---

	private Object[] readChannelValues() {
		Object[] values = new Object[_inputChannels.size()];
		for (int i = 0; i < _inputChannels.size(); i++) {
			values[i] = _inputChannels.get(i).get();
		}
		return values;
	}

	private static ObjectKey key(TLObject obj) {
		KnowledgeItem item = obj.tHandle();
		return item != null ? item.tId() : null;
	}
}
```

- [ ] **Step 3: Build view module**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Add ObservableTableModel for forwarding model changes to React tables.
```

---

### Task 8: Integrate ObservableTableModel into TableElement

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TableElement.java:178-247`

- [ ] **Step 1: Add observed-types configuration**

Add a new configuration property in `Config` interface (after `getSelection()`,
around line 151):

```java
/** Configuration name for {@link #getObservedTypes()}. */
String OBSERVED_TYPES = "observed-types";

/**
 * Types to observe for object creation events.
 *
 * <p>
 * When configured, the table re-evaluates its row function when objects of these types
 * are created. This enables automatic row insertion for tables that use
 * {@code all(...).filter(...)} style row queries.
 * </p>
 *
 * <p>
 * When empty (default), only updates and deletes of currently displayed rows are
 * observed. This is correct for tables whose rows come directly from a channel.
 * </p>
 */
@Name(OBSERVED_TYPES)
@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
List<TLModelPartRef> getObservedTypes();
```

- [ ] **Step 2: Replace createControl() method**

Replace the entire `createControl()` method (lines 178-247) with:

```java
@Override
public IReactControl createControl(ViewContext context) {
    // 1. Resolve input channels.
    List<ChannelRef> inputRefs = _config.getInputs();
    List<ViewChannel> inputChannels = new ArrayList<>(inputRefs.size());
    for (ChannelRef ref : inputRefs) {
        inputChannels.add(context.resolveChannel(ref));
    }

    // 2. Execute initial row query.
    Object[] channelValues = readChannelValues(inputChannels);
    Collection<?> rows = executeRowsQuery(_rowsExecutor, channelValues);

    // 3. Build table configuration.
    TableConfiguration tableConfig;
    List<TLModelPartRef> typeRefs = _config.getTypes();
    if (typeRefs != null && !typeRefs.isEmpty()) {
        Set<TLClass> types = resolveTypes(typeRefs);
        tableConfig = TableConfigurationFactory.build(new GenericTableConfigurationProvider(types));
    } else if (_columnsProvider != null) {
        tableConfig = TableConfigurationFactory.build(_columnsProvider);
    } else {
        tableConfig = TableConfigurationFactory.table();
    }

    // 4. Build column names from configuration.
    List<String> columnNames = new ArrayList<>(tableConfig.getDefaultColumns());
    if (columnNames.isEmpty()) {
        columnNames = new ArrayList<>(tableConfig.createColumnIndex().keySet());
    }

    // 5. Create ObjectTableModel.
    ObjectTableModel tableModel =
        new ObjectTableModel(columnNames, tableConfig, new ArrayList<>(rows));

    // 6. Wrap in ObservableTableModel.
    Set<TLStructuredType> observedTypes = resolveObservedTypes();
    QueryExecutor rowsExec = _rowsExecutor;
    ObservableTableModel observableModel = new ObservableTableModel(
        tableModel,
        args -> {
            Collection<?> result = executeRowsQuery(rowsExec, args);
            return result;
        },
        observedTypes,
        inputChannels
    );

    // 7. Create cell provider.
    ReactCellControlProvider cellProvider = createCellProvider(context);

    // 8. Create ReactTableControl (uses inner model directly).
    ReactTableControl tableControl = new ReactTableControl(context, tableModel, cellProvider);

    // 9. Wire selection channel.
    ChannelRef selectionRef = _config.getSelection();
    if (selectionRef != null) {
        ViewChannel selectionChannel = context.resolveChannel(selectionRef);
        tableControl.setSelectionListener(new ReactTableControl.SelectionListener() {
            @Override
            public void selectionChanged(Set<Object> selectedRows) {
                if (selectedRows.size() == 1) {
                    selectionChannel.set(selectedRows.iterator().next());
                } else if (selectedRows.isEmpty()) {
                    selectionChannel.set(null);
                } else {
                    selectionChannel.set(selectedRows);
                }
            }
        });
    }

    // 10. Lazy attach on render, cleanup on dispose.
    tableControl.addBeforeWriteAction(() -> {
        observableModel.attach(context.getModelScope());
    });
    tableControl.addCleanupAction(observableModel::detach);

    return tableControl;
}
```

- [ ] **Step 3: Add resolveObservedTypes helper and import**

Add at the end of the class (before closing `}`):

```java
private Set<TLStructuredType> resolveObservedTypes() {
    List<TLModelPartRef> refs = _config.getObservedTypes();
    if (refs == null || refs.isEmpty()) {
        return Set.of();
    }
    Set<TLStructuredType> types = new HashSet<>();
    for (TLModelPartRef ref : refs) {
        try {
            types.add((TLStructuredType) ref.resolveType());
        } catch (ConfigurationException ex) {
            throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName(), ex);
        }
    }
    return types;
}
```

Add import:

```java
import com.top_logic.layout.view.model.ObservableTableModel;
import com.top_logic.model.TLStructuredType;
```

- [ ] **Step 4: Remove old channel listener wiring**

The old channel listener wiring (lines 237-244 in the original file) is now
handled by `ObservableTableModel`. Verify it was removed in the `createControl()`
replacement above. The old code was:

```java
// 9. Wire input channel listeners for re-query on change.
ViewChannel.ChannelListener refreshListener = (sender, oldValue, newValue) -> {
    Object[] newValues = readChannelValues(inputChannels);
    Collection<?> newRows = executeRowsQuery(_rowsExecutor, newValues);
    tableModel.setRowObjects(new ArrayList<>(newRows));
};
for (ViewChannel channel : inputChannels) {
    channel.addListener(refreshListener);
}
```

This is no longer present in the new `createControl()`. Confirmed removed.

- [ ] **Step 5: Build view module**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```
Ticket #29108: Integrate ObservableTableModel into TableElement.
```

---

## Chunk 3: Manual Integration Test

### Task 9: Test with demo application

**Files:**
- No file changes. Manual verification.

- [ ] **Step 1: Start demo app**

Run from project root: `mvn jetty:run -pl com.top_logic.demo`
Wait for startup (can take a few minutes).

- [ ] **Step 2: Verify basic table rendering**

Open a view that contains a `<table>` element. Confirm the table renders
normally with rows, scrolling, and sorting working as before.

- [ ] **Step 3: Verify update forwarding**

1. Open a table showing persistent objects (e.g. a list of contacts).
2. In a second browser tab, edit one of the displayed objects and save.
3. Wait up to 30 seconds (heartbeat interval).
4. Verify the first tab's table shows the updated value without manual refresh.

- [ ] **Step 4: Verify delete forwarding**

1. In the second tab, delete one of the displayed objects.
2. Wait up to 30 seconds.
3. Verify the row disappears from the first tab's table.

- [ ] **Step 5: Verify create forwarding (if observed-types configured)**

1. Configure a table with `<observed-types><type name="..."/></observed-types>`.
2. Create a new object of that type in a second tab.
3. Wait up to 30 seconds.
4. Verify the new row appears in the first tab's table.

- [ ] **Step 6: Verify channel change still works**

1. Change the input to the table (e.g. select a different parent object).
2. Verify the table updates immediately with new rows.

---

## Summary

| Task | Module | Description |
|------|--------|-------------|
| 1 | core | `createForWindow()` factory on GlobalModelEventForwarder |
| 2 | react | `getModelScope()` on ReactContext + DefaultReactContext |
| 3 | react | Per-window ModelScope in ReactWindowRegistry |
| 4 | react | `addBeforeWriteAction()` on ReactControl |
| 5 | react | Synthesize events after command in ReactServlet |
| 6 | react | Synthesize events in SSE heartbeat |
| 7 | view | ObservableTableModel (new class) |
| 8 | view | TableElement integration |
| 9 | -- | Manual integration test |
