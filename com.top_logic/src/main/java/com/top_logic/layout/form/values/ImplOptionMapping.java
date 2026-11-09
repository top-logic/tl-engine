/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * {@link OptionMapping} for item properties that takes its options as implementation classes
 * (instead of implementation instances or {@link PolymorphicConfiguration} instances).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImplOptionMapping implements OptionMapping {

	/** Singleton {@link ImplOptionMapping} instance. */
	public static final ImplOptionMapping INSTANCE = new ImplOptionMapping();

	private ImplOptionMapping() {
		// singleton instance
	}

	@Override
	public Object toSelection(Object option) {
		if (option == null) {
			return null;
		}
		return newConfig((Class<?>) option, null);
	}

	/**
	 * Creates the configuration for the given implementation class.
	 *
	 * <p>
	 * No actual implementation instance is created, because the configuration must be customized
	 * before being used for the instantiation.
	 * </p>
	 *
	 * @param implType
	 *        The implementation class to configure.
	 * @param expectedConfigType
	 *        The configuration type of the property the configuration is created for, or
	 *        <code>null</code>, if unknown. If it is more specific than the configuration interface
	 *        of the implementation class, the configuration is created with it, as when reading
	 *        the implementation class from a configuration file.
	 * @return A new configuration with the given implementation class.
	 */
	public static PolymorphicConfiguration<Object> newConfig(Class<?> implType, Class<?> expectedConfigType) {
		Factory factory;
		try {
			factory = DefaultConfigConstructorScheme.getFactory(implType);
		} catch (ConfigurationException ex) {
			// Note cannot happen, because problematic classes are
			// filtered out above.
			throw new ConfigurationError("Cannot find factory for class: " + implType, ex);
		}
		Class<?> configType = factory.getConfigurationInterface();
		if (expectedConfigType != null && configType.isAssignableFrom(expectedConfigType)) {
			configType = expectedConfigType;
		}

		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<Object> defaultConfig =
			(PolymorphicConfiguration<Object>) TypedConfiguration
				.newConfigItem(configType.asSubclass(ConfigurationItem.class));
		defaultConfig.setImplementationClass(implType);
		return defaultConfig;
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		if (selection == null) {
			return null;
		}
		if (selection instanceof ConfiguredInstance<?>) {
			return ((ConfiguredInstance<?>) selection).getConfig().getImplementationClass();
		} else if (selection instanceof PolymorphicConfiguration<?>) {
			return ((PolymorphicConfiguration<?>) selection).getImplementationClass();
		} else {
			return selection.getClass();
		}
	}

}
