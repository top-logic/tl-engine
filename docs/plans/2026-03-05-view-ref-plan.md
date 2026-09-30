# `<view-ref>` Element Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a `<view-ref>` element that embeds another `.view.xml` file as an isolated component with explicit channel bindings.

**Architecture:** Extract view loading from `ViewServlet` into a shared `ViewLoader`. Add `ChannelBindingConfig` for `<bind>` elements. Create `ReferenceElement` that loads the referenced view lazily, creates an isolated `ViewContext`, pre-binds channels, and delegates to the referenced `ViewElement.createControl()`. Modify `ViewElement` to skip channels already pre-bound.

**Tech Stack:** Java 17, TypedConfiguration, JUnit 4, Maven

**Design doc:** `docs/plans/2026-03-05-view-ref-design.md`

---

### Task 1: Extract ViewLoader from ViewServlet

Extract the view loading and caching logic from `ViewServlet` into a standalone `ViewLoader` utility class so that both `ViewServlet` and the new `ReferenceElement` can load views.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewLoader.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`

**Step 1: Create ViewLoader with extracted logic**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Shared utility for loading and caching parsed {@link ViewElement} instances.
 *
 * <p>
 * View XML files are parsed via {@link TypedConfiguration} into a shared {@link ViewElement} tree
 * (Phase 1). The cache checks the source file's modification timestamp and re-parses when the file
 * has changed.
 * </p>
 */
public class ViewLoader {

	/** Base path for view XML files within the webapp. */
	public static final String VIEW_BASE_PATH = "/WEB-INF/views/";

	private static final ConcurrentHashMap<String, CachedView> CACHE = new ConcurrentHashMap<>();

	/**
	 * Retrieves a cached {@link ViewElement} or loads and caches it from the view XML file.
	 *
	 * @param viewPath
	 *        Full path to the view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @return The parsed {@link ViewElement}.
	 * @throws ConfigurationException
	 *         if the file cannot be found or parsed.
	 */
	public static ViewElement getOrLoadView(String viewPath) throws ConfigurationException {
		File file = FileManager.getInstance().getIDEFileOrNull(viewPath);
		long currentModified = file != null ? file.lastModified() : 0L;

		CachedView cached = CACHE.get(viewPath);
		if (cached != null && cached._lastModified == currentModified) {
			return cached._view;
		}

		ViewElement view = loadView(viewPath);
		CACHE.put(viewPath, new CachedView(view, currentModified));
		return view;
	}

	/**
	 * Parses a {@code .view.xml} file into a {@link ViewElement}.
	 *
	 * @param viewPath
	 *        Full path to the view file.
	 * @return The parsed {@link ViewElement}.
	 * @throws ConfigurationException
	 *         if the file cannot be found or parsed.
	 */
	public static ViewElement loadView(String viewPath) throws ConfigurationException {
		BinaryData source = FileManager.getInstance().getDataOrNull(viewPath);
		if (source == null) {
			throw new ConfigurationException("View file not found: " + viewPath);
		}

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(ViewLoader.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource((Content) source);

		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		UIElement uiElement = context.getInstance(config);
		if (!(uiElement instanceof ViewElement)) {
			throw new ConfigurationException(
				"Expected ViewElement but got: " + uiElement.getClass().getName());
		}
		return (ViewElement) uiElement;
	}

	private static final class CachedView {

		final ViewElement _view;

		final long _lastModified;

		CachedView(ViewElement view, long lastModified) {
			_view = view;
			_lastModified = lastModified;
		}
	}
}
```

**Step 2: Refactor ViewServlet to delegate to ViewLoader**

In `ViewServlet.java`, remove:
- The `VIEW_BASE_PATH` constant (use `ViewLoader.VIEW_BASE_PATH` instead)
- The `_viewCache` field
- The `getOrLoadView()` method
- The `loadView()` method
- The `CachedView` inner class

Change `resolveViewPath()` to use `ViewLoader.VIEW_BASE_PATH`.

Change `doGet()` to call `ViewLoader.getOrLoadView(viewPath)` instead of `getOrLoadView(viewPath)`.

The relevant section of `doGet()` becomes:

```java
ViewElement view;
try {
    view = ViewLoader.getOrLoadView(viewPath);
} catch (ConfigurationException ex) {
    // ... same error handling ...
}
```

**Step 3: Build to verify no regressions**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile`
Expected: BUILD SUCCESS

**Step 4: Commit**

```
Ticket #29108: Extract ViewLoader from ViewServlet for shared view loading.
```

---

### Task 2: Add `hasChannel()` to ViewContext

Add a method to check whether a channel is already registered, needed by ViewElement to skip pre-bound channels.

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelDeclaration.java`

