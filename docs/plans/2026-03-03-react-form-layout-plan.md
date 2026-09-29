# React Form Layout Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement three composable React form controls (TLFormLayout, TLFormGroup, TLFormField) that provide a responsive CSS Grid-based form layout with full field anatomy chrome.

**Architecture:** Server assembles a control tree (ReactFormLayoutControl → ReactFormGroupControl → ReactFormFieldChromeControl → existing ReactFormFieldControl). The React side uses a shared FormLayoutContext to propagate readOnly and resolved label position. CSS Grid with auto-fit handles responsive columns; CSS subgrid aligns fields across sibling groups.

**Tech Stack:** TypeScript/React (via tl-react-bridge), Java (ReactControl base class), CSS Grid + subgrid, CSS container queries.

**Design doc:** `docs/plans/2026-03-03-react-form-layout-design.md`

---

### Task 1: Create FormLayoutContext (shared React context)

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/FormLayoutContext.ts`

**Step 1: Create the shared context module**

```typescript
import { React } from 'tl-react-bridge';

export interface FormLayoutContextValue {
  readOnly: boolean;
  resolvedLabelPosition: 'side' | 'top';
}

const defaultContext: FormLayoutContextValue = {
  readOnly: false,
  resolvedLabelPosition: 'side',
};

export const FormLayoutContext = React.createContext<FormLayoutContextValue>(defaultContext);
```

**Step 2: Commit**

```
Ticket #29109: Add FormLayoutContext for form layout state propagation.
```

---

### Task 2: Create TLFormField React component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLFormField.tsx`

**Step 1: Implement TLFormField**

This component wraps a field control with label, required indicator, help icon, error message, help text, and dirty indicator. It reads `readOnly` and `resolvedLabelPosition` from FormLayoutContext.

```typescript
import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FormLayoutContext } from './FormLayoutContext';

const { useContext, useState, useCallback } = React;

/**
 * Form field chrome wrapper that renders label, required indicator,
 * help icon, error message, help text, and dirty indicator around
 * any field input control.
 *
 * State:
 * - label: string
 * - required: boolean
 * - error: string | null
 * - helpText: string | null
 * - dirty: boolean
 * - labelPosition: "side" | "top" | null  (null = inherit from context)
 * - fullLine: boolean
 * - visible: boolean
 * - field: ChildDescriptor
 */
const TLFormField: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const ctx = useContext(FormLayoutContext);

  const label = (state.label as string) ?? '';
  const required = state.required === true;
  const error = state.error as string | null;
  const helpText = state.helpText as string | null;
  const dirty = state.dirty === true;
  const labelPos = (state.labelPosition as string | null) ?? ctx.resolvedLabelPosition;
  const fullLine = state.fullLine === true;
  const visible = state.visible !== false;
  const field = state.field;
  const readOnly = ctx.readOnly;

  const [helpVisible, setHelpVisible] = useState(false);
  const toggleHelp = useCallback(() => setHelpVisible(v => !v), []);

  if (!visible) return null;

  const hasError = error != null;

  const className = [
    'tlFormField',
    `tlFormField--${labelPos}`,
    readOnly ? 'tlFormField--readonly' : '',
    fullLine ? 'tlFormField--fullLine' : '',
    hasError ? 'tlFormField--error' : '',
    dirty ? 'tlFormField--dirty' : '',
  ].filter(Boolean).join(' ');

  return (
    <div id={controlId} className={className}>
      {dirty && <div className="tlFormField__dirtyBar" />}
      <div className="tlFormField__label">
        <span className="tlFormField__labelText">{label}</span>
        {required && !readOnly && <span className="tlFormField__required">*</span>}
        {helpText && !readOnly && (
          <button type="button" className="tlFormField__helpIcon" onClick={toggleHelp}
            aria-label="Help">
            <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
              <circle cx="8" cy="8" r="7" fill="none" stroke="currentColor" strokeWidth="1.5" />
              <text x="8" y="12" textAnchor="middle" fontSize="10"
                fill="currentColor">?</text>
            </svg>
          </button>
        )}
      </div>
      <div className="tlFormField__input">
        <TLChild control={field} />
      </div>
      {!readOnly && hasError && (
        <div className="tlFormField__error">
          <svg className="tlFormField__errorIcon" viewBox="0 0 16 16" width="14" height="14"
            aria-hidden="true">
            <path d="M8 1l7 14H1L8 1z" fill="none" stroke="currentColor" strokeWidth="1.2" />
            <line x1="8" y1="6" x2="8" y2="10" stroke="currentColor" strokeWidth="1.2" />
            <circle cx="8" cy="12" r="0.8" fill="currentColor" />
          </svg>
          <span>{error}</span>
        </div>
      )}
      {!readOnly && helpText && helpVisible && (
        <div className="tlFormField__helpText">{helpText}</div>
      )}
    </div>
  );
};

export default TLFormField;
```

