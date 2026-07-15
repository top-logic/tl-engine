# FormElement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add `<form>` and `<field>` elements to the view system for declarative object editing with overlay-based transient state.

**Architecture:** A `FormElement` container manages the editing lifecycle (view/edit mode, locking, KB transactions) using a lean `TLObjectOverlay` as transient edit buffer. Nested `FieldElement`s resolve attributes from the TL type system and render via `ReactFormFieldChromeControl` wrapping new lightweight input controls that work without the legacy `FormField` hierarchy.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, ReactControl (SSE-based), TL model APIs (tl-core only), JUnit 4

**Design document:** `docs/plans/2026-03-06-form-element-design.md`

---

## Task 1: TLObjectOverlay

Create the lean overlay that wraps a persistent `TLObject` and intercepts writes into a local map.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/TLObjectOverlay.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/form/TestTLObjectOverlay.java`

**Step 1: Write the test**

```java
package test.com.top_logic.layout.view.form;

import junit.framework.TestCase;

import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Tests for {@link TLObjectOverlay}.
 */
public class TestTLObjectOverlay extends TestCase {

	/**
	 * Test that unchanged attributes delegate to the base object.
	 */
	public void testReadThrough() {
		TLObject base = createMockObject("name", "Alice");
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		TLStructuredTypePart namePart = resolvePart(base, "name");
		assertEquals("Alice", overlay.tValue(namePart));
	}

	/**
	 * Test that writes are captured in the overlay.
	 */
	public void testWriteCapture() {
		TLObject base = createMockObject("name", "Alice");
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		TLStructuredTypePart namePart = resolvePart(base, "name");
		overlay.tUpdate(namePart, "Bob");

		assertEquals("Bob", overlay.tValue(namePart));
		assertEquals("Alice", base.tValue(namePart));
	}

	/**
	 * Test that the overlay reports dirty state.
	 */
	public void testDirty() {
		TLObject base = createMockObject("name", "Alice");
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		assertFalse(overlay.isDirty());

		TLStructuredTypePart namePart = resolvePart(base, "name");
		overlay.tUpdate(namePart, "Bob");

		assertTrue(overlay.isDirty());
		assertTrue(overlay.isChanged(namePart));
	}

	/**
	 * Test that reset clears all changes.
	 */
	public void testReset() {
		TLObject base = createMockObject("name", "Alice");
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		TLStructuredTypePart namePart = resolvePart(base, "name");
		overlay.tUpdate(namePart, "Bob");
		overlay.reset();

		assertFalse(overlay.isDirty());
		assertEquals("Alice", overlay.tValue(namePart));
	}

	/**
	 * Test that applyTo transfers changes to target.
	 */
	public void testApplyTo() {
		TLObject base = createMockObject("name", "Alice");
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		TLStructuredTypePart namePart = resolvePart(base, "name");
		overlay.tUpdate(namePart, "Bob");
		overlay.applyTo(base);

		assertEquals("Bob", base.tValue(namePart));
	}

	/**
	 * Test that tType delegates to base object.
	 */
	public void testTypeFromBase() {
		TLObject base = createMockObject("name", "Alice");
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		assertSame(base.tType(), overlay.tType());
	}

	/**
	 * Test that setting value to null is tracked as a change.
	 */
	public void testSetNull() {
		TLObject base = createMockObject("name", "Alice");
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		TLStructuredTypePart namePart = resolvePart(base, "name");
		overlay.tUpdate(namePart, null);

		assertTrue(overlay.isChanged(namePart));
		assertNull(overlay.tValue(namePart));
	}

	// Helper methods - these need a mock or TransientTLObjectImpl.
	// The actual test will use TransientTLObjectImpl as the "base" since
	// it doesn't require a running KnowledgeBase. However, creating one
	// requires a TLClass from the model, which needs TypeIndex.
	// Alternative: use a simple mock approach with a Map-backed object.
	// Decision will depend on what's simplest without a full service stack.
}
```

Note: The test helpers (`createMockObject`, `resolvePart`) need to either use `TransientTLObjectImpl` (requires `TypeIndex.Module.INSTANCE` in `suite()`) or a simpler mock approach. Since `TLObjectOverlay` only calls `tValue()`, `tUpdate()`, and `tType()` on the base object, a minimal mock extending `TransientObject` with a Map-based store would work without any service setup. Choose whichever is simpler during implementation.

**Step 2: Run the test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.form.TestTLObjectOverlay`
Expected: Compilation failure — `TLObjectOverlay` does not exist.

