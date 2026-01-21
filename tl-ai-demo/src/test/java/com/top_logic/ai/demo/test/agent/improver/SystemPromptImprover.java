/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.improver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.top_logic.ai.service.AIService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * System prompt improver that analyzes test results and suggests improvements.
 */
public class SystemPromptImprover {

	public static final String DEFAULT_IMPROVER_MODEL = "claude-sonnet-4-5-20250929";

	private final AIService _aiService;
	private final String _improverModelName;

	/**
	 * Creates an improver with default model.
	 */
	public SystemPromptImprover(AIService aiService) {
		this(aiService, DEFAULT_IMPROVER_MODEL);
	}

	/**
	 * Creates an improver with specific model.
	 */
	public SystemPromptImprover(AIService aiService, String improverModelName) {
		_aiService = aiService;
		_improverModelName = improverModelName;
	}

	/**
	 * Analyzes test results and suggests improvements to the system prompt.
	 * 
	 * @param currentSystemPrompt
	 *        The current system prompt of the agent
	 * @param testResults
	 *        All test results to analyze
	 * @param improverSystemPrompt
	 *        The system prompt for the improver
	 * @return Improvement suggestions
	 */
	public String improveSystemPrompt(
		String currentSystemPrompt,
		List<TestResult> testResults,
		String improverSystemPrompt
	) {
		return ThreadContextManager.inSystemInteraction(
			SystemPromptImprover.class,
			() -> {
				try {
					ChatModel improverModel = _aiService.getChatModel(_improverModelName);

					String userMessage = buildUserMessage(currentSystemPrompt, testResults);

					SystemMessage systemMessage = SystemMessage.from(improverSystemPrompt);
					UserMessage userMsg = UserMessage.from(userMessage);

					ChatRequest request = ChatRequest.builder()
						.messages(systemMessage, userMsg)
						.build();

					ChatResponse response = improverModel.chat(request);
					AiMessage aiMessage = response.aiMessage();
					String resultText = aiMessage.text();

					Logger.info("Improver response: " + resultText, SystemPromptImprover.class);

					return resultText;

				} catch (Exception ex) {
					Logger.error("Failed to improve system prompt: " + ex.getMessage(), ex,
						SystemPromptImprover.class);
					throw new RuntimeException("Failed to improve system prompt: " + ex.getMessage(), ex);
				}
			});
	}

	/**
	 * Builds the user message with current prompt and test results.
	 */
	private String buildUserMessage(String currentSystemPrompt, List<TestResult> testResults) {
		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				json.name("current_system_prompt").value(currentSystemPrompt);

				json.name("test_results");
				json.beginArray();
				for (TestResult testResult : testResults) {
					json.beginObject();
					json.name("test_name").value(testResult.getTestName());
					json.name("user_prompt").value(testResult.getUserPrompt());
					json.name("agent_answer").value(testResult.getAgentAnswer());
					// Embed judge_evaluation JSON as escaped string (LLM will parse it)
					json.name("judge_evaluation").value(testResult.getJudgeEvaluationJson());
					// Embed final_score JSON as escaped string (LLM will parse it)
					json.name("final_score").value(testResult.getFinalScoreJson());
					json.endObject();
				}
				json.endArray();

				json.endObject();
			}
			return buffer.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to build user message", ex);
		}
	}

	/**
	 * Loads the improver system prompt from file.
	 */
	public static String loadImproverSystemPrompt() {
		try {
			return new String(Files.readAllBytes(
				Paths.get("src/test/resources/com/top_logic/ai/demo/system-prompt-improver-prompt.txt")));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load improver system prompt", ex);
		}
	}
}
