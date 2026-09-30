# React Field Error & Warning States — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add visual error/warning feedback (colored borders on inputs, text messages in chrome) to React form field controls.

**Architecture:** Two parallel update paths — the input control reads boolean flags (`hasError`, `hasWarnings`) for border styling, while the chrome control receives resolved message strings for text display. Server-side `AttributeFieldControl` wires `FieldModel` validation changes to both controls.

**Tech Stack:** Java 17 (server), TypeScript/React (client via tl-react-bridge), CSS custom properties

**Spec:** `docs/superpowers/specs/2026-03-16-react-field-error-states-design.md`

---

## Chunk 1: Server-Side Wiring

### Task 1: Add `HAS_WARNINGS` to ReactFormFieldControl

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactFormFieldControl.java`

- [ ] **Step 1: Add HAS_WARNINGS state key constant**

After line 47 (`ERROR_MESSAGE`), add:

```java
/** State key for whether the field has validation warnings. */
protected static final String HAS_WARNINGS = "hasWarnings";
```

- [ ] **Step 2: Send hasWarnings in initFieldState()**

After line 99 (`putState(HAS_ERROR, ...)`), add:

```java
putState(HAS_WARNINGS, _fieldModel.hasWarnings());
```

- [ ] **Step 3: Send hasWarnings in onValidationChanged()**

In the `onValidationChanged` method (line 129), after the existing `HAS_ERROR`/`ERROR_MESSAGE` block and before `putState(MANDATORY, ...)`, add:

```java
putState(HAS_WARNINGS, source.hasWarnings());
```

- [ ] **Step 4: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```
Ticket #29108: Send hasWarnings state from ReactFormFieldControl.
```

---

### Task 2: Add warnings setter to ReactFormFieldChromeControl

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactFormFieldChromeControl.java`

- [ ] **Step 1: Add WARNINGS state key constant**

After line 39 (`ERROR`), add:

```java
private static final String WARNINGS = "warnings";
```

- [ ] **Step 2: Add setWarnings() method**

After the `setError()` method (line 139), add:

```java
/**
 * Updates the warning messages.
 *
 * @param warnings
 *        The warning messages, or {@code null} to clear.
 */
public void setWarnings(java.util.List<String> warnings) {
    putState(WARNINGS, warnings);
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Add setWarnings() to ReactFormFieldChromeControl.
```

---

