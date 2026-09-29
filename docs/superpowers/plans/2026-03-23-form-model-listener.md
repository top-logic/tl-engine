# Form ModelListener Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `FormControl` react to persistent object changes (updates and deletes) so form fields stay in sync with the knowledge base.

**Architecture:** `FormControl` implements `ModelListener` directly and registers on `ModelScope` for the current object. `FormElement` wires lazy attachment via `addBeforeWriteAction`/`addCleanupAction`, matching the `TableElement`/`TreeElement` pattern.

**Tech Stack:** Java 17, TopLogic model/listen API (`ModelListener`, `ModelScope`, `ModelChangeEvent`)

**Spec:** `docs/superpowers/specs/2026-03-23-form-model-listener-design.md`

---

### Task 1: Add ModelListener to FormControl

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java`

- [ ] **Step 1: Add `ModelListener` import and interface declaration**

Add imports and change the class declaration:

```java
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
```

(`TLObject` is already imported.)

Change:
```java
public class FormControl extends ReactControl implements FormModel {
```
to:
```java
public class FormControl extends ReactControl implements FormModel, ModelListener {
```

- [ ] **Step 2: Add `_modelScope` field and `attach`/`detach` methods**

Add field after `_noModelMessage`:

```java
private ModelScope _modelScope;
```

Add methods after `setDirtyChannel`:

```java
/**
 * Registers this control as a {@link ModelListener} on the given scope for the current object.
 *
 * <p>
 * Called lazily on first render via {@code addBeforeWriteAction}, matching the
 * {@link com.top_logic.layout.view.model.ObservableTableModel} pattern.
 * </p>
 *
 * @param scope
 *        The model scope to observe.
 */
public void attach(ModelScope scope) {
    if (_modelScope != null) {
        return; // Already attached.
    }
    _modelScope = scope;
    registerModelListener();
}

/**
 * Removes all model listeners and releases the scope reference.
 */
public void detach() {
    deregisterModelListener();
    _modelScope = null;
}
```

- [ ] **Step 3: Add private helper methods for listener registration**

Add after `detach()`:

```java
private void registerModelListener() {
    if (_modelScope == null || _currentObject == null || _currentObject.tTransient()) {
        return;
    }
    _modelScope.addModelListener(_currentObject, this);
}

private void deregisterModelListener() {
    if (_modelScope == null || _currentObject == null || _currentObject.tTransient()) {
        return;
    }
    _modelScope.removeModelListener(_currentObject, this);
}
```

- [ ] **Step 4: Implement `notifyChange`**

Since the form observes exactly one object (unlike `ObservableTableModel` which iterates many rows),
use `event.getChange(_currentObject)` directly instead of streaming `getDeleted()`/`getUpdated()`:

```java
@Override
public void notifyChange(ModelChangeEvent event) {
    if (_currentObject == null) {
        return;
    }
    ModelChangeEvent.ChangeType change = event.getChange(_currentObject);
    if (change == ModelChangeEvent.ChangeType.DELETED) {
        onCurrentObjectDeleted();
    } else if (change == ModelChangeEvent.ChangeType.UPDATED && !_editMode) {
        // In view mode: refresh field values. In edit mode: overlay buffers changes,
        // base values become visible after save/cancel.
        fireFormStateChanged();
    }
}

private void onCurrentObjectDeleted() {
    if (_editMode) {
        exitEditMode();
    }
    deregisterModelListener();
    _currentObject = null;
    updateNoModelMessage();
    fireFormStateChanged();
}
```

Note: After `executeSave()` commits to the KB, the `ModelScope` fires a `ModelChangeEvent` with the
just-committed object in `UPDATED`. Since `_editMode` is already `false` at that point,
`fireFormStateChanged()` runs — this is harmless (idempotent, fields re-read the same values).

- [ ] **Step 5: Update `handleInputChanged` to re-register listener**

Change the existing `handleInputChanged` method from:

```java
private void handleInputChanged(ViewChannel sender, Object oldValue, Object newValue) {
    if (_editMode) {
        exitEditMode();
    }
    _currentObject = (TLObject) newValue;
    updateNoModelMessage();

    fireFormStateChanged();
}
```

to:

```java
private void handleInputChanged(ViewChannel sender, Object oldValue, Object newValue) {
    if (_editMode) {
        exitEditMode();
    }
    deregisterModelListener();
    _currentObject = (TLObject) newValue;
    registerModelListener();
    updateNoModelMessage();

    fireFormStateChanged();
}
```

- [ ] **Step 6: Update `onCleanup` to deregister listener**

Change the existing `onCleanup` method from:

```java
@Override
protected void onCleanup() {
    if (_editMode) {
        exitEditMode();
    }
    if (_inputChannel != null) {
        _inputChannel.removeListener(_inputListener);
    }
}
```

to:

```java
@Override
protected void onCleanup() {
    if (_editMode) {
        exitEditMode();
    }
    deregisterModelListener();
    if (_inputChannel != null) {
        _inputChannel.removeListener(_inputListener);
    }
}
```

- [ ] **Step 7: Compile**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```
Ticket #29108: Implement ModelListener in FormControl.

FormControl reacts to persistent object updates (refresh fields in view mode)
and deletes (exit edit mode, clear object). Transient objects are skipped.
```

---

### Task 2: Wire lazy attachment in FormElement

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java`

- [ ] **Step 1: Add lazy attach and cleanup in `createControl`**

In `FormElement.createControl()`, after step 11 (auto-enter edit mode) and before `return formControl`, add.
Note: When `initialEditMode=true`, `enterEditMode()` runs at step 11 before the listener attaches at
step 12. This is safe because create dialogs use transient objects (no listener would register anyway):

```java
// 12. Lazy attach model listener on render, cleanup on dispose.
formControl.addBeforeWriteAction(() -> {
    formControl.attach(context.getModelScope());
});
formControl.addCleanupAction(formControl::detach);
```

- [ ] **Step 2: Compile**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 3: Compile demo app**

Run: `mvn compile -DskipTests=true -pl com.top_logic.demo`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Wire lazy ModelListener attachment in FormElement.

Uses addBeforeWriteAction/addCleanupAction matching the TableElement/TreeElement
pattern. ModelScope is obtained from ViewContext.getModelScope().
```

---

### Task 3: Manual Integration Test

**Precondition:** Demo app running with a form view (e.g., an object detail form).

- [ ] **Step 1: Test view-mode update**

1. Open object A in a form (view mode).
2. In a second browser tab/session, modify an attribute of object A and save.
3. Verify: The first tab's form fields update to show the new values (via SSE).

- [ ] **Step 2: Test edit-mode isolation**

1. Open object A in a form and click Edit.
2. Change a field value but don't save.
3. In a second tab, modify the same object.
4. Verify: The editing tab's field values are unchanged (overlay buffers).
5. Cancel editing.
6. Verify: The form now shows the values from step 3 (the external update).

- [ ] **Step 3: Test delete handling**

1. Open object A in a form (view mode).
2. In a second tab, delete object A.
3. Verify: The first tab's form shows the "no object selected" message.

- [ ] **Step 4: Test delete during edit**

1. Open object B in a form and click Edit.
2. In a second tab, delete object B.
3. Verify: The first tab exits edit mode and shows "no object selected".

- [ ] **Step 5: Test transient object (no crash)**

1. Open a create dialog (which uses `initialEditMode=true` with a transient object).
2. Verify: No errors in the server log. The form works normally.
