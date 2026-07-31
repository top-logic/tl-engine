/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

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
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Maps a {@link ProviderRequest} to an {@link OpenAIResponsesRequest} proto DTO and serializes it.
 */
public final class OpenAIResponsesRequestBuilder {

	private OpenAIResponsesRequestBuilder() {
		// Utility class.
	}

	/**
	 * Serializes the given {@link ProviderRequest} to an <code>OpenAI</code> Responses API JSON request body.
	 */
	public static String buildRequestJson(ProviderRequest request) throws ProviderException {
		OpenAIResponsesRequest req = OpenAIResponsesRequest.create()
			.setModel(request.model())
			.setMaxOutputTokens(request.maxOutputTokens())
			.setStream(true)
			.setStore(true)
			.setInput(buildInput(request));

		if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
			req.setInstructions(request.systemPrompt());
		}

		Map<String, Object> modelOptions = request.modelOptions();
		if (modelOptions.get("reasoning_effort") != null || modelOptions.get("reasoning_summary") != null) {
			Map<String, Object> reasoning = new LinkedHashMap<>();
			Object effort = modelOptions.get("reasoning_effort");
			if (effort != null) {
				reasoning.put("effort", effort.toString());
			}
			Object summary = modelOptions.get("reasoning_summary");
			if (summary != null) {
				reasoning.put("summary", summary.toString());
			}
			req.setReasoning(reasoning);
		}

		if (!request.tools().isEmpty()) {
			List<Object> tools = new ArrayList<>();
			for (ToolSchema tool : request.tools()) {
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("type", "function");
				entry.put("name", tool.name());
				entry.put("description", tool.description());
				entry.put("parameters", parseJson(tool.inputSchemaJson()));
				tools.add(entry);
			}
			req.setTools(tools);
		}

		if (request.responseFormat() == ResponseFormat.JSON) {
			req.setText(Map.of("format", Map.of("type", "json_object")));
		}

		if (request.toolChoice() != null) {
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
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Failed to serialize request JSON", e);
		}
		return buffer.toString();
	}

	// --- Input building ---

	private static List<Object> buildInput(ProviderRequest request) {
		List<Object> items = new ArrayList<>();
		for (Message msg : request.messages()) {
			if (msg instanceof UserMessage user) {
				items.add(buildUserItem(user));
			} else if (msg instanceof AssistantMessage assistant) {
				buildAssistantItems(items, assistant);
			} else if (msg instanceof ToolResultMessage result) {
				items.add(buildToolResultItem(result));
			}
		}
		return items;
	}

	private static Map<String, Object> buildUserItem(UserMessage user) {
		List<Object> content = buildContentParts(user.content());
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("role", "user");
		item.put("content", content);
		return item;
	}

	private static void buildAssistantItems(List<Object> items, AssistantMessage assistant) {
		for (ContentPart part : assistant.content()) {
			if (part instanceof TextContent text) {
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("type", "message");
				item.put("role", "assistant");
				item.put("content", List.of(Map.of("type", "output_text", "text", text.text())));
				items.add(item);
			} else if (part instanceof ToolCall toolCall) {
				String args = toolCall.arguments();
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("type", "function_call");
				item.put("call_id", toolCall.id());
				item.put("name", toolCall.name());
				item.put("arguments", args.isEmpty() ? "{}" : args);
				items.add(item);
			} else if (part instanceof ThinkingContent thinking) {
				if (thinking.continuityToken() == null) {
					continue;
				}
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("type", "reasoning");
				item.put("encrypted_content", thinking.continuityToken());
				item.put("summary", List.of(Map.of("type", "summary_text", "text", thinking.text())));
				items.add(item);
			}
		}
	}

	/**
	 * Builds a {@code function_call_output} item.
	 *
	 * <p>Its {@code output} is a string for a text-only result, and an array of
	 * {@code input_text}/{@code input_image}/{@code input_file} parts for a result the tool filled
	 * with content parts — the same part vocabulary the Responses API takes in a user message, which
	 * is why {@link #buildContentPart} serves both.</p>
	 */
	private static Map<String, Object> buildToolResultItem(ToolResultMessage result) {
		ToolState state = result.state();
		Object output;
		if (state instanceof ToolState.Completed c) {
			output = c.content().isEmpty() ? c.result() : buildContentParts(c.content());
		} else if (state instanceof ToolState.Failed f) {
			output = f.error();
		} else {
			output = "";
		}
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("type", "function_call_output");
		item.put("call_id", result.toolCallId());
		item.put("output", output);
		return item;
	}

	private static List<Object> buildContentParts(List<ContentPart> parts) {
		List<Object> content = new ArrayList<>(parts.size());
		for (ContentPart part : parts) {
			content.add(buildContentPart(part));
		}
		return content;
	}

	private static Map<String, Object> buildContentPart(ContentPart part) {
		Map<String, Object> block = new LinkedHashMap<>();
		if (part instanceof TextContent text) {
			block.put("type", "input_text");
			block.put("text", text.text());
		} else if (part instanceof ImageContent image) {
			block.put("type", "input_image");
			block.put("image_url", image.url());
		} else if (part instanceof PdfContent pdf) {
			block.put("type", "input_file");
			block.put("filename", "document.pdf");
			block.put("file_data", "data:" + pdf.mediaType() + ";base64," + pdf.data());
		}
		return block;
	}
}
