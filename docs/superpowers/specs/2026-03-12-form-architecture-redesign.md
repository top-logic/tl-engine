# Form Architecture Redesign

## Problem

The current form editing lifecycle has several architectural issues:

1. **Asymmetric enter/exit**: `enterEditMode()` calls both `setEditMode()` and `setOverlay()` on
   fields, but `exitEditMode()` only calls `setEditMode()`. This causes the dirty indicator to
   persist after save because the field's chrome never gets told that dirty is now false.

2. **Form pushes state imperatively**: `FormControl` iterates over `_fieldControls` and calls
   different methods depending on the scenario (`setEditMode`, `setOverlay`, `refresh`). Each new
   scenario risks forgetting a call.

3. **Fields don't own their lifecycle**: Fields are passive recipients of state pushed by the form.
   They have no way to pull state or react uniformly to changes.

4. **OverlayFieldModel conflates concerns**: It delegates `isDirty()` to the overlay instead of
   tracking its own dirty state. It stores `_attributeName` separately from `_part`. It's named
   "overlay" but just wraps a `TLObject`.

5. **ViewContext exposes concrete FormControl**: Inner elements depend on the concrete class instead
   of an interface.

## Design

### FormModel interface

A new `FormModel` interface replaces the direct `FormControl` dependency. Fields depend only on
this interface.

```java
public interface FormModel {
    /** The current object (base TLObject in view mode, TLObjectOverlay in edit mode). */
    TLObject getCurrentObject();

    /** Whether the form is in edit mode. */
    boolean isEditMode();

    /** Register a listener for form state changes. */
    void addFormModelListener(FormModelListener listener);

    /** Remove a listener. */
    void removeFormModelListener(FormModelListener listener);
}

public interface FormModelListener {
    /** Called when the form's state changes (object switched, mode changed, overlay reset). */
    void onFormStateChanged(FormModel source);
}
```

`FormControl` implements `FormModel`. `ViewContext.getFormControl()` becomes
`ViewContext.getFormModel()` returning the interface.

### AttributeFieldModel (renamed from OverlayFieldModel)

The field model simplifies to a plain attribute-to-TLObject binding:

- **`_overlay` -> `_object`**: Just a `TLObject` (could be base or overlay).
- **No separate `_attributeName`**: Stored as `_part`, resolved once from the object's type.
- **`isDirty()` not overridden**: `AbstractFieldModel` tracks dirty state itself via
  `setValue()`/`resetDirty()`.
- **`setOverlay()` -> `setObject()`**: Rebinds to a new object, calls `resetDirty()`, fires value
  changed if the value differs.

### AttributeFieldControl (renamed from FieldControl)

The field control becomes a `FormModelListener` that pulls state when notified:

- **Implements `FormModelListener`**: Replaces the three separate methods `setEditMode()`,
  `setOverlay()`, `refresh()` with a single `onFormStateChanged()` callback.
- **Pulls state from FormModel**: Asks `getCurrentObject()` and `isEditMode()` instead of being
  told.
- **Self-registers** as listener in constructor.
- In `onFormStateChanged()`: calls `_model.setObject(source.getCurrentObject())`, updates
  editability, and syncs `_chrome.setDirty(_model.isDirty())`.

### Simplified FormControl

Every state transition becomes the same pattern: update internal state, then
`fireFormStateChanged()`.

- `enterEditMode()`: create overlay, set `_editMode = true`, fire.
- `exitEditMode()`: null overlay, set `_editMode = false`, release lock, fire.
- `executeApply()`: apply + reset overlay, fire.
- `handleInputChanged()`: exit edit mode if needed, update `_currentObject`, fire.

No more `_fieldControls` list or `registerFieldControl()`. Fields register themselves via
`addFormModelListener()`.

### How the dirty bug is fixed

`exitEditMode()` fires `onFormStateChanged()`. The field calls
`_model.setObject(baseObject)` which calls `resetDirty()`. Then
`_chrome.setDirty(_model.isDirty())` pushes `false` to React. The dirty dot disappears.

## Renames

| Current | New |
|---------|-----|
| `FieldControl` | `AttributeFieldControl` |
| `OverlayFieldModel` | `AttributeFieldModel` |
| `OverlayFieldModel._overlay` | `AttributeFieldModel._object` |
| `OverlayFieldModel.setOverlay()` | `AttributeFieldModel.setObject()` |
| `ViewContext.getFormControl()` | `ViewContext.getFormModel()` |
