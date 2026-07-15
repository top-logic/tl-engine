/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.List;

/**
 * Description of one action a control offers to a headless consumer.
 *
 * <p>
 * An action maps directly onto a
 * {@link com.top_logic.layout.react.control.ReactCommand @ReactCommandHandler} of the underlying control:
 * {@link #command()} is the command ID passed to
 * {@link com.top_logic.layout.react.control.ReactControl#executeCommand(String, java.util.Map)}, and
 * {@link #params()} or {@link #argsSchema()} describes the {@code arguments} the command expects.
 * This is the same dispatch path the browser client uses, so an agent invoking an advertised action
 * exercises the real application behavior rather than a parallel mock.
 * </p>
 *
 * @param command
 *        The command ID to invoke (matches a {@code @ReactCommandHandler} value on the control).
 * @param label
 *        A human/agent readable label for the action.
 * @param params
 *        The hand-declared argument list (from {@code @ReactParam}); an empty list for
 *        argument-less actions or for commands that advertise a typed {@link #argsSchema()} instead.
 * @param argsSchema
 *        The JSON Schema of the command's typed argument interface, as a parsed JSON value
 *        ({@code Map}/{@code List}/scalar), or {@code null} if the command has no typed argument.
 *        When present, this is the self-describing replacement for {@link #params()}.
 */
public record AgentAction(String command, String label, List<AgentParam> params, Object argsSchema) {

	/**
	 * Creates an argument-less action whose label equals its command ID.
	 *
	 * @param command
	 *        The command ID.
	 * @return An action with no parameters.
	 */
	public static AgentAction of(String command) {
		return new AgentAction(command, command, List.of(), null);
	}
}