**Step 1: Write the failing test**

Add to `TestChannelDeclaration.java`:

```java
/**
 * Tests that hasChannel reports registered channels correctly.
 */
public void testHasChannel() {
    ViewContext viewContext = new ViewContext(null);
    assertFalse("Should not have unregistered channel", viewContext.hasChannel("test"));

    viewContext.registerChannel("test", new DefaultViewChannel("test"));
    assertTrue("Should have registered channel", viewContext.hasChannel("test"));
    assertFalse("Should not have other channel", viewContext.hasChannel("other"));
}
```

**Step 2: Run test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestChannelDeclaration#testHasChannel`
Expected: Compilation error — `hasChannel` method does not exist.

**Step 3: Add hasChannel() to ViewContext**

Add to `ViewContext.java` after the `resolveChannel()` method:

```java
/**
 * Whether a channel with the given name is registered.
 *
 * @param name
 *        The channel name to check.
 * @return {@code true} if a channel with this name exists.
 */
public boolean hasChannel(String name) {
    return _channels.containsKey(name);
}
```

**Step 4: Run test to verify it passes**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestChannelDeclaration#testHasChannel`
Expected: PASS

**Step 5: Commit**

```
Ticket #29108: Add hasChannel() to ViewContext.
```

---

### Task 3: Modify ViewElement to skip pre-bound channels

Change `ViewElement.createControl()` to skip channel registration when a channel is already present in the context (pre-bound by a parent `<view-ref>`).

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelDeclaration.java`

**Step 1: Write the failing test**

Add to `TestChannelDeclaration.java`:

```java
/**
 * Tests that ViewElement.createControl() skips channels already registered in the context
 * (pre-bound by a parent view-ref).
 */
public void testPreBoundChannelNotOverwritten() throws Exception {
    // Parse the test-channels view (declares "selectedItem" and "editMode").
    DefaultInstantiationContext instContext = new DefaultInstantiationContext(TestChannelDeclaration.class);

    Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
        "view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

    BinaryContent source = new ClassRelativeBinaryContent(TestChannelDeclaration.class,
        "test-channels.view.xml");

    ConfigurationReader reader = new ConfigurationReader(instContext, descriptors);
    reader.setSource(source);
    ViewElement.Config config = (ViewElement.Config) reader.read();
    instContext.checkErrors();
    ViewElement viewElement = (ViewElement) instContext.getInstance(config);

    // Pre-bind "selectedItem" channel in the context.
    ViewContext viewContext = new ViewContext(null);
    ViewChannel preBound = new DefaultViewChannel("selectedItem");
    preBound.set("pre-bound-value");
    viewContext.registerChannel("selectedItem", preBound);

    // Create control — should skip "selectedItem", create "editMode".
    viewElement.createControl(viewContext);

    // The pre-bound channel must be preserved (same instance, same value).
    ViewChannel resolved = viewContext.resolveChannel(new ChannelRef("selectedItem"));
    assertSame("Pre-bound channel must not be replaced", preBound, resolved);
    assertEquals("Pre-bound value must be preserved", "pre-bound-value", resolved.get());

    // "editMode" should have been created fresh.
    ViewChannel editMode = viewContext.resolveChannel(new ChannelRef("editMode"));
    assertNotNull("editMode should be created", editMode);
    assertNotSame("editMode should be a different instance", preBound, editMode);
}
```

**Step 2: Run test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestChannelDeclaration#testPreBoundChannelNotOverwritten`
Expected: FAIL — `IllegalArgumentException: Duplicate channel name: 'selectedItem'`

**Step 3: Modify ViewElement.createControl()**

In `ViewElement.java`, change the channel registration loop in `createControl()`:

Replace:
```java
for (ChannelConfig channelConfig : _channelConfigs) {
    ViewChannel channel = new DefaultInstantiationContext(ViewElement.class).getInstance(channelConfig);
    context.registerChannel(channelConfig.getName(), channel);
}
```

With:
```java
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

**Step 4: Run test to verify it passes**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestChannelDeclaration#testPreBoundChannelNotOverwritten`
Expected: PASS

**Step 5: Run all existing tests to verify no regressions**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false`
Expected: All tests PASS

**Step 6: Commit**

```
Ticket #29108: Skip pre-bound channels in ViewElement.createControl().
```

---

### Task 4: Create ChannelBindingConfig

Add the configuration interface for `<bind>` elements used inside `<view-ref>`.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelBindingConfig.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestReferenceElement.java`
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/test-view-ref.view.xml`
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/child-view.view.xml`

