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
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.col.filter.typed.TypingFilterAdapter;
import com.top_logic.basic.col.filter.typed.TypingFilterAdapter.Config;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * {@link TestCase} for the {@link TypingFilterAdapter}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTypingFilterAdapter extends AbstractTestConfigurableFilter {

	public static class MistypedDummyNumberFilter implements Filter<Object> {

		@Override
		public boolean accept(Object value) {
			if (value instanceof Number) {
				return ((Number) value).doubleValue() >= 0;
			}
			return false;
		}

	}

	public static class DummyNumberFilter implements Filter<Number> {

		@Override
		public boolean accept(Number value) {
			if (value != null) {
				return value.doubleValue() >= 0;
			}
			return false;
		}

	}

	private PolymorphicConfiguration<? extends TypedFilter> _filter;

	private PolymorphicConfiguration<? extends TypedFilter> _mistypedFilter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_filter = newAdapter(new DummyNumberFilter(), Number.class, false);
		_mistypedFilter = newAdapter(new MistypedDummyNumberFilter(), Number.class, false);
	}

	@Override
	protected void tearDown() throws Exception {
		_filter = null;
		_mistypedFilter = null;
		super.tearDown();
	}

	public void testFilter() {
		assertRejects(_filter, -1);
		assertAccepts(_filter, 1);
		assertIsInapplicable(_filter, "Hello World!");
		assertIsInapplicable(_filter, null);
	}

	public void testMistypedFilter() {
		assertRejects(_mistypedFilter, -1);
		assertAccepts(_mistypedFilter, 1);
		assertIsInapplicable(_mistypedFilter, "Hello World!");
		assertIsInapplicable(_mistypedFilter, null);
	}

	public void testFilterWithApplicableNull() {
		PolymorphicConfiguration<? extends TypedFilter> filter =
			newAdapter(new DummyNumberFilter(), Number.class, true);
		assertRejects(filter, -1);
		assertAccepts(filter, 1);
		assertIsInapplicable(filter, "Hello World!");
		assertRejects(filter, null);
	}

	public void testMistypedFilterWithApplicableNull() {
		PolymorphicConfiguration<? extends TypedFilter> filter =
			newAdapter(new MistypedDummyNumberFilter(), Number.class, true);
		assertRejects(filter, -1);
		assertAccepts(filter, 1);
		assertIsInapplicable(filter, "Hello World!");
		assertRejects(filter, null);
	}

	public void testTypeForFilter() {
		assertEquals(Number.class, createInstance(_filter).getType());
	}

	public void testTypeForMistypedFilter() {
		assertEquals(Number.class, createInstance(_mistypedFilter).getType());
	}

	private <T> PolymorphicConfiguration<? extends TypedFilter> newAdapter(Filter<? super T> filter, Class<T> type,
			boolean isNullApplicable) {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(TypingFilterAdapter.Config.class);
		set(builder, TypingFilterAdapter.Config.FILTER, filter);
		set(builder, TypingFilterAdapter.Config.TYPE, type);
		set(builder, TypingFilterAdapter.Config.NULL_APPLICABLE, isNullApplicable);
		/* The cast is okay, as the Config is of Type T as the "type" property contains Class<T>. */
		@SuppressWarnings("unchecked")
		TypingFilterAdapter.Config<T> config = (Config<T>) builder.createConfig(getInstantiationContext());
		return config;
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestTypingFilterAdapter.class, TypeIndex.Module.INSTANCE);
	}

}
