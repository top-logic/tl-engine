# Declarative Dialogs Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable declaring dialog content in standalone `.view.xml` files and opening them from button actions without per-dialog Java code.

**Architecture:** Two new `ViewCommand` implementations in `com.top_logic.layout.view.command`: `OpenDialogCommand` loads a `.view.xml` file, creates an isolated `ViewContext`, copies input channel values, and opens the result via `DialogManager`. `CancelDialogCommand` closes the topmost dialog. A demo in `com.top_logic.demo` demonstrates the full workflow.

**Tech Stack:** Java 17, TopLogic view XML framework, TypedConfiguration, DialogManager API.

**Spec:** `docs/superpowers/specs/2026-03-16-declarative-dialogs-design.md`

---

## Chunk 1: Channel Mapping Config and OpenDialogCommand

### Task 1: Create ChannelMappingConfig

A configuration interface for a single channel mapping (`from` / `to` pair), used in both `<inputs>` and `<outputs>`.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/ChannelMappingConfig.java`

- [ ] **Step 1: Create ChannelMappingConfig**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration for a single channel mapping between two scopes.
 *
 * <p>
 * Used in {@code <input>} and {@code <output>} elements within dialog command configuration to map
 * a channel from one context (parent) to another (dialog).
 * </p>
 */
public interface ChannelMappingConfig extends ConfigurationItem {

	/** Configuration name for {@link #getFrom()}. */
	String FROM = "from";

	/** Configuration name for {@link #getTo()}. */
	String TO = "to";

	/**
	 * The source channel name (in the context providing the value).
	 */
	@Name(FROM)
	@Mandatory
	String getFrom();

	/**
	 * The target channel name (in the context receiving the value).
	 */
	@Name(TO)
	@Mandatory
	String getTo();
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
Ticket #29108: Add ChannelMappingConfig for dialog channel wiring.
```

---

### Task 2: Create OpenDialogCommand

The core command that loads a `.view.xml` file and opens it as a modal dialog.

**Key implementation detail:** `ViewCommand.execute()` receives a `ReactContext`, but at runtime this is always the `ViewContext` (since `ViewContext extends ReactContext` and `ReactButtonControl` stores the context from `ButtonElement.createControl()`). The command casts to `ViewContext` to access parent channels.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/OpenDialogCommand.java`

- [ ] **Step 1: Create OpenDialogCommand**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelMappingConfig;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that opens a modal dialog whose content is defined in a separate
 * {@code .view.xml} file.
 *
 * <p>
 * The dialog gets its own isolated {@link ViewContext}. Input channel values from the parent context
 * are copied once at open time (no live binding). The dialog view XML is fully self-contained and
 * defines its own chrome, layout, and action buttons.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.command.OpenDialogCommand"
 *     dialog-view="demo/show-details.view.xml"
 *     close-on-backdrop="true"&gt;
 *     &lt;inputs&gt;
 *       &lt;input from="selection" to="model"/&gt;
 *     &lt;/inputs&gt;
 *   &lt;/action&gt;
 * &lt;/button&gt;
 * </pre>
 */
public class OpenDialogCommand implements ViewCommand {

	/**
	 * Configuration for {@link OpenDialogCommand}.
	 */
	@TagName("open-dialog")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(OpenDialogCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getDialogView()}. */
		String DIALOG_VIEW = "dialog-view";

		/** Configuration name for {@link #getCloseOnBackdrop()}. */
		String CLOSE_ON_BACKDROP = "close-on-backdrop";

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getOutputs()}. */
		String OUTPUTS = "outputs";

		/**
		 * Path to the {@code .view.xml} file defining the dialog content, relative to
		 * {@code /WEB-INF/views/}.
		 */
		@Name(DIALOG_VIEW)
		@Mandatory
		String getDialogView();

		/**
		 * Whether clicking the backdrop (outside the dialog) closes it.
		 */
		@Name(CLOSE_ON_BACKDROP)
		@BooleanDefault(false)
		boolean getCloseOnBackdrop();

		/**
		 * Input channel mappings from the parent view to the dialog.
		 *
		 * <p>
		 * Each mapping copies the current value of a parent channel into a dialog channel at open
		 * time. This is a one-shot copy, not a live binding.
		 * </p>
		 */
		@Name(INPUTS)
		@EntryTag("input")
		List<ChannelMappingConfig> getInputs();

		/**
		 * Output channel mappings from the dialog back to the parent view.
		 *
		 * <p>
		 * Reserved for future use. When a result propagation mechanism is implemented, these
		 * mappings will define which dialog channel values are copied back to the parent on close.
		 * </p>
		 */
		@Name(OUTPUTS)
		@EntryTag("output")
		List<ChannelMappingConfig> getOutputs();
	}

	private final String _dialogViewPath;

	private final boolean _closeOnBackdrop;

	private final List<ChannelMappingConfig> _inputs;

	/**
	 * Creates a new {@link OpenDialogCommand}.
	 */
	@CalledByReflection
	public OpenDialogCommand(InstantiationContext context, Config config) {
		_dialogViewPath = ViewLoader.VIEW_BASE_PATH + config.getDialogView();
		_closeOnBackdrop = config.getCloseOnBackdrop();
		_inputs = config.getInputs();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ViewElement dialogView;
		try {
			dialogView = ViewLoader.getOrLoadView(_dialogViewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load dialog view: " + _dialogViewPath, ex);
		}

		// Create isolated ViewContext for the dialog.
		ViewContext dialogContext = new DefaultViewContext(context);

		// Propagate error sink from parent (same pattern as ReferenceElement).
		if (context instanceof ViewContext) {
			ViewContext parentViewContext = (ViewContext) context;
			ErrorSink parentErrorSink = parentViewContext.getErrorSink();
			if (parentErrorSink != null) {
				dialogContext = dialogContext.withErrorSink(parentErrorSink);
			}

			// One-shot copy of input channel values.
			for (ChannelMappingConfig mapping : _inputs) {
				String fromName = mapping.getFrom();
				if (!parentViewContext.hasChannel(fromName)) {
					Logger.warn("Input channel '" + fromName + "' not found in parent context, skipping.",
						OpenDialogCommand.class);
					continue;
				}
				ViewChannel parentChannel = parentViewContext.resolveChannel(new ChannelRef(fromName));
				DefaultViewChannel dialogChannel = new DefaultViewChannel(mapping.getTo());
				dialogChannel.set(parentChannel.get());
				dialogContext.registerChannel(mapping.getTo(), dialogChannel);
			}
		}

		// Build the dialog control tree.
		ReactControl dialogControl = (ReactControl) dialogView.createControl(dialogContext);

		mgr.openDialog(_closeOnBackdrop, dialogControl, result -> {
			// Output channel propagation: deferred to future implementation.
		});

		return HandlerResult.DEFAULT_RESULT;
	}
}
```

