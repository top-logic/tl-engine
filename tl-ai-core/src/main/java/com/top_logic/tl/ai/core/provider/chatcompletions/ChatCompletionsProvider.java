/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.chatcompletions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.AbstractStreamingProvider;
import com.top_logic.tl.ai.core.provider.AuthScheme;
import com.top_logic.tl.ai.core.provider.ContentBlockAccumulator;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.SSEReader;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * {@link AbstractStreamingProvider} base for providers that use the <code>OpenAI</code> Chat Completions wire
 * protocol (SSE streaming, {@code /v1/chat/completions} endpoint shape).
 *
 * <p>Covers Mistral, <code>xAI</code>, Moonshot, ZAI, local servers, and any <code>OpenAI</code>-compatible endpoint.
 * Subclasses can override {@link #toolChoiceOverride(ProviderRequest)} to inject
 * provider-specific {@code tool_choice} values, and can override {@link #endpointPath} for
 * providers that encode the model name in the URL (e.g. Azure <code>OpenAI</code>).</p>
 *
 * <p>Uses {@link HttpClient} for HTTP and msgbuf {@link JsonWriter}/{@link JsonReader} for JSON.
 * Thread-safe: a single instance handles concurrent streams.</p>
 */
public class ChatCompletionsProvider extends AbstractStreamingProvider {

	/** Default endpoint path used when no path template is configured. */
	private static final String DEFAULT_PATH = "/v1/chat/completions";

	/**
	 * Path template for the endpoint, may contain {@code {model}} as a placeholder.
	 * Defaults to {@value #DEFAULT_PATH}.
	 */
	private final String _pathTemplate;

	/**
	 * Additional query parameters appended to {@link #_pathTemplate} (e.g. {@code api-version}).
	 * Empty map means no query string is appended.
	 */
	private final Map<String, String> _queryParams;

	/**
	 * Authentication scheme controlling how {@link #_apiKey} is sent.
	 * Defaults to {@link AuthScheme#BEARER}.
	 */
	private final AuthScheme _authScheme;

	/**
	 * JSON field name carrying the output-token limit.
	 * Defaults to {@link TokenLimitParam#MAX_TOKENS}.
	 */
	private final TokenLimitParam _tokenLimitParam;

	/**
	 * Creates a {@link ChatCompletionsProvider}.
	 *
	 * @param apiKey
	 *        The API key, may be {@code null} for unauthenticated providers.
	 * @param baseUrl
	 *        The base URL of the provider API (without trailing slash).
	 */
	public ChatCompletionsProvider(String apiKey, String baseUrl) {
		this(apiKey, baseUrl, defaultHttpClient());
	}

	/**
	 * Creates a {@link ChatCompletionsProvider}.
	 *
	 * @param apiKey
	 *        The API key, may be {@code null} for unauthenticated providers.
	 * @param baseUrl
	 *        The base URL of the provider API (without trailing slash).
	 * @param httpClient
	 *        The HTTP client to use.
	 */
	public ChatCompletionsProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		super(apiKey, baseUrl, httpClient);
		_pathTemplate = DEFAULT_PATH;
		_queryParams = Map.of();
		_authScheme = AuthScheme.BEARER;
		_tokenLimitParam = TokenLimitParam.MAX_TOKENS;
	}

	/**
	 * Creates a {@link ChatCompletionsProvider} with configurable timeouts and headers.
	 *
	 * @param apiKey
	 *        The API key, may be {@code null} for unauthenticated providers.
	 * @param baseUrl
	 *        The base URL of the provider API (without trailing slash).
	 * @param connectTimeoutSeconds
	 *        The TCP connect timeout in seconds.
	 * @param requestTimeoutSeconds
	 *        The HTTP request timeout in seconds.
	 * @param customHeaders
	 *        Additional headers applied after provider-specific headers, allowing overrides.
	 */
	public ChatCompletionsProvider(String apiKey, String baseUrl,
			int connectTimeoutSeconds, int requestTimeoutSeconds, Map<String, String> customHeaders) {
		this(apiKey, baseUrl, connectTimeoutSeconds, requestTimeoutSeconds, customHeaders,
			DEFAULT_PATH, Map.of(), AuthScheme.BEARER);
	}

	/**
	 * Creates a {@link ChatCompletionsProvider} with full control over the endpoint path, query
	 * parameters, and authentication scheme.
	 *
	 * <p>Use this constructor to configure Azure-<code>OpenAI</code> deployment paths or any other provider
	 * that embeds the model name in the URL or uses a non-standard auth header.</p>
	 *
	 * @param apiKey
	 *        The API key, may be {@code null} for unauthenticated providers.
	 * @param baseUrl
	 *        The base URL of the provider API (without trailing slash).
	 * @param connectTimeoutSeconds
	 *        The TCP connect timeout in seconds.
	 * @param requestTimeoutSeconds
	 *        The HTTP request timeout in seconds.
	 * @param customHeaders
	 *        Additional headers applied after provider-specific headers, allowing overrides.
	 * @param pathTemplate
	 *        Endpoint path template, e.g. {@code "/openai/deployments/{model}/chat/completions"}.
	 *        The placeholder {@code {model}} is replaced with the URL-encoded model name at
	 *        request time. Use {@code "/v1/chat/completions"} for the standard path.
	 * @param queryParams
	 *        Additional query parameters appended to the path, e.g.
	 *        {@code Map.of("api-version", "2024-10-21")}. May be empty.
	 * @param authScheme
	 *        The authentication scheme to use for the {@code api-key} or {@code Authorization}
	 *        header.
	 */
	public ChatCompletionsProvider(String apiKey, String baseUrl,
			int connectTimeoutSeconds, int requestTimeoutSeconds, Map<String, String> customHeaders,
			String pathTemplate, Map<String, String> queryParams, AuthScheme authScheme) {
		this(apiKey, baseUrl, connectTimeoutSeconds, requestTimeoutSeconds, customHeaders,
			pathTemplate, queryParams, authScheme, TokenLimitParam.MAX_TOKENS);
	}

	/**
	 * Creates a {@link ChatCompletionsProvider} with full control over the endpoint path, query
	 * parameters, authentication scheme, and output-token limit field name.
	 *
	 * <p>Use this constructor for Azure <code>OpenAI</code> / <code>OpenAI</code> deployments serving GPT-5 or o-series
	 * reasoning models, which require {@link TokenLimitParam#MAX_COMPLETION_TOKENS} instead of the
	 * legacy {@code max_tokens} field.</p>
	 *
	 * @param tokenLimitParam
	 *        Which JSON field the output-token limit is written under. See {@link TokenLimitParam}.
	 *        Other parameters are as documented on the eight-argument constructor.
	 */
	public ChatCompletionsProvider(String apiKey, String baseUrl,
			int connectTimeoutSeconds, int requestTimeoutSeconds, Map<String, String> customHeaders,
			String pathTemplate, Map<String, String> queryParams, AuthScheme authScheme,
			TokenLimitParam tokenLimitParam) {
		super(apiKey, baseUrl, defaultHttpClient(connectTimeoutSeconds),
			Duration.ofSeconds(requestTimeoutSeconds), customHeaders);
		_pathTemplate = Objects.requireNonNullElse(pathTemplate, DEFAULT_PATH);
		_queryParams = Map.copyOf(Objects.requireNonNullElse(queryParams, Map.of()));
		_authScheme = Objects.requireNonNullElse(authScheme, AuthScheme.BEARER);
		_tokenLimitParam = Objects.requireNonNullElse(tokenLimitParam, TokenLimitParam.MAX_TOKENS);
	}

	@Override
	protected String endpointPath(ProviderRequest request) {
		// URLEncoder is form-encoding (space -> '+'), which is technically not RFC 3986 path
		// encoding. Model identifiers and api-version values contain no spaces or reserved
		// characters, so the output is identical to strict percent-encoding for these inputs.
		String path = _pathTemplate.replace(
			"{model}", URLEncoder.encode(request.model(), StandardCharsets.UTF_8));
		if (_queryParams.isEmpty()) {
			return path;
		}
		StringBuilder sb = new StringBuilder(path).append('?');
		boolean first = true;
		for (Map.Entry<String, String> entry : _queryParams.entrySet()) {
			if (!first) {
				sb.append('&');
			}
			sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
				.append('=')
				.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
			first = false;
		}
		return sb.toString();
	}

	@Override
	protected void addAuthHeaders(HttpRequest.Builder builder) {
		if (_authScheme == AuthScheme.API_KEY) {
			if (_apiKey != null && !_apiKey.isBlank()) {
				builder.header("api-key", _apiKey);
			}
		} else {
			super.addAuthHeaders(builder);
		}
	}

	@Override
	protected String buildRequestJson(ProviderRequest request) throws ProviderException {
		return ChatCompletionsRequestBuilder.buildRequestJson(
			request, toolChoiceOverride(request), _tokenLimitParam, includeUsage());
	}

	/**
	 * Whether to set {@code stream_options.include_usage} so the server reports token usage in the
	 * stream.
	 *
	 * <p>Defaults to {@code true}: <code>OpenAI</code>, Azure <code>OpenAI</code> and <code>xAI</code> omit usage from streamed responses
	 * unless asked. Override to {@code false} for providers that report usage unconditionally or
	 * reject the {@code stream_options} field (e.g. Mistral).</p>
	 *
	 * @return {@code true} to request a streaming usage report.
	 */
	protected boolean includeUsage() {
		return true;
	}

	/**
	 * Returns a provider-specific {@code tool_choice} override string, or {@code null} to let
	 * {@link ChatCompletionsRequestBuilder} apply standard <code>OpenAI</code> semantics.
	 *
	 * <p>Override when the provider uses non-standard values, e.g. Mistral maps
	 * {@code REQUIRED} to {@code "any"} and always sets a default when tools are present.</p>
	 *
	 * @param request
	 *        The request about to be sent. Never {@code null}.
	 * @return the override string (e.g. {@code "any"}), or {@code null} for standard behaviour.
	 */
	protected String toolChoiceOverride(ProviderRequest request) {
		return null;
	}

	@Override
	protected void processStream(InputStream input, StreamHandler handler)
			throws IOException, ProviderException {

		String messageId = null;
		String model = null;
		String finishReason = null;
		int inputTokens = 0;
		int outputTokens = 0;
		int cachedTokens = 0;
		DeltaState state = new DeltaState();

		handler.onEvent(new StreamEvent.StreamStart());

		SSEReader.Event event;
		try (SSEReader sse = new SSEReader(input)) {
			while ((event = sse.next()) != null) {
				String data = event.data();
				if (data == null || data.isEmpty()) {
					continue;
				}

				if ("[DONE]".equals(data)) {
					closeOpenBlocks(state, handler);
					List<ContentPart> content = state._accumulator.buildContentParts();
					TokenUsage usage = new TokenUsage(inputTokens, outputTokens, 0, cachedTokens, 0);
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
											case "delta" -> processDelta(json, state, handler);
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
								// With stream_options.include_usage, intermediate chunks carry
								// "usage": null and only the final chunk populates it.
								if (json.peek() == JsonToken.NULL) {
									json.skipValue();
								} else {
									json.beginObject();
									while (json.hasNext()) {
										switch (json.nextName()) {
											case "prompt_tokens" -> inputTokens = json.nextInt();
											case "completion_tokens" -> outputTokens = json.nextInt();
											// OpenAI-compatible usage reports cached prefix tokens in
											// prompt_tokens_details.cached_tokens (read only; no write count).
											// Providers that don't cache omit it, leaving cacheRead at 0.
											case "prompt_tokens_details" -> {
												json.beginObject();
												while (json.hasNext()) {
													if ("cached_tokens".equals(json.nextName())) {
														cachedTokens = json.nextInt();
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
								}
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
				"Failed to process Chat Completions stream response: " + e.getMessage(), e);
		}
	}

	/**
	 * Processes a single {@code delta} object from the Chat Completions stream.
	 *
	 * <p>Text fragments are appended to a single text block. Tool calls are accumulated by their
	 * protocol {@code index}: the first delta for an index carries {@code id} and {@code name} and
	 * opens the block; later deltas for the same index carry only {@code arguments} fragments that
	 * are concatenated. This matches the <code>OpenAI</code> / Azure <code>OpenAI</code> streaming wire format.</p>
	 */
	private void processDelta(JsonReader json, DeltaState state, StreamHandler handler)
			throws IOException {

		json.beginObject();
		while (json.hasNext()) {
			switch (json.nextName()) {
				case "content" -> {
					String text = normalizeContent(json);
					if (text != null && !text.isEmpty()) {
						if (!state._textOpen) {
							state._textContentIndex = state._nextContentIndex++;
							state._accumulator.addBlock(state._textContentIndex, "text", null, null);
							handler.onEvent(new StreamEvent.TextStart(state._textContentIndex));
							state._textOpen = true;
						}
						state._accumulator.appendText(state._textContentIndex, text);
						handler.onEvent(new StreamEvent.TextDelta(state._textContentIndex, text));
					}
				}
				case "tool_calls" -> processToolCalls(json, state, handler);
				default -> json.skipValue();
			}
		}
		json.endObject();
	}

	/**
	 * Processes a {@code tool_calls} array from a single delta, accumulating fragments into the tool
	 * blocks tracked by {@link DeltaState}.
	 *
	 * <p>Only the first delta for a given protocol {@code index} carries {@code id} and
	 * {@code name}; subsequent deltas carry just {@code arguments} fragments and are matched to the
	 * already-opened block by index.</p>
	 */
	private void processToolCalls(JsonReader json, DeltaState state, StreamHandler handler)
			throws IOException {

		json.beginArray();
		int position = 0;
		while (json.hasNext()) {
			Integer protocolIndex = null;
			String id = null;
			String name = null;
			String arguments = null;

			json.beginObject();
			while (json.hasNext()) {
				switch (json.nextName()) {
					case "index" -> protocolIndex = json.nextInt();
					case "id" -> {
						if (json.peek() == JsonToken.NULL) {
							json.skipValue();
						} else {
							id = json.nextString();
						}
					}
					case "function" -> {
						json.beginObject();
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "name" -> {
									if (json.peek() == JsonToken.NULL) {
										json.skipValue();
									} else {
										name = json.nextString();
									}
								}
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

			// Streaming chunks always carry "index"; fall back to array position defensively.
			int protocol = protocolIndex != null ? protocolIndex.intValue() : position;
			position++;

			Integer contentIndex = state._toolContentIndex.get(protocol);
			if (contentIndex == null) {
				// First delta for this tool call must carry id and name; without them there is no
				// block to open, so the fragment is dropped (should not happen per protocol).
				if (id == null || name == null) {
					continue;
				}
				contentIndex = Integer.valueOf(state._nextContentIndex++);
				state._toolContentIndex.put(protocol, contentIndex);
				state._accumulator.addBlock(contentIndex.intValue(), "tool_use", id, name);
				handler.onEvent(new StreamEvent.ToolCallStart(contentIndex.intValue(), id, name));
			}

			if (arguments != null && !arguments.isEmpty()) {
				state._accumulator.appendArguments(contentIndex.intValue(), arguments);
				handler.onEvent(new StreamEvent.ToolCallDelta(contentIndex.intValue(), arguments));
			}
		}
		json.endArray();
	}

	/**
	 * Emits the closing lifecycle events for every block opened during the stream. Chat Completions
	 * has no per-block end signal, so all open blocks are closed when the stream terminates.
	 */
	private static void closeOpenBlocks(DeltaState state, StreamHandler handler) {
		if (state._textOpen) {
			handler.onEvent(new StreamEvent.TextEnd(state._textContentIndex));
		}
		for (Integer toolContentIndex : state._toolContentIndex.values()) {
			handler.onEvent(new StreamEvent.ToolCallEnd(toolContentIndex.intValue()));
		}
	}

	/**
	 * Mutable per-stream state for Chat Completions delta processing: the content accumulator, the
	 * next free content index, the single text block, and the mapping from a tool call's protocol
	 * {@code index} to its assigned content-block index.
	 */
	private static final class DeltaState {

		final ContentBlockAccumulator _accumulator = new ContentBlockAccumulator();

		int _nextContentIndex = 0;

		int _textContentIndex = -1;

		boolean _textOpen = false;

		final LinkedHashMap<Integer, Integer> _toolContentIndex = new LinkedHashMap<>();
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
