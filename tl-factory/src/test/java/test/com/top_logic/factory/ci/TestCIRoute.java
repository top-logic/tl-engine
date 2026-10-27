/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.ci;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.factory.ci.CIBuildState;
import com.top_logic.factory.ci.CIJob;
import com.top_logic.factory.ci.CIRoute;
import com.top_logic.factory.ci.I18NConstants;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyError;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.WorkspaceProxy;
import com.top_logic.factory.proxy.WorkspaceProxyListener;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of the {@link CIRoute CI route} of the workspace proxy on a port of its own, against a
 * {@link RecordingCIProvider}: the three requests of its API, the JSON they answer with, the
 * bounded console output, and the 404 for everything else, for a workspace without CI job and
 * while CI is switched off.
 */
public class TestCIRoute extends TestCase {

	private static final String LOOPBACK = "127.0.0.1";

	private static final String TOKEN_ALPHA = "tlwst_alpha";

	private static final String TOKEN_NO_JOB = "tlwst_none";

	private static final String WS_ALPHA = "ws-alpha";

	private static final String WS_NO_JOB = "ws-none";

	private static final int MAX_LOG = 100;

	private RecordingCIProvider _provider;

	private boolean _ciConfigured = true;

	private String _job;

	private CIRoute _route;

	private WorkspaceProxyListener _listener;

	private String _routeUrl;

