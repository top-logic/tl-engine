/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import junit.framework.TestCase;

import test.com.top_logic.factory.proxy.HeldBackBody;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.WorkspaceProxy;
import com.top_logic.factory.proxy.WorkspaceProxyHandler;
import com.top_logic.factory.proxy.WorkspaceProxyListener;
import com.top_logic.factory.proxy.WorkspaceProxyService;

/**
 * Test of the {@link GitRoute git route} of the workspace proxy on a port of its own, the
 * {@link WorkspaceProxyListener}: the hosting service is a fake upstream server recording what it
 * receives.
 *
 * <p>
 * {@link TestGitRouteServlet} runs the same tests against the proxy in the web application.
 * </p>
 */
public class TestGitRoute extends TestCase {

	/**
	 * The loopback address the proxy is reached at.
	 */
	protected static final String LOOPBACK = "127.0.0.1";

	private static final String OWNER = "factory";

	private static final RepositoryName ALPHA = new RepositoryName(OWNER, "alpha");

	private static final RepositoryName BETA = new RepositoryName(OWNER, "beta");

	private static final String TOKEN_ALPHA = "tlwst_alpha";

	private static final String TOKEN_BETA = "tlwst_beta";

	private static final String TOKEN_NO_REPOSITORY = "tlwst_none";

	private static final String WS_ALPHA = "ws-alpha";

	private static final String WS_BETA = "ws-beta";

	private static final String WS_NO_REPOSITORY = "ws-none";

	private static final String UPSTREAM_TOKEN = "gitea-token-of-the-control-application";

	private static final String FRESH_UPSTREAM_TOKEN = "fresh-gitea-token";

	private static final String TOKEN_SCHEME = "token ";

	private static final String ADVERTISEMENT_TYPE = "application/x-git-upload-pack-advertisement";

	private static final String ADVERTISEMENT = "001e# service=git-upload-pack\n0000";

	private static final String PROTOCOL_V2 = "version=2";

	/**
	 * Size of the body of {@link #testLargePushStreamed()}, more than any buffer of the proxy.
	 */
	private static final long LARGE_BODY_SIZE = 40L * 1024 * 1024;

	private HttpServer _upstream;

	private RecordingGitHostingProvider _provider;

	private boolean _providerConfigured = true;

	private GitRoute _route;

	/**
	 * The URL of the git route, {@link GitRoute#DEFAULT_PATH} included.
	 */
	private String _routeUrl;

	private HttpClient _client;

	private WorkspaceProxyListener _listener;

	private final List<Recorded> _received = new CopyOnWriteArrayList<>();

	private final AtomicInteger _invalidations = new AtomicInteger();

	private volatile String _upstreamToken = UPSTREAM_TOKEN;

	/**
	 * The token the fake upstream accepts, {@code null} for every request.
	 */
	private volatile String _acceptedToken = UPSTREAM_TOKEN;

	/**
	 * What the fake upstream received.
	 */
	private record Recorded(String method, String path, String query, Headers headers, byte[] body) {
		// Plain data.
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_upstream = HttpServer.create(new InetSocketAddress(LOOPBACK, 0), 0);
		_upstream.createContext("/", this::answer);
		_upstream.start();

		CredentialProvider credentials = new CredentialProvider() {
			@Override
			public void authenticate(RequestAuthentication request) {
				request.setHeader(CredentialProvider.AUTHORIZATION, TOKEN_SCHEME + _upstreamToken);
			}

			@Override
			public void invalidate() {
				_invalidations.incrementAndGet();
				_upstreamToken = FRESH_UPSTREAM_TOKEN;
			}
		};
		_provider = new RecordingGitHostingProvider(OWNER,
			"http://" + LOOPBACK + ":" + _upstream.getAddress().getPort() + "/", credentials);
		Map<String, RepositoryName> repositories = Map.of(WS_ALPHA, ALPHA, WS_BETA, BETA);
		_route = new GitRoute(GitRoute.DEFAULT_PATH,
			workspace -> _providerConfigured ? GitRoute.Target.of(_provider, repositories.get(workspace)) : null,
			Duration.ofSeconds(5), Duration.ofMinutes(2));

