# View Command System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement the ViewCommand system — a complete command infrastructure for the view layer that replaces the legacy LayoutComponent-dependent CommandHandler.

**Architecture:** Stateless command pattern. ViewCommand is a stateless handler instantiated from config. ViewCommandModel bridges commands to reactive UI state. Commands receive a single `Object input` resolved from channels. Executability rules, confirmation, and dirty checking are evaluated before execution.

**Tech Stack:** Java 17, TypedConfiguration, ReactControl/SSE, existing ExecutableState/HandlerResult

**Design doc:** `docs/plans/2026-03-05-commands-design.md`

---

### Task 1: ViewCommand Interface and Config

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ViewCommand.java`

**Step 1: Create the ViewCommand interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A command in the view system.
 *
 * <p>
 * Replaces the legacy {@code CommandHandler} which depends on {@code LayoutComponent}. A
 * {@link ViewCommand} is a stateless handler instantiated once from configuration. All data flows
 * through channels, not through untyped argument maps.
 * </p>
 */
public interface ViewCommand {

	/**
	 * Configuration for a {@link ViewCommand}.
	 */
	interface Config extends PolymorphicConfiguration<ViewCommand> {

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getCssClasses()}. */
		String CSS_CLASSES = "css-classes";

		/** Configuration name for {@link #getPlacement()}. */
		String PLACEMENT = "placement";

		/** Configuration name for {@link #getClique()}. */
		String CLIQUE = "clique";

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getExecutability()}. */
		String EXECUTABILITY = "executability";

		/** Configuration name for {@link #getConfirmation()}. */
		String CONFIRMATION = "confirmation";

		/** Configuration name for {@link #getCheckDirty()}. */
		String CHECK_DIRTY = "check-dirty";

		/** Configuration name for {@link #getGroup()}. */
		String GROUP = "group";

		/**
		 * Command name for referencing from {@code <button>} or {@code <command-ref>}.
		 *
		 * <p>
		 * Optional. Only needed when the command must be referenced by name from a
		 * {@code <command-ref>} in a button or popup menu.
		 * </p>
		 */
		@Name(NAME)
		@Nullable
		String getName();

		/**
		 * Internationalized label (button text, menu entry text).
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Icon displayed on the command button.
		 */
		@Name(IMAGE)
		@Nullable
		ThemeImage getImage();

		/**
		 * CSS classes for the command button.
		 */
		@Name(CSS_CLASSES)
		@Nullable
		String getCssClasses();

		/**
		 * Where this command's button appears in the enclosing panel.
		 *
		 * <p>
		 * Default defined by the command implementation.
		 * </p>
		 */
		@Name(PLACEMENT)
		CommandPlacement getPlacement();

		/**
		 * Which clique this command belongs to (ordering and visual grouping).
		 *
		 * <p>
		 * Standard cliques: "create", "edit", "delete", "commit", "navigate", "view", "export",
		 * "more". Custom cliques can be defined per panel.
		 * </p>
		 */
		@Name(CLIQUE)
		@Nullable
		String getClique();

		/**
		 * Channel reference providing the input value this command operates on.
		 *
		 * <p>
		 * Replaces the legacy {@code target} property. Null means the command operates without input
		 * (e.g., a "create new" command).
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Rules determining if the command is executable (enabled/disabled/hidden).
		 */
		@Name(EXECUTABILITY)
		@EntryTag("rule")
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getExecutability();

		/**
		 * Confirmation dialog shown before command execution. Null means no confirmation.
		 */
		@Name(CONFIRMATION)
		@Nullable
		PolymorphicConfiguration<? extends ViewCommandConfirmation> getConfirmation();

		/**
		 * Scope of dirty checking before command execution.
		 */
		@Name(CHECK_DIRTY)
		DirtyCheckScope getCheckDirty();

		/**
		 * Security command group for access control.
		 */
		@Name(GROUP)
		@Nullable
		CommandGroupReference getGroup();
	}

	/**
	 * Execute the command.
	 *
	 * @param context
	 *        The display context for SSE updates and HTTP access.
	 * @param input
	 *        The resolved input value (from the configured input channel), or null if no input is
	 *        configured.
	 * @return The command result.
	 */
	HandlerResult execute(ViewDisplayContext context, Object input);
}
```

