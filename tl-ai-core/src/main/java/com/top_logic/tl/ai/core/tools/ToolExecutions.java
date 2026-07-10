/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Runs {@link Tool}s with a guaranteed TopLogic interaction context.
 *
 * <p>
 * Tools access the {@code KnowledgeBase} and therefore need a thread-bound interaction. Rather than
 * having every tool establish one itself, the tool-call boundaries - the agent dispatcher
 * ({@link ToolCatalog}), the MCP server adapter, and the TL-Script bridge - route execution
 * through {@link #execute}. It reuses the caller's interaction when one is present (e.g. the chat
 * user's sub-session, so tools act in the user's context and on the user's UI), and otherwise
 * establishes a system interaction so context-less callers (the MCP server, background jobs) can
 * still reach the {@code KnowledgeBase}.
 * </p>
 *
 * @see ThreadContextManager#inSystemInteraction(Class, com.top_logic.basic.util.ComputationEx2)
 */
public final class ToolExecutions {

	private ToolExecutions() {
		// Static utility.
	}

	/**
	 * Executes {@link Tool#execute(Map, ToolExecutionContext)}, ensuring an interaction context is
	 * bound to the current thread for the duration of the call.
	 *
	 * @param tool
	 *        The tool to run.
	 * @param args
	 *        The deserialized tool arguments.
	 * @param context
	 *        The tool execution context, or {@code null}.
	 * @return The tool result.
	 */
	public static ToolResult execute(Tool tool, Map<String, Object> args, ToolExecutionContext context) {
		return ThreadContextManager.inSystemInteraction(tool.getClass(),
			() -> {
				return tool.execute(args, context);
			});
	}
}
