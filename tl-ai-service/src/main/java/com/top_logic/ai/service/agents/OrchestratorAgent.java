/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.ai.service.providers.AnthropicChatModelFactory;
import com.top_logic.ai.service.skills.SkillConfig;
import com.top_logic.ai.service.skills.SkillRegistry;
import com.top_logic.ai.service.skills.SkillTool;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;

/**
 * Single entrypoint for chat: the LLM plans steps and delegates to specialists as needed (zero, one, or several).
 * <p>
 * The chat UI sends user messages here. The orchestrator thinks: what does the user want, what steps are needed,
 * do any need a specialist? It may call zero tools (e.g. ask for clarification), one, or multiple in sequence
 * (e.g. Explorer for model context, then TLScript for how to use it). After each tool result it reasons: is it
 * enough? When satisfied, it synthesizes and returns the final answer. See {@link OrchestratorRouter} for the
 * system prompt and {@link AgentDelegationTools} for the delegate tools.
 * </p>
 * <p>
 * The caller must set the run's trace (e.g. {@link com.top_logic.ai.service.tools.ToolCallTracking#setTracker})
 * before calling {@link #chat(ChatMemory, String)} so agent and tool calls are recorded.
 * </p>
 */
public final class OrchestratorAgent {

	/**
	 * Handles a user message: LLM plans steps, calls zero/one/multiple specialist tools as needed, synthesizes result.
	 *
	 * @param chatMemory
	 *        Conversation memory that persists across messages in this session. The orchestrator's
	 *        {@code @SystemMessage} is always retained; older user/assistant turns are evicted when
	 *        the window is full.
	 * @param userMessage
	 *        Raw user input (may include legacy prefixes like /explore-model or /tlscript).
	 * @return The delegated specialist's answer, or a short fallback.
	 */
	public static String chat(ChatMemory chatMemory, String userMessage) {
		if (userMessage == null || userMessage.trim().isEmpty()) {
			return "Please ask a question about the model, TLScript, or model creation.";
		}
		String trimmed = userMessage.trim();

		AIService aiService = AIService.getInstance();
		if (aiService == null) {
			return "AIService is not available. Please ensure the service is started.";
		}

		ChatModel model = createThinkingModel(aiService);
		SkillRegistry registry = SkillConfig.getInstance();
		String systemPrompt = registry.buildSystemPrompt(OrchestratorRouter.class);
		OrchestratorRouter router = AgenticServices.agentBuilder(OrchestratorRouter.class)
			.chatModel(model)
			.chatMemory(chatMemory)
			.systemMessageProvider(memoryId -> systemPrompt)
			.tools(
				new AgentDelegationTools.Exploration("Orchestrator"),
				new AgentDelegationTools.Creation("Orchestrator"),
				new AgentDelegationTools.TLScript("Orchestrator"),
				new SkillTool(registry, OrchestratorRouter.class))
			.build();

		return ThreadContextManager.inSystemInteraction(OrchestratorAgent.class,
			() -> router.chat(trimmed));
	}

	private static final String ORCHESTRATOR_MODEL = "claude-opus-4-6";

	/**
	 * Creates a dedicated Anthropic model with adaptive thinking enabled.
	 *
	 * <p>
	 * This builds an unpooled model specifically for the orchestrator so that thinking is
	 * structurally separated from the response text. The model decides autonomously when and how
	 * much to think based on query complexity.
	 * </p>
	 */
	private static ChatModel createThinkingModel(AIService aiService) {
		ChatModelFactory factory = aiService.getFactory(ORCHESTRATOR_MODEL);
		if (!(factory instanceof AnthropicChatModelFactory)) {
			// Fallback to pooled model if factory is not Anthropic
			return aiService.getChatModel(ORCHESTRATOR_MODEL);
		}

		AnthropicChatModelFactory.Config config = ((AnthropicChatModelFactory) factory).getConfig();
		return AnthropicChatModel.builder()
			.apiKey(config.getApiKey())
			.baseUrl(config.getBaseUrl())
			.modelName(ORCHESTRATOR_MODEL)
			.thinkingType("adaptive")
			.maxTokens(16000)
			.returnThinking(false)
			.sendThinking(true)
			.build();
	}

	private OrchestratorAgent() {
		// utility
	}
}