**Step 2: Build to verify compilation**

Run: `mvn compile -pl com.top_logic.layout.view -am`
Expected: Will fail because `CommandPlacement`, `DirtyCheckScope`, `ViewExecutabilityRule`, and `ViewCommandConfirmation` don't exist yet. That's expected — they're created in the next tasks.

**Step 3: Commit**

```
Ticket #29108: Add ViewCommand interface and Config.
```

---

### Task 2: CommandPlacement and DirtyCheckScope Enums

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CommandPlacement.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/DirtyCheckScope.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CommandCliques.java`

**Step 1: Create CommandPlacement enum**

```java
package com.top_logic.layout.view.command;

/**
 * Where a command's button appears in the enclosing panel.
 */
public enum CommandPlacement {

	/** Toolbar above panel content (default for most action commands). */
	TOOLBAR,

	/** Button bar below panel content (default for commit/cancel commands). */
	BUTTON_BAR,

	/** Context menu on data elements (right-click rows). */
	CONTEXT_MENU,

	/** Not displayed automatically; only rendered where an explicit button places it. */
	NONE;
}
```

**Step 2: Create DirtyCheckScope enum**

```java
package com.top_logic.layout.view.command;

/**
 * Scope of dirty checking before command execution.
 */
public enum DirtyCheckScope {

	/** Check forms within the enclosing panel (default). */
	SELF,

	/** Check all forms in the entire view. */
	VIEW,

	/** Skip dirty checking (e.g., for save/cancel commands). */
	NONE;
}
```

**Step 3: Create CommandCliques constants**

```java
package com.top_logic.layout.view.command;

/**
 * Standard clique names for command grouping and ordering.
 */
public final class CommandCliques {

	/** New, import, duplicate. */
	public static final String CREATE = "create";

	/** Edit, lock, unlock. */
	public static final String EDIT = "edit";

	/** Delete, remove. */
	public static final String DELETE = "delete";

	/** Save, apply, cancel. */
	public static final String COMMIT = "commit";

	/** Open, goto, back. */
	public static final String NAVIGATE = "navigate";

	/** Search, reset filters, column config. */
	public static final String VIEW = "view";

	/** Excel, PDF, print. */
	public static final String EXPORT = "export";

	/** Settings, help, about. */
	public static final String MORE = "more";

	private CommandCliques() {
		// Constants only.
	}
}
```

**Step 4: Commit**

```
Ticket #29108: Add CommandPlacement, DirtyCheckScope, and CommandCliques.
```

---

### Task 3: ViewExecutabilityRule Interface and Standard Implementations

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ViewExecutabilityRule.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/NullInputDisabled.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/NullInputHidden.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CombinedViewExecutabilityRule.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestViewExecutabilityRules.java`

**Step 1: Create ViewExecutabilityRule interface**

```java
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Determines whether a command is executable.
 *
 * <p>
 * Replaces the legacy {@code ExecutabilityRule} which received a {@code LayoutComponent}. The new
 * interface receives the resolved command input value directly.
 * </p>
 */
public interface ViewExecutabilityRule {

	/**
	 * Configuration for a {@link ViewExecutabilityRule}.
	 */
	interface Config extends PolymorphicConfiguration<ViewExecutabilityRule> {
		// Rules can have custom configuration properties.
	}

	/**
	 * Determine if the command is executable given the current input.
	 *
	 * @param input
	 *        The resolved input value (same as what the command receives). May be null.
	 * @return The executability state.
	 */
	ExecutableState isExecutable(Object input);
}
```

