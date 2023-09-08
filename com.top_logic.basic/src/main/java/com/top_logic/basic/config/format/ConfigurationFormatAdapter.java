/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.util.ResKey;

/**
 * Adapter for easy using of {@link Format}s as {@link ConfigurationValueProvider}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConfigurationFormatAdapter<T> extends AbstractConfigurationValueProvider<T> {

	/**
	 * Creates a {@link ConfigurationFormatAdapter}.
	 *
	 * @param type
	 *        See
	 *        {@link AbstractConfigurationValueProvider#AbstractConfigurationValueProvider(Class)}.
	 */
	public ConfigurationFormatAdapter(Class<?> type) {
		super(type);
	}

	@Override
	protected T getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		ParsePosition position = new ParsePosition(0);
		String rawValue = propertyValue.toString().trim();
		@SuppressWarnings("unchecked")
		T result = (T) getValueType().cast(format().parseObject(rawValue, position));
		int errorIndex = position.getErrorIndex();
		if (errorIndex < 0) {
			if (position.getIndex() < rawValue.length()) {
				// Trailing garbage at the end of the input.
				errorIndex = position.getIndex();
			}
		}
		if (errorIndex >= 0) {
			throw new ConfigurationException(errorMessage(errorIndex), propertyName, propertyValue);
		}
		return result;
	}

	/**
	 * An error message, if parsing fails at the given index.
	 */
	protected abstract ResKey errorMessage(int errorIndex);

	@Override
	protected String getSpecificationNonNull(T configValue) {
		return format().format(configValue);
	}

	/**
	 * The {@link Format} to use.
	 */
	protected abstract Format format();

}
