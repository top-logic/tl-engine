/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.agents.handoff.AgentWorkOrder;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.help.TLScriptReferenceTool;
import com.top_logic.ai.service.tools.model.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.model.InstanceBrowserTool;
import com.top_logic.ai.service.tools.model.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.model.ModelBrowserTool;
import com.top_logic.ai.service.tools.model.ModelEditorTool;
import com.top_logic.ai.service.tools.model.ModelSearchTool;
import com.top_logic.ai.service.tools.model.TypeAnalysisTool;
import com.top_logic.ai.service.skills.SkillConfig;
import com.top_logic.ai.service.skills.SkillRegistry;
import com.top_logic.ai.service.skills.SkillTool;
import com.top_logic.ai.service.tools.script.TLScriptTool;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;

/**
 * Internal component for agent-to-agent calls.
 * <p>
 * Used by Orchestrator (and by TLScript's "request exploration" path) to invoke
 * {@code ModelExplorer}, {@code ModelEditor}, or {@code TLScriptAgent}. Records {@link com.top_logic.ai.service.tools.TraceEntry.AgentCallTraceEntry}
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
			AIService aiService = AIService.getInstance();
			if (aiService == null) {
				throw new RuntimeException("AIService is not available.");
			}
			ChatModel explorationModel = aiService.getChatModel("mistral-large-latest");
			ModelExplorer explorer = buildModelExplorer(explorationModel);
			result = ThreadContextManager.inSystemInteraction(AgentInvoker.class,
				() -> explorer.explore(prompt));
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
			AIService aiService = AIService.getInstance();
			if (aiService == null) {
				throw new RuntimeException("AIService is not available.");
			}
			ChatModel tlscriptModel = aiService.getChatModel("mistral-large-latest");
			SkillRegistry registry = SkillConfig.getInstance();
			String systemPrompt = registry.buildSystemPrompt(TLScriptAgent.class);
			final TLScriptAgent agent = AgenticServices.agentBuilder(TLScriptAgent.class)
				.chatModel(tlscriptModel)
				.systemMessageProvider(memoryId -> systemPrompt)
				.tools(new TLScriptReferenceTool(), new AgentDelegationTools.Exploration("TLScriptAgent"),
					new TLScriptTool(), new SkillTool(registry, TLScriptAgent.class))
				.build();
			result = ThreadContextManager.inSystemInteraction(AgentInvoker.class,
				() -> agent.answer(messageForLambda));
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
			AIService aiService = AIService.getInstance();
			if (aiService == null) {
				throw new RuntimeException("AIService is not available.");
			}
			ChatModel creationModel = aiService.getChatModel("mistral-large-latest");
			SkillRegistry registry = SkillConfig.getInstance();
			String systemPrompt = registry.buildSystemPrompt(ModelEditor.class);
			ModelEditor creator = AgenticServices.agentBuilder(ModelEditor.class)
				.chatModel(creationModel)
				.systemMessageProvider(memoryId -> systemPrompt)
				.tools(new ModelEditorTool(), new AgentDelegationTools.TLScript("ModelEditor"),
					new SkillTool(registry, ModelEditor.class))
				.build();
			result = ThreadContextManager.inSystemInteraction(AgentInvoker.class,
				() -> creator.createModel(requirementsForLambda));
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

	private static ModelExplorer buildModelExplorer(ChatModel explorationModel) {
		ModelBrowserTool browserTool = new ModelBrowserTool();
		InstanceBrowserTool instanceTool = new InstanceBrowserTool();
		TypeAnalysisTool analysisTool = new TypeAnalysisTool();
		ModelSearchTool searchTool = new ModelSearchTool();
		ModelAnnotationBrowserTool annotationTool = new ModelAnnotationBrowserTool();
		AnnotationDefinitionExplorerTool definitionTool = new AnnotationDefinitionExplorerTool();
		SkillRegistry registry = SkillConfig.getInstance();
		String systemPrompt = registry.buildSystemPrompt(ModelExplorer.class);
		return AgenticServices.agentBuilder(ModelExplorer.class)
			.chatModel(explorationModel)
			.systemMessageProvider(memoryId -> systemPrompt)
			.tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool,
				new SkillTool(registry, ModelExplorer.class))
			.build();
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
