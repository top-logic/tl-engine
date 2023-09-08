/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.filter.configurable;

import static com.top_logic.basic.config.misc.TypedConfigUtil.*;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.col.filter.FalseFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableOrFilter;
import com.top_logic.basic.col.filter.configurable.DoubleRangeFilter;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * {@link TestCase} for the {@link ConfigurableOrFilter}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigurableOrFilter extends AbstractTestConfigurableFilter {

	public void testNoInnerFilter() {
		List<PolymorphicConfiguration<? extends TypedFilter>> innerFilter = list();
		PolymorphicConfiguration<? extends TypedFilter> filter = newOrFilter(innerFilter);
		assertEquals(Object.class, createInstance(filter).getType());
		assertIsInapplicable(filter, new Object());
	}

	public void testSingletonIsIdentity() {
		PolymorphicConfiguration<? extends TypedFilter> numberFilter = newNumberFilter(-5, +5);
		PolymorphicConfiguration<? extends TypedFilter> orFilter = newOrFilter(list(numberFilter));
		assertEqualMatchResults(numberFilter, orFilter, -10);
		assertEqualMatchResults(numberFilter, orFilter, 0);
		assertEqualMatchResults(numberFilter, orFilter, +10);
		assertEqualMatchResults(numberFilter, orFilter, "Hello World!");
		assertEqualMatchResults(numberFilter, orFilter, null);
	}

	public void testOrFalseIsIdentity() {
		PolymorphicConfiguration<DoubleRangeFilter> numberFilter = newNumberFilter(-5, +5);
		PolymorphicConfiguration<? extends TypedFilter> orFilter = newOrFilter(list(numberFilter, falseFilter()));
		assertEqualMatchResults(numberFilter, orFilter, -10);
		assertEqualMatchResults(numberFilter, orFilter, 0);
		assertEqualMatchResults(numberFilter, orFilter, +10);

		assertIsInapplicable(numberFilter, "Hello World!");
		assertRejects(orFilter, "Hello World!");
		assertIsInapplicable(numberFilter, null);
		assertRejects(orFilter, null);
	}

	private PolymorphicConfiguration<? extends TypedFilter> falseFilter() {
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<FalseFilter> result = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		result.setImplementationClass(FalseFilter.class);
		return result;
	}

	public void testSingleInnerFilter() {
		List<PolymorphicConfiguration<? extends TypedFilter>> innerFilter = list(newNumberFilter(-5, +5));
		PolymorphicConfiguration<? extends TypedFilter> filter = newOrFilter(innerFilter);
		assertEquals(Object.class, createInstance(filter).getType());
		assertRejects(filter, -10);
		assertAccepts(filter, 0);
		assertRejects(filter, +10);
		assertIsInapplicable(filter, "Hello World!");
	}

	public void testMultipleInnerFilters() {
		List<PolymorphicConfiguration<? extends TypedFilter>> innerFilter = list(
			newNumberFilter(-3, -1),
			newNumberFilter(+1, +3));
		PolymorphicConfiguration<? extends TypedFilter> filter = newOrFilter(innerFilter);
		assertEquals(Object.class, createInstance(filter).getType());
		assertRejects(filter, -4);
		assertAccepts(filter, -2);
		assertRejects(filter, 0);
		assertAccepts(filter, +2);
		assertRejects(filter, +4);
		assertIsInapplicable(filter, "Hello World!");
	}

	private PolymorphicConfiguration<? extends TypedFilter> newOrFilter(
			List<PolymorphicConfiguration<? extends TypedFilter>> innerFilters) {
		ConfigurableOrFilter.Config config = TypedConfiguration.newConfigItem(ConfigurableOrFilter.Config.class);
		set(config, ConfigurableOrFilter.Config.FILTERS, innerFilters);
		return config;
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigurableOrFilter.class, TypeIndex.Module.INSTANCE);
	}
}
