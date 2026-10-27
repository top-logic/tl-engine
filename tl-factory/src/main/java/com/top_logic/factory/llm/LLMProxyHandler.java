/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Proxy of the Anthropic Messages API for the workspace containers, independent of the server it
 * runs in: the {@link LLMProxyListener} on a port of its own or the {@link LLMProxyServlet} in the
 * web application, both at {@link #PATH}.
 *
 * <p>
 * Only {@code POST} of {@link #MESSAGES_PATH} and {@link #COUNT_TOKENS_PATH} (below {@link #PATH})
 * is forwarded, with the query string; everything else is answered with 404. A request from a
 * client address the {@link LLMProxy} does not accept is answered with 403 - the address is the one
 * of the connection ({@link LLMProxyExchange#remoteAddress()}), never a header such as
 * {@code X-Forwarded-For}. A request must carry a workspace token as bearer token in
 * {@link #AUTHORIZATION}, which the {@link LLMProxy} {@link LLMProxy#admit(String, String) admits}:
 * an invalid token is answered with 401, a token used from another address than its first request
 * with 403, a token over its rate limit with 429 and {@link #RETRY_AFTER}.
 * </p>
 *
 * <p>
 * The forwarded request carries the headers of the client except the credentials
 * ({@link #AUTHORIZATION}, {@link #X_API_KEY}), cookies, {@code Host}, the hop-by-hop headers and
 * {@code Accept-Encoding}, so that the answer comes uncompressed and can be streamed. The proxy adds
 * the credentials of the {@link LLMUpstream} - the provider's API key in {@link #X_API_KEY} -, and
 * sets {@link #ANTHROPIC_VERSION} to its version if the client sent none; a client's {@link #ANTHROPIC_VERSION} and {@code anthropic-beta}
 * are kept. Status, headers and body of the answer are passed back, the body flushed piece by piece
 * as it arrives, so that a server-sent event stream reaches the client while it is produced.
 * </p>
 *
 * <p>
 * The API key appears neither in a log nor in an answer of the proxy.
 * </p>
 */
public class LLMProxyHandler {

	/**
	 * Path the proxy is reached at, in the web application and on its own port.
	 */
	public static final String PATH = "/llm-proxy";

	/**
	 * Path below {@link #PATH} of the Messages API.
	 */
	public static final String MESSAGES_PATH = "/v1/messages";

	/**
	 * Path below {@link #PATH} of the token counting of the Messages API.
	 */
	public static final String COUNT_TOKENS_PATH = "/v1/messages/count_tokens";

	/**
	 * Header carrying the workspace token as {@link #BEARER_PREFIX bearer token}.
	 */
	public static final String AUTHORIZATION = "Authorization";

	/**
	 * Prefix of the value of {@link #AUTHORIZATION} before the token.
	 */
	public static final String BEARER_PREFIX = "Bearer ";

	/**
	 * Header carrying the API key of the Anthropic API.
	 */
	public static final String X_API_KEY = "x-api-key";

	/**
	 * Header naming the version of the Anthropic API.
	 */
	public static final String ANTHROPIC_VERSION = "anthropic-version";

	/**
	 * Header of a 429 answer naming the seconds after which the next request is admitted.
	 */
	public static final String RETRY_AFTER = "Retry-After";

	/**
	 * Header naming the content type of a body.
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * Content type of the error answers of the proxy.
	 */
	public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	/**
	 * Type of the error answering a request without valid token, as in the Anthropic API.
	 */
	public static final String AUTHENTICATION_ERROR = "authentication_error";

	/**
	 * Type of the error answering a request from an address that is not accepted, as in the
	 * Anthropic API.
	 */
	public static final String PERMISSION_ERROR = "permission_error";

	/**
	 * Type of the error answering a request over the rate limit of its token, as in the Anthropic
	 * API.
	 */
	public static final String RATE_LIMIT_ERROR = "rate_limit_error";

	/**
	 * Type of the error answering a request the proxy does not forward.
	 */
	public static final String NOT_FOUND_ERROR = "not_found_error";

	/**
	 * Type of the error answering a request the proxy cannot forward.
	 */
	public static final String API_ERROR = "api_error";

	/**
	 * Status of an answer to a request over the rate limit.
	 */
	public static final int SC_TOO_MANY_REQUESTS = 429;

	private static final int SC_FORBIDDEN = 403;

	private static final int SC_UNAUTHORIZED = 401;

	private static final int SC_NOT_FOUND = 404;

	private static final int SC_BAD_GATEWAY = 502;

	private static final int SC_SERVICE_UNAVAILABLE = 503;

	private static final int SC_GATEWAY_TIMEOUT = 504;

	private static final String POST = "POST";

	private static final Set<String> FORWARDED_PATHS = Set.of(MESSAGES_PATH, COUNT_TOKENS_PATH);

	/**
	 * Request headers (lower case) that are not forwarded: credentials, cookies of the control
	 * application, hop-by-hop headers, headers the HTTP client sets itself, and the encoding, so
	 * that the answer can be streamed as it is.
	 */
	private static final Set<String> DROPPED_REQUEST_HEADERS = Set.of(
		AUTHORIZATION.toLowerCase(Locale.ROOT), X_API_KEY, "cookie", "host", "content-length", "expect", "accept-encoding",
		"connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "proxy-connection", "te",
		"trailer", "transfer-encoding", "upgrade", "http2-settings");

	/**
	 * Response headers (lower case) that are not passed back: hop-by-hop headers, the length (the
	 * server chooses the transfer), and cookies of the upstream.
	 */
	private static final Set<String> DROPPED_RESPONSE_HEADERS = Set.of(
		"content-length", "set-cookie", "connection", "keep-alive", "proxy-authenticate",
		"proxy-authorization", "proxy-connection", "te", "trailer", "transfer-encoding", "upgrade");

	private static final String PSEUDO_HEADER_PREFIX = ":";

	private static final int BUFFER_SIZE = 8 * 1024;

	private final Supplier<LLMProxy> _proxy;

	private HttpClient _client;

	private Duration _clientConnectTimeout;

	/**
	 * Creates a {@link LLMProxyHandler}.
	 *
	 * @param proxy
	 *        Supplier of the {@link LLMProxy} asked for each request; it may supply {@code null}
	 *        when there is none, which answers requests with 404.
	 */
	public LLMProxyHandler(Supplier<LLMProxy> proxy) {
		_proxy = proxy;
	}

	/**
	 * Answers a request.
	 */
	public void handle(LLMProxyExchange exchange) throws IOException {
		LLMProxy proxy = _proxy.get();
		String path = exchange.path();
		if (proxy == null || !proxy.isEnabled() || !POST.equals(exchange.method()) || path == null
			|| !FORWARDED_PATHS.contains(path)) {
			error(exchange, SC_NOT_FOUND, NOT_FOUND_ERROR, "Not found.");
			return;
		}
		String source = exchange.remoteAddress();
		if (!proxy.isAllowedSource(source)) {
			Logger.info("LLM proxy: refused a request from " + source + ".", LLMProxyHandler.class);
			error(exchange, SC_FORBIDDEN, PERMISSION_ERROR, "Source not allowed.");
			return;
		}
		LLMAdmission admission = proxy.admit(bearerToken(exchange), source);
		switch (admission.status()) {
			case ADMITTED:
				break;
			case INVALID_TOKEN:
				error(exchange, SC_UNAUTHORIZED, AUTHENTICATION_ERROR, "Invalid token.");
				return;
			case FOREIGN_SOURCE:
				error(exchange, SC_FORBIDDEN, PERMISSION_ERROR, "Token used from another source.");
				return;
			case RATE_LIMITED:
				long seconds = Math.max(1, (admission.retryAfter().toMillis() + 999) / 1000);
				exchange.addHeader(RETRY_AFTER, Long.toString(seconds));
				error(exchange, SC_TOO_MANY_REQUESTS, RATE_LIMIT_ERROR, "Too many requests.");
				return;
		}
		LLMUpstream upstream = proxy.upstream();
		if (upstream == null) {
			error(exchange, SC_SERVICE_UNAVAILABLE, API_ERROR, "LLM access is not configured.");
			return;
		}
		forward(upstream, path, exchange);
	}

	private static String bearerToken(LLMProxyExchange exchange) {
		List<String> values = exchange.headers(AUTHORIZATION);
		if (values.isEmpty()) {
			return null;
		}
		String authorization = values.get(0);
		if (authorization == null
			|| !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length()).strip();
	}

	private void forward(LLMUpstream upstream, String path, LLMProxyExchange exchange) throws IOException {
		String query = exchange.query();
		URI target = URI.create(upstream.baseUrl() + path + (query == null ? "" : "?" + query));
		byte[] body;
		try (InputStream in = exchange.body()) {
			body = in.readAllBytes();
		}

		HttpRequest.Builder request = HttpRequest.newBuilder(target)
			.timeout(upstream.requestTimeout())
			.POST(HttpRequest.BodyPublishers.ofByteArray(body));
		boolean hasVersion = false;
		for (String name : exchange.headerNames()) {
			String lower = name.toLowerCase(Locale.ROOT);
			if (DROPPED_REQUEST_HEADERS.contains(lower)) {
				continue;
			}
			hasVersion |= lower.equals(ANTHROPIC_VERSION);
			for (String value : exchange.headers(name)) {
				request.header(name, value);
			}
		}
		for (Map.Entry<String, String> header : upstream.customHeaders().entrySet()) {
			request.setHeader(header.getKey(), header.getValue());
		}
		if (!hasVersion) {
			request.setHeader(ANTHROPIC_VERSION, upstream.version());
		}
		try {
			upstream.credentials().authenticate(RequestAuthentication.of(request));
		} catch (I18NRuntimeException ex) {
			Logger.warn("LLM proxy: no credentials for " + target + ": " + ex.getMessage(), LLMProxyHandler.class);
			error(exchange, SC_SERVICE_UNAVAILABLE, API_ERROR, "The LLM API credentials are not available.");
			return;
		}

		HttpResponse<InputStream> response;
		try {
			response = client(upstream).send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
		} catch (HttpTimeoutException ex) {
			Logger.warn("LLM proxy: " + target + " did not answer in time.", LLMProxyHandler.class);
			error(exchange, SC_GATEWAY_TIMEOUT, API_ERROR, "The LLM API did not answer in time.");
			return;
		} catch (IOException ex) {
			Logger.warn("LLM proxy: " + target + " not reachable: " + ex.getClass().getName() + ": "
				+ ex.getMessage(), LLMProxyHandler.class);
			error(exchange, SC_BAD_GATEWAY, API_ERROR, "The LLM API is not reachable.");
			return;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			error(exchange, SC_SERVICE_UNAVAILABLE, API_ERROR, "Interrupted.");
			return;
		}

		try (InputStream in = response.body()) {
			for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
				String name = header.getKey();
				String lower = name.toLowerCase(Locale.ROOT);
				if (lower.startsWith(PSEUDO_HEADER_PREFIX) || DROPPED_RESPONSE_HEADERS.contains(lower)) {
					continue;
				}
				for (String value : header.getValue()) {
					exchange.addHeader(name, value);
				}
			}
			if (response.statusCode() == SC_UNAUTHORIZED) {
				upstream.credentials().invalidate();
			}
			exchange.sendHeaders(response.statusCode(), -1);
			OutputStream out = exchange.responseBody();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			while ((length = in.read(buffer)) >= 0) {
				out.write(buffer, 0, length);
				out.flush();
			}
		}
	}

	/**
	 * Releases the connections to the upstream.
	 */
	public synchronized void close() {
		if (_client != null) {
			_client.close();
			_client = null;
		}
	}

	private synchronized HttpClient client(LLMUpstream upstream) {
		if (_client == null || !upstream.connectTimeout().equals(_clientConnectTimeout)) {
			_client = HttpClient.newBuilder()
				.connectTimeout(upstream.connectTimeout())
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
			_clientConnectTimeout = upstream.connectTimeout();
		}
		return _client;
	}

	/**
	 * Answers with an error in the format of the Anthropic API. The message is a fixed text of this
	 * class, never something of the request or the upstream.
	 */
	private static void error(LLMProxyExchange exchange, int status, String type, String message)
			throws IOException {
		byte[] body = ("{\"type\":\"error\",\"error\":{\"type\":\"" + type + "\",\"message\":\"" + message + "\"}}")
			.getBytes(StandardCharsets.UTF_8);
		exchange.addHeader(CONTENT_TYPE, JSON_CONTENT_TYPE);
		exchange.sendHeaders(status, body.length);
		OutputStream out = exchange.responseBody();
		out.write(body);
		out.flush();
	}

}