	private HttpClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_provider = new RecordingCIProvider();
		RepositoryName name = new RepositoryName("factory", "alpha");
		CIJob job = _provider.ensureJob(new GitRepository(name, "https://git.test/factory/alpha.git",
			"https://git.test/factory/alpha", "main"), "Alpha");
		_job = job.name();
		Map<String, String> jobs = Map.of(WS_ALPHA, _job);
		_route = new CIRoute(CIRoute.DEFAULT_PATH, () -> _ciConfigured ? _provider : null, jobs::get, MAX_LOG);
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
				if (TOKEN_NO_JOB.equals(token)) {
					return ProxyAdmission.admitted(WS_NO_JOB);
				}
				return ProxyAdmission.INVALID_TOKEN;
			}

			@Override
			public List<CIRoute> routes() {
				return List.of(_route);
			}
		};
		_listener = new WorkspaceProxyListener(new InetSocketAddress(LOOPBACK, 0), () -> proxy);
		_routeUrl = "http://" + LOOPBACK + ":" + _listener.getAddress().getPort() + CIRoute.DEFAULT_PATH;
		_client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	}

	@Override
	protected void tearDown() throws Exception {
		_client.close();
		_listener.close();
		_route.close();
		super.tearDown();
	}

	/**
	 * Starting a build answers with its number and page.
	 */
	public void testStartBuild() throws Exception {
		HttpResponse<String> response = send("POST", "/" + CIRoute.BUILDS, TOKEN_ALPHA);
		assertEquals(response.body(), 200, response.statusCode());
		assertTrue(response.headers().firstValue(ProxyForwarding.CONTENT_TYPE).orElse("").startsWith("application/json"));
		Map<?, ?> json = (Map<?, ?>) JSON.fromString(response.body());
		assertEquals(_job, json.get(CIRoute.FIELD_JOB));
		assertEquals(1, ((Number) json.get(CIRoute.FIELD_NUMBER)).intValue());
		assertEquals(Boolean.FALSE, json.get(CIRoute.FIELD_QUEUED));
		assertEquals(RecordingCIProvider.jobUrl(_job) + "1/", json.get(CIRoute.FIELD_URL));
		assertEquals(List.of(RecordingCIProvider.ENSURE + " factory/alpha", RecordingCIProvider.START + " " + _job),
			_provider.calls());
	}

	/**
	 * A build waiting for an executor has no number yet.
	 */
	public void testStartQueuedBuild() throws Exception {
		_provider.setQueued(true);
		HttpResponse<String> response = send("POST", "/" + CIRoute.BUILDS, TOKEN_ALPHA);
		assertEquals(200, response.statusCode());
		Map<?, ?> json = (Map<?, ?>) JSON.fromString(response.body());
		assertTrue(json.containsKey(CIRoute.FIELD_NUMBER));
		assertNull(json.get(CIRoute.FIELD_NUMBER));
		assertEquals(Boolean.TRUE, json.get(CIRoute.FIELD_QUEUED));
		assertEquals(RecordingCIProvider.jobUrl(_job), json.get(CIRoute.FIELD_URL));
	}

	/**
	 * The last build is answered with all its fields; without a build, with 404.
	 */
	public void testLastBuild() throws Exception {
		String last = "/" + CIRoute.BUILDS + "/" + CIRoute.LAST;
		assertEquals(404, send("GET", last, TOKEN_ALPHA).statusCode());

		_provider.startBuild(_job);
		_provider.startBuild(_job);
		_provider.finish(_job, CIBuildState.UNSTABLE);
		HttpResponse<String> response = send("GET", last, TOKEN_ALPHA);
		assertEquals(response.body(), 200, response.statusCode());
		Map<?, ?> json = (Map<?, ?>) JSON.fromString(response.body());
		assertEquals(Map.of(
			CIRoute.FIELD_JOB, _job,
			CIRoute.FIELD_NUMBER, 2,
			CIRoute.FIELD_STATE, CIBuildState.UNSTABLE.name(),
			CIRoute.FIELD_URL, RecordingCIProvider.jobUrl(_job) + "2/",
			CIRoute.FIELD_START_TIME, "2026-10-27T10:15:30Z",
			CIRoute.FIELD_DURATION, 2000), normalize(json));
	}

	/**
	 * The console output is plain text, bounded to its last lines, a cut output marked.
	 */
	public void testConsole() throws Exception {
		_provider.startBuild(_job);
		_provider.setConsole(_job, 1, "short output\n");
		HttpResponse<String> response = send("GET", "/" + CIRoute.BUILDS + "/1/" + CIRoute.LOG, TOKEN_ALPHA);
		assertEquals(200, response.statusCode());
		assertEquals(ProxyForwarding.TEXT_CONTENT_TYPE, response.headers().firstValue(ProxyForwarding.CONTENT_TYPE).get());
		assertEquals("short output\n", response.body());

		StringBuilder longOutput = new StringBuilder();
		for (int n = 0; n < 50; n++) {
			longOutput.append("line ").append(n).append('\n');
		}
		_provider.setConsole(_job, 1, longOutput.toString());
		String body = send("GET", "/" + CIRoute.BUILDS + "/1/" + CIRoute.LOG, TOKEN_ALPHA).body();
		assertTrue(body, body.startsWith(CIRoute.TRUNCATED_MARK + "\n"));
		assertTrue(body, body.endsWith("line 49\n"));
		assertFalse(body, body.contains("line 1\n"));
		assertTrue(body, body.getBytes(StandardCharsets.UTF_8).length <= MAX_LOG + CIRoute.TRUNCATED_MARK.length() + 1);

		assertEquals("No such build.", 404, send("GET", "/" + CIRoute.BUILDS + "/7/" + CIRoute.LOG, TOKEN_ALPHA)
			.statusCode());
	}

	/**
	 * Every other path and method is not found.
	 */
	public void testOtherRequestsNotFound() throws Exception {
		for (String[] request : new String[][] {
			{ "GET", "/" + CIRoute.BUILDS },
			{ "POST", "/" + CIRoute.BUILDS + "/" + CIRoute.LAST },
			{ "GET", "/" + CIRoute.BUILDS + "/x/" + CIRoute.LOG },
			{ "GET", "/" + CIRoute.BUILDS + "/0/" + CIRoute.LOG },
			{ "GET", "/" + CIRoute.BUILDS + "/1/" + CIRoute.LOG + "/more" },
			{ "DELETE", "/" + CIRoute.BUILDS + "/1" },
			{ "GET", "/job/" + _job + "/api/json" },
			{ "GET", "" },
		}) {
			HttpResponse<String> response = send(request[0], request[1], TOKEN_ALPHA);
			assertEquals(request[0] + " " + request[1], 404, response.statusCode());
		}
		assertTrue("Nothing reached the CI server.", _provider.calls().size() == 1);
	}

	/**
	 * A workspace whose project has no CI job, and every workspace while CI is switched off, gets
	 * 404.
	 */
	public void testNoJob() throws Exception {
		assertEquals(404, send("POST", "/" + CIRoute.BUILDS, TOKEN_NO_JOB).statusCode());
		_ciConfigured = false;
		assertEquals(404, send("POST", "/" + CIRoute.BUILDS, TOKEN_ALPHA).statusCode());
		assertEquals(404, send("GET", "/" + CIRoute.BUILDS + "/" + CIRoute.LAST, TOKEN_ALPHA).statusCode());
		assertEquals("Nothing reached the CI server.", 1, _provider.calls().size());
	}

	/**
	 * The token is checked by the proxy before the route sees the request.
	 */
	public void testInvalidToken() throws Exception {
		assertEquals(ProxyError.INVALID_TOKEN.status(), send("POST", "/" + CIRoute.BUILDS, "tlwst_unknown").statusCode());
		assertEquals(ProxyError.INVALID_TOKEN.status(), send("POST", "/" + CIRoute.BUILDS, null).statusCode());
		assertEquals(1, _provider.calls().size());
	}

	/**
	 * A failure of the CI server is answered with 502 and a fixed text; a job that is gone with 404.
	 */
	public void testCIFailure() throws Exception {
		_provider.setFailure(new TopLogicException(
			I18NConstants.ERROR_CI_UNREACHABLE__SERVICE_PROBLEM.fill(RecordingCIProvider.BASE_URL, "secret detail")));
		HttpResponse<String> response = send("POST", "/" + CIRoute.BUILDS, TOKEN_ALPHA);
		assertEquals(502, response.statusCode());
		assertFalse(response.body(), response.body().contains("secret detail"));

		_provider.setFailure(new TopLogicException(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.fill(_job)));
		assertEquals(404, send("GET", "/" + CIRoute.BUILDS + "/" + CIRoute.LAST, TOKEN_ALPHA).statusCode());
	}

	/**
	 * A workspace container gets the URL of the route; the agent is told how to call it.
	 */
	public void testEnvironment() {
		assertEquals(Map.of(CIRoute.ENV_CI_URL, "http://proxy.test/ci"), _route.containerEnv("http://proxy.test/ci"));
		String instructions = CIRoute.agentInstructions();
		assertTrue(instructions, instructions.contains("$" + CIRoute.ENV_CI_URL + "/" + CIRoute.BUILDS));
		assertTrue(instructions, instructions.contains("Bearer $" + ProxyTokens.ENV_PROXY_TOKEN));
		assertTrue(instructions, instructions.contains(CIRoute.BUILDS + "/" + CIRoute.LAST));
	}

	private static Map<Object, Object> normalize(Map<?, ?> json) {
		Map<Object, Object> result = new java.util.LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : json.entrySet()) {
			Object value = entry.getValue();
			result.put(entry.getKey(), value instanceof Number number ? Integer.valueOf(number.intValue()) : value);
		}
		return result;
	}

	private HttpResponse<String> send(String method, String path, String token) throws Exception {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(_routeUrl + path))
			.method(method, HttpRequest.BodyPublishers.noBody());
		if (token != null) {
			request.header(ProxyForwarding.AUTHORIZATION, "Bearer " + token);
		}
		return _client.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

}
