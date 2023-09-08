/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;


/**
 * Algorithm for managing listeners for notifying whenever a dependency of a derived property
 * changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Updater {

	/**
	 * Installs listeners on the given {@link ConfigurationItem}.
	 * 
	 * @param item
	 *        The item to install listeners on.
	 * @param updateListener
	 *        The listener that must be informed, whenever the derived property potentially changes
	 *        due to an update of a dependency.
	 */
	public abstract void install(ConfigurationItem item, ConfigurationListener updateListener);

	/**
	 * Removes all listeners installed in {@link #install(ConfigurationItem, ConfigurationListener)}
	 * .
	 */
	public abstract void uninstall(ConfigurationItem item, ConfigurationListener updateListener);

}
