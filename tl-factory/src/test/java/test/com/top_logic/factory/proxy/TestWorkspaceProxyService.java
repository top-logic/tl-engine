/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.proxy;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.workspace.RecordingWorkspaceProvider;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.llm.LLMRoute;
import com.top_logic.factory.proxy.GitEnvironment;
import com.top_logic.factory.proxy.ProcessToken;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyRoute;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.SourceFilter;
import com.top_logic.factory.proxy.TokenGrants;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.tl.ai.core.access.SecretTokens;

/**
 * Test of {@link WorkspaceProxyService} without knowledge base: the environment of the workspace
 * containers and processes, the format and lifetime of tokens, and the accepted client addresses
 * ({@link SourceFilter}).
 */
public class TestWorkspaceProxyService extends TestCase {

	private static final String PROXY_URL = "http://host.docker.internal:8091";

	private static final String LLM_URL = PROXY_URL + LLMRoute.DEFAULT_PATH;

	private static final String WORKSPACE = "ws-1";

	private static final String SOURCE = "172.30.0.5";

	private static final String OTHER_SOURCE = "172.30.0.6";

	private final MutableClock _clock = new MutableClock();

	/**
	 * A workspace container gets the URLs of the routes, but no token.
	 */
	public void testContainerEnvironmentHasNoToken() throws Exception {
		WorkspaceProxyService service = service(PROXY_URL);
		assertTrue(service.isEnabled());
		assertEquals(Map.of(LLMRoute.ENV_BASE_URL, LLM_URL), service.containerEnv());
		assertEquals("A slash at the end of the proxy URL is dropped.", Map.of(LLMRoute.ENV_BASE_URL, LLM_URL),
			service(PROXY_URL + "/").containerEnv());

		RecordingWorkspaceProvider provider = new RecordingWorkspaceProvider();
		ApplicationWorkspaces applications =
			new ApplicationWorkspaces(TypedConfiguration.newConfigItem(ApplicationWorkspaces.Config.class), provider);
		applications.create(new ApplicationSpec("my-app-1234", "com.example", "my-app")
			.setInitialPassword("initial")
			.putEnv(service.containerEnv()), ExecOutput.NONE);

		WorkspaceSpec created = provider.created("my-app-1234");
		Map<String, String> env = created.getEnv();
		assertEquals(LLM_URL, env.get(LLMRoute.ENV_BASE_URL));
		assertFalse(env.containsKey(LLMRoute.ENV_AUTH_TOKEN));
		for (String value : env.values()) {
			assertFalse("No token in the container environment: " + value,
				value.contains(TokenGrants.TOKEN_PREFIX));
		}
	}

	/**
	 * A process gets the token in the environment of its routes.
	 */
	public void testProcessEnvironment() {
		WorkspaceProxyService service = service(PROXY_URL);
		String token = service.issue(WORKSPACE, Duration.ofMinutes(5));
		assertEquals(Map.of(ProxyTokens.ENV_PROXY_TOKEN, token, LLMRoute.ENV_AUTH_TOKEN, token),
			service.processEnv(token));
	}

