# Dirty-Check for React UI — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent users from accidentally losing unsaved form changes when switching tabs or changing selection in the React UI.

**Architecture:** Two trigger mechanisms, one resolution path. (1) `ViewChannel` gets a veto mechanism — `FormControl` registers a `VetoListener` on its input channel that blocks selection changes while dirty. (2) `TabBarElement` creates an isolated `DirtyChannel` per tab — the tab bar checks it before switching. Both paths throw `ChannelVetoException` carrying a list of dirty `StateHandler`s and a continuation `Runnable`. `ReactCommandInvoker` catches the exception and opens a confirmation dialog via the existing `DialogManager`.

**Tech Stack:** Java 17, React/TypeScript (tl-react-bridge), Maven, JUnit 4

---

## File Structure

### New Files

| File | Responsibility |
|------|---------------|
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/StateHandler.java` | Interface: isDirty, hasErrors, executeSave, executeDiscard, getDescription |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelVetoException.java` | RuntimeException carrying `List<StateHandler>` + `Runnable` continuation |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DirtyChannel.java` | Holds `Map<StateHandler, Boolean>`, provides `getDirtyHandlers()` |
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/DirtyConfirmDialogControl.java` | Server-side React control for the 3-button confirmation dialog |
| `com.top_logic.layout.react/react-src/controls/TLDirtyConfirmDialog.tsx` | React component rendering Save/Discard/Cancel buttons |
| `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelVeto.java` | Tests for veto mechanism on DefaultViewChannel |
| `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDirtyChannel.java` | Tests for DirtyChannel |

### Modified Files

| File | Change |
|------|--------|
| `com.top_logic.layout.view/.../channel/ViewChannel.java` | Add `addVetoListener` / `removeVetoListener` + `VetoListener` interface |
| `com.top_logic.layout.view/.../channel/DefaultViewChannel.java` | Collect vetos in `set()`, throw `ChannelVetoException` |
| `com.top_logic.layout.view/.../form/FormControl.java` | Implement `StateHandler`, register/deregister veto on input channel, register/deregister on DirtyChannel |
| `com.top_logic.layout.view/.../element/TabBarElement.java` | Create isolated ViewContext per tab with `DirtyChannel`, pass to content factory |
| `com.top_logic.layout.view/.../element/FormElement.java` | Bind FormControl to scope-local DirtyChannel |
| `com.top_logic.layout.view/.../ViewContext.java` | Add `getDirtyChannel()` / `setDirtyChannel(DirtyChannel)` |
| `com.top_logic.layout.view/.../DefaultViewContext.java` | Store and propagate `DirtyChannel` in child contexts |
| `com.top_logic.layout.react/.../control/tabbar/ReactTabBarControl.java` | Check DirtyChannel before tab switch, throw ChannelVetoException |
| `com.top_logic.layout.react/.../control/ReactCommandInvoker.java` | Catch `ChannelVetoException`, open confirmation dialog |

---

## Task 1: StateHandler Interface

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/StateHandler.java`

- [ ] **Step 1: Create the StateHandler interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

/**
 * Abstraction for a component that holds state that can be dirty (modified but not yet persisted).
 *
 * <p>
 * Provides the operations needed by the dirty-check confirmation dialog: querying dirty/error
 * status, saving, and discarding changes.
 * </p>
 *
 * @see com.top_logic.layout.view.channel.DirtyChannel
 * @see com.top_logic.layout.view.channel.ChannelVetoException
 */
public interface StateHandler {

	/**
	 * Whether this handler has unsaved changes.
	 */
	boolean isDirty();

	/**
	 * Whether this handler currently has validation errors that would prevent saving.
	 */
	boolean hasErrors();

	/**
	 * Persists the current changes.
	 *
	 * <p>
	 * Must only be called when {@link #hasErrors()} returns {@code false}.
	 * </p>
	 */
	void executeSave();

	/**
	 * Discards all unsaved changes, reverting to the last persisted state.
	 */
	void executeDiscard();

	/**
	 * A human-readable description of this handler for display in the confirmation dialog.
	 *
	 * <p>
	 * Typically the form title or the name of the edited object.
	 * </p>
	 */
	String getDescription();
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/StateHandler.java
git commit -m "Ticket #29108: Add StateHandler interface for dirty-check abstraction.

Analyse der bestehenden Dirty-Check-Infrastruktur und Einfuehrung des StateHandler-Interface."
```

---

## Task 2: VetoListener on ViewChannel

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ViewChannel.java`

- [ ] **Step 1: Add VetoListener interface and methods to ViewChannel**

Add after the `ChannelListener` interface (after line 70):

```java
	/**
	 * Observer that can block a pending value change on a {@link ViewChannel}.
	 *
	 * <p>
	 * Veto listeners are checked <em>before</em> the value is updated and before regular
	 * {@link ChannelListener}s are notified. If any veto listener returns a non-{@code null}
	 * {@link com.top_logic.layout.view.form.StateHandler}, the change is blocked and a
	 * {@link com.top_logic.layout.view.channel.ChannelVetoException} is thrown collecting all dirty
	 * handlers.
	 * </p>
	 *
	 * @see #addVetoListener(VetoListener)
	 */
	interface VetoListener {

		/**
		 * Checks whether the pending value change should be blocked.
		 *
		 * @param sender
		 *        The channel about to change.
		 * @param oldValue
		 *        The current value.
		 * @param newValue
		 *        The proposed new value.
		 * @return A dirty {@link com.top_logic.layout.view.form.StateHandler} if this listener
		 *         vetoes the change, or {@code null} to allow it.
		 */
		com.top_logic.layout.view.form.StateHandler checkVeto(ViewChannel sender, Object oldValue,
			Object newValue);
	}

	/**
	 * Adds a listener that is consulted before value changes.
	 *
	 * @param listener
	 *        The veto listener to add.
	 *
	 * @see VetoListener
	 */
	void addVetoListener(VetoListener listener);

	/**
	 * Removes a previously added veto listener.
	 *
	 * @param listener
	 *        The veto listener to remove.
	 */
	void removeVetoListener(VetoListener listener);
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ViewChannel.java
git commit -m "Ticket #29108: Add VetoListener API to ViewChannel.

