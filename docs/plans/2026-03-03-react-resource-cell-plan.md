# React Resource Cell Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a `TLResourceCell` control that displays business objects with icon + label + goto-link in the React UI layer.

**Architecture:** Server-side `ReactResourceCellControl` resolves `ResourceProvider` data (label, icon, tooltip, CSS class, link availability) into a flat state object. The React `TLResourceCell` component renders it. Goto navigation uses the standard `useTLCommand` pattern.

**Tech Stack:** Java 17 (server-side ReactControl), TypeScript/React (client-side component), tl-react-bridge

---

### Task 1: Create ReactResourceCellControl (Java)

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactResourceCellControl.java`

**Step 1: Create the Java control class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * A cell control that displays a business object using a {@link ResourceProvider}.
 *
 * <p>
 * Resolves label, icon, CSS class, tooltip, and link availability from the provider and sends them
 * as flat state to the {@code TLResourceCell} React component. Supports goto navigation via a
 * server command.
 * </p>
 */
public class ReactResourceCellControl extends ReactControl {

	// -- State keys --

	private static final String LABEL = "label";

	private static final String ICON_CSS = "iconCss";

	private static final String ICON_SRC = "iconSrc";

	private static final String CSS_CLASS = "cssClass";

	private static final String TOOLTIP = "tooltip";

	private static final String HAS_LINK = "hasLink";

	// -- Configuration --

	private static final String CSS_PREFIX = "css:";

	private static final String COLORED_CSS_PREFIX = "colored:";

	// -- Commands --

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new GotoCommand());

	// -- Fields --

	private final ResourceProvider _provider;

	private final boolean _useImage;

	private final boolean _useLabel;

	private final boolean _useLink;

	private Object _rowObject;

	/**
	 * Creates a new {@link ReactResourceCellControl}.
	 *
	 * @param value
	 *        The value to display.
	 * @param provider
	 *        The {@link ResourceProvider} to resolve display data from.
	 * @param useImage
	 *        Whether to resolve and display the type icon.
	 * @param useLabel
	 *        Whether to resolve and display the label text.
	 * @param useLink
	 *        Whether to enable goto navigation on click.
	 */
	public ReactResourceCellControl(Object value, ResourceProvider provider, boolean useImage, boolean useLabel,
			boolean useLink) {
		super(null, "TLResourceCell", COMMANDS);
		_provider = provider;
		_useImage = useImage;
		_useLabel = useLabel;
		_useLink = useLink;
		_rowObject = value;
		resolveState(value);
	}

	/**
	 * Convenience constructor with all display options enabled.
	 */
	public ReactResourceCellControl(Object value, ResourceProvider provider) {
		this(value, provider, true, true, true);
	}

	/**
	 * Updates the displayed value.
	 */
	public void update(Object value) {
		_rowObject = value;
		resolveState(value);
	}

	private void resolveState(Object value) {
		if (_useLabel) {
			String label = value != null ? _provider.getLabel(value) : null;
			putState(LABEL, label != null ? label : "");
		}

		if (_useImage && value != null) {
			resolveIcon(value);
		}

		if (value != null) {
			String cssClass = _provider.getCssClass(value);
			if (cssClass != null) {
				putState(CSS_CLASS, cssClass);
			}

			String tooltip = _provider.getTooltip(value);
			if (tooltip != null) {
				putState(TOOLTIP, tooltip);
			}
		}

		putState(HAS_LINK, Boolean.valueOf(_useLink && value != null));
	}

	private void resolveIcon(Object value) {
		ThemeImage image = _provider.getImage(value, Flavor.DEFAULT);
		if (image == null) {
			return;
		}
		ThemeImage resolved = image.resolve();
		if (resolved == ThemeImage.none()) {
			return;
		}
		String encoded = resolved.toEncodedForm();
		if (encoded.startsWith(CSS_PREFIX)) {
			putState(ICON_CSS, encoded.substring(CSS_PREFIX.length()));
		} else if (encoded.startsWith(COLORED_CSS_PREFIX)) {
			putState(ICON_CSS, encoded.substring(COLORED_CSS_PREFIX.length()));
		} else {
			// Resource image - need context path prefix at render time, use theme file link.
			putState(ICON_SRC, encoded);
		}
	}

	/**
	 * Handles goto navigation when the user clicks the resource link.
	 */
	static class GotoCommand extends ControlCommand {

		GotoCommand() {
			super("goto");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.resource.goto");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactResourceCellControl cell = (ReactResourceCellControl) control;
			Object target = cell._rowObject;
			if (target == null) {
				return HandlerResult.DEFAULT_RESULT;
			}

			GotoHandler gotoHandler = GotoHandler.getInstance();
			return gotoHandler.executeGoto(context, context.getLayoutContext().getMainLayout(), null, target);
		}
	}
}
```