**Step 2: Commit**

```
Ticket #29109: Add TLFormField React component with field anatomy chrome.
```

---

### Task 3: Create TLFormGroup React component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLFormGroup.tsx`

**Step 1: Implement TLFormGroup**

```typescript
import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

const I18N_KEYS = {
  'js.formGroup.collapse': 'Collapse',
  'js.formGroup.expand': 'Expand',
};

/**
 * A nestable form section with optional header, collapsible body, and border.
 * Participates as a grid item in the parent layout; uses CSS subgrid internally.
 *
 * State:
 * - header: string | null
 * - headerActions: ChildDescriptor[]
 * - collapsible: boolean
 * - collapsed: boolean
 * - border: "none" | "subtle" | "outlined"
 * - fullLine: boolean
 * - children: ChildDescriptor[]
 */
const TLFormGroup: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const header = state.header as string | null;
  const headerActions = (state.headerActions as unknown[]) ?? [];
  const collapsible = state.collapsible === true;
  const collapsed = state.collapsed === true;
  const border = (state.border as string) ?? 'none';
  const fullLine = state.fullLine === true;
  const children = (state.children as unknown[]) ?? [];

  const hasHeader = header != null || headerActions.length > 0 || collapsible;

  const handleToggle = useCallback(() => {
    sendCommand('toggleCollapse');
  }, [sendCommand]);

  const className = [
    'tlFormGroup',
    `tlFormGroup--border-${border}`,
    fullLine ? 'tlFormGroup--fullLine' : '',
    collapsed ? 'tlFormGroup--collapsed' : '',
  ].filter(Boolean).join(' ');

  return (
    <div id={controlId} className={className}>
      {hasHeader && (
        <div className="tlFormGroup__header">
          {collapsible && (
            <button type="button" className="tlFormGroup__collapseToggle"
              onClick={handleToggle}
              aria-expanded={!collapsed}
              title={collapsed ? i18n['js.formGroup.expand'] : i18n['js.formGroup.collapse']}>
              <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true"
                className={collapsed ? 'tlFormGroup__chevron--collapsed' : 'tlFormGroup__chevron'}>
                <polyline points="4,6 8,10 12,6" fill="none" stroke="currentColor"
                  strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          )}
          {header && <span className="tlFormGroup__title">{header}</span>}
          {headerActions.length > 0 && (
            <div className="tlFormGroup__actions">
              {headerActions.map((action, i) => (
                <TLChild key={i} control={action} />
              ))}
            </div>
          )}
        </div>
      )}
      {!collapsed && (
        <div className="tlFormGroup__body">
          {children.map((child, i) => (
            <TLChild key={i} control={child} />
          ))}
        </div>
      )}
    </div>
  );
};

export default TLFormGroup;
```

**Step 2: Commit**

```
Ticket #29109: Add TLFormGroup React component with collapsible sections.
```

---

### Task 4: Create TLFormLayout React component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLFormLayout.tsx`

**Step 1: Implement TLFormLayout**

This is the top-level container. It renders a CSS Grid and provides FormLayoutContext. For `labelPosition: "auto"`, it uses a ResizeObserver on the container to measure column width and toggle between side/top labels.

