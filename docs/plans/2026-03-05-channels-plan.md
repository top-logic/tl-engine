# Value Channels Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add value channel infrastructure to the view system: ViewChannel interface,
DefaultViewChannel implementation, ChannelConfig declarations on ViewElement, ChannelRef
config type for element bindings, and channel registry on ViewContext.

**Architecture:** Channels are declared at the view level in `.view.xml` via a polymorphic
`ChannelConfig`. During `ViewElement.createControl()`, channel configs are instantiated
into `ViewChannel` objects and registered on the `ViewContext`. Child elements resolve
channels via `ChannelRef` (a config value type parsed from string attributes).

**Tech Stack:** Java 17, TypedConfiguration, existing view module (`tl-layout-view`).

---

### Task 1: ViewChannel Interface and DefaultViewChannel

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ViewChannel.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DefaultViewChannel.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/package-info.java`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestDefaultViewChannel.java`

**Step 1: Create the channel package-info**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Channel infrastructure for reactive data flow between view elements.
 *
 * @see com.top_logic.layout.view.channel.ViewChannel
 */
package com.top_logic.layout.view.channel;
```

**Step 2: Create the ViewChannel interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

/**
 * A named reactive value within a view.
 *
 * <p>
 * Channels hold a current value and notify listeners when it changes. UI elements bind to channels
 * to receive input and propagate output.
 * </p>
 *
 * <p>
 * Channels are per-session state, created during
 * {@link com.top_logic.layout.view.UIElement#createControl} and registered on the
 * {@link com.top_logic.layout.view.ViewContext}.
 * </p>
 */
public interface ViewChannel {

	/**
	 * The current value of this channel (may be {@code null}).
	 */
	Object get();

	/**
	 * Updates the value of this channel, notifying all listeners if the value changed.
	 *
	 * @param newValue
	 *        The new value (may be {@code null}).
	 * @return {@code true} if the value actually changed (was different from the previous value).
	 */
	boolean set(Object newValue);

	/**
	 * Adds a listener that is notified when this channel's value changes.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	void addListener(ChannelListener listener);

	/**
	 * Removes a previously added listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeListener(ChannelListener listener);

	/**
	 * Observer of a {@link ViewChannel}.
	 */
	interface ChannelListener {

		/**
		 * Called when the channel's value changes.
		 *
		 * @param sender
		 *        The channel whose value changed.
		 * @param oldValue
		 *        The previous value.
		 * @param newValue
		 *        The new value.
		 */
		void handleNewValue(ViewChannel sender, Object oldValue, Object newValue);
	}
}
```

