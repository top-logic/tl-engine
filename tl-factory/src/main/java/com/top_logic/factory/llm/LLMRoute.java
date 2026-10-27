/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyError;
import com.top_logic.factory.proxy.ProxyExchange;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.proxy.ProxyRoute;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;

/**
 * {@link ProxyRoute} of the {@link WorkspaceProxyService workspace proxy} to the Anthropic Messages
 * API, without an API key in the workspace.
 *
 * <p>
 * A workspace container gets the URL of the route in {@link #ENV_BASE_URL}, a process the token
 * of the proxy in {@link #ENV_AUTH_TOKEN}; Claude Code sends it as bearer token.
 * </p>
 *
 * <p>
 * Only {@code POST} of {@link #MESSAGES_PATH} and {@link #COUNT_TOKENS_PATH} (below the path of the
 * route, by default {@link #DEFAULT_PATH}) is forwarded, with the query string; everything else is
 * answered with 404. The refusals of the proxy are answered as errors of the Anthropic API, e.g.
 * {@link #AUTHENTICATION_ERROR} for an invalid token.
 * </p>
 *
 * <p>
 * The forwarded request carries the headers of the client except the credentials
 * ({@link ProxyForwarding#AUTHORIZATION}, {@link #X_API_KEY}), cookies, {@code Host},
 * {@link ProxyForwarding#DROPPED_REQUEST_HEADERS the hop-by-hop headers} and {@link #ACCEPT_ENCODING}, so
 * that the answer comes uncompressed and can be streamed. The body is streamed to the API as it
 * arrives. The route adds the credentials of the {@link LLMUpstream} - the provider's API key in
 * {@link #X_API_KEY} -, and sets {@link #ANTHROPIC_VERSION} to its version if the client sent none;
 * a client's {@link #ANTHROPIC_VERSION} and {@code anthropic-beta} are kept. Status, headers and body
 * of the answer are passed back, the body flushed piece by piece as it arrives, so that a
 * server-sent event stream reaches the client while it is produced.
 * </p>
 *
 * <p>
 * The API key appears neither in a log nor in an answer of the route.
 * </p>
 */
public class LLMRoute implements ProxyRoute {

	/**
	 * Configuration of {@link LLMRoute}.
	 */
	public interface Config<I extends LLMRoute> extends ProxyRoute.Config<I> {

		/**
		 * Configuration property name of {@link #getProvider()}.
		 */
		String PROVIDER = "provider";

		@Override
		@ClassDefault(LLMRoute.class)
		Class<? extends I> getImplementationClass();

		@Override
		@StringDefault(DEFAULT_PATH)
		String getPath();

		/**
		 * The name of the Anthropic provider of the {@link LLMProviderService} whose API key, base
		 * URL, version and timeouts the route uses.
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@StringDefault(DEFAULT_PROVIDER)
		String getProvider();

	}

	/**
	 * Default of {@link Config#getPath()}.
	 */
	public static final String DEFAULT_PATH = "/llm-proxy";

	/**
	 * Default of {@link Config#getProvider()}.
	 */
	public static final String DEFAULT_PROVIDER = "anthropic";

	/**
	 * Environment variable of a workspace container naming the URL of the route, read by Claude
	 * Code as the base URL of the Anthropic API.
	 */
	public static final String ENV_BASE_URL = "ANTHROPIC_BASE_URL";

	/**
	 * Environment variable of a process in a workspace - never of the container - holding its
	 * token, which Claude Code sends as bearer token.
	 */
	public static final String ENV_AUTH_TOKEN = "ANTHROPIC_AUTH_TOKEN";

	/**
	 * Path below the path of the route of the Messages API.
	 */
	public static final String MESSAGES_PATH = "/v1/messages";

	/**
	 * Path below the path of the route of the token counting of the Messages API.
	 */
	public static final String COUNT_TOKENS_PATH = "/v1/messages/count_tokens";

	/**
	 * Header carrying the API key of the Anthropic API.
	 */
	public static final String X_API_KEY = "x-api-key";

	/**
	 * Header naming the version of the Anthropic API.
	 */
	public static final String ANTHROPIC_VERSION = "anthropic-version";

	/**
	 * Header naming the encodings the client accepts, not forwarded so that the answer can be
	 * streamed as it is.
	 */
	public static final String ACCEPT_ENCODING = "accept-encoding";

	/**
	 * Content type of the error answers of the route.
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
	 * Type of the error answering a request the route does not forward.
	 */
	public static final String NOT_FOUND_ERROR = "not_found_error";

	/**
	 * Type of the error answering a request the route cannot forward.
	 */
	public static final String API_ERROR = "api_error";

	private static final int SC_UNAUTHORIZED = 401;

	private static final int SC_NOT_FOUND = 404;

	private static final int SC_BAD_GATEWAY = 502;

	private static final int SC_SERVICE_UNAVAILABLE = 503;

	private static final int SC_GATEWAY_TIMEOUT = 504;

	private static final String POST = "POST";

	private static final Set<String> FORWARDED_PATHS = Set.of(MESSAGES_PATH, COUNT_TOKENS_PATH);

	/**
	 * Request headers (lower case) that are not forwarded besides
	 * {@link ProxyForwarding#DROPPED_REQUEST_HEADERS}.
	 */
	private static final Set<String> DROPPED_REQUEST_HEADERS = Set.of(X_API_KEY, ACCEPT_ENCODING);

	private final String _path;

	private final Supplier<LLMUpstream> _upstream;

	private HttpClient _client;

	private Duration _clientConnectTimeout;