**Step 1: Create ChannelBindingConfig**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Configuration for a channel binding between a parent view and a referenced child view.
 *
 * <p>
 * Used inside {@code <view-ref>} to map a channel from the parent's scope to a channel declared in
 * the referenced view. At runtime, the parent's channel instance is shared with the child — both
 * sides read and write the same object.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;view-ref view="detail.view.xml"&gt;
 *   &lt;bind channel="item" to="selectedCustomer"/&gt;
 * &lt;/view-ref&gt;
 * </pre>
 */
@TagName("bind")
public interface ChannelBindingConfig extends ConfigurationItem {

	/** Configuration name for {@link #getChannel()}. */
	String CHANNEL = "channel";

	/** Configuration name for {@link #getTo()}. */
	String TO = "to";

	/**
	 * Name of the channel in the referenced (child) view.
	 */
	@Name(CHANNEL)
	@Mandatory
	String getChannel();

	/**
	 * Reference to a channel in the parent scope.
	 */
	@Name(TO)
	@Mandatory
	@Format(ChannelRefFormat.class)
	ChannelRef getTo();
}
```

**Step 2: Create child-view.view.xml test resource**

This is the view that will be referenced by `<view-ref>` in tests. Place at `src/test/resources/test/com/top_logic/layout/view/child-view.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="item" />
    <channel name="localOnly" />
  </channels>

  <panel title="Child View" />
</view>
```

**Step 3: Build to verify ChannelBindingConfig compiles**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile`
Expected: BUILD SUCCESS

**Step 4: Commit**

```
Ticket #29108: Add ChannelBindingConfig for view-ref channel bindings.
```

---

### Task 5: Create ReferenceElement

The main feature: a `UIElement` that embeds another view as an isolated component.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestReferenceElement.java`
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/test-view-ref.view.xml`

**Step 1: Create test-view-ref.view.xml test resource**

Place at `src/test/resources/test/com/top_logic/layout/view/test-view-ref.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="parentChannel" />
  </channels>

  <view-ref view="child-view.view.xml">
    <bind channel="item" to="parentChannel" />
  </view-ref>
</view>
```

Note: This test resource references `child-view.view.xml` (created in Task 4). In the test, we load the child view programmatically (not via `ViewLoader`/`FileManager`), so the `view` attribute is used for config parsing verification. The actual `createControl()` test constructs the child `ViewElement` directly.

**Step 2: Write TestReferenceElement**

Create `src/test/java/test/com/top_logic/layout/view/TestReferenceElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

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
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ChannelRef;

/**
 * Tests for {@link ReferenceElement} — the {@code <view-ref>} element.
 */
public class TestReferenceElement extends TestCase {

	/**
	 * Tests that a view-ref element with bindings is parsed from XML.
	 */
	public void testParseViewRef() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestReferenceElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestReferenceElement.class,
			"test-view-ref.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		// The content should be a ReferenceElement.
		assertEquals("Should have one content element", 1, config.getContent().size());
		assertTrue("Content should be ReferenceElement config",
			config.getContent().get(0) instanceof ReferenceElement.Config);

		ReferenceElement.Config refConfig = (ReferenceElement.Config) config.getContent().get(0);
		assertEquals("View path", "child-view.view.xml", refConfig.getView());

		// Verify bindings.
		assertEquals("Should have one binding", 1, refConfig.getBindings().size());
		ChannelBindingConfig binding = refConfig.getBindings().get(0);
		assertEquals("Binding child channel", "item", binding.getChannel());
		assertEquals("Binding parent ref", new ChannelRef("parentChannel"), binding.getTo());

		// Verify instantiation.
		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement tree should be instantiated", element);
	}

	/**
	 * Tests that a view-ref without bindings is parsed.
	 */
	public void testParseViewRefNoBindings() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestReferenceElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestReferenceElement.class,
			"test-view-ref-no-bindings.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		ReferenceElement.Config refConfig = (ReferenceElement.Config) config.getContent().get(0);
		assertEquals("View path", "child-view.view.xml", refConfig.getView());
		assertTrue("Should have no bindings", refConfig.getBindings().isEmpty());
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReferenceElement.class, TypeIndex.Module.INSTANCE);
	}
}
```

**Step 3: Create test-view-ref-no-bindings.view.xml**

Place at `src/test/resources/test/com/top_logic/layout/view/test-view-ref-no-bindings.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <view-ref view="child-view.view.xml" />
</view>
```

