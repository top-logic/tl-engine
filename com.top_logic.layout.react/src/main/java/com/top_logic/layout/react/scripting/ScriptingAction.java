/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

/**
 * Description of one action a control offers to a headless consumer.
 *
 * <p>
 * An action maps directly onto a
 * {@link com.top_logic.layout.react.control.ReactCommand @ReactCommandHandler} of the underlying control:
 * {@link #command()} is the command ID passed to
 * {@link com.top_logic.layout.react.control.ReactControl#executeCommand(String, java.util.Map)}, and
 * {@link #argsSchema()} describes the {@code arguments} the command expects.
 * This is the same dispatch path the browser client uses, so an agent invoking an advertised action
 * exercises the real application behavior rather than a parallel mock.
 * </p>
 *
 * @param command
 *        The command ID to invoke (matches a {@code @ReactCommandHandler} value on the control).
 * @param label
 *        A human/agent readable label for the action.
 * @param argsSchema
 *        The JSON Schema of the command's arguments - of its typed argument interface or its
 *        hand-declared {@code @ReactParam} parameters -, or {@code null} if the command takes no
 *        arguments.
 */
public record ScriptingAction(String command, String label, ArgsSchema argsSchema) {

	/**
	 * Creates an argument-less action whose label equals its command ID.
	 *
	 * @param command
	 *        The command ID.
	 * @return An action with no parameters.
	 */
	public static ScriptingAction of(String command) {
		return new ScriptingAction(command, command, null);
	}
}