**Step 3: Write the implementation**

```java
package com.top_logic.layout.view.form;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;

/**
 * Lean overlay over a persistent {@link TLObject} that intercepts writes.
 *
 * <p>
 * Unchanged attributes delegate to the base object. Changed attributes are stored in a local map.
 * This serves as the transient editing buffer for the form system.
 * </p>
 */
public class TLObjectOverlay extends TransientObject {

	private final TLObject _base;

	private final Map<TLStructuredTypePart, Object> _changes = new LinkedHashMap<>();

	/**
	 * Creates a new overlay over the given base object.
	 *
	 * @param base
	 *        The persistent object to overlay. Must not be {@code null}.
	 */
	public TLObjectOverlay(TLObject base) {
		_base = base;
	}

	@Override
	public TLStructuredType tType() {
		return _base.tType();
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		if (_changes.containsKey(part)) {
			return _changes.get(part);
		}
		return _base.tValue(part);
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		_changes.put(part, value);
	}

	/**
	 * Whether any attribute has been changed.
	 */
	public boolean isDirty() {
		return !_changes.isEmpty();
	}

	/**
	 * Whether the given attribute has been changed in this overlay.
	 */
	public boolean isChanged(TLStructuredTypePart part) {
		return _changes.containsKey(part);
	}

	/**
	 * Transfers all accumulated changes to the given target object.
	 *
	 * @param target
	 *        The object to apply changes to. Typically the original base object within a KB
	 *        transaction.
	 */
	public void applyTo(TLObject target) {
		for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
			target.tUpdate(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Discards all accumulated changes. After reset, all reads delegate to the base object again.
	 */
	public void reset() {
		_changes.clear();
	}

	/**
	 * The base object this overlay wraps.
	 */
	public TLObject getBase() {
		return _base;
	}
}
```

**Step 4: Run the test to verify it passes**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.form.TestTLObjectOverlay`
Expected: All tests PASS.

**Step 5: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/TLObjectOverlay.java \
       com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/form/TestTLObjectOverlay.java
git commit -m "Ticket #29108: Add TLObjectOverlay for lean transient editing state."
```

---

## Task 2: ViewContext Form Support

Extend `ViewContext` with a form-control slot so nested `<field>` elements can access the enclosing form.

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`

**Step 1: Add the form control slot**

Add to `ViewContext`:

```java
// New import (will exist after Task 5):
// import com.top_logic.layout.view.form.FormControl;

// For now, use Object type to avoid circular dependency.
// Will be refined when FormControl is implemented.
private Object _formControl;

/**
 * Sets the form control for this context.
 *
 * <p>
 * Called by {@link com.top_logic.layout.view.element.FormElement} during
 * {@link UIElement#createControl(ViewContext)} to make the form available to nested field
 * elements.
 * </p>
 */
public void setFormControl(Object formControl) {
    _formControl = formControl;
}

/**
 * The form control of the nearest enclosing form element, or {@code null} if no form is in
 * scope.
 */
public Object getFormControl() {
    return _formControl;
}
```

The private constructor and `childContext()` must propagate `_formControl`. Add it to the private constructor parameters and pass it through in `childContext()` and `withCommandScope()`.

**Step 2: Verify existing tests still pass**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false`
Expected: All existing tests PASS (no behavior change for existing code).

**Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java
git commit -m "Ticket #29108: Add form control slot to ViewContext for nested field element access."
```

---

## Task 3: Lean Input Controls

Create new lightweight `ReactControl` subclasses for form field input that work with plain typed values instead of `FormField` objects. These reuse the existing React components (`TLTextInput`, `TLCheckbox`, `TLNumberInput`, `TLDatePicker`, `TLSelect`) but without the `FormField` listener coupling.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewTextInputControl.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewCheckboxControl.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewNumberInputControl.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewDatePickerControl.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewSelectControl.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewFieldValueChanged.java`

**Step 1: Create the shared value-changed command**