**Step 3: Create DefaultViewChannel**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Default {@link ViewChannel} implementation that holds a mutable value.
 *
 * <p>
 * Thread-safe for listener management via {@link CopyOnWriteArrayList}. The {@link #set(Object)}
 * method uses {@link Objects#equals(Object, Object)} to detect changes and only fires listeners
 * when the value actually changes.
 * </p>
 */
public class DefaultViewChannel implements ViewChannel {

	private final String _name;

	private Object _value;

	private final CopyOnWriteArrayList<ChannelListener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link DefaultViewChannel} from configuration.
	 *
	 * @param context
	 *        The instantiation context.
	 * @param config
	 *        The channel configuration providing the channel name.
	 */
	@CalledByReflection
	public DefaultViewChannel(InstantiationContext context, ValueChannelConfig config) {
		_name = config.getName();
	}

	/**
	 * Creates a {@link DefaultViewChannel} programmatically.
	 *
	 * @param name
	 *        The channel name (for debugging).
	 */
	public DefaultViewChannel(String name) {
		_name = name;
	}

	@Override
	public Object get() {
		return _value;
	}

	@Override
	public boolean set(Object newValue) {
		Object oldValue = _value;
		if (Objects.equals(oldValue, newValue)) {
			return false;
		}
		_value = newValue;
		notifyListeners(oldValue, newValue);
		return true;
	}

	@Override
	public void addListener(ChannelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(ChannelListener listener) {
		_listeners.remove(listener);
	}

	private void notifyListeners(Object oldValue, Object newValue) {
		for (ChannelListener listener : _listeners) {
			listener.handleNewValue(this, oldValue, newValue);
		}
	}

	@Override
	public String toString() {
		return "ViewChannel[" + _name + "=" + _value + "]";
	}
}
```

Note: The constructor references `ValueChannelConfig` which is created in Task 2. To compile
this task standalone, temporarily comment out or stub the config constructor, OR implement
Task 2 first. The recommended approach is to implement Tasks 1 and 2 together before
compiling.

**Step 4: Write the test**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Tests for {@link DefaultViewChannel}.
 */
public class TestDefaultViewChannel extends TestCase {

	/**
	 * Tests that a new channel starts with a null value.
	 */
	public void testInitialValueIsNull() {
		ViewChannel channel = new DefaultViewChannel("test");
		assertNull(channel.get());
	}

	/**
	 * Tests basic get/set behavior.
	 */
	public void testSetAndGet() {
		ViewChannel channel = new DefaultViewChannel("test");

		assertTrue("First set should return true", channel.set("hello"));
		assertEquals("hello", channel.get());

		assertTrue("Different value should return true", channel.set("world"));
		assertEquals("world", channel.get());
	}

	/**
	 * Tests that setting the same value returns false and does not fire listeners.
	 */
	public void testSetSameValueReturnsFalse() {
		ViewChannel channel = new DefaultViewChannel("test");

		channel.set("value");
		assertFalse("Same value should return false", channel.set("value"));
	}

	/**
	 * Tests that setting null on a null channel returns false.
	 */
	public void testSetNullOnNullReturnsFalse() {
		ViewChannel channel = new DefaultViewChannel("test");
		assertFalse("Null to null should return false", channel.set(null));
	}

	/**
	 * Tests that listeners are notified on value change.
	 */
	public void testListenerNotified() {
		ViewChannel channel = new DefaultViewChannel("test");

		Object[] captured = new Object[3];
		ChannelListener listener = (sender, oldVal, newVal) -> {
			captured[0] = sender;
			captured[1] = oldVal;
			captured[2] = newVal;
		};

		channel.addListener(listener);
		channel.set("first");

		assertSame("Sender should be the channel", channel, captured[0]);
		assertNull("Old value should be null", captured[1]);
		assertEquals("New value should be 'first'", "first", captured[2]);
	}

	/**
	 * Tests that listeners are NOT notified when value does not change.
	 */
	public void testListenerNotNotifiedOnSameValue() {
		ViewChannel channel = new DefaultViewChannel("test");
		channel.set("value");

		int[] callCount = {0};
		channel.addListener((sender, oldVal, newVal) -> callCount[0]++);

		channel.set("value");
		assertEquals("Listener should not be called", 0, callCount[0]);
	}

	/**
	 * Tests that a removed listener is no longer notified.
	 */
	public void testRemoveListener() {
		ViewChannel channel = new DefaultViewChannel("test");

		int[] callCount = {0};
		ChannelListener listener = (sender, oldVal, newVal) -> callCount[0]++;

		channel.addListener(listener);
		channel.set("a");
		assertEquals(1, callCount[0]);

		channel.removeListener(listener);
		channel.set("b");
		assertEquals("Listener should not be called after removal", 1, callCount[0]);
	}
}
```

**Step 5: Compile and run tests**

Run: `mvn -f com.top_logic.layout.view/pom.xml test -DskipTests=false -Dtest=TestDefaultViewChannel`

Expected: All 6 tests pass.

**Step 6: Commit**

```
Ticket #29108: ViewChannel interface and DefaultViewChannel implementation.
```

---

### Task 2: ChannelConfig and ValueChannelConfig

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelConfig.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ValueChannelConfig.java`

**Step 1: Create ChannelConfig (polymorphic base)**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Base configuration for channel declarations in a view.
 *
 * <p>
 * This is polymorphic so that different channel kinds (value channels, derived channels) can
 * extend it. The implementation class determines the channel behavior at runtime.
 * </p>
 */
public interface ChannelConfig extends PolymorphicConfiguration<ViewChannel> {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/**
	 * The unique name of this channel within the view.
	 */
	@Name(NAME)
	@Mandatory
	String getName();
}
```

**Step 2: Create ValueChannelConfig**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Configuration for a mutable value channel.
 *
 * <p>
 * A value channel holds a single mutable value that can be read and written by UI elements.
 * </p>
 */
@TagName("channel")
public interface ValueChannelConfig extends ChannelConfig {

	@Override
	@ClassDefault(DefaultViewChannel.class)
	Class<? extends ViewChannel> getImplementationClass();
}
```

**Step 3: Compile**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS. The `DefaultViewChannel(InstantiationContext, ValueChannelConfig)`
constructor now resolves correctly.

**Step 4: Commit**

```
Ticket #29108: ChannelConfig and ValueChannelConfig for channel declarations.
```

---

### Task 3: ChannelRef and ChannelRefFormat

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelRef.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelRefFormat.java`

**Step 1: Create ChannelRef**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

/**
 * A reference to a {@link ViewChannel} used in configuration attributes.
 *
 * <p>
 * Currently holds just a channel name for local references within the same view. In the future,
 * this will be extended with a view path component for cross-view references
 * ({@code path/to/view.view.xml#channelName}).
 * </p>
 */
public class ChannelRef {

	private final String _channelName;

	/**
	 * Creates a {@link ChannelRef}.
	 *
	 * @param channelName
	 *        The name of the referenced channel.
	 */
	public ChannelRef(String channelName) {
		_channelName = channelName;
	}

	/**
	 * The name of the referenced channel.
	 */
	public String getChannelName() {
		return _channelName;
	}

	@Override
	public String toString() {
		return _channelName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ChannelRef)) return false;
		return _channelName.equals(((ChannelRef) obj)._channelName);
	}

	@Override
	public int hashCode() {
		return _channelName.hashCode();
	}
}
```

**Step 2: Create ChannelRefFormat**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link com.top_logic.basic.config.ConfigurationValueProvider} that parses a {@link ChannelRef}
 * from a string attribute value.
 *
 * <p>
 * Currently parses a plain channel name. In the future, will also parse cross-view references in
 * the format {@code path/to/view.view.xml#channelName}.
 * </p>
 *
 * <p>
 * Usage on a configuration property:
 * </p>
 *
 * <pre>
 * &#64;Format(ChannelRefFormat.class)
 * ChannelRef getSelection();
 * </pre>
 */
public class ChannelRefFormat extends AbstractConfigurationValueProvider<ChannelRef> {

	/** Singleton instance. */
	public static final ChannelRefFormat INSTANCE = new ChannelRefFormat();

	/**
	 * Creates a {@link ChannelRefFormat}.
	 */
	public ChannelRefFormat() {
		super(ChannelRef.class);
	}

	@Override
	protected ChannelRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		return new ChannelRef(propertyValue.toString().trim());
	}

	@Override
	protected String getSpecificationNonNull(ChannelRef configValue) {
		return configValue.getChannelName();
	}
}
```

