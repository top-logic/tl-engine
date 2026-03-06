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
	 * Dispatches a command sent by the React client.
	 *
	 * @param commandName
	 *        The command identifier (e.g. "click", "sort").
	 * @param arguments
	 *        The command arguments from the client.
	 * @return The result of the command execution.
	 */
	HandlerResult executeCommand(String commandName, Map<String, Object> arguments);
}
