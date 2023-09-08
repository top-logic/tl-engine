/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * {@link DecoratingLayoutControlProvider} for {@link LayoutControlProvider} which actually have no
 * configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SimpleDecoratingLayoutControlProvider<I extends SimpleDecoratingLayoutControlProvider<?>>
		extends DecoratingLayoutControlProvider<PolymorphicConfiguration<I>> {

	/**
	 * Create a {@link SimpleDecoratingLayoutControlProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SimpleDecoratingLayoutControlProvider(InstantiationContext context, PolymorphicConfiguration<I> config) {
		super(context, config);
	}

}