```typescript
import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FormLayoutContext } from './FormLayoutContext';

const { useMemo, useRef, useState, useEffect } = React;

/** Column width threshold (px) below which labels switch from side to top. */
const LABEL_SIDE_MIN_WIDTH = 320;

/**
 * Top-level responsive form grid.
 *
 * State:
 * - maxColumns: number
 * - labelPosition: "side" | "top" | "auto"
 * - readOnly: boolean
 * - children: ChildDescriptor[]
 */
const TLFormLayout: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const maxColumns = (state.maxColumns as number) ?? 3;
  const labelPosition = (state.labelPosition as string) ?? 'auto';
  const readOnly = state.readOnly === true;
  const children = (state.children as unknown[]) ?? [];

  const containerRef = useRef<HTMLDivElement>(null);
  const [resolvedPosition, setResolvedPosition] = useState<'side' | 'top'>(
    labelPosition === 'top' ? 'top' : 'side'
  );

  // Observe container width to resolve "auto" label position.
  useEffect(() => {
    if (labelPosition !== 'auto') {
      setResolvedPosition(labelPosition as 'side' | 'top');
      return;
    }

    const el = containerRef.current;
    if (!el) return;

    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const containerWidth = entry.contentRect.width;
        // Estimate column width: container / maxColumns (approximate)
        const estimatedColWidth = containerWidth / maxColumns;
        setResolvedPosition(estimatedColWidth < LABEL_SIDE_MIN_WIDTH ? 'top' : 'side');
      }
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, [labelPosition, maxColumns]);

  const ctxValue = useMemo(() => ({
    readOnly,
    resolvedLabelPosition: resolvedPosition,
  }), [readOnly, resolvedPosition]);

  // Compute min column width for auto-fit.
  // This ensures columns don't go below a reasonable width before wrapping.
  const minColWidth = `${Math.max(16, Math.floor(64 / maxColumns))}rem`;

  const style: React.CSSProperties = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${minColWidth}, 1fr))`,
  };

  const className = [
    'tlFormLayout',
    readOnly ? 'tlFormLayout--readonly' : '',
  ].filter(Boolean).join(' ');

  return (
    <FormLayoutContext.Provider value={ctxValue}>
      <div id={controlId} className={className} style={style} ref={containerRef}>
        {children.map((child, i) => (
          <TLChild key={i} control={child} />
        ))}
      </div>
    </FormLayoutContext.Provider>
  );
};

export default TLFormLayout;
```

**Step 2: Commit**

```
Ticket #29109: Add TLFormLayout React component with responsive grid and context.
```

---

### Task 5: Add CSS for form layout controls

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` (append at end, after line 1953)

**Step 1: Add form layout CSS**

Append the following CSS at the end of the file:

