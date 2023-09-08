/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.StringValueProvider;

/**
 * {@link StringValueProvider} storing values in an obfuscated way.
 */
public class PasswordValueProvider extends AbstractConfigurationValueProvider<Password> {

	/**
	 * Singleton {@link PasswordValueProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final PasswordValueProvider INSTANCE = new PasswordValueProvider();

	private PasswordValueProvider() {
		super(Password.class);
	}

	@Override
	public String getSpecificationNonNull(Password configValue) {
		return configValue.getCryptedValue();
	}

	@Override
	public Password getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return new Password(propertyValue.toString());
	}

}
