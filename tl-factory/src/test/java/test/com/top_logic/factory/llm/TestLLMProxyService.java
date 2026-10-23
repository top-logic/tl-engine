/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.llm;

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
import com.top_logic.factory.llm.LLMAdmission;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.llm.SourceFilter;
import com.top_logic.factory.llm.TokenGrants;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.tl.ai.core.access.SecretTokens;

/**
 * Test of {@link LLMProxyService} without knowledge base: the environment of the workspace
 * containers, the format and lifetime of tokens, and the accepted client addresses
 * ({@link SourceFilter}).
 */
public class TestLLMProxyService extends TestCase {

	private static final String PROXY_URL = "http://host.docker.internal:8091/llm-proxy";

	private static final String SOURCE = "172.30.0.5";

	private static final String OTHER_SOURCE = "172.30.0.6";

	private final MutableClock _clock = new MutableClock();

	/**
	 * A workspace container gets the URL of the proxy, but no token.
	 */
	public void testContainerEnvironmentHasNoToken() throws Exception {
		LLMProxyService service = service(PROXY_URL);
		assertTrue(service.isEnabled());
		assertEquals(Map.of(LLMProxyService.ENV_BASE_URL, PROXY_URL), service.containerEnv());

		RecordingWorkspaceProvider provider = new RecordingWorkspaceProvider();
		ApplicationWorkspaces applications =
			new ApplicationWorkspaces(TypedConfiguration.newConfigItem(ApplicationWorkspaces.Config.class), provider);
		applications.create(new ApplicationSpec("my-app-1234", "com.example", "my-app")
			.setInitialPassword("initial")
			.putEnv(service.containerEnv()), ExecOutput.NONE);

		WorkspaceSpec created = provider.created("my-app-1234");
		Map<String, String> env = created.getEnv();
		assertEquals(PROXY_URL, env.get(LLMProxyService.ENV_BASE_URL));
		assertFalse(env.containsKey(LLMProxyService.ENV_AUTH_TOKEN));
		for (String value : env.values()) {
			assertFalse("No token in the container environment: " + value,
				value.contains(LLMProxyService.TOKEN_PREFIX));
		}
	}

	/**
	 * Without proxy URL, the proxy is switched off: no URL for the containers, no tokens.
	 */
	public void testDisabledWithoutUrl() {
		LLMProxyService service = service("");
		assertFalse(service.isEnabled());
		assertEquals(Collections.emptyMap(), service.containerEnv());
		assertNull(service.issue("ws-1", Duration.ofMinutes(5)));
	}

	/**
	 * Tokens have the format of a workspace token, differ, and live at most the configured
	 * maximum.
	 */
	public void testTokenFormatAndLifetimeClamp() {
		LLMProxyService service = service(PROXY_URL);
		assertEquals(Duration.ofHours(4), service.getMaxTokenLifetime());

		Instant before = Instant.now();
		String token = service.issue("ws-1", Duration.ofDays(3));
		Instant after = Instant.now();
		assertTrue(SecretTokens.hasFormat(LLMProxyService.TOKEN_PREFIX, token));
		assertFalse(token.equals(service.issue("ws-1", Duration.ofDays(3))));

		Instant expires = service.expiresAt(token);
		assertFalse("Cut to the maximum.", expires.isAfter(after.plus(Duration.ofHours(4))));
		assertFalse(expires.isBefore(before.plus(Duration.ofHours(4))));

		String shortToken = service.issue("ws-1", Duration.ofMinutes(10));
		assertFalse(service.expiresAt(shortToken).isAfter(Instant.now().plus(Duration.ofMinutes(10))));

		service.revoke(token);
		assertNull("Revoked.", service.expiresAt(token));
		service.revokeAll("ws-1");
		assertNull(service.expiresAt(shortToken));
	}

	/**
	 * A token that is not a workspace token is refused without a lookup.
	 */
	public void testForeignTokenRefused() {
		LLMProxyService service = service(PROXY_URL);
		assertEquals(LLMAdmission.INVALID_TOKEN, service.admit(null, SOURCE));
		assertEquals(LLMAdmission.INVALID_TOKEN, service.admit("", SOURCE));
		assertEquals(LLMAdmission.INVALID_TOKEN, service.admit(SecretTokens.newToken("tlpat_"), SOURCE));
		assertEquals("Never issued.", LLMAdmission.INVALID_TOKEN,
			service.admit(SecretTokens.newToken(LLMProxyService.TOKEN_PREFIX), SOURCE));
	}

	/**
	 * The proxy listens on a port of its own on the loopback address and does not answer in the
	 * web application, unless configured otherwise.
	 */
	public void testListenerAndServletDefaults() {
		LLMProxyService.Config<?> config = service(PROXY_URL).getConfig();
		assertEquals(LLMProxyService.DEFAULT_LISTEN_PORT, config.getListenPort());
		assertEquals(LLMProxyService.DEFAULT_LISTEN_ADDRESS, config.getListenAddress());
		assertFalse(config.isServletEnabled());
		assertEquals(LLMProxyService.DEFAULT_RATE_LIMIT, config.getRateLimit());
		assertNull("No servlet proxy without a running service.", LLMProxyService.servletProxy());
	}

