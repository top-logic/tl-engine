/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.Map;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Lean interface for controls that can receive commands from the React client.
 *
 * <p>
 * Replaces {@link com.top_logic.layout.CommandListener} for React controls. Unlike
 * {@code CommandListener}, this interface does not require a
 * {@link com.top_logic.layout.DisplayContext} for command dispatch.
 * </p>
 */
public interface ReactCommandTarget {

	/**
	 * The unique control ID used for command routing.
	 */
	String getID();

	/**
	 * Dispatches a command programmatically (headless agent, script replay, server-side code).
	 *
	 * <p>
	 * State changes the command makes are pushed to the client like any other server-side change —
	 * no browser has anticipated them.
	 * </p>
	 *
	 * @param commandName
	 *        The command identifier (e.g. "click", "sort").
	 * @param arguments
	 *        The command arguments.
	 * @return The result of the command execution.
	 */
	HandlerResult executeCommand(String commandName, Map<String, Object> arguments);

	/**
	 * Dispatches a command sent by the browser client itself.
	 *
	 * <p>
	 * The difference to {@link #executeCommand}: the browser that sent the command has typically
	 * already applied the change optimistically (a typed character, a dragged window edge), so
	 * state changes reflecting exactly what the client did are not echoed back — for an input
	 * being typed into, a late echo would overwrite newer keystrokes. Only on this dispatch path
	 * may such echoes be omitted; {@link #executeCommand} compensates omissions by resending
	 * state.
	 * </p>
	 *
	 * @param commandName
	 *        The command identifier (e.g. "click", "sort").
	 * @param arguments
	 *        The command arguments from the client.
	 * @return The result of the command execution.
	 */
	default HandlerResult executeClientCommand(String commandName, Map<String, Object> arguments) {
		return executeCommand(commandName, arguments);
	}
}
