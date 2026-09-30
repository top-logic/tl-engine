# Form Architecture Redesign Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the form field lifecycle so fields observe a `FormModel` interface and pull their own state, eliminating imperative push from `FormControl` and fixing the dirty-after-save bug.

**Architecture:** Introduce a `FormModel` interface with `FormModelListener`. `FormControl` implements `FormModel` and fires a single `onFormStateChanged()` event on every state transition. Fields (`AttributeFieldControl`) implement `FormModelListener`, self-register, and pull `getCurrentObject()` + `isEditMode()` when notified. `AttributeFieldModel` (renamed `OverlayFieldModel`) holds a plain `TLObject` and relies on `AbstractFieldModel`'s built-in dirty tracking.

**Tech Stack:** Java 17, TopLogic framework (`com.top_logic.layout.view`, `com.top_logic.layout.react`)

**Spec:** `docs/superpowers/specs/2026-03-12-form-architecture-redesign.md`

---

## File Structure

All files are in `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/`.

| File | Action | Responsibility |
|------|--------|----------------|
| `form/FormModel.java` | **Create** | Interface: `getCurrentObject()`, `isEditMode()`, listener management |
| `form/FormModelListener.java` | **Create** | Callback interface: `onFormStateChanged(FormModel)` |
| `form/AttributeFieldModel.java` | **Create** (rename of `OverlayFieldModel`) | Field model bound to a `TLObject` attribute, owns dirty state |
| `form/AttributeFieldControl.java` | **Create** (rename of `FieldControl`) | Implements `FormModelListener`, pulls state from `FormModel` |
| `form/FormControl.java` | **Modify** | Implements `FormModel`, fires `onFormStateChanged()` instead of iterating fields |
| `form/OverlayFieldModel.java` | **Delete** | Replaced by `AttributeFieldModel` |
| `form/FieldControl.java` | **Delete** | Replaced by `AttributeFieldControl` |
| `form/FormCommandModel.java` | **Modify** | Switch from `Runnable` form state listener to `FormModelListener` |
| `ViewContext.java` | **Modify** | `getFormControl()` → `getFormModel()`, `setFormControl()` → `setFormModel()` |
| `DefaultViewContext.java` | **Modify** | Update field type and methods to use `FormModel` |
| `element/FieldElement.java` | **Modify** | Use `getFormModel()`, create `AttributeFieldControl` |
| `element/FormElement.java` | **Modify** | Call `setFormModel()` |

---

## Chunk 1: FormModel interface + AttributeFieldModel

### Task 1: Create FormModelListener interface

**Files:**
- Create: `form/FormModelListener.java`

- [ ] **Step 1: Create the listener interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

/**
 * Listener for {@link FormModel} state changes.
 */
public interface FormModelListener {

	/**
	 * Called when the form's state changes (object switched, mode changed, overlay reset).
	 *
	 * @param source
	 *        The form model that changed.
	 */
	void onFormStateChanged(FormModel source);
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormModelListener.java
git commit -m "Ticket #29108: Create FormModelListener interface."
```

### Task 2: Create FormModel interface

**Files:**
- Create: `form/FormModel.java`

- [ ] **Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.model.TLObject;

/**
 * Read-only view of a form's editing state.
 *
 * <p>
 * Provides the current object (base or overlay) and the edit mode flag. Field controls use this
 * interface to pull their state instead of being pushed by the form.
 * </p>
 */
public interface FormModel {

	/**
	 * The current object being displayed or edited.
	 *
	 * <p>
	 * In edit mode, returns the {@link TLObjectOverlay}. In view mode, returns the base
	 * {@link TLObject}.
	 * </p>
	 *
	 * @return The current object, or {@code null} if no object is selected.
	 */
	TLObject getCurrentObject();

	/**
	 * Whether the form is currently in edit mode.
	 */
	boolean isEditMode();

	/**
	 * Registers a listener that is notified when the form state changes.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	void addFormModelListener(FormModelListener listener);

	/**
	 * Removes a previously registered listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeFormModelListener(FormModelListener listener);
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormModel.java
git commit -m "Ticket #29108: Create FormModel interface."
```

### Task 3: Create AttributeFieldModel (replacing OverlayFieldModel)

**Files:**
- Create: `form/AttributeFieldModel.java`

The key differences from `OverlayFieldModel`:
- Holds a `TLObject _object` (not `TLObjectOverlay _overlay`)
- No separate `_attributeName` — stores only `_part`
- Does NOT override `isDirty()` — uses `AbstractFieldModel`'s built-in dirty tracking
- `setObject()` replaces `setOverlay()` — resets `_defaultValue` to clear dirty state

- [ ] **Step 1: Create the class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Objects;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} that reads and writes a single attribute of a
 * {@link TLObject}.
 *
 * <p>
 * The object may be a base persistent object (view mode) or a {@link TLObjectOverlay} (edit mode).
 * Dirty tracking is handled by the inherited {@link AbstractFieldModel} logic which compares the
 * current value to the default value.
 * </p>
 */