		WorkspaceProxy proxy = new WorkspaceProxy() {
			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean isAllowedSource(String remoteAddress) {
				return true;
			}

			@Override
			public ProxyAdmission admit(String token, String remoteAddress) {
				if (TOKEN_ALPHA.equals(token)) {
					return ProxyAdmission.admitted(WS_ALPHA);
				}
				if (TOKEN_BETA.equals(token)) {
					return ProxyAdmission.admitted(WS_BETA);
				}
				if (TOKEN_NO_REPOSITORY.equals(token)) {
					return ProxyAdmission.admitted(WS_NO_REPOSITORY);
				}
				return ProxyAdmission.INVALID_TOKEN;
			}

			@Override
			public List<GitRoute> routes() {
				return List.of(_route);
			}
		};
		_routeUrl = startProxy(proxy);
		_client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	}

	@Override
	protected void tearDown() throws Exception {
		_client.close();
		stopProxy();
		_route.close();
		_upstream.stop(0);
		super.tearDown();
	}

	/**
	 * Starts the proxy serving the given {@link WorkspaceProxy}.
	 *
	 * @return The URL of the git route.
	 */
	protected String startProxy(WorkspaceProxy proxy) throws Exception {
		_listener = new WorkspaceProxyListener(new InetSocketAddress(LOOPBACK, 0), () -> proxy);
		return "http://" + LOOPBACK + ":" + _listener.getAddress().getPort() + GitRoute.DEFAULT_PATH;
	}

	/**
	 * Stops the proxy started by {@link #startProxy(WorkspaceProxy)}.
	 */
	protected void stopProxy() throws Exception {
		_listener.close();
	}

	/**
	 * The fake hosting service: records the request, rejects other credentials than
	 * {@link #_acceptedToken} and answers with a ref advertisement or the request body.
	 */
	private void answer(HttpExchange exchange) throws IOException {
		byte[] body = exchange.getRequestBody().readAllBytes();
		_received.add(new Recorded(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
			exchange.getRequestURI().getRawQuery(), exchange.getRequestHeaders(), body));
		String accepted = _acceptedToken;
		if (accepted == null
			|| !(TOKEN_SCHEME + accepted).equals(exchange.getRequestHeaders().getFirst(ProxyForwarding.AUTHORIZATION))) {
			exchange.getResponseHeaders().add(GitRoute.WWW_AUTHENTICATE, "Basic realm=\"Gitea\"");
			exchange.sendResponseHeaders(401, -1);
			exchange.close();
			return;
		}
		byte[] answer;
		if ("GET".equals(exchange.getRequestMethod())) {
			exchange.getResponseHeaders().add(ProxyForwarding.CONTENT_TYPE, ADVERTISEMENT_TYPE);
			exchange.getResponseHeaders().add("Cache-Control", "no-cache, max-age=0, must-revalidate");
			exchange.getResponseHeaders().add("Set-Cookie", "i_like_gitea=upstream-session");
			answer = ADVERTISEMENT.getBytes(StandardCharsets.UTF_8);
		} else {
			exchange.getResponseHeaders().add(ProxyForwarding.CONTENT_TYPE, "application/x-git-receive-pack-result");
			answer = body;
		}
		exchange.sendResponseHeaders(200, answer.length == 0 ? -1 : answer.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(answer);
		}
	}

