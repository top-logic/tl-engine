/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Collection;

/**
 * {@link CommandListener}s can register at a {@link CommandListenerRegistry} if
 * they are dedicated to be executed by the user on the client.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommandListenerRegistry {

	/**
	 * Registers the given {@link CommandListener} for executing its commands.
	 */
	public void addCommandListener(CommandListener listener);

	/**
	 * Unregisters the given {@link CommandListener}, which was registered for
	 * executing its command.
	 * 
	 * @return <code>true</code>, if the given listener was formerly registered,
	 *         <code>false</code> otherwise.
	 */
	public boolean removeCommandListener(CommandListener listener);

	/**
	 * Returns the {@link CommandListener} formerly registered under the given
	 * id or <code>null</code> if there is none.
	 * 
	 * @param id
	 *        the id of the required {@link CommandListener}. must not be
	 *        <code>null</code>
	 */
	public CommandListener getCommandListener(String id);

	/**
	 * Returns a collection of all registered {@link CommandListener}. Changes to that collection
	 * don't change the internal collection of the registry.
	 */
	public Collection<CommandListener> getCommandListener();

	/**
	 * Removes all formerly registered {@link CommandListener} from this registry.
	 */
	public void clear();

}
