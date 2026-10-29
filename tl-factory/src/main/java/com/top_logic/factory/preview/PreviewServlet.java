/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.preview;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.factory.proxy.AsyncAnswer;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.proxy.ServletProxyExchange;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;

/**
 * The preview route of the control application: a reverse proxy to the applications of the
 * workspaces.
 *
 * <p>
 * The servlet is mapped to {@link ApplicationWorkspaces#PREVIEW_PATH} with {@code /*} (in the
 * {@code web-fragment.xml} of the module). A request
 * {@code <control context>/preview/<workspace>/...} is forwarded to the application of the
 * workspace {@link PreviewRoute#workspaceId(String) named by the first segment}, if the
 * {@link PreviewWorkspaces} let its client see it, with path and query unchanged: the
 * application is served under the browser-visible path, its
 * {@link ApplicationWorkspaces.Config#getContextPath() context path}, so that no URL in its pages,
 * redirects or cookies has to be rewritten. Only a path below that context path is
 * {@link PreviewRoute#target(PreviewTarget, String, String) forwarded}; a request the route does
 * not forward is answered with the status of its {@link PreviewRefusal}.
 * </p>
 *
 * <p>
 * The request is forwarded with its method, body and headers except the hop-by-hop headers and
 * the session cookie of the control application, see
 * {@link PreviewRoute#forwardedCookies(List, String, String)}; it gets the headers
 * {@link #X_FORWARDED_FOR}, {@link #X_FORWARDED_PROTO} and {@link #X_FORWARDED_HOST}. The answer
 * is passed back as it arrives, so that streams such as Server-Sent Events reach the browser at
 * once; its headers replace the ones the filters of the control application set, and only cookies
 * {@link PreviewRoute#isConfinedCookie(String, String) confined to the context path} of the
 * application pass. Redirects are passed back, not followed. Websocket upgrades are not forwarded.
 * </p>
 *
 * <p>
 * The forwarding is asynchronous (the servlet must be mapped with {@code async-supported}): no
 * thread waits for the application or for the client, so open streams do not hold threads of the
 * control application. A client that goes away cancels the answer, which closes the connection to
 * the application.
 * </p>
 */
public class PreviewServlet extends HttpServlet {

	/**
	 * Header naming the addresses of the client and the proxies a request passed.
	 */
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";

	/**
	 * Header naming the scheme the client used.
	 */
	public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

	/**
	 * Header naming the host the client addressed.
	 */
	public static final String X_FORWARDED_HOST = "X-Forwarded-Host";

	/**
	 * Header naming the host a request is addressed to.
	 */
	public static final String HOST = "Host";

	/**
	 * Name of the session cookie of a servlet container that is not configured otherwise.
	 */
	public static final String DEFAULT_SESSION_COOKIE = "JSESSIONID";

	/**
	 * Time within which the application of a workspace must accept the connection.
	 */
	public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

	private static final String FORWARDED_SEPARATOR = ", ";

	/**
	 * Request headers (lower case) that are not copied: their forwarded value is computed.
	 */
	private static final Set<String> COMPUTED_REQUEST_HEADERS = Set.of(
		ProxyForwarding.COOKIE.toLowerCase(Locale.ROOT), X_FORWARDED_FOR.toLowerCase(Locale.ROOT));

	private static final String SET_COOKIE = ProxyForwarding.SET_COOKIE.toLowerCase(Locale.ROOT);

	private final PreviewWorkspaces _workspaces;

	private HttpClient _client;

	/**
	 * Creates a {@link PreviewServlet} of the {@link StoredPreviewWorkspaces stored workspaces}.
	 */
	public PreviewServlet() {
		this(new StoredPreviewWorkspaces());
	}

	/**
	 * Creates a {@link PreviewServlet}.
	 *
	 * @param workspaces
	 *        Resolution of the workspaces the requests name.
	 */
	public PreviewServlet(PreviewWorkspaces workspaces) {
		_workspaces = workspaces;
	}

	@Override
	public void init() {
		_client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(CONNECT_TIMEOUT)
			.followRedirects(HttpClient.Redirect.NEVER)
			.build();
	}

