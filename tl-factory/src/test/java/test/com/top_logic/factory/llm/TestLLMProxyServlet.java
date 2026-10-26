/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.llm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.listener.Listener;
import com.top_logic.factory.llm.LLMAdmission;
import com.top_logic.factory.llm.LLMProxy;
import com.top_logic.factory.llm.LLMProxyHandler;
import com.top_logic.factory.llm.LLMProxyServlet;
import com.top_logic.factory.llm.LLMUpstream;
import com.top_logic.factory.llm.SourceFilter;
import com.top_logic.factory.llm.TokenGrants;

/**
 * Test of the {@link LLMProxyHandler LLM proxy} in the {@link LLMProxyServlet} over HTTP: the
 * servlet runs in an embedded Jetty, the Anthropic API is a fake upstream server recording what it
 * receives.
 *
 * <p>
 * {@link TestLLMProxyListener} runs the same tests against the proxy on a port of its own.
 * </p>
 */
public class TestLLMProxyServlet extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	private static final String VALID_TOKEN = "tlwst_valid";

	private static final String API_KEY = "sk-ant-secret-key-of-the-control-application";

	private static final String PROVIDER_VERSION = "2023-06-01";

	private static final String REQUEST_BODY = "{\"model\":\"claude\",\"max_tokens\":10}";

	/**
	 * The loopback address the proxy is reached at.
	 */
	protected static final String LOOPBACK = "127.0.0.1";

	/**
	 * Another loopback address, a client on another address than {@link #LOOPBACK}.
	 */
	private static final String OTHER_LOOPBACK = "127.0.0.2";

	/**
	 * Requests per minute of the tokens of {@link #_grants}.
	 */
	private static final int RATE_LIMIT = 2;

	private Server _jetty;

	/**
	 * The URL of the proxy, {@link LLMProxyHandler#PATH} included.
	 */
	private String _proxyBase;

	private LLMAdmission _admission = LLMAdmission.ADMITTED;

	private final TokenGrants _grants = new TokenGrants(Clock.systemUTC(), workspace -> true, RATE_LIMIT);

	private HttpServer _upstream;

	private boolean _enabled = true;

	private LLMUpstream _upstreamConfig;

	private SourceFilter _sources;

	private final List<Recorded> _received = new CopyOnWriteArrayList<>();

	private final List<String> _logged = new CopyOnWriteArrayList<>();

	private final Listener<LogEntry> _logListener = entry -> {
		_logged.add(String.valueOf(entry.getMessage()));
		if (entry.getException().hasValue()) {
			_logged.add(String.valueOf(entry.getException().get()));
		}
	};

	private HttpClient _client;

	/**
	 * What the fake upstream received.
	 */
	private record Recorded(String method, String path, String query, Headers headers, String body) {
		// Plain data.
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Logger.getListenerRegistry().register(_logListener);

		_upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_upstream.start();
		_upstreamConfig = upstreamAt("http://127.0.0.1:" + _upstream.getAddress().getPort());
		_sources = new SourceFilter(List.of(), Collections::emptyList);

		LLMProxy proxy = new LLMProxy() {
			@Override
			public boolean isEnabled() {
				return _enabled;
			}

			@Override
			public boolean isAllowedSource(String remoteAddress) {
				return _sources.allows(remoteAddress);
			}

			@Override
			public LLMAdmission admit(String token, String remoteAddress) {
				if (VALID_TOKEN.equals(token)) {
					return _admission;
				}
				return _grants.admit(token, remoteAddress);
			}

			@Override
			public LLMUpstream upstream() {
				return _upstreamConfig;
			}
		};

		_proxyBase = startProxy(proxy);

		_client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_client.close();
			stopProxy();
			_upstream.stop(0);
			for (String message : _logged) {
				assertFalse("API key in log: " + message, message.contains(API_KEY));
			}
		} finally {
			Logger.getListenerRegistry().unregister(_logListener);
			super.tearDown();
		}
	}

	/**
	 * Starts the proxy answering on {@link #LOOPBACK}.
	 *
	 * @return The URL of the proxy, {@link LLMProxyHandler#PATH} included.
	 */
	protected String startProxy(LLMProxy proxy) throws Exception {
		_jetty = new Server();
		ServerConnector connector = new ServerConnector(_jetty);
		connector.setHost(LOOPBACK);
		connector.setPort(0);
		_jetty.addConnector(connector);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath(CONTEXT_PATH);
		context.addServlet(new ServletHolder(new LLMProxyServlet(() -> proxy)), LLMProxyHandler.PATH + "/*");
		_jetty.setHandler(context);
		_jetty.start();
		return "http://" + LOOPBACK + ":" + connector.getLocalPort() + CONTEXT_PATH + LLMProxyHandler.PATH;
	}

	/**
	 * Stops the proxy started by {@link #startProxy(LLMProxy)}.
	 */
	protected void stopProxy() throws Exception {
		_jetty.stop();
	}

	private static LLMUpstream upstreamAt(String baseUrl) {
		return new LLMUpstream(baseUrl, API_KEY, PROVIDER_VERSION, Duration.ofSeconds(2), Duration.ofSeconds(20),
			Map.of());
	}

	/**
	 * A valid token is forwarded with the API key instead of the credentials of the client.
	 */
	public void testForwardsWithInjectedKey() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{\"id\":\"msg_1\"}", "request-id", "req_1");

		HttpResponse<String> response = send(HttpRequest.newBuilder(proxyUri(LLMProxyHandler.MESSAGES_PATH + "?beta=true"))
			.header(LLMProxyHandler.AUTHORIZATION, LLMProxyHandler.BEARER_PREFIX + VALID_TOKEN)
			.header(LLMProxyHandler.X_API_KEY, "client-key")
			.header("anthropic-beta", "some-beta")
			.header("Cookie", "JSESSIONID=control-session")
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));

		assertEquals(200, response.statusCode());
		assertEquals("{\"id\":\"msg_1\"}", response.body());
		assertEquals("req_1", response.headers().firstValue("request-id").orElse(null));

		assertEquals(1, _received.size());
		Recorded received = _received.get(0);
		assertEquals("POST", received.method());
		assertEquals(LLMProxyHandler.MESSAGES_PATH, received.path());
		assertEquals("beta=true", received.query());
		assertEquals(REQUEST_BODY, received.body());
		assertEquals(List.of(API_KEY), received.headers().get(LLMProxyHandler.X_API_KEY));
		assertNull("The bearer token is not forwarded.", received.headers().get(LLMProxyHandler.AUTHORIZATION));
		assertNull("Cookies are not forwarded.", received.headers().get("Cookie"));
		assertEquals(PROVIDER_VERSION, received.headers().getFirst(LLMProxyHandler.ANTHROPIC_VERSION));
		assertEquals("some-beta", received.headers().getFirst("anthropic-beta"));
		assertEquals("application/json", received.headers().getFirst("Content-Type"));
	}

	/**
	 * The version a client asks for is kept; the token counting is forwarded as well.
	 */
	public void testClientVersionAndCountTokens() throws Exception {
		upstreamAnswers(LLMProxyHandler.COUNT_TOKENS_PATH, 200, "{\"input_tokens\":3}");

		HttpResponse<String> response = send(authorized(LLMProxyHandler.COUNT_TOKENS_PATH)
			.header(LLMProxyHandler.ANTHROPIC_VERSION, "2099-01-01")
			.POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));

		assertEquals(200, response.statusCode());
		assertEquals("{\"input_tokens\":3}", response.body());
		Recorded received = _received.get(0);
		assertEquals(LLMProxyHandler.COUNT_TOKENS_PATH, received.path());
		assertEquals(List.of("2099-01-01"), received.headers().get(LLMProxyHandler.ANTHROPIC_VERSION));
	}

	/**
	 * A server-sent event stream reaches the client while the upstream still produces it.
	 */
	public void testStreamsIncrementally() throws Exception {
		CountDownLatch firstSeen = new CountDownLatch(1);
		_upstream.createContext(LLMProxyHandler.MESSAGES_PATH, exchange -> {
			record(exchange);
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			exchange.sendResponseHeaders(200, 0);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write("event: first\ndata: {}\n\n".getBytes(StandardCharsets.UTF_8));
				out.flush();
				if (!firstSeen.await(10, TimeUnit.SECONDS)) {
					return;
				}
				out.write("event: second\ndata: {}\n\n".getBytes(StandardCharsets.UTF_8));
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		});

		HttpResponse<InputStream> response = _client.send(
			authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)).build(),
			HttpResponse.BodyHandlers.ofInputStream());
		assertEquals(200, response.statusCode());
		assertEquals("text/event-stream", response.headers().firstValue("Content-Type").orElse(null));

		try (InputStream in = response.body()) {
			ByteArrayOutputStream seen = new ByteArrayOutputStream();
			CompletableFuture<Void> first = CompletableFuture.runAsync(() -> readUntil(in, seen, "event: first"));
			try {
				first.get(5, TimeUnit.SECONDS);
			} catch (TimeoutException ex) {
				fail("The first event did not arrive before the upstream sent the second one.");
			}
			assertFalse(seen.toString(StandardCharsets.UTF_8).contains("second"));
			firstSeen.countDown();
			seen.write(in.readAllBytes());
			assertEquals("event: first\ndata: {}\n\nevent: second\ndata: {}\n\n", seen.toString(StandardCharsets.UTF_8));
		}
	}

	/**
	 * A request without or with a wrong token is refused and not forwarded.
	 */
	public void testInvalidTokenRefused() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");

		List<HttpRequest.Builder> requests = new ArrayList<>();
		requests.add(HttpRequest.newBuilder(proxyUri(LLMProxyHandler.MESSAGES_PATH)));
		requests.add(HttpRequest.newBuilder(proxyUri(LLMProxyHandler.MESSAGES_PATH))
			.header(LLMProxyHandler.AUTHORIZATION, LLMProxyHandler.BEARER_PREFIX + "tlwst_wrong"));
		requests.add(HttpRequest.newBuilder(proxyUri(LLMProxyHandler.MESSAGES_PATH))
			.header(LLMProxyHandler.AUTHORIZATION, "Basic " + VALID_TOKEN));
		requests.add(HttpRequest.newBuilder(proxyUri(LLMProxyHandler.MESSAGES_PATH))
			.header(LLMProxyHandler.X_API_KEY, VALID_TOKEN));
		for (HttpRequest.Builder request : requests) {
			HttpResponse<String> response = send(request.POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
			assertEquals(401, response.statusCode());
			assertTrue(response.body(), response.body().contains(LLMProxyHandler.AUTHENTICATION_ERROR));
			assertFalse(response.body().contains(API_KEY));
		}
		assertEquals(Collections.emptyList(), _received);
	}

	/**
	 * Other paths and methods do not exist, even with a valid token.
	 */
	public void testOtherPathsAndMethodsNotFound() throws Exception {
		upstreamAnswers("/", 200, "{}");

		assertEquals(404, send(authorized(LLMProxyHandler.MESSAGES_PATH).GET()).statusCode());
		assertEquals(404,
			send(authorized("/v1/models").POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY))).statusCode());
		assertEquals(404,
			send(authorized("/v1/messages/batches").POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)))
				.statusCode());
		assertEquals(404, send(authorized("").POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY))).statusCode());
		assertEquals(Collections.emptyList(), _received);
	}

	/**
	 * A request from an address that is not accepted is refused before its token is looked at,
	 * whatever a forwarding header claims; the refusal is logged with the address.
	 */
	public void testForeignSourceRefused() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");
		_sources = new SourceFilter(List.of("10.0.0.0/8", "fd00::/8"), Collections::emptyList);

		HttpResponse<String> response = send(authorized(LLMProxyHandler.MESSAGES_PATH)
			.header("X-Forwarded-For", "10.1.2.3")
			.POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(403, response.statusCode());
		assertTrue(response.body(), response.body().contains(LLMProxyHandler.PERMISSION_ERROR));
		assertEquals(Collections.emptyList(), _received);
		assertTrue("The refusal is logged with the address: " + _logged,
			_logged.stream().anyMatch(message -> message.contains("127.0.0.1")));
		for (String message : _logged) {
			assertFalse("No token in the log.", message.contains(VALID_TOKEN));
		}
	}

	/**
	 * A request from an accepted range is forwarded.
	 */
	public void testAcceptedSourceForwarded() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");
		_sources = new SourceFilter(List.of("127.0.0.0/8"), Collections::emptyList);

		HttpResponse<String> response =
			send(authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(200, response.statusCode());
		assertEquals(1, _received.size());
	}

	/**
	 * A switched off proxy does not exist.
	 */
	public void testDisabled() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");
		_enabled = false;

		HttpResponse<String> response =
			send(authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(404, response.statusCode());
		assertEquals(Collections.emptyList(), _received);
	}

	/**
	 * An error of the upstream is passed back with its status, headers and body.
	 */
	public void testUpstreamErrorPassedThrough() throws Exception {
		String error = "{\"type\":\"error\",\"error\":{\"type\":\"rate_limit_error\",\"message\":\"Slow down.\"}}";
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 429, error, "retry-after", "7");

		HttpResponse<String> response =
			send(authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(429, response.statusCode());
		assertEquals(error, response.body());
		assertEquals("7", response.headers().firstValue("retry-after").orElse(null));
	}

	/**
	 * An unreachable upstream is answered with 502, without the key.
	 */
	public void testUpstreamNotReachable() throws Exception {
		int port = _upstream.getAddress().getPort();
		_upstream.stop(0);
		_upstreamConfig = upstreamAt("http://127.0.0.1:" + port);

		HttpResponse<String> response =
			send(authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(502, response.statusCode());
		assertFalse(response.body().contains(API_KEY));
		assertFalse("The failure is logged.", _logged.isEmpty());
	}

	/**
	 * Without a configured upstream, the request is answered with 503.
	 */
	public void testNoUpstream() throws Exception {
		_upstreamConfig = null;

		HttpResponse<String> response =
			send(authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(503, response.statusCode());
	}

	/**
	 * A valid token used from another address than its first request is refused with 403.
	 */
	public void testTokenOfOtherSourceRefused() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");
		_admission = LLMAdmission.FOREIGN_SOURCE;

		HttpResponse<String> response =
			send(authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(403, response.statusCode());
		assertTrue(response.body(), response.body().contains(LLMProxyHandler.PERMISSION_ERROR));
		assertEquals(Collections.emptyList(), _received);
	}

	/**
	 * A token over its rate limit is refused with 429 and the seconds to wait, rounded up.
	 */
	public void testRateLimitedRefused() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");
		_admission = LLMAdmission.rateLimited(Duration.ofMillis(6500));

		HttpResponse<String> response =
			send(authorized(LLMProxyHandler.MESSAGES_PATH).POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY)));
		assertEquals(LLMProxyHandler.SC_TOO_MANY_REQUESTS, response.statusCode());
		assertTrue(response.body(), response.body().contains(LLMProxyHandler.RATE_LIMIT_ERROR));
		assertEquals("7", response.headers().firstValue(LLMProxyHandler.RETRY_AFTER).orElse(null));
		assertEquals(Collections.emptyList(), _received);
	}

	/**
	 * A token is bound to the address of its first request: a request from another address is
	 * refused and logged, the token keeps working from its own address.
	 */
	public void testTokenBoundToFirstSource() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");
		String token = _grants.issue("ws-1", Duration.ofHours(1));

		assertEquals(200, sendFrom(LOOPBACK, token).statusCode());

		HttpResponse<String> foreign = sendFrom(OTHER_LOOPBACK, token);
		assertEquals(403, foreign.statusCode());
		assertTrue(foreign.body(), foreign.body().contains(LLMProxyHandler.PERMISSION_ERROR));
		assertTrue("The refusal is logged with the address: " + _logged,
			_logged.stream().anyMatch(message -> message.contains(OTHER_LOOPBACK) && message.contains("ws-1")));
		for (String message : _logged) {
			assertFalse("No token in the log.", message.contains(token));
		}

		assertEquals("Not revoked.", 200, sendFrom(LOOPBACK, token).statusCode());
		assertEquals(2, _received.size());

		String other = _grants.issue("ws-2", Duration.ofHours(1));
		assertEquals("Another token binds to its own first address.", 200, sendFrom(OTHER_LOOPBACK, other).statusCode());
	}

	/**
	 * A token making more requests than its rate limit is refused until its allowance refills.
	 */
	public void testTokenRateLimit() throws Exception {
		upstreamAnswers(LLMProxyHandler.MESSAGES_PATH, 200, "{}");
		String token = _grants.issue("ws-1", Duration.ofHours(1));

		for (int n = 0; n < RATE_LIMIT; n++) {
			assertEquals(200, sendFrom(LOOPBACK, token).statusCode());
		}
		HttpResponse<String> limited = sendFrom(LOOPBACK, token);
		assertEquals(LLMProxyHandler.SC_TOO_MANY_REQUESTS, limited.statusCode());
		long retryAfter = Long.parseLong(limited.headers().firstValue(LLMProxyHandler.RETRY_AFTER).orElseThrow());
		assertTrue("Retry after " + retryAfter, retryAfter > 0 && retryAfter <= 60 / RATE_LIMIT);
		assertEquals(RATE_LIMIT, _received.size());

		String other = _grants.issue("ws-1", Duration.ofHours(1));
		assertEquals("The limit is per token.", 200, sendFrom(LOOPBACK, other).statusCode());
	}

	/**
	 * Sends a request with the given token from the given local address.
	 */
	private HttpResponse<String> sendFrom(String localAddress, String token) throws Exception {
		try (HttpClient client = HttpClient.newBuilder()
			.localAddress(InetAddress.getByName(localAddress))
			.connectTimeout(Duration.ofSeconds(5))
			.build()) {
			return client.send(HttpRequest.newBuilder(proxyUri(LLMProxyHandler.MESSAGES_PATH))
				.timeout(Duration.ofSeconds(20))
				.header(LLMProxyHandler.AUTHORIZATION, LLMProxyHandler.BEARER_PREFIX + token)
				.POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY))
				.build(), HttpResponse.BodyHandlers.ofString());
		}
	}

	/**
	 * The string form of an upstream does not contain the key.
	 */
	public void testUpstreamToStringHidesKey() {
		assertFalse(_upstreamConfig.toString().contains(API_KEY));
	}

	private void upstreamAnswers(String path, int status, String body, String... headers) {
		_upstream.createContext(path, exchange -> {
			record(exchange);
			for (int n = 0; n < headers.length; n += 2) {
				exchange.getResponseHeaders().add(headers[n], headers[n + 1]);
			}
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(status, bytes.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(bytes);
			}
		});
	}

	private void record(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		_received.add(new Recorded(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
			exchange.getRequestURI().getRawQuery(), exchange.getRequestHeaders(), body));
	}

	private URI proxyUri(String path) {
		return URI.create(_proxyBase + path);
	}

	private HttpRequest.Builder authorized(String path) {
		return HttpRequest.newBuilder(proxyUri(path))
			.timeout(Duration.ofSeconds(20))
			.header(LLMProxyHandler.AUTHORIZATION, LLMProxyHandler.BEARER_PREFIX + VALID_TOKEN);
	}

	private HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
		return _client.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

	private static void readUntil(InputStream in, ByteArrayOutputStream seen, String marker) {
		try {
			byte[] buffer = new byte[256];
			while (!seen.toString(StandardCharsets.UTF_8).contains(marker)) {
				int length = in.read(buffer);
				if (length < 0) {
					throw new IllegalStateException("Stream ended before " + marker + ".");
				}
				seen.write(buffer, 0, length);
			}
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
