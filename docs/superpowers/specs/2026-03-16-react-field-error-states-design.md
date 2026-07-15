# React Field Error & Warning States

## Problem

React input controls (TLTextInput, TLNumberInput, TLDatePicker, TLSelect, TLCheckbox) have no visual feedback for validation errors or warnings. When the server rejects a value (e.g. "foo" in a number field), the user sees no indication that something is wrong.

## Architecture

Each form field has two separate React controls with independent state stores:

1. **Input Control** (`ReactFormFieldControl`) — renders the actual input element
2. **Chrome Control** (`ReactFormFieldChromeControl`) — renders label, error/warning messages, help text

Both are created by `AttributeFieldControl`, which also owns the `FieldModelListener`. Currently, `onValidationChanged()` in `AttributeFieldControl` is a no-op ("handled by ReactFormFieldControl"). The input control pushes `hasError`/`errorMessage` to its own state via its own listener, but the chrome control receives **no** validation updates at all.

## Data Flow

```
User types invalid value
  → valueChanged command sent to server
  → Server parses/validates via FieldModel
  → FieldModel.validate() sets error/warning state
  → Two parallel update paths:

  Path 1 — Input Control (ReactFormFieldControl's own FieldModelListener):
    → putState(HAS_ERROR, bool)
    → putState(HAS_WARNINGS, bool)  [NEW]
    → SSE patch → React input re-renders with error/warning border

  Path 2 — Chrome Control (via AttributeFieldControl's FieldModelListener):
    → AttributeFieldControl.onValidationChanged()  [CURRENTLY NO-OP]
    → calls _chrome.setError(resolvedErrorString)   [NEW]
    → calls _chrome.setWarnings(resolvedWarningStrings)  [NEW]
    → SSE patch → React chrome re-renders with error/warning text
```

## Server Changes

### ReactFormFieldControl.java

Add `HAS_WARNINGS` boolean to state, alongside existing `HAS_ERROR`:

- **New state key**: `protected static final String HAS_WARNINGS = "hasWarnings";`
- **`initFieldState()`**: Add `putState(HAS_WARNINGS, _fieldModel.hasWarnings())`
- **`onValidationChanged()`**: Add `putState(HAS_WARNINGS, source.hasWarnings())`

The existing `ERROR_MESSAGE` key remains for backwards compatibility but is not used by the input controls (the chrome handles message display).

### ReactFormFieldChromeControl.java

Add warnings support alongside existing error:

- **New state key**: `private static final String WARNINGS = "warnings";`
- **New setter**: `setWarnings(java.util.List<String> warnings)` — calls `putState(WARNINGS, warnings)`
- Constructor: No change needed. Warnings are dynamic state set after validation, not initial configuration.

### AttributeFieldControl.java

Wire `onValidationChanged()` to push error/warning state to the chrome control:

```java
@Override
public void onValidationChanged(FieldModel source) {
    if (_chrome == null) {
        return;
    }
    // Error
    if (source.hasError()) {
        _chrome.setError(Resources.getInstance().getString(source.getError()));
    } else {
        _chrome.setError(null);
    }
    // Warnings
    if (source.hasWarnings()) {
        List<String> msgs = source.getWarnings().stream()
            .map(key -> Resources.getInstance().getString(key))
            .collect(Collectors.toList());
        _chrome.setWarnings(msgs);
    } else {
        _chrome.setWarnings(null);
    }
    // Mandatory may change with validation
    _chrome.setRequired(source.isMandatory());
}
```

## Client Changes

### Input Controls (TLTextInput, TLNumberInput, TLDatePicker, TLSelect)

Read `state.hasError` and `state.hasWarnings`. Apply CSS modifier classes:

```tsx
const hasError = state.hasError === true;
const hasWarnings = state.hasWarnings === true;

const cls = [
  'tlReactTextInput',
  hasError ? 'tlReactTextInput--error' : '',
  !hasError && hasWarnings ? 'tlReactTextInput--warning' : '',
].filter(Boolean).join(' ');
```

Error takes precedence over warning. Add `aria-invalid="true"` when `hasError`.

### TLCheckbox

Same logic with `tlReactCheckbox--error` / `tlReactCheckbox--warning`. Note: `accent-color` rendering varies by browser; this is an acceptable best-effort visual hint for checkboxes.

### TLFormField.tsx (Chrome)

Extend to display warnings in addition to errors:

- Read `state.warnings` as `string[] | null`
- If error exists, show error only (error takes precedence)
- If no error but warnings exist, show warnings
- Each warning rendered as its own line (same layout as error: icon + text)
- Warning icon: yellow triangle (same SVG shape as error icon, different color)
- Add `tlFormField--warning` CSS class alongside existing `tlFormField--error`
- Add `role="alert"` to error container, `aria-live="polite"` to warning container

### CSS (tlReactControls.css)

#### Input error/warning borders

```css
/* Error state — red border, persists through focus */
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
}

/* Warning state — yellow/orange border */
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
}

/* Checkbox variants — best-effort, varies by browser */
.tlReactCheckbox--error { accent-color: var(--support-error); }
.tlReactCheckbox--warning { accent-color: var(--support-warning); }
```

#### Chrome warning display

```css
.tlFormField--warning { }  /* Modifier class for warning state on chrome wrapper */

.tlFormField__warnings {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-01);
  font-size: var(--body-compact-01-font-size);
}

.tlFormField__warning {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-02);
  color: var(--support-warning, #f1c21b);
}
```

## Visual Priority

| State | Border Color | Chrome Message |
|-------|-------------|----------------|
| Normal | `--border-subtle` (gray) | none |
| Focus | `--focus` (blue) | none |
| Warning | `--support-warning` (yellow) | warning text(s) |
| Error | `--support-error` (red) | error text |
| Error + Focus | `--support-error` (red) | error text |
| Error + Warning | `--support-error` (red) | error text only |

## Scope

### In scope
- TLTextInput, TLNumberInput, TLDatePicker, TLSelect, TLCheckbox
- TLFormField chrome (error + warning text display)
- Server wiring in ReactFormFieldControl, ReactFormFieldChromeControl, AttributeFieldControl

### Out of scope
- TLColorInput, TLDropdownSelect, TLIconSelect, TLFileUpload — these have their own error handling patterns and can be addressed separately
- Client-side validation (all validation comes from server)
- Animated transitions for error/warning appearance