```css
/* --- TLFormLayout ------------------------------------------------------ */

.tlFormLayout {
	display: grid;
	gap: var(--spacing-03, 0.5rem);
	font-family: var(--font-family);
	width: 100%;
}

/* --- TLFormGroup ------------------------------------------------------- */

.tlFormGroup {
	display: grid;
	grid-template-columns: subgrid;
	grid-template-rows: auto 1fr;
}

.tlFormGroup--fullLine {
	grid-column: 1 / -1;
}

.tlFormGroup--border-subtle {
	border: 1px solid var(--border-subtle, #e0e0e0);
	border-radius: var(--border-radius-02, 4px);
	padding: var(--spacing-03, 0.5rem);
}

.tlFormGroup--border-outlined {
	border: 1px solid var(--border-strong, #8d8d8d);
	border-radius: var(--border-radius-02, 4px);
	padding: var(--spacing-03, 0.5rem);
}

.tlFormGroup--border-none {
	border: none;
	padding: 0;
}

.tlFormGroup__header {
	grid-column: 1 / -1;
	display: flex;
	align-items: center;
	gap: var(--spacing-02, 0.25rem);
	padding-bottom: var(--spacing-03, 0.5rem);
	border-bottom: 1px solid var(--border-subtle, #e0e0e0);
	margin-bottom: var(--spacing-03, 0.5rem);
}

.tlFormGroup--border-none > .tlFormGroup__header {
	border-bottom: none;
}

.tlFormGroup__title {
	font-weight: 600;
	font-size: var(--heading-compact-02-font-size, 0.875rem);
	line-height: var(--heading-compact-02-line-height, 1.125rem);
	color: var(--text-primary, #161616);
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.tlFormGroup__collapseToggle {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 1.5rem;
	height: 1.5rem;
	padding: 0;
	border: none;
	background: none;
	cursor: pointer;
	color: var(--text-secondary, #525252);
	border-radius: var(--border-radius-02, 4px);
	flex-shrink: 0;
}

.tlFormGroup__collapseToggle:hover {
	background: var(--layer-hover, rgba(0, 0, 0, 0.06));
}

.tlFormGroup__chevron {
	transition: transform 0.15s ease;
}

.tlFormGroup__chevron--collapsed {
	transform: rotate(-90deg);
	transition: transform 0.15s ease;
}

.tlFormGroup__actions {
	display: flex;
	align-items: center;
	gap: var(--spacing-01, 0.125rem);
	flex-shrink: 0;
	margin-left: auto;
}

.tlFormGroup__body {
	grid-column: 1 / -1;
	display: grid;
	grid-template-columns: subgrid;
	gap: var(--spacing-03, 0.5rem);
}

/* When fullLine, body defines its own responsive grid instead of subgrid */
.tlFormGroup--fullLine > .tlFormGroup__body {
	grid-template-columns: inherit;
}

/* --- TLFormField ------------------------------------------------------- */

.tlFormField {
	display: grid;
	gap: var(--spacing-02, 0.25rem);
	position: relative;
	font-family: var(--font-family);
}

.tlFormField--fullLine {
	grid-column: 1 / -1;
}

/* Side-by-side label layout */
.tlFormField--side {
	grid-template-columns: minmax(6rem, 10rem) 1fr;
	grid-template-rows: auto auto auto;
	align-items: start;
}

.tlFormField--side > .tlFormField__label {
	grid-column: 1;
	grid-row: 1;
	padding-top: 0.375rem;
}

.tlFormField--side > .tlFormField__input {
	grid-column: 2;
	grid-row: 1;
}

.tlFormField--side > .tlFormField__error {
	grid-column: 2;
	grid-row: 2;
}

.tlFormField--side > .tlFormField__helpText {
	grid-column: 2;
	grid-row: 3;
}

/* Stacked label layout */
.tlFormField--top {
	grid-template-columns: 1fr;
}

/* Read-only: compact, no extra chrome */
.tlFormField--readonly {
	gap: 0;
}

.tlFormField--readonly > .tlFormField__error,
.tlFormField--readonly > .tlFormField__helpText {
	display: none;
}

/* Dirty indicator bar */
.tlFormField__dirtyBar {
	position: absolute;
	left: -2px;
	top: 0;
	bottom: 0;
	width: 3px;
	background: var(--layer-accent, #0f62fe);
	border-radius: 2px;
}

/* Label area */
.tlFormField__label {
	display: flex;
	align-items: center;
	gap: var(--spacing-01, 0.125rem);
	color: var(--text-secondary, #525252);
	font-size: var(--body-compact-01-font-size, 0.875rem);
	line-height: var(--body-compact-01-line-height, 1.125rem);
}

.tlFormField__labelText {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.tlFormField__required {
	color: var(--text-error, #da1e28);
	font-weight: 600;
	flex-shrink: 0;
}

.tlFormField__helpIcon {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 1.25rem;
	height: 1.25rem;
	padding: 0;
	border: none;
	background: none;
	cursor: pointer;
	color: var(--text-helper, #6f6f6f);
	flex-shrink: 0;
}

.tlFormField__helpIcon:hover {
	color: var(--text-primary, #161616);
}

/* Input wrapper */
.tlFormField__input {
	min-width: 0;
}

/* Error area */
.tlFormField__error {
	display: flex;
	align-items: flex-start;
	gap: var(--spacing-01, 0.125rem);
	color: var(--text-error, #da1e28);
	font-size: var(--body-compact-01-font-size, 0.875rem);
	line-height: var(--body-compact-01-line-height, 1.125rem);
}

.tlFormField__errorIcon {
	flex-shrink: 0;
	margin-top: 1px;
}

/* Help text */
.tlFormField__helpText {
	color: var(--text-helper, #6f6f6f);
	font-size: var(--body-compact-01-font-size, 0.875rem);
	line-height: var(--body-compact-01-line-height, 1.125rem);
}
```

**Step 2: Commit**

```
Ticket #29109: Add CSS for TLFormLayout, TLFormGroup, and TLFormField.
```

---

