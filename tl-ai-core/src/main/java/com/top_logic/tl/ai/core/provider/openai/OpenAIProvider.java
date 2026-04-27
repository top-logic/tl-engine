/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

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
import com.top_logic.tl.ai.core.provider.ToolSchema;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * {@link LLMProvider} for the OpenAI Responses API.
 *
 * <p>Uses {@link HttpClient} for HTTP and msgbuf {@link JsonWriter}/{@link JsonReader} for JSON.
 * Thread-safe: a single instance handles concurrent streams.</p>
 */
public class OpenAIProvider implements LLMProvider {

	private static final String DEFAULT_BASE_URL = "https://api.openai.com";

	private final HttpClient _httpClient;

	private final String _apiKey;

	private final String _baseUrl;

	/** */
	public OpenAIProvider(String apiKey) {
		this(apiKey, DEFAULT_BASE_URL);
	}

	/** */
	public OpenAIProvider(String apiKey, String baseUrl) {
		this(apiKey, baseUrl, defaultHttpClient());
	}

	/** */
	public OpenAIProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		_apiKey = Objects.requireNonNull(apiKey, "API key must not be null.");
		_baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
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
			.uri(URI.create(_baseUrl + "/v1/responses"))
			.header("Content-Type", "application/json")
			.header("Accept", "text/event-stream")
			.header("Authorization", "Bearer " + _apiKey)
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
			json.name("max_output_tokens").value((long) request.maxOutputTokens());
			json.name("stream").value(true);
			json.name("store").value(true);

