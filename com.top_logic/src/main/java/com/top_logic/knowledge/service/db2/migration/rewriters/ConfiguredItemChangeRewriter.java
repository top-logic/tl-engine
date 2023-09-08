/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Abstract superclass for {@link ItemChangeRewriter} with configuration.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredItemChangeRewriter<C extends PolymorphicConfiguration<?>>
		extends AbstractConfiguredInstance<C>
		implements ItemChangeRewriter {

	/**
	 * Creates a {@link ConfiguredItemChangeRewriter}.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ConfiguredItemChangeRewriter(InstantiationContext context, C config) {
		super(context, config);
	}

}

