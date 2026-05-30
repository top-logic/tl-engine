/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import static com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsRequestBuilder.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonUtil;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Maps a {@link ProviderRequest} to an AWS Bedrock Converse API request JSON.
 *
 * <p>The Converse API uses a content-block model closely analogous to the Anthropic Messages API:
 * user/assistant roles with typed content blocks, plus a top-level {@code toolConfig}. Unlike the
 * Anthropic API, the model identifier belongs in the URL path, not the request body, so no
 * {@code model} field is emitted here.</p>
 */
public final class BedrockConverseRequestBuilder {

	private BedrockConverseRequestBuilder() {
		// Static utility class.
	}

	/**
	 * Builds the Bedrock Converse API request JSON from a {@link ProviderRequest}.
	 *
	 * <p>The returned JSON is ready to use as the HTTP request body for the
	 * {@code /model/{modelId}/converse} or {@code /model/{modelId}/converse-stream} endpoint.
	 * The {@code model} field is intentionally absent — it belongs in the URL, not the body.</p>
	 */
	public static String buildRequestJson(ProviderRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();

		// system: array of text blocks (omitted when empty/null)
		if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
			List<Object> system = new ArrayList<>();
			Map<String, Object> sysBlock = new LinkedHashMap<>();
			sysBlock.put("text", request.systemPrompt());
			system.add(sysBlock);
			body.put("system", system);
		}

		// messages
		body.put("messages", buildMessages(request.messages()));

		// inferenceConfig: maxTokens always; temperature/topP only if present
		body.put("inferenceConfig", buildInferenceConfig(request));