VetoListener auf ViewChannel ergaenzt, damit Kanal-Aenderungen bei dirty Forms blockiert werden koennen."
```

---

## Task 3: ChannelVetoException

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelVetoException.java`

- [ ] **Step 1: Create the exception class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.view.form.StateHandler;

/**
 * Thrown when a {@link ViewChannel} value change is blocked by one or more
 * {@link ViewChannel.VetoListener}s because dependent forms have unsaved changes.
 *
 * <p>
 * Carries the list of all dirty {@link StateHandler}s and a {@link Runnable} continuation that
 * retries the blocked value change. The caller (typically
 * {@link com.top_logic.layout.react.control.ReactCommandInvoker}) catches this exception and opens
 * a confirmation dialog. After the user saves or discards, the continuation is executed to retry the
 * original action.
 * </p>
 */
public class ChannelVetoException extends RuntimeException {

	private final List<StateHandler> _dirtyHandlers;

	private final Runnable _continuation;

	/**
	 * Creates a new {@link ChannelVetoException}.
	 *
	 * @param dirtyHandlers
	 *        The dirty state handlers that caused the veto. Must not be empty.
	 * @param continuation
	 *        A {@link Runnable} that retries the blocked action after dirty state is resolved.
	 */
	public ChannelVetoException(List<StateHandler> dirtyHandlers, Runnable continuation) {
		super("Channel change vetoed by " + dirtyHandlers.size() + " dirty handler(s).");
		_dirtyHandlers = Collections.unmodifiableList(dirtyHandlers);
		_continuation = continuation;
	}

	/**
	 * The dirty state handlers that blocked the channel change.
	 */
	public List<StateHandler> getDirtyHandlers() {
		return _dirtyHandlers;
	}