**Note:** `ChannelRef` is a concrete class with a `ChannelRef(String channelName)` constructor. Use `new ChannelRef(mapping.getFrom())` directly — no inner class needed.

- [ ] **Step 2: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
Ticket #29108: Add OpenDialogCommand for declarative dialog opening.
```

---

### Task 3: Create CancelDialogCommand

A zero-configuration command that closes the topmost dialog.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CancelDialogCommand.java`

- [ ] **Step 1: Create CancelDialogCommand**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that closes the topmost open dialog without propagating any results.
 *
 * <p>
 * This command is intended for "Cancel" or "Close" buttons inside dialog view XML files. It takes
 * no configuration beyond the standard {@link ViewCommand.Config} properties (label, image, etc.).
 * </p>
 */
public class CancelDialogCommand implements ViewCommand {

	/**
	 * Configuration for {@link CancelDialogCommand}.
	 */
	@TagName("cancel-dialog")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(CancelDialogCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link CancelDialogCommand}.
	 */
	@CalledByReflection
	public CancelDialogCommand(InstantiationContext context, Config config) {
		// No additional configuration.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		mgr.closeTopDialog(DialogResult.cancelled());
		return HandlerResult.DEFAULT_RESULT;
	}
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
Ticket #29108: Add CancelDialogCommand for closing dialogs without result.
```

---

## Chunk 2: Demo

### Task 4: Create demo dialog view XML

A standalone dialog view XML file that shows a form bound to a `model` channel, with a Cancel button.

**Files:**
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/show-details-dialog.view.xml`

- [ ] **Step 1: Create the dialog view XML**

```xml
<?xml version="1.0" encoding="utf-8" ?>

<view>
	<channels>
		<channel name="model"/>
	</channels>
	<card title="Details">
		<stack direction="column" gap="default">
			<form model="model"/>
			<stack direction="row" gap="default">
				<button>
					<action class="com.top_logic.layout.view.command.CancelDialogCommand"
						label="Close"
					/>
				</button>
			</stack>
		</stack>
	</card>
</view>
```

**Note:** The `<form model="model"/>` element binds to the `model` channel. Verify that `FormElement` supports a `model` attribute referencing a channel — check `FormElement.Config`. If it uses a different mechanism (e.g., the form model is set on the ViewContext, not via channel), adjust accordingly.

- [ ] **Step 2: Commit**

```
Ticket #29108: Add dialog view XML for DemoTypeA details.
```

---

### Task 5: Update dialog-demo.view.xml to use OpenDialogCommand

Add a new button to the existing dialog demo that opens the declarative dialog. This demonstrates:
- `OpenDialogCommand` with `dialog-view` and `close-on-backdrop`
- Input channel wiring (tree selection to dialog model)

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/dialog-demo.view.xml`

- [ ] **Step 1: Examine the existing dialog demo view and channels demo for patterns**

Read `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/channels-demo.view.xml` to see how channels and selection are used in existing demos.
Read `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/form-demo.view.xml` to see how forms bind to models.

- [ ] **Step 2: Update dialog-demo.view.xml**

Add a new card section below the existing "Dialog Demo" card. This section declares a channel for selection, includes a simple data source, and uses `OpenDialogCommand`:

```xml
<?xml version="1.0" encoding="utf-8" ?>

<view>
	<channels>
		<channel name="selection"/>
	</channels>
	<stack
		direction="column"
		gap="default"
	>
		<card
			title="Dialog Demo (Programmatic)"
			variant="elevated"
		>
			<stack
				direction="column"
				gap="default"
			>
				<button>
					<action class="com.top_logic.demo.view.command.OpenDemoDialogCommand"
						label="Open Dialog"
						dialog-title="Greetings"
						dialog-message="Hello from the dialog manager!"
					/>
				</button>
				<button>
					<action class="com.top_logic.demo.view.command.OpenDemoDialogCommand"
						label="Open Another Dialog"
						dialog-title="Confirmation"
						dialog-message="Are you sure you want to proceed?"
					/>
				</button>
			</stack>
		</card>
		<card
			title="Dialog Demo (Declarative)"
			variant="elevated"
		>
			<stack
				direction="column"
				gap="default"
			>
				<button>
					<action class="com.top_logic.layout.view.command.OpenDialogCommand"
						label="Show Details Dialog"
						dialog-view="demo/show-details-dialog.view.xml"
						close-on-backdrop="true"
					/>
				</button>
			</stack>
		</card>
	</stack>
</view>
```

**Note:** The initial demo may not wire selection channels if no suitable data source (tree/table with DemoTypeA) exists in the dialog demo yet. Start with a simple `OpenDialogCommand` without inputs to prove the mechanism works. Input channel wiring can be added once the form binding in the dialog is verified.

- [ ] **Step 3: Commit**

```
Ticket #29108: Add declarative dialog demo using OpenDialogCommand.
```

---

### Task 6: Build and test

Build the demo module and verify the dialog opens correctly in the browser.

- [ ] **Step 1: Build com.top_logic.layout.view**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 2: Build com.top_logic.demo (incremental, no clean)**

Run: `mvn compile -DskipTests=true -pl com.top_logic.demo`
Expected: BUILD SUCCESS

- [ ] **Step 3: Start the demo app and test in browser**

Use the `tl-app` skill to start the demo application. Navigate to the "Dialog Demo" page in the sidebar. Click the "Show Details Dialog" button. Verify:
- The dialog opens as a modal overlay
- The dialog shows the card with "Details" title
- The "Close" button closes the dialog
- Clicking the backdrop closes the dialog (since `close-on-backdrop="true"`)

- [ ] **Step 4: Final commit if any fixes were needed**

```
Ticket #29108: Fix issues found during manual testing.
```

---

## Implementation Notes

### ReactContext is ViewContext at runtime

The `ViewCommand.execute(ReactContext context, Object input)` method receives a `ReactContext`. However, within the view system, `ReactButtonControl` stores the context from its constructor (which is the `ViewContext` from `ButtonElement.createControl()`). The `@ReactCommand` handler passes this stored context to the action. Therefore, `OpenDialogCommand` can safely cast `context instanceof ViewContext` to access parent channels.

See: `ReactButtonControl:102` (`handleClick`), `ReactControl:157` (`executeCommand` passes `_reactContext`), `ButtonElement:109` (`createControl` passes `ViewContext` to constructor).

### DefaultViewContext(ReactContext) creates isolated channel namespace

`new DefaultViewContext(parentContext)` creates a fresh context with an empty channel map. This is the same pattern used by `ReferenceElement:113`. The dialog's channels are completely independent from the parent.

### ViewLoader caches parsed views

`ViewLoader.getOrLoadView()` caches the parsed `ViewElement` tree. The dialog view XML is loaded once and reused across sessions. The per-session control tree is created by `ViewElement.createControl()`.

### @EntryTag for list properties

The `@EntryTag("input")` annotation on `getInputs()` allows the XML:
```xml
<inputs>
  <input from="selection" to="model"/>
</inputs>
```
Without `@EntryTag`, the entries would need the full interface tag name.

### Form model binding

The `<form model="model"/>` syntax needs verification against `FormElement.Config`. If `FormElement` doesn't support a `model` attribute that references a channel, the demo dialog content should be simplified to use static content (e.g., just a text label) until form-channel binding is implemented.