public class AttributeFieldModel extends AbstractFieldModel {

	private TLObject _object;

	private TLStructuredTypePart _part;

	/**
	 * Creates a new model for the given attribute.
	 *
	 * @param object
	 *        The object to read/write values from.
	 * @param part
	 *        The attribute to bind to.
	 */
	public AttributeFieldModel(TLObject object, TLStructuredTypePart part) {
		super(object.tValue(part));
		_object = object;
		_part = part;
		setMandatory(part.isMandatory());
	}

	@Override
	public Object getValue() {
		return _object.tValue(_part);
	}

	@Override
	public void setValue(Object value) {
		Object oldValue = getValue();
		if (Objects.equals(oldValue, value)) {
			return;
		}
		_object.tUpdate(_part, value);
		fireValueChanged(oldValue, value);
	}

	/**
	 * Rebinds this model to a different object.
	 *
	 * <p>
	 * Called when the form switches objects or transitions between view and edit mode. Re-resolves
	 * the attribute from the new object's type, updates the default value (clearing dirty state),
	 * and fires value changed if the value differs.
	 * </p>
	 *
	 * @param newObject
	 *        The new object to bind to.
	 */
	public void setObject(TLObject newObject) {
		Object oldValue = getValue();
		_object = newObject;
		_part = resolvePart(newObject);
		setMandatory(_part.isMandatory());
		Object newValue = getValue();
		setDefaultValue(newValue);
		setValueInternal(newValue);
		if (!Objects.equals(oldValue, newValue)) {
			fireValueChanged(oldValue, newValue);
		}
	}