**Step 3: Compile**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 4: Commit**

```
Ticket #29108: ChannelRef value type and ChannelRefFormat for config parsing.
```

---

### Task 4: ViewContext Channel Registry

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`

**Step 1: Add channel registry to ViewContext**

Add imports and fields. The modified file should become:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>
 * Provides session-scoped infrastructure needed to create and wire controls. Container elements may
 * create derived contexts that add scoped information for their children.
 * </p>
 */
public class ViewContext {

	private final ViewDisplayContext _displayContext;

	private final Map<String, ViewChannel> _channels = new HashMap<>();

	/**
	 * Creates a new {@link ViewContext}.
	 *
	 * @param displayContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public ViewContext(ViewDisplayContext displayContext) {
		_displayContext = displayContext;
	}

	/**
	 * The {@link ViewDisplayContext} for the current session.
	 */
	public ViewDisplayContext getDisplayContext() {
		return _displayContext;
	}

	/**
	 * Registers a channel in this context.
	 *
	 * <p>
	 * Called by {@link ViewElement} during {@link UIElement#createControl(ViewContext)} to populate
	 * the channel registry before child elements are created.
	 * </p>
	 *
	 * @param name
	 *        The channel name.
	 * @param channel
	 *        The channel instance.
	 * @throws IllegalArgumentException
	 *         if a channel with the given name is already registered.
	 */
	public void registerChannel(String name, ViewChannel channel) {
		ViewChannel existing = _channels.put(name, channel);
		if (existing != null) {
			_channels.put(name, existing);
			throw new IllegalArgumentException("Duplicate channel name: '" + name + "'");
		}
	}

	/**
	 * Resolves a {@link ChannelRef} to its runtime {@link ViewChannel}.
	 *
	 * @param ref
	 *        The channel reference to resolve.
	 * @return The channel instance.
	 * @throws IllegalArgumentException
	 *         if no channel with the referenced name exists.
	 */
	public ViewChannel resolveChannel(ChannelRef ref) {
		ViewChannel channel = _channels.get(ref.getChannelName());
		if (channel == null) {
			throw new IllegalArgumentException(
				"Unknown channel: '" + ref.getChannelName() + "'. "
					+ "Available channels: " + _channels.keySet());
		}
		return channel;
	}
}
```