**Step 4: Run tests to verify they fail (ReferenceElement not yet created)**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestReferenceElement`
Expected: Compilation error — `ReferenceElement` does not exist.

**Step 5: Create ReferenceElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * A {@link UIElement} that embeds another view as an isolated component.
 *
 * <p>
 * Each {@code <view-ref>} creates an independent instance of the referenced view with its own
 * {@link ViewContext} and channel namespace. Communication across the boundary is established via
 * explicit {@code <bind>} elements that share the parent's channel instance with the child.
 * </p>
 *
 * <p>
 * The referenced view is loaded lazily at {@link #createControl(ViewContext)} time, not at config
 * parse time. This naturally supports multiple instances and recursive usage.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;view-ref view="customers/detail.view.xml"&gt;
 *   &lt;bind channel="item" to="selectedCustomer"/&gt;
 * &lt;/view-ref&gt;
 * </pre>
 */
public class ReferenceElement implements UIElement {

	/**
	 * Configuration for {@link ReferenceElement}.
	 */
	@TagName("view-ref")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ReferenceElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #getBindings()}. */
		String BINDINGS = "bindings";

		/**
		 * Path to the referenced {@code .view.xml} file, relative to {@code /WEB-INF/views/}.
		 */
		@Name(VIEW)
		@Mandatory
		String getView();

		/**
		 * Channel bindings from the parent scope to the referenced view's channels.
		 *
		 * <p>
		 * Each binding maps a channel declared in the referenced view to a channel in the parent's
		 * scope. At runtime, the parent's channel instance is registered directly in the child's
		 * context — both sides share the same object.
		 * </p>
		 */
		@Name(BINDINGS)
		@DefaultContainer
		List<ChannelBindingConfig> getBindings();
	}

	private final String _viewPath;

	private final List<ChannelBindingConfig> _bindings;

	private final String _personalizationKey;

	/**
	 * Creates a new {@link ReferenceElement} from configuration.
	 */
	@CalledByReflection
	public ReferenceElement(InstantiationContext context, Config config) {
		_viewPath = config.getView();
		_bindings = config.getBindings();
		_personalizationKey = config.getPersonalizationKey();
	}

	@Override
	public ViewControl createControl(ViewContext parentContext) {
		String fullPath = ViewLoader.VIEW_BASE_PATH + _viewPath;

		ViewElement referencedView;
		try {
			referencedView = ViewLoader.getOrLoadView(fullPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load referenced view: " + fullPath, ex);
		}

		// Derive personalization segment.
		String segment;
		if (_personalizationKey != null && !_personalizationKey.isEmpty()) {
			segment = _personalizationKey;
		} else {
			// Strip .view.xml suffix and replace / with .
			segment = _viewPath;
			if (segment.endsWith(".view.xml")) {
				segment = segment.substring(0, segment.length() - ".view.xml".length());
			}
			segment = segment.replace('/', '.');
		}

		// Create isolated child context.
		ViewContext childContext = new ViewContext(parentContext.getDisplayContext());

		// Pre-bind parent channels into the child context.
		for (ChannelBindingConfig binding : _bindings) {
			ViewChannel parentChannel = parentContext.resolveChannel(binding.getTo());
			childContext.registerChannel(binding.getChannel(), parentChannel);
		}

		return referencedView.createControl(childContext);
	}
}
```

**Step 6: Run tests to verify they pass**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestReferenceElement`
Expected: PASS

**Step 7: Run all module tests**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false`
Expected: All tests PASS

**Step 8: Commit**

```
Ticket #29108: Add ReferenceElement for embedding views via <view-ref>.
```

---

### Task 6: Build the full module

Verify the complete module compiles and all tests pass.

**Step 1: Full build with tests**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn clean install -DskipTests=false`
Expected: BUILD SUCCESS, all tests PASS

---

### Summary of Changes

| File | Action | Description |
|------|--------|-------------|
| `ViewLoader.java` | Create | Shared view loading/caching extracted from ViewServlet |
| `ViewServlet.java` | Modify | Delegate to ViewLoader, remove private caching |
| `ViewContext.java` | Modify | Add `hasChannel(String)` method |
| `ViewElement.java` | Modify | Skip pre-bound channels in `createControl()` |
| `ChannelBindingConfig.java` | Create | `@TagName("bind")` config for channel bindings |
| `ReferenceElement.java` | Create | `@TagName("view-ref")` element for view embedding |
| `TestReferenceElement.java` | Create | Tests for parsing and config verification |
| `TestChannelDeclaration.java` | Modify | Add `testHasChannel` and `testPreBoundChannelNotOverwritten` |
| `child-view.view.xml` | Create | Test resource: child view with channels |
| `test-view-ref.view.xml` | Create | Test resource: parent with view-ref and bindings |
| `test-view-ref-no-bindings.view.xml` | Create | Test resource: view-ref without bindings |
