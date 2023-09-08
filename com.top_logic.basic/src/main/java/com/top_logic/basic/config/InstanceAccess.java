/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;

/**
 * {@link ConfigurationAccess} for properties that are instance-valued.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InstanceAccess implements ConfigurationAccess {

	/**
	 * Singleton {@link InstanceAccess} instance.
	 */
	public static final InstanceAccess INSTANCE = new InstanceAccess();

	private InstanceAccess() {
		// Singleton constructor.
	}

	@Override
	public ConfigurationItem getConfig(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof ConfiguredInstance<?>) {
			return ((ConfiguredInstance<?>) value).getConfig();
		} else {
			return configurationForImplementationClass(value.getClass());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private PolymorphicConfiguration<Object> configurationForImplementationClass(Class<?> implClass) {
		Class<? extends PolymorphicConfiguration<?>> configInterface;
		try {
			Factory factory = DefaultConfigConstructorScheme.getFactory(implClass);
			configInterface = (Class) factory.getConfigurationInterface();
		} catch (ConfigurationException ex) {
			configInterface = (Class) PolymorphicConfiguration.class;
		}
		return (PolymorphicConfiguration<Object>) DefaultConfigConstructorScheme.createDefaultPolymorphicConfiguration(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, configInterface, implClass);
	}

	@Override
	public Object createValue(InstantiationContext context, ConfigurationItem newConfig) {
		return context.getInstance((PolymorphicConfiguration<?>) newConfig);
	}

}
