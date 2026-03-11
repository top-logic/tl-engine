# ValueModel: Lightweight Form Value Binding

## Problem

`FormField` conflates two concerns:
1. **UI-level mechanics**: raw value parsing from HTTP requests, FormGroup hierarchy for layout, display state (label, tooltip, CSS)
2. **Value binding**: holding a typed value, dirty tracking, validation, edit lifecycle

React controls currently depend on the full FormField hierarchy via `ReactFormFieldControl`, inheriting all the UI baggage. A parallel set of `View*Control` classes was created in `com.top_logic.layout.view` to bypass FormField, but this duplicates control logic.

## Solution

Extract concern #2 into a `ValueModel` interface in tl-core. Refactor all React form controls to accept `ValueModel` instead of `FormField`. A `FormFieldValueModel` adapter lets existing FormField-based code work through the same controls. The duplicate `View*Control` classes are deleted.

## ValueModel Interface

Lives in `com.top_logic` (tl-core), package `com.top_logic.layout.form.model`. No dependencies on React, View, or FormField.

```java
public interface ValueModel {

    // --- Value ---
    Object getValue();
    void setValue(Object value);
    boolean isDirty();

    // --- Edit lifecycle ---
    boolean isEditable();

    // --- Validation ---
    boolean isMandatory();
    boolean hasError();
    ResKey getError();
    boolean hasWarnings();
    List<ResKey> getWarnings();
    void validate();

    // --- Constraints ---
    void addConstraint(ValueConstraint constraint);
    void removeConstraint(ValueConstraint constraint);

    // --- Listeners ---
    void addListener(ValueModelListener listener);
    void removeListener(ValueModelListener listener);
}
```

`isEditable()` subsumes the legacy `immutable`, `disabled`, `frozen`, and `blocked` states. Implementations compute editability from their underlying source. There is no separate `isDisabled()` — controls render non-editable state uniformly regardless of the reason.

Error and warning messages use `ResKey` for internationalization support. Resolution to display strings happens in the control/UI layer.

### ValueModelListener

Single unified listener:

```java
public interface ValueModelListener {
    void onValueChanged(ValueModel source, Object oldValue, Object newValue);
    void onEditabilityChanged(ValueModel source, boolean editable);
    void onValidationChanged(ValueModel source);
}
```

An `AbstractValueModelListener` provides empty default methods.

### ValueConstraint

```java
@FunctionalInterface
public interface ValueConstraint {
    /** Returns null if valid, error ResKey if invalid. */
    ResKey check(Object value);
}
```

## Sub-Interfaces

### SelectValueModel

For fields where the value must come from a defined set:

```java
public interface SelectValueModel extends ValueModel {
    List<?> getOptions();
    boolean isMultiple();
    void setOptions(List<?> options);
    void addOptionsListener(OptionsListener listener);
}
```

Options are plain objects. The control that consumes a `SelectValueModel` takes a `LabelProvider` as a separate constructor parameter for rendering option labels. This keeps value-level concerns (what options exist) separate from display concerns (how to label them).

### DataValueModel

For binary/file uploads:

```java
public interface DataValueModel extends ValueModel {
    String getAcceptedTypes();
    long getMaxUploadSize();
    boolean isMultipleFiles();
}
```

Both sub-interfaces live in the same package as `ValueModel` in tl-core.

## Implementations

### AbstractValueModel (tl-core)

Base class handling boilerplate:
- Listener management (add/remove/fire)
- Constraint list and `validate()` — iterates constraints, collects first error
- Dirty tracking via comparison with stored default value
- `_editable` field with `setEditable(boolean)` that fires `onEditabilityChanged`

### SimpleValueModel (tl-core)

Standalone for non-model scenarios (settings, preferences):
- Holds its own value in a field
- Full dirty tracking against default value
- No overlay, no FormField, no model dependency

### FormFieldValueModel (tl-core)

