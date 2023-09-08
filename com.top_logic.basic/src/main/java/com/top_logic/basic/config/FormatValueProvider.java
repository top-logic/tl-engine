/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.text.Format;
import java.text.ParseException;

/**
 * Adaptor from {@link Format} to {@link ConfigurationValueProvider}.
 * 
 * <p>
 * Note: Only thread-safe {@link Format} implementations may be wrapped into
 * {@link ConfigurationValueProvider}s. None of the default Java {@link Format}
 * implementations are thread-safe.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FormatValueProvider extends AbstractConfigurationValueProvider<Object> {

	/**
	 * Creates a {@link FormatValueProvider}.
	 *
	 * @param type
	 *        See {@link #getValueType()}.
	 */
	public FormatValueProvider(Class<?> type) {
		super(type);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String value = propertyValue.toString();
		try {
			return format().parseObject(value);
		} catch (ParseException ex) {
			throw new ConfigurationException(
				I18NConstants.ERROR_INVALID_FORMAT__VALUE_PROPERTY_DETAIL.fill(value, propertyName, ex.getMessage()),
				propertyName, propertyValue, ex);
		}
	}

	@Override
	public String getSpecificationNonNull(Object configValue) {
		return format().format(configValue);
	}

	/**
	 * The adapted {@link Format}.
	 */
	protected abstract Format format();
}
