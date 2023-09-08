/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.filter.typed.AbstractTypedFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.tools.NameBuilder;

/**
 * A {@link ConfigurableFilter} combining a {@link List} of inner {@link ConfigurableFilter}s.
 * <p>
 * The inner filters are asked in order.
 * </p>
 * <p>
 * In contrast to the JavaDoc from {@link AbstractTypedFilter#matchesTypesafe(Object)} that method
 * is called for null, too, to avoid code duplication.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ConfigurableCompositeFilter<C extends ConfigurableCompositeFilter.Config<?>>
		extends AbstractConfigurableFilter<Object, C> {

	/**
	 * The {@link TypedConfiguration} of the {@link ConfigurableCompositeFilter}.
	 */
	public interface Config<S extends ConfigurableCompositeFilter<?>>
			extends AbstractConfigurableFilter.Config<S> {

		/** Property name of {@link #getFilters()}. */
		String FILTERS = "filters";

		/**
		 * The inner {@link TypedFilter}s.
		 */
		@Name(FILTERS)
		List<PolymorphicConfiguration<? extends TypedFilter>> getFilters();

	}

	private final List<TypedFilter> _filters;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ConfigurableCompositeFilter}.
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
	public ConfigurableCompositeFilter(InstantiationContext context, C config) {
		super(context, config);

		_filters = TypedConfiguration.getInstanceList(context, config.getFilters());

		check(context, _filters);
	}

	@Override
	public Class<?> getType() {
		return Object.class;
	}

	/**
	 * All composed filters.
	 */
	public final List<TypedFilter> getFilters() {
		return _filters;
	}

	/**
	 * Checks that none of the given filters is null.
	 * 
	 * @param protocol
	 *        Is not allowed to be null.
	 * @param actualFilters
	 *        Is not allowed to be null.
	 */
	private void check(Log protocol, Collection<? extends TypedFilter> actualFilters) {
		for (TypedFilter child : actualFilters) {
			if (child == null) {
				String message = "None of the filters is allowed to be null.";
				protocol.error(message, new NullPointerException(message));
			}
		}
	}

	@Override
	protected FilterResult matchesNull() {
		return matchesTypesafe(null);
	}

	@Override
	public String toString() {
		return new NameBuilder(this).add(Config.FILTERS, getConfig().getFilters()).build();
	}

}
