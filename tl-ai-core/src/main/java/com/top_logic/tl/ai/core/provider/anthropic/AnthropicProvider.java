/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.AbstractStreamingProvider;
import com.top_logic.tl.ai.core.provider.ContentBlockAccumulator;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.SSEReader;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent;
import com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo;
import com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent;
import com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent;
import com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo;
import com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent;
import com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo;
import com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo;
import com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent;
import com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent;
import com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo;
import com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo;

import de.haumacher.msgbuf.json.JsonReader;

/**
 * {@link AbstractStreamingProvider} for the Anthropic Messages API.
 *
 * <p>Uses {@link HttpClient} for HTTP and msgbuf {@link JsonReader} for JSON. Serialization is
 * delegated to {@link AnthropicRequestBuilder}.
 * Thread-safe: a single instance handles concurrent streams.</p>
 */
public class AnthropicProvider extends AbstractStreamingProvider {

	private static final String DEFAULT_BASE_URL = "https://api.anthropic.com";

	private static final String DEFAULT_API_VERSION = "2023-06-01";

	private final String _apiVersion;

	/** Creates an {@link AnthropicProvider}. */
	public AnthropicProvider(String apiKey) {
		this(apiKey, DEFAULT_BASE_URL, DEFAULT_API_VERSION);
	}

	/** Creates an {@link AnthropicProvider}. */
	public AnthropicProvider(String apiKey, String baseUrl, String apiVersion) {
		this(apiKey, baseUrl, apiVersion, defaultHttpClient());
	}

	/** Creates an {@link AnthropicProvider}. */
	public AnthropicProvider(String apiKey, String baseUrl, String apiVersion, HttpClient httpClient) {
		super(apiKey, baseUrl, httpClient);
		_apiVersion = Objects.requireNonNull(apiVersion, "API version must not be null.");
	}

	/** Creates an {@link AnthropicProvider} with configurable timeouts and headers. */
	public AnthropicProvider(String apiKey, String baseUrl, String apiVersion,
			int connectTimeoutSeconds, int requestTimeoutSeconds, Map<String, String> customHeaders) {
		super(apiKey, baseUrl, defaultHttpClient(connectTimeoutSeconds),
			Duration.ofSeconds(requestTimeoutSeconds), customHeaders);
		_apiVersion = Objects.requireNonNull(apiVersion, "API version must not be null.");
	}

	@Override
	protected String endpointPath(ProviderRequest request) {
		return "/v1/messages";
	}

	@Override
	protected void addAuthHeaders(HttpRequest.Builder builder) {
		if (_apiKey != null && !_apiKey.isBlank()) {
			builder.header("x-api-key", _apiKey);
		}
	}

	@Override
	protected void addExtraHeaders(HttpRequest.Builder builder) {
		builder.header("anthropic-version", _apiVersion);
	}

	@Override
	protected String buildRequestJson(ProviderRequest request) throws ProviderException {
		return AnthropicRequestBuilder.buildRequestJson(request);
	}

