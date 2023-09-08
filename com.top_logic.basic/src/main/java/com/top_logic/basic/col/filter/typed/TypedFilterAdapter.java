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
import com.top_logic.basic.tools.NameBuilder;

/**
 * An adapter for using a {@link TypedFilter} where a {@link Filter} is required.
 * 
 * @see TypingFilterAdapter
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TypedFilterAdapter implements Filter<Object>, ConfiguredInstance<TypedFilterAdapter.Config> {

	/**
	 * The {@link TypedConfiguration} of the {@link TypedFilterAdapter}.
	 */
	public interface Config extends PolymorphicConfiguration<TypedFilterAdapter> {

		/** Property name of {@link #getFilter()}. */
		String FILTER = "filter";

		/** Property name of {@link #getDefault()}. */
		String DEFAULT = "default";

		/** The {@link TypedFilter} whose result should adapted. */
		@NonNullable
		@Mandatory
		@InstanceFormat
		@Name(FILTER)
		TypedFilter getFilter();

		/**
		 * This value is returned if the inner filter returns {@link FilterResult#INAPPLICABLE}.
		 */
		@Mandatory
		@Name(DEFAULT)
		boolean getDefault();

	}

	private final Config _config;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TypedFilterAdapter}.
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
	public TypedFilterAdapter(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public boolean accept(Object value) {
		FilterResult filterResult = getFilter().matches(value);
		if (filterResult == FilterResult.INAPPLICABLE) {
			return getConfig().getDefault();
		}
		return filterResult.toBoolean();
	}

	@Override
	public String toString() {
		return new NameBuilder(this).addUnnamed(getConfig().getFilter()).build();
	}

	/**
	 * The inner {@link TypedFilter}.
	 */
	public TypedFilter getFilter() {
		return getConfig().getFilter();
	}

}
