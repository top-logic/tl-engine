/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.control;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;

import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;

/**
 * Tests the order in which {@link TextInputControl#getPlaceHolder()} takes its text: the
 * placeholder set at the control, the placeholder of the field, the
 * {@link TextInputControl#LABEL_PLACEHOLDER} of the field.
 */
@SuppressWarnings("javadoc")
public class TestTextInputControlPlaceholder extends TestControl {

	public void testNoPlaceholder() {
		StringField field = FormFactory.newStringField("field");
		assertNull(new TextInputControl(field).getPlaceHolder());
	}

	public void testFieldPlaceholder() {
		StringField field = FormFactory.newStringField("field");
		field.setPlaceholder("inherited");
		assertEquals("inherited", new TextInputControl(field).getPlaceHolder());
	}

	public void testLabelPlaceholder() {
		StringField field = FormFactory.newStringField("field");
		field.set(TextInputControl.LABEL_PLACEHOLDER, "Name");
		assertEquals("Name", new TextInputControl(field).getPlaceHolder());
	}

	public void testFieldPlaceholderBeforeLabelPlaceholder() {
		StringField field = FormFactory.newStringField("field");
		field.set(TextInputControl.LABEL_PLACEHOLDER, "Name");
		field.setPlaceholder("inherited");
		assertEquals("inherited", new TextInputControl(field).getPlaceHolder());
	}

	/**
	 * The value a field inherits, given as its placeholder, takes the place of the label while it
	 * is set; without it, the label is shown again.
	 */
	public void testInheritedValueTakesPlaceOfLabel() {
		StringField field = FormFactory.newStringField("field");
		field.set(TextInputControl.LABEL_PLACEHOLDER, "Name");
		TextInputControl control = new TextInputControl(field);
		assertEquals("Name", control.getPlaceHolder());

		field.setPlaceholder("inherited");
		assertEquals("inherited", control.getPlaceHolder());

		field.setPlaceholder(null);
		assertEquals("Name", control.getPlaceHolder());
	}

	public void testControlPlaceholderBeforeLabelPlaceholder() {
		StringField field = FormFactory.newStringField("field");
		field.set(TextInputControl.LABEL_PLACEHOLDER, "Name");
		TextInputControl control = new TextInputControl(field);
		control.setPlaceHolder("Search");
		assertEquals("Search", control.getPlaceHolder());
	}

	public static Test suite() {
		return TestControl.suite(TestTextInputControlPlaceholder.class);
	}

}
