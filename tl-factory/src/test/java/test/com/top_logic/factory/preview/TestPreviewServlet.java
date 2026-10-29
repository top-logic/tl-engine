/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.preview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.factory.preview.PreviewLookup;
import com.top_logic.factory.preview.PreviewRefusal;
import com.top_logic.factory.preview.PreviewServlet;
import com.top_logic.factory.preview.PreviewTarget;
import com.top_logic.factory.preview.PreviewWorkspaces;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;

/**
 * Test of the {@link PreviewServlet preview route} in a servlet container in front of an
 * application: the refused requests, the request and answer passed unchanged, the cookies of the
 * control application kept from the application and the cookies of the application confined to
 * its context path, and a stream reaching the client while it is produced.
 */
@SuppressWarnings("javadoc")
public class TestPreviewServlet extends TestCase {

	private static final String LOOPBACK = "127.0.0.1";

	private static final String CONTROL_CONTEXT = "/control";

	private static final String WORKSPACE = "ws1";

	private static final String STOPPED_WORKSPACE = "ws-stopped";

	private static final String SILENT_WORKSPACE = "ws-silent";

	private static final String APP_CONTEXT = CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/" + WORKSPACE + "/app";

	private static final String SILENT_CONTEXT =
		CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/" + SILENT_WORKSPACE + "/app";

	private static final String LOGIN_PATH = "/login";

	private static final String LOGGED_IN = "loggedIn";

	private static final String SESSION_COOKIE = PreviewServlet.DEFAULT_SESSION_COOKIE;

	private static final Duration WAIT = Duration.ofSeconds(10);

	/**
	 * Maximum number of threads of the server of the control application, fewer than
	 * {@link #OPEN_STREAMS}.
	 */
	private static final int SERVER_THREADS = 8;

	/**
	 * Number of streams open at the same time in {@link #testOpenStreamsHoldNoThreads()}.
	 */
	private static final int OPEN_STREAMS = 3 * SERVER_THREADS;

	private Server _jetty;

	private HttpServer _upstream;

	private ExecutorService _upstreamThreads;

	private String _controlOrigin;

	private HttpClient _client;

	/** The requests the application received, by path. */
	private final Map<String, Received> _received = new ConcurrentHashMap<>();

	private final List<String> _paths = new CopyOnWriteArrayList<>();

	private final CountDownLatch _firstEventRead = new CountDownLatch(1);

	/** Counted down when the stream of the path {@code /ticking} cannot be written any more. */
	private final CountDownLatch _tickingClosed = new CountDownLatch(1);

	/** Ends the streams of the path {@code /held}. */
	private final CountDownLatch _releaseHeld = new CountDownLatch(1);

	private record Received(String method, String query, Map<String, List<String>> headers, String body) {

		String header(String name) {
			for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(name)) {
					return String.join(", ", entry.getValue());
				}
			}
			return null;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_upstream = HttpServer.create(new InetSocketAddress(LOOPBACK, 0), 0);
		_upstream.createContext("/", this::answer);
		_upstreamThreads = Executors.newCachedThreadPool();
		_upstream.setExecutor(_upstreamThreads);
		_upstream.start();
		String upstreamOrigin = "http://" + LOOPBACK + ":" + _upstream.getAddress().getPort();
		String silentOrigin = "http://" + LOOPBACK + ":" + freePort();

		PreviewWorkspaces workspaces = (request, workspaceId) -> {
			HttpSession session = request.getSession(false);
			if (session == null || session.getAttribute(LOGGED_IN) == null) {
				return PreviewLookup.refused(PreviewRefusal.NOT_LOGGED_IN);
			}
			switch (workspaceId) {
				case WORKSPACE:
					return PreviewLookup.found(new PreviewTarget(APP_CONTEXT, upstreamOrigin));
				case SILENT_WORKSPACE:
					return PreviewLookup.found(new PreviewTarget(SILENT_CONTEXT, silentOrigin));
				case STOPPED_WORKSPACE:
					return PreviewLookup.refused(PreviewRefusal.NOT_RUNNING);
				default:
					return PreviewLookup.refused(PreviewRefusal.NOT_FOUND);
			}
		};