		// toolConfig: only when tools are defined
		if (!request.tools().isEmpty()) {
			body.put("toolConfig", buildToolConfig(request));
		}

		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			JsonUtil.writeJsonValue(json, body);
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize Bedrock Converse request JSON", e);
		}
		return buffer.toString();
	}

	// ---- Private helpers ----

	private static List<Object> buildMessages(List<Message> messages) {
		List<Object> result = new ArrayList<>();
		int i = 0;
		while (i < messages.size()) {
			Message msg = messages.get(i);

			if (msg instanceof UserMessage user) {
				result.add(buildUserMessage(user));
				i++;
			} else if (msg instanceof AssistantMessage assistant) {
				result.add(buildAssistantMessage(assistant));
				i++;
			} else if (msg instanceof ToolResultMessage) {
				// Consecutive ToolResultMessages are batched into a single "user" message with
				// multiple toolResult content blocks — matching the Anthropic builder's behaviour.
				// Bedrock Converse accepts any number of toolResult blocks in one user message,
				// so batching is both correct and keeps the conversation structure compact.
				List<Object> content = new ArrayList<>();
				while (i < messages.size() && messages.get(i) instanceof ToolResultMessage tr) {
					content.add(buildToolResultBlock(tr));
					i++;
				}
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("role", "user");
				entry.put("content", content);
				result.add(entry);
			} else {
				// Skip CompactionMarker, SystemMessage, and any other non-conversation message.
				i++;
			}
		}
		return result;
	}

	private static Map<String, Object> buildUserMessage(UserMessage user) {
		List<Object> content = new ArrayList<>();
		for (ContentPart part : user.content()) {
			if (part instanceof TextContent text) {
				Map<String, Object> block = new LinkedHashMap<>();
				block.put("text", text.text());
				content.add(block);
			} else if (part instanceof ImageContent) {
				// TODO image support: Bedrock Converse supports {"image":{"format":..,"source":{"bytes":..}}}
				// but ImageContent only exposes a URL/data-URI, which requires non-trivial
				// base64 extraction. Skipping for now.
				// NOTE: a UserMessage that contains ONLY image parts will produce an empty
				// content array here, which Bedrock Converse rejects with HTTP 400. Until image
				// support is implemented, callers must ensure user messages contain at least one
				// text part.
			}
			// Other content types (PdfContent, etc.) are not supported by Bedrock Converse.
		}
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("role", "user");
		entry.put("content", content);
		return entry;
	}

	private static Map<String, Object> buildAssistantMessage(AssistantMessage assistant) {
		List<Object> content = new ArrayList<>();
		for (ContentPart part : assistant.content()) {
			if (part instanceof TextContent text) {
				Map<String, Object> block = new LinkedHashMap<>();
				block.put("text", text.text());
				content.add(block);
			} else if (part instanceof ToolCall toolCall) {
				String args = toolCall.arguments();
				Map<String, Object> toolUse = new LinkedHashMap<>();
				toolUse.put("toolUseId", toolCall.id());
				toolUse.put("name", toolCall.name());
				toolUse.put("input", parseJson(args.isEmpty() ? "{}" : args));

				Map<String, Object> block = new LinkedHashMap<>();
				block.put("toolUse", toolUse);
				content.add(block);
			}
			// ThinkingContent has no Bedrock Converse equivalent — skip.
		}
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("role", "assistant");
		entry.put("content", content);
		return entry;
	}

	private static Map<String, Object> buildToolResultBlock(ToolResultMessage result) {
		ToolState state = result.state();
		String text;
		String status;
		if (state instanceof ToolState.Completed c) {
			text = c.result();
			status = "success";
		} else if (state instanceof ToolState.Failed f) {
			text = f.error();
			status = "error";
		} else {
			text = "";
			status = "success";
		}

		List<Object> resultContent = new ArrayList<>();
		Map<String, Object> textBlock = new LinkedHashMap<>();
		textBlock.put("text", text);
		resultContent.add(textBlock);

		Map<String, Object> toolResult = new LinkedHashMap<>();
		toolResult.put("toolUseId", result.toolCallId());
		toolResult.put("content", resultContent);
		toolResult.put("status", status);

		Map<String, Object> block = new LinkedHashMap<>();
		block.put("toolResult", toolResult);
		return block;
	}

	private static Map<String, Object> buildInferenceConfig(ProviderRequest request) {
		Map<String, Object> cfg = new LinkedHashMap<>();
		cfg.put("maxTokens", request.maxOutputTokens());

		Map<String, Object> opts = request.modelOptions();
		Object temperature = opts.get("temperature");
		if (temperature != null) {
			cfg.put("temperature", Double.parseDouble(temperature.toString()));
		}
		Object topP = opts.get("topP");
		if (topP != null) {
			cfg.put("topP", Double.parseDouble(topP.toString()));
		}
		return cfg;
	}

	private static Map<String, Object> buildToolConfig(ProviderRequest request) {
		List<Object> tools = new ArrayList<>();
		for (ToolSchema schema : request.tools()) {
			Map<String, Object> inputSchema = new LinkedHashMap<>();
			inputSchema.put("json", parseJson(schema.inputSchemaJson()));

			Map<String, Object> toolSpec = new LinkedHashMap<>();
			toolSpec.put("name", schema.name());
			toolSpec.put("description", schema.description());
			toolSpec.put("inputSchema", inputSchema);

			Map<String, Object> toolEntry = new LinkedHashMap<>();
			toolEntry.put("toolSpec", toolSpec);
			tools.add(toolEntry);
		}

		Map<String, Object> toolConfig = new LinkedHashMap<>();
		toolConfig.put("tools", tools);

		// toolChoice mapping:
		//   AUTO     → {"auto":{}}
		//   REQUIRED → {"any":{}}
		//   NONE     → omit (Bedrock Converse has no "none" choice)
		//   null     → omit
		ToolChoice choice = request.toolChoice();
		if (choice == ToolChoice.AUTO) {
			toolConfig.put("toolChoice", Map.of("auto", Map.of()));
		} else if (choice == ToolChoice.REQUIRED) {
			toolConfig.put("toolChoice", Map.of("any", Map.of()));
		}
		// NONE and null: toolChoice omitted

		return toolConfig;
	}
}
