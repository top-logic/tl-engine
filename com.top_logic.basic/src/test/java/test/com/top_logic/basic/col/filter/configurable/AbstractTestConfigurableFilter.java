/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.filter.configurable;

import static com.top_logic.basic.config.misc.TypedConfigUtil.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;

import junit.framework.TestCase;

import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.DoubleRangeFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Common code for all {@link TestCase}s of {@link ConfigurableFilter}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractTestConfigurableFilter extends TestCase {

	/**
	 * Asserts that the given {@link TypedFilter} returns {@link FilterResult#TRUE} for the given
	 * value.
	 */
	protected static void assertAccepts(PolymorphicConfiguration<? extends TypedFilter> filter, Object value) {
		assertEquals(FilterResult.TRUE, createInstance(filter).matches(value));
	}

	/**
	 * Asserts that the given {@link TypedFilter} returns {@link FilterResult#FALSE} for the given
	 * value.
	 */
	protected static void assertRejects(PolymorphicConfiguration<? extends TypedFilter> filter, Object value) {
		assertEquals(FilterResult.FALSE, createInstance(filter).matches(value));
	}

	/**
	 * Asserts that the given {@link TypedFilter} returns {@link FilterResult#INAPPLICABLE} for the
	 * given value.
	 */
	protected static void assertIsInapplicable(PolymorphicConfiguration<? extends TypedFilter> filter, Object value) {
		assertEquals(FilterResult.INAPPLICABLE, createInstance(filter).matches(value));
	}

	/**
	 * Asserts that both filter return the same {@link MatchResult} for the given object.
	 */
	protected static void assertEqualMatchResults(PolymorphicConfiguration<? extends TypedFilter> firstFilter,
			PolymorphicConfiguration<? extends TypedFilter> secondFilter, Object object) {
		FilterResult firstMatchResult = createInstance(firstFilter).matches(object);
		FilterResult secondMatchResult = createInstance(secondFilter).matches(object);
		assertEquals(firstMatchResult, secondMatchResult);
	}

	/**
	 * Creates a {@link List} from the given array of {@link TypedFilter}s.
	 * <p>
	 * This method is used to avoid warnings when calling {@link Arrays#asList(Object...)} with
	 * filters whose type parameter is not just <?>.
	 * </p>
	 */
	@SafeVarargs
	protected static <T> List<T> list(T... filters) {
		return Arrays.asList(filters);
	}

	/**
	 * Creates a {@link PolymorphicConfiguration} for the given filter class that has no special
	 * configuration interface.
	 */
	protected static <T> PolymorphicConfiguration<T> config(Class<T> type) throws ConfigurationException {
		return TypedConfiguration.createConfigItemForImplementationClass(type);
	}

	/**
	 * Create a {@link DoubleRangeFilter} for the given range.
	 */
	protected PolymorphicConfiguration<DoubleRangeFilter> newNumberFilter(double min, double max) {
		DoubleRangeFilter.Config config = TypedConfiguration.newConfigItem(DoubleRangeFilter.Config.class);
		set(config, DoubleRangeFilter.Config.MIN, min);
		set(config, DoubleRangeFilter.Config.MAX, max);
		return config;
	}

	/**
	 * Set the value in the property with the given name in the given {@link ConfigurationItem}.
	 */
	protected void set(ConfigurationItem config, String property, Object value) {
		set(config, config.descriptor().getProperty(property), value);
	}

	/**
	 * Set the value in the given property in the given {@link ConfigurationItem}.
	 */
	protected void set(ConfigurationItem config, PropertyDescriptor property, Object value) {
		config.update(property, value);
	}

	/**
	 * Get the {@link InstantiationContext} for creating {@link ConfiguredInstance}s.
	 */
	protected InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

}
