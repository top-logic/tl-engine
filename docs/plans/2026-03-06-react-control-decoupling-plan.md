# ReactControl Decoupling Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Decouple ReactControl from AbstractControlBase so new-world React controls never touch DisplayContext on the command path, eliminating the NPE when ViewServlet-bootstrapped buttons are clicked.

**Architecture:** ReactControl stops extending AbstractVisibleControl and instead implements HTMLFragment + ViewControl + ReactCommandTarget directly. Commands are declared with `@ReactCommand` annotations and dispatched via cached MethodHandles. SSEUpdateQueue owns session-scoped ID allocation and stores ReactCommandTarget instead of CommandListener.

**Tech Stack:** Java 17, MethodHandles, ConcurrentHashMap for lazy per-class caching, msgbuf for JSON serialization.

**Design doc:** `docs/plans/2026-03-06-react-control-decoupling-design.md`

---

### Task 1: Create the new interfaces and annotation

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactCommandTarget.java`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactCommand.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ViewCommandAction.java`

**Step 1: Create ReactCommandTarget interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.Map;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Lean interface for controls that can receive commands from the React client.
 *
 * <p>
 * Replaces {@link com.top_logic.layout.CommandListener} for React controls. Unlike
 * {@code CommandListener}, this interface does not require a
 * {@link com.top_logic.layout.DisplayContext} for command dispatch.
 * </p>
 */
public interface ReactCommandTarget {

	/**
	 * The unique control ID used for command routing.
	 */
	String getID();

	/**
	 * Dispatches a command sent by the React client.
	 *
	 * @param commandName
	 *        The command identifier (e.g. "click", "sort").
	 * @param arguments
	 *        The command arguments from the client.
	 * @return The result of the command execution.
	 */
	HandlerResult executeCommand(String commandName, Map<String, Object> arguments);
}
```

**Step 2: Create @ReactCommand annotation**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a command handler for a React client command.
 *
 * <p>
 * Annotated methods may declare any subset of these parameters in any order:
 * </p>
 * <ul>
 * <li>{@link ViewDisplayContext} - the control's stored view context</li>
 * <li>{@code Map<String, Object>} - the raw arguments from the client</li>
 * </ul>
 *
 * <p>
 * No parameters is also valid. Return type must be
 * {@link com.top_logic.tool.boundsec.HandlerResult}.
 * </p>
 *
 * <p>
 * Resolution happens lazily on first instantiation of each {@link ReactControl} subclass and is
 * cached per class. The hot path uses {@link java.lang.invoke.MethodHandle} for zero-reflection
 * dispatch.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReactCommand {

	/**
	 * The command identifier sent by the React client (e.g. "click", "sort", "select").
	 */
	String value();
}
```

**Step 3: Create ViewCommandAction interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A command action in the view system.
 *
 * <p>
 * Replaces the old-world {@link com.top_logic.layout.basic.Command} interface for view-system
 * buttons. Receives a {@link ViewDisplayContext} instead of a
 * {@link com.top_logic.layout.DisplayContext}.
 * </p>
 */
@FunctionalInterface
public interface ViewCommandAction {

	/**
	 * Executes this action.
	 *
	 * @param context
	 *        The view display context.
	 * @return The result of the command execution.
	 */
	HandlerResult execute(ViewDisplayContext context);
}
```

**Step 4: Commit**

```
Ticket #29102: Add ReactCommandTarget, @ReactCommand annotation, and ViewCommandAction.
```

---

### Task 2: Create the command dispatch infrastructure

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactCommandInvoker.java`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactCommandMap.java`

**Step 1: Create ReactCommandInvoker**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Invokes a single {@link ReactCommand}-annotated method on a {@link ReactControl}.
 *
 * <p>
 * Built once per annotated method during {@link ReactCommandMap} resolution. Uses a
 * {@link MethodHandle} for zero-reflection dispatch on the hot path.
 * </p>
 */
class ReactCommandInvoker {

	private final MethodHandle _handle;

	private final boolean _needsContext;

	private final boolean _needsArgs;

	/**
	 * Creates a new invoker.
	 *
	 * @param handle
	 *        The method handle for the annotated method. First parameter is always the receiver
	 *        (ReactControl subclass).
	 * @param needsContext
	 *        Whether the method declares a {@link ViewDisplayContext} parameter.
	 * @param needsArgs
	 *        Whether the method declares a {@code Map<String, Object>} parameter.
	 */
	ReactCommandInvoker(MethodHandle handle, boolean needsContext, boolean needsArgs) {
		_handle = handle;
		_needsContext = needsContext;
		_needsArgs = needsArgs;
	}

	/**
	 * Invokes the command method on the given control.
	 */
	HandlerResult invoke(ReactControl control, ViewDisplayContext context,
			Map<String, Object> arguments) {
		try {
			if (_needsContext && _needsArgs) {
				return (HandlerResult) _handle.invoke(control, context, arguments);
			} else if (_needsContext) {
				return (HandlerResult) _handle.invoke(control, context);
			} else if (_needsArgs) {
				return (HandlerResult) _handle.invoke(control, arguments);
			} else {
				return (HandlerResult) _handle.invoke(control);
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to invoke @ReactCommand on " + control.getClass().getName(), ex);
		}
	}
}
```

