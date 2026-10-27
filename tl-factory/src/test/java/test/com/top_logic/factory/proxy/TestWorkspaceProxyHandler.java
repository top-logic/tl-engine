/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.TestCase;

import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyExchange;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.proxy.ProxyRoute;
import com.top_logic.factory.proxy.TokenGrants;
import com.top_logic.factory.proxy.WorkspaceProxy;
import com.top_logic.factory.proxy.WorkspaceProxyHandler;
import com.top_logic.factory.proxy.WorkspaceProxyListener;

/**
 * Test of the {@link WorkspaceProxyHandler}, on a {@link WorkspaceProxyListener}, with routes
 * recording what reaches them: the dispatch by path, the token in a bearer token or in basic
 * authentication, and the token valid on every route.
 */
public class TestWorkspaceProxyHandler extends TestCase {

	private static final String LOOPBACK = "127.0.0.1";

	private static final String WORKSPACE = "ws-1";

	private static final String ROUTE_A = "/a";

	private static final String ROUTE_A_B = "/a/b";

	private static final String ROUTE_C = "/c";

	private final TokenGrants _grants = new TokenGrants(Clock.systemUTC(), workspace -> true, 0);

	private final List<String> _handled = new CopyOnWriteArrayList<>();

	private WorkspaceProxyListener _listener;

	private HttpClient _client;

	private boolean _enabled = true;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<ProxyRoute> routes = List.of(new Recording(ROUTE_A), new Recording(ROUTE_A_B), new Recording(ROUTE_C));
		WorkspaceProxy proxy = new WorkspaceProxy() {
			@Override
			public boolean isEnabled() {
				return _enabled;
			}

			@Override
			public List<ProxyRoute> routes() {
				return routes;
			}

			@Override
			public boolean isAllowedSource(String remoteAddress) {
				return true;
			}

			@Override
			public ProxyAdmission admit(String token, String remoteAddress) {
				return _grants.admit(token, remoteAddress);
			}
		};
		_listener = new WorkspaceProxyListener(new InetSocketAddress(LOOPBACK, 0), () -> proxy);
		_client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	}

	@Override
	protected void tearDown() throws Exception {
		_client.close();
		_listener.close();
		super.tearDown();
	}

	/**
	 * A request goes to the route whose path is the longest prefix of its own, with the rest of
	 * the path and the workspace of its token.
	 */
	public void testDispatchByPrefix() throws Exception {
		String token = _grants.issue(WORKSPACE, Duration.ofHours(1));

		assertEquals(200, send("/a/x/y?q=1", bearer(token)).statusCode());
		assertEquals(200, send("/a", bearer(token)).statusCode());
		assertEquals(200, send("/a/b/z", bearer(token)).statusCode());
		assertEquals(200, send("/a/bz", bearer(token)).statusCode());
		assertEquals(200, send("/c/", bearer(token)).statusCode());
		assertEquals(List.of(
			ROUTE_A + " /x/y " + WORKSPACE,
			ROUTE_A + "  " + WORKSPACE,
			ROUTE_A_B + " /z " + WORKSPACE,
			ROUTE_A + " /bz " + WORKSPACE,
			ROUTE_C + " / " + WORKSPACE), _handled);
	}

	/**
	 * A path reaching no route is answered with 404 before its token is looked at.
	 */
	public void testUnknownPrefixNotFound() throws Exception {
		String token = _grants.issue(WORKSPACE, Duration.ofHours(1));
		for (String path : List.of("/", "/ab", "/b/a", "/cc/x", "/x")) {
			assertEquals(path, 404, send(path, bearer(token)).statusCode());
			assertEquals(path, 404, send(path, null).statusCode());
		}
		assertEquals(Collections.emptyList(), _handled);
	}

	/**
	 * A switched off proxy answers every path with 404.
	 */
	public void testDisabledNotFound() throws Exception {
		String token = _grants.issue(WORKSPACE, Duration.ofHours(1));
		_enabled = false;
		assertEquals(404, send("/a/x", bearer(token)).statusCode());
		assertEquals(Collections.emptyList(), _handled);
	}

	/**
	 * The token of an agent process is valid on every route of its proxy.
	 */
	public void testTokenValidOnEveryRoute() throws Exception {
		String token = _grants.issue(WORKSPACE, Duration.ofHours(1));
		for (String route : List.of(ROUTE_A, ROUTE_A_B, ROUTE_C)) {
			assertEquals(route, 200, send(route + "/x", bearer(token)).statusCode());
		}
		assertEquals(3, _handled.size());

		_grants.revoke(token);
		for (String route : List.of(ROUTE_A, ROUTE_A_B, ROUTE_C)) {
			assertEquals("Revoked for all routes: " + route, 401, send(route + "/x", bearer(token)).statusCode());
		}
		assertEquals(3, _handled.size());
	}

	/**
	 * The token is accepted as password of basic authentication, whatever the user name; without
	 * a password, a token in the user name, or a malformed value is refused.
	 */
	public void testBasicAuthentication() throws Exception {
		String token = _grants.issue(WORKSPACE, Duration.ofHours(1));

		assertEquals(200, send("/a/x", basic("git", token)).statusCode());
		assertEquals(200, send("/a/x", basic("", token)).statusCode());
		assertEquals("Lower case scheme.", 200,
			send("/a/x", "basic " + encode("git:" + token)).statusCode());
		assertEquals(3, _handled.size());

		assertEquals(401, send("/a/x", basic(token, "")).statusCode());
		assertEquals(401, send("/a/x", WorkspaceProxyHandler.BASIC_PREFIX + encode(token)).statusCode());
		assertEquals(401, send("/a/x", WorkspaceProxyHandler.BASIC_PREFIX + "not base64!").statusCode());
		assertEquals(401, send("/a/x", basic("git", "tlwst_unknown")).statusCode());
		assertEquals(401, send("/a/x", null).statusCode());
		assertEquals(3, _handled.size());
	}

	private HttpResponse<String> send(String path, String authorization) throws IOException, InterruptedException {
		HttpRequest.Builder request = HttpRequest.newBuilder(
			URI.create("http://" + LOOPBACK + ":" + _listener.getAddress().getPort() + path))
			.timeout(Duration.ofSeconds(10))
			.GET();
		if (authorization != null) {
			request.header(ProxyForwarding.AUTHORIZATION, authorization);
		}
		return _client.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

	private static String bearer(String token) {
		return WorkspaceProxyHandler.BEARER_PREFIX + token;
	}

	private static String basic(String user, String password) {
		return WorkspaceProxyHandler.BASIC_PREFIX + encode(user + ":" + password);
	}

	private static String encode(String text) {
		return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * {@link ProxyRoute} recording its path, the path of the request below it and the workspace
	 * of the token.
	 */
	private final class Recording implements ProxyRoute {

		private final String _path;

		Recording(String path) {
			_path = path;
		}

		@Override
		public String getPath() {
			return _path;
		}

		@Override
		public void handle(ProxyExchange exchange, String path, ProxyAdmission admission) throws IOException {
			_handled.add(_path + " " + path + " " + admission.workspaceId());
			ProxyForwarding.sendText(exchange, 200, "OK");
		}
	}

}