		QueuedThreadPool threads = new QueuedThreadPool(SERVER_THREADS);
		threads.setReservedThreads(0);
		_jetty = new Server(threads);
		ServerConnector connector = new ServerConnector(_jetty, 1, 1);
		connector.setHost(LOOPBACK);
		connector.setPort(0);
		_jetty.addConnector(connector);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(CONTROL_CONTEXT);
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
				req.getSession(true).setAttribute(LOGGED_IN, Boolean.TRUE);
				resp.getWriter().write(req.getSession().getId());
			}
		}), LOGIN_PATH);
		ServletHolder proxyHolder = new ServletHolder(new PreviewServlet(workspaces));
		// The proxy answers asynchronously.
		proxyHolder.setAsyncSupported(true);
		context.addServlet(proxyHolder, ApplicationWorkspaces.PREVIEW_PATH + "/*");
		_jetty.setHandler(context);
		_jetty.start();
		_controlOrigin = "http://" + LOOPBACK + ":" + connector.getLocalPort();
		_client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
	}

	@Override
	protected void tearDown() throws Exception {
		_client.close();
		_jetty.stop();
		_upstream.stop(0);
		_upstreamThreads.shutdownNow();
		super.tearDown();
	}

	private static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName(LOOPBACK))) {
			return socket.getLocalPort();
		}
	}

	private void answer(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getRawPath();
		byte[] body = exchange.getRequestBody().readAllBytes();
		_paths.add(path);
		_received.put(path, new Received(exchange.getRequestMethod(), exchange.getRequestURI().getRawQuery(),
			Map.copyOf(exchange.getRequestHeaders()), new String(body, StandardCharsets.UTF_8)));
		String local = path.substring(APP_CONTEXT.length());
		switch (local) {
			case "/redirect":
				exchange.getResponseHeaders().add("Location", APP_CONTEXT + "/target");
				exchange.sendResponseHeaders(302, -1);
				break;
			case "/cookies":
				exchange.getResponseHeaders().add(ProxyForwarding.SET_COOKIE, "WS=1; Path=" + APP_CONTEXT);
				exchange.getResponseHeaders().add(ProxyForwarding.SET_COOKIE, "PLAIN=1");
				exchange.getResponseHeaders().add(ProxyForwarding.SET_COOKIE, SESSION_COOKIE + "=evil; Path=" + CONTROL_CONTEXT);
				exchange.getResponseHeaders().add(ProxyForwarding.SET_COOKIE, "DOMAIN=1; Domain=example.com; Path=" + APP_CONTEXT);
				exchange.getResponseHeaders().add(ProxyForwarding.SET_COOKIE,
					"OTHER=1; Path=" + CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/ws2/app");
				exchange.sendResponseHeaders(204, -1);
				break;
			case "/events":
				exchange.getResponseHeaders().add(ProxyForwarding.CONTENT_TYPE, "text/event-stream");
				exchange.sendResponseHeaders(200, 0);
				try (OutputStream out = exchange.getResponseBody()) {
					out.write(": connected\n\n".getBytes(StandardCharsets.UTF_8));
					out.write("data: 1\n\n".getBytes(StandardCharsets.UTF_8));
					out.flush();
					// The client reads the first event while the stream is still open.
					if (!_firstEventRead.await(WAIT.toSeconds(), TimeUnit.SECONDS)) {
						return;
					}
					out.write("data: 2\n\n".getBytes(StandardCharsets.UTF_8));
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				break;
			case "/ticking":
				exchange.getResponseHeaders().add(ProxyForwarding.CONTENT_TYPE, "text/event-stream");
				exchange.sendResponseHeaders(200, 0);
				try (OutputStream out = exchange.getResponseBody()) {
					long end = System.currentTimeMillis() + WAIT.toMillis();
					while (System.currentTimeMillis() < end) {
						out.write("data: tick\n\n".getBytes(StandardCharsets.UTF_8));
						out.flush();
						Thread.sleep(50);
					}
				} catch (IOException ex) {
					_tickingClosed.countDown();
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				break;
			case "/held":
				exchange.getResponseHeaders().add(ProxyForwarding.CONTENT_TYPE, "text/event-stream");
				exchange.sendResponseHeaders(200, 0);
				try (OutputStream out = exchange.getResponseBody()) {
					out.write("data: open\n\n".getBytes(StandardCharsets.UTF_8));
					out.flush();
					_releaseHeld.await(WAIT.toSeconds(), TimeUnit.SECONDS);
					out.write("data: end\n\n".getBytes(StandardCharsets.UTF_8));
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				} catch (IOException ex) {
					// The proxy closed the stream.
				}
				break;
			default:
				byte[] answer = ("echo " + local).getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add(ProxyForwarding.CONTENT_TYPE, ProxyForwarding.TEXT_CONTENT_TYPE);
				exchange.getResponseHeaders().add("X-Upstream", "yes");
				exchange.sendResponseHeaders(200, answer.length);
				try (OutputStream out = exchange.getResponseBody()) {
					out.write(answer);
				}
		}
		exchange.close();
	}

	public void testRefused() throws Exception {
		assertEquals("Not logged in.", PreviewRefusal.NOT_LOGGED_IN.status(), get(APP_CONTEXT + "/x", null).statusCode());
		String cookie = login();
		assertEquals(404, get(CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/", cookie).statusCode());
		assertEquals(404, get(CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/unknown/app/", cookie).statusCode());
		assertEquals(503, get(CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/" + STOPPED_WORKSPACE + "/app/", cookie)
			.statusCode());
		assertEquals("Not below the context path of the application.", 404,
			get(CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/" + WORKSPACE + "/other/x", cookie).statusCode());
		int encodedParent = get(APP_CONTEXT + "/%2e%2e/%2e%2e/other/x", cookie).statusCode();
		assertTrue("Encoded parent segments: " + encodedParent, encodedParent == 404 || encodedParent == 400);
		assertTrue("Refused requests reached the application: " + _paths, _paths.isEmpty());
		HttpResponse<String> silent = get(SILENT_CONTEXT + "/", cookie);
		assertEquals(PreviewRefusal.NOT_ANSWERING.status(), silent.statusCode());
		assertEquals(PreviewRefusal.NOT_ANSWERING.message(), silent.body());
	}

	/**
	 * Method, path, query, body and headers reach the application unchanged, with the forwarded
	 * headers; its answer comes back with its headers.
	 */
	public void testForwarded() throws Exception {
		String cookie = login();
		HttpResponse<String> response = _client.send(HttpRequest.newBuilder(URI.create(_controlOrigin + APP_CONTEXT
			+ "/servlet/x%20y?a=1&b=%2F"))
			.header(ProxyForwarding.COOKIE, cookie)
			.header("X-Custom", "value")
			.header(ProxyForwarding.AUTHORIZATION, "Basic YTpi")
			.POST(HttpRequest.BodyPublishers.ofString("payload"))
			.build(), HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
		assertEquals("echo /servlet/x%20y", response.body());
		assertEquals("yes", response.headers().firstValue("X-Upstream").orElse(null));

		Received received = _received.get(APP_CONTEXT + "/servlet/x%20y");
		assertEquals("POST", received.method());
		assertEquals("a=1&b=%2F", received.query());
		assertEquals("payload", received.body());
		assertEquals("value", received.header("X-Custom"));
		assertEquals("The credentials of the application pass.", "Basic YTpi",
			received.header(ProxyForwarding.AUTHORIZATION));
		assertEquals(LOOPBACK, received.header(PreviewServlet.X_FORWARDED_FOR));
		assertEquals("http", received.header(PreviewServlet.X_FORWARDED_PROTO));
		assertEquals(URI.create(_controlOrigin).getAuthority(), received.header(PreviewServlet.X_FORWARDED_HOST));
		assertNull("The session cookie of the control application reached the application.",
			received.header(ProxyForwarding.COOKIE));

		get(APP_CONTEXT + "/", cookie);
		assertEquals("GET", _received.get(APP_CONTEXT + "/").method());
		get(APP_CONTEXT, cookie);
		assertEquals("The context path itself.", "GET", _received.get(APP_CONTEXT).method());
	}

	/**
	 * A redirect of the application is passed back, not followed: it stays below the origin of the
	 * control application.
	 */
	public void testRedirect() throws Exception {
		HttpResponse<String> response = get(APP_CONTEXT + "/redirect", login());
		assertEquals(302, response.statusCode());
		assertEquals(APP_CONTEXT + "/target", response.headers().firstValue("Location").orElse(null));
		assertNull(_received.get(APP_CONTEXT + "/target"));
	}

	/**
	 * The application gets its own cookies, also a session cookie with the name of the one of the
	 * control application, but never the session cookie of the control application.
	 */
	public void testRequestCookies() throws Exception {
		String control = login();
		String cookies = SESSION_COOKIE + "=node0workspace.node0; " + control + "; theme=dark";
		get(APP_CONTEXT + "/cookies-in", cookies);
		assertEquals(SESSION_COOKIE + "=node0workspace.node0; theme=dark",
			_received.get(APP_CONTEXT + "/cookies-in").header(ProxyForwarding.COOKIE));
	}

	/**
	 * Only the cookies of the application confined to its context path reach the browser.
	 */
	public void testResponseCookies() throws Exception {
		HttpResponse<String> response = get(APP_CONTEXT + "/cookies", login());
		assertEquals(204, response.statusCode());
		assertEquals(List.of("WS=1; Path=" + APP_CONTEXT, "PLAIN=1"),
			response.headers().allValues(ProxyForwarding.SET_COOKIE));
	}

	/**
	 * A stream reaches the client while the application still produces it.
	 */
	public void testStream() throws Exception {
		HttpResponse<InputStream> response = _client.send(
			HttpRequest.newBuilder(URI.create(_controlOrigin + APP_CONTEXT + "/events"))
				.header(ProxyForwarding.COOKIE, login())
				.build(),
			HttpResponse.BodyHandlers.ofInputStream());
		assertEquals(200, response.statusCode());
		assertEquals("text/event-stream", response.headers().firstValue(ProxyForwarding.CONTENT_TYPE).orElse(null));
		try (BufferedReader reader =
			new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
			assertEquals(": connected", reader.readLine());
			assertEquals("", reader.readLine());
			assertEquals("data: 1", reader.readLine());
			_firstEventRead.countDown();
			assertEquals("", reader.readLine());
			assertEquals("data: 2", reader.readLine());
		}
	}

	/**
	 * Open streams hold no thread of the control application: more streams than the server has
	 * threads all receive their events, the server still answers another request meanwhile, and
	 * each stream ends when the application ends it.
	 */
	public void testOpenStreamsHoldNoThreads() throws Exception {
		String cookie = login();
		List<BufferedReader> readers = new ArrayList<>();
		try {
			for (int n = 0; n < OPEN_STREAMS; n++) {
				HttpResponse<InputStream> response = _client.send(
					HttpRequest.newBuilder(URI.create(_controlOrigin + APP_CONTEXT + "/held"))
						.header(ProxyForwarding.COOKIE, cookie)
						.timeout(WAIT)
						.build(),
					HttpResponse.BodyHandlers.ofInputStream());
				assertEquals("Stream " + n, 200, response.statusCode());
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
				readers.add(reader);
				assertEquals("Stream " + n, "data: open", reader.readLine());
			}
			assertEquals("Another request while all streams are open.", "echo /x", get(APP_CONTEXT + "/x", cookie).body());
			_releaseHeld.countDown();
			for (BufferedReader reader : readers) {
				assertEquals("", reader.readLine());
				assertEquals("data: end", reader.readLine());
				assertEquals("", reader.readLine());
				assertNull("The stream ends with the one of the application.", reader.readLine());
			}
		} finally {
			_releaseHeld.countDown();
			for (BufferedReader reader : readers) {
				reader.close();
			}
		}
	}

	/**
	 * A client that goes away closes the connection to the application.
	 */
	public void testClientGoneClosesUpstream() throws Exception {
		HttpResponse<InputStream> response = _client.send(
			HttpRequest.newBuilder(URI.create(_controlOrigin + APP_CONTEXT + "/ticking"))
				.header(ProxyForwarding.COOKIE, login())
				.build(),
			HttpResponse.BodyHandlers.ofInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
		assertEquals("data: tick", reader.readLine());
		reader.close();
		assertTrue("The application still writes to the closed stream.",
			_tickingClosed.await(WAIT.toSeconds(), TimeUnit.SECONDS));
	}

	/**
	 * Logs into the control application.
	 *
	 * @return The session cookie of the control application, as the browser sends it.
	 */
	private String login() throws Exception {
		HttpResponse<String> response = get(CONTROL_CONTEXT + LOGIN_PATH, null);
		assertEquals(200, response.statusCode());
		String setCookie = response.headers().firstValue(ProxyForwarding.SET_COOKIE).orElseThrow();
		return setCookie.split(";", 2)[0];
	}

	private HttpResponse<String> get(String path, String cookie) throws Exception {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(_controlOrigin + path)).timeout(WAIT);
		if (cookie != null) {
			request.header(ProxyForwarding.COOKIE, cookie);
		}
		return _client.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

}