	/**
	 * Creates a {@link LLMRoute} from configuration, forwarding to the configured
	 * {@link Config#getProvider() provider}.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public LLMRoute(InstantiationContext context, Config<?> config) {
		this(config.getPath(), () -> providerUpstream(config.getProvider()));
	}

	/**
	 * Creates a {@link LLMRoute}.
	 *
	 * @param path
	 *        See {@link #getPath()}.
	 * @param upstream
	 *        Supplier of the {@link LLMUpstream} asked for each request; it may supply
	 *        {@code null} when none is configured, which answers requests with 503.
	 */
	public LLMRoute(String path, Supplier<LLMUpstream> upstream) {
		_path = path;
		_upstream = upstream;
	}

	@Override
	public String getPath() {
		return _path;
	}

	@Override
	public Map<String, String> containerEnv(String routeUrl) {
		return Map.of(ENV_BASE_URL, routeUrl);
	}

	@Override
	public Map<String, String> processEnv(String token) {
		return Map.of(ENV_AUTH_TOKEN, token);
	}

	@Override
	public void handle(ProxyExchange exchange, String path, ProxyAdmission admission) throws IOException {
		if (!POST.equals(exchange.method()) || !FORWARDED_PATHS.contains(path)) {
			error(exchange, SC_NOT_FOUND, NOT_FOUND_ERROR, ProxyError.NOT_FOUND.message());
			return;
		}
		LLMUpstream upstream = _upstream.get();
		if (upstream == null) {
			error(exchange, SC_SERVICE_UNAVAILABLE, API_ERROR, "LLM access is not configured.");
			return;
		}
		forward(upstream, path, exchange);
	}

	@Override
	public void sendError(ProxyExchange exchange, ProxyError error) throws IOException {
		error(exchange, error.status(), errorType(error), error.message());
	}

	private static String errorType(ProxyError error) {
		switch (error) {
			case NOT_FOUND:
				return NOT_FOUND_ERROR;
			case INVALID_TOKEN:
				return AUTHENTICATION_ERROR;
			case SOURCE_NOT_ALLOWED:
			case FOREIGN_SOURCE:
				return PERMISSION_ERROR;
			case RATE_LIMITED:
				return RATE_LIMIT_ERROR;
		}
		return API_ERROR;
	}

	private void forward(LLMUpstream upstream, String path, ProxyExchange exchange) throws IOException {
		String query = exchange.query();
		URI target = URI.create(upstream.baseUrl() + path + (query == null ? "" : "?" + query));

		HttpRequest.Builder request = HttpRequest.newBuilder(target)
			.timeout(upstream.requestTimeout())
			.POST(ProxyForwarding.requestBody(exchange));
		ProxyForwarding.copyRequestHeaders(exchange, request, DROPPED_REQUEST_HEADERS);
		for (Map.Entry<String, String> header : upstream.customHeaders().entrySet()) {
			request.setHeader(header.getKey(), header.getValue());
		}
		if (exchange.headers(ANTHROPIC_VERSION).isEmpty()) {
			request.setHeader(ANTHROPIC_VERSION, upstream.version());
		}
		try {
			upstream.credentials().authenticate(RequestAuthentication.of(request));
		} catch (I18NRuntimeException ex) {
			Logger.warn("LLM proxy: no credentials for " + target + ": " + ex.getMessage(), LLMRoute.class);
			error(exchange, SC_SERVICE_UNAVAILABLE, API_ERROR, "The LLM API credentials are not available.");
			return;
		}

		HttpResponse<InputStream> response;
		try {
			response = client(upstream).send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
		} catch (HttpTimeoutException ex) {
			Logger.warn("LLM proxy: " + target + " did not answer in time.", LLMRoute.class);
			error(exchange, SC_GATEWAY_TIMEOUT, API_ERROR, "The LLM API did not answer in time.");
			return;
		} catch (IOException ex) {
			Logger.warn("LLM proxy: " + target + " not reachable: " + ex.getClass().getName() + ": "
				+ ex.getMessage(), LLMRoute.class);
			error(exchange, SC_BAD_GATEWAY, API_ERROR, "The LLM API is not reachable.");
			return;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			error(exchange, SC_SERVICE_UNAVAILABLE, API_ERROR, "Interrupted.");
			return;
		}

		if (response.statusCode() == SC_UNAUTHORIZED) {
			upstream.credentials().invalidate();
		}
		ProxyForwarding.sendResponse(response, exchange);
	}

	@Override
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
	 * The upstream of the Anthropic provider of the given name of the {@link LLMProviderService}.
	 *
	 * @return The upstream, {@code null} if there is none.
	 */
	public static LLMUpstream providerUpstream(String name) {
		LLMProviderService providers = LLMProviderService.getInstance();
		if (providers == null) {
			Logger.warn("LLM proxy: the LLM providers are not running.", LLMRoute.class);
			return null;
		}
		LLMProviderConfig provider = providers.getProvider(name);
		if (!(provider instanceof AnthropicProviderConfig)) {
			Logger.warn("LLM proxy: no Anthropic provider '" + name + "' is configured.", LLMRoute.class);
			return null;
		}
		LLMUpstream result = LLMUpstream.fromProvider((AnthropicProviderConfig) provider);
		if (result == null) {
			Logger.warn("LLM proxy: the provider '" + name + "' has neither API key nor credentials.",
				LLMRoute.class);
		}
		return result;
	}

	/**
	 * Answers with an error in the format of the Anthropic API. The message is a fixed text of this
	 * class, never something of the request or the upstream.
	 */
	private static void error(ProxyExchange exchange, int status, String type, String message) throws IOException {
		byte[] body = ("{\"type\":\"error\",\"error\":{\"type\":\"" + type + "\",\"message\":\"" + message + "\"}}")
			.getBytes(StandardCharsets.UTF_8);
		ProxyForwarding.send(exchange, status, JSON_CONTENT_TYPE, body);
	}

}