**Notes:**
- `ThemeImage.toEncodedForm()` returns `"css:bi bi-person"` for CSS icons and `"/path/to/img.png"` for resource images. We parse the prefix to determine the icon type.
- The `GotoHandler.getInstance()` call needs verification — check if there's a static accessor or if it goes through `CommandHandlerFactory`. Adjust accordingly.
- `iconSrc` for resource images currently stores the theme-local path. The React component may need to prepend the context path. Check if the bridge provides this.

**Step 2: Verify GotoHandler access pattern**

Check how to obtain a `GotoHandler` instance. Search for `GotoHandler.getInstance` or `CommandHandlerFactory.getInstance().getHandler`. The `execute` method may need adjustment based on the actual API.

Run: `grep -r "GotoHandler.getInstance\|GotoHandler gotoHandler\|new GotoHandler" com.top_logic/src/main/java/ | head -10`

Adjust the `GotoCommand.execute()` method based on findings.

**Step 3: Build the module**

Run: `cd com.top_logic.layout.react && mvn install -DskipTests=true`

Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactResourceCellControl.java
git commit -m "Ticket #29109: Add ReactResourceCellControl for resource display in React tables."
```

---

### Task 2: Create TLResourceCell React Component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLResourceCell.tsx`
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts`

**Step 1: Create the React component**

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Displays a business object with optional icon, label, and goto link.
 *
 * State:
 * - label?: string      - display text
 * - iconCss?: string    - CSS icon class (e.g. "bi bi-person-fill")
 * - iconSrc?: string    - image URL for non-CSS icons
 * - cssClass?: string   - type-specific CSS class
 * - tooltip?: string    - plain text tooltip
 * - hasLink: boolean    - whether clicking navigates to the object
 */
const TLResourceCell: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const iconCss = state.iconCss as string | undefined;
  const iconSrc = state.iconSrc as string | undefined;
  const label = state.label as string | undefined;
  const cssClass = state.cssClass as string | undefined;
  const tooltip = state.tooltip as string | undefined;
  const hasLink = state.hasLink as boolean;

  const icon = iconCss
    ? <i className={iconCss} />
    : iconSrc
    ? <img src={iconSrc} className="tlTypeIcon" alt="" />
    : null;

  const content = (
    <>
      {icon}
      {label && <span className="tlResourceLabel">{label}</span>}
    </>
  );

  const handleClick = React.useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    sendCommand('goto', {});
  }, [sendCommand]);

  const className = ['tlResourceCell', cssClass].filter(Boolean).join(' ');

  if (hasLink) {
    return (
      <a className={className} href="#" onClick={handleClick} title={tooltip}>
        {content}
      </a>
    );
  }

  return (
    <span className={className} title={tooltip}>
      {content}
    </span>
  );
};

export default TLResourceCell;
```

**Step 2: Register the component**

In `controls-entry.ts`, add the import and registration:

```typescript
import TLResourceCell from './controls/TLResourceCell';
// ... after existing registrations:
register('TLResourceCell', TLResourceCell);
```

**Step 3: Build the React bundle**

Run: `cd com.top_logic.layout.react && mvn install -DskipTests=true`

Expected: BUILD SUCCESS (webpack compiles the new component into `tl-react-controls.js`)

**Step 4: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLResourceCell.tsx
git add com.top_logic.layout.react/react-src/controls-entry.ts
git commit -m "Ticket #29109: Add TLResourceCell React component for resource display."
```

---

### Task 3: Verify End-to-End Integration

**Step 1: Check compilation**

Run: `cd com.top_logic.layout.react && mvn install -DskipTests=true`

Expected: BUILD SUCCESS for both Java and React compilation.

**Step 2: Verify the component is registered**

Check the built `tl-react-controls.js` bundle for `TLResourceCell`:

Run: `grep -c "TLResourceCell" com.top_logic.layout.react/src/main/webapp/script/tl-react-controls.js`

Expected: At least 1 match.

**Step 3: Commit final state if any adjustments were needed**

```bash
git add -A com.top_logic.layout.react/
git commit -m "Ticket #29109: Fix integration issues in TLResourceCell."
```