	/**
	 * Retries the blocked action.
	 *
	 * <p>
	 * Must only be called after all dirty handlers have been saved or discarded, so that the retry
	 * does not trigger another veto.
	 * </p>
	 */
	public Runnable getContinuation() {
		return _continuation;
	}
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelVetoException.java
git commit -m "Ticket #29108: Add ChannelVetoException for dirty-check veto.

ChannelVetoException traegt Liste der dirty StateHandler und eine Continuation fuer den blockierten Kanalwechsel."
```

---

## Task 4: Implement Veto in DefaultViewChannel

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DefaultViewChannel.java`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelVeto.java`

- [ ] **Step 1: Write failing tests for the veto mechanism**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Tests for the {@link ViewChannel.VetoListener} mechanism on {@link DefaultViewChannel}.
 */
public class TestChannelVeto extends TestCase {

	/**
	 * Tests that a veto listener blocks the value change and throws
	 * {@link ChannelVetoException}.
	 */
	public void testVetoBlocksChange() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		StateHandler handler = stubHandler(true, false, "TestForm");
		channel.addVetoListener((sender, oldVal, newVal) -> handler);

		try {
			channel.set("blocked");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			assertEquals(1, ex.getDirtyHandlers().size());
			assertSame(handler, ex.getDirtyHandlers().get(0));
			assertNotNull(ex.getContinuation());
		}

		assertEquals("Value must not change on veto", "initial", channel.get());
	}

	/**
	 * Tests that regular listeners are NOT notified when a veto blocks the change.
	 */
	public void testListenersNotNotifiedOnVeto() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		StateHandler handler = stubHandler(true, false, "TestForm");
		channel.addVetoListener((sender, oldVal, newVal) -> handler);

		int[] callCount = { 0 };
		channel.addListener((sender, oldVal, newVal) -> callCount[0]++);

		try {
			channel.set("blocked");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// expected
		}
		assertEquals("Listener must not fire on veto", 0, callCount[0]);
	}

	/**
	 * Tests that a null return from veto listener allows the change.
	 */
	public void testNullVetoAllowsChange() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		channel.addVetoListener((sender, oldVal, newVal) -> null);
		channel.set("allowed");

		assertEquals("allowed", channel.get());
	}

	/**
	 * Tests that multiple veto listeners are collected into a single exception.
	 */
	public void testMultipleVetosCollected() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		StateHandler handler1 = stubHandler(true, false, "Form1");
		StateHandler handler2 = stubHandler(true, false, "Form2");
		channel.addVetoListener((sender, oldVal, newVal) -> handler1);
		channel.addVetoListener((sender, oldVal, newVal) -> handler2);

		try {
			channel.set("blocked");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			assertEquals(2, ex.getDirtyHandlers().size());
			assertTrue(ex.getDirtyHandlers().contains(handler1));
			assertTrue(ex.getDirtyHandlers().contains(handler2));
		}
	}

	/**
	 * Tests that continuation retries the set() call.
	 */
	public void testContinuationRetries() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		boolean[] allowChange = { false };
		StateHandler handler = stubHandler(true, false, "TestForm");
		channel.addVetoListener(
			(sender, oldVal, newVal) -> allowChange[0] ? null : handler);

		try {
			channel.set("target");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// Simulate: user saves form, handler is no longer dirty
			allowChange[0] = true;
			ex.getContinuation().run();
		}

		assertEquals("target", channel.get());
	}

	/**
	 * Tests that a removed veto listener no longer blocks changes.
	 */
	public void testRemoveVetoListener() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		StateHandler handler = stubHandler(true, false, "TestForm");
		ViewChannel.VetoListener vetoListener = (sender, oldVal, newVal) -> handler;
		channel.addVetoListener(vetoListener);

		channel.removeVetoListener(vetoListener);
		channel.set("allowed");

		assertEquals("allowed", channel.get());
	}

	private static StateHandler stubHandler(boolean dirty, boolean hasErrors, String description) {
		return new StateHandler() {
			@Override
			public boolean isDirty() {
				return dirty;
			}

			@Override
			public boolean hasErrors() {
				return hasErrors;
			}

			@Override
			public void executeSave() {
				// stub
			}

			@Override
			public void executeDiscard() {
				// stub
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
	}
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestChannelVeto -B &> $TMPDIR/mvn-veto-test.log; tail -20 $TMPDIR/mvn-veto-test.log`

Expected: Compilation errors — `addVetoListener` not yet implemented on `DefaultViewChannel`.

- [ ] **Step 3: Implement veto mechanism in DefaultViewChannel**

Add import and field (after existing `_listeners` field at line 26):

```java
import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.form.StateHandler;
```

Add the veto list field after `_listeners`:

```java
	private final CopyOnWriteArrayList<VetoListener> _vetoListeners = new CopyOnWriteArrayList<>();
```

Replace `set()` method (lines 44-52):

```java
	@Override
	public boolean set(Object newValue) {
		Object oldValue = _value;
		if (Objects.equals(oldValue, newValue)) {
			return false;
		}

		if (!_vetoListeners.isEmpty()) {
			List<StateHandler> dirtyHandlers = new ArrayList<>();
			for (VetoListener vl : _vetoListeners) {
				StateHandler handler = vl.checkVeto(this, oldValue, newValue);
				if (handler != null) {
					dirtyHandlers.add(handler);
				}
			}
			if (!dirtyHandlers.isEmpty()) {
				throw new ChannelVetoException(dirtyHandlers, () -> this.set(newValue));
			}
		}

		_value = newValue;
		notifyListeners(oldValue, newValue);
		return true;
	}
```

Add veto listener management methods (before `notifyListeners`):

```java
	@Override
	public void addVetoListener(VetoListener listener) {
		_vetoListeners.add(listener);
	}

	@Override
	public void removeVetoListener(VetoListener listener) {
		_vetoListeners.remove(listener);
	}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestChannelVeto -B &> $TMPDIR/mvn-veto-test.log; tail -20 $TMPDIR/mvn-veto-test.log`

Expected: All 6 tests pass.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DefaultViewChannel.java
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelVeto.java
git commit -m "Ticket #29108: Implement VetoListener mechanism on DefaultViewChannel.

Veto-Mechanismus auf DefaultViewChannel implementiert: set() sammelt alle Vetos und wirft ChannelVetoException."
```

---

## Task 5: DirtyChannel

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DirtyChannel.java`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDirtyChannel.java`

- [ ] **Step 1: Write failing tests for DirtyChannel**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Tests for {@link DirtyChannel}.
 */
public class TestDirtyChannel extends TestCase {

	/**
	 * Tests that a new DirtyChannel has no dirty handlers.
	 */
	public void testInitiallyClean() {
		DirtyChannel channel = new DirtyChannel();
		assertFalse(channel.hasDirtyHandlers());
		assertTrue(channel.getDirtyHandlers().isEmpty());
	}

	/**
	 * Tests that registering a dirty handler makes hasDirtyHandlers true.
	 */
	public void testRegisterDirty() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler handler = stubHandler("Form1");

		channel.updateState(handler, true);

		assertTrue(channel.hasDirtyHandlers());
		assertEquals(1, channel.getDirtyHandlers().size());
		assertSame(handler, channel.getDirtyHandlers().get(0));
	}

	/**
	 * Tests that setting a handler to clean removes it from the dirty set.
	 */
	public void testRegisterClean() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler handler = stubHandler("Form1");

		channel.updateState(handler, true);
		channel.updateState(handler, false);

		assertFalse(channel.hasDirtyHandlers());
		assertTrue(channel.getDirtyHandlers().isEmpty());
	}

	/**
	 * Tests multiple independent handlers.
	 */
	public void testMultipleHandlers() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler handler1 = stubHandler("Form1");
		StateHandler handler2 = stubHandler("Form2");

		channel.updateState(handler1, true);
		channel.updateState(handler2, true);
		assertEquals(2, channel.getDirtyHandlers().size());

		channel.updateState(handler1, false);
		assertEquals(1, channel.getDirtyHandlers().size());
		assertSame(handler2, channel.getDirtyHandlers().get(0));
	}

	/**
	 * Tests that setting a non-registered handler to clean is a no-op.
	 */
	public void testCleanUnregisteredIsNoOp() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler handler = stubHandler("Form1");

		channel.updateState(handler, false);
		assertFalse(channel.hasDirtyHandlers());
	}

	/**
	 * Tests that removeHandler removes the handler regardless of dirty state.
	 */
	public void testRemoveHandler() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler handler = stubHandler("Form1");

		channel.updateState(handler, true);
		channel.removeHandler(handler);

		assertFalse(channel.hasDirtyHandlers());
	}

	private static StateHandler stubHandler(String description) {
		return new StateHandler() {
			@Override
			public boolean isDirty() {
				return true;
			}

			@Override
			public boolean hasErrors() {
				return false;
			}

			@Override
			public void executeSave() {
			}

			@Override
			public void executeDiscard() {
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
	}
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestDirtyChannel -B &> $TMPDIR/mvn-dirty-test.log; tail -20 $TMPDIR/mvn-dirty-test.log`

Expected: Compilation error — `DirtyChannel` class does not exist.

- [ ] **Step 3: Implement DirtyChannel**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.view.form.StateHandler;

/**
 * Tracks the dirty state of multiple {@link StateHandler}s within a scope (e.g. a tab).
 *
 * <p>
 * Each {@link StateHandler} (typically a {@code FormControl}) registers its dirty state via
 * {@link #updateState(StateHandler, boolean)}. The channel aggregates these to answer "are there any
 * dirty handlers in this scope?" without requiring the caller to traverse a control tree.
 * </p>
 *
 * @see StateHandler
 */
public class DirtyChannel {

	private final Map<StateHandler, Boolean> _states = new LinkedHashMap<>();

