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

import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableAndFilter;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * {@link TestCase} for the {@link ConfigurableAndFilter}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigurableAndFilter extends AbstractTestConfigurableFilter {

	public void testNoInnerFilter() {
		List<PolymorphicConfiguration<? extends TypedFilter>> innerFilter = list();
		PolymorphicConfiguration<? extends TypedFilter> filter = newAndFilter(innerFilter);
		assertEquals(Object.class, createInstance(filter).getType());
		assertIsInapplicable(filter, new Object());
	}

	public void testSingletonIsIdentity() {
		PolymorphicConfiguration<? extends TypedFilter> numberFilter = newNumberFilter(-5, +5);
		PolymorphicConfiguration<? extends TypedFilter> andFilter = newAndFilter(list(numberFilter));
		assertEqualMatchResults(numberFilter, andFilter, -10);
		assertEqualMatchResults(numberFilter, andFilter, 0);
		assertEqualMatchResults(numberFilter, andFilter, +10);
		assertEqualMatchResults(numberFilter, andFilter, "Hello World!");
		assertEqualMatchResults(numberFilter, andFilter, null);
	}

	public void testAndTrueIsIdentity() throws ConfigurationException {
		PolymorphicConfiguration<? extends TypedFilter> numberFilter = newNumberFilter(-5, +5);
		PolymorphicConfiguration<? extends TypedFilter> andFilter =
			newAndFilter(list(numberFilter, config(TrueFilter.class)));
		assertEqualMatchResults(numberFilter, andFilter, -10);
		assertEqualMatchResults(numberFilter, andFilter, 0);
		assertEqualMatchResults(numberFilter, andFilter, +10);

		assertIsInapplicable(numberFilter, "Hello World!");
		assertAccepts(andFilter, "Hello World!");
		assertIsInapplicable(numberFilter, null);
		assertAccepts(andFilter, null);
	}

	public void testSingleInnerFilter() {
		List<PolymorphicConfiguration<? extends TypedFilter>> innerFilter = list(newNumberFilter(-5, +5));
		PolymorphicConfiguration<? extends TypedFilter> filter = newAndFilter(innerFilter);
		assertEquals(Object.class, createInstance(filter).getType());
		assertRejects(filter, -10);
		assertAccepts(filter, 0);
		assertRejects(filter, +10);
		assertIsInapplicable(filter, "Hello World!");
	}

	public void testMultipleInnerFilters() {
		List<PolymorphicConfiguration<? extends TypedFilter>> innerFilter = list(
			newNumberFilter(-3, +1),
			newNumberFilter(-1, +3));
		PolymorphicConfiguration<? extends TypedFilter> filter = newAndFilter(innerFilter);
		assertEquals(Object.class, createInstance(filter).getType());
		assertRejects(filter, -4);
		assertRejects(filter, -2);
		assertAccepts(filter, 0);
		assertRejects(filter, +2);
		assertRejects(filter, +4);
		assertIsInapplicable(filter, "Hello World!");
	}

	private PolymorphicConfiguration<? extends TypedFilter> newAndFilter(
			List<PolymorphicConfiguration<? extends TypedFilter>> innerFilters) {
		ConfigurableAndFilter.Config config = TypedConfiguration.newConfigItem(ConfigurableAndFilter.Config.class);
		set(config, ConfigurableAndFilter.Config.FILTERS, innerFilters);
		return config;
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigurableAndFilter.class, TypeIndex.Module.INSTANCE);
	}
}
