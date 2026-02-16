/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.help;

import com.top_logic.ai.service.agents.AgentInvoker;
import com.top_logic.ai.service.agents.handoff.AgentWorkOrder;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for requesting model exploration via a structured work order.
 * <p>
 * Used by {@code TLScriptAgent} (and similar agents) when they need model context. Accepts
 * <b>goal</b>, <b>task</b>, and <b>context</b> so the LLM fills structured fields
 * instead of pasting the full user prompt. Delegates to {@link AgentInvoker#invokeExplorer}
 * and records an agent-call trace event ({@code TLScriptAgent} → {@code ModelExplorer}).
 * </p>
 */
public class RequestModelExplorationTool {

	private static final String FROM_AGENT = "TLScriptAgent";

	/**
	 * Requests model exploration with a distilled work order.
	 * <p>
	 * Fill <b>goal</b> (short intent), <b>task</b> (concrete question), and <b>context</b>
	 * (e.g. type/part names, module hints). Do not paste the entire user message.
	 * </p>
	 *
	 * @param goal
	 *        Short intent (e.g. "Find TLScript annotations for type X").
	 * @param task
	 *        Concrete question or instruction (e.g. "List annotations on sales:Order#total").
	 * @param context
	 *        Optional hints: type/part names, module, entity ids.
	 * @return The {@code ModelExplorer}'s answer.
	 */
	@Tool("Request model exploration with a structured work order. Provide goal (short intent), task (concrete question or instruction), and context (type/part names, module hints). Do NOT paste the full user message; distill a clear, specific exploration request.")
	public String requestModelExploration(
			@P("Short intent, e.g. 'Find TLScript annotations for type X'") String goal,
			@P("Concrete question or instruction, e.g. 'List annotations on sales:Order#total'") String task,
			@P("Optional context: type/part names, module, entity hints") String context) {
		AgentWorkOrder workOrder = AgentWorkOrder.of(goal, task, context != null ? context : "", "");
		return AgentInvoker.invokeExplorer(FROM_AGENT, workOrder);
	}
}
