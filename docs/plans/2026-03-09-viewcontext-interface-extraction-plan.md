# ViewContext Interface Extraction Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Extract a `ViewContext` interface from the current concrete class, creating `DefaultViewContext` as the default implementation, to decouple UIElement implementations from the concrete class.

**Architecture:** Convert `ViewContext.java` from a class to an interface (keeping the same name and file). Move all implementation code into a new `DefaultViewContext` class. Update the 3 call sites that construct `ViewContext` directly: `ViewServlet`, `ReferenceElement`, and test code.

**Tech Stack:** Java 17, JUnit 4, Maven

---

### Task 1: Create DefaultViewContext implementation class

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java`

**Step 1: Create DefaultViewContext with all current implementation code**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.react.ReactDisplayContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FormControl;

/**
 * Default implementation of {@link ViewContext}.
 *
 * <p>
 * Provides the standard hierarchical context for UIElement control creation. Container elements may
 * create derived contexts that add scoped information for their children via
 * {@link #childContext(String)} and {@link #withCommandScope(CommandScope)}.
 * </p>
 */
public class DefaultViewContext implements ViewContext {

	private final ReactDisplayContext _displayContext;

	private final String _personalizationPath;

	private final Map<String, ViewChannel> _channels;

	private final CommandScope _commandScope;

	private FormControl _formControl;

	/**
	 * Creates a root {@link DefaultViewContext}.
	 *
	 * @param displayContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public DefaultViewContext(ReactDisplayContext displayContext) {
		this(displayContext, "view", new HashMap<>(), null, null);
	}

	private DefaultViewContext(ReactDisplayContext displayContext, String personalizationPath,
			Map<String, ViewChannel> channels, CommandScope commandScope, FormControl formControl) {
		_displayContext = displayContext;
		_personalizationPath = personalizationPath;
		_channels = channels;
		_commandScope = commandScope;
		_formControl = formControl;
	}

	@Override
	public ViewContext childContext(String segment) {
		return new DefaultViewContext(_displayContext, _personalizationPath + "." + segment, _channels, _commandScope,
			_formControl);
	}

	@Override
	public String getPersonalizationKey() {
		return _personalizationPath;
	}

	@Override
	public ReactDisplayContext getDisplayContext() {
		return _displayContext;
	}

	@Override
	public CommandScope getCommandScope() {
		return _commandScope;
	}

	@Override
	public FormControl getFormControl() {
		return _formControl;
	}

	@Override
	public void setFormControl(FormControl formControl) {
		_formControl = formControl;
	}

	@Override
	public ViewContext withCommandScope(CommandScope scope) {
		return new DefaultViewContext(_displayContext, _personalizationPath, _channels, scope, _formControl);
	}

	@Override
	public void registerChannel(String name, ViewChannel channel) {
		ViewChannel existing = _channels.put(name, channel);
		if (existing != null) {
			_channels.put(name, existing);
			throw new IllegalArgumentException("Duplicate channel name: '" + name + "'");
		}
	}

	@Override
	public boolean hasChannel(String name) {
		return _channels.containsKey(name);
	}

	@Override
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

**Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java
git commit -m "Ticket #29108: Add DefaultViewContext implementation class."
```

