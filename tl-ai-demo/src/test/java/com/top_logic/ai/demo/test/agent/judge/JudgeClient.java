/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.judge;

import java.util.List;
import java.util.Map;

import com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult;
import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;
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
 * 
 * @author jhu
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
	 * Scores an agent's answer with LLM-inferred expectations and tool trace evidence.
	 * 
	 * <p>
	 * This method provides both the model XML and a compact tool-evidence trace to the judge,
	 * allowing it to distinguish between:
	 * <ul>
	 * <li>Facts not in XML but fetched from tools (allowed)</li>
	 * <li>Facts not in XML and not fetched (hallucination/notFetched)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param userPrompt
	 *        The original prompt given to the agent
	 * @param modelXml
	 *        Complete model XML (authoritative source of truth)
	 * @param agentAnswer
	 *        The agent's actual answer
	 * @param toolTrace
	 *        List of tool calls with arguments and result references
	 * @param toolResults
	 *        Map of result references to minimized tool result data
	 * @param judgeSystemPrompt
	 *        The system prompt for the judge
	 * @return Judge result with score and violations
	 */
	public JudgeResult scoreWithPromptAndToolTrace(
		String userPrompt,
		String modelXml,
		String agentAnswer,
		List<ToolCallTracker.ToolCall> toolTrace,
		Map<String, Object> toolResults,
		String judgeSystemPrompt
	) {
		return ThreadContextManager.inSystemInteraction(
			JudgeClient.class,
			() -> {
				try {
					ChatModel judgeModel = _aiService.getChatModel(_judgeModelName);

					String judgeUserPrompt = buildPromptBasedUserMessageWithToolTrace(
						userPrompt, modelXml, agentAnswer, toolTrace, toolResults);

					SystemMessage systemMessage = SystemMessage.from(judgeSystemPrompt);
					UserMessage userMessage = UserMessage.from(judgeUserPrompt);

					ChatRequest request = ChatRequest.builder()
						.messages(systemMessage, userMessage)
						.build();

					ChatResponse response = judgeModel.chat(request);
					AiMessage aiMessage = response.aiMessage();
					String resultText = aiMessage.text();

					return parseJudgeResult(resultText);
					
				} catch (Exception ex) {
					Logger.error("Failed to score answer: " + ex.getMessage(), ex, JudgeClient.class);
					throw new RuntimeException("Failed to score answer: " + ex.getMessage(), ex);
				}
			});
	}

	/**
	 * Builds user prompt for prompt-based judging with tool trace evidence.
	 */
	private String buildPromptBasedUserMessageWithToolTrace(
		String userPrompt,
		String modelXml,
		String agentAnswer,
		List<ToolCallTracker.ToolCall> toolTrace,
		Map<String, Object> toolResults
	) {
		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				
				json.name("question").value(userPrompt);
				json.name("agent_answer").value(agentAnswer);
				json.name("model_xml").value(modelXml);
				
				// Write tool_trace array
				json.name("tool_trace");
				json.beginArray();
				for (ToolCallTracker.ToolCall call : toolTrace) {
					json.beginObject();
					json.name("tool").value(call.getTool());
					json.name("args");
					writeJsonValue(json, call.getArgs());
					json.name("result_ref").value(call.getResultRef());
					json.endObject();
				}
				json.endArray();
				
				// Write tool_results object
				json.name("tool_results");
				writeJsonValue(json, toolResults);
				
				json.endObject();
			}
			return buffer.toString();
		} catch (Exception ex) {
			Logger.error("Failed to build prompt with tool trace: " + ex.getMessage(), ex, JudgeClient.class);
			throw new RuntimeException("Failed to build prompt with tool trace: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Writes a JSON value (object, array, or primitive) to the JSON writer.
	 */
	@SuppressWarnings("unchecked")
	private void writeJsonValue(JsonWriter json, Object value) throws java.io.IOException {
		if (value == null) {
			json.value((String) null);
		} else if (value instanceof String) {
			json.value((String) value);
		} else if (value instanceof Number) {
			if (value instanceof Integer) {
				json.value((Integer) value);
			} else if (value instanceof Long) {
				json.value((Long) value);
			} else if (value instanceof Double) {
				json.value((Double) value);
			} else if (value instanceof Float) {
				json.value((Float) value);
			} else {
				json.value(value.toString());
			}
		} else if (value instanceof Boolean) {
			json.value((Boolean) value);
		} else if (value instanceof Map) {
			json.beginObject();
			Map<String, Object> map = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				json.name(entry.getKey());
				writeJsonValue(json, entry.getValue());
			}
			json.endObject();
		} else if (value instanceof List) {
			json.beginArray();
			List<Object> list = (List<Object>) value;
			for (Object item : list) {
				writeJsonValue(json, item);
			}
			json.endArray();
		} else {
			// For other types, convert to string
			json.value(value.toString());
		}
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
