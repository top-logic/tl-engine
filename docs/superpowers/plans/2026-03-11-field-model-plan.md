# FieldModel Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `FormField` dependency in React form controls with a lightweight `FieldModel` interface that decouples value binding from UI mechanics.

**Architecture:** A `FieldModel` interface in tl-core captures typed value, dirty tracking, validation, and edit lifecycle. React controls accept `FieldModel` instead of `FormField`. A `FormFieldAdapter` bridges existing `FormField` usage. `OverlayFieldModel` in the view module provides the lean path for model-based editing.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, ReactControl (SSE-based), JUnit 4

**Design document:** `docs/superpowers/specs/2026-03-11-value-model-design.md`

---

## Chunk 1: FieldModel Interfaces and Base Classes (tl-core)

### Task 1: FieldModel Interface

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldModel.java`

- [ ] **Step 1: Write the interface**

```java
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.basic.util.ResKey;

/**
 * Lightweight model for a single form field value.
 *
 * <p>
 * Captures typed value, dirty tracking, validation, and edit lifecycle without the UI mechanics
 * of {@link FormField} (raw value parsing, FormGroup hierarchy, display state).
 * </p>
 */
public interface FieldModel {

	/**
	 * The current typed value.
	 */
	Object getValue();

	/**
	 * Sets the typed value.
	 *
	 * <p>
	 * Fires {@link FieldModelListener#onValueChanged(FieldModel, Object, Object)} if the value
	 * changes.
	 * </p>
	 *
	 * @param value
	 *        The new value. May be {@code null}.
	 */
	void setValue(Object value);

	/**
	 * Whether the value has been modified from its default/original.
	 */
	boolean isDirty();

	/**
	 * Whether the value is currently editable.
	 *
	 * <p>
	 * Subsumes the legacy {@code immutable}, {@code disabled}, {@code frozen}, and {@code blocked}
	 * states. Controls render non-editable state uniformly.
	 * </p>
	 */
	boolean isEditable();

	/**
	 * Whether a value is required.
	 */
	boolean isMandatory();

	/**
	 * Whether the field currently has a validation error.
	 */
	boolean hasError();

	/**
	 * The current validation error, or {@code null} if no error.
	 */
	ResKey getError();

	/**
	 * Whether the field currently has validation warnings.
	 */
	boolean hasWarnings();

	/**
	 * The current validation warnings, or an empty list if none.
	 */
	List<ResKey> getWarnings();

	/**
	 * Triggers constraint validation against the current value.
	 */
	void validate();

	/**
	 * Adds a constraint to this model.
	 */
	void addConstraint(FieldConstraint constraint);

	/**
	 * Removes a previously added constraint.
	 */
	void removeConstraint(FieldConstraint constraint);

	/**
	 * Adds a listener for value, editability, and validation changes.
	 */
	void addListener(FieldModelListener listener);

	/**
	 * Removes a previously added listener.
	 */
	void removeListener(FieldModelListener listener);
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldModel.java
git commit -m "Ticket #29108: Add FieldModel interface for lightweight form value binding."
```

---

### Task 2: FieldModelListener and FieldConstraint

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldModelListener.java`
- Create: `com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldConstraint.java`

- [ ] **Step 1: Write FieldModelListener**

```java
package com.top_logic.layout.form.model;

/**
 * Listener for {@link FieldModel} state changes.
 */
public interface FieldModelListener {

	/**
	 * Called when the {@link FieldModel#getValue() value} changes.
	 *
	 * @param source
	 *        The model whose value changed.
	 * @param oldValue
	 *        The previous value.
	 * @param newValue
	 *        The new value.
	 */
	void onValueChanged(FieldModel source, Object oldValue, Object newValue);

	/**
	 * Called when the {@link FieldModel#isEditable() editability} changes.
	 *
	 * @param source
	 *        The model whose editability changed.
	 * @param editable
	 *        The new editability state.
	 */
	void onEditabilityChanged(FieldModel source, boolean editable);

	/**
	 * Called when validation state changes (error, warnings, or mandatory).
	 *
	 * @param source
	 *        The model whose validation state changed.
	 */
	void onValidationChanged(FieldModel source);
}
```

- [ ] **Step 2: Write FieldConstraint**

```java
package com.top_logic.layout.form.model;

import com.top_logic.basic.util.ResKey;

/**
 * Constraint that validates a field value.
 */
@FunctionalInterface
public interface FieldConstraint {

	/**
	 * Checks the given value.
	 *
	 * @param value
	 *        The value to check. May be {@code null}.
	 * @return {@code null} if valid, an error {@link ResKey} if invalid.
	 */
	ResKey check(Object value);
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldModelListener.java \
       com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldConstraint.java
git commit -m "Ticket #29108: Add FieldModelListener and FieldConstraint."
```

---

### Task 3: AbstractFieldModel

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/layout/form/model/AbstractFieldModel.java`
- Create: `com.top_logic/src/test/java/test/com/top_logic/layout/form/model/TestAbstractFieldModel.java`

- [ ] **Step 1: Write the test**

```java
package test.com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldConstraint;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;

/**
 * Tests for {@link AbstractFieldModel}.
 */
public class TestAbstractFieldModel extends TestCase {

	/**
	 * Test initial state.
	 */
	public void testInitialState() {
		AbstractFieldModel model = createModel("hello");
		assertEquals("hello", model.getValue());
		assertFalse(model.isDirty());
		assertTrue(model.isEditable());
		assertFalse(model.isMandatory());
		assertFalse(model.hasError());
		assertNull(model.getError());
	}

	/**
	 * Test setValue fires listener and tracks dirty.
	 */
	public void testSetValue() {
		AbstractFieldModel model = createModel("hello");
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);

		model.setValue("world");

		assertEquals("world", model.getValue());
		assertTrue(model.isDirty());
		assertEquals(1, listener.valueChanges.size());
		assertEquals("hello", listener.valueChanges.get(0).oldValue);
		assertEquals("world", listener.valueChanges.get(0).newValue);
	}

	/**
	 * Test setValue with same value does not fire.
	 */
	public void testSetSameValue() {
		AbstractFieldModel model = createModel("hello");
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);