### Task 6: Register form controls in controls-entry.ts

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts`

**Step 1: Add imports and register calls**

Add after the existing imports (after line 46, before the `register()` block):

```typescript
import TLFormLayout from './controls/TLFormLayout';
import TLFormGroup from './controls/TLFormGroup';
import TLFormField from './controls/TLFormField';
```

Add after the last `register()` call (after line 82):

```typescript
register('TLFormLayout', TLFormLayout);
register('TLFormGroup', TLFormGroup);
register('TLFormField', TLFormField);
```

**Step 2: Commit**

```
Ticket #29109: Register TLFormLayout, TLFormGroup, TLFormField in controls entry.
```

---

### Task 7: Build React bundle and verify

**Step 1: Build the React bundle**

Run from the module directory:

```bash
cd com.top_logic.layout.react && npm run build
```

Expected: Build succeeds without TypeScript errors. Output bundle `tl-react-controls.js` is regenerated.

**Step 2: Commit the rebuilt bundle**

```
Ticket #29109: Rebuild tl-react-controls bundle with form layout controls.
```

---

### Task 8: Add I18N constants for form layout commands

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/I18NConstants.java`

**Step 1: Add I18N constants**

Add before the `static { initConstants(...) }` block (before line 347):

```java
	/**
	 * @en Form group collapse toggled.
	 */
	public static ResKey REACT_FORM_GROUP_TOGGLE_COLLAPSE;

	// -- Form group client-side i18n keys --

	/**
	 * @en Collapse
	 * @de Zuklappen
	 */
	public static ResKey JS_FORM_GROUP_COLLAPSE = ResKey.internalCreate("js.formGroup.collapse");

	/**
	 * @en Expand
	 * @de Aufklappen
	 */
	public static ResKey JS_FORM_GROUP_EXPAND = ResKey.internalCreate("js.formGroup.expand");
```

**Step 2: Commit**

```
Ticket #29109: Add I18N constants for form group collapse toggle.
```

---

### Task 9: Create ReactFormFieldChromeControl (Java)

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactFormFieldChromeControl.java`

**Step 1: Implement the Java control**

This is the server-side counterpart of TLFormField. It wraps any `ReactControl` (typically a `ReactFormFieldControl`) with label/error/help chrome metadata.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.layout.react.ReactControl;

/**
 * A {@link ReactControl} that renders field anatomy chrome (label, required indicator, error,
 * help text, dirty indicator) around a child field control via the {@code TLFormField} React
 * component.
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code label} - the field label text</li>
 * <li>{@code required} - whether the field is required</li>
 * <li>{@code error} - error message, or {@code null}</li>
 * <li>{@code helpText} - help/description text, or {@code null}</li>
 * <li>{@code dirty} - whether the field has been modified</li>
 * <li>{@code labelPosition} - "side", "top", or {@code null} (inherit from layout)</li>
 * <li>{@code fullLine} - whether the field spans the full grid row</li>
 * <li>{@code visible} - whether the field is visible</li>
 * <li>{@code field} - the child field control descriptor</li>
 * </ul>
 */
public class ReactFormFieldChromeControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormField";

	private static final String LABEL = "label";

	private static final String REQUIRED = "required";

	private static final String ERROR = "error";

	private static final String HELP_TEXT = "helpText";

	private static final String DIRTY = "dirty";

	private static final String LABEL_POSITION = "labelPosition";

	private static final String FULL_LINE = "fullLine";

	private static final String VISIBLE = "visible";

	private static final String FIELD = "field";

	private ReactControl _field;

	/**
	 * Creates a form field chrome wrapper.
	 *
	 * @param label
	 *        The field label text.
	 * @param field
	 *        The child field control to wrap.
	 */
	public ReactFormFieldChromeControl(String label, ReactControl field) {
		this(label, false, false, null, null, null, false, true, field);
	}

	/**
	 * Creates a form field chrome wrapper with full configuration.
	 *
	 * @param label
	 *        The field label text.
	 * @param required
	 *        Whether the field is required.
	 * @param dirty
	 *        Whether the field has been modified.
	 * @param error
	 *        Error message, or {@code null}.
	 * @param helpText
	 *        Help text, or {@code null}.
	 * @param labelPosition
	 *        "side", "top", or {@code null} to inherit from layout.
	 * @param fullLine
	 *        Whether the field spans the full grid row.
	 * @param visible
	 *        Whether the field is visible.
	 * @param field
	 *        The child field control.
	 */
	public ReactFormFieldChromeControl(String label, boolean required, boolean dirty,
			String error, String helpText, String labelPosition,
			boolean fullLine, boolean visible, ReactControl field) {
		super(null, REACT_MODULE);
		_field = field;
		putState(LABEL, label);
		putState(REQUIRED, required);
		putState(DIRTY, dirty);
		if (error != null) {
			putState(ERROR, error);
		}
		if (helpText != null) {
			putState(HELP_TEXT, helpText);
		}
		if (labelPosition != null) {
			putState(LABEL_POSITION, labelPosition);
		}
		putState(FULL_LINE, fullLine);
		putState(VISIBLE, visible);
		putState(FIELD, field);
	}

	/**
	 * Updates the error message.
	 *
	 * @param error
	 *        The error message, or {@code null} to clear.
	 */
	public void setError(String error) {
		putState(ERROR, error);
	}

	/**
	 * Updates the dirty state.
	 *
	 * @param dirty
	 *        Whether the field has been modified.
	 */
	public void setDirty(boolean dirty) {
		putState(DIRTY, dirty);
	}

	/**
	 * Updates visibility.
	 *
	 * @param visible
	 *        Whether the field is visible.
	 */
	public void setVisible(boolean visible) {
		putState(VISIBLE, visible);
	}

	/**
	 * Updates the required state.
	 *
	 * @param required
	 *        Whether the field is required.
	 */
	public void setRequired(boolean required) {
		putState(REQUIRED, required);
	}

	@Override
	protected void cleanupChildren() {
		if (_field != null) {
			_field.cleanupTree();
		}
	}

}
```

