/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.StringServices;

/**
 * {@link ConfigurationValueProvider} which serializes {@link String}.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringValueProvider extends AbstractConfigurationValueProvider<String> {
	
	/** Sole {@link StringValueProvider} instance */
	public static final StringValueProvider INSTANCE = new StringValueProvider();

	/**
	 * Creates a {@link StringValueProvider}.
	 */
	protected StringValueProvider() {
		super(String.class);
	}
	
	@Override
	protected String getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	public String getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return propertyValue.toString();
	}

	@Override
	public String getSpecificationNonNull(String configValue) {
		return configValue;
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value != null;
	}

	@Override
	public String defaultValue() {
		return StringServices.EMPTY_STRING;
	}
	
	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return StringServices.EMPTY_STRING;
		}
		return super.normalize(value);
	}
}