	/**
	 * The ref advertisement is forwarded with the credentials of the control application, the
	 * headers of git's protocol and nothing else of the client.
	 */
	public void testAdvertisementForwarded() throws Exception {
		HttpResponse<String> response = send(HttpRequest.newBuilder(route("/factory/alpha.git/info/refs?service="
			+ GitRoute.UPLOAD_PACK))
			.header(ProxyForwarding.AUTHORIZATION, basic(TOKEN_ALPHA))
			.header(GitRoute.GIT_PROTOCOL, PROTOCOL_V2)
			.header(GitRoute.ACCEPT, "*/*")
			.header(GitRoute.USER_AGENT, "git/2.43.0")
			.header("Cookie", "JSESSIONID=control-session")
			.header("X-WEBAUTH-USER", "admin")
			.header("X-Forwarded-For", "10.0.0.1")
			.GET());

		assertEquals(200, response.statusCode());
		assertEquals(ADVERTISEMENT, response.body());
		assertEquals(ADVERTISEMENT_TYPE, response.headers().firstValue(ProxyForwarding.CONTENT_TYPE).orElse(null));
		assertEquals("no-cache, max-age=0, must-revalidate", response.headers().firstValue("Cache-Control").orElse(null));
		assertTrue("Cookies of the upstream are not passed back.", response.headers().allValues("Set-Cookie").isEmpty());

		Recorded received = single();
		assertEquals("GET", received.method());
		assertEquals("/factory/alpha.git/info/refs", received.path());
		assertEquals("service=" + GitRoute.UPLOAD_PACK, received.query());
		assertEquals(List.of(TOKEN_SCHEME + UPSTREAM_TOKEN), received.headers().get(ProxyForwarding.AUTHORIZATION));
		assertEquals(PROTOCOL_V2, received.headers().getFirst(GitRoute.GIT_PROTOCOL));
		assertEquals("*/*", received.headers().getFirst(GitRoute.ACCEPT));
		assertEquals("git/2.43.0", received.headers().getFirst(GitRoute.USER_AGENT));
		assertNull(received.headers().get("Cookie"));
		assertNull(received.headers().get("X-WEBAUTH-USER"));
		assertNull(received.headers().get("X-Forwarded-For"));
	}

	/**
	 * A compressed fetch request without {@link RepositoryName#GIT_SUFFIX} is forwarded with its
	 * encoding, its body unchanged.
	 */
	public void testUploadPackForwarded() throws Exception {
		byte[] body = gzip("0032want 0123456789012345678901234567890123456789\n00000009done\n");
		HttpResponse<byte[]> response = _client.send(HttpRequest.newBuilder(route("/factory/alpha/git-upload-pack"))
			.timeout(Duration.ofSeconds(20))
			.header(ProxyForwarding.AUTHORIZATION, WorkspaceProxyHandler.BEARER_PREFIX + TOKEN_ALPHA)
			.header(ProxyForwarding.CONTENT_TYPE, "application/x-git-upload-pack-request")
			.header(GitRoute.CONTENT_ENCODING, "gzip")
			.header(GitRoute.GIT_PROTOCOL, PROTOCOL_V2)
			.POST(HttpRequest.BodyPublishers.ofByteArray(body))
			.build(), HttpResponse.BodyHandlers.ofByteArray());

		assertEquals(200, response.statusCode());
		assertTrue(Arrays.equals(body, response.body()));
		Recorded received = single();
		assertEquals("POST", received.method());
		assertEquals("/factory/alpha.git/git-upload-pack", received.path());
		assertNull(received.query());
		assertTrue(Arrays.equals(body, received.body()));
		assertEquals("gzip", received.headers().getFirst(GitRoute.CONTENT_ENCODING));
		assertEquals("application/x-git-upload-pack-request", received.headers().getFirst(ProxyForwarding.CONTENT_TYPE));
		assertEquals(PROTOCOL_V2, received.headers().getFirst(GitRoute.GIT_PROTOCOL));
		assertEquals(List.of(TOKEN_SCHEME + UPSTREAM_TOKEN), received.headers().get(ProxyForwarding.AUTHORIZATION));
	}

	/**
	 * Owner and name are compared without regard to case; the request goes to the repository as it
	 * is stored.
	 */
	public void testRepositoryIgnoresCase() throws Exception {
		HttpResponse<String> response = send(authorized("/FACTORY/Alpha.git/info/refs?service=" + GitRoute.RECEIVE_PACK).GET());
		assertEquals(200, response.statusCode());
		assertEquals("/factory/alpha.git/info/refs", single().path());
		assertEquals("service=" + GitRoute.RECEIVE_PACK, single().query());
	}