**Step 2: Create NullInputDisabled**

```java
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Disables the command if the input is null.
 */
public class NullInputDisabled implements ViewExecutabilityRule {

	/** Singleton instance for programmatic use. */
	public static final NullInputDisabled INSTANCE = new NullInputDisabled();

	/**
	 * Configuration for {@link NullInputDisabled}.
	 */
	@TagName("NullInputDisabled")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(NullInputDisabled.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/**
	 * Creates a new {@link NullInputDisabled} from configuration.
	 */
	@CalledByReflection
	public NullInputDisabled(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	private NullInputDisabled() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		if (input == null) {
			return ExecutableState.NO_EXEC_NO_MODEL;
		}
		return ExecutableState.EXECUTABLE;
	}
}
```

**Step 3: Create NullInputHidden** (same pattern, returns `NOT_EXEC_HIDDEN`)

**Step 4: Create CombinedViewExecutabilityRule**

```java
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.tool.execution.ExecutableState;

/**
 * Combines multiple {@link ViewExecutabilityRule}s with short-circuit AND semantics.
 */
public class CombinedViewExecutabilityRule implements ViewExecutabilityRule {

	private final List<ViewExecutabilityRule> _rules;

	/**
	 * Creates a combined rule.
	 */
	public CombinedViewExecutabilityRule(List<ViewExecutabilityRule> rules) {
		_rules = rules;
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		for (ViewExecutabilityRule rule : _rules) {
			ExecutableState state = rule.isExecutable(input);
			if (!state.isExecutable()) {
				return state;
			}
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * Combines an intrinsic rule with configured rules.
	 */
	public static ViewExecutabilityRule combine(ViewExecutabilityRule intrinsic,
			List<ViewExecutabilityRule> configured) {
		if (intrinsic == null && configured.isEmpty()) {
			return input -> ExecutableState.EXECUTABLE;
		}
		if (intrinsic == null && configured.size() == 1) {
			return configured.get(0);
		}
		List<ViewExecutabilityRule> all = new java.util.ArrayList<>();
		if (intrinsic != null) {
			all.add(intrinsic);
		}
		all.addAll(configured);
		return new CombinedViewExecutabilityRule(all);
	}
}
```

**Step 5: Write test**

```java
package test.com.top_logic.layout.view.command;

import junit.framework.TestCase;

import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.NullInputHidden;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests for {@link ViewExecutabilityRule} implementations.
 */
public class TestViewExecutabilityRules extends TestCase {

	public void testNullInputDisabled() {
		NullInputDisabled rule = NullInputDisabled.INSTANCE;
		assertEquals(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));
		assertEquals(ExecutableState.EXECUTABLE, rule.isExecutable("someObject"));
	}

	public void testNullInputHidden() {
		NullInputHidden rule = NullInputHidden.INSTANCE;
		assertEquals(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));
		assertEquals(ExecutableState.EXECUTABLE, rule.isExecutable("someObject"));
	}

	public void testCombinedShortCircuit() {
		CombinedViewExecutabilityRule combined = new CombinedViewExecutabilityRule(
			java.util.List.of(NullInputDisabled.INSTANCE, NullInputHidden.INSTANCE));
		// First rule (disabled) short-circuits before second rule (hidden)
		assertEquals(ExecutableState.NO_EXEC_NO_MODEL, combined.isExecutable(null));
		assertEquals(ExecutableState.EXECUTABLE, combined.isExecutable("someObject"));
	}
}
```

**Step 6: Run tests**

Run: `mvn test -pl com.top_logic.layout.view -am -DskipTests=false -Dtest=TestViewExecutabilityRules`
Expected: PASS

**Step 7: Commit**

```
Ticket #29108: Add ViewExecutabilityRule interface with NullInputDisabled, NullInputHidden, and CombinedViewExecutabilityRule.
```

---

