/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.tools.NameBuilder;

/**
 * A {@link ConfigurableFilter} that negates its inner {@link ConfigurableFilter}.
 * <p>
 * {@link FilterResult#INAPPLICABLE} stays {@link FilterResult#INAPPLICABLE INAPPLICABLE}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ConfigurableNotFilter<T> extends AbstractConfigurableFilter<T, ConfigurableNotFilter.Config<T>> {

	/**
	 * The {@link TypedConfiguration} of the {@link ConfigurableNotFilter}.
	 */
	@TagName("not")
	public interface Config<T> extends AbstractConfigurableFilter.Config<ConfigurableNotFilter<T>> {

		/** Property name of {@link #getFilter()}. */
		String FILTER = "filter";

		/**
		 * The inner {@link TypedFilter} whose result should be negated.
		 */
		@Name(FILTER)
		PolymorphicConfiguration<? extends TypedFilter> getFilter();

	}

	private TypedFilter _filter;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ConfigurableNotFilter}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ConfigurableNotFilter(InstantiationContext context, Config<T> config) {
		super(context, config);
		
		_filter = context.getInstance(config.getFilter());
	}

	@Override
	public FilterResult matchesTypesafe(T object) {
		FilterResult innerResult = getInnerFilter().matches(object);
		switch (innerResult) {
			case TRUE: {
				return FilterResult.FALSE;
			}
			case FALSE: {
				return FilterResult.TRUE;
			}
			case INAPPLICABLE: {
				return FilterResult.INAPPLICABLE;
			}
			default: {
				throw new UnreachableAssertion("Unexpected value: " + innerResult);
			}
		}
	}

	@Override
	protected FilterResult matchesNull() {
		return matchesTypesafe(null);
	}

	@Override
	public Class<?> getType() {
		return getInnerFilter().getType();
	}

	/**
	 * The inverted filter.
	 */
	public final TypedFilter getInnerFilter() {
		return _filter;
	}

	@Override
	public String toString() {
		return new NameBuilder(this).addUnnamed(getInnerFilter()).build();
	}

}
