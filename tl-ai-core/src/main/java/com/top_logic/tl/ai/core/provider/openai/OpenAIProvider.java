/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
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
import com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent;
import com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope;
import com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent;
import com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo;
import com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo;
import com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent;
import com.top_logic.tl.ai.core.provider.openai.messages.TextDeltaEvent;

import de.haumacher.msgbuf.json.JsonReader;

/**
 * {@link AbstractStreamingProvider} for the {@code OpenAI} Responses API.
 *
 * <p>Uses {@link HttpClient} for HTTP and msgbuf {@link JsonReader} for JSON stream parsing.
 * Request serialization is delegated to {@link OpenAIResponsesRequestBuilder}.
 * Thread-safe: a single instance handles concurrent streams.</p>
 */
public class OpenAIProvider extends AbstractStreamingProvider {

	private static final String DEFAULT_BASE_URL = "https://api.openai.com";

	/** Creates an {@link OpenAIProvider}. */
	public OpenAIProvider(String apiKey) {
		this(apiKey, DEFAULT_BASE_URL);
	}

	/** Creates an {@link OpenAIProvider}. */
	public OpenAIProvider(String apiKey, String baseUrl) {
		this(apiKey, baseUrl, defaultHttpClient());
	}

	/** Creates an {@link OpenAIProvider}. */
	public OpenAIProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		super(apiKey, baseUrl, httpClient);
	}

	/** Creates an {@link OpenAIProvider} with configurable timeouts and headers. */
	public OpenAIProvider(String apiKey, String baseUrl,
			int connectTimeoutSeconds, int requestTimeoutSeconds, Map<String, String> customHeaders) {
		super(apiKey, baseUrl, defaultHttpClient(connectTimeoutSeconds),
			Duration.ofSeconds(requestTimeoutSeconds), customHeaders);
	}

	@Override
	protected String endpointPath() {
		return "/v1/responses";
	}

	@Override
	protected String buildRequestJson(ProviderRequest request) throws ProviderException {
		return OpenAIResponsesRequestBuilder.buildRequestJson(request);
	}

	@Override
	protected void processStream(InputStream input, StreamHandler handler)
			throws IOException, ProviderException {

		String responseId = null;
		String model = null;
		ContentBlockAccumulator accumulator = new ContentBlockAccumulator();

		SSEReader.Event event;
		try (SSEReader sse = new SSEReader(input)) {
		while ((event = sse.next()) != null) {
			String data = event.data();
			if (data == null || data.isEmpty()) {
				continue;
			}

			OpenAIEventEnvelope envelope = OpenAIEventEnvelope.readOpenAIEventEnvelope(jsonReader(data));
			String eventType = envelope.getType();
			if (eventType == null || eventType.isEmpty()) {
				continue;
			}

			switch (eventType) {
				case "response.created" -> {
					ResponseLifecycleEvent e =
						ResponseLifecycleEvent.readResponseLifecycleEvent(jsonReader(data));
					ResponseInfo r = e.getResponse();
					if (r != null) {
						responseId = r.getId();
						model = r.getModel();
					}
				}

				case "response.output_item.added" -> {
					OutputItemEvent e = OutputItemEvent.readOutputItemEvent(jsonReader(data));
					int outputIndex = e.getOutputIndex();
					OutputItemInfo item = e.getItem();
					if (item != null) {
						String itemType = item.getType();
						String callId = item.getCallId();
						String name = item.getName();
						accumulator.addBlock(outputIndex, itemType, callId, name);

						switch (itemType) {
							case "message" -> handler.onEvent(new StreamEvent.TextStart(outputIndex));
							case "reasoning" -> handler.onEvent(new StreamEvent.ThinkingStart(outputIndex));
							case "function_call" -> handler.onEvent(
								new StreamEvent.ToolCallStart(outputIndex, callId, name));
							default -> { /* Unknown item type — ignore. */ }
						}
					}
				}

				case "response.output_text.delta" -> {
					TextDeltaEvent e = TextDeltaEvent.readTextDeltaEvent(jsonReader(data));
					int outputIndex = e.getOutputIndex();
					String delta = e.getDelta();
					if (delta != null && !delta.isEmpty()) {
						accumulator.appendText(outputIndex, delta);
						handler.onEvent(new StreamEvent.TextDelta(outputIndex, delta));
					}
				}

				case "response.reasoning_text.delta",
						"response.reasoning_summary_text.delta" -> {
					TextDeltaEvent e = TextDeltaEvent.readTextDeltaEvent(jsonReader(data));
					int outputIndex = e.getOutputIndex();
					String delta = e.getDelta();
					if (delta != null && !delta.isEmpty()) {
						accumulator.appendText(outputIndex, delta);
						handler.onEvent(new StreamEvent.ThinkingDelta(outputIndex, delta));
					}
				}

				case "response.function_call_arguments.delta" -> {
					FunctionCallArgsDeltaEvent e =
						FunctionCallArgsDeltaEvent.readFunctionCallArgsDeltaEvent(jsonReader(data));
					int outputIndex = e.getOutputIndex();
					String delta = e.getDelta();
					if (delta != null && !delta.isEmpty()) {
						accumulator.appendArguments(outputIndex, delta);
						handler.onEvent(new StreamEvent.ToolCallDelta(outputIndex, delta));
					}
				}

				case "response.output_item.done" -> {
					OutputItemEvent e = OutputItemEvent.readOutputItemEvent(jsonReader(data));
					int outputIndex = e.getOutputIndex();
					OutputItemInfo item = e.getItem();
					if (item != null) {
						String itemType = item.getType();
						// output_item.done carries authoritative final arguments for function calls.
						if ("function_call".equals(itemType)) {
							String arguments = item.getArguments();
							if (arguments != null && !arguments.isEmpty()) {
								accumulator.setArguments(outputIndex, arguments);
							}
							handler.onEvent(new StreamEvent.ToolCallEnd(outputIndex));
						} else if ("message".equals(itemType)) {
							handler.onEvent(new StreamEvent.TextEnd(outputIndex));
						} else if ("reasoning".equals(itemType)) {
							String encryptedContent = item.getEncryptedContent();
							if (encryptedContent != null && !encryptedContent.isEmpty()) {
								accumulator.setContinuityToken(outputIndex, encryptedContent);
							}
							handler.onEvent(new StreamEvent.ThinkingEnd(outputIndex));
						}
					}
				}

				case "response.completed", "response.incomplete" -> {
					ResponseLifecycleEvent e =
						ResponseLifecycleEvent.readResponseLifecycleEvent(jsonReader(data));
					ResponseInfo r = e.getResponse();
					if (r != null) {
						if (r.getId() != null && !r.getId().isEmpty()) {
							responseId = r.getId();
						}
						if (r.getModel() != null && !r.getModel().isEmpty()) {
							model = r.getModel();
						}
					}
					String finishReason = "response.incomplete".equals(eventType)
						? "max_tokens"
						: mapFinishReason(r != null ? r.getStatus() : "completed");
					TokenUsage usage = extractUsage(r != null ? r.getUsage() : null);
					List<ContentPart> content = accumulator.buildContentParts();
					handler.onEvent(new StreamEvent.MessageComplete(
						responseId, model, finishReason, usage, content));
				}

				case "response.failed" -> {
					ResponseLifecycleEvent e =
						ResponseLifecycleEvent.readResponseLifecycleEvent(jsonReader(data));
					ResponseInfo r = e.getResponse();
					String code = "";
					String message = "Stream failed";
					if (r != null && r.hasError() && r.getError() instanceof Map<?, ?> errorMap) {
						Object c = errorMap.get("code");
						if (c != null) {
							code = c.toString();
						}
						Object m = errorMap.get("message");
						if (m != null) {
							message = m.toString();
						}
					}
					ProviderException.Kind kind = switch (code) {
						case "context_length_exceeded" -> ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED;
						case "rate_limit_exceeded" -> ProviderException.Kind.RATE_LIMITED;
						case "server_error" -> ProviderException.Kind.OVERLOADED;
						case "authentication_error" -> ProviderException.Kind.AUTHENTICATION;
						default -> ProviderException.Kind.UNKNOWN;
					};
					handler.onEvent(new StreamEvent.Error(message, kind));
				}

				default -> { /* Ignore unknown event types. */ }
			}
		}
		}
	}

	/**
	 * Extracts {@link TokenUsage} from the {@code json usage} field of a {@link ResponseInfo}.
	 * Handles null and nested nullable detail objects gracefully.
	 */
	private static TokenUsage extractUsage(Object usageObj) {
		if (!(usageObj instanceof Map<?, ?> usage)) {
			return new TokenUsage(0, 0, 0, 0, 0);
		}
		int input = intValue(usage, "input_tokens");
		int output = intValue(usage, "output_tokens");
		int reasoning = 0;
		int cached = 0;
		if (usage.get("output_tokens_details") instanceof Map<?, ?> outputDetails) {
			reasoning = intValue(outputDetails, "reasoning_tokens");
		}
		if (usage.get("input_tokens_details") instanceof Map<?, ?> inputDetails) {
			cached = intValue(inputDetails, "cached_tokens");
		}
		return new TokenUsage(input, output, reasoning, cached, 0);
	}

	private static int intValue(Map<?, ?> map, String key) {
		Object value = map.get(key);
		if (value instanceof Number n) {
			return n.intValue();
		}
		return 0;
	}

	private String mapFinishReason(String status) {
		return switch (status) {
			case "completed" -> "end_turn";
			case "incomplete" -> "max_tokens";
			case "cancelled" -> "end_turn";
			default -> status;
		};
	}

	@Override
	protected void handleErrorResponse(HttpResponse<InputStream> response) throws ProviderException {
		int status = response.statusCode();
		String body = drainResponseBody(response);

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

}
