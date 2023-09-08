/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Base class for {@link Handler} implementations that can be customized by configuration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConfiguredImportHandler<C extends ConfiguredImportHandler.Config<?>>
		extends AbstractConfiguredInstance<C> implements Handler, ConfiguredImportPart<C> {

	/**
	 * Creates a {@link ConfiguredImportHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredImportHandler(InstantiationContext context, C config) {
		super(context, config);
	}

}
