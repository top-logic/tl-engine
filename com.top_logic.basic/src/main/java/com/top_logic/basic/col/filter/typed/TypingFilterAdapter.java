/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.typed;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.tools.NameBuilder;

/**
 * An adapter for using a {@link Filter} where a {@link TypedFilter} is required.
 * 
 * @see TypedFilterAdapter
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TypingFilterAdapter<T> extends AbstractTypedFilter<T>
		implements ConfiguredInstance<TypingFilterAdapter.Config<T>> {

	/**
	 * The {@link TypedConfiguration} of the {@link TypingFilterAdapter}.
	 */
	public interface Config<T> extends PolymorphicConfiguration<TypingFilterAdapter<T>> {

		/** Property name of {@link #getFilter()}. */
		String FILTER = "filter";

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/** Property name of {@link #isNullApplicable()}. */
		String NULL_APPLICABLE = "null-applicable";

		/** The {@link Filter} whose result should adapted. */
		@NonNullable
		@Mandatory
		@InstanceFormat
		@Name(FILTER)
		Filter<? super T> getFilter();

		/** The type supported by {@link #getFilter()}. */
		@Mandatory
		@Name(TYPE)
		Class<T> getType();

		/**
		 * Whether null is applicable and should be decided by {@link #getFilter()}.
		 * <p>
		 * If null is applicable, the inner {@link #getFilter()} is called with null.
		 * </p>
		 * <p>
		 * If null is not applicable, {@link FilterResult#INAPPLICABLE} is returned.
		 * </p>
		 */
		@BooleanDefault(false)
		@Name(NULL_APPLICABLE)
		boolean isNullApplicable();

	}

	private final Config<T> _config;

	private final Class<T> _type;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TypingFilterAdapter}.
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
	public TypingFilterAdapter(InstantiationContext context, Config<T> config) {
		_config = config;
		_type = config.getType();
	}

	@Override
	public Class<?> getType() {
		return _type;
	}

	@Override
	public Config<T> getConfig() {
		return _config;
	}

	@Override
	protected FilterResult matchesTypesafe(T value) {
		return FilterResult.valueOf(getFilter().accept(value));
	}

	@Override
	protected FilterResult matchesNull() {
		if (getConfig().isNullApplicable()) {
			return matchesTypesafe(null);
		}
		return FilterResult.INAPPLICABLE;
	}

	@Override
	public String toString() {
		return new NameBuilder(this).addUnnamed(getFilter()).build();
	}

	/**
	 * The inner {@link Filter}.
	 */
	public Filter<? super T> getFilter() {
		return getConfig().getFilter();
	}

}