			if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
				json.name("instructions").value(request.systemPrompt());
			}

			writeReasoning(json, request.modelOptions());

			json.name("input");
			writeInput(json, request.messages());

			if (!request.tools().isEmpty()) {
				json.name("tools");
				writeTools(json, request.tools());
			}

			if (request.responseFormat() == ResponseFormat.JSON) {
				json.name("text");
				json.beginObject();
				json.name("format");
				json.beginObject();
				json.name("type").value("json_object");
				json.endObject();
				json.endObject();
			}

			if (request.toolChoice() != null) {
				String choice = switch (request.toolChoice()) {
					case REQUIRED -> "required";
					case NONE -> "none";
					case AUTO -> "auto";
				};
				json.name("tool_choice").value(choice);
			}

			json.endObject();
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Failed to build request JSON", e);
		}
		return buffer.toString();
	}

	private void writeReasoning(JsonWriter json, Map<String, Object> modelOptions) throws IOException {
		Object effort = modelOptions.get("reasoning_effort");
		Object summary = modelOptions.get("reasoning_summary");
		if (effort != null || summary != null) {
			json.name("reasoning");
			json.beginObject();
			if (effort != null) {
				json.name("effort").value(effort.toString());
			}
			if (summary != null) {
				json.name("summary").value(summary.toString());
			}
			json.endObject();
		}
	}

	private void writeInput(JsonWriter json, List<Message> messages) throws IOException {
		json.beginArray();
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
		json.name("content");
		json.beginArray();
		for (ContentPart part : user.content()) {
			if (part instanceof TextContent text) {
				json.beginObject();
				json.name("type").value("input_text");
				json.name("text").value(text.text());
				json.endObject();
			} else if (part instanceof ImageContent image) {
				json.beginObject();
				json.name("type").value("input_image");
				json.name("image_url").value(image.url());
				json.endObject();
			} else if (part instanceof PdfContent pdf) {
				json.beginObject();
				json.name("type").value("input_file");
				json.name("filename").value("document.pdf");
				json.name("file_data").value("data:" + pdf.mediaType() + ";base64," + pdf.data());
				json.endObject();
			}
		}
		json.endArray();
		json.endObject();
	}

	private void writeAssistantMessage(JsonWriter json, AssistantMessage assistant) throws IOException {
		for (ContentPart part : assistant.content()) {
			if (part instanceof TextContent text) {
				json.beginObject();
				json.name("type").value("message");
				json.name("role").value("assistant");
				json.name("content");
				json.beginArray();
				json.beginObject();
				json.name("type").value("output_text");
				json.name("text").value(text.text());
				json.endObject();
				json.endArray();
				json.endObject();
			} else if (part instanceof ToolCall toolCall) {
				json.beginObject();
				json.name("type").value("function_call");
				json.name("call_id").value(toolCall.id());
				json.name("name").value(toolCall.name());
				String args = toolCall.arguments();
				json.name("arguments").value(args.isEmpty() ? "{}" : args);
				json.endObject();
			} else if (part instanceof ThinkingContent thinking) {
				json.beginObject();
				json.name("type").value("reasoning");
				json.name("summary");
				json.beginArray();
				json.beginObject();
				json.name("type").value("summary_text");
				json.name("text").value(thinking.text());
				json.endObject();
				json.endArray();
				json.endObject();
			}
		}
	}

	private void writeToolResult(JsonWriter json, ToolResultMessage result) throws IOException {
		json.beginObject();
		json.name("type").value("function_call_output");
		json.name("call_id").value(result.toolCallId());

		ToolState state = result.state();
		String content;
		if (state instanceof ToolState.Completed c) {
			content = c.result();
		} else if (state instanceof ToolState.Failed f) {
			content = f.error();
		} else {
			content = "";
		}
		json.name("output").value(content);
		json.endObject();
	}

	private void writeTools(JsonWriter json, List<ToolSchema> tools) throws IOException {
		json.beginArray();
		for (ToolSchema tool : tools) {
			json.beginObject();
			json.name("type").value("function");
			json.name("name").value(tool.name());
			json.name("description").value(tool.description());
			json.name("parameters").jsonValue(tool.inputSchemaJson());
			json.endObject();
		}
		json.endArray();
	}

	private void processStream(InputStream input, StreamHandler handler)
			throws IOException, ProviderException {

		String responseId = null;
		String model = null;
		String finishReason = null;
		int inputTokens = 0;
		int outputTokens = 0;
		int reasoningTokens = 0;
		int cachedTokens = 0;
		Map<String, ContentBlock> blocks = new LinkedHashMap<>();
		Map<Integer, String> outputIndexToBlockKey = new LinkedHashMap<>();
		int nextOutputIndex = 0;

		SSEReader.Event event;
		try (SSEReader sse = new SSEReader(input)) {
		while ((event = sse.next()) != null) {
			String data = event.data();
			if (data == null || data.isEmpty()) {
				continue;
			}

			String eventType = parseEventType(data);
			if (eventType == null) {
				continue;
			}

			switch (eventType) {
				case "response.created" -> {
					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							if ("response".equals(json.nextName())) {
								json.beginObject();
								while (json.hasNext()) {
									switch (json.nextName()) {
										case "id" -> responseId = json.nextString();
										case "model" -> model = json.nextString();
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

				case "response.output_item.added" -> {
					String itemType = null;
					String callId = null;
					String name = null;
					String itemId = null;
					int outputIndex = nextOutputIndex++;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "output_index" -> outputIndex = json.nextInt();
								case "item" -> {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "type" -> itemType = json.nextString();
											case "call_id" -> callId = json.nextString();
											case "name" -> name = json.nextString();
											case "id" -> itemId = json.nextString();
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

					if (itemType != null) {
						String key = callId != null ? callId : (itemId != null ? itemId : "item-" + outputIndex);
						ContentBlock block = new ContentBlock(itemType);
						block._id = callId;
						block._itemId = itemId;
						block._name = name;
						blocks.put(key, block);
						outputIndexToBlockKey.put(outputIndex, key);

						if ("function_call".equals(itemType) && callId != null && name != null) {
							handler.onEvent(new StreamEvent.ToolCallStart(outputIndex, callId, name));
						}
					}
				}

				case "response.output_text.delta" -> {
					int outputIndex = 0;
					String delta = null;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "output_index" -> outputIndex = json.nextInt();
								case "delta" -> delta = json.nextString();
								default -> json.skipValue();
							}
						}
						json.endObject();
					}

					if (delta != null) {
						// Accumulate in the message block.
						for (ContentBlock block : blocks.values()) {
							if ("message".equals(block._type)) {
								block._text.append(delta);
								break;
							}
						}
						handler.onEvent(new StreamEvent.TextDelta(outputIndex, delta));
					}
				}

				case "response.reasoning_text.delta" -> {
					int outputIndex = 0;
					String delta = null;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "output_index" -> outputIndex = json.nextInt();
								case "delta" -> delta = json.nextString();
								default -> json.skipValue();
							}
						}
						json.endObject();
					}

					if (delta != null) {
						for (ContentBlock block : blocks.values()) {
							if ("reasoning".equals(block._type)) {
								block._text.append(delta);
								break;
							}
						}
						handler.onEvent(new StreamEvent.ThinkingDelta(outputIndex, delta));
					}
				}

				case "response.function_call_arguments.delta" -> {
					int outputIndex = 0;
					String delta = null;
					String callId = null;
					String itemId = null;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "output_index" -> outputIndex = json.nextInt();
								case "delta" -> delta = json.nextString();
								case "call_id" -> callId = json.nextString();
								case "item_id" -> itemId = json.nextString();
								default -> json.skipValue();
							}
						}
						json.endObject();
					}

					if (delta != null) {
						ContentBlock block =
							findFunctionCallBlock(blocks, outputIndexToBlockKey, callId, itemId, outputIndex);
						if (block != null) {
							block._arguments.append(delta);
						}
						handler.onEvent(new StreamEvent.ToolCallDelta(outputIndex, delta));
					}
				}

				case "response.function_call_arguments.done" -> {
					int outputIndex = 0;
					String callId = null;
					String itemId = null;
					String arguments = null;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "output_index" -> outputIndex = json.nextInt();
								case "call_id" -> callId = json.nextString();
								case "item_id" -> itemId = json.nextString();
								case "arguments" -> arguments = json.nextString();
								case "item" -> {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "arguments" -> arguments = json.nextString();
											case "call_id" -> callId = json.nextString();
											case "id" -> itemId = json.nextString();
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

					if (arguments != null) {
						setFunctionArguments(
							blocks,
							outputIndexToBlockKey,
							callId,
							itemId,
							outputIndex,
							arguments);
					}
				}

				case "response.output_item.done" -> {
					int outputIndex = 0;
					String itemType = null;
					String callId = null;
					String itemId = null;
					String arguments = null;

					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "output_index" -> outputIndex = json.nextInt();
								case "item" -> {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "type" -> itemType = json.nextString();
											case "call_id" -> callId = json.nextString();
											case "id" -> itemId = json.nextString();
											case "arguments" -> arguments = json.nextString();
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

					if ("function_call".equals(itemType) && arguments != null) {
						setFunctionArguments(
							blocks,
							outputIndexToBlockKey,
							callId,
							itemId,
							outputIndex,
							arguments);
					}
				}

				case "response.completed" -> {
					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							if ("response".equals(json.nextName())) {
								json.beginObject();
								while (json.hasNext()) {
									switch (json.nextName()) {
										case "id" -> responseId = json.nextString();
										case "model" -> model = json.nextString();
										case "status" -> finishReason = mapFinishReason(json.nextString());
										case "usage" -> {
											int[] u = parseUsage(json);
											inputTokens = u[0];
											outputTokens = u[1];
											reasoningTokens = u[2];
											cachedTokens = u[3];
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

					List<ContentPart> content = buildContentParts(blocks);
					TokenUsage usage = new TokenUsage(
						inputTokens, outputTokens, reasoningTokens, cachedTokens, 0);
					handler.onEvent(new StreamEvent.MessageComplete(
						responseId, model,
						finishReason != null ? finishReason : "end_turn",
						usage, content));
				}

				case "response.failed" -> {
					throw parseStreamError(data);
				}

				case "response.incomplete" -> {
					// Incomplete responses still carry content and usage — emit what we have.
					try (JsonReader json = jsonReader(data)) {
						json.beginObject();
						while (json.hasNext()) {
							if ("response".equals(json.nextName())) {
								json.beginObject();
								while (json.hasNext()) {
									switch (json.nextName()) {
										case "id" -> responseId = json.nextString();
										case "model" -> model = json.nextString();
										case "usage" -> {
											int[] u = parseUsage(json);
											inputTokens = u[0];
											outputTokens = u[1];
											reasoningTokens = u[2];
											cachedTokens = u[3];
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

					List<ContentPart> content = buildContentParts(blocks);
					TokenUsage usage = new TokenUsage(
						inputTokens, outputTokens, reasoningTokens, cachedTokens, 0);
					handler.onEvent(new StreamEvent.MessageComplete(
						responseId, model, "max_tokens", usage, content));
				}

				default -> { /* Ignore unknown event types. */ }
			}
		}
		}
	}

	private String parseEventType(String data) {
		try (JsonReader json = jsonReader(data)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("type".equals(json.nextName())) {
					return json.nextString();
				}
				json.skipValue();
			}
			json.endObject();
		} catch (Exception e) {
			// Not valid JSON — ignore.
		}
		return null;
	}

	private static JsonReader jsonReader(String json) {
		return new JsonReader(new StringR(json));
	}

	private int[] parseUsage(JsonReader json) throws IOException {
		int input = 0, output = 0, reasoning = 0, cached = 0;
		json.beginObject();
		while (json.hasNext()) {
			switch (json.nextName()) {
				case "input_tokens" -> input = json.nextInt();
				case "output_tokens" -> output = json.nextInt();
				case "output_tokens_details" -> {
					json.beginObject();
					while (json.hasNext()) {
						if ("reasoning_tokens".equals(json.nextName())) {
							reasoning = json.nextInt();
						} else {
							json.skipValue();
						}
					}
					json.endObject();
				}
				case "input_tokens_details" -> {
					json.beginObject();
					while (json.hasNext()) {
						if ("cached_tokens".equals(json.nextName())) {
							cached = json.nextInt();
						} else {
							json.skipValue();
						}
					}
					json.endObject();
				}
				default -> json.skipValue();
			}
		}
		json.endObject();
		return new int[] { input, output, reasoning, cached };
	}

	private String mapFinishReason(String status) {
		return switch (status) {
			case "completed" -> "end_turn";
			case "incomplete" -> "max_tokens";
			case "cancelled" -> "end_turn";
			default -> status;
		};
	}

	private List<ContentPart> buildContentParts(Map<String, ContentBlock> blocks) {
		List<ContentPart> parts = new ArrayList<>(blocks.size());
		for (ContentBlock block : blocks.values()) {
			if ("message".equals(block._type)) {
				String text = block._text.toString();
				if (!text.isEmpty()) {
					parts.add(new TextContent(text));
				}
			} else if ("reasoning".equals(block._type)) {
				String text = block._text.toString();
				if (!text.isEmpty()) {
					parts.add(new ThinkingContent(text));
				}
			} else if ("function_call".equals(block._type)) {
				parts.add(new ToolCall(block._id, block._name, block._arguments.toString()));
			}
		}
		return parts;
	}

	private void setFunctionArguments(
			Map<String, ContentBlock> blocks,
			Map<Integer, String> outputIndexToBlockKey,
			String callId,
			String itemId,
			int outputIndex,
			String arguments) {
		ContentBlock block = findFunctionCallBlock(blocks, outputIndexToBlockKey, callId, itemId, outputIndex);
		if (block != null) {
			block._arguments.setLength(0);
			block._arguments.append(arguments);
		}
	}

	private ContentBlock findFunctionCallBlock(
			Map<String, ContentBlock> blocks,
			Map<Integer, String> outputIndexToBlockKey,
			String callId,
			String itemId,
			int outputIndex) {
		if (callId != null) {
			for (ContentBlock block : blocks.values()) {
				if ("function_call".equals(block._type) && callId.equals(block._id)) {
					return block;
				}
			}
		}
		if (itemId != null) {
			ContentBlock direct = blocks.get(itemId);
			if (direct != null && "function_call".equals(direct._type)) {
				return direct;
			}
			for (ContentBlock block : blocks.values()) {
				if ("function_call".equals(block._type) && itemId.equals(block._itemId)) {
					return block;
				}
			}
		}
		String key = outputIndexToBlockKey.get(outputIndex);
		if (key != null) {
			ContentBlock byOutputIndex = blocks.get(key);
			if (byOutputIndex != null && "function_call".equals(byOutputIndex._type)) {
				return byOutputIndex;
			}
		}
		return null;
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
		String code = parseErrorCode(body);

		ProviderException.Kind kind = switch (code) {
			case "context_length_exceeded" -> ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED;
			case "rate_limit_exceeded" -> ProviderException.Kind.RATE_LIMITED;
			case "authentication_error" -> ProviderException.Kind.AUTHENTICATION;
			default -> switch (status) {
				case 401, 403 -> ProviderException.Kind.AUTHENTICATION;
				case 429 -> ProviderException.Kind.RATE_LIMITED;
				default -> {
					if (status >= 500) {
						yield ProviderException.Kind.OVERLOADED;
					}
					yield ProviderException.Kind.INVALID_REQUEST;
				}
			};
		};

		throw new ProviderException(kind, status, message, retryAfter, null);
	}

	private String parseErrorCode(String jsonBody) {
		try (JsonReader json = jsonReader(jsonBody)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("error".equals(json.nextName())) {
					json.beginObject();
					while (json.hasNext()) {
						if ("code".equals(json.nextName())) {
							switch (json.peek()) {
								case NULL -> { json.skipValue(); return ""; }
								default -> { return json.nextString(); }
							}
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
			// Not valid JSON.
		}
		return "";
	}

	private ProviderException parseStreamError(String data) {
		String code = "";
		String message = "";
		try (JsonReader json = jsonReader(data)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("response".equals(json.nextName())) {
					json.beginObject();
					while (json.hasNext()) {
						if ("error".equals(json.nextName())) {
							json.beginObject();
							while (json.hasNext()) {
								switch (json.nextName()) {
									case "code" -> code = json.nextString();
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
				} else {
					json.skipValue();
				}
			}
			json.endObject();
		} catch (Exception e) {
			message = data;
		}

		ProviderException.Kind kind = switch (code) {
			case "context_length_exceeded" -> ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED;
			case "rate_limit_exceeded" -> ProviderException.Kind.RATE_LIMITED;
			case "server_error" -> ProviderException.Kind.OVERLOADED;
			case "authentication_error" -> ProviderException.Kind.AUTHENTICATION;
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

		String _itemId;

		String _name;

		final StringBuilder _arguments = new StringBuilder();

		ContentBlock(String type) {
			_type = type;
		}
	}
}
