/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.judge;

import com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult;
import com.top_logic.ai.service.AIService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * Simplified client for using AI to score agent answers.
 * 
 * <p>
 * This sends:
 * <ul>
 * <li>Required facts (what the agent MUST mention)</li>
 * <li>Oracle facts (what exists in the model - for hallucination detection)</li>
 * <li>Agent's answer</li>
 * </ul>
 * to an AI judge that scores the answer.
 * </p>
 */
public class JudgeClient {

	public static final String DEFAULT_JUDGE_MODEL = "claude-sonnet-4-5-20250929";

	private final AIService _aiService;
	private final String _judgeModelName;

	/**
	 * Creates a judge client with default model.
	 */
	public JudgeClient(AIService aiService) {
		this(aiService, DEFAULT_JUDGE_MODEL);
	}

	/**
	 * Creates a judge client with specific model.
	 */
	public JudgeClient(AIService aiService, String judgeModelName) {
		_aiService = aiService;
		_judgeModelName = judgeModelName;
	}

	/**
	 * Scores an agent's answer against required facts and oracle.
	 * 
	 * @param requiredFactsJson
	 *        What the agent MUST mention (from RequiredFacts.toJson())
	 * @param oracleJson
	 *        Complete model truth (from OracleLoader)
	 * @param agentAnswer
	 *        The agent's actual answer
	 * @param judgeSystemPrompt
	 *        The system prompt for the judge
	 * @return Judge result with score and violations
	 */
	public JudgeResult score(
		String requiredFactsJson,
		String oracleJson,
		String agentAnswer,
		String judgeSystemPrompt
	) {
		// Use ThreadContextManager to provide InteractionContext
		return ThreadContextManager.inSystemInteraction(
			JudgeClient.class,
			() -> {
				try {
					ChatModel judgeModel = _aiService.getChatModel(_judgeModelName);

					String userPrompt = buildUserPrompt(requiredFactsJson, oracleJson, agentAnswer);

					SystemMessage systemMessage = SystemMessage.from(judgeSystemPrompt);
					UserMessage userMessage = UserMessage.from(userPrompt);

					ChatRequest request = ChatRequest.builder()
						.messages(systemMessage, userMessage)
						.build();

					ChatResponse response = judgeModel.chat(request);
					AiMessage aiMessage = response.aiMessage();
					String resultText = aiMessage.text();

					Logger.info("Judge response: " + resultText, JudgeClient.class);

					return parseJudgeResult(resultText);
					
				} catch (Exception ex) {
					Logger.error("Failed to score answer: " + ex.getMessage(), ex, JudgeClient.class);
					throw new RuntimeException("Failed to score answer: " + ex.getMessage(), ex);
				}
			});
	}

	/**
	 * Builds the user prompt with required facts, oracle, and agent answer as a single JSON
	 * envelope.
	 * 
	 * <p>
	 * This prevents misbinding between required_facts and available_facts by sending a clear
	 * structured format instead of plain-text sections.
	 * </p>
	 */
	private String buildUserPrompt(String requiredFactsJson, String oracleJson, String agentAnswer) {
		// Build JSON envelope
		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append("  \"required_facts\": ").append(requiredFactsJson).append(",\n");
		json.append("  \"available_facts\": ").append(oracleJson).append(",\n");
		json.append("  \"agent_answer\": ").append(escapeJson(agentAnswer)).append("\n");
		json.append("}\n");
		return json.toString();
	}

	/**
	 * Escapes a string for use as a JSON value.
	 * 
	 * <p>
	 * Handles quotes, backslashes, newlines, and other special characters.
	 * </p>
	 */
	private String escapeJson(String value) {
		if (value == null) {
			return "null";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("\"");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					// Control characters
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
					break;
			}
		}
		sb.append("\"");
		return sb.toString();
	}

	/**
	 * Extracts JSON from a response that may contain surrounding text.
	 */
	private String extractJson(String response) {
		int start = response.indexOf('{');
		int end = response.lastIndexOf('}');
		if (start == -1 || end == -1 || end < start) {
			throw new IllegalArgumentException("Response does not contain valid JSON object.");
		}
		return response.substring(start, end + 1);
	}

	/**
	 * Parses the judge's JSON response.
	 */
	private JudgeResult parseJudgeResult(String responseText) {
		try {
			String jsonOnly = extractJson(responseText);
			StringR reader = new StringR(jsonOnly);
			JsonReader jsonReader = new JsonReader(reader);
			return JudgeResult.readJudgeResult(jsonReader);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Failed to parse judge result JSON: " + ex.getMessage() + "\nResponse was: " + responseText, ex);
		}
	}
}