	/**
	 * Updates the dirty state of a handler.
	 *
	 * @param handler
	 *        The state handler.
	 * @param dirty
	 *        {@code true} if dirty, {@code false} if clean. A clean handler is removed from the
	 *        tracking map.
	 */
	public void updateState(StateHandler handler, boolean dirty) {
		if (dirty) {
			_states.put(handler, Boolean.TRUE);
		} else {
			_states.remove(handler);
		}
	}

	/**
	 * Removes a handler from tracking, regardless of its dirty state.
	 *
	 * <p>
	 * Called during cleanup when a handler is disposed.
	 * </p>
	 *
	 * @param handler
	 *        The handler to remove.
	 */
	public void removeHandler(StateHandler handler) {
		_states.remove(handler);
	}

	/**
	 * Whether any tracked handler is currently dirty.
	 */
	public boolean hasDirtyHandlers() {
		return !_states.isEmpty();
	}

	/**
	 * Returns all currently dirty handlers.
	 *
	 * @return An unmodifiable snapshot of the dirty handlers.
	 */
	public List<StateHandler> getDirtyHandlers() {
		return List.copyOf(_states.keySet());
	}
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestDirtyChannel -B &> $TMPDIR/mvn-dirty-test.log; tail -20 $TMPDIR/mvn-dirty-test.log`

Expected: All 6 tests pass.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DirtyChannel.java
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDirtyChannel.java
git commit -m "Ticket #29108: Add DirtyChannel for scope-level dirty tracking.

DirtyChannel haelt Map<StateHandler, Boolean> fuer Tab-Scope-Level Dirty-Tracking."
```

---

## Task 6: DirtyChannel on ViewContext

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java`

- [ ] **Step 1: Add DirtyChannel accessors to ViewContext**

Add to `ViewContext.java` after the `setFormModel` method (after line 74):

```java
	/**
	 * The {@link DirtyChannel} of the nearest enclosing scope, or {@code null} if no dirty tracking
	 * is configured.
	 *
	 * <p>
	 * {@link StateHandler} implementations use this to register their dirty state so that enclosing
	 * containers (e.g. tab bars) can check for unsaved changes before navigation.
	 * </p>
	 *
	 * @see com.top_logic.layout.view.channel.DirtyChannel
	 */
	DirtyChannel getDirtyChannel();

	/**
	 * Sets the dirty channel for this context scope.
	 *
	 * <p>
	 * Called by container elements (e.g. tab bars) that want to track dirty state of their children.
	 * </p>
	 *
	 * @param dirtyChannel
	 *        The dirty channel, or {@code null}.
	 */
	void setDirtyChannel(DirtyChannel dirtyChannel);
```

Add import:

```java
import com.top_logic.layout.view.channel.DirtyChannel;
```

- [ ] **Step 2: Implement in DefaultViewContext**

Add field after `_formModel` (after line 41):

```java
	private DirtyChannel _dirtyChannel;
```

Add to the private constructor parameter list (line 54-56), adding `DirtyChannel dirtyChannel` as a new parameter:

Replace the private constructor:

```java
	private DefaultViewContext(ReactContext reactContext, String personalizationPath,
			Map<String, ViewChannel> channels, CommandScope commandScope, FormModel formModel,
			ErrorSink errorSink, DirtyChannel dirtyChannel) {
		_reactContext = reactContext;
		_personalizationPath = personalizationPath;
		_channels = channels;
		_commandScope = commandScope;
		_formModel = formModel;
		_errorSink = errorSink;
		_dirtyChannel = dirtyChannel;
	}
```

Update the root constructor (line 50-52):

```java
	public DefaultViewContext(ReactContext reactContext) {
		this(reactContext, "view", new HashMap<>(), null, null, null, null);
	}
```

Update `childContext()` (line 66-69):

```java
	@Override
	public ViewContext childContext(String segment) {
		return new DefaultViewContext(_reactContext, _personalizationPath + "." + segment, _channels, _commandScope,
			_formModel, _errorSink, _dirtyChannel);
	}
```

Update `withCommandScope()` (line 92-94):

```java
	@Override
	public ViewContext withCommandScope(CommandScope scope) {
		return new DefaultViewContext(_reactContext, _personalizationPath, _channels, scope, _formModel, _errorSink,
			_dirtyChannel);
	}
```

Update `withErrorSink()` (line 97-100):

```java
	@Override
	public ViewContext withErrorSink(ErrorSink errorSink) {
		return new DefaultViewContext(_reactContext, _personalizationPath, _channels,
			_commandScope, _formModel, errorSink, _dirtyChannel);
	}
```

Add the getter/setter:

```java
	@Override
	public DirtyChannel getDirtyChannel() {
		return _dirtyChannel;
	}

	@Override
	public void setDirtyChannel(DirtyChannel dirtyChannel) {
		_dirtyChannel = dirtyChannel;
	}
```

Add import:

```java
import com.top_logic.layout.view.channel.DirtyChannel;
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view -B &> $TMPDIR/mvn-ctx.log; tail -20 $TMPDIR/mvn-ctx.log`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java
git commit -m "Ticket #29108: Add DirtyChannel to ViewContext.

ViewContext und DefaultViewContext um DirtyChannel-Accessor erweitert."
```

---

## Task 7: FormControl implements StateHandler + Veto Registration

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java`

- [ ] **Step 1: Add StateHandler implementation to FormControl**

Add `StateHandler` to the `implements` clause (line 45):

```java
public class FormControl extends ReactControl implements FormModel, ModelListener, StateHandler {
```

Add import:

```java
import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
```

Add fields after the existing `_editModeListener` field (around line 83):

```java
	private VetoListener _inputVeto;

	private DirtyChannel _scopeDirtyChannel;
```

- [ ] **Step 2: Implement StateHandler methods**

Add before the existing `handleInputChanged` method:

```java
	// -- StateHandler --

	@Override
	public boolean isDirty() {
		return _editMode && _overlay != null && (_overlay.isDirty() || hasParticipantChanges());
	}