### Task 2: Convert ViewContext from class to interface

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`

**Step 1: Replace ViewContext class body with interface declaration**

Replace the entire file content with:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.layout.react.ReactDisplayContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FormControl;

/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>
 * Provides session-scoped infrastructure needed to create and wire controls. Container elements may
 * create derived contexts that add scoped information for their children.
 * </p>
 *
 * @see DefaultViewContext
 */
public interface ViewContext {

	/**
	 * Creates a child context with an appended path segment.
	 *
	 * <p>
	 * The child context shares the same channel registry as the parent.
	 * </p>
	 *
	 * @param segment
	 *        The segment to append (e.g. "sidebar", "split-panel").
	 * @return A new context with the extended personalization path.
	 */
	ViewContext childContext(String segment);

	/**
	 * The personalization key for the current position in the view tree.
	 *
	 * <p>
	 * Auto-derived from the tree path (e.g. "view.sidebar", "view.split-panel"). UIElements may
	 * override this with an explicit personalization key from configuration.
	 * </p>
	 */
	String getPersonalizationKey();

	/**
	 * The {@link ReactDisplayContext} for the current session.
	 */
	ReactDisplayContext getDisplayContext();

	/**
	 * The {@link CommandScope} of the nearest enclosing panel, or {@code null} if no panel is in
	 * scope.
	 */
	CommandScope getCommandScope();

	/**
	 * The {@link FormControl} of the nearest enclosing form element, or {@code null} if no form is
	 * in scope.
	 */
	FormControl getFormControl();

	/**
	 * Sets the form control for this context.
	 *
	 * <p>
	 * Called by {@link com.top_logic.layout.view.element.FormElement} during
	 * {@link UIElement#createControl(ViewContext)} to make the form available to nested field
	 * elements.
	 * </p>
	 *
	 * @param formControl
	 *        The form control, or {@code null}.
	 */
	void setFormControl(FormControl formControl);

	/**
	 * Creates a derived context with the given {@link CommandScope}.
	 *
	 * <p>
	 * Called by panel elements to establish their command scope for child elements.
	 * </p>
	 *
	 * @param scope
	 *        The command scope to set.
	 * @return A new context with the given command scope but same display context, personalization
	 *         path, and channels.
	 */
	ViewContext withCommandScope(CommandScope scope);

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
	void registerChannel(String name, ViewChannel channel);

	/**
	 * Whether a channel with the given name is registered.
	 *
	 * @param name
	 *        The channel name to check.
	 * @return {@code true} if a channel with this name exists.
	 */
	boolean hasChannel(String name);

	/**
	 * Resolves a {@link ChannelRef} to its runtime {@link ViewChannel}.
	 *
	 * @param ref
	 *        The channel reference to resolve.
	 * @return The channel instance.
	 * @throws IllegalArgumentException
	 *         if no channel with the referenced name exists.
	 */
	ViewChannel resolveChannel(ChannelRef ref);
}
```

**Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java
git commit -m "Ticket #29108: Convert ViewContext from class to interface."
```

### Task 3: Update constructor call sites

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java:88`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java:110`
- Modify: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelDeclaration.java:64,78,93,106,135`

**Step 1: Update ViewServlet**

In `ViewServlet.java`, change line 88 from:
```java
		ViewContext viewContext = new ViewContext(displayContext);
```
to:
```java
		ViewContext viewContext = new DefaultViewContext(displayContext);
```

Add import:
```java
import com.top_logic.layout.view.DefaultViewContext;
```

Note: The `import com.top_logic.layout.view.ViewContext;` import is not needed in ViewServlet since ViewContext is in the same package.

**Step 2: Update ReferenceElement**

In `ReferenceElement.java`, change line 110 from:
```java
		ViewContext childContext = new ViewContext(parentContext.getDisplayContext());
```
to:
```java
		ViewContext childContext = new DefaultViewContext(parentContext.getDisplayContext());
```

No import needed — `DefaultViewContext` is in the same package.

**Step 3: Update TestChannelDeclaration**

In `TestChannelDeclaration.java`, change all 5 occurrences of `new ViewContext(null)` to `new DefaultViewContext(null)`.

Add import:
```java
import com.top_logic.layout.view.DefaultViewContext;
```

The `ViewContext` import stays because it's still used as the variable type.

**Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelDeclaration.java
git commit -m "Ticket #29108: Update call sites to use DefaultViewContext constructor."
```

### Task 4: Build and verify

**Step 1: Build the module**

Run: `cd com.top_logic.layout.view && mvn clean install -DskipTests=true`
Expected: BUILD SUCCESS

**Step 2: Run tests**

Run: `cd com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestChannelDeclaration`
Expected: All 5 tests pass

**Step 3: Commit if any fixes needed, otherwise done**
