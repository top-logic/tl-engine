/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.mistral;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.AbstractStreamingProvider;
import com.top_logic.tl.ai.core.provider.ContentBlockAccumulator;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.SSEReader;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsRequestBuilder;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * {@link AbstractStreamingProvider} for the Mistral Chat Completions API.
 *
 * <p>Uses {@link HttpClient} for HTTP and msgbuf {@link JsonWriter}/{@link JsonReader} for JSON.
 * Thread-safe: a single instance handles concurrent streams.</p>
 */
public class MistralProvider extends AbstractStreamingProvider {

	private static final String DEFAULT_BASE_URL = "https://api.mistral.ai";

	/** Creates a {@link MistralProvider}. */
	public MistralProvider(String apiKey) {
		this(apiKey, DEFAULT_BASE_URL);
	}

	/** Creates a {@link MistralProvider}. */
	public MistralProvider(String apiKey, String baseUrl) {
		this(apiKey, baseUrl, defaultHttpClient());
	}

	/** Creates a {@link MistralProvider}. */
	public MistralProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		super(apiKey, baseUrl, httpClient);
	}

	/** Creates a {@link MistralProvider} with configurable timeouts and headers. */
	public MistralProvider(String apiKey, String baseUrl,
			int connectTimeoutSeconds, int requestTimeoutSeconds, Map<String, String> customHeaders) {
		super(apiKey, baseUrl, defaultHttpClient(connectTimeoutSeconds),
			Duration.ofSeconds(requestTimeoutSeconds), customHeaders);
	}

	/**
	 * Undocumented Mistral API limit: request bodies above ~32 KB cause the connection to hang
	 * indefinitely with no response. Confirmed via curl — this is a server-side infrastructure
	 * issue (likely a reverse proxy), not a client bug. Guard with a conservative threshold.
	 */
	private static final int MAX_REQUEST_BODY_BYTES = 5_000_000;

	@Override
	protected String endpointPath(ProviderRequest request) {
		return "/v1/chat/completions";
	}

	@Override
	protected void validateRequest(String body) throws ProviderException {
		int bodyBytes = body.getBytes(StandardCharsets.UTF_8).length;
		if (bodyBytes > MAX_REQUEST_BODY_BYTES) {
			throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 0,
				"Request body (" + bodyBytes + " bytes) exceeds Mistral's ~32 KB limit. "
					+ "Reduce the system prompt or number of tools, or use a different provider.");
		}
	}

	@Override
	protected String buildRequestJson(ProviderRequest request) throws ProviderException {
		// Mistral-specific: default tool_choice to "auto" when tools present,
		// and map REQUIRED to "any" (Mistral's non-standard name for "required").
		String toolChoiceOverride = null;
		if (request.toolChoice() != null || !request.tools().isEmpty()) {
			ToolChoice choice = request.toolChoice() != null ? request.toolChoice() : ToolChoice.AUTO;
			toolChoiceOverride = switch (choice) {
				case REQUIRED -> "any";
				case NONE -> "none";
				case AUTO -> "auto";
			};
		}
		return ChatCompletionsRequestBuilder.buildRequestJson(request, toolChoiceOverride);
	}

	@Override
	protected void processStream(InputStream input, StreamHandler handler)
			throws IOException, ProviderException {

		String messageId = null;
		String model = null;
		String finishReason = null;
		int inputTokens = 0;
		int outputTokens = 0;
		ContentBlockAccumulator accumulator = new ContentBlockAccumulator();
		int contentIndex = 0;
		boolean textBlockOpen = false;

		handler.onEvent(new StreamEvent.StreamStart());

		SSEReader.Event event;
		try (SSEReader sse = new SSEReader(input)) {
			while ((event = sse.next()) != null) {
				String data = event.data();
				if (data == null || data.isEmpty()) {
					continue;
				}

				if ("[DONE]".equals(data)) {
					if (textBlockOpen) {
						handler.onEvent(new StreamEvent.TextEnd(0));
					}
					List<ContentPart> content = accumulator.buildContentParts();
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
												int[] result = processDelta(json, accumulator, contentIndex,
													textBlockOpen, handler);
												contentIndex = result[0];
												textBlockOpen = result[1] != 0;
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

	/**
	 * Processes a single delta object from the Mistral stream.
	 *
	 * @return a two-element array: {@code [contentIndex, textBlockOpen]} where
	 *         {@code textBlockOpen} is 1 if a text block is currently open, 0 otherwise.
	 */
	private int[] processDelta(JsonReader json, ContentBlockAccumulator accumulator,
			int contentIndex, boolean textBlockOpen, StreamHandler handler) throws IOException {

		json.beginObject();
		while (json.hasNext()) {
			switch (json.nextName()) {
				case "content" -> {
					String text = normalizeContent(json);
					if (text != null && !text.isEmpty()) {
						if (!textBlockOpen) {
							accumulator.addBlock(contentIndex, "text", null, null);
							handler.onEvent(new StreamEvent.TextStart(contentIndex));
							textBlockOpen = true;
						}
						accumulator.appendText(contentIndex, text);
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
							accumulator.addBlock(contentIndex, "tool_use", id, name);
							if (arguments != null && !arguments.isEmpty()) {
								accumulator.appendArguments(contentIndex, arguments);
							}

							handler.onEvent(new StreamEvent.ToolCallStart(contentIndex, id, name));
							if (arguments != null && !arguments.isEmpty()) {
								handler.onEvent(new StreamEvent.ToolCallDelta(contentIndex, arguments));
							}
							handler.onEvent(new StreamEvent.ToolCallEnd(contentIndex));
							contentIndex++;
						}
					}
					json.endArray();
				}
				default -> json.skipValue();
			}
		}
		json.endObject();

		return new int[] { contentIndex, textBlockOpen ? 1 : 0 };
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

	@Override
	protected void handleErrorResponse(HttpResponse<InputStream> response) throws ProviderException {
		int status = response.statusCode();
		String body = drainResponseBody(response);

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


}