	@Override
	public boolean hasErrors() {
		return _validationModel != null && !_validationModel.isValid();
	}

	@Override
	public void executeDiscard() {
		executeCancel();
	}

	@Override
	public String getDescription() {
		if (_currentObject != null) {
			return String.valueOf(_currentObject);
		}
		return "Form";
	}

	/**
	 * Sets the scope-level {@link DirtyChannel} that this form publishes its dirty state to.
	 *
	 * @param dirtyChannel
	 *        The dirty channel of the enclosing scope (e.g. tab).
	 */
	public void setScopeDirtyChannel(DirtyChannel dirtyChannel) {
		_scopeDirtyChannel = dirtyChannel;
	}
```

- [ ] **Step 3: Register/deregister veto on input channel in enterEditMode/exitEditMode**

In `enterEditMode()` (after the existing channel/lock setup, before `fireFormStateChanged()`), add:

```java
		if (_inputChannel != null && _inputVeto == null) {
			_inputVeto = (sender, oldVal, newVal) -> isDirty() ? this : null;
			_inputChannel.addVetoListener(_inputVeto);
		}
```

In `exitEditMode()` (at the beginning, before clearing `_overlay`), add:

```java
		if (_inputVeto != null && _inputChannel != null) {
			_inputChannel.removeVetoListener(_inputVeto);
			_inputVeto = null;
		}
```

- [ ] **Step 4: Publish dirty state to scope DirtyChannel**

In the existing `updateDirtyState()` method (lines 479-485), add after the `_dirtyChannel` block:

```java
		if (_scopeDirtyChannel != null) {
			_scopeDirtyChannel.updateState(this, dirty);
		}
```

- [ ] **Step 5: Cleanup scope DirtyChannel on dispose**

In `onCleanup()` (around line 591), add before the input channel removal:

```java
		if (_scopeDirtyChannel != null) {
			_scopeDirtyChannel.removeHandler(this);
		}
```

- [ ] **Step 6: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view -B &> $TMPDIR/mvn-form.log; tail -20 $TMPDIR/mvn-form.log`

Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java
git commit -m "Ticket #29108: FormControl implements StateHandler with veto and DirtyChannel.

FormControl implementiert StateHandler, registriert VetoListener auf Input-Kanal bei Edit-Mode und publiziert Dirty-State auf scope-lokalen DirtyChannel."
```

---

## Task 8: FormElement Binds DirtyChannel

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java`

- [ ] **Step 1: Bind FormControl to scope DirtyChannel**

In `createControl()`, after the existing dirty channel binding (after line 326 — the `if (dirtyRef != null)` block), add:

```java
		// Bind to scope-level DirtyChannel if one is available (e.g. inside a tab).
		DirtyChannel scopeDirtyChannel = context.getDirtyChannel();
		if (scopeDirtyChannel != null) {
			formControl.setScopeDirtyChannel(scopeDirtyChannel);
		}
```

Add import:

```java
import com.top_logic.layout.view.channel.DirtyChannel;
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view -B &> $TMPDIR/mvn-formel.log; tail -20 $TMPDIR/mvn-formel.log`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java
git commit -m "Ticket #29108: FormElement binds FormControl to scope DirtyChannel.

FormElement bindet FormControl an den DirtyChannel des umgebenden Scopes (z.B. Tab)."
```

---

## Task 9: TabBarElement Creates Isolated DirtyChannel Per Tab

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TabBarElement.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/ReactTabBarControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/TabDefinition.java`

- [ ] **Step 1: Extend TabDefinition to carry DirtyChannel**

Add field, constructor parameter, and getter to `TabDefinition.java`:

```java
import com.top_logic.layout.view.channel.DirtyChannel;
```

Add field after `_contentFactory` (line 26):

```java
	private final DirtyChannel _dirtyChannel;
```

Replace constructor (lines 38-42):

```java
	public TabDefinition(String id, String label, Supplier<ReactControl> contentFactory,
			DirtyChannel dirtyChannel) {
		_id = id;
		_label = label;
		_contentFactory = contentFactory;
		_dirtyChannel = dirtyChannel;
	}
```

Add getter:

```java
	/**
	 * The dirty channel tracking unsaved changes in this tab's content, or {@code null}.
	 */
	public DirtyChannel getDirtyChannel() {
		return _dirtyChannel;
	}
```

- [ ] **Step 2: Update TabBarElement to create DirtyChannel per tab**

In `TabBarElement.java`, add import:

```java
import com.top_logic.layout.view.channel.DirtyChannel;
```

Replace the `createControl` method (lines 124-132):

```java
	@Override
	public IReactControl createControl(ViewContext context) {
		List<TabDefinition> tabDefs = new ArrayList<>();
		for (TabEntry entry : _tabs) {
			DirtyChannel dirtyChannel = new DirtyChannel();
			tabDefs.add(new TabDefinition(entry._id, entry._label,
				() -> createContent(entry._children, context, dirtyChannel), dirtyChannel));
		}
		String activeTab = _activeTab != null && !_activeTab.isEmpty() ? _activeTab : null;
		return new ReactTabBarControl(context, null, tabDefs, activeTab);
	}
```

Replace the `createContent` helper (lines 134-142):

```java
	private static ReactControl createContent(List<UIElement> elements, ViewContext context,
			DirtyChannel dirtyChannel) {
		ViewContext tabContext = context.childContext("tab");
		tabContext.setDirtyChannel(dirtyChannel);

		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(tabContext);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(tabContext))
			.collect(Collectors.toList());
		return new ReactStackControl(tabContext, children);
	}
```

- [ ] **Step 3: Update ReactTabBarControl to check DirtyChannel before tab switch**

In `ReactTabBarControl.java`, add imports:

```java
import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.form.StateHandler;
```

Replace `handleSelectTab` (lines 173-177):

```java
	@ReactCommand("selectTab")
	void handleSelectTab(Map<String, Object> arguments) {
		String tabId = (String) arguments.get(TAB_ID_ARG);

		// Check for dirty forms in the current tab before switching.
		TabDefinition currentTab = findTab(_activeTabId);
		DirtyChannel dirtyChannel = currentTab.getDirtyChannel();
		if (dirtyChannel != null && dirtyChannel.hasDirtyHandlers()) {
			throw new ChannelVetoException(dirtyChannel.getDirtyHandlers(), () -> selectTab(tabId));
		}

		selectTab(tabId);
	}
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view -B &> $TMPDIR/mvn-tab1.log; tail -20 $TMPDIR/mvn-tab1.log`

Then: `mvn compile -DskipTests=true -pl com.top_logic.layout.react -B &> $TMPDIR/mvn-tab2.log; tail -20 $TMPDIR/mvn-tab2.log`

Expected: BUILD SUCCESS on both.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/TabDefinition.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TabBarElement.java
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/ReactTabBarControl.java
git commit -m "Ticket #29108: Tab bar creates DirtyChannel per tab and checks before switching.

TabBarElement erstellt isolierten DirtyChannel pro Tab. ReactTabBarControl prueft vor Tab-Wechsel und wirft ChannelVetoException bei dirty Forms."
```

---

## Task 10: DirtyConfirmDialogControl (Server-Side)

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/DirtyConfirmDialogControl.java`

- [ ] **Step 1: Create the dialog control**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Content control for the dirty-check confirmation dialog.
 *
 * <p>
 * Renders a dialog asking the user whether to save, discard, or cancel when navigating away from
 * dirty forms. The "save" option is hidden when any handler has validation errors.
 * </p>
 *
 * <p>
 * State sent to React:
 * </p>
 * <ul>
 * <li>{@code descriptions} - list of handler descriptions (strings)</li>
 * <li>{@code canSave} - whether save is available (no validation errors)</li>
 * </ul>
 */
public class DirtyConfirmDialogControl extends ReactControl {

