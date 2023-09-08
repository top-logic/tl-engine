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

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.configurable.DoubleRangeFilter;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.col.filter.typed.TypedFilterAdapter;
import com.top_logic.basic.col.filter.typed.TypedFilterAdapter.Config;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * {@link TestCase} for the {@link TypedFilterAdapter}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedFilterAdapter extends AbstractTestConfigurableFilter {

	public void testDefaultFalse() {
		Filter<Object> filter = newAdapter(createInstance(newNumberFilter(2, 4)), false);
		assertFalse(filter.accept(1));
		assertTrue(filter.accept(3));
		assertFalse(filter.accept(5));
		assertFalse(filter.accept(5));
		assertFalse(filter.accept("Hello World!"));
		assertFalse(filter.accept(null));
	}

	public void testDefaultTrue() {
		Filter<Object> filter = newAdapter(createInstance(newNumberFilter(2, 4)), true);
		assertFalse(filter.accept(1));
		assertTrue(filter.accept(3));
		assertFalse(filter.accept(5));
		assertFalse(filter.accept(5));
		assertTrue(filter.accept("Hello World!"));
		assertTrue(filter.accept(null));
	}

	public void testType() {
		DoubleRangeFilter filter = createInstance(newNumberFilter(-5, +5));
		assertEquals(Number.class, filter.getType());
	}

	private Filter<Object> newAdapter(TypedFilter typedFilter, boolean defaultValue) {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(TypedFilterAdapter.Config.class);
		set(builder, TypedFilterAdapter.Config.FILTER, typedFilter);
		set(builder, TypedFilterAdapter.Config.DEFAULT, defaultValue);
		TypedFilterAdapter.Config config = (Config) builder.createConfig(getInstantiationContext());
		return getInstantiationContext().getInstance(config);
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestTypedFilterAdapter.class, TypeIndex.Module.INSTANCE);
	}

}