**Step 2: Create ReactCommandMap**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Cached mapping from command IDs to {@link ReactCommandInvoker}s for a single
 * {@link ReactControl} subclass.
 *
 * <p>
 * Built lazily on first instantiation of each class via
 * {@link #forClass(Class)}. Scans the class hierarchy for
 * {@link ReactCommand}-annotated methods, builds {@link MethodHandle}s, and determines parameter
 * injection flags.
 * </p>
 */
class ReactCommandMap {

	private final Map<String, ReactCommandInvoker> _invokers;

	private ReactCommandMap(Map<String, ReactCommandInvoker> invokers) {
		_invokers = invokers;
	}

	/**
	 * Looks up the invoker for the given command ID.
	 *
	 * @return The invoker, or {@code null} if no command is registered for the given ID.
	 */
	ReactCommandInvoker get(String commandId) {
		return _invokers.get(commandId);
	}

	/**
	 * Scans the given class hierarchy for {@link ReactCommand}-annotated methods and builds a
	 * {@link ReactCommandMap}.
	 *
	 * @throws IllegalStateException
	 *         If a method has an unsupported parameter type or return type.
	 */
	static ReactCommandMap forClass(Class<?> controlClass) {
		Map<String, ReactCommandInvoker> invokers = new HashMap<>();
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		// Walk class hierarchy bottom-up. Subclass methods win over superclass for same command ID.
		for (Class<?> clazz = controlClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getDeclaredMethods()) {
				ReactCommand annotation = method.getAnnotation(ReactCommand.class);
				if (annotation == null) {
					continue;
				}

				String commandId = annotation.value();
				if (invokers.containsKey(commandId)) {
					// Subclass already registered a handler for this command.
					continue;
				}

				validate(method);
				method.setAccessible(true);

				boolean needsContext = false;
				boolean needsArgs = false;
				for (Class<?> paramType : method.getParameterTypes()) {
					if (ViewDisplayContext.class.isAssignableFrom(paramType)) {
						needsContext = true;
					} else if (Map.class.isAssignableFrom(paramType)) {
						needsArgs = true;
					}
				}

				try {
					MethodHandle handle = lookup.unreflect(method);
					invokers.put(commandId, new ReactCommandInvoker(handle, needsContext, needsArgs));
				} catch (IllegalAccessException ex) {
					throw new IllegalStateException(
						"Cannot access @ReactCommand method " + method + " on " + controlClass.getName(), ex);
				}
			}
		}
		return new ReactCommandMap(invokers);
	}

	private static void validate(Method method) {
		if (method.getReturnType() != HandlerResult.class) {
			throw new IllegalStateException(
				"@ReactCommand method " + method.getName() + " must return HandlerResult, but returns "
					+ method.getReturnType().getName());
		}
		for (Class<?> paramType : method.getParameterTypes()) {
			if (!ViewDisplayContext.class.isAssignableFrom(paramType)
				&& !Map.class.isAssignableFrom(paramType)) {
				throw new IllegalStateException(
					"@ReactCommand method " + method.getName() + " has unsupported parameter type: "
						+ paramType.getName()
						+ ". Allowed: ViewDisplayContext, Map<String, Object>.");
			}
		}
	}
}
```

**Step 3: Commit**

```
Ticket #29102: Add ReactCommandMap and ReactCommandInvoker for annotation-based dispatch.
```

---

### Task 3: Add session-scoped ID allocation to SSEUpdateQueue

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/SSEUpdateQueue.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultViewDisplayContext.java`