**Step 2: Commit**

```
Ticket #29109: Add ReactFormFieldChromeControl for form field anatomy chrome.
```

---

### Task 10: Create ReactFormGroupControl (Java)

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactFormGroupControl.java`

**Step 1: Implement the Java control**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactControl} that renders a nestable, optionally collapsible form section via the
 * {@code TLFormGroup} React component.
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code header} - group heading text, or {@code null}</li>
 * <li>{@code headerActions} - optional action buttons in the header</li>
 * <li>{@code collapsible} - whether the group can be collapsed</li>
 * <li>{@code collapsed} - current collapsed state</li>
 * <li>{@code border} - "none", "subtle", or "outlined"</li>
 * <li>{@code fullLine} - whether the group spans the full grid row</li>
 * <li>{@code children} - child controls</li>
 * </ul>
 */
public class ReactFormGroupControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormGroup";

	private static final String HEADER = "header";

	private static final String HEADER_ACTIONS = "headerActions";

	private static final String COLLAPSIBLE = "collapsible";

	private static final String COLLAPSED = "collapsed";

	private static final String BORDER = "border";

	private static final String FULL_LINE = "fullLine";

	private static final String CHILDREN = "children";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ToggleCollapseCommand());

	private final List<ReactControl> _children;

	private final List<ReactControl> _headerActions;

	private boolean _collapsed;

	/**
	 * Creates a form group with full configuration.
	 *
	 * @param header
	 *        The group heading, or {@code null}.
	 * @param collapsible
	 *        Whether the group can be collapsed.
	 * @param collapsed
	 *        Initial collapsed state.
	 * @param border
	 *        "none", "subtle", or "outlined".
	 * @param fullLine
	 *        Whether the group spans all parent columns.
	 * @param headerActions
	 *        Optional action buttons in the header.
	 * @param children
	 *        The child controls.
	 */
	public ReactFormGroupControl(String header, boolean collapsible, boolean collapsed,
			String border, boolean fullLine,
			List<? extends ReactControl> headerActions,
			List<? extends ReactControl> children) {
		super(null, REACT_MODULE, COMMANDS);
		_collapsed = collapsed;
		_children = new ArrayList<>(children);
		_headerActions = new ArrayList<>(headerActions);
		if (header != null) {
			putState(HEADER, header);
		}
		putState(COLLAPSIBLE, collapsible);
		putState(COLLAPSED, collapsed);
		putState(BORDER, border);
		putState(FULL_LINE, fullLine);
		putState(HEADER_ACTIONS, _headerActions);
		putState(CHILDREN, _children);
	}

	/**
	 * Creates a simple form group with a header and default settings.
	 *
	 * @param header
	 *        The group heading.
	 * @param children
	 *        The child controls.
	 */
	public ReactFormGroupControl(String header, List<? extends ReactControl> children) {
		this(header, false, false, "none", false, List.of(), children);
	}

	/**
	 * Toggles the collapsed state.
	 */
	public void toggleCollapsed() {
		_collapsed = !_collapsed;
		putState(COLLAPSED, _collapsed);
	}

	/**
	 * Whether the group is currently collapsed.
	 */
	public boolean isCollapsed() {
		return _collapsed;
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
		for (ReactControl action : _headerActions) {
			action.cleanupTree();
		}
	}

	/**
	 * Command sent when the user toggles the collapse state.
	 */
	public static class ToggleCollapseCommand extends ControlCommand {

		/** Creates a {@link ToggleCollapseCommand}. */
		public ToggleCollapseCommand() {
			super("toggleCollapse");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_FORM_GROUP_TOGGLE_COLLAPSE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			((ReactFormGroupControl) control).toggleCollapsed();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
```