	private static final String REACT_MODULE = "TLDirtyConfirmDialog";

	private static final String DESCRIPTIONS = "descriptions";

	private static final String CAN_SAVE = "canSave";

	private final List<StateHandler> _dirtyHandlers;

	private final Runnable _continuation;

	private final DialogManager _dialogManager;

	/**
	 * Creates a new {@link DirtyConfirmDialogControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param dirtyHandlers
	 *        The dirty state handlers.
	 * @param continuation
	 *        The action to execute after save/discard.
	 * @param dialogManager
	 *        The dialog manager for closing this dialog.
	 */
	public DirtyConfirmDialogControl(ReactContext context, List<StateHandler> dirtyHandlers,
			Runnable continuation, DialogManager dialogManager) {
		super(context, null, REACT_MODULE);
		_dirtyHandlers = dirtyHandlers;
		_continuation = continuation;
		_dialogManager = dialogManager;

		List<String> descriptions = new ArrayList<>();
		boolean canSave = true;
		for (StateHandler handler : dirtyHandlers) {
			descriptions.add(handler.getDescription());
			if (handler.hasErrors()) {
				canSave = false;
			}
		}
		putState(DESCRIPTIONS, descriptions);
		putState(CAN_SAVE, Boolean.valueOf(canSave));
	}

	/**
	 * Saves all dirty handlers, closes the dialog, and continues.
	 */
	@ReactCommand("save")
	void handleSave() {
		for (StateHandler handler : _dirtyHandlers) {
			handler.executeSave();
		}
		_dialogManager.closeTopDialog(DialogResult.ok(null));
		_continuation.run();
	}

	/**
	 * Discards all dirty handlers, closes the dialog, and continues.
	 */
	@ReactCommand("discard")
	void handleDiscard() {
		for (StateHandler handler : _dirtyHandlers) {
			handler.executeDiscard();
		}
		_dialogManager.closeTopDialog(DialogResult.ok(null));
		_continuation.run();
	}