**Step 1: Add allocateId() and change control type in SSEUpdateQueue**

Change `_controls` from `Map<String, CommandListener>` to `Map<String, ReactCommandTarget>`. Add `AtomicInteger _nextId` and `allocateId()` method. Update `registerControl`, `unregisterControl`, `getControl` to use `ReactCommandTarget`. Update `sendFullState` to use `ReactCommandTarget` instead of `CommandListener`.

Remove the import of `com.top_logic.layout.CommandListener`.

**Step 2: Update DefaultViewDisplayContext**

Replace the local `AtomicInteger _nextId` with delegation to `SSEUpdateQueue.allocateId()`.

The constructor changes from:
```java
public DefaultViewDisplayContext(String contextPath, String windowName, SSEUpdateQueue sseQueue)
```
The `allocateId()` method changes from:
```java
return "v" + _nextId.getAndIncrement();
```
to:
```java
return _sseQueue.allocateId();
```

Remove the `AtomicInteger` field.

**Step 3: Commit**

```
Ticket #29102: Move ID allocation to session-scoped SSEUpdateQueue.
```

---

### Task 4: Rewrite ReactControl base class

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactControl.java`

This is the core change. ReactControl stops extending AbstractVisibleControl and implements the three interfaces directly.

**Step 1: Change class declaration and fields**

Change from:
```java
public class ReactControl extends AbstractVisibleControl implements ViewControl {
```
to:
```java
public class ReactControl implements HTMLFragment, ViewControl, ReactCommandTarget {
```

Add the lazy command map cache as a static field:
```java
private static final ConcurrentHashMap<Class<?>, ReactCommandMap> COMMAND_MAPS = new ConcurrentHashMap<>();
```

Replace the `id` field (previously inherited protected field from AbstractControlBase) with:
```java
private String _id;
```

Remove the constructor that accepts `Map<String, ControlCommand> commands`. Replace with simpler constructors:
```java
public ReactControl(Object model, String reactModule) {
    _model = model;
    _reactModule = reactModule;
    _reactState = new HashMap<>();
}
```

**Step 2: Implement getID() and helper methods**

```java
@Override
public String getID() {
    if (_id == null) {
        throw new IllegalStateException("Control has no ID. Call write() first.");
    }
    return _id;
}

protected void writeIdAttribute(TagWriter out) throws IOException {
    out.writeAttribute("id", _id);
}

protected void writeControlClasses(TagWriter out) throws IOException {
    out.beginCssClasses();
    out.append("is-control");
    out.endCssClasses();
}
```

**Step 3: Implement HTMLFragment.write (legacy bridge)**

```java
@Override
public void write(DisplayContext context, TagWriter out) throws IOException {
    write(ViewDisplayContext.fromDisplayContext(context), out);
}
```

This replaces the old `internalWrite()` hook. The `write(ViewDisplayContext, TagWriter)` method stays as-is.

**Step 4: Implement ReactCommandTarget.executeCommand**

```java
@Override
public HandlerResult executeCommand(String commandName, Map<String, Object> arguments) {
    ReactCommandMap commandMap = COMMAND_MAPS.computeIfAbsent(getClass(), ReactCommandMap::forClass);
    ReactCommandInvoker invoker = commandMap.get(commandName);
    if (invoker == null) {
        throw new IllegalArgumentException(
            "No @ReactCommand(\"" + commandName + "\") on " + getClass().getName());
    }
    return invoker.invoke(this, _viewContext, arguments);
}
```

**Step 5: Replace lifecycle hooks**

Replace `internalDetach()` with a new `cleanup()` method that subclasses can override:
```java
/**
 * Called when this control is removed from its SSE queue.
 *
 * <p>
 * Subclasses override to release model listeners or other resources.
 * </p>
 */
protected void onCleanup() {
    // Default: no-op. Subclasses override.
}
```

Update `cleanupTree()` to call `onCleanup()` before unregistering:
```java
public final void cleanupTree() {
    cleanupChildren();
    onCleanup();
    SSEUpdateQueue queue = _sseQueue;
    if (queue != null) {
        queue.unregisterControl(this);
        _sseQueue = null;
    }
    _viewContext = null;
}
```

Replace `internalWrite(DisplayContext, TagWriter)` usages: subclasses that overrode `internalWrite` to run initialization before rendering should now override a new `onBeforeWrite(ViewDisplayContext)` hook called at the start of `write(ViewDisplayContext, TagWriter)`:

```java
@Override
public void write(ViewDisplayContext context, TagWriter out) throws IOException {
    _viewContext = context;
    _id = context.allocateId();
    SSEUpdateQueue queue = context.getSSEQueue();
    _sseQueue = queue;
    queue.registerControl(this);

    onBeforeWrite(context);

    String stateJson = toJsonString(_reactState, context);

    out.beginBeginTag(HTMLConstants.DIV);
    writeIdAttribute(out);
    writeControlClasses(out);
    out.writeAttribute("data-react-module", _reactModule);
    out.writeAttribute("data-react-state", stateJson);
    out.writeAttribute("data-window-name", context.getWindowName());
    out.writeAttribute("data-context-path", context.getContextPath());
    out.endBeginTag();
    out.endTag(HTMLConstants.DIV);
}

/**
 * Hook called before the control is rendered, after ID and SSE queue are assigned.
 *
 * <p>
 * Subclasses override to perform initialization that must happen before rendering, such as
 * registering model listeners or rebuilding state caches.
 * </p>
 */
protected void onBeforeWrite(ViewDisplayContext context) {
    // Default: no-op.
}
```

Remove all imports of: `AbstractVisibleControl`, `ControlCommand`, `UpdateQueue`, `ControlScope`.

Update `_id` assignment in `writeAsChild` to use `_id` field instead of inherited `id`.

**Step 6: Remove the old constructor overload**

Remove the constructor `ReactControl(Object model, String reactModule, Map<String, ControlCommand> commands)`. All subclasses will be migrated to use `ReactControl(Object model, String reactModule)`.

**Step 7: Commit**

```
Ticket #29102: Rewrite ReactControl to implement HTMLFragment directly without AbstractControlBase.
```

---

### Task 5: Update ReactServlet command dispatch

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactServlet.java`

**Step 1: Change handleCommand to use ReactCommandTarget**

In `handleCommand()`, change:
```java
CommandListener control = queue.getControl(controlId);
// ...
result = control.executeCommand(displayContext, commandName, arguments);
```
to:
```java
ReactCommandTarget control = queue.getControl(controlId);
// ...
result = control.executeCommand(commandName, arguments);
```

The `DisplayContext` is still retrieved from the request for `installSubSession()` and `forwardPendingUpdates()`, but it is no longer passed to the control.

**Step 2: Update handleUpload similarly**

The upload path also needs to change from `CommandListener` to `ReactCommandTarget`.

**Step 3: Update handleState and handleDataDownload**

Change `CommandListener` to `ReactCommandTarget` in the lookup calls.

**Step 4: Remove unused CommandListener import**

**Step 5: Commit**

```
Ticket #29102: Update ReactServlet to dispatch commands via ReactCommandTarget.
```

---

### Task 6: Update ButtonElement to use ViewCommandAction

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ButtonElement.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactButtonControl.java`

**Step 1: Rewrite ReactButtonControl**

Remove `ClickCommand` inner class and `COMMANDS` map. Change `_action` from `Command` to `ViewCommandAction`. Add `@ReactCommand("click")` method.

```java
public class ReactButtonControl extends ReactControl {

