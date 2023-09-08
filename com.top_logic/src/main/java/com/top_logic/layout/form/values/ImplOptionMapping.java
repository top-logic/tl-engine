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
		Class<?> implType = (Class<?>) option;

		Factory factory;
		try {
			factory = DefaultConfigConstructorScheme.getFactory(implType);
		} catch (ConfigurationException ex) {
			// Note cannot happen, because problematic classes are
			// filtered out above.
			throw new ConfigurationError("Cannot find factory for class: " + implType, ex);
		}
		@SuppressWarnings("unchecked")
		Class<? extends ConfigurationItem> configType =
			(Class<? extends ConfigurationItem>) factory.getConfigurationInterface();

		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<Object> defaultConfig =
			(PolymorphicConfiguration<Object>) TypedConfiguration.newConfigItem(configType);
		defaultConfig.setImplementationClass(implType);

		// Note: No actual implementation instance must be created,
		// because the selection must be customized before being used as
		// configuration of the instantiation.
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