### Task 4: ViewCommandConfirmation Interface and Standard Implementations

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ViewCommandConfirmation.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/DefaultViewConfirmation.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/DefaultDeleteConfirmation.java`

**Step 1: Create ViewCommandConfirmation interface**

```java
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;

/**
 * Computes a confirmation message before command execution.
 */
public interface ViewCommandConfirmation {

	/**
	 * Configuration for a {@link ViewCommandConfirmation}.
	 */
	interface Config extends PolymorphicConfiguration<ViewCommandConfirmation> {
		// Implementations can add custom configuration.
	}

	/**
	 * Compute the confirmation message.
	 *
	 * @param commandLabel
	 *        The command's label.
	 * @param input
	 *        The resolved input value (may be null).
	 * @return The confirmation message, or null to skip confirmation.
	 */
	ResKey getConfirmation(ResKey commandLabel, Object input);
}
```

**Step 2: Create DefaultViewConfirmation**

Uses an I18N key like "Do you really want to execute '{0}'?". The I18N constant will be defined in the module's `I18NConstants`.

**Step 3: Create DefaultDeleteConfirmation**

Handles single vs. collection: "Do you really want to delete '{0}'?"

**Step 4: Build to verify compilation**

Run: `mvn compile -pl com.top_logic.layout.view -am`
Expected: PASS (all referenced types now exist)

**Step 5: Commit**

```
Ticket #29108: Add ViewCommandConfirmation interface with default implementations.
```

---

### Task 5: ViewCommandModel (Runtime Bridge)

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ViewCommandModel.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestViewCommandModel.java`

**Step 1: Create ViewCommandModel**

This is the runtime bridge between a stateless `ViewCommand` and its UI button.

```java
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Runtime bridge between a stateless {@link ViewCommand} and its UI button.
 *
 * <p>
 * Created by the panel at setup time, one per command. Subscribes to the input channel and
 * re-evaluates executability when the input changes.
 * </p>
 */
public class ViewCommandModel implements ViewChannel.ChannelListener {

	private final ViewCommand _command;

	private final ViewCommand.Config _config;

	private final ViewChannel _inputChannel;

	private final ViewExecutabilityRule _rule;

	private final ViewCommandConfirmation _confirmation;

	private ExecutableState _executableState;

	private Runnable _stateChangeListener;

	/**
	 * Creates a new {@link ViewCommandModel}.
	 */
	public ViewCommandModel(ViewCommand command, ViewCommand.Config config, ViewChannel inputChannel,
			ViewExecutabilityRule rule, ViewCommandConfirmation confirmation) {
		_command = command;
		_config = config;
		_inputChannel = inputChannel;
		_rule = rule;
		_confirmation = confirmation;
		_executableState = ExecutableState.EXECUTABLE;
	}

	/**
	 * Resolves the current input value from the channel.
	 */
	public Object resolveInput() {
		return _inputChannel != null ? _inputChannel.get() : null;
	}

	/**
	 * The command's label.
	 */
	public ResKey getLabel() {
		return _config.getLabel();
	}

	/**
	 * The command's image.
	 */
	public ThemeImage getImage() {
		return _config.getImage();
	}

	/**
	 * The command's CSS classes.
	 */
	public String getCssClasses() {
		return _config.getCssClasses();
	}

	/**
	 * The command's placement.
	 */
	public CommandPlacement getPlacement() {
		return _config.getPlacement();
	}

	/**
	 * The command's clique.
	 */
	public String getClique() {
		return _config.getClique();
	}

	/**
	 * The command's name (may be null).
	 */
	public String getName() {
		return _config.getName();
	}

	/**
	 * The current executability state.
	 */
	public ExecutableState getExecutableState() {
		return _executableState;
	}

	/**
	 * Called when user clicks the button.
	 */
	public HandlerResult executeCommand(ViewDisplayContext context) {
		Object input = resolveInput();

		ExecutableState state = _rule.isExecutable(input);
		if (!state.isExecutable()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// TODO: dirty check (DirtyCheckScope from config)

		if (_confirmation != null) {
			ResKey confirmKey = _confirmation.getConfirmation(_config.getLabel(), input);
			if (confirmKey != null) {
				// TODO: show confirmation dialog, resume on OK
				return HandlerResult.DEFAULT_RESULT;
			}
		}

		return _command.execute(context, input);
	}

	/**
	 * Subscribe to input channel changes and update executability state.
	 */
	public void attach() {
		if (_inputChannel != null) {
			_inputChannel.addListener(this);
		}
		updateExecutableState();
	}

	/**
	 * Unsubscribe from input channel changes.
	 */
	public void detach() {
		if (_inputChannel != null) {
			_inputChannel.removeListener(this);
		}
	}

	/**
	 * Set a listener that is notified when the executability state changes.
	 */
	public void setStateChangeListener(Runnable listener) {
		_stateChangeListener = listener;
	}

	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		updateExecutableState();
	}

	private void updateExecutableState() {
		Object input = resolveInput();
		ExecutableState newState = _rule.isExecutable(input);
		if (!newState.equals(_executableState)) {
			_executableState = newState;
			if (_stateChangeListener != null) {
				_stateChangeListener.run();
			}
		}
	}
}
```