Adapter wrapping an existing `FormField`:
- `getValue()` delegates to `field.getValue()`
- `setValue(Object)` delegates to `field.setValue(value)`
- `isDirty()` delegates to `field.isChanged()`
- `isEditable()` computes from `!field.isImmutable() && !field.isDisabled()`
- `isMandatory()` delegates to `field.isMandatory()`
- `hasError()` delegates to `field.hasError()`
- `getError()` delegates to `field.getError()` (already returns `ResKey`)
- Registers FormField property listeners internally and translates to `ValueModelListener` calls
- Enables existing FormField-based code to work through ValueModel-based controls

Also provides label, tooltip, and visibility from the wrapped `FormField` via accessor methods (not part of `ValueModel` interface — these are additional methods on `FormFieldValueModel` used by `ReactFormFieldControl` for display properties).

### OverlayValueModel (com.top_logic.layout.view.form)

Main implementation for model editing in the view system:
- Constructor takes `TLObjectOverlay` + `TLStructuredTypePart`
- `getValue()` reads `overlay.tValue(part)`
- `setValue(Object)` writes `overlay.tUpdate(part, value)` and fires listeners
- `isDirty()` reads `overlay.isChanged(part)`
- Editable state set by `FieldControl` when the form toggles edit mode (calls `setEditable(boolean)` inherited from `AbstractValueModel`)
- Supports `setOverlay(TLObjectOverlay)` for object switching — when the selected object changes, `FieldControl` calls this to re-bind to the new overlay and re-resolves the `TLStructuredTypePart`. Fires `onValueChanged` so the control updates.

## Control Refactoring

### ReactFormFieldControl Base

Refactored to take `ValueModel` instead of `FormField`. Constructor signature:

```java
protected ReactFormFieldControl(ReactContext context, ValueModel model, String reactModule)
```

The `reactModule` string remains determined by each subclass (e.g., `"TLTextInput"`, `"TLCheckbox"`).

Reads value/editable/mandatory/error from the model, listens for changes via `ValueModelListener`, sends SSE patches to the React component.

When the client sends a `valueChanged` command, the control calls `model.setValue(parsedValue)`. The model handles dirty tracking and fires listeners. No separate callback interface needed.

**Display properties (label, tooltip, hidden):** These are NOT part of `ValueModel`. When the model is a `FormFieldValueModel`, the control reads label/tooltip/visibility from the adapter's additional methods. In the view system, these are handled by `ReactFormFieldChromeControl` which wraps the input control with label, error display, etc. `ReactFormFieldControl` itself provides hooks (`getLabel()`, `getTooltip()`, `isHidden()`) that can be overridden or set by the call site. Default behavior: no label, no tooltip, visible.

### Control Classes (all in com.top_logic.layout.react)

All controls take `ReactContext` + `ValueModel` in constructor:
- `ReactTextInputControl(ReactContext, ValueModel)`
- `ReactCheckboxControl(ReactContext, ValueModel)`
- `ReactNumberInputControl(ReactContext, ValueModel)`
- `ReactDatePickerControl(ReactContext, ValueModel)`
- `ReactSelectFormFieldControl(ReactContext, SelectValueModel, LabelProvider)`
- `ReactColorInputControl(ReactContext, ValueModel, ColorPaletteConfig)` — see below

Call sites that currently pass `FormField` wrap it in `FormFieldValueModel`.

### ReactColorInputControl

Absorbs palette logic from `ViewColorInputControl`. The color value flows through `ValueModel` (hex string or `java.awt.Color`). Palette management (personal palette, default palette, palette columns, canReset) is a control-level concern, passed via a `ColorPaletteConfig` parameter:

```java
public class ColorPaletteConfig {
    List<String> palette;
    List<String> defaultPalette;
    int paletteColumns;
    boolean canReset;
}
```

The control loads/saves the personal palette via `PersonalConfiguration` as `ViewColorInputControl` does today.

### View*Controls Deleted

