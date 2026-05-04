/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import static com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsRequestBuilder.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Maps a {@link ProviderRequest} to an {@link AnthropicRequest} proto DTO and serializes it.
 */
public final class AnthropicRequestBuilder {

	private AnthropicRequestBuilder() {
		// Static utility class.
	}

	/**
	 * Builds the Anthropic Messages API request JSON from a {@link ProviderRequest}.
	 */
	public static String buildRequestJson(ProviderRequest request) throws ProviderException {
		String cacheTtl = (String) request.modelOptions().get("cacheTtl");

		List<Message> messages = request.messages();
		int userRoleCount = countUserRolePositions(messages);
		int cacheFrom = userRoleCount - Math.min(2, userRoleCount);

		AnthropicRequest req = AnthropicRequest.create()
			.setModel(request.model())
			.setMaxTokens(request.maxOutputTokens())
			.setStream(true);

		writeSamplingParams(req, request.modelOptions());
		writeThinkingParams(req, request.modelOptions());

		if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
			req.setSystem(List.of(buildSystemBlock(request.systemPrompt(), cacheTtl)));
		}

		req.setMessages(buildMessages(messages, cacheFrom, cacheTtl));

		if (!request.tools().isEmpty()) {
			List<Object> tools = new ArrayList<>();
			for (ToolSchema tool : request.tools()) {
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("name", tool.name());
				entry.put("description", tool.description());
				entry.put("input_schema", parseJson(tool.inputSchemaJson()));
				tools.add(entry);
			}
			req.setTools(tools);
		}

		if (request.responseFormat() == ResponseFormat.JSON) {
			req.setResponseFormat(Map.of("type", "json_object"));
		}