**Step 2: Write test**

```java
package test.com.top_logic.layout.view.command;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests for {@link ViewCommandModel}.
 */
public class TestViewCommandModel extends TestCase {

	public void testReactiveExecutability() {
		ViewChannel channel = new DefaultViewChannel("test");

		// Command that returns HandlerResult.DEFAULT_RESULT
		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE, null);
		model.attach();

		// Initially null input -> disabled
		assertFalse("Should be disabled with null input",
			model.getExecutableState().isExecutable());

		// Set value -> enabled
		channel.set("someValue");
		assertTrue("Should be enabled with non-null input",
			model.getExecutableState().isExecutable());

		// Set back to null -> disabled
		channel.set(null);
		assertFalse("Should be disabled again",
			model.getExecutableState().isExecutable());
	}
}
```

Note: `createMinimalConfig()` will need a helper to programmatically create a minimal `ViewCommand.Config`. This can use `TypedConfiguration.newConfigItem(ViewCommand.Config.class)`.

**Step 3: Run test**

Run: `mvn test -pl com.top_logic.layout.view -am -DskipTests=false -Dtest=TestViewCommandModel`
Expected: PASS

**Step 4: Commit**

```
Ticket #29108: Add ViewCommandModel as runtime bridge between ViewCommand and UI.
```

---

### Task 6: CommandScope for Panel Command Management

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CommandScope.java`

**Step 1: Create CommandScope**

```java
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Runtime container for commands owned by a panel or dialog.
 *
 * <p>
 * Holds both explicit commands (from {@code <commands>}) and implicit commands (contributed by child
 * elements like tables). Supports reactive add/remove for lazy children.
 * </p>
 */
public class CommandScope {

	private final List<ViewCommandModel> _explicitCommands;

	private final List<ViewCommandModel> _implicitCommands;

	private final List<Runnable> _listeners;

	/**
	 * Creates a new {@link CommandScope} with the given explicit commands.
	 */
	public CommandScope(List<ViewCommandModel> explicitCommands) {
		_explicitCommands = new ArrayList<>(explicitCommands);
		_implicitCommands = new CopyOnWriteArrayList<>();
		_listeners = new CopyOnWriteArrayList<>();
	}

	/**
	 * Add an implicit command (called by child elements).
	 */
	public void addCommand(ViewCommandModel command) {
		_implicitCommands.add(command);
		fireChanged();
	}

	/**
	 * Remove an implicit command (called when child is destroyed).
	 */
	public void removeCommand(ViewCommandModel command) {
		_implicitCommands.remove(command);
		fireChanged();
	}

