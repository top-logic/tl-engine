/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.currency;

import java.util.Currency;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Configuration format for {@link Currency} values.
 */
public class CurrencyConfigFormat extends AbstractConfigurationValueProvider<Currency> {

	/**
	 * Creates a {@link CurrencyConfigFormat}.
	 */
	public CurrencyConfigFormat() {
		super(Currency.class);
	}

	@Override
	protected Currency getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String value = propertyValue.toString();

		try {
			return Currency.getInstance(value);
		} catch (IllegalArgumentException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_NO_SUCH_CURRENCY__VALUE.fill(value), propertyName,
				propertyValue);
		}
	}

	@Override
	protected String getSpecificationNonNull(Currency configValue) {
		return configValue.getCurrencyCode();
	}

}
