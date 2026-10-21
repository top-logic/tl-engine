/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
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
 * reuses the caller's interaction when one is present, without changing its identity (e.g. the chat
 * user's sub-session, so tools act in the user's context, with the user's rights, and on the user's
 * UI), and otherwise establishes a {@link ThreadContext#inSystemContext(Class, ComputationEx2) system
 * context} so a context-less caller (a background job) can still reach the {@code KnowledgeBase}.
 * {@link ThreadContext#inSystemContext(Class, ComputationEx2)} alone is not that: inside an existing
 * interaction it switches the caller's sub-session to the system identity, which would run the tool
 * without the user's account and without any access check.
 * </p>
 *
 * <p>
 * <b>The MCP server adapter deliberately does not use this.</b> Its requests arrive with an
 * authenticated account whose context {@code McpAuthenticationFilter} has already opened, and the
 * fallback below would be the wrong answer if that ever failed: a system context bypasses all access
 * checks, and issue #261 was exactly the case of an MCP request being served in one. It
 * calls the tool inside the caller's own context and refuses when there is none.
 * </p>
 *
 * <p>
 * It also {@link RunInfoBinding binds the run} the call belongs to for the duration of the call, on
 * the thread the tool runs on - which is a worker thread for the tools of a concurrent batch - so
 * that code <em>beneath</em> a tool, which gets no {@link ToolExecutionContext}, can still learn
 * which agent run it is serving. The binding is removed when the call returns. The run's
 * {@link ReadingListBinding reading list} is bound the same way, so that the code which renders what
 * a tool read - the read filter, the page renderer - can register it as a source.
 * </p>
 *
 * @see ThreadContext#inSystemContext(Class, ComputationEx2)
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
		try (RunInfoBinding.Binding bound = RunInfoBinding.bind(run);
				ReadingListBinding.Binding reading = ReadingListBinding.bind(readingListOf(context))) {
			return inCallerOrSystemContext(tool.getClass(), () -> tool.execute(args, context));
		}
	}

	/** The reading list the call registers what it read into, or <code>null</code> outside a run. */
	private static ReadingList readingListOf(ToolExecutionContext context) {
		return context instanceof ToolContext toolContext ? toolContext.readingList() : null;
	}

	/**
	 * Asks {@link Tool#refusal(Map, ToolExecutionContext)} in the interaction
	 * {@link #execute(Tool, Map, ToolExecutionContext)} would run the call in, so that a check reading
	 * the model or the knowledge base sees what the call itself would see.
	 *
	 * @param tool
	 *        The tool to ask.
	 * @param args
	 *        The deserialized tool arguments.
	 * @param context
	 *        The tool execution context, or {@code null}.
	 * @return The refusal, or {@code null} if the call may proceed.
	 */
	public static String refusal(Tool tool, Map<String, Object> args, ToolExecutionContext context) {
		RunInfo run = context != null ? context.runInfo() : RunInfo.NONE;
		try (RunInfoBinding.Binding bound = RunInfoBinding.bind(run)) {
			return inCallerOrSystemContext(tool.getClass(), () -> tool.refusal(args, context));
		}
	}

	/**
	 * Runs the given job in the interaction bound to the current thread, keeping its identity, or in
	 * a {@link ThreadContext#inSystemContext(Class, ComputationEx2) system context} when no
	 * interaction is bound.
	 *
	 * @param caller
	 *        The class naming the system context, if one is established.
	 * @param job
	 *        The computation to run.
	 * @return The result of the job.
	 */
	private static <T> T inCallerOrSystemContext(Class<?> caller,
			ComputationEx2<T, RuntimeException, RuntimeException> job) {
		if (ThreadContextManager.getInteraction() != null) {
			return job.run();
		}
		return ThreadContext.inSystemContext(caller, job);
	}
}