	/**
	 * All commands (explicit first, then implicit).
	 */
	public List<ViewCommandModel> getAllCommands() {
		List<ViewCommandModel> all = new ArrayList<>(_explicitCommands.size() + _implicitCommands.size());
		all.addAll(_explicitCommands);
		all.addAll(_implicitCommands);
		return Collections.unmodifiableList(all);
	}

	/**
	 * Resolves a command by name.
	 *
	 * @return The command model, or null if not found.
	 */
	public ViewCommandModel resolveCommand(String name) {
		for (ViewCommandModel model : _explicitCommands) {
			if (name.equals(model.getName())) {
				return model;
			}
		}
		for (ViewCommandModel model : _implicitCommands) {
			if (name.equals(model.getName())) {
				return model;
			}
		}
		return null;
	}

	/**
	 * Listen for changes to the command list (for toolbar re-rendering).
	 */
	public void addListener(Runnable listener) {
		_listeners.add(listener);
	}

	/**
	 * Remove a listener.
	 */
	public void removeListener(Runnable listener) {
		_listeners.remove(listener);
	}

	private void fireChanged() {
		for (Runnable listener : _listeners) {
			listener.run();
		}
	}
}
```

**Step 2: Commit**

```
Ticket #29108: Add CommandScope for managing panel command collections.
```

---

### Task 7: Extend ViewContext with CommandScope

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`

**Step 1: Add CommandScope field and accessor methods**

Add to `ViewContext`:
- `private CommandScope _commandScope` field (nullable, null when no enclosing panel)
- `getCommandScope()` method
- `withCommandScope(CommandScope)` method that creates a derived context with the scope set
- The child context inherits the command scope from its parent

**Step 2: Build to verify**

Run: `mvn compile -pl com.top_logic.layout.view -am`
Expected: PASS

**Step 3: Commit**

```
Ticket #29108: Extend ViewContext with CommandScope for command resolution.
```

---

### Task 8: Update ReactButtonControl for Disabled State

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactButtonControl.java`

**Step 1: Add disabled state support**

The React `TLButton` component already reads `state.disabled`. Add server-side support:

```java
// Add to ReactButtonControl:
private static final String DISABLED = "disabled";

public void setDisabled(boolean disabled) {
    putState(DISABLED, disabled);
}
```

Also add a constructor overload that takes a `ViewCommandModel` and wires the click action to `model.executeCommand()`, and adds a state change listener to update disabled state:

```java
public ReactButtonControl(ViewCommandModel model) {
    super(null, "TLButton", COMMANDS);
    _action = context -> model.executeCommand(
        com.top_logic.layout.react.ViewDisplayContext.fromDisplayContext(context));
    ResKey label = model.getLabel();
    if (label != null) {
        putState(LABEL, com.top_logic.basic.util.ResourcesModule.getInstance()
            .getBundle(java.util.Locale.getDefault()).getString(label));
    }
    putState(DISABLED, !model.getExecutableState().isExecutable());
    model.setStateChangeListener(() -> {
        putState(DISABLED, !model.getExecutableState().isExecutable());
    });
}
```

**Step 2: Build to verify**

Run: `mvn compile -pl com.top_logic.layout.react -am`
Expected: PASS

**Step 3: Commit**

```
Ticket #29108: Add disabled state support to ReactButtonControl with ViewCommandModel integration.
```

---

### Task 9: Update ButtonElement with Inline Command Support

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ButtonElement.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestButtonCommand.java`
- Test resource: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/command/test-button-command.view.xml`

**Step 1: Redesign ButtonElement.Config**

Replace the current simple `label` property with a `PolymorphicConfiguration<ViewCommand>` action property:

```java
@TagName("button")
public interface Config extends UIElement.Config {

    @Override
    @ClassDefault(ButtonElement.class)
    Class<? extends UIElement> getImplementationClass();

    /** Configuration name for {@link #getAction()}. */
    String ACTION = "action";

    /** Configuration name for {@link #getStyle()}. */
    String STYLE = "style";

