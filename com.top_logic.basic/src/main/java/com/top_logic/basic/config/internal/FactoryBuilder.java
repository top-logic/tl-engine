/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import com.top_logic.basic.config.ConfigurationDescriptor;

/**
 * Factory for {@link ItemFactory} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FactoryBuilder {

	/**
	 * Creates an {@link ItemFactory} for the given {@link ConfigurationDescriptor}.
	 */
	public abstract ItemFactory createFactory(ConfigurationDescriptor descriptor);

}