	/**
	 * Cancels the navigation, keeping dirty state intact.
	 */
	@ReactCommand("cancel")
	void handleCancel() {
		_dialogManager.closeTopDialog(DialogResult.cancelled());
	}
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.react -B &> $TMPDIR/mvn-dialog.log; tail -20 $TMPDIR/mvn-dialog.log`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/DirtyConfirmDialogControl.java
git commit -m "Ticket #29108: Add DirtyConfirmDialogControl for dirty-check confirmation.

Server-seitige Dialog-Kontrolle mit save/discard/cancel Commands."
```

---

## Task 11: TLDirtyConfirmDialog React Component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLDirtyConfirmDialog.tsx`

- [ ] **Step 1: Create the React component**

```tsx
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

import { React, useTLState, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Confirmation dialog for unsaved changes.
 *
 * Displays the list of dirty handlers and offers Save (if no errors), Discard, and Cancel buttons.
 */
const TLDirtyConfirmDialog: React.FC<TLCellProps> = () => {
	const state = useTLState();
	const sendCommand = useTLCommand();
	const i18n = useI18N([
		'com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_TITLE',
		'com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_MESSAGE',
		'com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_SAVE',
		'com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_DISCARD',
		'com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_CANCEL',
	]);

	const descriptions = (state.descriptions as string[]) ?? [];
	const canSave = state.canSave as boolean;

	const handleSave = React.useCallback(() => {
		sendCommand('save');
	}, [sendCommand]);

	const handleDiscard = React.useCallback(() => {
		sendCommand('discard');
	}, [sendCommand]);

	const handleCancel = React.useCallback(() => {
		sendCommand('cancel');
	}, [sendCommand]);

	const title = i18n['com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_TITLE']
		?? 'Unsaved Changes';
	const message = i18n['com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_MESSAGE']
		?? 'The following forms have unsaved changes:';
	const saveLabel = i18n['com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_SAVE']
		?? 'Save';
	const discardLabel = i18n['com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_DISCARD']
		?? 'Discard';
	const cancelLabel = i18n['com.top_logic.layout.react.control.overlay.I18NConstants.DIRTY_CONFIRM_CANCEL']
		?? 'Cancel';

	return (
		<div className="tlDirtyConfirmDialog">
			<h3 className="tlDirtyConfirmDialog__title">{title}</h3>
			<p className="tlDirtyConfirmDialog__message">{message}</p>
			{descriptions.length > 0 && (
				<ul className="tlDirtyConfirmDialog__list">
					{descriptions.map((desc, idx) => (
						<li key={idx}>{desc}</li>
					))}
				</ul>
			)}
			<div className="tlDirtyConfirmDialog__actions">
				{canSave && (
					<button className="tlButton tlButton--primary" onClick={handleSave}>
						{saveLabel}
					</button>
				)}
				<button className="tlButton tlButton--danger" onClick={handleDiscard}>
					{discardLabel}
				</button>
				<button className="tlButton" onClick={handleCancel}>
					{cancelLabel}
				</button>
			</div>
		</div>
	);
};

export default TLDirtyConfirmDialog;
```

- [ ] **Step 2: Register in module index**

Add the export to the React controls index file (the file that maps module names to components — check the existing pattern for how `TLDialog` is registered and follow it).

- [ ] **Step 3: Verify build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react -B &> $TMPDIR/mvn-tsx.log; tail -20 $TMPDIR/mvn-tsx.log`

Expected: BUILD SUCCESS (frontend-maven-plugin compiles the TypeScript).

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLDirtyConfirmDialog.tsx
git commit -m "Ticket #29108: Add TLDirtyConfirmDialog React component.

React-Komponente fuer den Dirty-Check-Bestaetigungsdialog mit Save/Discard/Cancel."
```

---

## Task 12: I18N Constants for Dialog

**Files:**
- Modify or create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/I18NConstants.java`

- [ ] **Step 1: Add I18N constants**

Check if the file exists. If not, create it. Add the following constants:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the overlay package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Unsaved Changes
	 */
	public static ResKey DIRTY_CONFIRM_TITLE;

	/**
	 * @en The following forms have unsaved changes:
	 */
	public static ResKey DIRTY_CONFIRM_MESSAGE;

	/**
	 * @en Save
	 */
	public static ResKey DIRTY_CONFIRM_SAVE;

	/**
	 * @en Discard
	 */
	public static ResKey DIRTY_CONFIRM_DISCARD;

	/**
	 * @en Cancel
	 */
	public static ResKey DIRTY_CONFIRM_CANCEL;

	static {
		initConstants(I18NConstants.class);
	}
}
```

- [ ] **Step 2: Run mvn install to generate messages files**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react -B &> $TMPDIR/mvn-i18n.log; tail -20 $TMPDIR/mvn-i18n.log`

Expected: BUILD SUCCESS and `messages_en.properties` regenerated.

- [ ] **Step 3: Add German translations**

Edit the generated `messages_de.properties` file in `src/main/java/META-INF/` to add German translations for the keys:

- `DIRTY_CONFIRM_TITLE` = `Ungespeicherte \u00C4nderungen`
- `DIRTY_CONFIRM_MESSAGE` = `Die folgenden Formulare haben ungespeicherte \u00C4nderungen:`
- `DIRTY_CONFIRM_SAVE` = `Speichern`
- `DIRTY_CONFIRM_DISCARD` = `Verwerfen`
- `DIRTY_CONFIRM_CANCEL` = `Abbrechen`

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/I18NConstants.java
git add com.top_logic.layout.react/src/main/java/META-INF/messages_en.properties
git add com.top_logic.layout.react/src/main/java/META-INF/messages_de.properties
git commit -m "Ticket #29108: Add I18N constants for dirty-check confirmation dialog.

I18N-Konstanten fuer den Dirty-Check-Dialog (DE/EN)."
```

---

## Task 13: ReactCommandInvoker Catches ChannelVetoException

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactCommandInvoker.java`

- [ ] **Step 1: Add veto handling to invoke()**

Add imports:

```java
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DirtyConfirmDialogControl;
import com.top_logic.layout.view.channel.ChannelVetoException;
```

In the `invoke()` method, add a specific catch for `ChannelVetoException` **before** the existing `catch (Throwable ex)` block (at line 80):

```java
	} catch (ChannelVetoException ex) {
		DialogManager dm = context.getDialogManager();
		if (dm != null) {
			DirtyConfirmDialogControl dialogContent =
				new DirtyConfirmDialogControl(context, ex.getDirtyHandlers(), ex.getContinuation(), dm);
			dm.openDialog(false, dialogContent, result -> {
				// Dialog closed — nothing to do here. Save/discard/cancel
				// are handled by the dialog's own commands.
			});
			return HandlerResult.DEFAULT_RESULT;
		}
		Logger.warn("No DialogManager available for dirty-check dialog.", ReactCommandInvoker.class);
		return HandlerResult.DEFAULT_RESULT;
	} catch (Throwable ex) {
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.react -B &> $TMPDIR/mvn-invoker.log; tail -20 $TMPDIR/mvn-invoker.log`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactCommandInvoker.java
git commit -m "Ticket #29108: ReactCommandInvoker catches ChannelVetoException and opens dialog.

ReactCommandInvoker faengt ChannelVetoException und oeffnet den Dirty-Check-Bestaetigungsdialog ueber DialogManager."
```

---

## Task 14: VetoListener on DerivedViewChannel

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedViewChannel.java`

- [ ] **Step 1: Add no-op VetoListener methods to DerivedViewChannel**

`DerivedViewChannel` is read-only (no `set()`), so veto listeners are not applicable. Add no-op implementations to satisfy the `ViewChannel` interface:

```java
	@Override
	public void addVetoListener(VetoListener listener) {
		// DerivedViewChannel is read-only; veto listeners are not applicable.
	}

	@Override
	public void removeVetoListener(VetoListener listener) {
		// DerivedViewChannel is read-only; veto listeners are not applicable.
	}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view -B &> $TMPDIR/mvn-derived.log; tail -20 $TMPDIR/mvn-derived.log`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedViewChannel.java
git commit -m "Ticket #29108: Add no-op VetoListener methods to DerivedViewChannel.

DerivedViewChannel ist read-only, VetoListener sind nicht anwendbar."
```

---

## Task 15: Integration Test — End-to-End Dirty Check

**Files:**
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDirtyCheckIntegration.java`

- [ ] **Step 1: Write integration test**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Integration tests for the dirty-check mechanism: veto on channels and DirtyChannel interaction.
 */
public class TestDirtyCheckIntegration extends TestCase {

	/**
	 * Simulates the selection-change scenario: a channel has a veto listener from a dirty form.
	 * Changing the channel throws, then after discard the retry succeeds.
	 */
	public void testSelectionChangeVetoThenDiscard() {
		DefaultViewChannel selectionChannel = new DefaultViewChannel("selection");
		selectionChannel.set("objectA");

		MutableStubHandler form = new MutableStubHandler("EditForm");
		form._dirty = true;

		selectionChannel.addVetoListener(
			(sender, oldVal, newVal) -> form.isDirty() ? form : null);

		// Attempt to change selection — should be vetoed.
		try {
			selectionChannel.set("objectB");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			List<StateHandler> handlers = ex.getDirtyHandlers();
			assertEquals(1, handlers.size());
			assertSame(form, handlers.get(0));

			// User chooses "discard".
			form.executeDiscard();
			assertFalse(form.isDirty());

			// Retry continuation.
			ex.getContinuation().run();
		}

		assertEquals("objectB", selectionChannel.get());
	}

	/**
	 * Simulates the tab-switch scenario: DirtyChannel tracks dirty forms in a tab.
	 */
	public void testTabSwitchWithDirtyChannel() {
		DirtyChannel tabDirty = new DirtyChannel();
		MutableStubHandler form1 = new MutableStubHandler("Form1");
		MutableStubHandler form2 = new MutableStubHandler("Form2");

		// Both forms start clean.
		assertFalse(tabDirty.hasDirtyHandlers());

		// Form1 becomes dirty.
		form1._dirty = true;
		tabDirty.updateState(form1, true);
		assertTrue(tabDirty.hasDirtyHandlers());
		assertEquals(1, tabDirty.getDirtyHandlers().size());

		// Form2 also becomes dirty.
		form2._dirty = true;
		tabDirty.updateState(form2, true);
		assertEquals(2, tabDirty.getDirtyHandlers().size());

		// Save form1 — still dirty because of form2.
		form1.executeSave();
		tabDirty.updateState(form1, false);
		assertTrue(tabDirty.hasDirtyHandlers());
		assertEquals(1, tabDirty.getDirtyHandlers().size());

		// Discard form2 — now clean.
		form2.executeDiscard();
		tabDirty.updateState(form2, false);
		assertFalse(tabDirty.hasDirtyHandlers());
	}

	/**
	 * Tests that canSave is false when any handler has errors.
	 */
	public void testCanSaveWithErrors() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		MutableStubHandler cleanForm = new MutableStubHandler("Clean");
		cleanForm._dirty = true;
		cleanForm._hasErrors = false;

		MutableStubHandler errorForm = new MutableStubHandler("WithErrors");
		errorForm._dirty = true;
		errorForm._hasErrors = true;

		channel.addVetoListener((sender, oldVal, newVal) -> cleanForm.isDirty() ? cleanForm : null);
		channel.addVetoListener((sender, oldVal, newVal) -> errorForm.isDirty() ? errorForm : null);

		try {
			channel.set("new");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			List<StateHandler> handlers = ex.getDirtyHandlers();
			assertEquals(2, handlers.size());

			// Check that at least one has errors (dialog should hide save button).
			boolean anyErrors = handlers.stream().anyMatch(StateHandler::hasErrors);
			assertTrue("Save should be disabled when any handler has errors", anyErrors);
		}
	}

	private static class MutableStubHandler implements StateHandler {

		boolean _dirty;

		boolean _hasErrors;

		private final String _description;

		MutableStubHandler(String description) {
			_description = description;
		}

		@Override
		public boolean isDirty() {
			return _dirty;
		}

		@Override
		public boolean hasErrors() {
			return _hasErrors;
		}

		@Override
		public void executeSave() {
			_dirty = false;
		}

		@Override
		public void executeDiscard() {
			_dirty = false;
		}

		@Override
		public String getDescription() {
			return _description;
		}
	}
}
```

- [ ] **Step 2: Run tests**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestDirtyCheckIntegration -B &> $TMPDIR/mvn-integ.log; tail -20 $TMPDIR/mvn-integ.log`

Expected: All 3 tests pass.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDirtyCheckIntegration.java
git commit -m "Ticket #29108: Add integration tests for dirty-check mechanism.

Integrationstests fuer Veto-Mechanismus (Selektionswechsel) und DirtyChannel (Tab-Wechsel)."
```

---

## Task 16: Full Build Verification

- [ ] **Step 1: Build com.top_logic.layout.view with tests**

Run: `mvn install -DskipTests=false -pl com.top_logic.layout.view -B &> $TMPDIR/mvn-full-view.log; grep -E 'Tests run|BUILD' $TMPDIR/mvn-full-view.log`

Expected: All tests pass, BUILD SUCCESS.

- [ ] **Step 2: Build com.top_logic.layout.react**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react -B &> $TMPDIR/mvn-full-react.log; grep -E 'BUILD' $TMPDIR/mvn-full-react.log`

Expected: BUILD SUCCESS (includes frontend build).

- [ ] **Step 3: Build com.top_logic.demo to verify no downstream breakage**

Run: `mvn compile -DskipTests=true -pl com.top_logic.demo -B &> $TMPDIR/mvn-demo.log; tail -20 $TMPDIR/mvn-demo.log`

Expected: BUILD SUCCESS.
