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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.DataUris;
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
		writeThinkingParams(req, request.model(), request.maxOutputTokens(), request.modelOptions());

		if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
			req.setSystem(List.of(buildSystemBlock(request.systemPrompt(), cacheTtl)));
		}

		req.setMessages(buildMessages(messages, cacheFrom, cacheTtl));

		if (!request.tools().isEmpty()) {
			req.setTools(buildTools(request, cacheTtl));
		}

		writeResponseFormat(req, request.responseFormat());

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

	/**
	 * The tool definitions, with a cache breakpoint where the array stops being the same bytes.
	 *
	 * <p>
	 * Anthropic renders a request as tools, then system, then messages, and a breakpoint caches
	 * everything in front of it. The breakpoint on the system block therefore covers the whole tool
	 * array, which is right until a run loads a skill: one appended tool moves the system block and
	 * the entire prefix is written again (Gitea #346).
	 * </p>
	 *
	 * <p>
	 * A second breakpoint on the last tool that <em>cannot</em> move keeps that part readable across
	 * the load. It is only worth a breakpoint when there is something behind it - with no loaded
	 * skill the system breakpoint already covers the array, and the four we may place are better
	 * spent on the conversation.
	 * </p>
	 */
	private static List<Object> buildTools(ProviderRequest request, String cacheTtl) {
		List<ToolSchema> schemas = request.tools();
		int stable = request.stableToolCount();
		boolean breakAfterStable = stable > 0 && stable < schemas.size();

		List<Object> tools = new ArrayList<>();
		for (int i = 0; i < schemas.size(); i++) {
			ToolSchema tool = schemas.get(i);
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("name", tool.name());
			entry.put("description", tool.description());
			entry.put("input_schema", parseJson(tool.inputSchemaJson()));
			if (breakAfterStable && i == stable - 1) {
				entry.put("cache_control", buildCacheControl(cacheTtl));
			}
			tools.add(entry);
		}
		return tools;
	}

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
			} else if (part instanceof ThinkingContent thinking) {
				Map<String, Object> block = new LinkedHashMap<>();
				block.put("type", "thinking");
				block.put("thinking", thinking.text());
				if (thinking.continuityToken() != null) {
					block.put("signature", thinking.continuityToken());
				}
				content.add(block);
			}
		}
		return content;
	}

	/**
	 * Builds a {@code tool_result} block.
	 *
	 * <p>Its {@code content} is a plain string for a text-only result, and a list of
	 * {@code text}/{@code image}/{@code document} blocks for a result the tool filled with content
	 * parts — the Messages API permits exactly those types inside a tool result, so an image needs no
	 * detour through a user message.</p>
	 */
	private static Map<String, Object> buildToolResult(ToolResultMessage result, boolean cache,
			String cacheTtl) {
		ToolState state = result.state();
		Object content;
		boolean isError = false;
		if (state instanceof ToolState.Completed c) {
			content = c.content().isEmpty()
				? c.result()
				: buildToolResultContent(c.content(), cacheTtl);
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

	/**
	 * The nested content blocks of a multimodal {@code tool_result}.
	 *
	 * <p>The cache breakpoint stays on the enclosing {@code tool_result} block, so none of the nested
	 * blocks carries one.</p>
	 */
	private static List<Object> buildToolResultContent(List<ContentPart> parts, String cacheTtl) {
		List<Object> content = new ArrayList<>(parts.size());
		for (ContentPart part : parts) {
			content.add(buildContentBlock(part, false, cacheTtl));
		}
		return content;
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
			source.put("type", "base64");
			source.put("media_type", image.mediaType());
			source.put("data", DataUris.base64Payload(image.url()));
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

	/**
	 * Writes the requested {@link ResponseFormat} as Anthropic's structured output.
	 *
	 * <p>
	 * Anthropic states the constraint as {@code output_config.format} with a {@code json_schema}.
	 * There is no untyped JSON mode - a schema is what the API takes - so a request that asks for
	 * JSON without saying which JSON is refused here rather than sent and rejected, or worse, sent
	 * and answered in prose.
	 * </p>
	 */
	private static void writeResponseFormat(AnthropicRequest req, ResponseFormat format)
			throws ProviderException {
		if (format == null || format.kind() == ResponseFormat.Kind.TEXT) {
			return;
		}
		if (format.kind() == ResponseFormat.Kind.JSON) {
			throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 0,
				"Anthropic has no untyped JSON mode. Pass a JSON schema as the response format"
					+ " (a map with a 'schema') to get a structured answer.");
		}
		Map<String, Object> jsonSchema = new LinkedHashMap<>();
		jsonSchema.put("type", "json_schema");
		jsonSchema.put("schema", format.schema());
		putOutputConfig(req, "format", jsonSchema);
	}

	/**
	 * Adds one entry to the request's {@code output_config}, keeping what is already there.
	 *
	 * <p>
	 * Both the thinking effort and the structured output live in that one object, and they are
	 * written by different code paths.
	 * </p>
	 */
	private static void putOutputConfig(AnthropicRequest req, String key, Object value) {
		Map<String, Object> outputConfig = new LinkedHashMap<>();
		if (req.getOutputConfig() instanceof Map<?, ?> existing) {
			existing.forEach((k, v) -> outputConfig.put(String.valueOf(k), v));
		}
		outputConfig.put(key, value);
		req.setOutputConfig(outputConfig);
	}

	/**
	 * Writes the thinking configuration for the requested effort.
	 *
	 * <p>
	 * Adaptive thinking asks for the {@code summarized} display. On the models since Opus 4.7 the
	 * API default is {@code omitted}: the thinking blocks stream with empty text, so a model that
	 * thinks for minutes shows nothing at all until its first tool call (Gitea #602 measured 234 s
	 * of silence on Opus 5.5). The summary costs nothing extra - thinking is billed the same under
	 * either display - and it is what the chat shows while the model works.
	 * </p>
	 *
	 * <p>
	 * Without an effort, a model that {@link #thinksUnasked(String) thinks unasked} still gets the
	 * adaptive configuration: for it, adaptive is what omitting the parameter means anyway, so the
	 * request only changes what is shown. Every other model is left without thinking, as before.
	 * </p>
	 */
	private static void writeThinkingParams(AnthropicRequest req, String model, int maxOutputTokens,
			Map<String, Object> modelOptions) {
		Object thinkingEffort = modelOptions.get("thinking_effort");
		if (thinkingEffort == null) {
			if (thinksUnasked(model)) {
				req.setThinking(adaptiveThinking());
			}
			return;
		}
		if (requiresBudgetTokens(model)) {
			int budgetTokens = effortToBudgetTokens(thinkingEffort.toString(), maxOutputTokens);
			req.setThinking(Map.of("type", "enabled", "budget_tokens", budgetTokens));
		} else {
			req.setThinking(adaptiveThinking());
			putOutputConfig(req, "effort", thinkingEffort.toString());
		}
	}

	private static Map<String, Object> adaptiveThinking() {
		Map<String, Object> thinking = new LinkedHashMap<>();
		thinking.put("type", "adaptive");
		thinking.put("display", "summarized");
		return thinking;
	}

	/**
	 * Whether the model thinks adaptively when the request says nothing about thinking.
	 *
	 * <p>
	 * True for the generation-5 models and later (Opus 5.x, Sonnet 5.x) and for Fable and Mythos;
	 * on Opus 5.5 and Fable thinking cannot even be switched off. The 4.x models do not think
	 * unless asked.
	 * </p>
	 */
	static boolean thinksUnasked(String model) {
		if (model.contains("fable") || model.contains("mythos")) {
			return true;
		}
		Matcher generation = MODEL_GENERATION.matcher(model);
		return generation.find() && Integer.parseInt(generation.group(1)) >= 5;
	}

	/** The major version of an Opus or Sonnet model name, e.g. {@code 5} in {@code claude-opus-5-5}. */
	private static final Pattern MODEL_GENERATION = Pattern.compile("(?:opus|sonnet)-(\\d+)(?:\\D|$)");

	private static boolean requiresBudgetTokens(String model) {
		return model.contains("haiku");
	}

	private static int effortToBudgetTokens(String effort, int maxOutputTokens) {
		double fraction = switch (effort) {
			case "low" -> 0.25;
			case "medium" -> 0.5;
			case "high" -> 0.75;
			case "max" -> 1.0;
			default -> 0.5;
		};
		return Math.max(1024, (int) (maxOutputTokens * fraction));
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