		if (request.toolChoice() != null && request.toolChoice() != ToolChoice.AUTO) {
			String choiceType = request.toolChoice() == ToolChoice.REQUIRED ? "any" : "none";
			req.setToolChoice(Map.of("type", choiceType));
		}

		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			req.writeTo(json);
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Failed to serialize Anthropic request JSON", e);
		}
		return buffer.toString();
	}

	// ---- Private helpers ----

	private static int countUserRolePositions(List<Message> messages) {
		int count = 0;
		for (int i = 0; i < messages.size(); i++) {
			Message msg = messages.get(i);
			if (msg instanceof UserMessage) {
				count++;
			} else if (msg instanceof ToolResultMessage) {
				if (i == 0 || !(messages.get(i - 1) instanceof ToolResultMessage)) {
					count++;
				}
			}
		}
		return count;
	}

	private static List<Object> buildMessages(List<Message> messages, int cacheFrom,
			String cacheTtl) {
		List<Object> result = new ArrayList<>();
		int userRoleOrdinal = 0;
		int i = 0;
		while (i < messages.size()) {
			Message msg = messages.get(i);

			if (msg instanceof SystemMessage || msg instanceof CompactionMarker) {
				i++;
			} else if (msg instanceof UserMessage user) {
				boolean cache = userRoleOrdinal >= cacheFrom;
				userRoleOrdinal++;
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("role", "user");
				entry.put("content", buildUserContent(user.content(), cache, cacheTtl));
				result.add(entry);
				i++;
			} else if (msg instanceof AssistantMessage assistant) {
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("role", "assistant");
				entry.put("content", buildAssistantContent(assistant.content()));
				result.add(entry);
				i++;
			} else if (msg instanceof ToolResultMessage) {
				boolean cache = userRoleOrdinal >= cacheFrom;
				userRoleOrdinal++;
				List<Object> content = new ArrayList<>();
				while (i < messages.size() && messages.get(i) instanceof ToolResultMessage tr) {
					boolean lastInGroup = (i + 1 >= messages.size()
						|| !(messages.get(i + 1) instanceof ToolResultMessage));
					content.add(buildToolResult(tr, cache && lastInGroup, cacheTtl));
					i++;
				}
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("role", "user");
				entry.put("content", content);
				result.add(entry);
			}
		}
		return result;
	}

	private static List<Object> buildUserContent(List<ContentPart> parts, boolean cache,
			String cacheTtl) {
		List<Object> content = new ArrayList<>();
		for (int i = 0; i < parts.size(); i++) {
			boolean cacheOnThis = cache && (i == parts.size() - 1);
			content.add(buildContentBlock(parts.get(i), cacheOnThis, cacheTtl));
		}
		return content;
	}

	private static List<Object> buildAssistantContent(List<ContentPart> parts) {
		List<Object> content = new ArrayList<>();
		for (ContentPart part : parts) {
			if (part instanceof TextContent text) {
				content.add(Map.of("type", "text", "text", text.text()));
			} else if (part instanceof ToolCall toolCall) {
				String args = toolCall.arguments();
				Map<String, Object> block = new LinkedHashMap<>();
				block.put("type", "tool_use");
				block.put("id", toolCall.id());
				block.put("name", toolCall.name());
				block.put("input", parseJson(args.isEmpty() ? "{}" : args));
				content.add(block);
			}
		}
		return content;
	}

	private static Map<String, Object> buildToolResult(ToolResultMessage result, boolean cache,
			String cacheTtl) {
		ToolState state = result.state();
		String content;
		boolean isError = false;
		if (state instanceof ToolState.Completed c) {
			content = c.result();
		} else if (state instanceof ToolState.Failed f) {
			content = f.error();
			isError = true;
		} else {
			content = "";
		}

		Map<String, Object> block = new LinkedHashMap<>();
		block.put("type", "tool_result");
		block.put("tool_use_id", result.toolCallId());
		block.put("content", content);
		if (isError) {
			block.put("is_error", true);
		}
		if (cache) {
			block.put("cache_control", buildCacheControl(cacheTtl));
		}
		return block;
	}

	private static Map<String, Object> buildContentBlock(ContentPart part, boolean cacheOnThis,
			String cacheTtl) {
		Map<String, Object> block = new LinkedHashMap<>();
		if (part instanceof TextContent text) {
			block.put("type", "text");
			block.put("text", text.text());
		} else if (part instanceof ImageContent image) {
			block.put("type", "image");
			block.put("source", buildImageSource(image));
		} else if (part instanceof PdfContent pdf) {
			block.put("type", "document");
			Map<String, Object> source = new LinkedHashMap<>();
			source.put("type", "base64");
			source.put("media_type", pdf.mediaType());
			source.put("data", pdf.data());
			block.put("source", source);
		}
		if (cacheOnThis) {
			block.put("cache_control", buildCacheControl(cacheTtl));
		}
		return block;
	}

	private static Map<String, Object> buildImageSource(ImageContent image) {
		Map<String, Object> source = new LinkedHashMap<>();
		if (image.url().startsWith("data:")) {
			String dataUri = image.url();
			int commaIdx = dataUri.indexOf(',');
			String base64Data = commaIdx >= 0 ? dataUri.substring(commaIdx + 1) : dataUri;
			source.put("type", "base64");
			source.put("media_type", image.mediaType());
			source.put("data", base64Data);
		} else {
			source.put("type", "url");
			source.put("url", image.url());
		}
		return source;
	}

	private static Map<String, Object> buildSystemBlock(String systemPrompt, String cacheTtl) {
		Map<String, Object> block = new LinkedHashMap<>();
		block.put("type", "text");
		block.put("text", systemPrompt);
		block.put("cache_control", buildCacheControl(cacheTtl));
		return block;
	}

	private static Map<String, Object> buildCacheControl(String cacheTtl) {
		Map<String, Object> cc = new LinkedHashMap<>();
		cc.put("type", "ephemeral");
		if (cacheTtl != null && !cacheTtl.isEmpty()) {
			cc.put("ttl", cacheTtl);
		}
		return cc;
	}

	private static void writeThinkingParams(AnthropicRequest req, Map<String, Object> modelOptions) {
		Object thinkingEffort = modelOptions.get("thinking_effort");
		if (thinkingEffort != null) {
			req.setThinking(Map.of("type", "adaptive"));
			req.setOutputConfig(Map.of("effort", thinkingEffort.toString()));
		}
	}

	private static void writeSamplingParams(AnthropicRequest req, Map<String, Object> modelOptions) {
		Object temperature = modelOptions.get("temperature");
		if (temperature != null) {
			req.setTemperature(Double.parseDouble(temperature.toString()));
		}
		Object topP = modelOptions.get("topP");
		if (topP != null) {
			req.setTopP(Double.parseDouble(topP.toString()));
		}
		Object topK = modelOptions.get("top_k");
		if (topK != null) {
			req.setTopK(Long.parseLong(topK.toString()));
		}
	}
}
