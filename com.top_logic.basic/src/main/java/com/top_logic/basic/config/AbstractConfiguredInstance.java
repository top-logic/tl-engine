/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.annotation.Inspectable;

/**
 * Common base class for {@link ConfiguredInstance} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConfiguredInstance<C extends PolymorphicConfiguration<?>> implements
		ConfiguredInstance<C> {

	@Inspectable
	private final C _config;

	/**
	 * Creates a {@link AbstractConfiguredInstance} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractConfiguredInstance(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

}
