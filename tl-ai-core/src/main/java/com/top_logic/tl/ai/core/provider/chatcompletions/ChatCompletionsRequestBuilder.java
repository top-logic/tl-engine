/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.chatcompletions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonUtil;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Maps a {@link ProviderRequest} to a {@link ChatCompletionsRequest} proto DTO and serializes it.
 *
 * <p>Used by all Chat Completions API-compatible providers (<code>OpenAI</code>-compatible, Mistral, <code>xAI</code>,
 * etc.).</p>
 */
public class ChatCompletionsRequestBuilder {

	/**
	 * Builds the JSON request body for a Chat Completions API request.
	 */
	public static String buildRequestJson(ProviderRequest request) {
		return buildRequestJson(request, null);
	}

	/**
	 * Builds the JSON request body, allowing a provider-specific {@code tool_choice} override.
	 */
	public static String buildRequestJson(ProviderRequest request, String toolChoiceOverride) {
		return buildRequestJson(request, toolChoiceOverride, TokenLimitParam.MAX_TOKENS);
	}

	/**
	 * Builds the JSON request body, allowing a provider-specific {@code tool_choice} override and
	 * selecting which JSON field carries the output-token limit.
	 *
	 * @param tokenLimitParam
	 *        Which field name the output-token limit is written under. Use
	 *        {@link TokenLimitParam#MAX_COMPLETION_TOKENS} for GPT-5/o-series reasoning models on
	 *        Azure <code>OpenAI</code>; {@link TokenLimitParam#MAX_TOKENS} for all other backends.
	 */
	public static String buildRequestJson(ProviderRequest request, String toolChoiceOverride,
			TokenLimitParam tokenLimitParam) {
		return buildRequestJson(request, toolChoiceOverride, tokenLimitParam, false);
	}