	/**
	 * The resolved attribute part.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	private TLStructuredTypePart resolvePart(TLObject obj) {
		TLStructuredType type = obj.tType();
		TLStructuredTypePart part = type.getPart(_part.getName());
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _part.getName() + "' not found in type '" + type + "'.");
		}
		return part;
	}
}
```

- [ ] **Step 2: Build to verify compilation**

```bash
cd com.top_logic.layout.view && mvn compile -DskipTests=true
```

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldModel.java
git commit -m "Ticket #29108: Create AttributeFieldModel replacing OverlayFieldModel."
```

---

## Chunk 2: FormControl implements FormModel

### Task 4: Make FormControl implement FormModel

**Files:**
- Modify: `form/FormControl.java`

Changes:
1. Add `implements FormModel` to class declaration
2. Replace `List<Runnable> _formStateListeners` with `List<FormModelListener> _formModelListeners`
3. Remove `registerFieldControl()` and `_fieldControls` list
4. Remove `addFormStateListener()`/`removeFormStateListener()` — replaced by `FormModel` methods
5. Remove `hasCurrentObject()` — callers use `getCurrentObject() != null` instead
6. Implement `addFormModelListener()`/`removeFormModelListener()`
7. Replace all `fireFormStateChanged()` to notify `FormModelListener`s
8. Remove `updateDirtyChannel()` package-visible method — fields update form dirty via `FormModel`
9. Simplify `enterEditMode()`, `exitEditMode()`, `executeApply()`, `handleInputChanged()` to all follow the same pattern: update state, fire event

- [ ] **Step 1: Apply changes to FormControl**

The full replacement is too large for inline code, so here are the key structural changes:

**Class declaration:**
```java
public class FormControl extends ReactControl implements FormModel {
```

**Remove fields:**
```java
// DELETE these:
private final List<FieldControl> _fieldControls = new ArrayList<>();
private final List<Runnable> _formStateListeners = new ArrayList<>();
```

**Add field:**
```java
private final List<FormModelListener> _formModelListeners = new ArrayList<>();
```

**Implement FormModel methods:**
```java
@Override
public void addFormModelListener(FormModelListener listener) {
    _formModelListeners.add(listener);
}

@Override
public void removeFormModelListener(FormModelListener listener) {
    _formModelListeners.remove(listener);
}
```

**Replace fireFormStateChanged:**
```java
private void fireFormStateChanged() {
    for (FormModelListener listener : _formModelListeners) {
        listener.onFormStateChanged(this);
    }
}
```

**Remove methods:** `registerFieldControl()`, `addFormStateListener()`, `removeFormStateListener()`, `hasCurrentObject()`, `updateDirtyChannel()`

**Simplify enterEditMode:**
```java
public void enterEditMode() {
    if (_editMode || _currentObject == null) return;
    _lockHandler.acquireLock(_currentObject);
    _overlay = new TLObjectOverlay(_currentObject);
    _editMode = true;
    putState(EDIT_MODE, Boolean.TRUE);
    updateEditModeChannel();
    updateDirtyState();
    fireFormStateChanged();
}
```

**Simplify exitEditMode:**
```java
private void exitEditMode() {
    _overlay = null;
    _editMode = false;
    releaseLock();
    putState(EDIT_MODE, Boolean.FALSE);
    updateEditModeChannel();
    updateDirtyState();
    fireFormStateChanged();
}
```

**Simplify executeApply:**
```java
public void executeApply() {
    if (!_editMode || _overlay == null || !_overlay.isDirty()) return;
    KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
    Transaction tx = kb.beginTransaction(I18NConstants.FORM_APPLY);
    try {
        _overlay.applyTo(_currentObject);
        tx.commit();
    } finally {
        tx.rollback();
    }
    _overlay.reset();
    updateDirtyState();
    fireFormStateChanged();
}
```

**Simplify handleInputChanged:**
```java
private void handleInputChanged(ViewChannel sender, Object oldValue, Object newValue) {
    if (_editMode) exitEditMode();
    _currentObject = (TLObject) newValue;
    updateNoModelMessage();
    fireFormStateChanged();
}
```

**Add method for fields to trigger form-level dirty update:**
```java
/**
 * Recalculates the form-level dirty state.
 *
 * <p>
 * Called by field controls when a value changes so the form's overall dirty state is updated.
 * </p>
 */
public void updateDirtyState() {
    boolean dirty = _editMode && _overlay != null && _overlay.isDirty();
    putState(DIRTY, Boolean.valueOf(dirty));
    if (_dirtyChannel != null) {
        _dirtyChannel.set(Boolean.valueOf(dirty));
    }
}
```

Note: `updateDirtyState()` changes from `private` to `public` so `AttributeFieldControl` can call it after writing to the overlay.

- [ ] **Step 2: Build to verify compilation (will fail — dependents not yet updated)**

```bash
cd com.top_logic.layout.view && mvn compile -DskipTests=true
```

Expected: compilation errors in `FormCommandModel`, `FieldElement`, `FormElement` (they reference removed methods). This is OK — we fix those in subsequent tasks.

- [ ] **Step 3: Commit (even with compilation errors, to keep changes atomic)**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java
git commit -m "Ticket #29108: FormControl implements FormModel with single fireFormStateChanged()."
```

### Task 5: Update ViewContext and DefaultViewContext

**Files:**
- Modify: `ViewContext.java`
- Modify: `DefaultViewContext.java`

- [ ] **Step 1: Update ViewContext interface**

Replace `getFormControl()`/`setFormControl(FormControl)` with `getFormModel()`/`setFormModel(FormModel)`:

```java
// In ViewContext.java:

/**
 * The {@link FormModel} of the nearest enclosing form element, or {@code null} if no form is
 * in scope.
 */
FormModel getFormModel();

/**
 * Sets the form model for this context.
 */
void setFormModel(FormModel formModel);
```

Add import for `FormModel`, remove import for `FormControl`.

- [ ] **Step 2: Update DefaultViewContext**

Change the field type from `FormControl _formControl` to `FormModel _formModel` and update the getter/setter and `childContext`/`withCommandScope` constructors.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java \
    com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java
git commit -m "Ticket #29108: ViewContext propagates FormModel instead of FormControl."
```

### Task 6: Update FormCommandModel to use FormModelListener

**Files:**
- Modify: `form/FormCommandModel.java`

- [ ] **Step 1: Switch from Runnable to FormModelListener**

```java
// Change the field:
private final Runnable _formStateHandler = this::handleFormStateChanged;
// To:
private final FormModelListener _formModelListener = this::handleFormStateChanged;

// Change attach():
public void attach() {
    _form.addFormModelListener(_formModelListener);
}

// Change detach():
public void detach() {
    _form.removeFormModelListener(_formModelListener);
}

// Change handler signature:
private void handleFormStateChanged(FormModel source) {
    boolean newExecutable = _executableWhen.test(_form);
    // ... rest unchanged
}
```

Replace `hasCurrentObject()` call in the edit command predicate:
```java
// From:
f -> f.hasCurrentObject() && !f.isEditMode()
// To:
f -> f.getCurrentObject() != null && !f.isEditMode()
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormCommandModel.java
git commit -m "Ticket #29108: FormCommandModel uses FormModelListener."
```

---

## Chunk 3: AttributeFieldControl + wiring

### Task 7: Create AttributeFieldControl (replacing FieldControl)

**Files:**
- Create: `form/AttributeFieldControl.java`

This is the core of the redesign. The field:
1. Implements `FormModelListener`
2. Self-registers on the `FormModel` in its constructor
3. In `onFormStateChanged()`, pulls `getCurrentObject()` and `isEditMode()`, calls `_model.setObject()`, updates editability, syncs dirty to chrome

- [ ] **Step 1: Create the class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Resources;

/**
 * Per-field session object that bridges a model attribute to a React input control wrapped in
 * {@link ReactFormFieldChromeControl chrome}.
 *
 * <p>
 * Implements {@link FormModelListener} and self-registers on the {@link FormModel}. When the form
 * state changes (object switch, edit mode toggle, apply), pulls the current object and edit mode
 * from the {@link FormModel} and updates the inner control and chrome accordingly.
 * </p>
 */
public class AttributeFieldControl implements FormModelListener {

	private final FormModel _formModel;

	private final FormControl _formControl;

	private final String _attributeName;

	private final ResKey _labelOverride;

	private final boolean _forceReadonly;

	private AttributeFieldModel _model;

	private FieldModelListener _modelListener;

	private ReactFormFieldChromeControl _chrome;

	private ReactControl _innerControl;

	/**
	 * Creates a new {@link AttributeFieldControl} and registers as listener on the form model.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formModel
	 *        The form model to observe.
	 * @param formControl
	 *        The concrete form control (for triggering dirty state updates).
	 * @param attributeName
	 *        The name of the model attribute to display.
	 * @param labelOverride
	 *        Optional label override, or {@code null} to derive the label from the model.
	 * @param forceReadonly
	 *        Whether the field should always be read-only regardless of form edit mode.
	 */
	public AttributeFieldControl(ReactContext context, FormModel formModel, FormControl formControl,
			String attributeName, ResKey labelOverride, boolean forceReadonly) {
		_formModel = formModel;
		_formControl = formControl;
		_attributeName = attributeName;
		_labelOverride = labelOverride;
		_forceReadonly = forceReadonly;
		formModel.addFormModelListener(this);
	}

	/**
	 * Creates the chrome-wrapped React control for this field.
	 *
	 * @return The chrome control.
	 */
	public ReactFormFieldChromeControl createChromeControl() {
		TLObject current = _formModel.getCurrentObject();
		if (current == null) {
			_innerControl = new ReactTextInputControl(
				_formControl.getContext(), new AbstractFieldModel(null) {
					// Placeholder model with default state.
				});
			_chrome = new ReactFormFieldChromeControl(_formControl.getContext(), _attributeName,
				false, false, null, null, null, false, true, _innerControl);
			return _chrome;
		}

		TLStructuredTypePart part = resolvePart(current);
		_model = new AttributeFieldModel(current, part);
		_model.setEditable(_formModel.isEditMode() && !_forceReadonly);

		addModelListener();

		_innerControl = FieldControlFactory.createFieldControl(
			_formControl.getContext(), part, _model);

		String label = resolveLabel();
		boolean mandatory = part.isMandatory();
		boolean dirty = _model.isDirty();

		_chrome = new ReactFormFieldChromeControl(_formControl.getContext(), label, mandatory,
			dirty, null, null, null, false, true, _innerControl);

		return _chrome;
	}

	@Override
	public void onFormStateChanged(FormModel source) {
		if (_chrome == null) {
			return;
		}

		TLObject current = source.getCurrentObject();

		if (current == null) {
			// Object gone — nothing to display.
			return;
		}

		if (_model == null) {
			// First object arrived — create model and inner control.
			TLStructuredTypePart part = resolvePart(current);
			_model = new AttributeFieldModel(current, part);
			_model.setEditable(source.isEditMode() && !_forceReadonly);

			addModelListener();

			_innerControl = FieldControlFactory.createFieldControl(
				_formControl.getContext(), part, _model);

			_chrome.setLabel(resolveLabel());
			_chrome.setRequired(part.isMandatory());
			_chrome.setField(_innerControl);
			_chrome.setDirty(false);
			return;
		}

		// Rebind model to the current object (may be base object or overlay).
		_model.setObject(current);
		_model.setEditable(source.isEditMode() && !_forceReadonly);
		_chrome.setDirty(_model.isDirty());
	}

	/**
	 * The resolved model attribute part, or {@code null} if not yet resolved.
	 */
	public TLStructuredTypePart getResolvedPart() {
		return _model != null ? _model.getPart() : null;
	}

	/**
	 * The chrome control wrapping the inner input.
	 */
	public ReactFormFieldChromeControl getChromeControl() {
		return _chrome;
	}

	/**
	 * The inner input control.
	 */
	public ReactControl getInnerControl() {
		return _innerControl;
	}

	private void addModelListener() {
		if (_modelListener != null) {
			_model.removeListener(_modelListener);
		}
		_modelListener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_formControl.updateDirtyState();
				if (_chrome != null) {
					_chrome.setDirty(_model.isDirty());
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Handled by the ReactFormFieldControl via its own FieldModel listener.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Handled by the ReactFormFieldControl via its own FieldModel listener.
			}
		};
		_model.addListener(_modelListener);
	}

	private TLStructuredTypePart resolvePart(TLObject obj) {
		TLStructuredType type = obj.tType();
		TLStructuredTypePart part = type.getPart(_attributeName);
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _attributeName + "' not found in type '" + type + "'.");
		}
		return part;
	}

	private String resolveLabel() {
		if (_labelOverride != null) {
			return Resources.getInstance().getString(_labelOverride);
		}
		if (_model != null) {
			return MetaLabelProvider.INSTANCE.getLabel(_model.getPart());
		}
		return _attributeName;
	}
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java
git commit -m "Ticket #29108: Create AttributeFieldControl implementing FormModelListener."
```

### Task 8: Update FieldElement and FormElement

**Files:**
- Modify: `element/FieldElement.java`
- Modify: `element/FormElement.java`

- [ ] **Step 1: Update FieldElement**

```java
// Change imports:
// Remove: import com.top_logic.layout.view.form.FieldControl;
// Remove: import com.top_logic.layout.view.form.FormControl;
// Add:    import com.top_logic.layout.view.form.AttributeFieldControl;
// Add:    import com.top_logic.layout.view.form.FormControl;
// Add:    import com.top_logic.layout.view.form.FormModel;

@Override
public IReactControl createControl(ViewContext context) {
    FormModel formModel = context.getFormModel();
    if (formModel == null) {
        throw new IllegalStateException(
            "FieldElement must be nested inside a FormElement. No FormModel found in ViewContext.");
    }

    // FormElement always sets a FormControl as the FormModel.
    FormControl formControl = (FormControl) formModel;

    AttributeFieldControl fieldControl = new AttributeFieldControl(
        context, formModel, formControl, _config.getAttribute(), _config.getLabel(),
        _config.getReadonly());

    ReactFormFieldChromeControl chrome = fieldControl.createChromeControl();
    return chrome;
}
```

- [ ] **Step 2: Update FormElement**

In `createControl()`, change:
```java
// From:
formContext.setFormControl(formControl);
// To:
formContext.setFormModel(formControl);
```

Update the `contributeEditCommands` method if it references `addFormStateListener` (it does through `FormCommandModel.attach()` which was already updated in Task 6).

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FieldElement.java \
    com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java
git commit -m "Ticket #29108: FieldElement and FormElement use FormModel."
```

### Task 9: Delete old files

**Files:**
- Delete: `form/OverlayFieldModel.java`
- Delete: `form/FieldControl.java`

- [ ] **Step 1: Delete the old files**

```bash
git rm com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/OverlayFieldModel.java
git rm com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControl.java
```

- [ ] **Step 2: Build the module**

```bash
cd com.top_logic.layout.view && mvn clean install -DskipTests=true
```

Expected: BUILD SUCCESS — all references now point to the new classes.

- [ ] **Step 3: Commit**

```bash
git commit -m "Ticket #29108: Remove OverlayFieldModel and FieldControl (replaced by AttributeFieldModel and AttributeFieldControl)."
```

---

## Chunk 4: Integration test

### Task 10: Build and test in browser

- [ ] **Step 1: Build everything**

```bash
cd com.top_logic.layout.view && mvn clean install -DskipTests=true
```

- [ ] **Step 2: Start the demo app**

Use the tl-app skill: `start com.top_logic.demo`

- [ ] **Step 3: Verify edit-mode toggle**

1. Login (root/root1234)
2. Navigate to Input Controls
3. Select A0
4. Click Edit → fields become editable
5. Click Save (without changes) → fields revert to read-only, **no dirty dot**

- [ ] **Step 4: Verify dirty indicator appears and clears on save**

1. Click Edit
2. Change Name field (e.g. "A0" → "A0x")
3. Verify dirty dot appears next to "Name"
4. Click Save
5. Verify dirty dot disappears, value is saved

- [ ] **Step 5: Verify object switching**

1. Select A1 → values update
2. Select A6 → values update (boolean=Ja)
3. Select A0x → values update back

- [ ] **Step 6: Verify edit + object switch**

1. Select A0x, click Edit
2. Switch to A1 → should exit edit mode, show A1 in view mode

- [ ] **Step 7: Commit (if any fixes were needed)**

```bash
git commit -m "Ticket #29108: Fix issues found during integration testing."
```
