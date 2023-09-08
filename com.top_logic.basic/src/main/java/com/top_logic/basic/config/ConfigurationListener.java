/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Listener that is notifies when {@link ConfigurationItem}s change.
 * 
 * @see ConfigurationItem#addConfigurationListener(PropertyDescriptor, ConfigurationListener)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationListener {

	/**
	 * Called whenever the value of the property identified by the given {@link PropertyDescriptor}
	 * in the given {@link ConfigurationItem} changes.
	 * 
	 * @param change
	 *        Description of the change.
	 */
	void onChange(ConfigurationChange change);

}