    /** Configuration name for {@link #getSize()}. */
    String SIZE = "size";

    /**
     * The command this button executes.
     *
     * <p>
     * Either an inline command definition or a {@code <command-ref>} pointing to a named panel
     * command.
     * </p>
     */
    @Name(ACTION)
    @DefaultContainer
    PolymorphicConfiguration<? extends ViewCommand> getAction();

    /**
     * Button display style.
     */
    @Name(STYLE)
    ButtonStyle getStyle();

    /**
     * Button size.
     */
    @Name(SIZE)
    ButtonSize getSize();
}
```

**Step 2: Create ButtonStyle and ButtonSize enums**

```java
public enum ButtonStyle { DEFAULT, PRIMARY, ICON, LINK }
public enum ButtonSize { SMALL, DEFAULT, LARGE }
```

**Step 3: Update ButtonElement.createControl()**

```java
@Override
public ViewControl createControl(ViewContext context) {
    ViewCommand.Config cmdConfig = _command.getConfig();

    // Resolve input channel
    ChannelRef inputRef = cmdConfig.getInput();
    ViewChannel inputChannel = inputRef != null ? context.resolveChannel(inputRef) : null;

    // Build executability rule
    // ... instantiate rules from config ...

    // Create model and control
    ViewCommandModel model = new ViewCommandModel(_command, cmdConfig, inputChannel, rule, confirmation);
    model.attach();

    return new ReactButtonControl(model);
}
```

**Step 4: Write test XML**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <channels>
        <channel name="selectedItem"/>
    </channels>
    <button>
        <command class="test.com.top_logic.layout.view.command.TestCommand"
                 input="selectedItem" label="Test Button"/>
    </button>
</view>
```

**Step 5: Write test**

Test that the button element can parse an inline command from XML and create a control.

**Step 6: Run tests**

Run: `mvn test -pl com.top_logic.layout.view -am -DskipTests=false -Dtest=TestButtonCommand`
Expected: PASS

**Step 7: Commit**

```
Ticket #29108: Redesign ButtonElement with inline ViewCommand support via action property.
```

---

### Task 10: CommandReference for Named Command Lookup

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CommandReference.java`

**Step 1: Create CommandReference**

A `ViewCommand` implementation with `@TagName("command-ref")` that delegates to a named command from the enclosing `CommandScope`:

```java
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that delegates to a named command from the enclosing
 * {@link CommandScope}.
 *
 * <p>
 * Used in {@code <button>} elements to reference a command declared on the enclosing panel:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *     &lt;command-ref name="approve"/&gt;
 * &lt;/button&gt;
 * </pre>
 */
public class CommandReference implements ViewCommand {

	/**
	 * Configuration for {@link CommandReference}.
	 */
	@TagName("command-ref")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(CommandReference.class)
		Class<? extends ViewCommand> getImplementationClass();

		@Override
		@Name(NAME)
		@Mandatory
		String getName();
	}

	private final String _name;

	/**
	 * Creates a new {@link CommandReference} from configuration.
	 */
	@CalledByReflection
	public CommandReference(InstantiationContext context, Config config) {
		_name = config.getName();
	}

	/**
	 * The referenced command name.
	 */
	public String getReferenceName() {
		return _name;
	}

	@Override
	public HandlerResult execute(ViewDisplayContext context, Object input) {
		throw new UnsupportedOperationException(
			"CommandReference must be resolved to the actual command before execution.");
	}
}
```

The `ButtonElement.createControl()` method will detect `CommandReference` and resolve it via `context.getCommandScope().resolveCommand(name)`.

**Step 2: Commit**

```
Ticket #29108: Add CommandReference for named command lookup from enclosing panel.
```

---

### Task 11: Update PanelElement with Commands Section

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/PanelElement.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestPanelCommands.java`
- Test resource: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/command/test-panel-commands.view.xml`

**Step 1: Add commands property to PanelElement.Config**

```java
/** Configuration name for {@link #getCommands()}. */
String COMMANDS = "commands";

