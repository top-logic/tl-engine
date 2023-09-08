/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;

/**
 * The class {@link ManagedClass} that is constructed using the typed configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredManagedClass<C extends ConfiguredManagedClass.Config<?>> extends ManagedClass
		implements ConfiguredInstance<C> {

	/**
	 * Configuration of the {@link ConfiguredManagedClass}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends ConfiguredManagedClass<?>> extends ServiceConfiguration<I> {

		// Configuration of the ConfiguredManagedClass

	}

	/** @see #getConfig() */
	private final C _config;

	/**
	 * Creates a {@link ConfiguredManagedClass}.
	 * 
	 * @param context
	 *        the context which can be used to instantiate inner configurations.
	 * @param config
	 *        the configuration for the service.
	 */
	protected ConfiguredManagedClass(InstantiationContext context, C config) {
		super(context, config);
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

}

