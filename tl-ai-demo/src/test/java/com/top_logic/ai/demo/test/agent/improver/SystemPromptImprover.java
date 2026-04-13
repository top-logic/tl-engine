/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.improver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.scripting.OpenAIScriptFunctions;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * System prompt improver that analyzes test results and suggests improvements.
 */
public class SystemPromptImprover {

	public static final String DEFAULT_IMPROVER_MODEL = "claude-sonnet-4-5-20250929";

	private final String _improverModelName;

	/**
	 * Creates an improver with default model.
	 */
	public SystemPromptImprover() {
		this(DEFAULT_IMPROVER_MODEL);
	}

	/**
	 * Creates an improver with specific model.
	 */
	public SystemPromptImprover(String improverModelName) {
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
					String userMessage = buildUserMessage(currentSystemPrompt, testResults);

					List<Object> messages = List.of(
						Map.of("role", "system", "content", improverSystemPrompt),
						Map.of("role", "user", "content", userMessage));

					return OpenAIScriptFunctions.chat(messages, _improverModelName, null, null);

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
					json.name("judge_evaluation").value(testResult.getJudgeEvaluationJson());
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
