/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.filter;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.col.filter.TestFilterRegistry.MyFilter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.FilterFormat;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Tests the {@link FilterFormat}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestFilterFormat extends BasicTestCase {

	public static class GeZeroFilter implements Filter<Integer> {

		/** Singleton {@link GeZeroFilter} instance. */
		public static final GeZeroFilter INSTANCE = new GeZeroFilter();

		private GeZeroFilter() {
			// singleton instance
		}

		@Override
		public boolean accept(Integer anObject) {
			return anObject >= 0;
		}

	}

	public void testTrue() throws ConfigurationException {
		assertTrue(FilterFactory.isTrue(getFilter("true")));
	}

	public void testFalse() throws ConfigurationException {
		assertTrue(FilterFactory.isFalse(getFilter("false")));
	}

	public void testSimpleFilter() throws ConfigurationException {
		String filterName = larger0FilterName();
		assertSame(GeZeroFilter.INSTANCE, getFilter("class:" + filterName));
	}

	public void testConfiguredFilter() throws ConfigurationException {
		assertSame(MyFilter.INSTANCE, getFilter(MyFilter.CONFIG_NAME));
	}

	private String larger0FilterName() throws AssertionFailedError {
		return checkExistence("test.com.top_logic.basic.col.filter.TestFilterFormat$GeZeroFilter");
	}

	private String checkExistence(String filterName) throws AssertionFailedError {
		try {
			Class.forName(filterName);
		} catch (ClassNotFoundException ex) {
			throw fail(filterName + " has been moved?", ex);
		}
		return filterName;
	}

	@SuppressWarnings("unchecked")
	private Filter<Object> getFilter(String propertyValue) throws ConfigurationException {
		return (Filter<Object>) FilterFormat.INSTANCE.getValue("propName", propertyValue);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestFilterFormat}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestFilterFormat.class));
	}

}
