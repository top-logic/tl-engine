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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;

/**
 * Proxy of the Anthropic Messages API for the workspace containers, mounted at {@link #PATH}.
 *
 * <p>
 * Only {@code POST} of {@link #MESSAGES_PATH} and {@link #COUNT_TOKENS_PATH} (below {@link #PATH})
 * is forwarded, with the query string; everything else is answered with 404. A request must carry
 * a workspace token as bearer token in {@link #AUTHORIZATION}, which the {@link LLMProxy} checks,
 * otherwise it is answered with 401.
 * </p>
 *
 * <p>
 * The forwarded request carries the headers of the client except the credentials
 * ({@link #AUTHORIZATION}, {@link #X_API_KEY}), cookies, {@code Host}, the hop-by-hop headers and
 * {@code Accept-Encoding}, so that the answer comes uncompressed and can be streamed. The proxy sets
 * {@link #X_API_KEY} to the key of the {@link LLMUpstream}, and {@link #ANTHROPIC_VERSION} to its
 * version if the client sent none; a client's {@link #ANTHROPIC_VERSION} and {@code anthropic-beta}
 * are kept. Status, headers and body of the answer are passed back, the body flushed piece by piece
 * as it arrives, so that a server-sent event stream reaches the client while it is produced.
 * </p>
 *
 * <p>
 * The API key appears neither in a log nor in an answer of the proxy.
 * </p>
 */
public class LLMProxyServlet extends HttpServlet {

	/**
	 * Path the servlet is mounted at in the web application, see the {@code web-fragment.xml} of
	 * this module.
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
	 * Content type of the error answers of the proxy.
	 */
	public static final String JSON_CONTENT_TYPE = "application/json";

	/**
	 * Type of the error answering a request without valid token, as in the Anthropic API.
	 */
	public static final String AUTHENTICATION_ERROR = "authentication_error";

	/**
	 * Type of the error answering a request the proxy does not forward.
	 */
	public static final String NOT_FOUND_ERROR = "not_found_error";

	/**
	 * Type of the error answering a request the proxy cannot forward.
	 */
	public static final String API_ERROR = "api_error";

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
	 * container chooses the transfer), and cookies of the upstream.
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
	 * Creates a {@link LLMProxyServlet} using the {@link LLMProxyService} of the application.
	 */
	public LLMProxyServlet() {
		this(LLMProxyService::getInstance);
	}

	/**
	 * Creates a {@link LLMProxyServlet}.
	 *
	 * @param proxy
	 *        Supplier of the {@link LLMProxy} asked for each request; it may supply {@code null}
	 *        when there is none, which answers requests with 404.
	 */
	public LLMProxyServlet(Supplier<LLMProxy> proxy) {
		_proxy = proxy;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		LLMProxy proxy = _proxy.get();
		String path = req.getPathInfo();
		if (proxy == null || !proxy.isEnabled() || !POST.equals(req.getMethod()) || path == null
			|| !FORWARDED_PATHS.contains(path)) {
			error(resp, HttpServletResponse.SC_NOT_FOUND, NOT_FOUND_ERROR, "Not found.");
			return;
		}
		if (!proxy.authenticate(bearerToken(req))) {
			error(resp, HttpServletResponse.SC_UNAUTHORIZED, AUTHENTICATION_ERROR, "Invalid token.");
			return;
		}
		LLMUpstream upstream = proxy.upstream();
		if (upstream == null) {
			error(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, API_ERROR, "LLM access is not configured.");
			return;
		}
		forward(upstream, path, req, resp);
	}

	private static String bearerToken(HttpServletRequest req) {
		String authorization = req.getHeader(AUTHORIZATION);
		if (authorization == null || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length()).strip();
	}

	private void forward(LLMUpstream upstream, String path, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String query = req.getQueryString();
		URI target = URI.create(upstream.baseUrl() + path + (query == null ? "" : "?" + query));
		byte[] body = req.getInputStream().readAllBytes();

		HttpRequest.Builder request = HttpRequest.newBuilder(target)
			.timeout(upstream.requestTimeout())
			.POST(HttpRequest.BodyPublishers.ofByteArray(body));
		boolean hasVersion = false;
		for (String name : Collections.list(req.getHeaderNames())) {
			String lower = name.toLowerCase(Locale.ROOT);
			if (DROPPED_REQUEST_HEADERS.contains(lower)) {
				continue;
			}
			hasVersion |= lower.equals(ANTHROPIC_VERSION);
			for (String value : Collections.list(req.getHeaders(name))) {
				request.header(name, value);
			}
		}
		for (Map.Entry<String, String> header : upstream.customHeaders().entrySet()) {
			request.setHeader(header.getKey(), header.getValue());
		}
		if (!hasVersion) {
			request.setHeader(ANTHROPIC_VERSION, upstream.version());
		}
		request.setHeader(X_API_KEY, upstream.apiKey());

		HttpResponse<InputStream> response;
		try {
			response = client(upstream).send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
		} catch (HttpTimeoutException ex) {
			Logger.warn("LLM proxy: " + target + " did not answer in time.", LLMProxyServlet.class);
			error(resp, HttpServletResponse.SC_GATEWAY_TIMEOUT, API_ERROR, "The LLM API did not answer in time.");
			return;
		} catch (IOException ex) {
			Logger.warn("LLM proxy: " + target + " not reachable: " + ex.getClass().getName() + ": "
				+ ex.getMessage(), LLMProxyServlet.class);
			error(resp, HttpServletResponse.SC_BAD_GATEWAY, API_ERROR, "The LLM API is not reachable.");
			return;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			error(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, API_ERROR, "Interrupted.");
			return;
		}

		try (InputStream in = response.body()) {
			resp.setStatus(response.statusCode());
			for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
				String name = header.getKey();
				String lower = name.toLowerCase(Locale.ROOT);
				if (lower.startsWith(PSEUDO_HEADER_PREFIX) || DROPPED_RESPONSE_HEADERS.contains(lower)) {
					continue;
				}
				for (String value : header.getValue()) {
					resp.addHeader(name, value);
				}
			}
			OutputStream out = resp.getOutputStream();
			resp.flushBuffer();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			while ((length = in.read(buffer)) >= 0) {
				out.write(buffer, 0, length);
				out.flush();
			}
		}
	}

	@Override
	public synchronized void destroy() {
		if (_client != null) {
			_client.close();
			_client = null;
		}
		super.destroy();
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
	private static void error(HttpServletResponse resp, int status, String type, String message)
			throws IOException {
		byte[] body = ("{\"type\":\"error\",\"error\":{\"type\":\"" + type + "\",\"message\":\"" + message + "\"}}")
			.getBytes(StandardCharsets.UTF_8);
		resp.setStatus(status);
		resp.setContentType(JSON_CONTENT_TYPE);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setContentLength(body.length);
		resp.getOutputStream().write(body);
	}

}