	/**
	 * Only the requests of the smart HTTP protocol are forwarded.
	 */
	public void testOtherRequestsNotFound() throws Exception {
		String[][] requests = {
			{ "GET", "/factory/alpha.git/info/refs" },
			{ "GET", "/factory/alpha.git/info/refs?service=git-upload-archive" },
			{ "POST", "/factory/alpha.git/info/refs?service=git-upload-pack" },
			{ "GET", "/factory/alpha.git/git-upload-pack" },
			{ "GET", "/factory/alpha.git/git-receive-pack" },
			{ "GET", "/factory/alpha.git/HEAD" },
			{ "GET", "/factory/alpha.git/objects/info/packs" },
			{ "POST", "/factory/alpha.git/info/lfs/objects/batch" },
			{ "GET", "/factory/alpha.git" },
			{ "GET", "/factory" },
			{ "GET", "" },
			{ "GET", "/" },
			{ "POST", "/factory/alpha.git/git-receive-pack/" },
			{ "POST", "/factory/.git/git-receive-pack" },
			{ "DELETE", "/factory/alpha.git/git-receive-pack" },
		};
		assertNotFound(requests);
	}

	/**
	 * A path with dot or empty segments is not resolved by the route.
	 */
	public void testDotSegmentsNotFound() throws Exception {
		assertNotFound(new String[][] {
			{ "POST", "/factory/../factory/alpha.git/git-receive-pack" },
			{ "POST", "/../alpha.git/git-receive-pack" },
			{ "POST", "/factory/./alpha.git/git-receive-pack" },
			{ "POST", "/factory//alpha.git/git-receive-pack" },
		});
	}

	private void assertNotFound(String[][] requests) throws IOException, InterruptedException {
		for (String[] request : requests) {
			HttpResponse<String> response = send(authorized(request[1])
				.method(request[0], HttpRequest.BodyPublishers.ofString("0000")));
			assertEquals(request[0] + " " + request[1], 404, response.statusCode());
		}
		assertTrue("Nothing reached the hosting service: " + _received, _received.isEmpty());
	}

	/**
	 * The repository of another project and any repository of a workspace without one are not
	 * found.
	 */
	public void testForeignRepositoryNotFound() throws Exception {
		assertEquals(404, send(authorized("/factory/beta.git/info/refs?service=git-receive-pack").GET()).statusCode());
		assertEquals(404, send(authorized("/factory/beta.git/git-receive-pack")
			.POST(HttpRequest.BodyPublishers.ofString("0000"))).statusCode());
		assertEquals(404, send(authorized("/other/alpha.git/info/refs?service=git-upload-pack").GET()).statusCode());
		assertEquals(404, send(HttpRequest.newBuilder(route("/factory/alpha.git/info/refs?service=git-upload-pack"))
			.header(ProxyForwarding.AUTHORIZATION, basic(TOKEN_NO_REPOSITORY)).GET()).statusCode());
		assertTrue(_received.isEmpty());

		assertEquals("The other project has access to its own.", 200,
			send(HttpRequest.newBuilder(route("/factory/beta.git/info/refs?service=git-receive-pack"))
				.header(ProxyForwarding.AUTHORIZATION, basic(TOKEN_BETA)).GET()).statusCode());
	}

	/**
	 * Without git hosting, the route answers every request with 404.
	 */
	public void testNoProviderNotFound() throws Exception {
		_providerConfigured = false;
		assertEquals(404, send(authorized("/factory/alpha.git/info/refs?service=git-upload-pack").GET()).statusCode());
		assertTrue(_received.isEmpty());
	}

