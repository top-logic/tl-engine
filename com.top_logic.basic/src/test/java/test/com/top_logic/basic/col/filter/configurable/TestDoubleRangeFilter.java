/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.filter.configurable;

import static com.top_logic.basic.config.misc.TypedConfigUtil.*;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.filter.configurable.DoubleRangeFilter;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * {@link TestCase} for the {@link DoubleRangeFilter}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestDoubleRangeFilter extends AbstractTestConfigurableFilter {

	public void testSimple() {
		PolymorphicConfiguration<? extends TypedFilter> filter = newNumberFilter(2, 4);
		assertRejects(filter, 1);
		assertAccepts(filter, 3);
		assertRejects(filter, 5);
	}

	public void testInfinity() {
		PolymorphicConfiguration<? extends TypedFilter> filter = newNumberFilter(2, Double.POSITIVE_INFINITY);
		assertRejects(filter, 1);
		assertAccepts(filter, 3);
		assertAccepts(filter, Double.MAX_VALUE);
		assertAccepts(filter, Double.POSITIVE_INFINITY);
	}

	public void testBorders() {
		PolymorphicConfiguration<? extends TypedFilter> filter = newNumberFilter(2, 4);
		assertRejects(filter, 1);
		assertRejects(filter, Math.nextAfter(2, 1));
		assertAccepts(filter, 2);
		assertAccepts(filter, Math.nextAfter(2, 3));
		assertAccepts(filter, 3);
		assertAccepts(filter, Math.nextAfter(4, 3));
		assertAccepts(filter, 4);
		assertRejects(filter, Math.nextAfter(4, 5));
		assertRejects(filter, 5);
	}

	public void testPoint() {
		PolymorphicConfiguration<? extends TypedFilter> filter = newNumberFilter(2, 2);
		assertRejects(filter, 1);
		assertAccepts(filter, 2);
		assertRejects(filter, 3);
	}

	public void testInverseBorders() {
		try {
			createInstance(newNumberFilter(4, 2));
		} catch (ConfigurationError ex) {
			// Good
			return;
		}
		fail("The minimum is higher than the maximum, but filter instantiation does not fail.");
	}

	public void testNanAsMin() {
		try {
			createInstance(newNumberFilter(Double.NaN, 1));
		} catch (ConfigurationError ex) {
			// Good
			return;
		}
		fail("The minimum is NaN, but filter instantiation does not fail.");
	}

	public void testNanAsMax() {
		try {
			createInstance(newNumberFilter(1, Double.NaN));
		} catch (ConfigurationError ex) {
			// Good
			return;
		}
		fail("The maximum is NaN, but filter instantiation does not fail.");
	}

	public void testNanBoth() {
		try {
			createInstance(newNumberFilter(Double.NaN, Double.NaN));
		} catch (ConfigurationError ex) {
			// Good
			return;
		}
		fail("The minimum and the maximum are both NaN, but filter instantiation does not fail.");
	}

	public void testType() {
		PolymorphicConfiguration<? extends TypedFilter> filter = newNumberFilter(-5, +5);
		assertEquals(Number.class, TypedConfigUtil.createInstance(filter).getType());
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestDoubleRangeFilter.class, TypeIndex.Module.INSTANCE);
	}

}