	@Override
	protected void processStream(InputStream input, StreamHandler handler)
			throws IOException, ProviderException {

		String messageId = null;
		String model = null;
		String finishReason = null;
		int inputTokens = 0, outputTokens = 0, cacheReadTokens = 0, cacheWriteTokens = 0;
		ContentBlockAccumulator accumulator = new ContentBlockAccumulator();

		handler.onEvent(new StreamEvent.StreamStart());

		SSEReader.Event event;
		try (SSEReader sse = new SSEReader(input)) {
		while ((event = sse.next()) != null) {
			String data = event.data();
			if (data == null || data.isEmpty()) continue;

			switch (event.event()) {
				case "message_start" -> {
					MessageStartEvent e = MessageStartEvent.readMessageStartEvent(jsonReader(data));
					MessageInfo msg = e.getMessage();
					messageId = msg.getId();
					model = msg.getModel();
					UsageInfo u = msg.getUsage();
					if (u != null) {
						inputTokens = u.getInputTokens();
						outputTokens = u.getOutputTokens();
						cacheReadTokens = u.getCacheReadInputTokens();
						cacheWriteTokens = u.getCacheCreationInputTokens();
					}
				}

				case "content_block_start" -> {
					ContentBlockStartEvent e = ContentBlockStartEvent.readContentBlockStartEvent(jsonReader(data));
					ContentBlockInfo block = e.getContentBlock();
					int idx = e.getIndex();
					accumulator.addBlock(idx, block.getType(), block.getId(), block.getName());

					switch (block.getType()) {
						case "text" -> handler.onEvent(new StreamEvent.TextStart(idx));
						case "thinking" -> handler.onEvent(new StreamEvent.ThinkingStart(idx));
						case "tool_use" -> {
							if (block.getId() != null && block.getName() != null) {
								handler.onEvent(new StreamEvent.ToolCallStart(idx, block.getId(), block.getName()));
							}
						}
					}
				}

				case "content_block_delta" -> {
					ContentBlockDeltaEvent e = ContentBlockDeltaEvent.readContentBlockDeltaEvent(jsonReader(data));
					DeltaInfo delta = e.getDelta();
					int idx = e.getIndex();

					switch (delta.getType()) {
						case "text_delta" -> {
							if (delta.getText() != null) {
								accumulator.appendText(idx, delta.getText());
								handler.onEvent(new StreamEvent.TextDelta(idx, delta.getText()));
							}
						}
						case "thinking_delta" -> {
							String text = delta.getThinking();
							if (text != null && !text.isEmpty()) {
								accumulator.appendText(idx, text);
								handler.onEvent(new StreamEvent.ThinkingDelta(idx, text));
							}
						}
						case "input_json_delta" -> {
							if (delta.getPartialJson() != null) {
								accumulator.appendArguments(idx, delta.getPartialJson());
								handler.onEvent(new StreamEvent.ToolCallDelta(idx, delta.getPartialJson()));
							}
						}
						case "signature_delta" -> {
							String sig = delta.getSignature();
							if (sig != null && !sig.isEmpty()) {
								accumulator.setContinuityToken(idx, sig);
							}
						}
					}
				}

				case "content_block_stop" -> {
					ContentBlockStopEvent e = ContentBlockStopEvent.readContentBlockStopEvent(jsonReader(data));
					int idx = e.getIndex();
					String blockType = accumulator.getBlockType(idx);
					if (blockType != null) {
						switch (blockType) {
							case "text" -> handler.onEvent(new StreamEvent.TextEnd(idx));
							case "thinking" -> handler.onEvent(new StreamEvent.ThinkingEnd(idx));
							case "tool_use" -> handler.onEvent(new StreamEvent.ToolCallEnd(idx));
						}
					}
				}

				case "message_delta" -> {
					MessageDeltaEvent e = MessageDeltaEvent.readMessageDeltaEvent(jsonReader(data));
					MessageDeltaInfo d = e.getDelta();
					if (d != null && d.getStopReason() != null && !d.getStopReason().isEmpty()) {
						finishReason = d.getStopReason();
					}
					UsageInfo u = e.getUsage();
					if (u != null) {
						outputTokens += u.getOutputTokens();
					}
				}

				case "message_stop" -> {
					handler.onEvent(new StreamEvent.MessageComplete(
						messageId, model,
						finishReason != null ? finishReason : "end_turn",
						new TokenUsage(inputTokens, outputTokens, 0, cacheReadTokens, cacheWriteTokens),
						accumulator.buildContentParts()));
				}

				case "error" -> {
					StreamErrorEvent e = StreamErrorEvent.readStreamErrorEvent(jsonReader(data));
					StreamErrorInfo err = e.getError();
					String errType = err != null ? err.getType() : "";
					String errMsg = err != null ? err.getMessage() : data;
					handler.onEvent(new StreamEvent.Error(errMsg, classifyError(errType, 0)));
				}

				default -> { /* Ignore unknown event types. */ }
			}
		}
		}
	}

	@Override
	protected void handleErrorResponse(HttpResponse<InputStream> response) throws ProviderException {
		int status = response.statusCode();
		String body = drainResponseBody(response);

		Duration retryAfter = parseRetryAfter(response.headers());
		String requestId = response.headers().firstValue("request-id").orElse(null);
		String[] error = parseError(body);
		ProviderException.Kind kind = classifyError(error[0], status);

		throw new ProviderException(kind, status, error[1], retryAfter, requestId, null);
	}

	/**
	 * Parses both {@code error.type} and {@code error.message} from an Anthropic error JSON body.
	 *
	 * @return two-element array: {@code [type, message]}. Empty strings if parsing fails.
	 */
	private String[] parseError(String jsonBody) {
		String type = "";
		String message = "";
		try (JsonReader json = jsonReader(jsonBody)) {
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
			if (message.isEmpty()) {
				message = jsonBody;
			}
		}
		return new String[] { type, message };
	}

	/**
	 * Classifies an Anthropic API error using the {@code error.type} string and HTTP status code.
	 * The type field is authoritative; the status code is the fallback.
	 */
	private static ProviderException.Kind classifyError(String errorType, int statusCode) {
		ProviderException.Kind kind = switch (errorType) {
			case "authentication_error", "permission_error" -> ProviderException.Kind.AUTHENTICATION;
			case "billing_error" -> ProviderException.Kind.BILLING;
			case "rate_limit_error" -> ProviderException.Kind.RATE_LIMITED;
			case "overloaded_error" -> ProviderException.Kind.OVERLOADED;
			case "api_error" -> ProviderException.Kind.OVERLOADED;
			case "timeout_error" -> ProviderException.Kind.OVERLOADED;
			case "request_too_large" -> ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED;
			case "not_found_error" -> ProviderException.Kind.INVALID_REQUEST;
			case "invalid_request_error" -> ProviderException.Kind.INVALID_REQUEST;
			default -> null;
		};
		if (kind != null) {
			return kind;
		}
		return switch (statusCode) {
			case 401, 403 -> ProviderException.Kind.AUTHENTICATION;
			case 402 -> ProviderException.Kind.BILLING;
			case 413 -> ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED;
			case 429 -> ProviderException.Kind.RATE_LIMITED;
			case 504 -> ProviderException.Kind.OVERLOADED;
			case 529 -> ProviderException.Kind.OVERLOADED;
			default -> {
				if (statusCode >= 500) {
					yield ProviderException.Kind.OVERLOADED;
				}
				yield ProviderException.Kind.INVALID_REQUEST;
			}
		};
	}

}
