/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;

/**
 * Context passed to output handlers containing the {@link AgentResult} and
 * {@link BackgroundContext}.
 *
 * <p>
 * Instances are created by the {@link BackgroundAgentExecutor} and passed to external output handler
 * factories registered via
 * {@link BackgroundAgentExecutor#registerOutputHandlerFactory(Class, java.util.function.BiConsumer)}.
 * </p>
 *
 * @see BackgroundAgentExecutor
 */
public final class OutputHandlerContext {

	private final AgentResult _result;

	private final BackgroundContext _context;

	/**
	 * Creates a new {@link OutputHandlerContext}.
	 *
	 * @param result
	 *        The agent execution result.
	 * @param context
	 *        The background execution context.
	 */
	public OutputHandlerContext(AgentResult result, BackgroundContext context) {
		_result = result;
		_context = context;
	}

	/**
	 * The agent execution result.
	 */
	public AgentResult result() {
		return _result;
	}

	/**
	 * The background execution context.
	 */
	public BackgroundContext context() {
		return _context;
	}
}
