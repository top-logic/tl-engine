/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormFactory;

/**
 * @author <a href="mailto:tma@top-logic.com">tma</a>
 */
public class TestStringField extends TestCase {
	
	public void testNormalizedInitialValue() {
		FormField field = FormFactory.newStringField("name");

		final BooleanFlag eventReceived = new BooleanFlag(false);
		field.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				eventReceived.set(true);
			}
		});

		field.setValue(field.getValue());
		assertFalse("Ticket #6809: setting the same value again must not trigger event", eventReceived.get());
	}

	/**
	 * Tests a valid update of a {@link FormField}
	 */
	public void testValidUpdate() throws VetoException {
		FormField field = FormFactory.newStringField("name");

		StringLengthConstraint constraint = new StringLengthConstraint(0, 3);
		field.addConstraint(constraint);

		field.update("tma");

		field.check();

		assertTrue(field.isValid());
		assertFalse(field.hasError());
		assertEquals("tma", field.getValue());
		assertEquals("tma", field.getRawValue());
	}
	
	/**
	 * Tests an invalid update of a {@link FormField}
	 */
	public void testInvalidUpdate() throws VetoException {
		FormField field = FormFactory.newStringField("name");

		StringLengthConstraint constraint = new StringLengthConstraint(0, 3);
		field.addConstraint(constraint);

		final String TMA = "tma_too_long";

		field.update(TMA);

		field.check();

		assertFalse(field.isValid());
		assertTrue(field.hasError());
		assertNotNull(field.getError());
		assertEquals(TMA, field.getValue()); // the value
		assertEquals(TMA, field.getRawValue());
	}
	
	public static Test suite() {
        TestSuite theSuite = new TestSuite(TestStringField.class);
        
        return TLTestSetup.createTLTestSetup(theSuite);
    }
	
}
