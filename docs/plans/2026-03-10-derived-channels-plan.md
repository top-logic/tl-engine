# Derived Channels Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add derived (computed, read-only) channels to the view system, with a ChannelFactory abstraction that separates config-time compilation from per-session channel wiring.

**Architecture:** `ChannelConfig` changes from `PolymorphicConfiguration<ViewChannel>` to `PolymorphicConfiguration<ChannelFactory>`. Factories are instantiated once at config parse time (where TL-Script expressions are compiled). `ChannelFactory.createChannel(ViewContext)` is called per-session. `DerivedViewChannel` is a per-session read-only channel that listens on input channels and re-evaluates on change. `DerivedViewChannel.bind()` accepts a `java.util.function.Function<Object[], Object>` (not `QueryExecutor` directly) to enable unit testing without services.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, TL-Script (`QueryExecutor`), JUnit 4

**Design doc:** `docs/plans/2026-03-10-derived-channels-design.md`

---

## File Map

All paths relative to `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/`.

**New files:**
| File | Responsibility |
|------|----------------|
| `channel/ChannelFactory.java` | Interface: config-time factory producing per-session `ViewChannel` |
| `channel/ValueChannelFactory.java` | Factory creating `DefaultViewChannel` instances |
| `channel/DerivedChannelConfig.java` | Config interface: `inputs` (comma-separated ChannelRefs) + `expr` (TL-Script) |
| `channel/DerivedChannelFactory.java` | Factory: compiles expression once, creates `DerivedViewChannel` per session |
| `channel/DerivedViewChannel.java` | Per-session read-only channel with reactive recomputation |
| `channel/CommaSeparatedChannelRefs.java` | Format: parses `"a,b,c"` attribute to `List<ChannelRef>` |

**Modified files:**
| File | Change |
|------|--------|
| `channel/ChannelConfig.java` | `PolymorphicConfiguration<ViewChannel>` -> `PolymorphicConfiguration<ChannelFactory>` |
| `channel/ValueChannelConfig.java` | `@ClassDefault(DefaultViewChannel.class)` -> `@ClassDefault(ValueChannelFactory.class)` |
| `channel/DefaultViewChannel.java` | Remove `(InstantiationContext, ValueChannelConfig)` constructor |
| `ViewElement.java` | Replace `DefaultInstantiationContext.getInstance(channelConfig)` with factory pattern |

**Test files** (under `src/test/java/test/com/top_logic/layout/view/channel/`):
| File | Responsibility |
|------|----------------|
| `TestDerivedViewChannel.java` | Unit tests for DerivedViewChannel behavior (no services needed) |
| `TestDerivedChannelConfig.java` | Config parsing + factory instantiation tests |
| `test-derived-channels.view.xml` | Test fixture: view XML with value + derived channels |

**Existing tests (no changes expected):**
| File | Note |
|------|------|
| `TestChannelDeclaration.java` | Should pass without modification: config parsing still produces `ValueChannelConfig` items; `ViewElement` constructor handles the `ChannelFactory` transition internally. |

---

## Chunk 1: ChannelFactory Refactoring

This chunk introduces the `ChannelFactory` abstraction and migrates existing value channels to use it. No new functionality yet -- the system should behave identically after this chunk.

### Task 1: Create ChannelFactory interface and ValueChannelFactory

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelFactory.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ValueChannelFactory.java`

- [ ] **Step 1: Create the ChannelFactory interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.layout.view.ViewContext;

/**
 * Config-time factory for creating per-session {@link ViewChannel} instances.
 *
 * <p>
 * Instantiated once via {@link com.top_logic.basic.config.PolymorphicConfiguration} at
 * configuration parse time. Expensive operations (e.g. expression compilation) happen in the
 * constructor. {@link #createChannel(ViewContext)} is called per-session to produce wired channel
 * instances.
 * </p>
 */
public interface ChannelFactory {

	/**
	 * Creates a wired, ready-to-use {@link ViewChannel} for this session.
	 *
	 * @param context
	 *        The view context for resolving channel references and other session-scoped state.
	 * @return A new channel instance.
	 */
	ViewChannel createChannel(ViewContext context);
}
```

- [ ] **Step 2: Create ValueChannelFactory**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link ChannelFactory} that creates mutable {@link DefaultViewChannel} instances.
 */
public class ValueChannelFactory implements ChannelFactory {

	private final String _name;