	/**
	 * With the git route, a container gets the URL of the route, a process the credential helper
	 * for that URL in one set of git configuration variables, without the token in it.
	 */
	public void testGitRouteEnvironment() {
		WorkspaceProxyService service = service(PROXY_URL + "/", TypedConfiguration.newConfigItem(GitRoute.Config.class));
		String gitUrl = PROXY_URL + GitRoute.DEFAULT_PATH;
		assertEquals(gitUrl, service.getRouteUrl(service.getRoute(GitRoute.class)));
		assertEquals(gitUrl, service.containerEnv().get(GitRoute.ENV_GIT_URL));
		assertEquals(LLM_URL, service.containerEnv().get(LLMRoute.ENV_BASE_URL));

		String token = service.issue(WORKSPACE, Duration.ofMinutes(5));
		Map<String, String> env = service.processEnv(token);
		assertEquals(token, env.get(ProxyTokens.ENV_PROXY_TOKEN));
		assertEquals(token, env.get(LLMRoute.ENV_AUTH_TOKEN));
		assertEquals("4", env.get(GitEnvironment.ENV_CONFIG_COUNT));
		assertEquals(GitRoute.helperKey(gitUrl), env.get(GitEnvironment.ENV_CONFIG_KEY_PREFIX + 0));
		assertEquals("Resets the helpers of the URL.", "", env.get(GitEnvironment.ENV_CONFIG_VALUE_PREFIX + 0));
		assertEquals(GitRoute.helperKey(gitUrl), env.get(GitEnvironment.ENV_CONFIG_KEY_PREFIX + 1));
		assertEquals(GitRoute.CREDENTIAL_HELPER, env.get(GitEnvironment.ENV_CONFIG_VALUE_PREFIX + 1));
		assertEquals(GitRoute.GIT_USER_NAME, env.get(GitEnvironment.ENV_CONFIG_KEY_PREFIX + 2));
		assertEquals(GitRoute.DEFAULT_USER_NAME, env.get(GitEnvironment.ENV_CONFIG_VALUE_PREFIX + 2));
		assertEquals(GitRoute.GIT_USER_EMAIL, env.get(GitEnvironment.ENV_CONFIG_KEY_PREFIX + 3));
		assertEquals(GitRoute.DEFAULT_USER_EMAIL, env.get(GitEnvironment.ENV_CONFIG_VALUE_PREFIX + 3));
		for (Map.Entry<String, String> entry : env.entrySet()) {
			if (!entry.getKey().equals(ProxyTokens.ENV_PROXY_TOKEN) && !entry.getKey().equals(LLMRoute.ENV_AUTH_TOKEN)) {
				assertFalse("Token in " + entry.getKey(), entry.getValue().contains(token));
			}
		}
		assertEquals(11, env.size());
	}

	/**
	 * A git route without identity contributes the credential helper only.
	 */
	public void testGitRouteWithoutIdentity() {
		GitRoute.Config<?> route = TypedConfiguration.newConfigItem(GitRoute.Config.class);
		TypedConfigUtil.setProperty(route, GitRoute.Config.USER_NAME, "");
		TypedConfigUtil.setProperty(route, GitRoute.Config.USER_EMAIL, "");
		WorkspaceProxyService service = service(PROXY_URL, route);
		Map<String, String> env = service.processEnv("token");
		assertEquals("2", env.get(GitEnvironment.ENV_CONFIG_COUNT));
		assertFalse(env.containsValue(GitRoute.GIT_USER_NAME));
	}

	/**
	 * The git configuration of several routes is numbered through, so that no route overwrites the
	 * entries of another.
	 */
	public void testGitConfigOfSeveralRoutes() {
		GitRoute.Config<?> first = TypedConfiguration.newConfigItem(GitRoute.Config.class);
		GitRoute.Config<?> second = TypedConfiguration.newConfigItem(GitRoute.Config.class);
		TypedConfigUtil.setProperty(second, ProxyRoute.Config.PATH, "/git2");
		WorkspaceProxyService service = service(PROXY_URL, first, second);
		Map<String, String> env = service.processEnv("token");
		assertEquals("8", env.get(GitEnvironment.ENV_CONFIG_COUNT));
		assertEquals(GitRoute.helperKey(PROXY_URL + "/git2"), env.get(GitEnvironment.ENV_CONFIG_KEY_PREFIX + 5));
	}

