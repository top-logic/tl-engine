/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.text.ParseException;

import com.top_logic.basic.io.TLFormat;

/**
 * {@link AbstractConfigurationValueProvider} that implements {@link TLFormat} and dispatches
 * formatting and parsing to {@link TLFormat} method.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractFormattedConfigurationValueProvider<T> extends AbstractConfigurationValueProvider<T>
		implements TLFormat<T> {

	/**
	 * Creates a new {@link AbstractFormattedConfigurationValueProvider}.
	 */
	public AbstractFormattedConfigurationValueProvider(Class<?> type) {
		super(type);
	}

	@Override
	public T getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String formattedValue = propertyValue.toString();
		try {
			return parse(formattedValue);
		} catch (ParseException ex) {
			throw new ConfigurationException("Unable to parse '" + formattedValue + "'", ex);
		}
	}

	@Override
	public String getSpecificationNonNull(T configValue) {
		return format(configValue);
	}

}

