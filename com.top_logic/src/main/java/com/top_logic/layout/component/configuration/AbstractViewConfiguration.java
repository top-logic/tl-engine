/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * For legacy-compatibility only.
 * 
 * @deprecated Directly implement {@link ViewConfiguration}.
 */
@Deprecated
public abstract class AbstractViewConfiguration<C extends AbstractViewConfiguration.Config<?>>
		implements ViewConfiguration, ConfiguredInstance<C> {

	private String _name;

	private C _config;

	/**
	 * Configuration interface for {@link AbstractViewConfiguration}s.
	 */
	public interface Config<I extends AbstractViewConfiguration<?>>
			extends PolymorphicConfiguration<I>, NamedConfiguration {
		// Pure sum interface
	}

	/**
	 * Creates a {@link AbstractViewConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 * @throws ConfigurationException
	 *         If configuration is invalid.
	 */
	@CalledByReflection
	public AbstractViewConfiguration(InstantiationContext context, C config) throws ConfigurationException {
		_name = config.getName();
		_config = config;
	}

	/**
	 * Legacy configuration name, no longer in use.
	 */
	@Deprecated
	public final String getName() {
		return _name;
	}

	@Override
	public C getConfig() {
		return _config;
	}
}