	/**
	 * A token for one command of the control application carries the environment of a process and
	 * is revoked when closed.
	 */
	public void testProcessToken() {
		WorkspaceProxyService service = service(PROXY_URL);
		String token;
		try (ProcessToken processToken = service.issueProcessToken(WORKSPACE, Duration.ofMinutes(1))) {
			token = processToken.env().get(ProxyTokens.ENV_PROXY_TOKEN);
			assertNotNull(token);
			assertEquals(service.processEnv(token), processToken.env());
			assertFalse("No token in the string form.", processToken.toString().contains(token));
			assertNotNull(service.expiresAt(token));
		}
		assertNull("Revoked.", service.expiresAt(token));
		assertNull("Switched off.", service("").issueProcessToken(WORKSPACE, Duration.ofMinutes(1)));
	}

	/**
	 * Without proxy URL, the proxy is switched off: no URL for the containers, no tokens.
	 */
	public void testDisabledWithoutUrl() {
		WorkspaceProxyService service = service("");
		assertFalse(service.isEnabled());
		assertEquals(Collections.emptyMap(), service.containerEnv());
		assertNull(service.issue(WORKSPACE, Duration.ofMinutes(5)));
	}

	/**
	 * Tokens have the format of a workspace token, differ, and live at most the configured
	 * maximum.
	 */
	public void testTokenFormatAndLifetimeClamp() {
		WorkspaceProxyService service = service(PROXY_URL);
		assertEquals(Duration.ofHours(4), service.getMaxTokenLifetime());

		Instant before = Instant.now();
		String token = service.issue(WORKSPACE, Duration.ofDays(3));
		Instant after = Instant.now();
		assertTrue(SecretTokens.hasFormat(TokenGrants.TOKEN_PREFIX, token));
		assertFalse(token.equals(service.issue(WORKSPACE, Duration.ofDays(3))));

		Instant expires = service.expiresAt(token);
		assertFalse("Cut to the maximum.", expires.isAfter(after.plus(Duration.ofHours(4))));
		assertFalse(expires.isBefore(before.plus(Duration.ofHours(4))));

		String shortToken = service.issue(WORKSPACE, Duration.ofMinutes(10));
		assertFalse(service.expiresAt(shortToken).isAfter(Instant.now().plus(Duration.ofMinutes(10))));

		service.revoke(token);
		assertNull("Revoked.", service.expiresAt(token));
		service.revokeAll(WORKSPACE);
		assertNull(service.expiresAt(shortToken));
	}

	/**
	 * A token that is not a workspace token is refused without a lookup.
	 */
	public void testForeignTokenRefused() {
		WorkspaceProxyService service = service(PROXY_URL);
		assertEquals(ProxyAdmission.INVALID_TOKEN, service.admit(null, SOURCE));
		assertEquals(ProxyAdmission.INVALID_TOKEN, service.admit("", SOURCE));
		assertEquals(ProxyAdmission.INVALID_TOKEN, service.admit(SecretTokens.newToken("tlpat_"), SOURCE));
		assertEquals("Never issued.", ProxyAdmission.INVALID_TOKEN,
			service.admit(SecretTokens.newToken(TokenGrants.TOKEN_PREFIX), SOURCE));
	}

	/**
	 * The proxy listens on a port of its own on the loopback address and does not answer in the
	 * web application, unless configured otherwise.
	 */
	public void testListenerAndServletDefaults() {
		WorkspaceProxyService.Config<?> config = service(PROXY_URL).getConfig();
		assertEquals(WorkspaceProxyService.DEFAULT_LISTEN_PORT, config.getListenPort());
		assertEquals(WorkspaceProxyService.DEFAULT_LISTEN_ADDRESS, config.getListenAddress());
		assertFalse(config.isServletEnabled());
		assertEquals(WorkspaceProxyService.DEFAULT_RATE_LIMIT, config.getRateLimit());
		assertNull("No servlet proxy without a running service.", WorkspaceProxyService.servletProxy());
	}

