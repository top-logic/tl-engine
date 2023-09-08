/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Locale;

import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link ConfigurationValueProvider} for {@link Locale}s.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LocaleValueProvider extends AbstractConfigurationValueProvider<Locale> {

	/** Singleton {@link LocaleValueProvider} instance. */
	public static final LocaleValueProvider INSTANCE = new LocaleValueProvider();

	private LocaleValueProvider() {
		super(Locale.class);
	}

	@Override
	protected Locale getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ResourcesModule.localeFromString(propertyValue.toString());
	}

	@Override
	protected String getSpecificationNonNull(Locale configValue) {
		return configValue.toString();
	}

}