    private static final String LABEL = "label";
    private static final String DISABLED = "disabled";

    private final ViewCommandAction _action;

    public ReactButtonControl(String label, ViewCommandAction action) {
        super(null, "TLButton");
        _action = action;
        putState(LABEL, label);
    }

    public void setLabel(String label) {
        putState(LABEL, label);
    }

    public void setDisabled(boolean disabled) {
        putState(DISABLED, disabled);
    }

    @ReactCommand("click")
    HandlerResult handleClick(ViewDisplayContext context) {
        return _action.execute(context);
    }
}
```

**Step 2: Update ButtonElement**

Change the lambda from:
```java
ReactButtonControl control = new ReactButtonControl(label,
    displayContext -> model.executeCommand(ViewDisplayContext.fromDisplayContext(displayContext)));
```
to:
```java
ReactButtonControl control = new ReactButtonControl(label,
    context -> model.executeCommand(context));
```

Update the no-command case from:
```java
return new ReactButtonControl("", displayContext -> HandlerResult.DEFAULT_RESULT);
```
to:
```java
return new ReactButtonControl("", context -> HandlerResult.DEFAULT_RESULT);
```

Remove imports of `com.top_logic.layout.basic.Command`, `com.top_logic.layout.DisplayContext`, and `com.top_logic.layout.react.ViewDisplayContext` (the static `fromDisplayContext` call).

Add import of `com.top_logic.layout.view.command.ViewCommandAction`.

**Step 3: Commit**

```
Ticket #29102: Rewrite ReactButtonControl with @ReactCommand and ViewCommandAction.
```

---

### Task 7: Migrate simple subclasses (no overrides)

These controls only have ControlCommand inner classes and constructors to update. No lifecycle hook overrides.

**Files (batch):**
- `control/overlay/ReactSnackbarControl.java`
- `control/overlay/ReactDialogControl.java`
- `control/overlay/ReactDrawerControl.java`
- `control/overlay/ReactMenuControl.java`
- `control/toggle/ReactToggleButtonControl.java`
- `control/layout/ReactFormGroupControl.java`
- `control/layout/ReactPanelControl.java`
- `control/layout/ReactSplitPanelControl.java`
- `control/layout/ReactMaximizeRootControl.java`

**Per-file migration pattern:**

1. Delete all `static class XxxCommand extends ControlCommand { ... }` inner classes
2. Delete `private static final Map<String, ControlCommand> COMMANDS = createCommandMap(...);`
3. Change constructor from `super(model, MODULE, COMMANDS)` to `super(model, MODULE)`
4. For each deleted command, add an `@ReactCommand("commandId")` method with the body from the old `execute()`, removing the `DisplayContext context` and `Control control` parameters. Access `this` directly instead of casting `control`.
5. Remove imports of `ControlCommand`, `Control`, `DisplayContext`, `ResKey` (if only used for I18N key)

**Example migration (ReactSnackbarControl):**

Before:
```java
public static class DismissCommand extends ControlCommand {
    public DismissCommand() { super("dismiss"); }
    @Override public ResKey getI18NKey() { return I18NConstants.REACT_SNACKBAR_DISMISS; }
    @Override
    protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
        ReactSnackbarControl snackbar = (ReactSnackbarControl) control;
        snackbar.hide();
        snackbar._dismissHandler.run();
        return HandlerResult.DEFAULT_RESULT;
    }
}
```

After:
```java
@ReactCommand("dismiss")
HandlerResult handleDismiss() {
    hide();
    _dismissHandler.run();
    return HandlerResult.DEFAULT_RESULT;
}
```

**Step: Commit**

```
Ticket #29102: Migrate simple ReactControl subclasses to @ReactCommand.
```

---

### Task 8: Migrate controls with lifecycle overrides

These controls override `internalDetach()` or `internalWrite()` from the old hierarchy and need to use the new hooks (`onCleanup()`, `onBeforeWrite()`).

**Files:**
- `control/download/ReactDownloadControl.java` - overrides `internalDetach()`
- `control/audio/ReactAudioPlayerControl.java` - overrides `internalDetach()`
- `control/photo/ReactPhotoViewerControl.java` - overrides `internalDetach()`
- `control/tree/ReactTreeControl.java` - overrides `internalWrite()`
- `control/form/ReactFormFieldControl.java` - overrides `internalWrite()` and `writeAsChild()`

**Migration for internalDetach() overrides:**

Replace:
```java
@Override
protected void internalDetach() {
    _model.removeListener(_modelListener);
    super.internalDetach();
}
```
with:
```java
@Override
protected void onCleanup() {
    _model.removeListener(_modelListener);
}
```

**Migration for internalWrite() overrides:**

Replace:
```java
@Override
protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
    // initialization logic
    super.internalWrite(context, out);
}
```
with:
```java
@Override
protected void onBeforeWrite(ViewDisplayContext context) {
    // initialization logic
}
```

**Special case: ReactFormFieldControl**

This control also overrides `writeAsChild()` to call `registerFieldListeners()`. Since `writeAsChild` is defined on ReactControl (not AbstractControlBase), this override stays as-is but the call to `super.writeAsChild()` remains valid. The `registerFieldListeners()` call in the old `internalWrite` moves to `onBeforeWrite`.

Also apply the standard command migration (remove `ValueChanged` ControlCommand, add `@ReactCommand("valueChanged")`).

**Step: Commit**

```
Ticket #29102: Migrate ReactControl subclasses with lifecycle overrides to new hooks.
```

---

### Task 9: Migrate complex controls (table, tree, sidebar, tabbar)

These controls have many commands and child management.

**Files:**
- `control/table/ReactTableControl.java` - 7 commands
- `control/tree/ReactTreeControl.java` - 7 commands
- `control/sidebar/ReactSidebarControl.java` - 4 commands
- `control/tabbar/ReactTabBarControl.java` - 1 command
- `control/upload/ReactFileUploadControl.java`

**Same migration pattern as Task 7**, but more commands to convert.

**Example: ReactTableControl ScrollCommand**

Before:
```java
static class ScrollCommand extends ControlCommand {
    ScrollCommand() { super("scroll"); }
    @Override public ResKey getI18NKey() { return ResKey.legacy("react.table.scroll"); }
    @Override
    protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
        ReactTableControl table = (ReactTableControl) control;
        int start = ((Number) arguments.get("start")).intValue();
        int count = ((Number) arguments.get("count")).intValue();
        table.updateViewport(start, count);
        return HandlerResult.DEFAULT_RESULT;
    }
}
```

After:
```java
@ReactCommand("scroll")
HandlerResult handleScroll(Map<String, Object> arguments) {
    int start = ((Number) arguments.get("start")).intValue();
    int count = ((Number) arguments.get("count")).intValue();
    updateViewport(start, count);
    return HandlerResult.DEFAULT_RESULT;
}
```

Note: each command that previously cast `control` to the specific type now simply accesses `this` directly, since the method is on the control class itself.

**Step: Commit**

```
Ticket #29102: Migrate complex ReactControl subclasses (table, tree, sidebar, tabbar) to @ReactCommand.
```

---

### Task 10: Migrate leaf controls (no commands)

These controls have no commands at all and only need the constructor change.

**Files:**
- `control/layout/ReactCardControl.java`
- `control/layout/ReactFormLayoutControl.java`
- `control/layout/ReactGridControl.java`
- `control/layout/ReactStackControl.java`
- `control/layout/ReactFormFieldChromeControl.java`
- `control/layout/ReactDeckPaneControl.java`
- `control/table/ReactTextCellControl.java`
- `control/table/ReactResourceCellControl.java`
- `control/nav/ReactAppShellControl.java`
- `control/nav/ReactAppBarControl.java`
- `control/nav/ReactBottomBarControl.java`
- `control/nav/ReactBreadcrumbControl.java`
- `control/audio/ReactAudioRecorderControl.java`
- `control/photo/ReactPhotoCaptureControl.java`
- `control/ReactFieldListControl.java`

**Per-file change:**
- Remove the `COMMANDS` argument from the `super()` call (if present)
- Remove imports of `ControlCommand`

**Step: Commit**

```
Ticket #29102: Update leaf ReactControl subclasses (no commands) for new base class.
```

---

### Task 11: Migrate demo module controls

**Files:**
- `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoFieldTogglesControl.java`
- `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactCounterComponent.java`

Same migration pattern. These may have custom commands.

**Step: Commit**

```
Ticket #29102: Migrate demo module ReactControl subclasses to @ReactCommand.
```

---

### Task 12: Build verification

**Step 1: Build the react module**

Run:
```bash
cd com.top_logic.layout.react && mvn clean install -DskipTests=true
```
Expected: BUILD SUCCESS

**Step 2: Build the view module**

Run:
```bash
cd com.top_logic.layout.view && mvn clean install -DskipTests=true
```
Expected: BUILD SUCCESS

**Step 3: Build the demo module**

Run:
```bash
cd com.top_logic.demo && mvn clean install -DskipTests=true
```
Expected: BUILD SUCCESS

**Step 4: Fix any compilation errors**

If any errors remain, they are likely:
- Missing imports in migrated files
- Remaining references to `createCommandMap` or `ControlCommand`
- `internalDetach`/`internalWrite` overrides not yet converted

Fix and recommit.

---

### Task 13: Manual smoke test

**Step 1: Start the demo application**

Run the demo app at http://localhost:8080 (credentials: root / root1234).

**Step 2: Test ViewServlet button click**

Navigate to a ViewServlet-bootstrapped page (e.g. `/view/`). Click a button. Verify no NPE in the server log. This was the original bug.

**Step 3: Test table, tree, form controls**

Navigate to pages with tables, trees, and forms. Verify sorting, scrolling, selection, expand/collapse, and form field changes all work without errors.

**Step 4: Commit any fixes**

```
Ticket #29102: Fix issues found during smoke testing.
```
