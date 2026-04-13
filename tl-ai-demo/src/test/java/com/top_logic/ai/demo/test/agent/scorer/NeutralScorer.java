/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.scorer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult;
import com.top_logic.ai.service.scripting.OpenAIScriptFunctions;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Neutral scorer that provides a final assessment after the judge's evaluation.
 */
public class NeutralScorer {

	public static final String DEFAULT_SCORER_MODEL = "claude-sonnet-4-5-20250929";

	private final String _scorerModelName;

	/**
	 * Creates a neutral scorer with default model.
	 */
	public NeutralScorer() {
		this(DEFAULT_SCORER_MODEL);
	}

	/**
	 * Creates a neutral scorer with specific model.
	 */
	public NeutralScorer(String scorerModelName) {
		_scorerModelName = scorerModelName;
	}

	/**
	 * Scores the agent's performance based on user prompt, agent answer, and judge evaluation.
	 *
	 * @param userPrompt
	 *        The original user prompt
	 * @param agentAnswer
	 *        The agent's response
	 * @param judgeResult
	 *        The judge's detailed evaluation
	 * @param scorerSystemPrompt
	 *        The system prompt for the scorer
	 * @return Final score and assessment
	 */
	public NeutralScorerResult score(
		String userPrompt,
		String agentAnswer,
		JudgeResult judgeResult,
		String scorerSystemPrompt
	) {
		return ThreadContextManager.inSystemInteraction(
			NeutralScorer.class,
			() -> {
				try {
					String userMessage = buildUserMessage(userPrompt, agentAnswer, judgeResult);

					List<Object> messages = List.of(
						Map.of("role", "system", "content", scorerSystemPrompt),
						Map.of("role", "user", "content", userMessage));

					String resultText = OpenAIScriptFunctions.chat(messages, _scorerModelName, null, null);
					return parseResult(resultText);

				} catch (Exception ex) {
					Logger.error("Failed to score: " + ex.getMessage(), ex, NeutralScorer.class);
					throw new RuntimeException("Failed to score: " + ex.getMessage(), ex);
				}
			});
	}

	/**
	 * Builds the user message with prompt, answer, and judge evaluation.
	 */
	private String buildUserMessage(String userPrompt, String agentAnswer, JudgeResult judgeResult) {
		try {
			// Convert JudgeResult to JSON
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				judgeResult.writeTo(json);
			}
			String judgeJson = buffer.toString();

			// Build JSON envelope
			StringBuilder json = new StringBuilder();
			json.append("{\n");
			json.append("  \"user_prompt\": ").append(escapeJson(userPrompt)).append(",\n");
			json.append("  \"agent_answer\": ").append(escapeJson(agentAnswer)).append(",\n");
			json.append("  \"judge_evaluation\": ").append(judgeJson).append("\n");
			json.append("}\n");
			return json.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to build user message", ex);
		}
	}

	/**
	 * Escapes a string for JSON.
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
	 * Parses the scorer's JSON response.
	 */
	private NeutralScorerResult parseResult(String responseText) {
		try {
			String jsonOnly = extractJson(responseText);
			StringR reader = new StringR(jsonOnly);
			JsonReader jsonReader = new JsonReader(reader);

			NeutralScorerResult result = new NeutralScorerResult();

			jsonReader.beginObject();
			while (jsonReader.hasNext()) {
				String field = jsonReader.nextName();
				switch (field) {
					case "finalScore":
						result.setFinalScore(jsonReader.nextInt());
						break;
					case "explanation":
						result.setExplanation(jsonReader.nextString());
						break;
					case "keyIssues":
						jsonReader.beginArray();
						while (jsonReader.hasNext()) {
							result.getKeyIssues().add(jsonReader.nextString());
						}
						jsonReader.endArray();
						break;
					case "strengths":
						jsonReader.beginArray();
						while (jsonReader.hasNext()) {
							result.getStrengths().add(jsonReader.nextString());
						}
						jsonReader.endArray();
						break;
					default:
						jsonReader.skipValue();
						break;
				}
			}
			jsonReader.endObject();

			return result;
		} catch (Exception ex) {
			throw new RuntimeException(
				"Failed to parse scorer result JSON: " + ex.getMessage() + "\nResponse was: " + responseText, ex);
		}
	}

	/**
	 * Extracts JSON from response.
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
	 * Loads the scorer system prompt from file.
	 */
	public static String loadScorerSystemPrompt() {
		try {
			return new String(Files.readAllBytes(
				Paths.get("src/test/resources/com/top_logic/ai/demo/neutral-scorer-system-prompt.txt")));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load scorer system prompt", ex);
		}
	}
}