	@Override
	public void destroy() {
		if (_client != null) {
			_client.close();
			_client = null;
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ServletProxyExchange exchange = new ServletProxyExchange(req, resp);
		String workspaceId = PreviewRoute.workspaceId(req.getPathInfo());
		if (workspaceId == null) {
			refuse(exchange, PreviewRefusal.NOT_FOUND);
			return;
		}
		PreviewLookup lookup = _workspaces.lookup(req, workspaceId);
		if (lookup.refusal() != null) {
			refuse(exchange, lookup.refusal());
			return;
		}
		PreviewTarget target = lookup.target();
		URI url = PreviewRoute.target(target, req.getRequestURI(), req.getQueryString());
		if (url == null) {
			refuse(exchange, PreviewRefusal.NOT_FOUND);
			return;
		}
		forward(exchange, url, target.contextPath());
	}

	private void forward(ServletProxyExchange exchange, URI url, String contextPath) throws IOException {
		HttpServletRequest req = exchange.request();
		HttpRequest.Builder request = HttpRequest.newBuilder(url).method(req.getMethod(),
			ProxyForwarding.hasRequestBody(exchange) ? ProxyForwarding.requestBody(exchange)
				: HttpRequest.BodyPublishers.noBody());
		ProxyForwarding.forwardRequestHeaders(exchange, request,
			lower -> ProxyForwarding.HOP_BY_HOP_REQUEST_HEADERS.contains(lower)
				|| COMPUTED_REQUEST_HEADERS.contains(lower));
		String cookies = PreviewRoute.forwardedCookies(exchange.headers(ProxyForwarding.COOKIE),
			sessionCookie(req), req.getRequestedSessionId());
		if (cookies != null) {
			request.header(ProxyForwarding.COOKIE, cookies);
		}
		addForwardedHeaders(exchange, request);

		AsyncAnswer answer = exchange.startAsync();
		ProxyForwarding.sendAsync(_client, request.build()).whenComplete((response, failure) -> {
			try {
				if (failure != null) {
					Logger.info("Preview: the application at " + url.getAuthority() + " does not answer: "
						+ ProxyForwarding.cause(failure).getMessage(), PreviewServlet.class);
					refuse(exchange, PreviewRefusal.NOT_ANSWERING);
					answer.complete();
					return;
				}
				ProxyForwarding.passBack(response, exchange, answer,
					(lower, value) -> !ProxyForwarding.HOP_BY_HOP_RESPONSE_HEADERS.contains(lower)
						&& (!SET_COOKIE.equals(lower) || PreviewRoute.isConfinedCookie(value, contextPath)));
			} catch (IOException | RuntimeException ex) {
				Logger.info("Preview: answer to the client failed: " + ex.getMessage(), PreviewServlet.class);
				answer.complete();
			}
		});
	}

	/**
	 * Adds the headers telling the application of the workspace the client and the address it
	 * used: the address of the client after the ones of the proxies before, and the scheme and
	 * host of the request unless a proxy before named them.
	 */
	private static void addForwardedHeaders(ServletProxyExchange exchange, HttpRequest.Builder request) {
		HttpServletRequest req = exchange.request();
		List<String> before = exchange.headers(X_FORWARDED_FOR);
		String client = req.getRemoteAddr();
		request.header(X_FORWARDED_FOR,
			before.isEmpty() ? client : String.join(FORWARDED_SEPARATOR, before) + FORWARDED_SEPARATOR + client);
		if (exchange.headers(X_FORWARDED_PROTO).isEmpty()) {
			request.header(X_FORWARDED_PROTO, req.getScheme());
		}
		List<String> host = exchange.headers(HOST);
		if (exchange.headers(X_FORWARDED_HOST).isEmpty() && !host.isEmpty()) {
			request.header(X_FORWARDED_HOST, host.get(0));
		}
	}

	/**
	 * The name of the session cookie of the control application.
	 */
	private static String sessionCookie(HttpServletRequest req) {
		String name = req.getServletContext().getSessionCookieConfig().getName();
		return name == null || name.isEmpty() ? DEFAULT_SESSION_COOKIE : name;
	}

	private static void refuse(ServletProxyExchange exchange, PreviewRefusal refusal) throws IOException {
		ProxyForwarding.sendText(exchange, refusal.status(), refusal.message());
	}

}
