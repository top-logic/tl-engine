/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Abstract implementation of {@link ConfiguredMethodBuilder} that cares about configuration
 * handling.
 * 
 * @param <C>
 *        Concrete configuration type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractConfiguredMethodBuilder<C extends ConfiguredMethodBuilder.Config<?>>
		extends AbstractConfiguredInstance<C> implements ConfiguredMethodBuilder<C> {

	/**
	 * Create a {@link AbstractConfiguredMethodBuilder}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AbstractConfiguredMethodBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

}
