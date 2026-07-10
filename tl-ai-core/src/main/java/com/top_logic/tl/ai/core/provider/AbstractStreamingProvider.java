/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.Logger;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Base class for streaming LLM providers that share a common HTTP lifecycle.
 *
 * <p>Implements the template method pattern: {@link #stream} is final and owns the full
 * HTTP request/response/streaming lifecycle. Subclasses provide endpoint-specific details
 * via abstract methods and hooks.</p>
 */
public abstract class AbstractStreamingProvider implements LLMProvider {

	/** The API key for authentication. May be {@code null} for unauthenticated providers. */
	protected final String _apiKey;

	/** The base URL of the provider API (without trailing slash). */
	protected final String _baseUrl;

	/** The HTTP client used for provider requests. */
	protected final HttpClient _httpClient;

	/** The timeout for individual HTTP requests. */
	private final Duration _requestTimeout;

	/** Custom headers applied after all other headers, allowing overrides. */
	private final Map<String, String> _customHeaders;

	/**
	 * Creates a new {@link AbstractStreamingProvider} with default timeout (10 minutes) and no
	 * custom headers.
	 *
	 * @param apiKey
	 *        The API key, may be {@code null} for unauthenticated providers.
	 * @param baseUrl
	 *        The base URL. A trailing slash is stripped.
	 * @param httpClient
	 *        The HTTP client to use.
	 */
	protected AbstractStreamingProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		this(apiKey, baseUrl, httpClient, Duration.ofMinutes(10), Map.of());
	}

	/**
	 * Creates a new {@link AbstractStreamingProvider}.
	 *
	 * @param apiKey
	 *        The API key, may be {@code null} for unauthenticated providers.
	 * @param baseUrl
	 *        The base URL. A trailing slash is stripped.
	 * @param httpClient
	 *        The HTTP client to use.
	 * @param requestTimeout
	 *        The timeout for individual HTTP requests.
	 * @param customHeaders
	 *        Additional headers applied after provider-specific headers, allowing overrides.
	 */
	protected AbstractStreamingProvider(String apiKey, String baseUrl, HttpClient httpClient,
			Duration requestTimeout, Map<String, String> customHeaders) {
		_apiKey = apiKey;
		_baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		_httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null.");
		_requestTimeout = Objects.requireNonNull(requestTimeout, "Request timeout must not be null.");
		_customHeaders = Map.copyOf(Objects.requireNonNull(customHeaders, "Custom headers must not be null."));
	}

	@Override
	public final void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
		Objects.requireNonNull(request, "Request must not be null.");
		Objects.requireNonNull(handler, "Handler must not be null.");

		String body = buildRequestJson(request);
		validateRequest(body);

		String path = endpointPath(request);
		URI uri = URI.create(_baseUrl + path);
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(uri)
			.header("Content-Type", "application/json")
			.header("Accept", acceptHeader())
			.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
			.timeout(request.timeout() != null ? request.timeout() : _requestTimeout);

		addAuthHeaders(requestBuilder);
		addExtraHeaders(requestBuilder);
		addCustomHeaders(requestBuilder);

		HttpRequest httpRequest = requestBuilder.build();

		String dumpLabel = getClass().getSimpleName() + " " + request.model() + " @" + uri.getHost();
		boolean dumpStream = StreamDump.enabled(dumpLabel);
		long startNanos = System.nanoTime();

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

		if (dumpStream) {
			Logger.info("[stream-dump " + dumpLabel + "] response status=" + response.statusCode()
				+ ", headers received +" + (System.nanoTime() - startNanos) / 1_000_000L + "ms",
				AbstractStreamingProvider.class);
		}

		if (response.statusCode() != 200) {
			if (Logger.isDebugEnabled(AbstractStreamingProvider.class)) {
				Logger.debug("LLM provider request failed: " + response.statusCode() + " "
					+ _baseUrl + path + "; request body: " + body, AbstractStreamingProvider.class);
			}
			handleErrorResponse(response);
		}

		try (InputStream responseBody = response.body()) {
			InputStream parseInput =
				dumpStream ? StreamDump.wrap(responseBody, dumpLabel, startNanos) : responseBody;
			processStream(parseInput, handler);
		} catch (ProviderException e) {
			throw e;
		} catch (IOException e) {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0,
				"Stream read error: " + e.getMessage(), e);
		}
	}

	// --- Abstract methods (subclass must implement) ---

	/**
	 * The API endpoint path for the given request, e.g. {@code "/v1/messages"}.
	 *
	 * <p>The {@code request} parameter is provided so that providers whose endpoint encodes the
	 * model name (e.g. Azure <code>OpenAI</code>) can compute the path dynamically. Simple providers that use
	 * a fixed path may ignore it.</p>
	 *
	 * @param request
	 *        The request about to be sent. Never {@code null}.
	 */
	protected abstract String endpointPath(ProviderRequest request);

	/**
	 * Builds the provider-specific JSON request body.
	 */
	protected abstract String buildRequestJson(ProviderRequest request) throws ProviderException;

	/**
	 * Parses the SSE stream and emits events to the handler.
	 */
	protected abstract void processStream(InputStream input, StreamHandler handler)
		throws IOException, ProviderException;

	/**
	 * Handles a non-200 HTTP response by throwing an appropriate {@link ProviderException}.
	 *
	 * <p>Implementations can use {@link #drainResponseBody} and {@link #parseRetryAfter} as
	 * helpers.</p>
	 */
	protected abstract void handleErrorResponse(HttpResponse<InputStream> response)
		throws ProviderException;

	// --- Hooks with defaults ---

	/**
	 * Validates the request body before sending. Default is a no-op.
	 *
	 * <p>Override to add provider-specific validation (e.g. body size limits).</p>
	 */
	protected void validateRequest(String body) throws ProviderException {
		// No-op by default.
	}

	/**
	 * Adds authentication headers. Default adds {@code Authorization: Bearer} if the API key
	 * is set. Override for providers that use a different auth scheme.
	 */
	protected void addAuthHeaders(HttpRequest.Builder builder) {
		if (_apiKey != null && !_apiKey.isBlank()) {
			builder.header("Authorization", "Bearer " + _apiKey);
		}
	}

	/**
	 * Adds extra headers beyond authentication. Default is a no-op.
	 *
	 * <p>Override to add provider-specific headers (e.g. API version).</p>
	 */
	protected void addExtraHeaders(HttpRequest.Builder builder) {
		// No-op by default.
	}

	/**
	 * The value for the {@code Accept} request header.
	 *
	 * <p>Defaults to {@code "text/event-stream"} for SSE-based providers. Override to specify a
	 * different media type (e.g. {@code "application/vnd.amazon.eventstream"} for Bedrock).</p>
	 */
	protected String acceptHeader() {
		return "text/event-stream";
	}

	/**
	 * Adds custom headers configured at construction time. Applied after all other headers so they
	 * can override provider-specific values.
	 */
	private void addCustomHeaders(HttpRequest.Builder builder) {
		for (Map.Entry<String, String> entry : _customHeaders.entrySet()) {
			builder.setHeader(entry.getKey(), entry.getValue());
		}
	}

	// --- Shared utilities ---

	/**
	 * Creates a new default {@link HttpClient} with a 30-second connect timeout.
	 */
	protected static HttpClient defaultHttpClient() {
		return defaultHttpClient(30);
	}

	/**
	 * Creates a new default {@link HttpClient} with the given connect timeout.
	 *
	 * @param connectTimeoutSeconds
	 *        The connect timeout in seconds.
	 */
	protected static HttpClient defaultHttpClient(int connectTimeoutSeconds) {
		return HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
			.build();
	}

	/**
	 * Parses the {@code retry-after} header as a duration in seconds.
	 *
	 * <p>When {@code retry-after} is absent or unparseable, falls back to the
	 * {@code ratelimit-reset} header (also interpreted as whole seconds). This covers gateways
	 * that send only {@code ratelimit-reset} without a {@code retry-after} header.</p>
	 *
	 * @return the duration, or {@code null} if neither header is present or parseable.
	 */
	protected Duration parseRetryAfter(java.net.http.HttpHeaders headers) {
		Duration primary = headers.firstValue("retry-after")
			.map(value -> {
				try {
					return Duration.ofSeconds(Long.parseLong(value));
				} catch (NumberFormatException e) {
					return null;
				}
			})
			.orElse(null);
		if (primary != null) {
			return primary;
		}
		return headers.firstValue("ratelimit-reset")
			.map(value -> {
				try {
					return Duration.ofSeconds(Long.parseLong(value));
				} catch (NumberFormatException e) {
					return null;
				}
			})
			.orElse(null);
	}

	/**
	 * Creates a {@link JsonReader} from a JSON string.
	 */
	protected static JsonReader jsonReader(String json) {
		return new JsonReader(new StringR(json));
	}

	/**
	 * Reads the full response body as a UTF-8 string. Returns an empty string on I/O failure.
	 */
	protected static String drainResponseBody(HttpResponse<InputStream> response) {
		try (InputStream is = response.body()) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return "";
		}
	}
}
