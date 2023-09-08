/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Format that searches for a configured filter with the given name. If there is no filter with the
 * configuration value, the value is expected to be the class of a filter. An instance of that class
 * is returned.
 * 
 * @see FilterRegistry
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilterFormat extends AbstractConfigurationValueProvider<Filter<?>> {

	private static final String CLASS_PREFIX = "class:";

	/** Singleton {@link FilterFormat} instance. */
	public static final FilterFormat INSTANCE = new FilterFormat();

	private FilterFormat() {
		super(Filter.class);
	}

	@Override
	protected Filter<?> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String formattedValue = propertyValue.toString();
		if (formattedValue.startsWith(CLASS_PREFIX)) {
			CharSequence formattedClass = propertyValue.subSequence(CLASS_PREFIX.length(), propertyValue.length());
			return ConfigUtil.getInstanceMandatory(Filter.class, propertyName, formattedClass);
		}
		Filter<Object> configuredFilter = FilterRegistry.getFilter(formattedValue);
		if (configuredFilter != null) {
			return configuredFilter;
		}
		throw new ConfigurationException(I18NConstants.ILLEGAL_FILTER_FORMAT__CLASS_PREFIX.fill(CLASS_PREFIX),
			propertyName, propertyValue);
	}

	@Override
	protected String getSpecificationNonNull(Filter<?> configValue) {
		String filterName = FilterRegistry.getFilterName(configValue);
		if (filterName != null) {
			return filterName;
		}
		return CLASS_PREFIX + configValue.getClass().getName();
	}

}

