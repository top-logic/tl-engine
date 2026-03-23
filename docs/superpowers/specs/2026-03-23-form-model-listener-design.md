# Form ModelListener Design

## Problem

Tables and trees in the View/React system already react to persistent object changes via `ModelListener`/`ModelScope` (see `ObservableTableModel`). Forms do not — when the currently displayed object is updated or deleted externally (by another user, another component, or a background process), form fields remain stale.

## Goal

`FormControl` reacts to persistent object changes (updates and deletes) of the currently displayed object, keeping form fields in sync with the knowledge base.

## Scope

- **Updates** in view mode: field values refresh automatically.
- **Updates** in edit mode: no action (overlay buffers local changes; new base values become visible after save/cancel).
- **Deletes**: exit edit mode if active, clear current object, show no-model message.
- **Transient objects**: no listener registration (checked via `TLObject.tTransient()`).

## Design

### FormControl implements ModelListener

`FormControl` implements `ModelListener` directly. It is the central state manager of the form and already holds all required references (`_currentObject`, `_overlay`, `fireFormStateChanged()`). A separate companion class would add indirection without benefit, since a form always observes exactly one object (unlike a table with many rows).

### ModelScope Access

`FormControl` receives a `ModelScope` via an `attach(ModelScope)`/`detach()` pair, called by `FormElement` during control creation and cleanup. This follows the same pattern as `ObservableTableModel`.

### Listener Registration

On every object switch (`handleInputChanged`, initial setup in `attach`):

1. Deregister listener from previous object (if persistent).
2. Check `_currentObject.tTransient()`.
3. If **not transient**: `_modelScope.addModelListener(_currentObject, this)`.
4. If **transient**: skip — no listener, no updates.

On `detach()` or `onCleanup()`: deregister from current object.

### notifyChange Implementation

```java
@Override
public void notifyChange(ModelChangeEvent event) {
    handleObjectDeleted(event);
    handleObjectUpdated(event);
}
```

**Delete handling:**
- Check if `event.getDeleted()` contains the current object (by `ObjectKey`).
- If yes:
  - If in edit mode: `exitEditMode()` (releases lock, discards overlay).
  - Deregister model listener.
  - Set `_currentObject = null`.
  - `updateNoModelMessage()`.
  - `fireFormStateChanged()` — field controls hide themselves via existing `onFormStateChanged` logic.

**Update handling:**
- Check if `event.getUpdated()` contains the current object (by `ObjectKey`).
- If yes and **not in edit mode**:
  - `fireFormStateChanged()` — triggers `AttributeFieldControl.onFormStateChanged()`, which calls `_model.setObject(current)`, re-reads values via `tValue()`, fires `valueChanged`, and React controls update via SSE.
- If yes and **in edit mode**: no action (overlay buffers changes).

### Object Identity

Object matching uses `ObjectKey` comparison (via `KnowledgeItem.tId()`), consistent with `ObservableTableModel`.

## Affected Files

| File | Change |
|---|---|
| `FormControl.java` | Implement `ModelListener`, add `attach(ModelScope)`/`detach()`, listener registration/deregistration on object switch, `notifyChange()` with delete/update handling |
| `FormElement.java` | Pass `ModelScope` to `FormControl` via `attach()`/`detach()` during control lifecycle |

## Not Affected

- **`AttributeFieldControl`**, **`AttributeFieldModel`**: The existing `onFormStateChanged`/`setObject` chain already handles value refreshes correctly.
- **React/TypeScript side**: No changes needed — SSE patches flow through existing infrastructure.
- **`TLObjectOverlay`**: No changes — overlay isolation works as-is.

## Edge Cases

- **Transient object becomes persistent** (e.g., after first save): The object switch after save goes through `handleInputChanged`, which re-evaluates `tTransient()` and registers a listener if appropriate.
- **Object deleted while in edit mode**: Lock release may warn, but `exitEditMode()` handles this gracefully.
- **Rapid successive updates**: Each `notifyChange` call triggers `fireFormStateChanged`, which is idempotent — field controls simply re-read the current value.