```java
package com.top_logic.layout.view.form;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command handler for value changes from lean view field controls.
 *
 * <p>
 * Unlike {@link com.top_logic.layout.react.control.form.ReactFormFieldControl}'s ValueChanged
 * command, this does not depend on {@link com.top_logic.layout.form.FormField}. Instead, it
 * delegates to the control's {@link ValueChangeHandler} interface.
 * </p>
 */
public class ViewFieldValueChanged extends ControlCommand {

	/** Singleton instance. */
	public static final ViewFieldValueChanged INSTANCE = new ViewFieldValueChanged();

	private static final String VALUE = "value";

	private ViewFieldValueChanged() {
		super("valueChanged");
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.FIELD_VALUE_CHANGED;
	}

	@Override
	protected HandlerResult execute(DisplayContext context, Control control,
			Map<String, Object> arguments) {
		Object rawValue = arguments.get(VALUE);
		((ValueChangeHandler) control).handleValueChanged(rawValue);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Interface for controls that handle value changes from the client.
	 */
	public interface ValueChangeHandler {

		/**
		 * Called when the client sends a new value.
		 *
		 * @param rawValue
		 *        The raw value from the client (type depends on control kind).
		 */
		void handleValueChanged(Object rawValue);
	}
}
```

**Step 2: Create ViewTextInputControl**

```java
package com.top_logic.layout.view.form;

import java.util.Map;

import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.ReactControl;

/**
 * Lean text input control for the view form system.
 *
 * <p>
 * Renders the {@code TLTextInput} React component but without {@code FormField} dependency.
 * </p>
 */
public class ViewTextInputControl extends ReactControl implements ViewFieldValueChanged.ValueChangeHandler {

	private static final String VALUE = "value";

	private static final String EDITABLE = "editable";

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(ViewFieldValueChanged.INSTANCE);

	private ValueCallback _callback;

	/**
	 * Creates a new text input control.
	 *
	 * @param value
	 *        The initial text value. May be {@code null}.
	 * @param editable
	 *        Whether the field is editable.
	 */
	public ViewTextInputControl(String value, boolean editable) {
		super(null, "TLTextInput", COMMANDS);
		putState(VALUE, value != null ? value : "");
		putState(EDITABLE, editable);
	}

	/**
	 * Sets a callback to be notified when the value changes.
	 */
	public void setValueCallback(ValueCallback callback) {
		_callback = callback;
	}

	/**
	 * Updates the displayed value.
	 */
	public void setValue(String value) {
		putState(VALUE, value != null ? value : "");
	}

	/**
	 * Updates the editable state.
	 */
	public void setEditable(boolean editable) {
		putState(EDITABLE, editable);
	}

	@Override
	public void handleValueChanged(Object rawValue) {
		String value = rawValue != null ? rawValue.toString() : null;
		putState(VALUE, value != null ? value : "");
		if (_callback != null) {
			_callback.valueChanged(value);
		}
	}

	/**
	 * Callback for value changes.
	 */
	public interface ValueCallback {

		/**
		 * Called when the field value changes.
		 *
		 * @param newValue
		 *        The new typed value.
		 */
		void valueChanged(Object newValue);
	}
}
```

**Step 3: Create ViewCheckboxControl, ViewNumberInputControl, ViewDatePickerControl, ViewSelectControl**

Follow the same pattern as `ViewTextInputControl`:
- Each extends `ReactControl` and implements `ViewFieldValueChanged.ValueChangeHandler`
- Each uses the corresponding React module name (`TLCheckbox`, `TLNumberInput`, `TLDatePicker`, `TLSelect`)
- Each handles type-appropriate value conversion in `handleValueChanged()`:
  - Checkbox: `Boolean` value, raw value is boolean from client
  - NumberInput: `Number` value, raw value is number from client; constructor takes `int decimalPlaces` and sets `config.decimal` state
  - DatePicker: `String` ISO date value from client (date parsing deferred to later phase)
  - Select: `Object` value from options list; constructor takes `List<Map<String, Object>> options`
