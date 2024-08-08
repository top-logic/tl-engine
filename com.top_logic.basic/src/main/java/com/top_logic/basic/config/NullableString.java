/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Nullable;

/**
 * {@link ConfigurationValueProvider} for {@link String} properties that are <code>null</code> (not
 * empty).
 * 
 * <p>
 * Note: Do not use this format directly, apply the {@link Nullable} annotation to the string
 * property that you want to have <code>null</code> instead of empty values.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NullableString extends AbstractConfigurationValueProvider<String> {

	/**
	 * Singleton {@link NullableString} instance.
	 */
	public static final NullableString INSTANCE = new NullableString();

	private NullableString() {
		super(String.class);
	}

	@Override
	public Object normalize(Object value) {
		if (value == null || ((String) value).length() == 0) {
			return null;
		}
		return super.normalize(value);
	}

	@Override
	protected String getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return propertyValue.toString();
	}

	@Override
	protected String getSpecificationNonNull(String configValue) {
		return configValue;
	}

}