**Step 2: Commit**

```
Ticket #29109: Add ReactFormGroupControl with collapse toggle command.
```

---

### Task 11: Create ReactFormLayoutControl (Java)

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactFormLayoutControl.java`

**Step 1: Implement the Java control**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

/**
 * A {@link ReactControl} that renders a responsive form grid via the {@code TLFormLayout} React
 * component.
 *
 * <p>
 * The layout uses CSS Grid with {@code auto-fit} to provide responsive columns up to
 * {@code maxColumns}. Label positioning can be fixed ("side", "top") or automatic ("auto"), where
 * the React component measures column width and switches between side-by-side and stacked labels.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code maxColumns} - maximum number of columns</li>
 * <li>{@code labelPosition} - "side", "top", or "auto"</li>
 * <li>{@code readOnly} - whether the form is read-only</li>
 * <li>{@code children} - child controls (TLFormGroup or TLFormField)</li>
 * </ul>
 */
public class ReactFormLayoutControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormLayout";

	private static final String MAX_COLUMNS = "maxColumns";

	private static final String LABEL_POSITION = "labelPosition";

	private static final String READ_ONLY = "readOnly";

	private static final String CHILDREN = "children";

	private final List<ReactControl> _children;

	/**
	 * Creates a form layout with full configuration.
	 *
	 * @param maxColumns
	 *        Maximum number of columns (e.g. 3).
	 * @param labelPosition
	 *        "side", "top", or "auto".
	 * @param readOnly
	 *        Whether the form is read-only.
	 * @param children
	 *        The child controls (TLFormGroup or TLFormField).
	 */
	public ReactFormLayoutControl(int maxColumns, String labelPosition, boolean readOnly,
			List<? extends ReactControl> children) {
		super(null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(MAX_COLUMNS, Integer.valueOf(maxColumns));
		putState(LABEL_POSITION, labelPosition);
		putState(READ_ONLY, readOnly);
		putState(CHILDREN, _children);
	}

	/**
	 * Creates a form layout with default settings (3 columns, auto labels, editable).
	 *
	 * @param children
	 *        The child controls.
	 */
	public ReactFormLayoutControl(List<? extends ReactControl> children) {
		this(3, "auto", false, children);
	}

	/**
	 * Updates the read-only state.
	 *
	 * @param readOnly
	 *        Whether the form is read-only.
	 */
	public void setReadOnly(boolean readOnly) {
		putState(READ_ONLY, readOnly);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
	}

}
```

**Step 2: Commit**

```
Ticket #29109: Add ReactFormLayoutControl with responsive grid configuration.
```

---

### Task 12: Build Java module and verify

**Step 1: Build the module**

```bash
cd com.top_logic.layout.react && mvn install -DskipTests=true
```

Expected: BUILD SUCCESS. No compilation errors.

**Step 2: Verify I18N messages were generated**

Check that the generated `messages_en.properties` contains the new `REACT_FORM_GROUP_TOGGLE_COLLAPSE` key.

**Step 3: Commit any generated files**

```
Ticket #29109: Rebuild com.top_logic.layout.react with form layout controls.
```
