/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.tools.NameBuilder;

/**
 * A {@link ConfigurableFilter} for accepting only double values within the given range.
 * <p>
 * Accepts {@link Number}s for convenience, not just {@link Double}s.
 * </p>
 * 
 * @see ConfigurableAndFilter
 * @see ConfigurableNotFilter
 * @see ConfigurableOrFilter
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DoubleRangeFilter extends AbstractConfigurableFilter<Number, DoubleRangeFilter.Config> {

	/**
	 * The {@link TypedConfiguration} of the {@link DoubleRangeFilter}.
	 */
	public interface Config extends AbstractConfigurableFilter.Config<DoubleRangeFilter> {

		/** Property name of {@link #getMin()}. */
		String MIN = "min";

		/** Property name of {@link #getMax()}. */
		String MAX = "max";

		/**
		 * The minimum accepted value.
		 */
		@Name(MIN)
		double getMin();

		/**
		 * The maximum accepted value.
		 */
		@Name(MAX)
		double getMax();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link DoubleRangeFilter}.
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
	public DoubleRangeFilter(InstantiationContext context, Config config) {
		super(context, config);
		check(context, config);
	}

	@Override
	public Class<?> getType() {
		return Number.class;
	}

	private void check(InstantiationContext context, Config config) {
		if (Double.isNaN(config.getMin()) || Double.isNaN(config.getMax())) {
			String message = "Neither the minimum nor the maximum value are allowed to be NaN.";
			error(context, config, message);
		}
		if (config.getMin() > config.getMax()) {
			String message = "The minimum value is not allowed to be greater than the maximum value.";
			error(context, config, message);
		}
	}

	private void error(InstantiationContext context, Config config, String message) {
		String fullMessage = message + " Min: " + config.getMin() + "; Max: " + config.getMax();
		context.error(fullMessage, new ConfigurationException(fullMessage));
	}

	@Override
	public FilterResult matchesTypesafe(Number number) {
		double doubleValue = number.doubleValue();
		double min = getConfig().getMin();
		double max = getConfig().getMax();
		boolean result = doubleValue >= min && doubleValue <= max;
		return FilterResult.valueOf(result);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add(Config.MIN, getConfig().getMin())
			.add(Config.MAX, getConfig().getMax())
			.build();
	}

}
