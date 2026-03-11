/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.FormFactory;
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
		StringField field = FormFactory.newStringField("test");
		field.initializeField("hello");
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
		StringField field = FormFactory.newStringField("test");
		field.initializeField("original");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertFalse(adapter.isDirty());

		adapter.setValue("changed");
		assertTrue(adapter.isDirty());
	}

	/**
	 * Test editability delegation.
	 */
	public void testEditable() {
		StringField field = FormFactory.newStringField("test");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertTrue(adapter.isEditable());

		field.setImmutable(true);
		assertFalse(adapter.isEditable());
	}

	/**
	 * Test mandatory delegation.
	 */
	public void testMandatory() {
		StringField field = FormFactory.newStringField("test");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertFalse(adapter.isMandatory());

		field.setMandatory(true);
		assertTrue(adapter.isMandatory());
	}

	/**
	 * Test label accessor.
	 */
	public void testLabel() {
		StringField field = FormFactory.newStringField("test");
		field.setLabel("Test Label");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		assertEquals("Test Label", adapter.getLabel());
	}

	/**
	 * Test value change listener forwarding.
	 */
	public void testValueChangeListener() {
		StringField field = FormFactory.newStringField("test");
		field.initializeField("a");
		FormFieldAdapter adapter = new FormFieldAdapter(field);

		List<Object> newValues = new ArrayList<>();
		adapter.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				newValues.add(newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not tested here.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Not tested here.
			}
		});

		field.setValue("b");
		assertEquals(1, newValues.size());
		assertEquals("b", newValues.get(0));
	}

	/**
	 * Test suite for {@link TestFormFieldAdapter}.
	 */
	public static Test suite() {
		TestSuite theSuite = new TestSuite(TestFormFieldAdapter.class);
		return TLTestSetup.createTLTestSetup(theSuite);
	}
}
