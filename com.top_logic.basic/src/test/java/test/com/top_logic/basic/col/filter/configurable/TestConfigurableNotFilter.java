/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.filter.configurable;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableNotFilter;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * {@link TestCase} for the {@link ConfigurableNotFilter}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigurableNotFilter extends AbstractTestConfigurableFilter {

	public void testNegation() {
		PolymorphicConfiguration<? extends TypedFilter> filter = newNotFilter(newNumberFilter(-5, +5));
		assertAccepts(filter, -10);
		assertRejects(filter, 0);
		assertAccepts(filter, +10);
		assertIsInapplicable(filter, "Hello World!");
		assertIsInapplicable(filter, null);
	}

	public void testNotNotIsIdentity() throws ConfigurationException {
		PolymorphicConfiguration<? extends TypedFilter> numberFilter = newNumberFilter(-5, +5);
		PolymorphicConfiguration<? extends TypedFilter> notNotFilter = newNotFilter(newNotFilter(numberFilter));

		assertEqualMatchResults(numberFilter, notNotFilter, -10);
		assertEqualMatchResults(numberFilter, notNotFilter, 0);
		assertEqualMatchResults(numberFilter, notNotFilter, +10);
		assertEqualMatchResults(numberFilter, notNotFilter, "Hello World!");
		assertEqualMatchResults(numberFilter, notNotFilter, null);

		PolymorphicConfiguration<? extends TypedFilter> trueFilter = config(TrueFilter.class);
		PolymorphicConfiguration<? extends TypedFilter> notNotTrueFilter = newNotFilter(newNotFilter(trueFilter));
		assertEqualMatchResults(trueFilter, notNotTrueFilter, null);
	}

	public void testType() {
		PolymorphicConfiguration<? extends TypedFilter> filter = newNotFilter(newNumberFilter(-5, +5));
		assertEquals(Number.class, TypedConfigUtil.createInstance(filter).getType());
	}

	private PolymorphicConfiguration<? extends TypedFilter> newNotFilter(
			PolymorphicConfiguration<? extends TypedFilter> innerFilter) {
		ConfigurableNotFilter.Config<?> config =
			TypedConfiguration.newConfigItem(ConfigurableNotFilter.Config.class);
		set(config, ConfigurableNotFilter.Config.FILTER, innerFilter);
		return config;
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigurableNotFilter.class, TypeIndex.Module.INSTANCE);
	}
}