/**
 * Commands scoped to this panel.
 */
@Name(COMMANDS)
List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
```

**Step 2: Update PanelElement constructor**

Instantiate commands from config using the `InstantiationContext`.

**Step 3: Update PanelElement.createControl()**

1. Instantiate `ViewCommandModel` for each command (resolve input channels, build rules)
2. Create `CommandScope` with explicit commands
3. Create derived `ViewContext` with the command scope
4. Create child controls passing the derived context
5. Pass toolbar-placed command models to `ReactPanelControl` for rendering

**Step 4: Write test XML**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <channels>
        <channel name="selectedItem"/>
    </channels>
    <panel title="Commands Test">
        <commands>
            <command class="test.com.top_logic.layout.view.command.TestCommand"
                     name="testCmd" input="selectedItem" label="Test"/>
        </commands>
        <button>
            <command-ref name="testCmd"/>
        </button>
    </panel>
</view>
```

**Step 5: Write test**

Test parsing of panel with commands section, verifying:
- Commands are parsed and accessible
- CommandScope is populated
- ButtonElement with command-ref resolves correctly

**Step 6: Run tests**

Run: `mvn test -pl com.top_logic.layout.view -am -DskipTests=false -Dtest=TestPanelCommands`
Expected: PASS

**Step 7: Commit**

```
Ticket #29108: Add commands section to PanelElement with CommandScope integration.
```

---

### Task 12: I18NConstants for Command System

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/I18NConstants.java`

**Step 1: Add I18N constants**

Add constants for:
- Default confirmation message: `DEFAULT_CONFIRM_MESSAGE__COMMAND`
- Delete confirmation: `CONFIRM_DELETE__ELEMENT`
- Unknown command reference error: `ERROR_UNKNOWN_COMMAND_REF__NAME`

**Step 2: Build to regenerate messages**

Run: `mvn install -pl com.top_logic.layout.view`

**Step 3: Commit**

```
Ticket #29108: Add I18NConstants for the command system.
```

---

### Task 13: Demo Integration

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml`
- Create: `com.top_logic.demo/src/main/java/com/top_logic/demo/view/command/DemoDeleteCommand.java` (or similar)

**Step 1: Create a demo command**

A simple command that logs the input or shows a message:

```java
public class DemoDeleteCommand implements ViewCommand {

    @TagName("demo-delete")
    public interface Config extends ViewCommand.Config {
        @Override
        @ClassDefault(DemoDeleteCommand.class)
        Class<? extends ViewCommand> getImplementationClass();
    }

    @CalledByReflection
    public DemoDeleteCommand(InstantiationContext context, Config config) {
        // No-op.
    }

    @Override
    public HandlerResult execute(ViewDisplayContext context, Object input) {
        com.top_logic.basic.Logger.info(
            "Demo delete command executed with input: " + input, DemoDeleteCommand.class);
        return HandlerResult.DEFAULT_RESULT;
    }
}
```

**Step 2: Add to demo view**

Update `app.view.xml` to include a panel with toolbar commands and a button with an inline command, demonstrating the command system.

**Step 3: Build and verify**

Run: `mvn install -pl com.top_logic.demo -am`
Expected: PASS

**Step 4: Commit**

```
Ticket #29108: Add demo commands to showcase view command system.
```

---

### Task 14: Full Build and Integration Test

**Step 1: Full module build**

Run: `mvn install -pl com.top_logic.layout.view -am`
Expected: PASS

**Step 2: Run all view module tests**

Run: `mvn test -pl com.top_logic.layout.view -DskipTests=false`
Expected: All tests PASS

**Step 3: Build demo**

Run: `mvn install -pl com.top_logic.demo -am`
Expected: PASS

**Step 4: Final commit if any cleanup needed**

```
Ticket #29108: Final cleanup for view command system.
```
