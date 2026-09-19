/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.tl.ai.core.execution.RunInfoBinding;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Runs {@link Tool}s with a guaranteed TopLogic interaction context.
 *
 * <p>
 * Tools access the {@code KnowledgeBase} and therefore need a thread-bound interaction. Rather than
 * having every tool establish one itself, the tool-call boundaries - the agent dispatcher
 * ({@link ToolCatalog}) and the TL-Script bridge - route execution through {@link #execute}. It
 * reuses the caller's interaction when one is present (e.g. the chat user's sub-session, so tools
 * act in the user's context and on the user's UI), and otherwise establishes a system interaction so
 * a context-less caller (a background job) can still reach the {@code KnowledgeBase}.
 * </p>
 *
 * <p>
 * <b>The MCP server adapter deliberately does not use this.</b> Its requests arrive with an
 * authenticated account whose context {@code McpAuthenticationFilter} has already opened, and the
 * fallback below would be the wrong answer if that ever failed: a system interaction is the engine's
 * super-user bypass, and issue #261 was exactly the case of an MCP request being served in one. It
 * calls the tool inside the caller's own context and refuses when there is none.
 * </p>
 *
 * <p>
 * It also {@link RunInfoBinding binds the run} the call belongs to for the duration of the call, on
 * the thread the tool runs on - which is a worker thread for the tools of a concurrent batch - so
 * that code <em>beneath</em> a tool, which gets no {@link ToolExecutionContext}, can still learn
 * which agent run it is serving. The binding is removed when the call returns.
 * </p>
 *
 * @see ThreadContextManager#inSystemInteraction(Class, com.top_logic.basic.util.ComputationEx2)
 * @see ToolExecutionContext#runInfo()
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
		RunInfo run = context != null ? context.runInfo() : RunInfo.NONE;
		try (RunInfoBinding.Binding bound = RunInfoBinding.bind(run)) {
			return ThreadContextManager.inSystemInteraction(tool.getClass(),
				() -> {
					return tool.execute(args, context);
				});
		}
	}
}
