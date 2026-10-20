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
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.DataUris;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
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

	/**
	 * The {@code name} of every {@code document} block.
	 *
	 * <p>Required by the API, restricted to alphanumerics, whitespace, hyphens, parentheses and
	 * square brackets, and — per the AWS documentation — read by the model, hence a neutral
	 * constant rather than a caller-supplied file name.</p>
	 */
	private static final String DOCUMENT_NAME = "attachment";

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
	public static String buildRequestJson(ProviderRequest request) throws ProviderException {
		refuseResponseFormat(request.responseFormat());

		Map<String, Object> body = new LinkedHashMap<>();

		// system: array of text blocks (omitted when empty/null)
		if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
			List<Object> system = new ArrayList<>();
			Map<String, Object> sysBlock = new LinkedHashMap<>();
			sysBlock.put("text", request.systemPrompt());
			system.add(sysBlock);
			// Cache breakpoint after the system prompt. A cachePoint in the system field caches the
			// preceding tools and system prefix — the large static portion of an agent request that
			// is resent unchanged every turn. Mirrors the Anthropic builder's system cache_control.
			system.add(cachePoint());
			body.put("system", system);
		}

		// messages (with cache breakpoints on the most recent turns)
		List<Object> messages = buildMessages(request.messages());
		addConversationCachePoints(messages);
		body.put("messages", messages);

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

	/**
	 * Refuses a request the Converse API cannot express.
	 *
	 * <p>
	 * The Converse API has no {@code response_format}: a model is steered to JSON through a tool
	 * whose input schema is the wanted shape, which is a different request, not a field. Saying so
	 * is the point - an ignored response format reaches the caller as prose that looked like it was
	 * going to be JSON.
	 * </p>
	 */
	private static void refuseResponseFormat(ResponseFormat responseFormat) throws ProviderException {
		if (responseFormat == null || responseFormat.kind() == ResponseFormat.Kind.TEXT) {
			return;
		}
		throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 0,
			"The Bedrock Converse API cannot be asked for a JSON response format."
				+ " Use a provider that supports structured output, or give the model a tool whose"
				+ " input schema is the wanted shape.");
	}

	/**
	 * A Bedrock Converse {@code cachePoint} block: {@code {"cachePoint": {"type": "default"}}}.
	 *
	 * <p>Marks a prompt-cache breakpoint; everything from the start of the cacheable prefix up to
	 * this point is cached for reuse on subsequent requests. Segments below the model's minimum
	 * cacheable token count are silently ignored by Bedrock, so adding the marker unconditionally
	 * is safe for short prompts.</p>
	 */
	private static Map<String, Object> cachePoint() {
		Map<String, Object> block = new LinkedHashMap<>();
		block.put("cachePoint", Map.of("type", "default"));
		return block;
	}

	/**
	 * Appends a {@link #cachePoint()} to the content of the last up to two user-role messages.
	 *
	 * <p>Mirrors the Anthropic builder, which caches the two most recent user turns so that a
	 * growing agent loop gets cache hits on the conversation prefix turn after turn. Combined with
	 * the system cache point this stays within Bedrock's four-breakpoint limit.</p>
	 */
	private static void addConversationCachePoints(List<Object> messages) {
		List<Map<String, Object>> userTurns = new ArrayList<>();
		for (Object entry : messages) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) entry;
			if ("user".equals(map.get("role"))) {
				userTurns.add(map);
			}
		}
		int from = Math.max(0, userTurns.size() - 2);
		for (int i = from; i < userTurns.size(); i++) {
			@SuppressWarnings("unchecked")
			List<Object> content = (List<Object>) userTurns.get(i).get("content");
			if (content != null && !content.isEmpty()) {
				content.add(cachePoint());
			}
		}
	}

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
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("role", "user");
		entry.put("content", buildContentBlocks(user.content()));
		return entry;
	}

	/**
	 * Maps content parts to Converse content blocks.
	 *
	 * <p>Text, images and PDFs all have a first-class block; {@code ThinkingContent} and
	 * {@code ToolCall} have no equivalent in a user turn and are skipped.</p>
	 */
	private static List<Object> buildContentBlocks(List<ContentPart> parts) {
		List<Object> content = new ArrayList<>(parts.size());
		for (ContentPart part : parts) {
			if (part instanceof TextContent text) {
				Map<String, Object> block = new LinkedHashMap<>();
				block.put("text", text.text());
				content.add(block);
			} else if (part instanceof ImageContent image) {
				content.add(buildImageBlock(image));
			} else if (part instanceof PdfContent pdf) {
				content.add(buildDocumentBlock(pdf));
			}
		}
		return content;
	}

	/**
	 * Builds a Bedrock Converse {@code image} content block from an {@link ImageContent}.
	 *
	 * <p>Bedrock expects: {@code {"image": {"format": "png", "source": {"bytes": "<base64>"}}}}.</p>
	 */
	private static Map<String, Object> buildImageBlock(ImageContent image) {
		String base64Data;
		if (image.url().startsWith("data:")) {
			base64Data = DataUris.base64Payload(image.url());
		} else {
			// Bedrock Converse requires raw bytes — URLs are not supported.
			// Fall back to a text placeholder rather than silently dropping the image.
			Map<String, Object> block = new LinkedHashMap<>();
			block.put("text", "[Image URL not supported by Bedrock Converse: " + image.url() + "]");
			return block;
		}

		String format = extractImageFormat(image.mediaType());

		Map<String, Object> source = new LinkedHashMap<>();
		source.put("bytes", base64Data);

		Map<String, Object> imageBlock = new LinkedHashMap<>();
		imageBlock.put("format", format);
		imageBlock.put("source", source);

		Map<String, Object> block = new LinkedHashMap<>();
		block.put("image", imageBlock);
		return block;
	}

	/**
	 * Builds a Bedrock Converse {@code document} content block from a {@link PdfContent}.
	 *
	 * <p>Bedrock expects:
	 * {@code {"document": {"name": "attachment", "format": "pdf", "source": {"bytes": "<base64>"}}}}.
	 * The name is required; it is deliberately a neutral constant, because AWS documents document
	 * names as a prompt-injection vector.</p>
	 */
	private static Map<String, Object> buildDocumentBlock(PdfContent pdf) {
		Map<String, Object> source = new LinkedHashMap<>();
		source.put("bytes", pdf.data());

		Map<String, Object> document = new LinkedHashMap<>();
		document.put("name", DOCUMENT_NAME);
		document.put("format", "pdf");
		document.put("source", source);

		Map<String, Object> block = new LinkedHashMap<>();
		block.put("document", document);
		return block;
	}

	/**
	 * Extracts the Bedrock image format string from a MIME type.
	 *
	 * <p>
	 * The subtype of the {@link ImageContent#normalizeMediaType(String) canonical} media type: the
	 * {@code image/jpg} spelling Bedrock does not accept is resolved by that shared rule, not by a
	 * copy of it here, so this builder and the paths that decide what may be built agree on what a
	 * type is. Whatever an unexpected type would produce is still sent as the format string: a
	 * payload that got this far was built past the checks, and guessing on its behalf here would only
	 * move the provider error somewhere less obvious.
	 * </p>
	 *
	 * @return one of {@code png}, {@code jpeg}, {@code gif}, {@code webp}
	 */
	private static String extractImageFormat(String mediaType) {
		String normalized = ImageContent.normalizeMediaType(mediaType);
		int slashIdx = normalized.indexOf('/');
		return slashIdx >= 0 ? normalized.substring(slashIdx + 1) : normalized;
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

	/**
	 * Builds a {@code toolResult} block.
	 *
	 * <p>Its {@code content} is already a block list in this API, so a tool result carrying an image
	 * or a PDF simply contributes {@code image}/{@code document} blocks next to the text — no extra
	 * user message, which Converse would reject anyway because it enforces strict user/assistant
	 * alternation and tool results already travel in a user turn.</p>
	 */
	private static Map<String, Object> buildToolResultBlock(ToolResultMessage result) {
		ToolState state = result.state();
		String text;
		String status;
		List<ContentPart> content = List.of();
		if (state instanceof ToolState.Completed c) {
			text = c.result();
			status = "success";
			content = c.content();
		} else if (state instanceof ToolState.Failed f) {
			text = f.error();
			status = "error";
		} else {
			text = "";
			status = "success";
		}

		List<Object> resultContent;
		if (content.isEmpty()) {
			resultContent = new ArrayList<>();
			Map<String, Object> textBlock = new LinkedHashMap<>();
			textBlock.put("text", text);
			resultContent.add(textBlock);
		} else {
			resultContent = buildContentBlocks(content);
		}

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

	/**
	 * The {@code toolConfig}, with a cache breakpoint where the tool array stops being the same
	 * bytes.
	 *
	 * <p>
	 * Same reasoning as the Anthropic builder's, in Bedrock's spelling: the tools are rendered in
	 * front of the system prompt, so the {@code cachePoint} in {@code system} covers them all, and a
	 * skill loaded mid-conversation moves everything behind the tool it appended. A
	 * {@code cachePoint} between the agent's own tools and a run's loaded skills keeps the first
	 * part readable across that load. Only placed when there is something behind it - see #346.
	 * </p>
	 */
	private static Map<String, Object> buildToolConfig(ProviderRequest request) {
		List<ToolSchema> schemas = request.tools();
		int stable = request.stableToolCount();
		boolean breakAfterStable = stable > 0 && stable < schemas.size();

		List<Object> tools = new ArrayList<>();
		for (int i = 0; i < schemas.size(); i++) {
			ToolSchema schema = schemas.get(i);
			Map<String, Object> inputSchema = new LinkedHashMap<>();
			inputSchema.put("json", parseJson(schema.inputSchemaJson()));

			Map<String, Object> toolSpec = new LinkedHashMap<>();
			toolSpec.put("name", schema.name());
			toolSpec.put("description", schema.description());
			toolSpec.put("inputSchema", inputSchema);

			Map<String, Object> toolEntry = new LinkedHashMap<>();
			toolEntry.put("toolSpec", toolSpec);
			tools.add(toolEntry);
			if (breakAfterStable && i == stable - 1) {
				tools.add(cachePoint());
			}
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