### Task 3: Wire onValidationChanged in AttributeFieldControl

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java`

- [ ] **Step 1: Add imports**

Add at the top of the file:

```java
import java.util.List;
import java.util.stream.Collectors;
```

- [ ] **Step 2: Implement onValidationChanged()**

Replace the no-op `onValidationChanged` body (lines 192-193) with:

```java
@Override
public void onValidationChanged(FieldModel source) {
    if (_chrome == null) {
        return;
    }
    if (source.hasError()) {
        _chrome.setError(Resources.getInstance().getString(source.getError()));
    } else {
        _chrome.setError(null);
    }
    if (source.hasWarnings()) {
        List<String> msgs = source.getWarnings().stream()
            .map(key -> Resources.getInstance().getString(key))
            .collect(Collectors.toList());
        _chrome.setWarnings(msgs);
    } else {
        _chrome.setWarnings(null);
    }
    _chrome.setRequired(source.isMandatory());
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Wire validation changes to chrome control in AttributeFieldControl.
```

---

## Chunk 2: Client-Side Input Controls

### Task 4: Add error/warning CSS classes for inputs

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

- [ ] **Step 1: Add error/warning styles after the checkbox block**

Insert after line 2592 (`.tlReactCheckbox--immutable` closing brace), before the `TLDropdownSelect` section:

```css
/* --- Error / Warning states for simple inputs ----------------------------- */

/* Error — red border, persists through focus */
.tlReactTextInput--error,
.tlReactNumberInput--error,
.tlReactDatePicker--error,
.tlReactSelect--error {
	border-color: var(--support-error, #da1e28);
}

.tlReactTextInput--error:focus,
.tlReactNumberInput--error:focus,
.tlReactDatePicker--error:focus,
.tlReactSelect--error:focus {
	outline-color: var(--support-error, #da1e28);
	border-color: var(--support-error, #da1e28);
}

/* Warning — yellow border, only when no error */
.tlReactTextInput--warning,
.tlReactNumberInput--warning,
.tlReactDatePicker--warning,
.tlReactSelect--warning {
	border-color: var(--support-warning, #f1c21b);
}

.tlReactTextInput--warning:focus,
.tlReactNumberInput--warning:focus,
.tlReactDatePicker--warning:focus,
.tlReactSelect--warning:focus {
	outline-color: var(--support-warning, #f1c21b);
	border-color: var(--support-warning, #f1c21b);
}

/* Checkbox error/warning — best-effort via accent-color */
.tlReactCheckbox--error {
	accent-color: var(--support-error, #da1e28);
}

.tlReactCheckbox--warning {
	accent-color: var(--support-warning, #f1c21b);
}
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add error/warning CSS for simple React input controls.
```

---

### Task 5: Update TLTextInput to show error/warning state

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLTextInput.tsx`

- [ ] **Step 1: Read state and build className**

Replace the component body. The full updated file:

```tsx
import { React, useTLFieldValue, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A text input field rendered via React.
 */
const TLTextInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value);
    },
    [setValue]
  );

  if (state.editable === false) {
    return (
      <span id={controlId} className="tlReactTextInput tlReactTextInput--immutable">
        {(value as string) ?? ''}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactTextInput',
    hasError ? 'tlReactTextInput--error' : '',
    !hasError && hasWarnings ? 'tlReactTextInput--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <input
        type="text"
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
      />
    </span>
  );
};

export default TLTextInput;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add error/warning visual state to TLTextInput.
```

---

### Task 6: Update TLNumberInput to show error/warning state

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLNumberInput.tsx`

- [ ] **Step 1: Update the component**

Full updated file:

```tsx
import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A number input field rendered via React.
 */
const TLNumberInput: React.FC<TLCellProps> = ({ controlId, state, config }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const raw = e.target.value;
      const parsed = raw === '' ? null : Number(raw);
      setValue(parsed);
    },
    [setValue]
  );

  const step = config?.decimal ? '0.01' : '1';

  if (state.editable === false) {
    return (
      <span id={controlId} className="tlReactNumberInput tlReactNumberInput--immutable">
        {value != null ? String(value) : ''}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactNumberInput',
    hasError ? 'tlReactNumberInput--error' : '',
    !hasError && hasWarnings ? 'tlReactNumberInput--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <input
        type="number"
        value={value != null ? String(value) : ''}
        onChange={handleChange}
        step={step}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
      />
    </span>
  );
};

export default TLNumberInput;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add error/warning visual state to TLNumberInput.
```

---

### Task 7: Update TLDatePicker to show error/warning state

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLDatePicker.tsx`

- [ ] **Step 1: Update the component**

Full updated file:

```tsx
import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A date picker field rendered via React.
 */
const TLDatePicker: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  if (state.editable === false) {
    return (
      <span id={controlId} className="tlReactDatePicker tlReactDatePicker--immutable">
        {(value as string) ?? ''}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactDatePicker',
    hasError ? 'tlReactDatePicker--error' : '',
    !hasError && hasWarnings ? 'tlReactDatePicker--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <input
        type="date"
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
      />
    </span>
  );
};

export default TLDatePicker;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add error/warning visual state to TLDatePicker.
```

---

### Task 8: Update TLSelect to show error/warning state

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLSelect.tsx`

- [ ] **Step 1: Update the component**

Full updated file:

```tsx
import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

interface SelectOption {
  value: string;
  label: string;
}

/**
 * A select dropdown rendered via React.
 */
const TLSelect: React.FC<TLCellProps> = ({ controlId, state, config }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLSelectElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  const options = ((state.options ?? config?.options) as SelectOption[]) ?? [];

  if (state.editable === false) {
    const selectedLabel = options.find((opt) => opt.value === value)?.label ?? '';
    return (
      <span id={controlId} className="tlReactSelect tlReactSelect--immutable">
        {selectedLabel}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactSelect',
    hasError ? 'tlReactSelect--error' : '',
    !hasError && hasWarnings ? 'tlReactSelect--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <select
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
      >
        <option value=""></option>
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </span>
  );
};

export default TLSelect;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add error/warning visual state to TLSelect.
```

---

### Task 9: Update TLCheckbox to show error/warning state

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLCheckbox.tsx`

- [ ] **Step 1: Update the component**

Full updated file:

```tsx
import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A checkbox field rendered via React.
 */
const TLCheckbox: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.checked);
    },
    [setValue]
  );

  if (state.editable === false) {
    return (
      <input
        type="checkbox"
        id={controlId}
        checked={value === true}
        disabled
        className="tlReactCheckbox tlReactCheckbox--immutable"
      />
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactCheckbox',
    hasError ? 'tlReactCheckbox--error' : '',
    !hasError && hasWarnings ? 'tlReactCheckbox--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <input
      type="checkbox"
      id={controlId}
      checked={value === true}
      onChange={handleChange}
      disabled={state.disabled === true}
      className={cls}
      aria-invalid={hasError || undefined}
    />
  );
};

export default TLCheckbox;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add error/warning visual state to TLCheckbox.
```

---

## Chunk 3: Chrome Warning Display

### Task 10: Add warning display to TLFormField.tsx

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLFormField.tsx`

- [ ] **Step 1: Update the component to read and display warnings**

Full updated file:

```tsx
import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FormLayoutContext } from './FormLayoutContext';

const { useContext, useState, useCallback } = React;

/**
 * Form field chrome wrapper that renders label, required indicator,
 * help icon, error message, warning messages, help text, and dirty
 * indicator around any field input control.
 *
 * State:
 * - label: string
 * - required: boolean
 * - error: string | null
 * - warnings: string[] | null
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
  const warnings = state.warnings as string[] | null;
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
  const hasWarnings = warnings != null && warnings.length > 0;

  const className = [
    'tlFormField',
    `tlFormField--${labelPos}`,
    readOnly ? 'tlFormField--readonly' : '',
    fullLine ? 'tlFormField--fullLine' : '',
    hasError ? 'tlFormField--error' : '',
    !hasError && hasWarnings ? 'tlFormField--warning' : '',
    dirty ? 'tlFormField--dirty' : '',
  ].filter(Boolean).join(' ');

  return (
    <div id={controlId} className={className}>
      <div className="tlFormField__label">
        <span className="tlFormField__labelText">{label}</span>
        {required && !readOnly && <span className="tlFormField__required">*</span>}
        {dirty && <span className="tlFormField__dirtyDot" />}
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
        <div className="tlFormField__error" role="alert">
          <svg className="tlFormField__errorIcon" viewBox="0 0 16 16" width="14" height="14"
            aria-hidden="true">
            <path d="M8 1l7 14H1L8 1z" fill="none" stroke="currentColor" strokeWidth="1.2" />
            <line x1="8" y1="6" x2="8" y2="10" stroke="currentColor" strokeWidth="1.2" />
            <circle cx="8" cy="12" r="0.8" fill="currentColor" />
          </svg>
          <span>{error}</span>
        </div>
      )}
      {!readOnly && !hasError && hasWarnings && (
        <div className="tlFormField__warnings" aria-live="polite">
          {warnings.map((msg, i) => (
            <div key={i} className="tlFormField__warning">
              <svg className="tlFormField__warningIcon" viewBox="0 0 16 16" width="14" height="14"
                aria-hidden="true">
                <path d="M8 1l7 14H1L8 1z" fill="none" stroke="currentColor" strokeWidth="1.2" />
                <line x1="8" y1="6" x2="8" y2="10" stroke="currentColor" strokeWidth="1.2" />
                <circle cx="8" cy="12" r="0.8" fill="currentColor" />
              </svg>
              <span>{msg}</span>
            </div>
          ))}
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

- [ ] **Step 2: Commit**

```
Ticket #29108: Add warning display to TLFormField chrome component.
```

---

### Task 11: Add warning CSS to chrome styles

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

- [ ] **Step 1: Add warning styles after the error area block**

Insert after the `.tlFormField__errorIcon` rule (line 2374), before the help text block:

```css
/* Warning area */
.tlFormField__warnings {
	display: flex;
	flex-direction: column;
	gap: var(--spacing-01, 0.125rem);
}

.tlFormField__warning {
	display: flex;
	align-items: flex-start;
	gap: var(--spacing-01, 0.125rem);
	color: var(--support-warning, #f1c21b);
	font-size: var(--body-compact-01-font-size, 0.875rem);
	line-height: var(--body-compact-01-line-height, 1.125rem);
}

.tlFormField__warningIcon {
	flex-shrink: 0;
	margin-top: 1px;
}
```

- [ ] **Step 2: Add grid placement for warnings in side layout**

Check if `.tlFormField--side .tlFormField__error` has grid placement. If so, add the same for warnings. The error block sits in the input column (grid-column 2) in side layout. Add after the warning styles:

```css
.tlFormField--side .tlFormField__warnings {
	grid-column: 2;
}
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Add warning CSS styles for TLFormField chrome.
```

---

## Chunk 4: Build & Verify

### Task 12: Build and verify everything compiles

- [ ] **Step 1: Build the layout.react module (includes JS/TS via frontend-maven-plugin)**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS (Java + TypeScript compilation)

- [ ] **Step 2: Build the layout.view module**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 3: Verify no TypeScript errors**

Check the build output for any `tsc` or `vite` errors in the frontend-maven-plugin section.

- [ ] **Step 4: Final commit if any fixups needed**

```
Ticket #29108: Fix build issues for error/warning states.
```
