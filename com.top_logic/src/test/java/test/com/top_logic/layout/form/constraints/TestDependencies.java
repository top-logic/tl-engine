/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.listener.CopyValueListenerDependency;
import com.top_logic.layout.form.listener.PresetValueListenerDependency;
import com.top_logic.layout.form.model.FormFactory;

/**
 * Tests some dependencies.
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class TestDependencies extends TestCase {

	public void testCopyValueListenerDependency() {
		FormField field1 = FormFactory.newStringField("field1");
		FormField field2 = FormFactory.newStringField("field2");
		CopyValueListenerDependency dependency = new CopyValueListenerDependency(field1, field2);
		dependency.attach();
		field1.setValue("foo");
		assertEquals("foo", field1.getValue());
		assertEquals("foo", field2.getValue());
		field1.setValue("bar");
		assertEquals("bar", field1.getValue());
		assertEquals("bar", field2.getValue());
		field2.setValue("Hallo");
		assertEquals("bar", field1.getValue());
		assertEquals("Hallo", field2.getValue());
		field1.setValue("Test");
		assertEquals("Test", field1.getValue());
		assertEquals("Test", field2.getValue());
	}

	public void testPresetValueListenerDependency() {
		FormField field1 = FormFactory.newStringField("field1");
		FormField field2 = FormFactory.newStringField("field2");
		PresetValueListenerDependency dependency = new PresetValueListenerDependency(field1, field2);
		dependency.attach();
		field1.setValue("foo");
		assertEquals("foo", field1.getValue());
		assertEquals("foo", field2.getValue());
		field1.setValue("bar");
		assertEquals("bar", field1.getValue());
		assertEquals("bar", field2.getValue());
		field2.setValue("Hallo");
		assertEquals("bar", field1.getValue());
		assertEquals("Hallo", field2.getValue());
		field1.setValue("Test");
		assertEquals("Test", field1.getValue());
		assertEquals("Hallo", field2.getValue());
	}

	/**
	 * Returns the test suite.
	 */
	public static Test suite() {
		TestSuite theSuite = new TestSuite(TestDependencies.class);
		return TLTestSetup.createTLTestSetup(theSuite);
	}

	/**
	 * The main method.
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestDependencies.class);
	}

}