	/**
	 * A request without valid token is challenged for basic authentication, so that git asks its
	 * credential helper.
	 */
	public void testChallengeWithoutToken() throws Exception {
		String path = "/factory/alpha.git/info/refs?service=git-upload-pack";
		List<HttpRequest.Builder> requests = new ArrayList<>();
		requests.add(HttpRequest.newBuilder(route(path)));
		requests.add(HttpRequest.newBuilder(route(path)).header(ProxyForwarding.AUTHORIZATION, basic("tlwst_unknown")));
		requests.add(HttpRequest.newBuilder(route(path))
			.header(ProxyForwarding.AUTHORIZATION, WorkspaceProxyHandler.BEARER_PREFIX + "tlwst_unknown"));
		for (HttpRequest.Builder request : requests) {
			HttpResponse<String> response = send(request.GET());
			assertEquals(401, response.statusCode());
			assertEquals("Basic realm=\"" + GitRoute.REALM + "\"",
				response.headers().firstValue(GitRoute.WWW_AUTHENTICATE).orElse(null));
		}
		assertTrue(_received.isEmpty());
	}

	/**
	 * Credentials the hosting service rejects are invalidated, and a ref advertisement is
	 * requested once more with fresh ones.
	 */
	public void testRejectedCredentialsRetriedForAdvertisement() throws Exception {
		_acceptedToken = FRESH_UPSTREAM_TOKEN;
		HttpResponse<String> response = send(authorized("/factory/alpha.git/info/refs?service=git-upload-pack").GET());
		assertEquals(200, response.statusCode());
		assertEquals(1, _invalidations.get());
		assertEquals(2, _received.size());
		assertEquals(TOKEN_SCHEME + FRESH_UPSTREAM_TOKEN,
			_received.get(1).headers().getFirst(ProxyForwarding.AUTHORIZATION));
	}

	/**
	 * A request with body whose credentials the hosting service rejects cannot be repeated: the
	 * credentials are invalidated and the client gets 502, not a challenge for its own token.
	 */
	public void testRejectedCredentialsOfPush() throws Exception {
		_acceptedToken = null;
		HttpResponse<String> response = send(authorized("/factory/alpha.git/git-receive-pack")
			.POST(HttpRequest.BodyPublishers.ofString("0000")));
		assertEquals(502, response.statusCode());
		assertTrue(response.headers().firstValue(GitRoute.WWW_AUTHENTICATE).isEmpty());
		assertEquals(1, _invalidations.get());
		assertEquals(1, _received.size());

		HttpResponse<String> advertisement = send(authorized("/factory/alpha.git/info/refs?service=git-receive-pack").GET());
		assertEquals("Rejected also after the retry.", 502, advertisement.statusCode());
		assertEquals(3, _invalidations.get());
	}

	/**
	 * A push larger than any buffer is streamed to the hosting service while the client sends it.
	 */
	public void testLargePushStreamed() throws Exception {
		CountDownLatch upstreamReading = new CountDownLatch(1);
		long[] received = { 0 };
		_upstream.removeContext("/");
		_upstream.createContext("/", exchange -> {
			long count = 0;
			byte[] buffer = new byte[64 * 1024];
			try (InputStream in = exchange.getRequestBody()) {
				int n;
				while ((n = in.read(buffer)) >= 0) {
					count += n;
					upstreamReading.countDown();
				}
			}
			received[0] = count;
			byte[] answer = "0000".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, answer.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(answer);
			}
		});

		long size = LARGE_BODY_SIZE;
		HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.fromPublisher(
			HttpRequest.BodyPublishers.ofInputStream(() -> new HeldBackBody(size, upstreamReading)), size);
		HttpResponse<String> response = _client.send(authorized("/factory/alpha.git/git-receive-pack")
			.timeout(Duration.ofMinutes(2))
			.header(ProxyForwarding.CONTENT_TYPE, "application/x-git-receive-pack-request")
			.POST(body)
			.build(), HttpResponse.BodyHandlers.ofString());

