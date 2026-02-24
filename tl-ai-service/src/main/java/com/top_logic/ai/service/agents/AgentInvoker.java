/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.Map;

import com.top_logic.ai.service.agents.handoff.AgentWorkOrder;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agentic.UntypedAgent;

/**
 * Internal component for agent-to-agent calls.
 * <p>
 * Used by Orchestrator (and by TLScript's "request exploration" path) to invoke
 * other configured agents. Records {@link com.top_logic.ai.service.tools.TraceEntry.AgentCallTraceEntry}
 * events via the current run's trace (see {@link com.top_logic.ai.service.tools.ToolCallTracking}).
 * No agent is exposed as a @Tool to another agent.
 * </p>
 */
public final class AgentInvoker {

	/** Max length for result summary in trace; use large value so UI can show full text. */
	private static final int RESULT_SUMMARY_MAX_LEN = 100_000;

	/**
	 * Invokes the {@code ModelExplorer} with a work-order-derived prompt.
	 *
	 * @param fromAgent
	 *        Name of the calling agent (e.g. "Orchestrator").
	 * @param workOrder
	 *        Distilled goal, task, context, expected output.
	 * @return The explorer's text answer.
	 */
	public static String invokeExplorer(String fromAgent, AgentWorkOrder workOrder) {
		String prompt = buildExplorerPrompt(workOrder);
		ToolCallTracking.recordAgentCallStartIfTracking(fromAgent, "ModelExplorer", prompt);
		String result;
		try {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				throw new RuntimeException("AgentRegistry is not available.");
			}
			UntypedAgent explorer = registry.buildUntypedAgent("ModelExplorer");
			result = ThreadContextManager.inSystemInteraction(AgentInvoker.class,
				() -> (String) explorer.invoke(Map.of("message", prompt)));
		} catch (Exception e) {
			result = "Error: " + e.getMessage();
			ToolCallTracking.recordAgentCallCompleteIfTracking(truncate(result, RESULT_SUMMARY_MAX_LEN));
			throw e;
		}
		String resultSummary = truncate(result, RESULT_SUMMARY_MAX_LEN);
		ToolCallTracking.recordAgentCallCompleteIfTracking(resultSummary);
		return result;
	}

	/**
	 * Invokes the {@code TLScriptAgent} with a work-order-derived user message.
	 *
	 * @param fromAgent
	 *        Name of the calling agent (e.g. "Orchestrator").
	 * @param workOrder
	 *        Distilled goal, task, context.
	 * @return The TLScript agent's answer.
	 */
	public static String invokeTLScript(String fromAgent, AgentWorkOrder workOrder) {
		String userMessage = workOrder.task() != null && !workOrder.task().isEmpty()
			? workOrder.task()
			: workOrder.goal();
		if (workOrder.context() != null && !workOrder.context().isEmpty()) {
			userMessage = userMessage + "\n\nContext: " + workOrder.context();
		}
		final String messageForLambda = userMessage;
		ToolCallTracking.recordAgentCallStartIfTracking(fromAgent, "TLScriptAgent", messageForLambda);
		String result;
		try {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				throw new RuntimeException("AgentRegistry is not available.");
			}
			UntypedAgent agent = registry.buildUntypedAgent("tlscript");
			result = ThreadContextManager.inSystemInteraction(AgentInvoker.class,
				() -> (String) agent.invoke(Map.of("message", messageForLambda)));
		} catch (Exception e) {
			result = "Error: " + e.getMessage();
			ToolCallTracking.recordAgentCallCompleteIfTracking(truncate(result, RESULT_SUMMARY_MAX_LEN));
			throw e;
		}
		String resultSummary = truncate(result, RESULT_SUMMARY_MAX_LEN);
		ToolCallTracking.recordAgentCallCompleteIfTracking(resultSummary);
		return result;
	}

	/**
	 * Invokes the {@code ModelEditor} agent with work-order-derived requirements.
	 *
	 * @param fromAgent
	 *        Name of the calling agent (e.g. "Orchestrator").
	 * @param workOrder
	 *        Task holds the business requirements.
	 * @return The creation result.
	 */
	public static String invokeCreator(String fromAgent, AgentWorkOrder workOrder) {
		String requirements = workOrder.task() != null && !workOrder.task().isEmpty()
			? workOrder.task()
			: workOrder.goal();
		if (workOrder.context() != null && !workOrder.context().isEmpty()) {
			requirements = requirements + "\n\nContext: " + workOrder.context();
		}
		final String requirementsForLambda = requirements;
		ToolCallTracking.recordAgentCallStartIfTracking(fromAgent, "ModelEditor", requirementsForLambda);
		String result;
		try {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				throw new RuntimeException("AgentRegistry is not available.");
			}
			UntypedAgent creator = registry.buildUntypedAgent("model-editor");
			result = ThreadContextManager.inSystemInteraction(AgentInvoker.class,
				() -> (String) creator.invoke(Map.of("message", requirementsForLambda)));
		} catch (Exception e) {
			result = "Error: " + e.getMessage();
			ToolCallTracking.recordAgentCallCompleteIfTracking(truncate(result, RESULT_SUMMARY_MAX_LEN));
			throw e;
		}
		String resultSummary = truncate(result, RESULT_SUMMARY_MAX_LEN);
		ToolCallTracking.recordAgentCallCompleteIfTracking(resultSummary);
		return result;
	}

	private static String buildExplorerPrompt(AgentWorkOrder workOrder) {
		StringBuilder sb = new StringBuilder();
		if (workOrder.goal() != null && !workOrder.goal().isEmpty()) {
			sb.append("Goal: ").append(workOrder.goal()).append("\n");
		}
		if (workOrder.task() != null && !workOrder.task().isEmpty()) {
			sb.append("Task: ").append(workOrder.task()).append("\n");
		}
		if (workOrder.context() != null && !workOrder.context().isEmpty()) {
			sb.append("Context: ").append(workOrder.context()).append("\n");
		}
		if (workOrder.expectedOutputFormat() != null && !workOrder.expectedOutputFormat().isEmpty()) {
			sb.append("Expected output format: ").append(workOrder.expectedOutputFormat()).append("\n");
		}
		return sb.length() > 0 ? sb.toString() : workOrder.task();
	}

	private static String truncate(String s, int maxLen) {
		if (s == null) {
			return "";
		}
		return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
	}

	private AgentInvoker() {
		// utility
	}
}
