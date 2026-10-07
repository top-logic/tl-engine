/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
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
		assertFalse(model.isDisabled());
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
		assertEquals(1, listener._valueChanges.size());
		assertEquals("hello", listener._valueChanges.get(0)._oldValue);
		assertEquals("world", listener._valueChanges.get(0)._newValue);
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
		assertEquals(0, listener._valueChanges.size());
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
		assertEquals(1, listener._editabilityChanges.size());
		assertFalse(listener._editabilityChanges.get(0).booleanValue());
	}

	/**
	 * Test setDisabled makes the field non-editable and fires both the disabled and the
	 * editability change.
	 */
	public void testSetDisabled() {
		AbstractFieldModel model = createModel(null);
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);

		model.setDisabled(true);

		assertTrue(model.isDisabled());
		assertFalse(model.isEditable());
		assertEquals(List.of(Boolean.TRUE), listener._disabledChanges);
		assertEquals(List.of(Boolean.FALSE), listener._editabilityChanges);
		assertEquals(1, listener._validationChanges);

		model.setDisabled(false);

		assertFalse(model.isDisabled());
		assertTrue(model.isEditable());
		assertEquals(List.of(Boolean.TRUE, Boolean.FALSE), listener._disabledChanges);
		assertEquals(List.of(Boolean.FALSE, Boolean.TRUE), listener._editabilityChanges);
		assertEquals(2, listener._validationChanges);
	}

	/**
	 * Test setDisabled with the current state does not fire.
	 */
	public void testSetSameDisabled() {
		AbstractFieldModel model = createModel(null);
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);

		model.setDisabled(false);

		assertFalse(model.isDisabled());
		assertTrue(model.isEditable());
		assertEquals(0, listener._disabledChanges.size());
		assertEquals(0, listener._editabilityChanges.size());
		assertEquals(0, listener._validationChanges);
	}

	/**
	 * Test disabling a non-editable field fires no editability change, since the effective
	 * editability stays the same.
	 */
	public void testDisableNonEditable() {
		AbstractFieldModel model = createModel(null);
		model.setEditable(false);
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);

		model.setDisabled(true);

		assertTrue(model.isDisabled());
		assertFalse(model.isEditable());
		assertEquals(List.of(Boolean.TRUE), listener._disabledChanges);
		assertEquals(0, listener._editabilityChanges.size());
		assertEquals(0, listener._validationChanges);
	}

	/**
	 * Test setEditable on a disabled field keeps the field non-editable and fires no editability
	 * change; the editable state applies once the field is no longer disabled.
	 */
	public void testSetEditableWhileDisabled() {
		AbstractFieldModel model = createModel(null);
		model.setEditable(false);
		model.setDisabled(true);
		RecordingListener listener = new RecordingListener();
		model.addListener(listener);

		model.setEditable(true);

		assertFalse(model.isEditable());
		assertTrue(model.isDisabled());
		assertEquals(0, listener._editabilityChanges.size());
		assertEquals(0, listener._disabledChanges.size());

		model.setDisabled(false);

		assertTrue(model.isEditable());
		assertEquals(List.of(Boolean.FALSE), listener._disabledChanges);
		assertEquals(List.of(Boolean.TRUE), listener._editabilityChanges);
	}

	/**
	 * Test model validation errors and warnings are hidden while the field is disabled, like for
	 * any non-editable field.
	 */
	public void testValidationHiddenWhileDisabled() {
		AbstractFieldModel model = createModel(null);
		model.setRevealed(true);
		model.setModelValidationError(ResKey.text("error"));
		model.setModelValidationWarnings(List.of(ResKey.text("warning")));
		assertTrue(model.hasError());
		assertTrue(model.hasWarnings());

		model.setDisabled(true);

		assertFalse(model.hasError());
		assertNull(model.getError());
		assertFalse(model.hasWarnings());
		assertTrue(model.getWarnings().isEmpty());
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
		assertEquals(0, listener._valueChanges.size());
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

	/**
	 * Recording listener for tests. Also used by TestFormFieldAdapter.
	 */
	public static class RecordingListener implements FieldModelListener {

		/** Recorded value changes. */
		public final List<ValueChange> _valueChanges = new ArrayList<>();

		/** Recorded editability changes. */
		public final List<Boolean> _editabilityChanges = new ArrayList<>();

		/** Recorded disabled state changes. */
		public final List<Boolean> _disabledChanges = new ArrayList<>();

		/** Count of validation change notifications. */
		public int _validationChanges = 0;

		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			_valueChanges.add(new ValueChange(oldValue, newValue));
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			_editabilityChanges.add(editable);
		}

		@Override
		public void onDisabledChanged(FieldModel source, boolean disabled) {
			_disabledChanges.add(disabled);
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			_validationChanges++;
		}
	}

	/**
	 * Records a value change event.
	 */
	public static class ValueChange {

		/** The old value. */
		public final Object _oldValue;

		/** The new value. */
		public final Object _newValue;

		ValueChange(Object oldValue, Object newValue) {
			_oldValue = oldValue;
			_newValue = newValue;
		}
	}
}
