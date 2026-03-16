# React Field Error & Warning States

## Problem

React input controls (TLTextInput, TLNumberInput, TLDatePicker, TLSelect, TLCheckbox) have no visual feedback for validation errors or warnings. When the server rejects a value (e.g. "foo" in a number field), the user sees no indication that something is wrong.

## Architecture

Each form field has two separate React controls with independent state stores:

1. **Input Control** (`ReactFormFieldControl`) — renders the actual input element
2. **Chrome Control** (`ReactFormFieldChromeControl`) — renders label, error/warning messages, help text

Both listen to the same `FieldModel` and receive state patches independently via SSE.

## Data Flow

```
User types invalid value
  → valueChanged command sent to server
  → Server parses/validates via FieldModel
  → FieldModel.validate() sets error/warning state
  → FieldModelListener.onValidationChanged() fires
  → ReactFormFieldControl sends:  hasError (bool), hasWarnings (bool)
  → ReactFormFieldChromeControl sends: error (string|null), warnings (string[]|null)
  → React components re-render with updated state
```

## Server Changes

### ReactFormFieldControl.java

Add `hasWarnings` boolean to state, alongside existing `hasError`:

- **State keys**: `HAS_ERROR` (existing), `HAS_WARNINGS` (new)
- **`initFieldState()`**: Add `putState(HAS_WARNINGS, _fieldModel.hasWarnings())`
- **`onValidationChanged()`**: Add `putState(HAS_WARNINGS, source.hasWarnings())`

The `errorMessage` key is already sent but not needed by input controls (the chrome handles message display). It can remain for backwards compatibility but input controls only read the boolean flags.

### ReactFormFieldChromeControl.java

Add warnings support alongside existing error:

- **State key**: `WARNINGS` (new, `List<String>`)
- Resolve `FieldModel.getWarnings()` (list of `ResKey`) to localized strings
- Send as JSON array in state

## Client Changes

### Input Controls (TLTextInput, TLNumberInput, TLDatePicker, TLSelect)

Read `state.hasError` and `state.hasWarnings` from state. Apply CSS modifier classes:

```tsx
const hasError = state.hasError === true;
const hasWarnings = state.hasWarnings === true;

const cls = [
  'tlReactTextInput',
  hasError ? 'tlReactTextInput--error' : '',
  !hasError && hasWarnings ? 'tlReactTextInput--warning' : '',
].filter(Boolean).join(' ');
```

Error takes precedence over warning (both visually and in class assignment).

### TLCheckbox

Same logic with `tlReactCheckbox--error` / `tlReactCheckbox--warning`.

### TLFormField.tsx (Chrome)

Extend to display warnings in addition to errors:

- Read `state.warnings` as `string[] | null`
- Show warnings below the field, styled with warning icon and `--support-warning` color
- Error messages take visual precedence: if both error and warnings exist, show error only
- Warning rendering mirrors error rendering (icon + text), but with warning styling

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

/* Checkbox variants */
.tlReactCheckbox--error { accent-color: var(--support-error); }
.tlReactCheckbox--warning { accent-color: var(--support-warning); }
```

#### Chrome warning display

```css
.tlFormField__warnings {
  /* Same layout as .tlFormField__error but warning colors */
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-02);
  color: var(--support-warning, #f1c21b);
  font-size: var(--body-compact-01-font-size);
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

## Not In Scope

- Client-side validation (all validation comes from server)
- Inline error messages on input controls (text display is chrome-only)
- Animated transitions for error/warning appearance