- Each has the same `ValueCallback` interface and `setValueCallback()` method (reuse the one from `ViewTextInputControl` — extract to a shared location or use the same interface since it's generic)

**Step 4: Add I18NConstants entry**

Add to `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/I18NConstants.java`:

```java
/**
 * @en Field value changed.
 */
public static ResKey FIELD_VALUE_CHANGED;
```

**Step 5: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile`
Expected: Compilation succeeds.

**Step 6: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/
git commit -m "Ticket #29108: Add lean view input controls for form system."
```

---

## Task 4: FieldControlFactory

Create the factory that maps `TLStructuredTypePart` types to the appropriate lean input control.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java`

**Step 1: Write the factory**

```java
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * Maps {@link TLStructuredTypePart} types to lean React input controls.
 *
 * <p>
 * Uses only tl-core model APIs. No dependency on {@code com.top_logic.element}.
 * </p>
 */
public class FieldControlFactory {

	/**
	 * Creates the appropriate input control for the given attribute.
	 *
	 * @param part
	 *        The model attribute.
	 * @param value
	 *        The current value.
	 * @param editable
	 *        Whether the field should be editable.
	 * @return A React control for the field input widget.
	 */
	public static ReactControl createFieldControl(TLStructuredTypePart part, Object value,
			boolean editable) {
		TLType type = part.getType();

		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			switch (primitive.getKind()) {
				case BOOLEAN:
					return createCheckbox(value, editable);
				case INT:
					return createNumberInput(value, editable, 0);
				case FLOAT:
					return createNumberInput(value, editable, 2);
				case DATE:
				case DATE_TIME:
					return createDatePicker(value, editable);
				case STRING:
				case TRISTATE:
				case BINARY:
				case CUSTOM:
				default:
					return createTextInput(value, editable);
			}
		}

		// Reference types and unknown: display as text for now
		return createTextInput(asLabel(value), editable);
	}

	private static ReactControl createTextInput(Object value, boolean editable) {
		return new ViewTextInputControl(value != null ? value.toString() : "", editable);
	}

	private static ReactControl createCheckbox(Object value, boolean editable) {
		return new ViewCheckboxControl(Boolean.TRUE.equals(value), editable);
	}

	private static ReactControl createNumberInput(Object value, boolean editable, int decimals) {
		return new ViewNumberInputControl(value instanceof Number ? (Number) value : null,
			editable, decimals);
	}

	private static ReactControl createDatePicker(Object value, boolean editable) {
		return new ViewDatePickerControl(value, editable);
	}

	private static String asLabel(Object value) {
		if (value == null) {
			return "";
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}
}
```

**Step 2: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile`
Expected: Compilation succeeds.

**Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java
git commit -m "Ticket #29108: Add FieldControlFactory for TL type to input control mapping."
```

---

## Task 5: FormControl

Create the session-scoped form control that manages the editing lifecycle.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java`

**Step 1: Write the implementation**

```java
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.Lock;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Session-scoped control managing the form editing lifecycle.
 *
 * <p>
 * Handles view/edit mode switching, lock acquisition, overlay management, and KB transactions.
 * </p>
 */
public class FormControl extends ReactControl {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		EditCommand.INSTANCE,
		ApplyCommand.INSTANCE,
		SaveCommand.INSTANCE,
		CancelCommand.INSTANCE);

	private final ViewChannel _inputChannel;
	private final ViewChannel _editModeChannel;
	private final ViewChannel _dirtyChannel;

	private TLObject _currentObject;
	private TLObjectOverlay _overlay;
	private boolean _editMode;
	private Lock _lock;

	private final List<FieldControl> _fields = new ArrayList<>();

	/**
	 * Creates a new form control.
	 *
	 * @param inputChannel
	 *        Channel providing the object to display/edit.
	 * @param editModeChannel
	 *        Optional channel to expose edit mode state. May be {@code null}.
	 * @param dirtyChannel
	 *        Optional channel to expose dirty state. May be {@code null}.
	 */
	public FormControl(ViewChannel inputChannel, ViewChannel editModeChannel,
			ViewChannel dirtyChannel) {
		super(null, "TLFormLayout", COMMANDS);
		_inputChannel = inputChannel;
		_editModeChannel = editModeChannel;
		_dirtyChannel = dirtyChannel;
		_currentObject = (TLObject) inputChannel.get();

		inputChannel.addListener(this::handleInputChanged);
	}

	/**
	 * Registers a field control with this form.
	 */
	public void registerField(FieldControl field) {
		_fields.add(field);
	}

	/**
	 * The current object being displayed or edited.
	 *
	 * <p>
	 * In edit mode, this returns the {@link TLObjectOverlay}. In view mode, this returns the
	 * persistent object from the input channel.
	 * </p>
	 */
	public TLObject getCurrentObject() {
		if (_editMode && _overlay != null) {
			return _overlay;
		}
		return _currentObject;
	}

	/**
	 * The overlay for the current edit session, or {@code null} if not in edit mode.
	 */
	public TLObjectOverlay getOverlay() {
		return _overlay;
	}

	/**
	 * Whether the form is currently in edit mode.
	 */
	public boolean isEditMode() {
		return _editMode;
	}

	private void handleInputChanged(ViewChannel sender, Object oldValue, Object newValue) {
		if (_editMode) {
			exitEditMode();
		}
		_currentObject = (TLObject) newValue;
		notifyFields();
	}

	void enterEditMode() {
		if (_currentObject == null) {
			return;
		}
		_lock = LockService.getInstance().createLock("editValues", _currentObject);
		_lock.lock();

		_overlay = new TLObjectOverlay(_currentObject);
		_editMode = true;
		updateChannels();
		notifyFields();
	}

	void executeApply() {
		if (!_editMode || _overlay == null) {
			return;
		}
		TLObject base = _overlay.getBase();
		KnowledgeBase kb = base.tHandle().getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			_overlay.applyTo(base);
			tx.commit();
		} finally {
			tx.rollback();
		}
		_overlay.reset();
		updateDirtyChannel();
		notifyFields();
	}

	void executeSave() {
		executeApply();
		exitEditMode();
	}

	void executeCancel() {
		exitEditMode();
	}

	private void exitEditMode() {
		_overlay = null;
		_editMode = false;
		if (_lock != null) {
			_lock.unlock();
			_lock = null;
		}
		updateChannels();
		notifyFields();
	}

	private void updateChannels() {
		if (_editModeChannel != null) {
			_editModeChannel.set(_editMode);
		}
		updateDirtyChannel();
	}

	void updateDirtyChannel() {
		if (_dirtyChannel != null) {
			_dirtyChannel.set(_overlay != null && _overlay.isDirty());
		}
	}

	private void notifyFields() {
		for (FieldControl field : _fields) {
			field.refresh();
		}
	}

	// --- Internal commands ---

	static class EditCommand extends ControlCommand {
		static final EditCommand INSTANCE = new EditCommand();

		EditCommand() {
			super("edit");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_EDIT;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).enterEditMode();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	static class ApplyCommand extends ControlCommand {
		static final ApplyCommand INSTANCE = new ApplyCommand();

		ApplyCommand() {
			super("apply");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_APPLY;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).executeApply();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	static class SaveCommand extends ControlCommand {
		static final SaveCommand INSTANCE = new SaveCommand();

		SaveCommand() {
			super("save");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_SAVE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).executeSave();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	static class CancelCommand extends ControlCommand {
		static final CancelCommand INSTANCE = new CancelCommand();

		CancelCommand() {
			super("cancel");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_CANCEL;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).executeCancel();
			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
```

**Step 2: Add I18NConstants entries**

Add to `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/I18NConstants.java`:

```java
/**
 * @en Edit.
 */
public static ResKey FORM_EDIT;

/**
 * @en Apply.
 */
public static ResKey FORM_APPLY;

/**
 * @en Save.
 */
public static ResKey FORM_SAVE;

/**
 * @en Cancel.
 */
public static ResKey FORM_CANCEL;
```

**Step 3: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile`
Expected: Compilation succeeds.

**Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/I18NConstants.java
git commit -m "Ticket #29108: Add FormControl with edit lifecycle management."
```

---

## Task 6: FieldControl

Create the per-field session control that bridges model attributes to React input controls.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControl.java`

**Step 1: Write the implementation**

```java
package com.top_logic.layout.view.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Per-field session control that bridges a model attribute to a React input control.
 *
 * <p>
 * Resolves the attribute from the current object's type, creates the appropriate input control via
 * {@link FieldControlFactory}, and wraps it in {@link ReactFormFieldChromeControl} for
 * label/required/dirty/error display.
 * </p>
 */
public class FieldControl {

	private final FormControl _form;

	private final String _attributeName;

	private final ResKey _labelOverride;

	private final boolean _forceReadonly;

	private TLStructuredTypePart _resolvedPart;

	private ReactFormFieldChromeControl _chrome;

	private ReactControl _innerControl;

	/**
	 * Creates a new field control.
	 *
	 * @param form
	 *        The enclosing form control.
	 * @param attributeName
	 *        The attribute name to resolve against the object's type.
	 * @param labelOverride
	 *        Optional label override. {@code null} to use the attribute's label.
	 * @param forceReadonly
	 *        Whether this field is always read-only, even in edit mode.
	 */
	public FieldControl(FormControl form, String attributeName, ResKey labelOverride,
			boolean forceReadonly) {
		_form = form;
		_attributeName = attributeName;
		_labelOverride = labelOverride;
		_forceReadonly = forceReadonly;

		form.registerField(this);
	}

	/**
	 * Creates the chrome-wrapped React control for this field.
	 *
	 * @return The {@link ReactFormFieldChromeControl} wrapping the inner input control, or
	 *         {@code null} if no current object is available.
	 */
	public ReactFormFieldChromeControl createChromeControl() {
		TLObject current = _form.getCurrentObject();
		if (current == null) {
			return null;
		}

		_resolvedPart = resolvePart(current);
		String label = resolveLabel();
		boolean editable = _form.isEditMode() && !_forceReadonly;
		Object value = current.tValue(_resolvedPart);

		_innerControl = FieldControlFactory.createFieldControl(_resolvedPart, value, editable);
		setupValueCallback();

		boolean mandatory = _resolvedPart.isMandatory();
		_chrome = new ReactFormFieldChromeControl(label, mandatory, false, null, null, null,
			false, true, _innerControl);

		return _chrome;
	}

	/**
	 * Refreshes the field after form state changes (object changed, edit mode toggled).
	 */
	public void refresh() {
		TLObject current = _form.getCurrentObject();
		if (current == null || _chrome == null) {
			return;
		}

		TLStructuredTypePart newPart = resolvePart(current);
		if (newPart != _resolvedPart) {
			_resolvedPart = newPart;
			_chrome.setLabel(resolveLabel());
			_chrome.setRequired(_resolvedPart.isMandatory());
		}

		Object value = current.tValue(_resolvedPart);
		boolean editable = _form.isEditMode() && !_forceReadonly;

		// Update inner control state.
		// The inner control type may need to change if the attribute type differs
		// between objects. For now, update value and editability in-place.
		updateInnerControl(value, editable);

		// Update dirty indicator
		TLObjectOverlay overlay = _form.getOverlay();
		boolean dirty = overlay != null && overlay.isChanged(_resolvedPart);
		_chrome.setDirty(dirty);
	}

	private TLStructuredTypePart resolvePart(TLObject object) {
		TLStructuredType type = object.tType();
		TLStructuredTypePart part = type.getPart(_attributeName);
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _attributeName + "' not found in type '" + type + "'.");
		}
		return part;
	}

	private String resolveLabel() {
		if (_labelOverride != null) {
			// ResKey resolution requires a ResourcesModule. For now, use toString.
			return _labelOverride.toString();
		}
		return MetaLabelProvider.INSTANCE.getLabel(_resolvedPart);
	}

	private void setupValueCallback() {
		if (_innerControl instanceof ViewTextInputControl) {
			((ViewTextInputControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewCheckboxControl) {
			((ViewCheckboxControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewNumberInputControl) {
			((ViewNumberInputControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewDatePickerControl) {
			((ViewDatePickerControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewSelectControl) {
			((ViewSelectControl) _innerControl).setValueCallback(this::handleValueChange);
		}
	}

	private void handleValueChange(Object newValue) {
		TLObjectOverlay overlay = _form.getOverlay();
		if (overlay != null) {
			overlay.tUpdate(_resolvedPart, newValue);
			_chrome.setDirty(true);
			_form.updateDirtyChannel();
		}
	}

	private void updateInnerControl(Object value, boolean editable) {
		if (_innerControl instanceof ViewTextInputControl) {
			ViewTextInputControl text = (ViewTextInputControl) _innerControl;
			text.setValue(value != null ? value.toString() : "");
			text.setEditable(editable);
		} else if (_innerControl instanceof ViewCheckboxControl) {
			ViewCheckboxControl cb = (ViewCheckboxControl) _innerControl;
			cb.setValue(Boolean.TRUE.equals(value));
			cb.setEditable(editable);
		}
		// ... similar for other control types
	}
}
```

Note: The `setupValueCallback` and `updateInnerControl` methods have repetitive `instanceof` checks. This can be cleaned up by extracting a common `ViewFieldControl` interface with `setValueCallback()`, `setValue(Object)`, and `setEditable(boolean)` methods. Consider this during implementation if the pattern feels too verbose.

**Step 2: Build to verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile`
Expected: Compilation succeeds.

**Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControl.java
git commit -m "Ticket #29108: Add FieldControl bridging model attributes to React input controls."
```

---

## Task 7: FormElement (UIElement)

Create the `<form>` view element that is the configuration/factory for `FormControl`.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestFormElement.java`
- Test resource: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-form.view.xml`

**Step 1: Write the test view XML**

```xml
<?xml version="1.0" encoding="utf-8" ?>

<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="selectedItem"/>
    <channel name="isEditing"/>
    <channel name="isDirty"/>
  </channels>

  <form input="selectedItem" editMode="isEditing" dirty="isDirty">
    <children>
      <field attribute="name"/>
    </children>
  </form>
</view>
```

**Step 2: Write the parsing test**

```java
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.FieldElement;
import com.top_logic.layout.view.channel.ChannelRef;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

/**
 * Tests for {@link FormElement} XML parsing.
 */
public class TestFormElement extends TestCase {

	/**
	 * Tests that a form view XML is parsed correctly.
	 */
	public void testParseFormView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestFormElement.class, "test-form.view.xml"));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertNotNull(config);

		// Verify the form element config
		FormElement.Config formConfig = (FormElement.Config) config.getContent().get(0);
		assertNotNull(formConfig.getInput());
		assertEquals("selectedItem", formConfig.getInput().getChannelName());
		assertNotNull(formConfig.getEditMode());
		assertEquals("isEditing", formConfig.getEditMode().getChannelName());
		assertNotNull(formConfig.getDirty());
		assertEquals("isDirty", formConfig.getDirty().getChannelName());

		// Verify child field element
		assertEquals(1, formConfig.getChildren().size());
		FieldElement.Config fieldConfig = (FieldElement.Config) formConfig.getChildren().get(0);
		assertEquals("name", fieldConfig.getAttribute());
	}

	/**
	 * Suite with TypeIndex.
	 */
	public static Test suite() {
		return com.top_logic.basic.SimpleTestFactory.newTestSuite(
			com.top_logic.basic.module.ServiceTestSetup.createSetup(
				TestFormElement.class,
				com.top_logic.basic.reflect.TypeIndex.Module.INSTANCE));
	}
}
```

**Step 3: Run the test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.element.TestFormElement`
Expected: Compilation failure — `FormElement` does not exist.

**Step 4: Write FormElement**

```java
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FormControl;

/**
 * View element providing a form editing context.
 *
 * <p>
 * Receives the edited object via an input channel, manages the editing lifecycle (view/edit mode,
 * locking, transactions), and provides the form context to nested {@link FieldElement}s.
 * </p>
 *
 * <p>
 * Usage in view XML:
 * </p>
 *
 * <pre>
 * &lt;form input="selectedItem" editMode="isEditing" dirty="isDirty"&gt;
 *   &lt;field attribute="name"/&gt;
 *   &lt;field attribute="email"/&gt;
 * &lt;/form&gt;
 * </pre>
 */
public class FormElement extends ContainerElement {

	/**
	 * Configuration of a {@link FormElement}.
	 */
	@TagName("form")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getEditMode()}. */
		String EDIT_MODE = "editMode";

		/** Configuration name for {@link #getDirty()}. */
		String DIRTY = "dirty";

		@Override
		@com.top_logic.basic.config.annotation.defaults.ClassDefault(FormElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel reference providing the {@link com.top_logic.model.TLObject} to display/edit.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Optional channel reference to expose the edit mode state (boolean).
		 */
		@Name(EDIT_MODE)
		@Format(ChannelRefFormat.class)
		ChannelRef getEditMode();

		/**
		 * Optional channel reference to expose the dirty state (boolean).
		 */
		@Name(DIRTY)
		@Format(ChannelRefFormat.class)
		ChannelRef getDirty();
	}

	private final Config _config;

	/**
	 * Creates a new {@link FormElement}.
	 */
	@CalledByReflection
	public FormElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		ViewChannel inputChannel = context.resolveChannel(_config.getInput());

		ViewChannel editModeChannel = _config.getEditMode() != null
			? context.resolveChannel(_config.getEditMode())
			: null;

		ViewChannel dirtyChannel = _config.getDirty() != null
			? context.resolveChannel(_config.getDirty())
			: null;

		FormControl formControl = new FormControl(inputChannel, editModeChannel, dirtyChannel);

		// Store form control in context so nested FieldElements can access it
		context.setFormControl(formControl);

		// Create child controls (fields and layout elements)
		List<ViewControl> childControls = createChildControls(context);

		// The FormControl wraps child controls as its React children
		// FormControl extends ReactControl("TLFormLayout") which renders children
		List<ReactControl> reactChildren = childControls.stream()
			.filter(c -> c instanceof ReactControl)
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());
		formControl.putState("children", reactChildren);

		return formControl;
	}
}
```

**Step 5: Write FieldElement**

```java
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.FieldControl;
import com.top_logic.layout.view.form.FormControl;

/**
 * View element rendering a single attribute field within a {@link FormElement}.
 *
 * <p>
 * Resolves the attribute from the current object's TL type and creates the appropriate input
 * control. Must be nested inside a {@link FormElement}.
 * </p>
 *
 * <p>
 * Usage in view XML:
 * </p>
 *
 * <pre>
 * &lt;field attribute="name"/&gt;
 * &lt;field attribute="email" readonly="true"/&gt;
 * </pre>
 */
public class FieldElement implements UIElement {

	/**
	 * Configuration of a {@link FieldElement}.
	 */
	@TagName("field")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		@Override
		@com.top_logic.basic.config.annotation.defaults.ClassDefault(FieldElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The attribute name to display. Resolved against the form object's
		 * {@link com.top_logic.model.TLStructuredType}.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * Optional label override. If not set, the label is derived from the attribute via
		 * {@link com.top_logic.layout.provider.MetaLabelProvider}.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * Whether this field is always read-only, even when the form is in edit mode.
		 */
		@Name(READONLY)
		boolean getReadonly();
	}

	private final Config _config;

	/**
	 * Creates a new {@link FieldElement}.
	 */
	@CalledByReflection
	public FieldElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		FormControl formControl = (FormControl) context.getFormControl();
		if (formControl == null) {
			throw new IllegalStateException(
				"<field> element must be nested inside a <form> element. "
					+ "Attribute: '" + _config.getAttribute() + "'");
		}

		FieldControl fieldControl = new FieldControl(
			formControl, _config.getAttribute(), _config.getLabel(), _config.getReadonly());

		ReactFormFieldChromeControl chrome = fieldControl.createChromeControl();
		return chrome;
	}
}
```

**Step 6: Run the parsing test**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.element.TestFormElement`
Expected: All tests PASS.

**Step 7: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FieldElement.java \
       com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestFormElement.java \
       com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-form.view.xml
git commit -m "Ticket #29108: Add FormElement and FieldElement for declarative form editing."
```

---

## Task 8: Build and Verify

Run the full module build to ensure everything compiles and all existing tests still pass.

**Step 1: Full build with tests**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn clean install -DskipTests=false`
Expected: BUILD SUCCESS, all tests pass.

**Step 2: Fix any compilation or test issues discovered**

Address any issues found during the full build.

**Step 3: Commit any fixes**

```bash
git add -A
git commit -m "Ticket #29108: Fix build issues in form element implementation."
```

---

## Summary

| Task | What | Key Files |
|------|------|-----------|
| 1 | TLObjectOverlay | `form/TLObjectOverlay.java`, test |
| 2 | ViewContext form slot | `ViewContext.java` modification |
| 3 | Lean input controls | `form/ViewTextInputControl.java`, etc. |
| 4 | FieldControlFactory | `form/FieldControlFactory.java` |
| 5 | FormControl | `form/FormControl.java` |
| 6 | FieldControl | `form/FieldControl.java` |
| 7 | FormElement + FieldElement | `element/FormElement.java`, `element/FieldElement.java`, tests |
| 8 | Build verification | Full module build |

## Dependencies

```
Task 1 (TLObjectOverlay) ─────────┐
Task 2 (ViewContext)  ─────────────┤
Task 3 (Lean input controls) ──────┤
                                    ├──> Task 5 (FormControl)
Task 4 (FieldControlFactory) ──────┤           │
                                    │           ├──> Task 7 (FormElement + FieldElement)
                                    ├──> Task 6 (FieldControl)         │
                                                                        ├──> Task 8 (Build)
```

Tasks 1-4 are independent and can be implemented in parallel. Task 5 depends on 1 and 3. Task 6 depends on 4 and 5. Task 7 depends on 2, 5, and 6. Task 8 depends on all.
