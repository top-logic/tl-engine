/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person.deletion;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Super class for configurable {@link PersonDeletionPolicy}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredDeletionPolicy extends PersonDeletionPolicy implements
		ConfiguredInstance<ConfiguredDeletionPolicy.Config> {

	/**
	 * Configuration of a {@link ConfiguredDeletionPolicy}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ConfiguredDeletionPolicy> {

		// No additional properties here

	}

	private final Config _config;

	/**
	 * Creates a new {@link ConfiguredDeletionPolicy} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ConfiguredDeletionPolicy}.
	 */
	public ConfiguredDeletionPolicy(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}
}
