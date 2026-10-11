/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

/**
 * Application hook: handles a structured data part of an incoming message before the agent reads it,
 * and tells the agent what became of it instead of handing it the raw JSON.
 *
 * <p>
 * Asked for every data part of a message to an exposed agent, in the {@code TLContext} of the account
 * the run acts as, before the run starts. Handlers are asked in order ({@code <data-handlers>} on
 * {@link A2AService}); the first non-<code>null</code> answer replaces the part in what the agent
 * reads. Without an answer the part reaches the agent as JSON. A handler that throws is logged and
 * skipped.
 * </p>
 */
public interface A2ADataHandler {

	/**
	 * Handles one data part.
	 *
	 * @return The text the agent reads in place of the part, or <code>null</code> to leave it.
	 */
	String handle(Request request);

	/**
	 * One data part of an incoming message.
	 *
	 * @param agent
	 *        The exposed agent's path.
	 * @param peer
	 *        Who sent it: the calling application as it named itself, else the account.
	 * @param contextId
	 *        The A2A context id.
	 * @param taskId
	 *        The A2A task id.
	 * @param data
	 *        The part's JSON value.
	 */
	record Request(String agent, String peer, String contextId, String taskId, Object data) {
		// Pure value.
	}
}
