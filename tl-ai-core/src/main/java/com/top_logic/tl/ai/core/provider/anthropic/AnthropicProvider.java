/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

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

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
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
import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * {@link LLMProvider} for the Anthropic Messages API.
 *
 * <p>Uses {@link HttpClient} for HTTP and msgbuf {@link JsonWriter}/{@link JsonReader} for JSON.
 * Thread-safe: a single instance handles concurrent streams.</p>
 */
public class AnthropicProvider implements LLMProvider {

	private static final String DEFAULT_BASE_URL = "https://api.anthropic.com";

	private static final String DEFAULT_API_VERSION = "2023-06-01";

	private final HttpClient _httpClient;

	private final String _apiKey;

	private final String _baseUrl;

	private final String _apiVersion;

	/** */
	public AnthropicProvider(String apiKey) {
		this(apiKey, DEFAULT_BASE_URL, DEFAULT_API_VERSION);
	}

	/** */
	public AnthropicProvider(String apiKey, String baseUrl, String apiVersion) {
		this(apiKey, baseUrl, apiVersion, defaultHttpClient());
	}

	/** */
	public AnthropicProvider(String apiKey, String baseUrl, String apiVersion, HttpClient httpClient) {
		_apiKey = Objects.requireNonNull(apiKey, "API key must not be null.");
		_baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		_apiVersion = Objects.requireNonNull(apiVersion, "API version must not be null.");
		_httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null.");
	}

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

		HttpRequest httpRequest = HttpRequest.newBuilder()
			.uri(URI.create(_baseUrl + "/v1/messages"))
			.header("Content-Type", "application/json")
			.header("Accept", "text/event-stream")
			.header("x-api-key", _apiKey)
			.header("anthropic-version", _apiVersion)
			.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
			.timeout(Duration.ofMinutes(10))
			.build();

		HttpResponse<InputStream> response;
		try {
			response = _httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Connection failed: " + e.getMessage(), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
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

	String buildRequestJson(ProviderRequest request) throws ProviderException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("model").value(request.model());
			json.name("max_tokens").value((long) request.maxOutputTokens());
			json.name("stream").value(true);

			if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
				json.name("system");
				json.beginArray();
				json.beginObject();
				json.name("type").value("text");
				json.name("text").value(request.systemPrompt());
				json.name("cache_control");
				json.beginObject();
				json.name("type").value("ephemeral");
				json.endObject();
				json.endObject();
				json.endArray();
			}

			json.name("messages");
			writeMessages(json, request.messages());

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

			if (request.toolChoice() != null && request.toolChoice() != ToolChoice.AUTO) {
				json.name("tool_choice");
				json.beginObject();
				json.name("type").value(request.toolChoice() == ToolChoice.REQUIRED ? "any" : "none");
				json.endObject();
			}

