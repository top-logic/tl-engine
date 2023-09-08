/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.FilterRegistry;

/**
 * Test for {@link FilterRegistry}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestFilterRegistry extends BasicTestCase {

	public static class MyFilter implements Filter<Integer> {

		/** Name of this filter in the test application config. */
		public static final String CONFIG_NAME = "myFilter";

		/** Singleton {@link MyFilter} instance. */
		public static final MyFilter INSTANCE = new MyFilter();

		private MyFilter() {
			// Just reduce the visibility
		}

		@Override
		public boolean accept(Integer anObject) {
			return anObject.intValue() >= 0;
		}

	}

	/**
	 * Tests that the <code>true</code> filter is configured.
	 */
	public void testTrue() {
		assertTrue(FilterFactory.isTrue(FilterRegistry.getFilter("true")));
	}

	/**
	 * Tests that the <code>false</code> filter is configured.
	 */
	public void testFalse() {
		assertTrue(FilterFactory.isFalse(FilterRegistry.getFilter("false")));
	}

	public void testNotConfigured() {
		assertNull(FilterRegistry.getFilter("notConfiguredFilterName"));
	}

	public void testConfigured() {
		Filter<Object> filter = FilterRegistry.getFilter(MyFilter.CONFIG_NAME);
		assertNotNull(filter);
		assertSame("Must not create new instance for singleton classes.", MyFilter.INSTANCE, filter);

		String filterName = FilterRegistry.getFilterName(MyFilter.INSTANCE);
		assertNotNull(filterName);
		assertEquals(MyFilter.CONFIG_NAME, filterName);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestFilterRegistry}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestFilterRegistry.class));
	}

}