	/**
	 * A token is bound to the address of its first request; other addresses are refused, its own
	 * keeps working; another token binds to its own first address.
	 */
	public void testTokenBoundToFirstSource() {
		TokenGrants grants = new TokenGrants(_clock, workspace -> true, 0);
		String token = grants.issue("ws-1", Duration.ofHours(1));
		assertEquals(LLMAdmission.ADMITTED, grants.admit(token, SOURCE));
		assertEquals(LLMAdmission.FOREIGN_SOURCE, grants.admit(token, OTHER_SOURCE));
		assertEquals(LLMAdmission.ADMITTED, grants.admit(token, SOURCE));
		assertNotNull("Not revoked.", grants.expiresAt(token));

		String other = grants.issue("ws-1", Duration.ofHours(1));
		assertEquals(LLMAdmission.ADMITTED, grants.admit(other, OTHER_SOURCE));
		assertEquals(LLMAdmission.FOREIGN_SOURCE, grants.admit(other, SOURCE));
	}

	/**
	 * A refused token does not bind: the first admitted request decides.
	 */
	public void testInvalidRequestDoesNotBind() {
		boolean[] exists = { false };
		TokenGrants grants = new TokenGrants(_clock, workspace -> exists[0], 0);
		String token = grants.issue("ws-1", Duration.ofHours(1));
		assertEquals(LLMAdmission.INVALID_TOKEN, grants.admit(token, OTHER_SOURCE));
		assertNull("Dropped with its workspace.", grants.expiresAt(token));

		exists[0] = true;
		String next = grants.issue("ws-1", Duration.ofHours(1));
		assertEquals(LLMAdmission.ADMITTED, grants.admit(next, SOURCE));
	}

	/**
	 * A token makes at most the rate limit of requests in a burst; the allowance refills with the
	 * time, and the wait reported is the time until the next request is admitted.
	 */
	public void testRateLimit() {
		TokenGrants grants = new TokenGrants(_clock, workspace -> true, 6);
		String token = grants.issue("ws-1", Duration.ofHours(1));
		for (int n = 0; n < 6; n++) {
			assertEquals("Request " + n, LLMAdmission.ADMITTED, grants.admit(token, SOURCE));
		}
		LLMAdmission limited = grants.admit(token, SOURCE);
		assertEquals(LLMAdmission.Status.RATE_LIMITED, limited.status());
		assertEquals("One request every ten seconds.", Duration.ofSeconds(10), limited.retryAfter());

		_clock.advance(Duration.ofSeconds(4));
		assertEquals(Duration.ofSeconds(6), grants.admit(token, SOURCE).retryAfter());
		_clock.advance(Duration.ofSeconds(6));
		assertEquals(LLMAdmission.ADMITTED, grants.admit(token, SOURCE));
		assertEquals(LLMAdmission.Status.RATE_LIMITED, grants.admit(token, SOURCE).status());

		_clock.advance(Duration.ofMinutes(5));
		for (int n = 0; n < 6; n++) {
			assertEquals("At most a burst of the limit: " + n, LLMAdmission.ADMITTED, grants.admit(token, SOURCE));
		}
		assertEquals(LLMAdmission.Status.RATE_LIMITED, grants.admit(token, SOURCE).status());

		TokenGrants unlimited = new TokenGrants(_clock, workspace -> true, 0);
		String free = unlimited.issue("ws-1", Duration.ofHours(1));
		for (int n = 0; n < 1000; n++) {
			assertEquals(LLMAdmission.ADMITTED, unlimited.admit(free, SOURCE));
		}
	}

	/**
	 * A token is refused after its lifetime.
	 */
	public void testExpiry() {
		TokenGrants grants = new TokenGrants(_clock, workspace -> true, 0);
		String token = grants.issue("ws-1", Duration.ofMinutes(10));
		assertEquals(LLMAdmission.ADMITTED, grants.admit(token, SOURCE));
		_clock.advance(Duration.ofMinutes(10));
		assertEquals(LLMAdmission.INVALID_TOKEN, grants.admit(token, SOURCE));
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

	private static LLMProxyService service(String proxyUrl) {
		LLMProxyService.Config<?> config = TypedConfiguration.newConfigItem(LLMProxyService.Config.class);
		TypedConfigUtil.setProperty(config, LLMProxyService.Config.PROXY_URL, proxyUrl);
		return new LLMProxyService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	/**
	 * The tests of this class, with the type index the configuration needs.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestLLMProxyService.class, TypeIndex.Module.INSTANCE);
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