**Step 2: Compile**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 3: Commit**

```
Ticket #29108: Channel registry on ViewContext.
```

---

### Task 5: Wire Channel Creation into ViewElement

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java`

**Step 1: Add channels property to ViewElement.Config and creation logic**

In `ViewElement.Config`, add the channels property. In the constructor, store the channel
configs. In `createControl()`, instantiate channels and register them before creating child
controls.

The `channels` property is a `List<ChannelConfig>`. Since `ChannelConfig` extends
`PolymorphicConfiguration<ViewChannel>`, and `ValueChannelConfig` has `@TagName("channel")`,
the `<channel>` tag will be resolved inside the `<channels>` list.

Modified `ViewElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * The mandatory root element of every {@code .view.xml} file.
 *
 * <p>
 * Establishes the scope boundary for a view including channel declarations.
 * </p>
 */
public class ViewElement implements UIElement {

	/**
	 * Configuration for {@link ViewElement}.
	 */
	@TagName("view")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ViewElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getChannels()}. */
		String CHANNELS = "channels";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * Channel declarations for this view.
		 *
		 * <p>
		 * Channels are named reactive values that can be read and written by UI elements within
		 * this view.
		 * </p>
		 */
		@Name(CHANNELS)
		List<ChannelConfig> getChannels();

		/**
		 * The root content element of this view.
		 *
		 * <p>
		 * Exactly one element is expected. The list type enables {@code @TagName} resolution so that
		 * short element names (e.g. {@code <app-shell>}) can be used directly inside {@code <view>}.
		 * </p>
		 */
		@Name(CONTENT)
		@DefaultContainer
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	private final List<ChannelConfig> _channelConfigs;

	private final List<UIElement> _content;

	/**
	 * Creates a new {@link ViewElement} from configuration.
	 */
	@CalledByReflection
	public ViewElement(InstantiationContext context, Config config) {
		_channelConfigs = config.getChannels();

		List<PolymorphicConfiguration<? extends UIElement>> contentList = config.getContent();
		if (contentList.isEmpty()) {
			context.error("View element must have at least one content element.");
			_content = List.of();
		} else {
			_content = contentList.stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
		}
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		// Phase 2a: Create and register channels.
		for (ChannelConfig channelConfig : _channelConfigs) {
			ViewChannel channel = new com.top_logic.basic.config.DefaultInstantiationContext(
				ViewElement.class).getInstance(channelConfig);
			context.registerChannel(channelConfig.getName(), channel);
		}

		// Phase 2b: Create child controls (children can now resolve channels).
		if (_content.size() == 1) {
			return _content.get(0).createControl(context);
		}
		List<ReactControl> children = _content.stream()
			.map(e -> (ReactControl) e.createControl(context))
			.collect(Collectors.toList());
		return new ReactStackControl(children);
	}
}
```

**Important note:** The channel instantiation uses a fresh `DefaultInstantiationContext`
because the original `InstantiationContext` from the constructor (Phase 1) is not available
in `createControl()` (Phase 2). This is correct: channels are per-session runtime objects,
not shared configuration objects. Each call to `createControl()` for a new session creates
fresh channel instances.

**Step 2: Compile**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 3: Commit**

```
Ticket #29108: Wire channel creation into ViewElement.
```

---

### Task 6: Test Channel Declaration Parsing and Registration

**Files:**
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/channel/test-channels.view.xml`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelDeclaration.java`

**Step 1: Create test view XML with channel declarations**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="selectedItem" />
    <channel name="editMode" />
  </channels>

  <stack direction="column" />
</view>
```

