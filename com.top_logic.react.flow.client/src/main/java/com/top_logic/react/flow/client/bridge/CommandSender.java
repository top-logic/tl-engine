/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.bridge;

import jsinterop.annotations.JsFunction;

/**
 * Function sending a command to the server-side control of the diagram.
 *
 * <p>
 * Provided by the React component mounting the diagram, which sends the command through the
 * command channel of the page: a command of the diagram and a command of another control the user
 * triggers afterwards reach the server in the order they were sent.
 * </p>
 */
@JsFunction
public interface CommandSender {

	/**
	 * Sends a command to the server-side control of the diagram.
	 *
	 * @param command
	 *        The name of the command.
	 * @param arguments
	 *        The JSON-serializable arguments of the command.
	 * @param background
	 *        Whether the command runs in the background, so that the user does not wait for its
	 *        answer and the page is not locked while it is on its way.
	 */
	void send(String command, Object arguments, boolean background);

}
