/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Registry to register names of {@link CommandHandler} as known in the
 * {@link CommandHandlerFactory}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommandRegistry {

	/**
	 * Registers the given command either to be displayed as button or not.
	 * 
	 * @param command
	 *        The {@link CommandHandler#getID() command} to register.
	 * @param asButton
	 *        Whether the command is displayed as button or not.
	 */
	default void registerHandler(String command, boolean asButton) {
		if (asButton) {
			registerButton(command);
		} else {
			registerCommand(command);
		}
	}

	/**
	 * Registers the given command if it is available in the {@link CommandHandlerFactory}.
	 * 
	 * @see #registerHandler(String, boolean)
	 */
	default void registerIfExists(String command, boolean asButton) {
		CommandHandler resolvedCommand = CommandHandlerFactory.getInstance().getHandler(command);
		if (resolvedCommand != null) {
			registerHandler(command, asButton);
		}
	}

	/**
	 * Removes a formerly registered command. It doesn't matter whether the command was previously
	 * registered as command or as button.
	 * 
	 * <p>
	 * When the command was not registered before, nothing happens.
	 * </p>
	 * 
	 * @param command
	 *        The command to unregister.
	 */
	void unregisterHandler(String command);

	/**
	 * Registers the given command as known in the {@link CommandHandlerFactory}.
	 * 
	 * @param command
	 *        The command to register.
	 * 
	 * @see #registerButton(String)
	 */
	void registerCommand(String command);

	/**
	 * Registers the given command as known in the {@link CommandHandlerFactory}.
	 * 
	 * <p>
	 * The registered command is displayed as button.
	 * </p>
	 * 
	 * @param command
	 *        The command to register.
	 * 
	 * @see #registerCommand(String)
	 */
	void registerButton(String command);

}

