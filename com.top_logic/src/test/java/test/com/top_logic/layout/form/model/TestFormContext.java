/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.RawValueListener;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;

/**
 * Tests the class {@link com.top_logic.layout.form.model.FormContext}.
 * 
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
public class TestFormContext extends TestFormGroup {
	
	private static final String INITIAL_VALUE = "otto";
	private static final String USER_PROVIDED_VALUE = "tim";
	private static final String SERVER_PROVIDED_VALUE = "alexander";

	private boolean called;
	
	/**
	 * Returns an instance of FormContext.
	 * 
	 * @see AbstractFormContainerTest#createContainer(String, ResPrefix)
	 */
	@Override
	protected FormContainer createContainer(String name, ResPrefix i18n) {
		return new FormContext(name,i18n);
	}
	
	/**
	 * Tests the {@link RawValueListener} implementation.
	 */
	public void testPropertyListener() throws Exception {
		final StringField nameField = FormFactory.newStringField("name", INITIAL_VALUE, true, false, new StringLengthConstraint(1,9));
		final IntField ageField = FormFactory.newIntField("age", Integer.valueOf(29), true);

		FormContext context = new FormContext("person", ResPrefix.forTest("i18n"));
		context.addMember(nameField);
		context.addMember(ageField);
		
		context.addListener(FormField.VALUE_PROPERTY, new RawValueListener() {
		
			@Override
			public Bubble handleRawValueChanged(FormField field, Object oldValue, Object newValue) {
				called = true;
				assertEquals(INITIAL_VALUE, oldValue);

				// The value of the value property is the GUI-layer
				// representation of the value (raw value). For string fields,
				// this value is identical to the model value.
				assertEquals(SERVER_PROVIDED_VALUE, newValue);

				assertTrue(nameField == field);
				return Bubble.BUBBLE;
			}
		
		});
		
		nameField.setValue(SERVER_PROVIDED_VALUE);
		assertTrue("A server-side change of the field does fire a property event for updating the view.", called);
	}
	
	public static Test suite() {
		Test test = new TestSuite(TestFormContext.class);
		test = ServiceTestSetup.createSetup(test, SecurityObjectProviderManager.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}
	
}