	/**
	 * A token is bound to the address of its first request; other addresses are refused, its own
	 * keeps working; another token binds to its own first address.
	 */
	public void testTokenBoundToFirstSource() {
		TokenGrants grants = new TokenGrants(_clock, workspace -> true, 0);
		String token = grants.issue(WORKSPACE, Duration.ofHours(1));
		assertAdmitted(grants.admit(token, SOURCE));
		assertEquals(ProxyAdmission.FOREIGN_SOURCE, grants.admit(token, OTHER_SOURCE));
		assertAdmitted(grants.admit(token, SOURCE));
		assertNotNull("Not revoked.", grants.expiresAt(token));

		String other = grants.issue(WORKSPACE, Duration.ofHours(1));
		assertAdmitted(grants.admit(other, OTHER_SOURCE));
		assertEquals(ProxyAdmission.FOREIGN_SOURCE, grants.admit(other, SOURCE));
	}

	/**
	 * A refused token does not bind: the first admitted request decides.
	 */
	public void testInvalidRequestDoesNotBind() {
		boolean[] exists = { false };
		TokenGrants grants = new TokenGrants(_clock, workspace -> exists[0], 0);
		String token = grants.issue(WORKSPACE, Duration.ofHours(1));
		assertEquals(ProxyAdmission.INVALID_TOKEN, grants.admit(token, OTHER_SOURCE));
		assertNull("Dropped with its workspace.", grants.expiresAt(token));

		exists[0] = true;
		String next = grants.issue(WORKSPACE, Duration.ofHours(1));
		assertAdmitted(grants.admit(next, SOURCE));
	}

	/**
	 * A token makes at most the rate limit of requests in a burst; the allowance refills with the
	 * time, and the wait reported is the time until the next request is admitted.
	 */
	public void testRateLimit() {
		TokenGrants grants = new TokenGrants(_clock, workspace -> true, 6);
		String token = grants.issue(WORKSPACE, Duration.ofHours(1));
		for (int n = 0; n < 6; n++) {
			assertAdmitted("Request " + n, grants.admit(token, SOURCE));
		}
		ProxyAdmission limited = grants.admit(token, SOURCE);
		assertEquals(ProxyAdmission.Status.RATE_LIMITED, limited.status());
		assertEquals("One request every ten seconds.", Duration.ofSeconds(10), limited.retryAfter());

		_clock.advance(Duration.ofSeconds(4));
		assertEquals(Duration.ofSeconds(6), grants.admit(token, SOURCE).retryAfter());
		_clock.advance(Duration.ofSeconds(6));
		assertAdmitted(grants.admit(token, SOURCE));
		assertEquals(ProxyAdmission.Status.RATE_LIMITED, grants.admit(token, SOURCE).status());

		_clock.advance(Duration.ofMinutes(5));
		for (int n = 0; n < 6; n++) {
			assertAdmitted("At most a burst of the limit: " + n, grants.admit(token, SOURCE));
		}
		assertEquals(ProxyAdmission.Status.RATE_LIMITED, grants.admit(token, SOURCE).status());

		TokenGrants unlimited = new TokenGrants(_clock, workspace -> true, 0);
		String free = unlimited.issue(WORKSPACE, Duration.ofHours(1));
		for (int n = 0; n < 1000; n++) {
			assertAdmitted(unlimited.admit(free, SOURCE));
		}
	}

	/**
	 * A token is refused after its lifetime.
	 */
	public void testExpiry() {
		TokenGrants grants = new TokenGrants(_clock, workspace -> true, 0);
		String token = grants.issue(WORKSPACE, Duration.ofMinutes(10));
		assertAdmitted(grants.admit(token, SOURCE));
		_clock.advance(Duration.ofMinutes(10));
		assertEquals(ProxyAdmission.INVALID_TOKEN, grants.admit(token, SOURCE));
		assertNull(grants.expiresAt(token));
	}

	/**
	 * Without entries, every address is accepted.
	 */
	public void testEmptySourcesAcceptAll() throws ParseException {
		SourceFilter filter = new SourceFilter(List.of(), Collections::emptyList);
		assertTrue(filter.allows("203.0.113.7"));
		assertTrue(filter.allows("2001:db8::1"));
		assertTrue(service(PROXY_URL).isAllowedSource("203.0.113.7"));
	}

