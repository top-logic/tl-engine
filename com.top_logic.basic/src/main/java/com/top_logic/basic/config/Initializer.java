/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;


/**
 * Initializer (like a constructor) of a {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class Initializer {

	/**
	 * Performs an initialization operation directly after constructing a new
	 * {@link ConfigurationItem}.
	 * 
	 * @param item
	 *        The newly constructed item. Is not allowed to be <code>null</code>.
	 */
	public abstract void init(AbstractConfigItem item);

}