`ViewTextInputControl`, `ViewCheckboxControl`, `ViewNumberInputControl`, `ViewDatePickerControl`, `ViewSelectControl`, `ViewColorInputControl` are all deleted from `com.top_logic.layout.view`. The view module's `FieldControlFactory` creates react module controls with `OverlayValueModel`.

## FieldControl Lifecycle

`FieldControl` (in `com.top_logic.layout.view.form`) manages the per-field lifecycle:

**Initial creation:** Creates an `OverlayValueModel` for the attribute and passes it to the control constructor. The model and control are created once.

**Edit mode toggle:** `FormControl` calls `FieldControl.setEditMode(boolean)`, which calls `model.setEditable(editable)`. The model fires `onEditabilityChanged`, the control receives it and sends an SSE patch. No manual refresh needed.

**Object switching:** When the input channel changes, `FormControl` creates a new `TLObjectOverlay` and calls `FieldControl.setOverlay(newOverlay)`. The `FieldControl` calls `model.setOverlay(newOverlay)` on the `OverlayValueModel`, which re-resolves the `TLStructuredTypePart` from the new object's type and fires `onValueChanged` with the new value. If the attribute does not exist on the new type, the field becomes hidden.

**Dirty tracking:** The `OverlayValueModel` fires `onValueChanged` when the user edits. `FormControl` listens to each model and recomputes aggregate dirty state.

## FormControl Integration

`FormControl` manages the editing lifecycle. Changes minimally:
- `enterEditMode()` iterates field controls and calls `setEditMode(true)`
- `exitEditMode()` iterates field controls and calls `setEditMode(false)`
- The ValueModel fires `onEditabilityChanged`, controls receive it via SSE
- Dirty channel driven by `onValueChanged` events from ValueModels
- FormControl does not know about controls — it only talks to FieldControls and ValueModels

## Threading Model

`ValueModel` instances are session-scoped and accessed from a single thread at a time, consistent with the existing control threading model. No concurrent access is expected.

## Module Dependencies

```
tl-core (ValueModel, FormFieldValueModel, AbstractValueModel, SimpleValueModel)
  |
com.top_logic.layout.react (ReactFormFieldControl, all control subclasses)
  |
com.top_logic.layout.view (OverlayValueModel, FieldControlFactory, FormControl, elements)
```

## Migration Plan

### Phase 1: ValueModel interfaces + base classes (tl-core)

- `ValueModel`, `SelectValueModel`, `DataValueModel` interfaces
- `ValueModelListener`, `AbstractValueModelListener`, `ValueConstraint`
- `AbstractValueModel`, `SimpleValueModel`
- `FormFieldValueModel` adapter

### Phase 2: Refactor React controls (com.top_logic.layout.react)

- `ReactFormFieldControl` base refactored to take `ValueModel`
- All subclasses updated with `(ReactContext, ValueModel)` constructors
- `ReactColorInputControl` created, absorbing palette logic from `ViewColorInputControl`
- All call sites that pass `FormField` wrap in `FormFieldValueModel`

### Phase 3: View module cleanup (com.top_logic.layout.view)

- Delete all `View*Control` classes
- `FieldControlFactory` creates react module controls with `OverlayValueModel`
- `FieldControl` simplified — creates model + control, no manual sync
- `FormControl` talks to FieldControls/ValueModels for editability and dirty tracking

### Phase 4: Cleanup

- Delete `ValueCallback` interface
- Delete manual `refresh()` / `updateInnerControl()` plumbing
- Delete `ViewFieldValueChanged` command class

## Properties NOT in ValueModel

These stay in FormField / UI layer:
- `rawValue` / `parseRawValue()` — HTTP-era artifact
- `name`, `parent`, `label`, `tooltip`, `cssClass` — pure UI/display
- `FormGroup` membership — layout hierarchy
- `frozen` / `blocked` / `disabled` / `immutable` — subsumed by `isEditable()`
- `placeholder`, `exampleValue` — display hints
- Validation state machine (INITIAL/ILLEGAL_INPUT/WAIT/VALID) — parsing pipeline