	/**
	 * Builds the JSON request body, additionally requesting a streaming usage report.
	 *
	 * @param includeUsage
	 *        When {@code true}, sets {@code stream_options.include_usage} so the server emits a final
	 *        chunk carrying token usage. <code>OpenAI</code>, Azure <code>OpenAI</code> and <code>xAI</code> omit usage from streamed
	 *        responses unless this flag is set; Mistral reports usage unconditionally and does not
	 *        need it. Other parameters are as documented on the three-argument overload.
	 */
	public static String buildRequestJson(ProviderRequest request, String toolChoiceOverride,
			TokenLimitParam tokenLimitParam, boolean includeUsage) {
		ChatCompletionsRequest req = ChatCompletionsRequest.create()
			.setModel(request.model())
			.setStream(true)
			.setMessages(buildMessages(request));

		if (includeUsage) {
			req.setStreamOptions(Map.of("include_usage", Boolean.TRUE));
		}

		if (tokenLimitParam == TokenLimitParam.MAX_COMPLETION_TOKENS) {
			req.setMaxCompletionTokens(request.maxOutputTokens());
		} else {
			req.setMaxTokens(request.maxOutputTokens());
		}

		writeSamplingParams(req, request.modelOptions());

		if (!request.tools().isEmpty()) {
			List<Object> tools = new ArrayList<>();
			for (ToolSchema tool : request.tools()) {
				Map<String, Object> function = new LinkedHashMap<>();
				function.put("name", tool.name());
				function.put("description", tool.description());
				function.put("parameters", parseJson(tool.inputSchemaJson()));

				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("type", "function");
				entry.put("function", function);
				tools.add(entry);
			}
			req.setTools(tools);
		}

		if (request.responseFormat() == ResponseFormat.JSON) {
			req.setResponseFormat(Map.of("type", "json_object"));
		}

		if (toolChoiceOverride != null) {
			req.setToolChoice(toolChoiceOverride);
		} else if (request.toolChoice() != null) {
			req.setToolChoice(switch (request.toolChoice()) {
				case REQUIRED -> "required";
				case NONE -> "none";
				case AUTO -> "auto";
			});
		}

		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			req.writeTo(json);
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize ProviderRequest to JSON", e);
		}
		return buffer.toString();
	}

	// --- Message building ---

	private static List<Object> buildMessages(ProviderRequest request) {
		List<Object> messages = new ArrayList<>();
		if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
			Map<String, Object> system = new LinkedHashMap<>();
			system.put("role", "system");
			system.put("content", request.systemPrompt());
			messages.add(system);
		}
		for (Message msg : request.messages()) {
			if (msg instanceof UserMessage user) {
				messages.add(buildUserMessage(user));
			} else if (msg instanceof AssistantMessage assistant) {
				messages.add(buildAssistantMessage(assistant));
			} else if (msg instanceof ToolResultMessage result) {
				messages.add(buildToolResultMessage(result));
			}
		}
		return messages;
	}

	private static Map<String, Object> buildUserMessage(UserMessage user) {
		boolean hasImages = user.content().stream()
			.anyMatch(p -> p instanceof ImageContent || p instanceof PdfContent);

		Map<String, Object> msg = new LinkedHashMap<>();
		msg.put("role", "user");

		if (hasImages) {
			List<Object> parts = new ArrayList<>();
			for (ContentPart part : user.content()) {
				if (part instanceof TextContent text) {
					parts.add(Map.of("type", "text", "text", text.text()));
				} else if (part instanceof ImageContent image) {
					Map<String, Object> imageUrlObj = new LinkedHashMap<>();
					imageUrlObj.put("url", image.url());
					parts.add(Map.of("type", "image_url", "image_url", imageUrlObj));
				}
				// PdfContent triggers array mode but is not serialized itself.
			}
			msg.put("content", parts);
		} else {
			StringBuilder text = new StringBuilder();
			for (ContentPart part : user.content()) {
				if (part instanceof TextContent tc) {
					text.append(tc.text());
				}
			}
			msg.put("content", text.toString());
		}

		return msg;
	}

	private static Map<String, Object> buildAssistantMessage(AssistantMessage assistant) {
		Map<String, Object> msg = new LinkedHashMap<>();
		msg.put("role", "assistant");

		StringBuilder text = new StringBuilder();
		for (ContentPart part : assistant.content()) {
			if (part instanceof TextContent tc) {
				text.append(tc.text());
			}
		}
		msg.put("content", text.toString());

		List<Object> toolCalls = new ArrayList<>();
		for (ContentPart part : assistant.content()) {
			if (part instanceof ToolCall toolCall) {
				String args = toolCall.arguments();
				Map<String, Object> function = new LinkedHashMap<>();
				function.put("name", toolCall.name());
				function.put("arguments", args.isEmpty() ? "{}" : args);

				Map<String, Object> tc = new LinkedHashMap<>();
				tc.put("id", toolCall.id());
				tc.put("type", "function");
				tc.put("function", function);
				toolCalls.add(tc);
			}
		}
		if (!toolCalls.isEmpty()) {
			msg.put("tool_calls", toolCalls);
		}

		return msg;
	}

	private static Map<String, Object> buildToolResultMessage(ToolResultMessage result) {
		String content;
		ToolState state = result.state();
		if (state instanceof ToolState.Completed c) {
			content = c.result();
		} else if (state instanceof ToolState.Failed f) {
			content = f.error();
		} else {
			content = "";
		}

		Map<String, Object> msg = new LinkedHashMap<>();
		msg.put("role", "tool");
		msg.put("tool_call_id", result.toolCallId());
		msg.put("name", result.toolName());
		msg.put("content", content);
		return msg;
	}

	// --- Utilities ---

	/** Parses a JSON string to a Map/List/primitive for use as a {@code json} field value. */
	public static Object parseJson(String json) {
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			return JsonUtil.nextJsonValue(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void writeSamplingParams(ChatCompletionsRequest req,
			Map<String, Object> modelOptions) {
		Object temperature = modelOptions.get("temperature");
		if (temperature != null) {
			req.setTemperature(Double.parseDouble(temperature.toString()));
		}
		Object topP = modelOptions.get("topP");
		if (topP != null) {
			req.setTopP(Double.parseDouble(topP.toString()));
		}
	}

	private ChatCompletionsRequestBuilder() {
		// Utility class — no instances.
	}
}