	/**
	 * IPv4 and IPv6 ranges and single addresses.
	 */
	public void testAddressRanges() throws ParseException {
		SourceFilter filter = new SourceFilter(List.of("172.18.0.0/16", " 127.0.0.1 ", "fd00:1::/64"),
			Collections::emptyList);
		assertTrue(filter.allows("172.18.0.5"));
		assertTrue(filter.allows("127.0.0.1"));
		assertFalse("Foreign IPv4.", filter.allows("172.19.0.5"));
		assertFalse(filter.allows("127.0.0.2"));
		assertTrue(filter.allows("fd00:1::42"));
		assertTrue("Bracketed IPv6.", filter.allows("[fd00:1::42]"));
		assertFalse("Foreign IPv6.", filter.allows("fd00:2::42"));
		assertTrue("IPv4 mapped to IPv6.", filter.allows("::ffff:172.18.0.5"));
		assertFalse(filter.allows(null));
		assertFalse(filter.allows("not-an-address"));
	}

	/**
	 * {@value SourceFilter#WORKSPACE_NETWORK} stands for the ranges the provider reports for the
	 * network of the workspace containers.
	 */
	public void testWorkspaceNetwork() throws ParseException {
		RecordingWorkspaceProvider provider =
			new RecordingWorkspaceProvider().setNetworkSubnets("172.30.0.0/16", "fd00:30::/64");
		SourceFilter filter = new SourceFilter(List.of(SourceFilter.WORKSPACE_NETWORK), provider::networkSubnets);
		assertTrue(filter.allows("172.30.4.2"));
		assertTrue(filter.allows("fd00:30::2"));
		assertFalse(filter.allows("172.31.4.2"));
		assertFalse(filter.allows("10.0.0.1"));
	}

	/**
	 * An entry that is neither a range nor the workspace network is a configuration error.
	 */
	public void testInvalidSource() {
		for (String invalid : List.of("172.18.0.0/33", "workspace", "example.com")) {
			try {
				new SourceFilter(List.of(invalid), Collections::emptyList);
				fail("Accepted: " + invalid);
			} catch (ParseException ex) {
				// Expected.
			}
		}
	}

	private static WorkspaceProxyService service(String proxyUrl) {
		return service(proxyUrl, new ProxyRoute.Config<?>[0]);
	}

	/**
	 * A proxy with the LLM route and the given further routes.
	 */
	private static WorkspaceProxyService service(String proxyUrl, ProxyRoute.Config<?>... routes) {
		WorkspaceProxyService.Config<?> config = TypedConfiguration.newConfigItem(WorkspaceProxyService.Config.class);
		TypedConfigUtil.setProperty(config, WorkspaceProxyService.Config.PROXY_URL, proxyUrl);
		config.getRoutes().add(TypedConfiguration.newConfigItem(LLMRoute.Config.class));
		config.getRoutes().addAll(List.of(routes));
		return new WorkspaceProxyService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	private static void assertAdmitted(ProxyAdmission admission) {
		assertAdmitted(null, admission);
	}

	private static void assertAdmitted(String message, ProxyAdmission admission) {
		assertEquals(message, ProxyAdmission.Status.ADMITTED, admission.status());
		assertEquals(message, WORKSPACE, admission.workspaceId());
	}

	/**
	 * The tests of this class, with the type index the configuration needs.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestWorkspaceProxyService.class, TypeIndex.Module.INSTANCE);
	}

	/**
	 * {@link Clock} the test advances.
	 */
	private static final class MutableClock extends Clock {

		private Instant _now = Instant.parse("2026-01-01T00:00:00Z");

		void advance(Duration duration) {
			_now = _now.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return _now;
		}
	}

}
