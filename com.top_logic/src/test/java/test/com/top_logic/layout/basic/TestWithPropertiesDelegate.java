/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.io.IOException;

import junit.framework.Test;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.TagWriter.State;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.template.NoSuchPropertyException;

/**
 * Test for {@link WithPropertiesDelegate}.
 */
@SuppressWarnings("javadoc")
public class TestWithPropertiesDelegate extends AbstractLayoutTest {

	public class A {

		@TemplateVariable("x")
		public String getX() {
			return "x";
		}

		@TemplateVariable("y")
		public String getY() {
			return "y";
		}

	}

	public class B extends A {

		@Override
		public String getY() {
			return "y1";
		}

		@TemplateVariable("z")
		public String getZ() {
			return "z";
		}
	}

	public void testGet() throws NoSuchPropertyException {
		// Note: Test subclass first to check recursive delegate creation.

		A b = new B();
		WithPropertiesDelegate delegateB = WithPropertiesDelegate.lookup(B.class);
		assertEquals("x", delegateB.getPropertyValue(b, "x"));
		assertEquals("y1", delegateB.getPropertyValue(b, "y"));
		assertEquals("z", delegateB.getPropertyValue(b, "z"));

		A a = new A();

		WithPropertiesDelegate delegateA = WithPropertiesDelegate.lookup(A.class);
		assertEquals("x", delegateA.getPropertyValue(a, "x"));
		assertEquals("y", delegateA.getPropertyValue(a, "y"));
	}

	public void testRender() throws IOException {
		A b = new B();
		WithPropertiesDelegate delegateB = WithPropertiesDelegate.lookup(B.class);
		assertRender("x", delegateB, b, "x");
		assertRender("y1", delegateB, b, "y");
		assertRender("z", delegateB, b, "z");

		A a = new A();

		WithPropertiesDelegate delegateA = WithPropertiesDelegate.lookup(A.class);
		assertRender("x", delegateA, a, "x");
		assertRender("y", delegateA, a, "y");
	}

	private void assertRender(String expected, WithPropertiesDelegate delegate, Object self, String propertyName)
			throws IOException {
		TagWriter out = new TagWriter();
		out.setState(State.ELEMENT_CONTENT);
		delegate.renderProperty(displayContext(), out, self, propertyName);
		assertEquals(expected, out.toString());
	}

	public static Test suite() {
		return suite(TestWithPropertiesDelegate.class);
	}
}