**Step 2: Write the integration test**

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

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Tests that channel declarations in {@code .view.xml} files are parsed and that channels are
 * registered on the {@link ViewContext} during control creation.
 */
public class TestChannelDeclaration extends TestCase {

	/**
	 * Tests that channel configs are parsed from the view XML.
	 */
	public void testParseChannelDeclarations() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestChannelDeclaration.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestChannelDeclaration.class,
			"test-channels.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertEquals("Should have 2 channel declarations", 2, config.getChannels().size());
		assertTrue("First channel should be ValueChannelConfig",
			config.getChannels().get(0) instanceof ValueChannelConfig);
		assertEquals("selectedItem", config.getChannels().get(0).getName());
		assertEquals("editMode", config.getChannels().get(1).getName());
	}

	/**
	 * Tests that resolving an unknown channel throws an exception.
	 */
	public void testResolveUnknownChannelFails() {
		ViewContext viewContext = new ViewContext(null);

		try {
			viewContext.resolveChannel(new ChannelRef("nonExistent"));
			fail("Should throw for unknown channel");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	/**
	 * Tests that duplicate channel registration throws an exception.
	 */
	public void testDuplicateChannelFails() {
		ViewContext viewContext = new ViewContext(null);
		viewContext.registerChannel("test", new com.top_logic.layout.view.channel.DefaultViewChannel("test"));

		try {
			viewContext.registerChannel("test",
				new com.top_logic.layout.view.channel.DefaultViewChannel("test"));
			fail("Should throw for duplicate channel");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	/**
	 * Tests basic channel registration and resolution on ViewContext.
	 */
	public void testRegisterAndResolve() {
		ViewContext viewContext = new ViewContext(null);
		ViewChannel channel = new com.top_logic.layout.view.channel.DefaultViewChannel("myChannel");

		viewContext.registerChannel("myChannel", channel);

		ViewChannel resolved = viewContext.resolveChannel(new ChannelRef("myChannel"));
		assertSame("Should resolve to the same channel instance", channel, resolved);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestChannelDeclaration.class, TypeIndex.Module.INSTANCE));
	}
}
```

**Step 3: Run tests**

Run: `mvn -f com.top_logic.layout.view/pom.xml test -DskipTests=false -Dtest=TestChannelDeclaration`

Expected: All 4 tests pass.

**Step 4: Commit**

```
Ticket #29108: Tests for channel declaration parsing and ViewContext registration.
```

---

### Task 7: Run All Tests and Final Verification

**Step 1: Run all tests in the module**

Run: `mvn -f com.top_logic.layout.view/pom.xml test -DskipTests=false`

Expected: All tests pass (TestViewElement + TestDefaultViewChannel + TestChannelDeclaration).

**Step 2: Full module build**

Run: `mvn -f com.top_logic.layout.view/pom.xml clean install`

Expected: BUILD SUCCESS.

**Step 3: Verify no regressions in dependent modules**

Run: `mvn -f com.top_logic.layout.react/pom.xml compile`

Expected: BUILD SUCCESS (no changes to react module).

**Step 4: Commit any adjustments**

```
Ticket #29108: Value channel infrastructure complete.
```

---

## Summary

| Task | What | Key Files |
|------|------|-----------|
| 1 | ViewChannel + DefaultViewChannel + tests | `ViewChannel.java`, `DefaultViewChannel.java`, `TestDefaultViewChannel.java` |
| 2 | ChannelConfig + ValueChannelConfig | `ChannelConfig.java`, `ValueChannelConfig.java` |
| 3 | ChannelRef + ChannelRefFormat | `ChannelRef.java`, `ChannelRefFormat.java` |
| 4 | ViewContext channel registry | `ViewContext.java` (modify) |
| 5 | ViewElement channel wiring | `ViewElement.java` (modify) |
| 6 | Integration test | `TestChannelDeclaration.java`, `test-channels.view.xml` |
| 7 | Final verification | Full build |

All new files go in `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/`.