			json.endObject();
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Failed to build request JSON", e);
		}
		return buffer.toString();
	}

	private void writeMessages(JsonWriter json, List<Message> messages) throws IOException {
		json.beginArray();
		int i = 0;
		while (i < messages.size()) {
			Message msg = messages.get(i);

			if (msg instanceof SystemMessage) {
				i++;
			} else if (msg instanceof CompactionMarker) {
				i++;
			} else if (msg instanceof UserMessage user) {
				json.beginObject();
				json.name("role").value("user");
				json.name("content");
				writeUserContent(json, user.content());
				json.endObject();
				i++;
			} else if (msg instanceof AssistantMessage assistant) {
				json.beginObject();
				json.name("role").value("assistant");
				json.name("content");
				writeAssistantContent(json, assistant.content());
				json.endObject();
				i++;
			} else if (msg instanceof ToolResultMessage) {
				json.beginObject();
				json.name("role").value("user");
				json.name("content");
				json.beginArray();
				while (i < messages.size()
					&& messages.get(i) instanceof ToolResultMessage tr) {
					writeToolResult(json, tr);
					i++;
				}
				json.endArray();
				json.endObject();
			}
		}
		json.endArray();
	}

	private void writeUserContent(JsonWriter json, List<ContentPart> parts) throws IOException {
		json.beginArray();
		for (ContentPart part : parts) {
			if (part instanceof TextContent text) {
				json.beginObject();
				json.name("type").value("text");
				json.name("text").value(text.text());
				json.endObject();
			} else if (part instanceof ImageContent image) {
				json.beginObject();
				json.name("type").value("image");
				json.name("source");
				json.beginObject();
				if (image.url().startsWith("data:")) {
					String dataUri = image.url();
					int commaIdx = dataUri.indexOf(',');
					String base64Data = commaIdx >= 0 ? dataUri.substring(commaIdx + 1) : dataUri;
					json.name("type").value("base64");
					json.name("media_type").value(image.mediaType());
					json.name("data").value(base64Data);
				} else {
					json.name("type").value("url");
					json.name("url").value(image.url());
				}
				json.endObject();
				json.endObject();
			} else if (part instanceof PdfContent pdf) {
				json.beginObject();
				json.name("type").value("document");
				json.name("source");
				json.beginObject();
				json.name("type").value("base64");
				json.name("media_type").value(pdf.mediaType());
				json.name("data").value(pdf.data());
				json.endObject();
				json.endObject();
			} /* ToolCall and ThinkingContent not valid in user messages. */
		}
		json.endArray();
	}

	private void writeAssistantContent(JsonWriter json, List<ContentPart> parts) throws IOException {
		json.beginArray();
		for (ContentPart part : parts) {
			if (part instanceof TextContent text) {
				json.beginObject();
				json.name("type").value("text");
				json.name("text").value(text.text());
				json.endObject();
			} else if (part instanceof ToolCall toolCall) {
				json.beginObject();
				json.name("type").value("tool_use");
				json.name("id").value(toolCall.id());
				json.name("name").value(toolCall.name());
				String args = toolCall.arguments();
				json.name("input").jsonValue(args.isEmpty() ? "{}" : args);
				json.endObject();
			} /* ImageContent, PdfContent, ThinkingContent not valid/serialized in assistant messages. */
		}
		json.endArray();
	}

	private void writeToolResult(JsonWriter json, ToolResultMessage result) throws IOException {
		json.beginObject();
		json.name("type").value("tool_result");
		json.name("tool_use_id").value(result.toolCallId());

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

		if (result.state() instanceof ToolState.Failed) {
			json.name("is_error").value(true);
		}
		json.endObject();
	}

	private void writeTools(JsonWriter json, List<ToolSchema> tools) throws IOException {
		json.beginArray();
		for (ToolSchema tool : tools) {
			json.beginObject();
			json.name("name").value(tool.name());
			json.name("description").value(tool.description());
			json.name("input_schema").jsonValue(tool.inputSchemaJson());
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
		int cacheReadTokens = 0;
		int cacheWriteTokens = 0;
		Map<Integer, ContentBlock> blocks = new LinkedHashMap<>();

		SSEReader.Event event;
		try (SSEReader sse = new SSEReader(input)) {
		while ((event = sse.next()) != null) {
			String data = event.data();
			if (data == null || data.isEmpty()) {
				continue;
			}

			switch (event.event()) {
				case "message_start" -> {
					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							if ("message".equals(json.nextName())) {
								json.beginObject();
								while (json.hasNext()) {
									switch (json.nextName()) {
										case "id" -> messageId = json.nextString();
										case "model" -> model = json.nextString();
										case "usage" -> {
											int[] u = parseUsage(json);
											inputTokens = u[0];
											outputTokens = u[1];
											cacheReadTokens = u[2];
											cacheWriteTokens = u[3];
										}
										default -> json.skipValue();
									}
								}
								json.endObject();
							} else {
								json.skipValue();
							}
						}
						json.endObject();
					}
				}

				case "content_block_start" -> {
					int index = -1;
					String blockType = null;
					String toolId = null;
					String toolName = null;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "index" -> index = json.nextInt();
								case "content_block" -> {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "type" -> blockType = json.nextString();
											case "id" -> toolId = json.nextString();
											case "name" -> toolName = json.nextString();
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

					if (index >= 0 && blockType != null) {
						ContentBlock block = new ContentBlock(blockType);
						block._id = toolId;
						block._name = toolName;
						blocks.put(index, block);

						if ("tool_use".equals(blockType) && toolId != null && toolName != null) {
							handler.onEvent(new StreamEvent.ToolCallStart(index, toolId, toolName));
						}
					}
				}

				case "content_block_delta" -> {
					int index = -1;
					String deltaType = null;
					String text = null;
					String partialJson = null;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "index" -> index = json.nextInt();
								case "delta" -> {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "type" -> deltaType = json.nextString();
											case "text" -> text = json.nextString();
											case "partial_json" -> partialJson = json.nextString();
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

					ContentBlock block = blocks.get(index);
					if (block != null && index >= 0) {
						if ("text_delta".equals(deltaType) && text != null) {
							block._text.append(text);
							handler.onEvent(new StreamEvent.TextDelta(index, text));
						} else if ("thinking_delta".equals(deltaType) && text != null) {
							block._text.append(text);
							handler.onEvent(new StreamEvent.ThinkingDelta(index, text));
						} else if ("input_json_delta".equals(deltaType) && partialJson != null) {
							block._arguments.append(partialJson);
							handler.onEvent(new StreamEvent.ToolCallDelta(index, partialJson));
						}
					}
				}

				case "content_block_stop" -> {
					// Block already accumulated — nothing to do.
				}

				case "message_delta" -> {
					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "delta" -> {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "stop_reason" -> {
												switch (json.peek()) {
													case NULL -> json.skipValue();
													default -> finishReason = json.nextString();
												}
											}
											default -> json.skipValue();
										}
									}
									json.endObject();
								}
								case "usage" -> {
									// message_delta only carries output_tokens (incremental).
									int[] u = parseUsage(json);
									outputTokens += u[1];
								}
								default -> json.skipValue();
							}
						}
						json.endObject();
					}
				}

				case "message_stop" -> {
					List<ContentPart> content = buildContentParts(blocks);
					TokenUsage usage = new TokenUsage(
						inputTokens, outputTokens, 0, cacheReadTokens, cacheWriteTokens);
					handler.onEvent(new StreamEvent.MessageComplete(
						messageId, model,
						finishReason != null ? finishReason : "end_turn",
						usage, content));
				}

				case "error" -> {
					throw parseStreamError(data);
				}

				default -> { /* Ignore unknown event types. */ }
			}
		}
		}
	}

	private static JsonReader jsonReader(String json) {
		return new JsonReader(new StringR(json));
	}

	private int[] parseUsage(JsonReader json) throws IOException {
		int input = 0, output = 0, cacheRead = 0, cacheWrite = 0;
		json.beginObject();
		while (json.hasNext()) {
			switch (json.nextName()) {
				case "input_tokens" -> input = json.nextInt();
				case "output_tokens" -> output = json.nextInt();
				case "cache_read_input_tokens" -> cacheRead = json.nextInt();
				case "cache_creation_input_tokens" -> cacheWrite = json.nextInt();
				default -> json.skipValue();
			}
		}
		json.endObject();
		return new int[] { input, output, cacheRead, cacheWrite };
	}

	private List<ContentPart> buildContentParts(Map<Integer, ContentBlock> blocks) {
		List<ContentPart> parts = new ArrayList<>(blocks.size());
		for (ContentBlock block : blocks.values()) {
			if ("text".equals(block._type)) {
				String text = block._text.toString();
				if (!text.isEmpty()) {
					parts.add(new TextContent(text));
				}
			} else if ("thinking".equals(block._type)) {
				String text = block._text.toString();
				if (!text.isEmpty()) {
					parts.add(new ThinkingContent(text));
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
			case 529 -> ProviderException.Kind.OVERLOADED;
			default -> {
				if (message.toLowerCase().contains("context") && message.contains("long")) {
					yield ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED;
				}
				if (status >= 500) {
					yield ProviderException.Kind.OVERLOADED;
				}
				yield ProviderException.Kind.INVALID_REQUEST;
			}
		};

		throw new ProviderException(kind, status, message, retryAfter, null);
	}

	private ProviderException parseStreamError(String data) {
		String type = "";
		String message = "";
		try (JsonReader json = jsonReader(data)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("error".equals(json.nextName())) {
					json.beginObject();
					while (json.hasNext()) {
						switch (json.nextName()) {
							case "type" -> type = json.nextString();
							case "message" -> message = json.nextString();
							default -> json.skipValue();
						}
					}
					json.endObject();
				} else {
					json.skipValue();
				}
			}
			json.endObject();
		} catch (Exception e) {
			message = data;
		}

		ProviderException.Kind kind = switch (type) {
			case "overloaded_error" -> ProviderException.Kind.OVERLOADED;
			case "rate_limit_error" -> ProviderException.Kind.RATE_LIMITED;
			case "authentication_error" -> ProviderException.Kind.AUTHENTICATION;
			case "invalid_request_error" -> ProviderException.Kind.INVALID_REQUEST;
			default -> ProviderException.Kind.UNKNOWN;
		};
		return new ProviderException(kind, 0, message);
	}

	private String parseErrorMessage(String jsonBody) {
		try (JsonReader json = jsonReader(jsonBody)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("error".equals(json.nextName())) {
					json.beginObject();
					while (json.hasNext()) {
						if ("message".equals(json.nextName())) {
							return json.nextString();
						}
						json.skipValue();
					}
					json.endObject();
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
