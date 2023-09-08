/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Map;

import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link CommandListener} has the possibility to execute
 * {@link ControlCommand}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommandListener extends NotifyListener {
	
	/**
	 * The identifier for this {@link CommandListener}.
	 * 
	 * <p>
	 * All {@link CommandListener}s that
	 * {@link CommandListenerRegistry#addCommandListener(CommandListener) register at} a
	 * {@link ControlScope} must have a unique identifier, which is used to find the listener that
	 * is the target for a command.
	 * </p>
	 */
	String getID();

	/**
	 * Executes a {@link ControlCommand} registered under the given command name
	 * <code>commandName</code>.
	 */
	HandlerResult executeCommand(DisplayContext context, String commandName, Map<String, Object> arguments);

}
