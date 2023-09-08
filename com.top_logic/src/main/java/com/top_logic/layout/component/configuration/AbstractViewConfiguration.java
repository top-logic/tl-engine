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

/**
 * The class {@link AbstractViewConfiguration} is the abstract superclass of
 * {@link ViewConfiguration} which handles the {@link ViewConfiguration#getName()} aspect.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractViewConfiguration<C extends AbstractViewConfiguration.Config<?>>
		implements ViewConfiguration, ConfiguredInstance<C> {

	private String _name;

	private C _config;

	/**
	 * Configuration interface for {@link AbstractViewConfiguration}s.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config<I extends AbstractViewConfiguration<?>>
			extends ViewConfiguration.Config<I> {
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

	@Override
	public final String getName() {
		return _name;
	}

	@Override
	public C getConfig() {
		return _config;
	}
}