	/**
	 * Creates a {@link ValueChannelFactory} from configuration.
	 */
	@CalledByReflection
	public ValueChannelFactory(InstantiationContext context, ValueChannelConfig config) {
		_name = config.getName();
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		return new DefaultViewChannel(_name);
	}
}
```

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelFactory.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ValueChannelFactory.java
git commit -m "Ticket #29108: Add ChannelFactory interface and ValueChannelFactory."
```

### Task 2: Migrate ChannelConfig, ValueChannelConfig, DefaultViewChannel, and ViewElement atomically

All four files must change together -- `ChannelConfig` changes its type parameter, `ValueChannelConfig` changes its `@ClassDefault`, `DefaultViewChannel` loses its config constructor, and `ViewElement` switches to factory-based channel creation.

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelConfig.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ValueChannelConfig.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DefaultViewChannel.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java`

- [ ] **Step 1: Change ChannelConfig to extend PolymorphicConfiguration\<ChannelFactory\>**

In `ChannelConfig.java` line 20, change:
```java
public interface ChannelConfig extends PolymorphicConfiguration<ViewChannel> {
```
to:
```java
public interface ChannelConfig extends PolymorphicConfiguration<ChannelFactory> {
```

- [ ] **Step 2: Update ValueChannelConfig @ClassDefault**

In `ValueChannelConfig.java` lines 21-23, change:
```java
	@Override
	@ClassDefault(DefaultViewChannel.class)
	Class<? extends ViewChannel> getImplementationClass();
```
to:
```java
	@Override
	@ClassDefault(ValueChannelFactory.class)
	Class<? extends ChannelFactory> getImplementationClass();
```

- [ ] **Step 3: Remove the InstantiationContext constructor from DefaultViewChannel**

In `DefaultViewChannel.java`, remove lines 31-42 (the `(InstantiationContext, ValueChannelConfig)` constructor and its JavaDoc). Also remove the unused imports for `CalledByReflection` (line 11) and `InstantiationContext` (line 12).

The remaining constructor `DefaultViewChannel(String name)` is sufficient -- it's what `ValueChannelFactory` calls.

- [ ] **Step 4: Update ViewElement to use ChannelFactory**

Replace the `_channelConfigs` field (line 75) and its usage:

Old field (line 75):
```java
	private final List<ChannelConfig> _channelConfigs;
```

New field:
```java
	private final List<Map.Entry<String, ChannelFactory>> _channelEntries;
```

Old constructor line (line 84):
```java
		_channelConfigs = config.getChannels();
```

New constructor logic:
```java
		_channelEntries = config.getChannels().stream()
			.map(cc -> Map.entry(cc.getName(), context.getInstance(cc)))
			.collect(Collectors.toList());
```

Old createControl channel loop (lines 99-107):
```java
		// Phase 2a: Create and register channels.
		for (ChannelConfig channelConfig : _channelConfigs) {
			String name = channelConfig.getName();
			if (context.hasChannel(name)) {
				// Pre-bound by parent via <view-ref> binding. Skip local instantiation.
				continue;
			}
			ViewChannel channel = new DefaultInstantiationContext(ViewElement.class).getInstance(channelConfig);
			context.registerChannel(name, channel);
		}
```

New createControl channel loop:
```java
		// Phase 2a: Create and register channels via factories.
		for (Map.Entry<String, ChannelFactory> entry : _channelEntries) {
			String name = entry.getKey();
			if (context.hasChannel(name)) {
				// Pre-bound by parent via <view-ref> binding. Skip local instantiation.
				continue;
			}
			ChannelFactory factory = entry.getValue();
			context.registerChannel(name, factory.createChannel(context));
		}
```

Update imports: add `java.util.Map`, `com.top_logic.layout.view.channel.ChannelFactory`. Remove `com.top_logic.basic.config.DefaultInstantiationContext`, `com.top_logic.layout.view.channel.ChannelConfig`, and `com.top_logic.layout.view.channel.ViewChannel` (no longer directly referenced).

- [ ] **Step 5: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view`

Expected: BUILD SUCCESS

- [ ] **Step 6: Run existing tests to verify no regression**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view`

Expected: All tests pass. The refactoring is purely structural -- runtime behavior is identical. `TestChannelDeclaration` should pass without changes because:
- `testParseChannelDeclarations` checks `instanceof ValueChannelConfig` on config items (config parsing unchanged)
- `testPreBoundChannelNotOverwritten` parses the full view config and calls `instContext.getInstance(config)` on the `ViewElement.Config`, which constructs a `ViewElement` whose constructor now calls `context.getInstance(cc)` on channel configs, returning `ChannelFactory` -- all internal to `ViewElement`.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelConfig.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ValueChannelConfig.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DefaultViewChannel.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java
git commit -m "Ticket #29108: Migrate to ChannelFactory pattern for extensible channel creation."
```

---

## Chunk 2: Derived Channel Implementation

### Task 3: Write DerivedViewChannel test (TDD red phase)

**Files:**
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDerivedViewChannel.java`

The test uses plain lambdas as the evaluation function (not `QueryExecutor`), so no services are needed. This is a pure unit test extending `TestCase`.

- [ ] **Step 1: Write the unit test**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.List;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Tests for {@link DerivedViewChannel}.
 */
public class TestDerivedViewChannel extends TestCase {

	/**
	 * Tests that the derived channel computes its initial value eagerly.
	 */
	public void testInitialValueComputed() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("hello");

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0]);

		assertEquals("hello", derived.get());
	}

	/**
	 * Tests that the derived channel recomputes when an input changes.
	 */
	public void testRecomputesOnInputChange() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("initial");

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0]);

		assertEquals("initial", derived.get());

		input.set("updated");
		assertEquals("updated", derived.get());
	}

	/**
	 * Tests that set() throws IllegalStateException.
	 */
	public void testSetThrows() {
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(), args -> null);

		try {
			derived.set("value");
			fail("Expected IllegalStateException");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("derived"));
			assertTrue(expected.getMessage().contains("read-only"));
		}
	}

	/**
	 * Tests that listeners are notified when the derived value changes.
	 */
	public void testListenerNotifiedOnChange() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set(null);

		// Computes: input != null
		Function<Object[], Object> isNotNull = args -> args[0] != null;

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), isNotNull);

		assertEquals(Boolean.FALSE, derived.get());

		Object[] captured = new Object[3];
		ChannelListener listener = (sender, oldVal, newVal) -> {
			captured[0] = sender;
			captured[1] = oldVal;
			captured[2] = newVal;
		};
		derived.addListener(listener);

		input.set("something");

		assertSame(derived, captured[0]);
		assertEquals(Boolean.FALSE, captured[1]);
		assertEquals(Boolean.TRUE, captured[2]);
	}

	/**
	 * Tests that listeners are NOT notified when the recomputed value equals the current value.
	 */
	public void testNoNotificationOnEqualValue() {
		DefaultViewChannel input1 = new DefaultViewChannel("input1");
		DefaultViewChannel input2 = new DefaultViewChannel("input2");
		input1.set("a");
		input2.set("b");

		// Always returns constant "fixed" regardless of inputs.
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input1, input2), args -> "fixed");

		assertEquals("fixed", derived.get());

		int[] callCount = {0};
		derived.addListener((sender, oldVal, newVal) -> callCount[0]++);

		// Change input -- recomputed value is still "fixed".
		input1.set("changed");
		assertEquals(0, callCount[0]);
	}

	/**
	 * Tests multi-input derived channels.
	 */
	public void testMultiInput() {
		DefaultViewChannel input1 = new DefaultViewChannel("a");
		DefaultViewChannel input2 = new DefaultViewChannel("b");
		input1.set(null);
		input2.set(null);

		// Returns true if both inputs are non-null.
		Function<Object[], Object> bothNonNull = args -> args[0] != null && args[1] != null;

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input1, input2), bothNonNull);

		assertEquals(Boolean.FALSE, derived.get());

		input1.set("x");
		assertEquals(Boolean.FALSE, derived.get());

		input2.set("y");
		assertEquals(Boolean.TRUE, derived.get());
	}

	/**
	 * Tests that removeListener prevents further notifications.
	 */
	public void testRemoveListener() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set(null);

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0]);

		int[] callCount = {0};
		ChannelListener listener = (sender, oldVal, newVal) -> callCount[0]++;
		derived.addListener(listener);

		input.set("a");
		assertEquals(1, callCount[0]);

		derived.removeListener(listener);
		input.set("b");
		assertEquals("Listener should not be called after removal", 1, callCount[0]);
	}
}
```

- [ ] **Step 2: Verify the test does NOT compile yet (DerivedViewChannel doesn't exist)**

Run: `mvn test-compile -DskipTests=true -pl com.top_logic.layout.view`

Expected: COMPILATION ERROR -- `DerivedViewChannel` not found.

- [ ] **Step 3: Commit the failing test**

```bash
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDerivedViewChannel.java
git commit -m "Ticket #29108: Add DerivedViewChannel unit tests (red phase)."
```

### Task 4: Implement DerivedViewChannel (TDD green phase)

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedViewChannel.java`

The `bind()` method accepts `Function<Object[], Object>` instead of `QueryExecutor` directly. This keeps `DerivedViewChannel` decoupled from the TL-Script infrastructure and enables unit testing with plain lambdas. `DerivedChannelFactory` (Task 6) will wrap the `QueryExecutor` in a lambda.

- [ ] **Step 1: Create DerivedViewChannel**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * A read-only {@link ViewChannel} whose value is computed from other channels.
 *
 * <p>
 * The derived value is recomputed whenever any input channel changes. Listeners are only notified if
 * the recomputed value is different from the current value (using {@link Objects#equals}).
 * </p>
 *
 * <p>
 * This is a per-session object. The evaluation function is typically compiled once at configuration
 * time (e.g. from a TL-Script expression) and passed in via {@link #bind(List, Function)}.
 * </p>
 */
public class DerivedViewChannel implements ViewChannel {

	private final String _name;

	private Object _value;

	private final CopyOnWriteArrayList<ChannelListener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link DerivedViewChannel}.
	 *
	 * @param name
	 *        The channel name (for error messages and debugging).
	 */
	public DerivedViewChannel(String name) {
		_name = name;
	}

	/**
	 * Wires this channel to its input channels, computes the initial value, and attaches change
	 * listeners for automatic recomputation.
	 *
	 * @param inputs
	 *        The resolved input channels whose values become positional arguments to the function.
	 * @param evaluator
	 *        A function that takes an array of input values and returns the derived value.
	 */
	public void bind(List<ViewChannel> inputs, Function<Object[], Object> evaluator) {
		_value = evaluate(evaluator, inputs);

		ChannelListener refreshListener = (sender, oldVal, newVal) -> recompute(evaluator, inputs);
		for (ViewChannel input : inputs) {
			input.addListener(refreshListener);
		}
	}

	@Override
	public Object get() {
		return _value;
	}

	@Override
	public boolean set(Object newValue) {
		throw new IllegalStateException("Derived channel '" + _name + "' is read-only.");
	}

	@Override
	public void addListener(ChannelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(ChannelListener listener) {
		_listeners.remove(listener);
	}

	private void recompute(Function<Object[], Object> evaluator, List<ViewChannel> inputs) {
		Object newValue = evaluate(evaluator, inputs);
		Object oldValue = _value;
		if (!Objects.equals(oldValue, newValue)) {
			_value = newValue;
			for (ChannelListener listener : _listeners) {
				listener.handleNewValue(this, oldValue, newValue);
			}
		}
	}

	private static Object evaluate(Function<Object[], Object> evaluator, List<ViewChannel> inputs) {
		Object[] args = new Object[inputs.size()];
		for (int i = 0; i < args.length; i++) {
			args[i] = inputs.get(i).get();
		}
		return evaluator.apply(args);
	}

	@Override
	public String toString() {
		return "DerivedViewChannel[" + _name + "=" + _value + "]";
	}
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn test-compile -DskipTests=true -pl com.top_logic.layout.view`

Expected: BUILD SUCCESS

- [ ] **Step 3: Run the DerivedViewChannel tests**

Run: `mvn test -DskipTests=false -Dtest=TestDerivedViewChannel -pl com.top_logic.layout.view`

Expected: All 7 tests pass. No services required -- pure unit test with lambdas.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedViewChannel.java
git commit -m "Ticket #29108: Implement DerivedViewChannel."
```

### Task 5: Create CommaSeparatedChannelRefs format

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/CommaSeparatedChannelRefs.java`

- [ ] **Step 1: Create the format class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.CommaSeparatedListValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Parses a comma-separated list of channel references from a configuration attribute value.
 *
 * <p>
 * Example: {@code inputs="selectedCustomer,editMode"} parses to a list of two {@link ChannelRef}
 * instances.
 * </p>
 */
public class CommaSeparatedChannelRefs extends CommaSeparatedListValueProvider<ChannelRef> {

	/** Singleton instance. */
	public static final CommaSeparatedChannelRefs INSTANCE = new CommaSeparatedChannelRefs();

	@Override
	protected ChannelRef parseSingleValue(String propertyName, CharSequence propertyValue,
			String singlePropertyValue) throws ConfigurationException {
		return ChannelRefFormat.INSTANCE.getValueNonEmpty(propertyName, singlePropertyValue);
	}

	@Override
	protected String formatSingleValue(ChannelRef value) {
		return ChannelRefFormat.INSTANCE.getSpecificationNonNull(value);
	}
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/CommaSeparatedChannelRefs.java
git commit -m "Ticket #29108: Add CommaSeparatedChannelRefs format."
```

### Task 6: Create DerivedChannelConfig and DerivedChannelFactory

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedChannelConfig.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedChannelFactory.java`

- [ ] **Step 1: Create DerivedChannelConfig**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration for a derived (computed, read-only) channel.
 *
 * <p>
 * A derived channel computes its value from other channels using a TL-Script expression. The
 * {@link #getInputs() inputs} are channel references whose current values become positional
 * arguments to the {@link #getExpr() expression}.
 * </p>
 */
@TagName("derived-channel")
public interface DerivedChannelConfig extends ChannelConfig {

	@Override
	@ClassDefault(DerivedChannelFactory.class)
	Class<? extends ChannelFactory> getImplementationClass();

	/** Configuration name for {@link #getInputs()}. */
	String INPUTS = "inputs";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/**
	 * Comma-separated references to channels whose current values become positional arguments to the
	 * expression.
	 */
	@Name(INPUTS)
	@Format(CommaSeparatedChannelRefs.class)
	List<ChannelRef> getInputs();

	/**
	 * TL-Script expression computing the derived value.
	 *
	 * <p>
	 * The expression receives the input channel values as positional arguments. For example, with
	 * {@code inputs="a,b"} and {@code expr="x -> y -> $x + $y"}, the expression is called with the
	 * current values of channels {@code a} and {@code b}.
	 * </p>
	 */
	@Name(EXPR)
	@Mandatory
	@NonNullable
	Expr getExpr();
}
```

- [ ] **Step 2: Create DerivedChannelFactory**

Note: `DerivedChannelFactory` wraps `QueryExecutor.execute` in a `Function<Object[], Object>` when calling `DerivedViewChannel.bind()`.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ChannelFactory} for derived (computed, read-only) channels.
 *
 * <p>
 * Compiles the TL-Script expression once at configuration parse time. Per-session, resolves input
 * channel references and creates a {@link DerivedViewChannel} that recomputes on input changes.
 * </p>
 */
public class DerivedChannelFactory implements ChannelFactory {

	private final String _name;

	private final List<ChannelRef> _inputRefs;

	private final QueryExecutor _executor;

	/**
	 * Creates a {@link DerivedChannelFactory} from configuration.
	 *
	 * <p>
	 * The TL-Script expression is compiled here (config time) and shared across all sessions.
	 * </p>
	 */
	@CalledByReflection
	public DerivedChannelFactory(InstantiationContext context, DerivedChannelConfig config) {
		_name = config.getName();
		_inputRefs = config.getInputs();
		_executor = QueryExecutor.compile(config.getExpr());
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		DerivedViewChannel channel = new DerivedViewChannel(_name);
		List<ViewChannel> inputs = _inputRefs.stream()
			.map(context::resolveChannel)
			.toList();
		channel.bind(inputs, _executor::execute);
		return channel;
	}
}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedChannelConfig.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedChannelFactory.java
git commit -m "Ticket #29108: Add DerivedChannelConfig and DerivedChannelFactory."
```

---

## Chunk 3: Integration Tests

### Task 7: Write config parsing and integration tests for derived channels

**Files:**
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/channel/test-derived-channels.view.xml`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDerivedChannelConfig.java`

- [ ] **Step 1: Create test view XML fixture**

File: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/channel/test-derived-channels.view.xml`

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="selectedItem" />
    <derived-channel name="hasSelection"
        inputs="selectedItem"
        expr="item -> $item != null" />
  </channels>

  <stack direction="column" />
</view>
```

- [ ] **Step 2: Create the config parsing test**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DerivedChannelConfig;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Tests configuration parsing and integration of derived channels.
 */
public class TestDerivedChannelConfig extends TestCase {

	/**
	 * Tests that a {@code <derived-channel>} element is parsed as {@link DerivedChannelConfig}.
	 */
	public void testParseDerivedChannelConfig() throws Exception {
		ViewElement.Config config = parseTestView();

		assertEquals("Should have 2 channel declarations", 2, config.getChannels().size());

		ChannelConfig first = config.getChannels().get(0);
		assertTrue("First should be ValueChannelConfig", first instanceof ValueChannelConfig);
		assertEquals("selectedItem", first.getName());

		ChannelConfig second = config.getChannels().get(1);
		assertTrue("Second should be DerivedChannelConfig", second instanceof DerivedChannelConfig);
		assertEquals("hasSelection", second.getName());

		DerivedChannelConfig derived = (DerivedChannelConfig) second;
		assertEquals("Should have 1 input", 1, derived.getInputs().size());
		assertEquals("selectedItem", derived.getInputs().get(0).getChannelName());
		assertNotNull("Expression should be parsed", derived.getExpr());
	}

	/**
	 * Tests that derived channels are created and wired during createControl().
	 */
	public void testDerivedChannelCreatedDuringCreateControl() throws Exception {
		ViewElement.Config config = parseTestView();

		DefaultInstantiationContext instContext =
			new DefaultInstantiationContext(TestDerivedChannelConfig.class);
		ViewElement viewElement = (ViewElement) instContext.getInstance(config);
		instContext.checkErrors();

		ViewContext viewContext = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue()));

		viewElement.createControl(viewContext);

		// Value channel should exist.
		assertTrue(viewContext.hasChannel("selectedItem"));
		// Derived channel should exist.
		assertTrue(viewContext.hasChannel("hasSelection"));

		// Derived channel should have initial value (selectedItem is null -> false).
		ViewChannel hasSelection = viewContext.resolveChannel(new ChannelRef("hasSelection"));
		assertEquals(Boolean.FALSE, hasSelection.get());

		// Set the value channel and check derived updates.
		ViewChannel selectedItem = viewContext.resolveChannel(new ChannelRef("selectedItem"));
		selectedItem.set("something");
		assertEquals(Boolean.TRUE, hasSelection.get());
	}

	/**
	 * Tests that set() on a derived channel throws.
	 */
	public void testDerivedChannelIsReadOnly() throws Exception {
		ViewElement.Config config = parseTestView();

		DefaultInstantiationContext instContext =
			new DefaultInstantiationContext(TestDerivedChannelConfig.class);
		ViewElement viewElement = (ViewElement) instContext.getInstance(config);
		instContext.checkErrors();

		ViewContext viewContext = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue()));
		viewElement.createControl(viewContext);

		ViewChannel hasSelection = viewContext.resolveChannel(new ChannelRef("hasSelection"));
		try {
			hasSelection.set(true);
			fail("Expected IllegalStateException for read-only derived channel");
		} catch (IllegalStateException expected) {
			// Expected.
		}
	}

	private ViewElement.Config parseTestView() throws Exception {
		DefaultInstantiationContext context =
			new DefaultInstantiationContext(TestDerivedChannelConfig.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestDerivedChannelConfig.class,
			"test-derived-channels.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestDerivedChannelConfig.class, TypeIndex.Module.INSTANCE);
	}
}
```

- [ ] **Step 3: Run the tests**

Run: `mvn test -DskipTests=false -Dtest=TestDerivedChannelConfig -pl com.top_logic.layout.view`

Expected: All 3 tests pass. If `QueryExecutor.compile` (called inside `DerivedChannelFactory` during `ViewElement` construction) needs additional services like `PersistencyLayer`, add them to the `suite()` setup. The `DeferredQueryExecutor` from `QueryExecutor.compile(Expr)` lazily compiles on first `execute()`, which happens in `createControl()`, so services must be available at test execution time if the expression requires model resolution.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/channel/test-derived-channels.view.xml
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDerivedChannelConfig.java
git commit -m "Ticket #29108: Add derived channel config parsing and integration tests."
```

### Task 8: Run full test suite and verify broader compilation

- [ ] **Step 1: Run all tests in the view module**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view`

Expected: All tests pass -- both new and existing.

- [ ] **Step 2: Fix any failures and commit**

If tests needed fixes:
```bash
git add -u
git commit -m "Ticket #29108: Fix test issues in derived channel integration."
```

- [ ] **Step 3: Build the module for install**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`

Expected: BUILD SUCCESS. This ensures the module installs cleanly and downstream modules that depend on it won't break.
