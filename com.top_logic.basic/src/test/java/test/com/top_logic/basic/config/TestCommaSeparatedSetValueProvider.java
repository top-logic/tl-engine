/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.CommaSeparatedSetValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * Test for {@link CommaSeparatedSetValueProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCommaSeparatedSetValueProvider extends TestCase {

	private static class TestClass {

		String _name;

		public TestClass(String name) {
			_name = name;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof TestClass && CollectionUtil.equals(_name, ((TestClass) obj)._name);
		}

		@Override
		public int hashCode() {
			return 17 + (_name != null ? _name.hashCode() : 0);
		}
	}

	private static class TestingCommaSeparatedSetValueProvider extends CommaSeparatedSetValueProvider<TestClass> {

		public TestingCommaSeparatedSetValueProvider() {
		}

		@Override
		protected TestClass parseSingleValue(String propertyName, CharSequence propertyValue,
				String singlePropertyValue) throws ConfigurationException {
			return new TestClass(singlePropertyValue);
		}

		@Override
		protected String formatSingleValue(TestClass singleConfigValue) {
			return singleConfigValue._name;
		}

	}

	public void testSpecification() {
		ConfigurationValueProvider<Set<TestClass>> valueProvider = new TestingCommaSeparatedSetValueProvider();
		assertEquals("a,b,x", valueProvider.getSpecification(CollectionUtil.linkedSet(tc("a"), tc("b"), tc("x"))));
		assertEquals("a;b;x", valueProvider.getSpecification(CollectionUtil.set(tc("a;b;x"))));
		assertEquals("a,,x", valueProvider.getSpecification(CollectionUtil.linkedSet(tc("a"), tc(""), tc("x"))));
		assertEquals("", valueProvider.getSpecification(CollectionUtil.set()));
		assertEquals("", valueProvider.getSpecification(null));
	}

	public void testGetValue() throws ConfigurationException {
		ConfigurationValueProvider<Set<TestClass>> valueProvider = new TestingCommaSeparatedSetValueProvider();
		String prop = "";

		assertEquals(CollectionUtil.set(tc("a"), tc("b"), tc("c")), valueProvider.getValue(prop, "a,c,b"));
		assertEquals(CollectionUtil.set(tc("a"), tc("b"), tc("c")), valueProvider.getValue(prop, "   a,c, c   ,b,a"));
		assertEquals(CollectionUtil.set(tc("a;b;c")), valueProvider.getValue(prop, "   a;b;c"));

		assertEquals(CollectionUtil.set(), valueProvider.getValue(prop, null));
		assertEquals(CollectionUtil.set(), valueProvider.getValue(prop, ""));
		assertEquals(CollectionUtil.set(), valueProvider.getValue(prop, "    "));
	}

	static TestClass tc(String name) {
		return new TestClass(name);
	}

}

