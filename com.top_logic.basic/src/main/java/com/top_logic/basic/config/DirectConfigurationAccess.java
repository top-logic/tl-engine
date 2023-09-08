/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * {@link ConfigurationAccess} for configuration-valued properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectConfigurationAccess implements ConfigurationAccess {

	/**
	 * Singleton {@link DirectConfigurationAccess} instance.
	 */
	public static final DirectConfigurationAccess INSTANCE = new DirectConfigurationAccess();

	private DirectConfigurationAccess() {
		// Singleton constructor.
	}

	@Override
	public ConfigurationItem getConfig(Object value) {
		return (ConfigurationItem) value;
	}

	@Override
	public Object createValue(InstantiationContext context, ConfigurationItem newConfig) {
		return newConfig;
	}

}
