/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.mistral;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.provider.SSEReader;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * {@link LLMProvider} for the Mistral Chat Completions API.
 *
 * <p>Uses {@link HttpClient} for HTTP and msgbuf {@link JsonWriter}/{@link JsonReader} for JSON.
 * Thread-safe: a single instance handles concurrent streams.</p>
 */
public class MistralProvider implements LLMProvider {

	private static final String DEFAULT_BASE_URL = "https://api.mistral.ai";

	private final HttpClient _httpClient;

	private final String _apiKey;

	private final String _baseUrl;

	/** */
	public MistralProvider(String apiKey) {
		this(apiKey, DEFAULT_BASE_URL);
	}

	/** */
	public MistralProvider(String apiKey, String baseUrl) {
		this(apiKey, baseUrl, defaultHttpClient());
	}

	/** */
	public MistralProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		_apiKey = apiKey;
		_baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		_httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null.");
	}

	/**
	 * Undocumented Mistral API limit: request bodies above ~32 KB cause the connection to hang
	 * indefinitely with no response. Confirmed via curl — this is a server-side infrastructure
	 * issue (likely a reverse proxy), not a client bug. Guard with a conservative threshold.
	 */
	private static final int MAX_REQUEST_BODY_BYTES = 30_000;

	private static HttpClient defaultHttpClient() {
		return HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(30))
			.build();
	}

	@Override
	public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
		Objects.requireNonNull(request, "Request must not be null.");
		Objects.requireNonNull(handler, "Handler must not be null.");

		String body = buildRequestJson(request);

		int bodyBytes = body.getBytes(StandardCharsets.UTF_8).length;
		if (bodyBytes > MAX_REQUEST_BODY_BYTES) {
			throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 0,
				"Request body (" + bodyBytes + " bytes) exceeds Mistral's ~32 KB limit. "
					+ "Reduce the system prompt or number of tools, or use a different provider.");
		}

		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create(_baseUrl + "/v1/chat/completions"))
			.header("Content-Type", "application/json")
			.header("Accept", "text/event-stream")
			.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
			.timeout(Duration.ofMinutes(10));
		if (_apiKey != null && !_apiKey.isBlank()) {
			requestBuilder.header("Authorization", "Bearer " + _apiKey);
		}
		HttpRequest httpRequest = requestBuilder.build();

		HttpResponse<InputStream> response;
		try {
			response = _httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Connection failed: " + e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0, "Interrupted", e);
		}

		if (response.statusCode() != 200) {
			handleErrorResponse(response);
		}

		try (InputStream responseBody = response.body()) {
			processStream(responseBody, handler);
		} catch (ProviderException e) {
			throw e;
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Stream read error: " + e.getMessage(), e);
		}
	}

	private String buildRequestJson(ProviderRequest request) throws ProviderException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("model").value(request.model());
			json.name("max_tokens").value(request.maxOutputTokens());
			json.name("stream").value(true);

			json.name("messages");
			writeMessages(json, request.systemPrompt(), request.messages());

			if (!request.tools().isEmpty()) {
				json.name("tools");
				writeTools(json, request.tools());
			}

			if (request.responseFormat() == ResponseFormat.JSON) {
				json.name("response_format");
				json.beginObject();
				json.name("type").value("json_object");
				json.endObject();
			}

			if (request.toolChoice() != null || !request.tools().isEmpty()) {
				// Default to "auto" when tools are present; honour explicit choice otherwise.
				ToolChoice choice = request.toolChoice() != null ? request.toolChoice() : ToolChoice.AUTO;
				String choiceStr = switch (choice) {
					case REQUIRED -> "any";
					case NONE -> "none";
					case AUTO -> "auto";
				};
				json.name("tool_choice").value(choiceStr);
			}

			json.endObject();
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Failed to build request JSON", e);
		}
		return buffer.toString();
	}

	private void writeMessages(JsonWriter json, String systemPrompt, List<Message> messages)
			throws IOException {
		json.beginArray();

		if (systemPrompt != null && !systemPrompt.isEmpty()) {
			json.beginObject();
			json.name("role").value("system");
			json.name("content").value(systemPrompt);
			json.endObject();
		}

		for (Message msg : messages) {
			if (msg instanceof UserMessage user) {
				writeUserMessage(json, user);
			} else if (msg instanceof AssistantMessage assistant) {
				writeAssistantMessage(json, assistant);
			} else if (msg instanceof ToolResultMessage result) {
				writeToolResult(json, result);
			}
		}
		json.endArray();
	}

	private void writeUserMessage(JsonWriter json, UserMessage user) throws IOException {
		json.beginObject();
		json.name("role").value("user");

		boolean hasImages = user.content().stream()
			.anyMatch(p -> p instanceof ImageContent || p instanceof PdfContent);

		if (hasImages) {
			json.name("content");
			json.beginArray();
			for (ContentPart part : user.content()) {
				if (part instanceof TextContent text) {
					json.beginObject();
					json.name("type").value("text");
					json.name("text").value(text.text());
					json.endObject();
				} else if (part instanceof ImageContent image) {
					json.beginObject();
					json.name("type").value("image_url");
					json.name("image_url");
					json.beginObject();
					json.name("url").value(image.url());
					json.endObject();
					json.endObject();
				}
			}
			json.endArray();
		} else {
			// Text-only: emit as plain string.
			StringBuilder text = new StringBuilder();
			for (ContentPart part : user.content()) {
				if (part instanceof TextContent tc) {
					text.append(tc.text());
				}
			}
			json.name("content").value(text.toString());
		}

		json.endObject();
	}

	private void writeAssistantMessage(JsonWriter json, AssistantMessage assistant) throws IOException {
		json.beginObject();
		json.name("role").value("assistant");

		boolean hasToolCalls = assistant.content().stream()
			.anyMatch(p -> p instanceof ToolCall);

		if (hasToolCalls) {
			// Emit text content (may be empty).
			StringBuilder text = new StringBuilder();
			for (ContentPart part : assistant.content()) {
				if (part instanceof TextContent tc) {
					text.append(tc.text());
				}
			}
			json.name("content").value(text.toString());

			json.name("tool_calls");
			json.beginArray();
			for (ContentPart part : assistant.content()) {
				if (part instanceof ToolCall toolCall) {
					json.beginObject();
					json.name("id").value(toolCall.id());
					json.name("type").value("function");
					json.name("function");
					json.beginObject();
					json.name("name").value(toolCall.name());
					String args = toolCall.arguments();
					json.name("arguments").value(args.isEmpty() ? "{}" : args);
					json.endObject();
					json.endObject();
				}
			}
			json.endArray();
		} else {
			// Text-only assistant message.
			StringBuilder text = new StringBuilder();
			for (ContentPart part : assistant.content()) {
				if (part instanceof TextContent tc) {
					text.append(tc.text());
				}
			}
			json.name("content").value(text.toString());
		}

		json.endObject();
	}

	private void writeToolResult(JsonWriter json, ToolResultMessage result) throws IOException {
		json.beginObject();
		json.name("role").value("tool");
		json.name("tool_call_id").value(result.toolCallId());
		json.name("name").value(result.toolName());

		ToolState state = result.state();
		String content;
		if (state instanceof ToolState.Completed c) {
			content = c.result();
		} else if (state instanceof ToolState.Failed f) {
			content = f.error();
		} else {
			content = "";
		}
		json.name("content").value(content);
		json.endObject();
	}

	private void writeTools(JsonWriter json, List<ToolSchema> tools) throws IOException {
		json.beginArray();
		for (ToolSchema tool : tools) {
			json.beginObject();
			json.name("type").value("function");
			json.name("function");
			json.beginObject();
			json.name("name").value(tool.name());
			json.name("description").value(tool.description());
			json.name("parameters").jsonValue(tool.inputSchemaJson());
			json.endObject();
			json.endObject();
		}
		json.endArray();
	}

	private void processStream(InputStream input, StreamHandler handler)
			throws IOException, ProviderException {

		String messageId = null;
		String model = null;
		String finishReason = null;
		int inputTokens = 0;
		int outputTokens = 0;
		Map<Integer, ContentBlock> blocks = new LinkedHashMap<>();
		int contentIndex = 0;

		SSEReader.Event event;
		try (SSEReader sse = new SSEReader(input)) {
			while ((event = sse.next()) != null) {
				String data = event.data();
				if (data == null || data.isEmpty()) {
					continue;
				}

				if ("[DONE]".equals(data)) {
					List<ContentPart> content = buildContentParts(blocks);
					TokenUsage usage = new TokenUsage(inputTokens, outputTokens, 0, 0, 0);
					handler.onEvent(new StreamEvent.MessageComplete(
						messageId, model,
						finishReason != null ? finishReason : "end_turn",
						usage, content));
					break;
				}

				try (JsonReader json = jsonReader(data)) {
					json.beginObject();
					while (json.hasNext()) {
						switch (json.nextName()) {
							case "id" -> messageId = json.nextString();
							case "model" -> model = json.nextString();
							case "choices" -> {
								json.beginArray();
								while (json.hasNext()) {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "delta" -> {
												contentIndex = processDelta(json, blocks, contentIndex, handler);
											}
											case "finish_reason" -> {
												if (json.peek() == JsonToken.NULL) {
													json.skipValue();
												} else {
													finishReason = mapFinishReason(json.nextString());
												}
											}
											default -> json.skipValue();
										}
									}
									json.endObject();
								}
								json.endArray();
							}
							case "usage" -> {
								json.beginObject();
								while (json.hasNext()) {
									switch (json.nextName()) {
										case "prompt_tokens" -> inputTokens = json.nextInt();
										case "completion_tokens" -> outputTokens = json.nextInt();
										default -> json.skipValue();
									}
								}
								json.endObject();
							}
							default -> json.skipValue();
						}
					}
					json.endObject();
				}
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Failed to process Mistral stream response: " + e.getMessage(), e);
		}
	}

	private int processDelta(JsonReader json, Map<Integer, ContentBlock> blocks,
			int contentIndex, StreamHandler handler) throws IOException {

		json.beginObject();
		while (json.hasNext()) {
			switch (json.nextName()) {
				case "content" -> {
					String text = normalizeContent(json);
					if (text != null && !text.isEmpty()) {
						ContentBlock block = blocks.computeIfAbsent(contentIndex,
							idx -> new ContentBlock("text"));
						block._text.append(text);
						handler.onEvent(new StreamEvent.TextDelta(contentIndex, text));
					}
				}
				case "tool_calls" -> {
					json.beginArray();
					while (json.hasNext()) {
						String id = null;
						String name = null;
						String arguments = null;

						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "id" -> id = json.nextString();
								case "function" -> {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "name" -> name = json.nextString();
											case "arguments" -> arguments = readArguments(json);
											default -> json.skipValue();
										}
									}
									json.endObject();
								}
								default -> json.skipValue();
							}
						}
						json.endObject();

						if (id != null && name != null) {
							ContentBlock block = new ContentBlock("tool_use");
							block._id = id;
							block._name = name;
							block._arguments.append(arguments != null ? arguments : "");
							blocks.put(contentIndex, block);

							handler.onEvent(new StreamEvent.ToolCallStart(contentIndex, id, name));
							if (arguments != null && !arguments.isEmpty()) {
								handler.onEvent(new StreamEvent.ToolCallDelta(contentIndex, arguments));
							}
							contentIndex++;
						}
					}
					json.endArray();
				}
				default -> json.skipValue();
			}
		}
		json.endObject();

		return contentIndex;
	}

	/**
	 * Normalize the {@code content} field which may be a string or an array of typed blocks.
	 */
	private String normalizeContent(JsonReader json) throws IOException {
		if (json.peek() == JsonToken.NULL) {
			json.skipValue();
			return null;
		}
		if (json.peek() == JsonToken.STRING) {
			return json.nextString();
		}
		// Array of content blocks — extract text from each.
		StringBuilder result = new StringBuilder();
		json.beginArray();
		while (json.hasNext()) {
			json.beginObject();
			String text = null;
			while (json.hasNext()) {
				switch (json.nextName()) {
					case "text" -> text = json.nextString();
					default -> json.skipValue();
				}
			}
			json.endObject();
			if (text != null) {
				result.append(text);
			}
		}
		json.endArray();
		return result.toString();
	}

	/**
	 * Read the {@code arguments} field which may be a string or a pre-parsed JSON object.
	 * If it is an object, re-serialize it to a JSON string using {@link #copyJsonValue(JsonReader)}.
	 */
	private String readArguments(JsonReader json) throws IOException {
		if (json.peek() == JsonToken.STRING) {
			return json.nextString();
		}
		// Pre-parsed object — re-serialize to JSON string.
		return copyJsonValue(json);
	}

	/**
	 * Copy an arbitrary JSON value from the reader and return it as a string.
	 */
	private String copyJsonValue(JsonReader json) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter writer = new JsonWriter(buffer)) {
			copyValue(json, writer);
		}
		return buffer.toString();
	}

	private void copyValue(JsonReader reader, JsonWriter writer) throws IOException {
		switch (reader.peek()) {
			case BEGIN_OBJECT -> {
				reader.beginObject();
				writer.beginObject();
				while (reader.hasNext()) {
					writer.name(reader.nextName());
					copyValue(reader, writer);
				}
				reader.endObject();
				writer.endObject();
			}
			case BEGIN_ARRAY -> {
				reader.beginArray();
				writer.beginArray();
				while (reader.hasNext()) {
					copyValue(reader, writer);
				}
				reader.endArray();
				writer.endArray();
			}
			case STRING -> writer.value(reader.nextString());
			case NUMBER -> writer.value(reader.nextDouble());
			case BOOLEAN -> writer.value(reader.nextBoolean());
			case NULL -> {
				reader.skipValue();
				writer.nullValue();
			}
			default -> reader.skipValue();
		}
	}

	private String mapFinishReason(String reason) {
		return switch (reason) {
			case "stop" -> "end_turn";
			case "length" -> "max_tokens";
			case "tool_calls" -> "tool_use";
			default -> reason;
		};
	}

	private List<ContentPart> buildContentParts(Map<Integer, ContentBlock> blocks) {
		List<ContentPart> parts = new ArrayList<>(blocks.size());
		for (ContentBlock block : blocks.values()) {
			if ("text".equals(block._type)) {
				String text = block._text.toString();
				if (!text.isEmpty()) {
					parts.add(new TextContent(text));
				}
			} else if ("tool_use".equals(block._type)) {
				parts.add(new ToolCall(block._id, block._name, block._arguments.toString()));
			}
		}
		return parts;
	}

	private void handleErrorResponse(HttpResponse<InputStream> response) throws ProviderException {
		int status = response.statusCode();
		String body = "";
		try (InputStream is = response.body()) {
			body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			// Best effort.
		}

		Duration retryAfter = parseRetryAfter(response.headers());
		String message = parseErrorMessage(body);

		ProviderException.Kind kind = switch (status) {
			case 401, 403 -> ProviderException.Kind.AUTHENTICATION;
			case 429 -> ProviderException.Kind.RATE_LIMITED;
			default -> {
				if (status >= 500) {
					yield ProviderException.Kind.OVERLOADED;
				}
				yield ProviderException.Kind.INVALID_REQUEST;
			}
		};

		throw new ProviderException(kind, status, message, retryAfter, null);
	}

	private String parseErrorMessage(String jsonBody) {
		try (JsonReader json = jsonReader(jsonBody)) {
			json.beginObject();
			while (json.hasNext()) {
				String name = json.nextName();
				if ("error".equals(name)) {
					json.beginObject();
					while (json.hasNext()) {
						if ("message".equals(json.nextName())) {
							return json.nextString();
						}
						json.skipValue();
					}
					json.endObject();
				} else if ("message".equals(name)) {
					return json.nextString();
				} else {
					json.skipValue();
				}
			}
			json.endObject();
		} catch (Exception e) {
			// Not valid JSON — return raw body.
		}
		return jsonBody;
	}

	private Duration parseRetryAfter(java.net.http.HttpHeaders headers) {
		return headers.firstValue("retry-after")
			.map(value -> {
				try {
					return Duration.ofSeconds(Long.parseLong(value));
				} catch (NumberFormatException e) {
					return null;
				}
			})
			.orElse(null);
	}

	private static JsonReader jsonReader(String json) {
		return new JsonReader(new StringR(json));
	}

	private static final class ContentBlock {
		final String _type;

		final StringBuilder _text = new StringBuilder();

		String _id;

		String _name;

		final StringBuilder _arguments = new StringBuilder();

		ContentBlock(String type) {
			_type = type;
		}
	}
}