		assertEquals(response.body(), 200, response.statusCode());
		assertEquals(size, received[0]);
	}

	/**
	 * The environment of a process makes git answer the proxy with the token, through the
	 * credential helper of the route only, and nothing for other hosts.
	 */
	public void testCredentialHelper() throws Exception {
		if (!gitAvailable()) {
			System.err.println("Skipping " + getName() + ": no git on the path.");
			return;
		}
		String proxyRoot = _routeUrl.substring(0, _routeUrl.length() - GitRoute.DEFAULT_PATH.length());
		String token = "tlwst_secret-of-the-process";
		Map<String, String> env = WorkspaceProxyService.processEnv(List.of(_route), proxyRoot, token);
		assertEquals(token, env.get(ProxyTokens.ENV_PROXY_TOKEN));

		Path home = Files.createTempDirectory("git-route-test");
		try {
			// A helper of the user's configuration, which the route's entries drop for its URL.
			Path global = home.resolve(".gitconfig");
			Files.writeString(global,
				"[credential]\n\thelper = \"!f() { echo username=user; echo password=wrong; }; f\"\n");

			String remote = GitRoute.remoteUrl(_routeUrl, ALPHA);
			String answer = credentialFill(home, global, env, remote);
			assertTrue(answer, answer.contains("username=" + GitRoute.HELPER_USERNAME + "\n"));
			assertTrue(answer, answer.contains("password=" + token + "\n"));

			String elsewhere = credentialFill(home, global, env, "https://git.example.com/factory/alpha.git");
			assertTrue("Another host gets the user's helper: " + elsewhere, elsewhere.contains("password=wrong\n"));
			assertFalse(elsewhere.contains(token));
		} finally {
			deleteTree(home);
		}
	}

	private static String credentialFill(Path home, Path global, Map<String, String> env, String url)
			throws IOException, InterruptedException {
		ProcessBuilder git = new ProcessBuilder("git", "credential", "fill");
		git.environment().putAll(env);
		git.environment().put("HOME", home.toString());
		git.environment().put("GIT_CONFIG_NOSYSTEM", "1");
		git.environment().put("GIT_CONFIG_GLOBAL", global.toString());
		git.environment().put("GIT_TERMINAL_PROMPT", "0");
		git.redirectErrorStream(true);
		Process process = git.start();
		URI uri = URI.create(url);
		String host = uri.getPort() < 0 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
		try (OutputStream in = process.getOutputStream()) {
			in.write(("protocol=" + uri.getScheme() + "\nhost=" + host + "\npath=" + uri.getPath().substring(1) + "\n\n")
				.getBytes(StandardCharsets.UTF_8));
		}
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertTrue("git credential fill did not end.", process.waitFor(30, TimeUnit.SECONDS));
		return output;
	}

	private static boolean gitAvailable() {
		try {
			Process process = new ProcessBuilder("git", "--version").redirectErrorStream(true).start();
			process.getInputStream().readAllBytes();
			return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;
		} catch (IOException | InterruptedException ex) {
			return false;
		}
	}

	private static void deleteTree(Path root) throws IOException {
		try (var paths = Files.walk(root)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(path);
			}
		}
	}

	private Recorded single() {
		assertEquals(String.valueOf(_received), 1, _received.size());
		return _received.get(0);
	}

	private static byte[] gzip(String text) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try (GZIPOutputStream out = new GZIPOutputStream(buffer)) {
			out.write(text.getBytes(StandardCharsets.UTF_8));
		}
		return buffer.toByteArray();
	}

	private static String basic(String token) {
		return WorkspaceProxyHandler.BASIC_PREFIX + Base64.getEncoder()
			.encodeToString((GitRoute.HELPER_USERNAME + ":" + token).getBytes(StandardCharsets.UTF_8));
	}

	private URI route(String path) {
		return URI.create(_routeUrl + path);
	}

	private HttpRequest.Builder authorized(String path) {
		return HttpRequest.newBuilder(route(path))
			.timeout(Duration.ofSeconds(20))
			.header(ProxyForwarding.AUTHORIZATION, basic(TOKEN_ALPHA));
	}

	private HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
		return _client.send(request.timeout(Duration.ofSeconds(20)).build(), HttpResponse.BodyHandlers.ofString());
	}

}
