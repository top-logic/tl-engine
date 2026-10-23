/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.llm;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
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
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.llm.SourceFilter;
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

	private static final String PROXY_URL = "http://host.docker.internal:8080/tl-factory-app/llm-proxy";

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
		assertFalse(service.authenticate(null));
		assertFalse(service.authenticate(""));
		assertFalse(service.authenticate(SecretTokens.newToken("tlpat_")));
		assertFalse("Never issued.", service.authenticate(SecretTokens.newToken(LLMProxyService.TOKEN_PREFIX)));
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

}