		model.setValue("hello");

		assertFalse(model.isDirty());
		assertEquals(0, listener.valueChanges.size());
	}

	/**
	 * Test setEditable fires listener.
	 */
	public void testSetEditable() {
		AbstractFieldModel model = createModel(null);
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);

		model.setEditable(false);

		assertFalse(model.isEditable());
		assertEquals(1, listener.editabilityChanges.size());
		assertFalse(listener.editabilityChanges.get(0));
	}

	/**
	 * Test constraint validation.
	 */
	public void testValidation() {
		AbstractFieldModel model = createModel(null);
		model.setMandatory(true);
		model.addConstraint(value -> value == null ? ResKey.text("Required") : null);

		model.validate();

		assertTrue(model.hasError());
		assertNotNull(model.getError());
	}

	/**
	 * Test validation clears error when value is valid.
	 */
	public void testValidationClears() {
		AbstractFieldModel model = createModel(null);
		model.addConstraint(value -> value == null ? ResKey.text("Required") : null);
		model.validate();
		assertTrue(model.hasError());

		model.setValue("valid");
		model.validate();
		assertFalse(model.hasError());
	}

	/**
	 * Test removeListener stops notifications.
	 */
	public void testRemoveListener() {
		AbstractFieldModel model = createModel("a");
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);
		model.removeListener(listener);

		model.setValue("b");
		assertEquals(0, listener.valueChanges.size());
	}

	/**
	 * Test dirty resets when value returns to default.
	 */
	public void testDirtyResetsOnDefault() {
		AbstractFieldModel model = createModel("original");
		model.setValue("changed");
		assertTrue(model.isDirty());

		model.setValue("original");
		assertFalse(model.isDirty());
	}

	private AbstractFieldModel createModel(Object initialValue) {
		return new AbstractFieldModel(initialValue) {
			// Use base implementation.
		};
	}

	static class RecordingListener implements FieldModelListener {
		final List<ValueChange> valueChanges = new ArrayList<>();
		final List<Boolean> editabilityChanges = new ArrayList<>();
		int validationChanges = 0;

		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			valueChanges.add(new ValueChange(oldValue, newValue));
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			editabilityChanges.add(editable);
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			validationChanges++;
		}
	}

	static class ValueChange {
		final Object oldValue;
		final Object newValue;

		ValueChange(Object oldValue, Object newValue) {
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.form.model.TestAbstractFieldModel`
Expected: Compilation failure — `AbstractFieldModel` does not exist.

- [ ] **Step 3: Write the implementation**

```java
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.util.ResKey;

/**
 * Base implementation of {@link FieldModel} with listener management, dirty tracking, and
 * constraint validation.
 *
 * <p>
 * Subclasses can override value storage by overriding {@link #getValue()} and
 * {@link #setValueInternal(Object)}.
 * </p>
 */
public class AbstractFieldModel implements FieldModel {

	private Object _value;

	private Object _defaultValue;

	private boolean _editable = true;

	private boolean _mandatory;

	private ResKey _error;

	private List<ResKey> _warnings = Collections.emptyList();

	private List<FieldConstraint> _constraints = Collections.emptyList();

	private List<FieldModelListener> _listeners = Collections.emptyList();

	/**
	 * Creates a new model with the given initial value.
	 *
	 * @param initialValue
	 *        The initial value, also used as default for dirty tracking.
	 */
	public AbstractFieldModel(Object initialValue) {
		_value = initialValue;
		_defaultValue = initialValue;
	}

	@Override
	public Object getValue() {
		return _value;
	}

	@Override
	public void setValue(Object value) {
		Object oldValue = _value;
		if (Objects.equals(oldValue, value)) {
			return;
		}
		_value = value;
		fireValueChanged(oldValue, value);
	}

	/**
	 * Internal setter that does not fire listeners. For use by subclasses.
	 */
	protected void setValueInternal(Object value) {
		_value = value;
	}

	@Override
	public boolean isDirty() {
		return !Objects.equals(_value, _defaultValue);
	}

	@Override
	public boolean isEditable() {
		return _editable;
	}

	/**
	 * Sets the editability state.
	 *
	 * <p>
	 * Fires {@link FieldModelListener#onEditabilityChanged(FieldModel, boolean)} if the state
	 * changes.
	 * </p>
	 */
	public void setEditable(boolean editable) {
		if (_editable == editable) {
			return;
		}
		_editable = editable;
		fireEditabilityChanged(editable);
	}

	@Override
	public boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * Sets the mandatory flag.
	 *
	 * <p>
	 * Fires {@link FieldModelListener#onValidationChanged(FieldModel)}.
	 * </p>
	 */
	public void setMandatory(boolean mandatory) {
		if (_mandatory == mandatory) {
			return;
		}
		_mandatory = mandatory;
		fireValidationChanged();
	}

	@Override
	public boolean hasError() {
		return _error != null;
	}

	@Override
	public ResKey getError() {
		return _error;
	}

	@Override
	public boolean hasWarnings() {
		return !_warnings.isEmpty();
	}

	@Override
	public List<ResKey> getWarnings() {
		return _warnings;
	}

	@Override
	public void validate() {
		ResKey firstError = null;
		List<ResKey> warnings = Collections.emptyList();

		for (FieldConstraint constraint : _constraints) {
			ResKey result = constraint.check(_value);
			if (result != null) {
				if (firstError == null) {
					firstError = result;
				}
			}
		}

		boolean changed = !Objects.equals(_error, firstError) || !_warnings.equals(warnings);
		_error = firstError;
		_warnings = warnings;
		if (changed) {
			fireValidationChanged();
		}
	}

	@Override
	public void addConstraint(FieldConstraint constraint) {
		if (_constraints.isEmpty()) {
			_constraints = new ArrayList<>();
		}
		_constraints.add(constraint);
	}

	@Override
	public void removeConstraint(FieldConstraint constraint) {
		_constraints.remove(constraint);
	}

	@Override
	public void addListener(FieldModelListener listener) {
		if (_listeners.isEmpty()) {
			_listeners = new ArrayList<>();
		}
		_listeners.add(listener);
	}

	@Override
	public void removeListener(FieldModelListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Resets the default value used for dirty tracking.
	 */
	public void setDefaultValue(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	/**
	 * Fires {@link FieldModelListener#onValueChanged(FieldModel, Object, Object)}.
	 */
	protected void fireValueChanged(Object oldValue, Object newValue) {
		for (FieldModelListener listener : _listeners) {
			listener.onValueChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Fires {@link FieldModelListener#onEditabilityChanged(FieldModel, boolean)}.
	 */
	protected void fireEditabilityChanged(boolean editable) {
		for (FieldModelListener listener : _listeners) {
			listener.onEditabilityChanged(this, editable);
		}
	}

	/**
	 * Fires {@link FieldModelListener#onValidationChanged(FieldModel)}.
	 */
	protected void fireValidationChanged() {
		for (FieldModelListener listener : _listeners) {
			listener.onValidationChanged(this);
		}
	}
}
```

- [ ] **Step 4: Run the test**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.form.model.TestAbstractFieldModel`
Expected: All tests PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/layout/form/model/AbstractFieldModel.java \
       com.top_logic/src/test/java/test/com/top_logic/layout/form/model/TestAbstractFieldModel.java
git commit -m "Ticket #29108: Add AbstractFieldModel with dirty tracking and constraint validation."
```

---

### Task 4: SelectFieldModel Sub-Interface

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/layout/form/model/SelectFieldModel.java`
- Create: `com.top_logic/src/main/java/com/top_logic/layout/form/model/OptionsListener.java`

- [ ] **Step 1: Write OptionsListener**

```java
package com.top_logic.layout.form.model;

import java.util.List;

/**
 * Listener for {@link SelectFieldModel#getOptions() options} changes.
 */
public interface OptionsListener {

	/**
	 * Called when the available options change.
	 *
	 * @param source
	 *        The model whose options changed.
	 * @param newOptions
	 *        The new options list.
	 */
	void onOptionsChanged(SelectFieldModel source, List<?> newOptions);
}
```

- [ ] **Step 2: Write SelectFieldModel**

```java
package com.top_logic.layout.form.model;

import java.util.List;

/**
 * {@link FieldModel} for fields where the value must come from a defined set of options.
 */
public interface SelectFieldModel extends FieldModel {

	/**
	 * The available options.
	 */
	List<?> getOptions();

	/**
	 * Whether multiple options can be selected simultaneously.
	 */
	boolean isMultiple();

	/**
	 * Updates the available options.
	 *
	 * <p>
	 * Fires {@link OptionsListener#onOptionsChanged(SelectFieldModel, List)}.
	 * </p>
	 */
	void setOptions(List<?> options);

	/**
	 * Adds a listener for options changes.
	 */
	void addOptionsListener(OptionsListener listener);

	/**
	 * Removes a previously added options listener.
	 */
	void removeOptionsListener(OptionsListener listener);
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/layout/form/model/SelectFieldModel.java \
       com.top_logic/src/main/java/com/top_logic/layout/form/model/OptionsListener.java
git commit -m "Ticket #29108: Add SelectFieldModel for option-based fields."
```

---

### Task 5: FormFieldAdapter

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/layout/form/model/FormFieldAdapter.java`
- Create: `com.top_logic/src/test/java/test/com/top_logic/layout/form/model/TestFormFieldAdapter.java`

- [ ] **Step 1: Write the test**

```java
package test.com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.FormFieldAdapter;
import com.top_logic.layout.form.model.StringField;

/**
 * Tests for {@link FormFieldAdapter}.
 */
public class TestFormFieldAdapter extends TestCase {

	/**
	 * Test value delegation.
	 */
	public void testGetSetValue() {
		StringField field = StringField.newStringField("test");
		field.initializeField("hello", false);
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertEquals("hello", adapter.getValue());

		adapter.setValue("world");
		assertEquals("world", adapter.getValue());
		assertEquals("world", field.getValue());
	}

	/**
	 * Test dirty delegation.
	 */
	public void testDirty() {
		StringField field = StringField.newStringField("test");
		field.initializeField("original", false);
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertFalse(adapter.isDirty());

		adapter.setValue("changed");
		assertTrue(adapter.isDirty());
	}

	/**
	 * Test editability delegation.
	 */
	public void testEditable() {
		StringField field = StringField.newStringField("test");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertTrue(adapter.isEditable());

		field.setImmutable(true);
		assertFalse(adapter.isEditable());
	}

	/**
	 * Test mandatory delegation.
	 */
	public void testMandatory() {
		StringField field = StringField.newStringField("test");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertFalse(adapter.isMandatory());

		field.setMandatory(true);
		assertTrue(adapter.isMandatory());
	}

	/**
	 * Test label accessor.
	 */
	public void testLabel() {
		StringField field = StringField.newStringField("test");
		field.setLabel("Test Label");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertEquals("Test Label", adapter.getLabel());
	}

	/**
	 * Test value change listener forwarding.
	 */
	public void testValueChangeListener() {
		StringField field = StringField.newStringField("test");
		field.initializeField("a", false);
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		List<Object> newValues = new ArrayList<>();
		adapter.addListener(new TestAbstractFieldModel.RecordingListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				newValues.add(newValue);
			}
		});

		field.setValue("b");
		assertEquals(1, newValues.size());
		assertEquals("b", newValues.get(0));
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.form.model.TestFormFieldAdapter`
Expected: Compilation failure — `FormFieldAdapter` does not exist.

- [ ] **Step 3: Write the implementation**

```java
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;

/**
 * Adapter that wraps a {@link FormField} as a {@link FieldModel}.
 *
 * <p>
 * Translates {@link FormField} property listeners into {@link FieldModelListener} calls.
 * Also exposes display properties (label, tooltip, visibility) that are not part of
 * {@link FieldModel} but needed by controls in the traditional layout system.
 * </p>
 */
public class FormFieldAdapter implements FieldModel {

	private final FormField _field;

	private List<FieldModelListener> _listeners = Collections.emptyList();

	/**
	 * Creates an adapter wrapping the given form field.
	 */
	public FormFieldAdapter(FormField field) {
		_field = field;
		registerFieldListeners();
	}

	@Override
	public Object getValue() {
		return _field.getValue();
	}

	@Override
	public void setValue(Object value) {
		_field.setValue(value);
	}

	@Override
	public boolean isDirty() {
		return _field.isChanged();
	}

	@Override
	public boolean isEditable() {
		return !_field.isImmutable() && !_field.isDisabled();
	}

	@Override
	public boolean isMandatory() {
		return _field.isMandatory();
	}

	@Override
	public boolean hasError() {
		return _field.hasError();
	}

	@Override
	public ResKey getError() {
		// FormField.getError() returns a display String (already resolved).
		// Wrap in ResKey.text() for the FieldModel API.
		return _field.hasError() ? ResKey.text(_field.getError()) : null;
	}

	@Override
	public boolean hasWarnings() {
		return _field.hasWarnings();
	}

	@Override
	public List<ResKey> getWarnings() {
		if (!_field.hasWarnings()) {
			return Collections.emptyList();
		}
		List<String> fieldWarnings = _field.getWarnings();
		List<ResKey> result = new ArrayList<>(fieldWarnings.size());
		for (String w : fieldWarnings) {
			result.add(ResKey.text(w));
		}
		return result;
	}

	@Override
	public void validate() {
		_field.check();
	}

	@Override
	public void addConstraint(FieldConstraint constraint) {
		// Constraints on the underlying FormField are managed separately.
		// This adapter does not add FieldConstraint instances to the FormField.
		throw new UnsupportedOperationException(
			"Add constraints directly to the wrapped FormField.");
	}

	@Override
	public void removeConstraint(FieldConstraint constraint) {
		throw new UnsupportedOperationException(
			"Remove constraints directly from the wrapped FormField.");
	}

	@Override
	public void addListener(FieldModelListener listener) {
		if (_listeners.isEmpty()) {
			_listeners = new ArrayList<>();
		}
		_listeners.add(listener);
	}

	@Override
	public void removeListener(FieldModelListener listener) {
		_listeners.remove(listener);
	}

	// --- Display properties (not part of FieldModel) ---

	/**
	 * The field label.
	 */
	public String getLabel() {
		return _field.getLabel();
	}

	/**
	 * The field tooltip.
	 */
	public String getTooltip() {
		return _field.getTooltip();
	}

	/**
	 * Whether the field is visible.
	 */
	public boolean isVisible() {
		return _field.isVisible();
	}

	/**
	 * The wrapped {@link FormField}.
	 */
	public FormField getFormField() {
		return _field;
	}

	private void registerFieldListeners() {
		_field.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				for (FieldModelListener listener : _listeners) {
					listener.onValueChanged(FormFieldAdapter.this, oldValue, newValue);
				}
			}
		});

		_field.addListener(FormField.HAS_ERROR_PROPERTY,
			(sender, oldError, newError) -> fireValidationChanged());

		_field.addListener(FormField.MANDATORY_PROPERTY,
			(sender, oldMandatory, newMandatory) -> fireValidationChanged());

		_field.addListener(FormMember.IMMUTABLE_PROPERTY,
			(sender, oldValue, newValue) -> fireEditabilityChanged());

		_field.addListener(FormMember.DISABLED_PROPERTY,
			(sender, oldValue, newValue) -> fireEditabilityChanged());
	}

	private void fireEditabilityChanged() {
		boolean editable = isEditable();
		for (FieldModelListener listener : _listeners) {
			listener.onEditabilityChanged(this, editable);
		}
	}

	private void fireValidationChanged() {
		for (FieldModelListener listener : _listeners) {
			listener.onValidationChanged(this);
		}
	}
}
```

Note: The listener registrations use lambda syntax. The exact listener interfaces (`ImmutablePropertyListener`, `DisabledPropertyListener`, `MandatoryChangedListener`, `HasErrorChanged`) need to match the event types declared on `FormField`/`FormMember`. During implementation, check the exact functional interface signatures and adjust the lambdas if they don't match. Each of these listeners has a single abstract method matching `(sender, oldValue, newValue)`.

- [ ] **Step 4: Run the test**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.form.model.TestFormFieldAdapter`
Expected: All tests PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/layout/form/model/FormFieldAdapter.java \
       com.top_logic/src/test/java/test/com/top_logic/layout/form/model/TestFormFieldAdapter.java
git commit -m "Ticket #29108: Add FormFieldAdapter bridging FormField to FieldModel."
```

---

## Chunk 2: Refactor React Controls (com.top_logic.layout.react)

### Task 6: Refactor ReactFormFieldControl Base

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactFormFieldControl.java`

This is the most impactful change. The base class changes from `FormField` to `FieldModel`.

- [ ] **Step 1: Refactor the constructor and state initialization**

Change the constructor from:
```java
protected ReactFormFieldControl(ReactContext context, FormField model, String reactModule) {
    super(context, model, reactModule);
    initFieldState(model);
}
```

To:
```java
protected ReactFormFieldControl(ReactContext context, FieldModel model, String reactModule) {
    super(context, null, reactModule);
    _fieldModel = model;
    initFieldState();
    registerModelListeners();
}
```

Add field: `private final FieldModel _fieldModel;`

Add accessor: `public FieldModel getFieldModel() { return _fieldModel; }`

Refactor `initFieldState()` to read from `_fieldModel` instead of `FormField`:
```java
private void initFieldState() {
    putState(VALUE, _fieldModel.getValue());
    putState(EDITABLE, _fieldModel.isEditable());
    putState(MANDATORY, _fieldModel.isMandatory());
    putState(HAS_ERROR, _fieldModel.hasError());
    if (_fieldModel.hasError()) {
        putState(ERROR_MESSAGE, Resources.getInstance().getString(_fieldModel.getError()));
    }
    // Display properties: label, tooltip, hidden
    if (_fieldModel instanceof FormFieldAdapter) {
        FormFieldAdapter adapter = (FormFieldAdapter) _fieldModel;
        putState(LABEL, adapter.getLabel());
        putState(TOOLTIP, adapter.getTooltip());
        putState(HIDDEN, !adapter.isVisible());
    }
}
```

- [ ] **Step 2: Refactor listener registration**

Replace `registerFieldListeners()` (which registered 6 FormField property listeners) with a single `FieldModelListener` registration:

```java
private void registerModelListeners() {
    _fieldModel.addListener(new FieldModelListener() {
        @Override
        public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
            putState(VALUE, newValue);
        }

        @Override
        public void onEditabilityChanged(FieldModel source, boolean editable) {
            putState(EDITABLE, editable);
        }

        @Override
        public void onValidationChanged(FieldModel source) {
            putState(HAS_ERROR, source.hasError());
            if (source.hasError()) {
                putState(ERROR_MESSAGE,
                    Resources.getInstance().getString(source.getError()));
            } else {
                putState(ERROR_MESSAGE, null);
            }
            putState(MANDATORY, source.isMandatory());
        }
    });
}
```

- [ ] **Step 3: Refactor valueChanged command handler**

Change from:
```java
@ReactCommand("valueChanged")
void handleValueChanged(Map<String, Object> arguments) {
    FormField field = getFieldModel();
    Object newValue = arguments.get(VALUE);
    String rawValue = newValue != null ? newValue.toString() : null;
    FormFieldInternals.updateFieldNoClientUpdate(field, rawValue);
}
```

To:
```java
@ReactCommand("valueChanged")
void handleValueChanged(Map<String, Object> arguments) {
    Object newValue = arguments.get(VALUE);
    _fieldModel.setValue(parseClientValue(newValue));
}

/**
 * Parses the raw client value into the appropriate typed value.
 *
 * <p>
 * Subclasses override this for type-specific parsing. Default returns the raw value.
 * </p>
 */
protected Object parseClientValue(Object rawValue) {
    return rawValue;
}
```

- [ ] **Step 4: Remove the DISABLED state key**

The `DISABLED` state key is no longer needed — `EDITABLE` covers it. Remove the `DISABLED` constant and any `putState(DISABLED, ...)` calls. The React components already handle `editable` — check that `TLTextInput`, `TLCheckbox`, etc. use the `editable` prop, not a separate `disabled` prop.

- [ ] **Step 5: Remove all FormField listener interfaces**

The class no longer needs to implement `MandatoryChangedListener`, `HasErrorChanged`, `ImmutablePropertyListener`, `DisabledPropertyListener`, `VisibilityListener`, `ValueListener`. Remove these from the `implements` clause and delete the corresponding callback methods.

- [ ] **Step 6: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.react && mvn compile -DskipTests=true`
Expected: Compilation errors in subclasses (they still pass `FormField` to super). This is expected — fixed in next tasks.

- [ ] **Step 7: Commit (work-in-progress)**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactFormFieldControl.java
git commit -m "Ticket #29108: Refactor ReactFormFieldControl to use FieldModel."
```

---

### Task 7: Refactor React Control Subclasses

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactTextInputControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactCheckboxControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactNumberInputControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactDatePickerControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactSelectFormFieldControl.java`

- [ ] **Step 1: Refactor ReactTextInputControl**

Change constructor from:
```java
public ReactTextInputControl(ReactContext context, FormField model) {
    super(context, model, "TLTextInput");
}
```
To:
```java
public ReactTextInputControl(ReactContext context, FieldModel model) {
    super(context, model, "TLTextInput");
}
```

Override `parseClientValue`:
```java
@Override
protected Object parseClientValue(Object rawValue) {
    return rawValue != null ? rawValue.toString() : null;
}
```

- [ ] **Step 2: Refactor ReactCheckboxControl**

Change constructor to take `FieldModel`. Override `parseClientValue`:
```java
@Override
protected Object parseClientValue(Object rawValue) {
    return Boolean.TRUE.equals(rawValue);
}
```

- [ ] **Step 3: Refactor ReactNumberInputControl**

Change constructor to take `FieldModel` (keeping `decimalPlaces` parameter). Override `parseClientValue`:
```java
@Override
protected Object parseClientValue(Object rawValue) {
    if (rawValue instanceof Number) {
        return rawValue;
    }
    if (rawValue != null) {
        return Double.parseDouble(rawValue.toString());
    }
    return null;
}
```

- [ ] **Step 4: Refactor ReactDatePickerControl**

Change constructor to take `FieldModel`. Value remains as-is from client (String/Date).

- [ ] **Step 5: Refactor ReactSelectFormFieldControl**

Change constructor to take `SelectFieldModel` + `LabelProvider`:
```java
public ReactSelectFormFieldControl(ReactContext context, SelectFieldModel model,
        LabelProvider labelProvider) {
    super(context, model, "TLSelect");
    _labelProvider = labelProvider;
    putState(OPTIONS, buildOptionsList(model.getOptions()));
    model.addOptionsListener((source, newOptions) ->
        putState(OPTIONS, buildOptionsList(newOptions)));
}

private List<Map<String, Object>> buildOptionsList(List<?> options) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Object option : options) {
        result.add(Map.of(
            "value", option,
            "label", _labelProvider.getLabel(option)));
    }
    return result;
}
```

- [ ] **Step 6: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.react && mvn compile -DskipTests=true`
Expected: May still fail if call sites in other modules pass `FormField` directly. That's addressed in Task 8.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactTextInputControl.java \
       com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactCheckboxControl.java \
       com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactNumberInputControl.java \
       com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactDatePickerControl.java \
       com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactSelectFormFieldControl.java
git commit -m "Ticket #29108: Refactor React control subclasses to use FieldModel."
```

---

### Task 8: Update Call Sites

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactFormFieldsComponent.java`
- Potentially other call sites (search for `new React*Control` across the codebase)

- [ ] **Step 1: Find all call sites**

Run: Search for `new ReactTextInputControl\|new ReactCheckboxControl\|new ReactNumberInputControl\|new ReactDatePickerControl\|new ReactSelectFormFieldControl` across the codebase.

- [ ] **Step 2: Wrap FormField arguments in FormFieldAdapter**

For each call site that passes a `FormField`, wrap it:

Before:
```java
new ReactTextInputControl(ctx, nameField)
```

After:
```java
new ReactTextInputControl(ctx, new FormFieldAdapter(nameField))
```

For `ReactSelectFormFieldControl`, the call site needs a `SelectFieldModel`. Create `SelectFieldAdapter`:

```java
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter wrapping a {@link SelectField} as a {@link SelectFieldModel}.
 */
public class SelectFieldAdapter extends FormFieldAdapter implements SelectFieldModel {

    private final SelectField _selectField;

    private List<OptionsListener> _optionsListeners = Collections.emptyList();

    /**
     * Creates an adapter for the given select field.
     */
    public SelectFieldAdapter(SelectField field) {
        super(field);
        _selectField = field;
    }

    @Override
    public List<?> getOptions() {
        return _selectField.getOptions();
    }

    @Override
    public boolean isMultiple() {
        return _selectField.isMultiple();
    }

    @Override
    public void setOptions(List<?> options) {
        _selectField.setOptionModel(new DefaultListOptionModel(options));
    }

    @Override
    public void addOptionsListener(OptionsListener listener) {
        if (_optionsListeners.isEmpty()) {
            _optionsListeners = new ArrayList<>();
        }
        _optionsListeners.add(listener);
    }

    @Override
    public void removeOptionsListener(OptionsListener listener) {
        _optionsListeners.remove(listener);
    }
}
```

File: `com.top_logic/src/main/java/com/top_logic/layout/form/model/SelectFieldAdapter.java`

Then wrap at call sites:
```java
new ReactSelectFormFieldControl(ctx, new SelectFieldAdapter(langField), MetaLabelProvider.INSTANCE)
```

- [ ] **Step 3: Build the full project**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c && mvn install -DskipTests=true -pl com.top_logic,com.top_logic.layout.react,com.top_logic.demo -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add -u
git commit -m "Ticket #29108: Update call sites to use FormFieldAdapter with React controls."
```

---

### Task 9: Create ReactColorInputControl

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactColorInputControl.java`

This absorbs palette logic from `ViewColorInputControl` in the view module.

- [ ] **Step 1: Write the implementation**

```java
package com.top_logic.layout.react.control.form;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;

/**
 * React color input control with palette management.
 *
 * <p>
 * Renders the {@code TLColorInput} React component. The color value flows through
 * {@link FieldModel}. Palette management (personal palette, drag-drop reordering, reset)
 * is handled by this control.
 * </p>
 */
public class ReactColorInputControl extends ReactFormFieldControl {

    private static final String PALETTE = "palette";
    private static final String DEFAULT_PALETTE = "defaultPalette";
    private static final String PALETTE_COLUMNS = "paletteColumns";
    private static final String CAN_RESET = "canReset";

    private List<String> _palette;
    private final List<String> _defaultPalette;
    private final int _paletteColumns;
    private final boolean _canReset;

    /**
     * Creates a new color input control.
     */
    public ReactColorInputControl(ReactContext context, FieldModel model,
            List<String> palette, List<String> defaultPalette,
            int paletteColumns, boolean canReset) {
        super(context, model, "TLColorInput");
        _palette = palette;
        _defaultPalette = defaultPalette;
        _paletteColumns = paletteColumns;
        _canReset = canReset;

        putState(PALETTE, _palette);
        putState(DEFAULT_PALETTE, _defaultPalette);
        putState(PALETTE_COLUMNS, _paletteColumns);
        putState(CAN_RESET, _canReset);
    }

    @Override
    protected Object parseClientValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        return hexToColor(rawValue.toString());
    }

    // Palette command handlers (paletteChanged, etc.) move here from
    // ViewColorInputControl. Implementation details: use @ReactCommand annotation,
    // load/save via PersonalConfiguration.
    // Copy the paletteChanged handler, savePalette, loadPalette methods from
    // ViewColorInputControl.java (lines 130-173).

    /**
     * Converts a hex string (#RRGGBB) to a {@link Color}.
     */
    public static Color hexToColor(String hex) {
        if (hex == null || hex.isEmpty() || "transparent".equals(hex)) {
            return null;
        }
        return Color.decode(hex);
    }

    /**
     * Converts a {@link Color} to a hex string (#RRGGBB).
     */
    public static String colorToHex(Color color) {
        if (color == null) {
            return "transparent";
        }
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
```

Note: The `@ReactCommand("paletteChanged")` handler and `PersonalConfiguration` palette persistence must be copied from `ViewColorInputControl.java` (lines 130-173). Adapt to work with the `FieldModel` base class.

- [ ] **Step 2: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.react && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactColorInputControl.java
git commit -m "Ticket #29108: Add ReactColorInputControl with palette management."
```

---

## Chunk 3: View Module Cleanup (com.top_logic.layout.view)

### Task 10: Create OverlayFieldModel

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/OverlayFieldModel.java`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/form/TestOverlayFieldModel.java`

- [ ] **Step 1: Write the test**

```java
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.view.form.OverlayFieldModel;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Tests for {@link OverlayFieldModel}.
 */
public class TestOverlayFieldModel extends TestCase {

	// Note: This test requires a mock or TransientTLObject setup.
	// If a simple TLObject mock is available from existing tests, use that pattern.
	// Otherwise create a minimal mock.
	// The exact test setup depends on what's available in the test infrastructure.

	/**
	 * Test that getValue reads from overlay.
	 */
	public void testPlaceholder() {
		// Placeholder test — actual test requires TLObject mock setup.
		// Implement during execution based on available test infrastructure.
		assertTrue(true);
	}
}
```

- [ ] **Step 2: Write the implementation**

```java
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} that reads/writes a single attribute of a
 * {@link TLObjectOverlay}.
 */
public class OverlayFieldModel extends AbstractFieldModel {

	private TLObjectOverlay _overlay;

	private String _attributeName;

	private TLStructuredTypePart _part;

	/**
	 * Creates a new model for the given attribute.
	 *
	 * @param overlay
	 *        The overlay to read/write values from.
	 * @param attributeName
	 *        The attribute name to resolve against the overlay's type.
	 */
	public OverlayFieldModel(TLObjectOverlay overlay, String attributeName) {
		super(resolveValue(overlay, attributeName));
		_overlay = overlay;
		_attributeName = attributeName;
		_part = resolvePart(overlay);
		setMandatory(_part.isMandatory());
	}

	@Override
	public Object getValue() {
		if (_overlay == null) {
			return null;
		}
		return _overlay.tValue(_part);
	}

	@Override
	public void setValue(Object value) {
		if (_overlay == null) {
			return;
		}
		Object oldValue = _overlay.tValue(_part);
		_overlay.tUpdate(_part, value);
		fireValueChanged(oldValue, value);
	}

	@Override
	public boolean isDirty() {
		if (_overlay == null) {
			return false;
		}
		return _overlay.isChanged(_part);
	}

	/**
	 * Re-binds this model to a new overlay (when the edited object changes).
	 *
	 * <p>
	 * Re-resolves the attribute from the new object's type and fires
	 * {@link com.top_logic.layout.form.model.FieldModelListener#onValueChanged} with the new value.
	 * </p>
	 */
	public void setOverlay(TLObjectOverlay newOverlay) {
		Object oldValue = getValue();
		_overlay = newOverlay;
		if (newOverlay != null) {
			_part = resolvePart(newOverlay);
			setMandatory(_part.isMandatory());
		}
		Object newValue = getValue();
		fireValueChanged(oldValue, newValue);
	}

	/**
	 * The resolved attribute part.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	private TLStructuredTypePart resolvePart(TLObjectOverlay overlay) {
		TLStructuredType type = overlay.tType();
		TLStructuredTypePart part = type.getPart(_attributeName);
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _attributeName + "' not found in type '" + type + "'.");
		}
		return part;
	}

	private static Object resolveValue(TLObjectOverlay overlay, String attributeName) {
		TLStructuredType type = overlay.tType();
		TLStructuredTypePart part = type.getPart(attributeName);
		if (part == null) {
			return null;
		}
		return overlay.tValue(part);
	}
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/OverlayFieldModel.java \
       com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/form/TestOverlayFieldModel.java
git commit -m "Ticket #29108: Add OverlayFieldModel for view-based model editing."
```

---

### Task 11: Refactor FieldControlFactory

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java`

Change from creating `View*Control` instances to creating `React*Control` instances with `FieldModel`.

- [ ] **Step 1: Refactor to use React controls**

Change all factory methods to create react module controls:

```java
public static ReactControl createFieldControl(ReactContext context,
        TLStructuredTypePart part, FieldModel model) {
    TLType type = part.getType();

    if (type instanceof TLPrimitive) {
        TLPrimitive primitive = (TLPrimitive) type;
        switch (primitive.getKind()) {
            case BOOLEAN:
                return new ReactCheckboxControl(context, model);
            case INT:
                return new ReactNumberInputControl(context, model, 0);
            case FLOAT:
                return new ReactNumberInputControl(context, model, 2);
            case DATE:
            case DATE_TIME:
                return new ReactDatePickerControl(context, model);
            case STRING:
            case TRISTATE:
            case BINARY:
                return new ReactTextInputControl(context, model);
            case CUSTOM:
                if (isColorType(primitive)) {
                    return createColorControl(context, model);
                }
                return new ReactTextInputControl(context, model);
            default:
                return new ReactTextInputControl(context, model);
        }
    }

    return new ReactTextInputControl(context, model);
}
```

The method signature changes: instead of `(ReactContext, TLStructuredTypePart, Object, boolean)` it takes `(ReactContext, TLStructuredTypePart, FieldModel)`. The value and editability come from the model.

- [ ] **Step 2: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: May fail if `FieldControl` still uses old signature — fixed in Task 12.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java
git commit -m "Ticket #29108: Refactor FieldControlFactory to create FieldModel-based React controls."
```

---

### Task 12: Simplify FieldControl

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControl.java`

- [ ] **Step 1: Replace value callback with FieldModel**

Major simplification:
- Remove `_innerControl` field — the control is created once and wired via `FieldModel`
- Remove `setupValueCallback()` method (the instanceof cascade)
- Remove `updateInnerControl()` method (the instanceof cascade)
- Remove `handleValueChange()` — the `OverlayFieldModel` handles this directly

The new `FieldControl` creates an `OverlayFieldModel` and passes it to the control. The model handles two-way sync.

```java
public class FieldControl {

    private final ReactContext _context;
    private final FormControl _form;
    private final String _attributeName;
    private final ResKey _labelOverride;
    private final boolean _forceReadonly;

    private OverlayFieldModel _model;
    private ReactFormFieldChromeControl _chrome;

    public FieldControl(ReactContext context, FormControl form, String attributeName,
            ResKey labelOverride, boolean forceReadonly) {
        _context = context;
        _form = form;
        _attributeName = attributeName;
        _labelOverride = labelOverride;
        _forceReadonly = forceReadonly;

        form.registerFieldControl(this);
    }

    public ReactFormFieldChromeControl createChromeControl() {
        TLObjectOverlay overlay = _form.getOverlay();
        if (overlay == null && _form.getCurrentObject() != null) {
            // View mode: create a temporary read-only overlay for value access
            overlay = new TLObjectOverlay(_form.getCurrentObject());
        }
        if (overlay == null) {
            return null;
        }

        _model = new OverlayFieldModel(overlay, _attributeName);
        _model.setEditable(_form.isEditMode() && !_forceReadonly);

        // Listen for value changes to update form dirty state
        _model.addListener(new FieldModelListener() {
            @Override
            public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
                _form.updateDirtyChannel();
            }

            @Override
            public void onEditabilityChanged(FieldModel source, boolean editable) {
                // Handled by the control via FieldModel listener.
            }

            @Override
            public void onValidationChanged(FieldModel source) {
                // Handled by the control via FieldModel listener.
            }
        });

        TLStructuredTypePart part = _model.getPart();
        ReactControl innerControl = FieldControlFactory.createFieldControl(
            _context, part, _model);

        String label = resolveLabel();
        boolean mandatory = part.isMandatory();
        boolean dirty = _model.isDirty();

        _chrome = new ReactFormFieldChromeControl(label, mandatory, dirty,
            null, null, null, false, true, innerControl);

        return _chrome;
    }

    public OverlayFieldModel getModel() {
        return _model;
    }

    public void setEditMode(boolean editMode) {
        if (_model != null) {
            _model.setEditable(editMode && !_forceReadonly);
        }
    }

    public void setOverlay(TLObjectOverlay newOverlay) {
        if (_model != null) {
            _model.setOverlay(newOverlay);
            if (_chrome != null) {
                _chrome.setDirty(_model.isDirty());
            }
        }
    }

    private String resolveLabel() {
        if (_labelOverride != null) {
            return Resources.getInstance().getString(_labelOverride);
        }
        return MetaLabelProvider.INSTANCE.getLabel(_model.getPart());
    }
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControl.java
git commit -m "Ticket #29108: Simplify FieldControl to use OverlayFieldModel."
```

---

### Task 13: Simplify FormControl

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java`

- [ ] **Step 1: Replace notifyFields with FieldModel-based approach**

Change `enterEditMode()` and `exitEditMode()`:
```java
void enterEditMode() {
    // ... lock, create overlay ...
    for (FieldControl field : _fieldControls) {
        field.setEditMode(true);
    }
}

private void exitEditMode() {
    // ... release lock ...
    for (FieldControl field : _fieldControls) {
        field.setEditMode(false);
    }
}
```

Change object switching:
```java
private void handleInputChanged(Object oldValue, Object newValue) {
    if (_editMode) {
        exitEditMode();
    }
    _currentObject = (TLObject) newValue;
    if (_currentObject != null) {
        TLObjectOverlay newOverlay = new TLObjectOverlay(_currentObject);
        for (FieldControl field : _fieldControls) {
            field.setOverlay(newOverlay);
        }
    }
}
```

Remove the `notifyFields()` method — it's no longer needed.

- [ ] **Step 2: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java
git commit -m "Ticket #29108: Simplify FormControl to use FieldModel-based editing."
```

---

### Task 14: Delete View*Controls and Cleanup

**Files:**
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewTextInputControl.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewCheckboxControl.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewNumberInputControl.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewDatePickerControl.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewSelectControl.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewColorInputControl.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewFieldValueChanged.java`

- [ ] **Step 1: Delete all View*Control files**

```bash
git rm com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewTextInputControl.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewCheckboxControl.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewNumberInputControl.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewDatePickerControl.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewSelectControl.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewColorInputControl.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewFieldValueChanged.java
```

- [ ] **Step 2: Remove ValueCallback references**

Search for any remaining references to `ValueCallback` or `ViewFieldValueChanged` in the codebase and remove them.

- [ ] **Step 3: Build the full project**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c && mvn install -DskipTests=true -pl com.top_logic,com.top_logic.layout.react,com.top_logic.layout.view -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add -u
git commit -m "Ticket #29108: Delete View*Controls and ValueCallback — replaced by FieldModel."
```

---

### Task 15: Full Build Verification

- [ ] **Step 1: Build all affected modules**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c && mvn install -DskipTests=true -pl com.top_logic,com.top_logic.layout.react,com.top_logic.layout.view,com.top_logic.demo -am`
Expected: BUILD SUCCESS

- [ ] **Step 2: Run tests in tl-core**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.form.model.TestAbstractFieldModel,test.com.top_logic.layout.form.model.TestFormFieldAdapter`
Expected: All tests PASS.

- [ ] **Step 3: Fix any issues discovered**

Address compilation or test failures.

- [ ] **Step 4: Commit fixes if needed**

```bash
git add -u
git commit -m "Ticket #29108: Fix build issues in FieldModel refactoring."
```

---

## Summary

| Task | What | Module |
|------|------|--------|
| 1 | FieldModel interface | tl-core |
| 2 | FieldModelListener + FieldConstraint | tl-core |
| 3 | AbstractFieldModel + tests | tl-core |
| 4 | SelectFieldModel + OptionsListener | tl-core |
| 5 | FormFieldAdapter + tests | tl-core |
| 6 | Refactor ReactFormFieldControl base | layout.react |
| 7 | Refactor React control subclasses | layout.react |
| 8 | Update call sites (FormFieldAdapter wrapping) | demo + others |
| 9 | Create ReactColorInputControl | layout.react |
| 10 | OverlayFieldModel | layout.view |
| 11 | Refactor FieldControlFactory | layout.view |
| 12 | Simplify FieldControl | layout.view |
| 13 | Simplify FormControl | layout.view |
| 14 | Delete View*Controls + cleanup | layout.view |
| 15 | Full build verification | all |

## Dependencies

```
Task 1 (FieldModel) ──────────────┐
Task 2 (Listener + Constraint) ───┤
                                   ├─> Task 3 (AbstractFieldModel)
                                   │         │
                                   ├─> Task 4 (SelectFieldModel)
                                   │         │
                                   ├─> Task 5 (FormFieldAdapter) ──> Task 8 (call sites)
                                   │                                       │
                                   ├─> Task 6 (ReactFormFieldControl) ─────┤
                                   │         │                              │
                                   ├─> Task 7 (React subclasses) ──────────┤
                                   │         │                              │
                                   ├─> Task 9 (ReactColorInputControl) ────┤
                                   │                                       │
                                   ├─> Task 10 (OverlayFieldModel) ────────┤
                                   │         │                              │
                                   ├─> Task 11 (FieldControlFactory) ──────┤
                                   │         │                              │
                                   ├─> Task 12 (FieldControl) ─────────────┤
                                   │         │                              │
                                   └─> Task 13 (FormControl) ─────────────> Task 14 (Delete) ──> Task 15 (Build)
```

Tasks 1-2 are independent. Tasks 3-5 depend on 1-2 but are independent of each other. Tasks 6-9 depend on 3-5. Tasks 10-13 depend on 6-9. Task 14 depends on 10-13. Task 15 depends on 14.
