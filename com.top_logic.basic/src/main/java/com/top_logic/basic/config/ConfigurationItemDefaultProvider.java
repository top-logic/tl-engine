/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.DefaultValueProvider;

/**
 * {@link DefaultValueProvider} which creates a mutable configuration of the given type, fills some
 * values, and returns it.
 * 
 * @param <T>
 *        The type of the property, for which a default value is created.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfigurationItemDefaultProvider<T extends ConfigurationItem> extends DefaultValueProvider {

	protected final Class<? extends T> _configType;

	/**
	 * Creates a {@link ConfigurationItemDefaultProvider}.
	 *
	 * @param configType The dynamic type of the property, for which a default is created.
	 */
	public ConfigurationItemDefaultProvider(Class<? extends T> configType) {
		this._configType = configType;
	}

	@Override
	public final Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		T item = TypedConfiguration.newConfigItem(_configType);
		intitializeConfiguration(item);
		return item;
	}

	protected abstract void intitializeConfiguration(T config);

}
