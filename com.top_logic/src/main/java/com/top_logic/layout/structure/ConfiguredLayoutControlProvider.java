/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Configured variant of {@link LayoutControlProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConfiguredLayoutControlProvider<C extends PolymorphicConfiguration<?>>
		extends AbstractConfiguredInstance<C> implements LayoutControlProvider {

	/**
	 * Creates a new {@link DecoratingLayoutControlProvider}.
	 * 
	 * @param context
	 *        Context to instantiate sub configurations.
	 * @param config
	 *        Configuration of the {@link DecoratingLayoutControlProvider}.
	 */
	public ConfiguredLayoutControlProvider(InstantiationContext context, C config) {
		super(context, config);
	}

}
