/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2AService.RemoteAgentConfig;
import com.top_logic.ai.a2a.client.RemoteAgents.RemoteAgent;
import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Tests how the {@link A2AClient} authenticates the calls to a remote agent: with the configured
 * {@link RemoteAgentConfig#getCredentials() credentials} of the agent, without credentials if none
 * are configured, and once more with fresh credentials after a rejection.
 */
public class TestRemoteAgentCredentials extends TestCase {

	/** The system property holding the token of the remote agent. */
	private static final String VARIABLE = "test_a2a_remote_token";

	/** The token in {@link #VARIABLE}. */
	private static final String TOKEN = "a2a-token-0123456789";

	private static final String OK = "{\"name\":\"Other\"}";

	private HttpServer _server;

	private final List<String> _authorizations = new CopyOnWriteArrayList<>();

	private IntSupplier _status = () -> 200;

	/**
	 * Parsing remote agent configurations resolves the nested configuration types of the secret
	 * sources and credentials through the <code>TypeIndex</code>, which needs the module system
	 * running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRemoteAgentCredentials.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::serve);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** A bearer token referenced by its variable is read from the variable for each call. */
	public void testBearerTokenFromEnvironment() throws Exception {
		System.setProperty(VARIABLE, TOKEN);
		RemoteAgent agent = agent("<credentials class='" + HeaderCredentials.class.getName() + "'"
			+ " scheme='" + CredentialProvider.BEARER + "'>"
			+ "<secret class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>"
			+ "</credentials>");
		new A2AClient().getJson(url(), agent.credentials(), false);

		System.setProperty(VARIABLE, "rotated");
		new A2AClient().getJson(url(), agent.credentials(), false);

		assertEquals(List.of(CredentialProvider.BEARER + " " + TOKEN, CredentialProvider.BEARER + " rotated"),
			_authorizations);
	}

	/** A remote agent without credentials is called without credentials. */
	public void testNoCredentials() throws Exception {
		RemoteAgent agent = agent("");
		new A2AClient().getJson(url(), agent.credentials(), false);

		assertEquals(List.of("null"), _authorizations);
	}

	/** A rejected call is repeated once with fresh credentials. */
	public void testRetryWithFreshCredentials() throws Exception {
		AtomicInteger requests = new AtomicInteger();
		_status = () -> requests.getAndIncrement() == 0 ? 401 : 200;
		RotatingCredentials credentials = new RotatingCredentials();

		new A2AClient().getJson(url(), credentials, false);

		assertEquals(1, credentials._invalidations);
		assertEquals(List.of("Bearer token-0", "Bearer token-1"), _authorizations);
	}

	/** Credentials rejected again are not tried a third time; the rejection is reported. */
	public void testRetryOnlyOnce() throws Exception {
		_status = () -> 401;
		RotatingCredentials credentials = new RotatingCredentials();
		try {
			new A2AClient().call(url(), credentials, "GetTask", Json.map("id", "t1"), Duration.ofSeconds(10));
			fail("A rejected call must fail.");
		} catch (A2AError ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("refused the credentials"));
		}
		assertEquals(2, _authorizations.size());
		assertEquals(2, credentials._invalidations);
	}

	/**
	 * The service editor stores a token referenced by its secret name in the form it was read,
	 * holding only the name of the secret.
	 */
	public void testCredentialsStorage() throws IOException, ConfigurationException {
		System.setProperty(VARIABLE, TOKEN);
		String stored = """
			<?xml version="1.0" encoding="utf-8" ?>

			<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
				xmlns:config="http://www.top-logic.com/ns/config/6.0"
			>
				<services>
					<config service-class="com.top_logic.ai.a2a.A2AService"
						config:override="true"
					>
						<instance class="com.top_logic.ai.a2a.A2AService">
							<remote-agents>
								<remote-agent name="other" card-url="https://example.com/a2a/agent-card.json">
									<credentials class="com.top_logic.basic.credential.HeaderCredentials"
										scheme="Bearer"
									>
										<secret name="%s"/>
									</credentials>
								</remote-agent>
							</remote-agents>
						</instance>
					</config>
				</services>
			</application>""".formatted(VARIABLE);
		File file = BasicTestCase.createNamedTestFile(getName() + InAppServiceConfigStore.FILE_SUFFIX);
		try {
			FileUtilities.writeStringToFile(stored, file, StringServices.UTF8);
			ApplicationConfig.Config config = InAppServiceConfigStore.read(file);
			String written = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG, config,
				InAppServiceConfigStore.OVERRIDE_SERVICE);

			assertTrue(written, written.contains("name=\"" + VARIABLE + "\""));
			// An element without class is a NamedSecretSource, the default secret source.
			assertTrue(written, written.contains("<secret name=\"" + VARIABLE + "\"/>"));
			assertFalse(written, written.contains(TOKEN));

			FileUtilities.writeStringToFile(written, file, StringServices.UTF8);
			String rewritten = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG,
				InAppServiceConfigStore.read(file), InAppServiceConfigStore.OVERRIDE_SERVICE);
			assertEquals(written, rewritten);
		} finally {
			file.delete();
		}
	}

	private String url() {
		return "http://127.0.0.1:" + _server.getAddress().getPort() + "/a2a/other";
	}

	private RemoteAgent agent(String content) throws ConfigurationException {
		String xml = "<remote-agent xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + A2AService.RemoteAgentConfig.class.getName() + "'"
			+ " name='other' card-url='" + url() + "'>" + content + "</remote-agent>";
		RemoteAgentConfig config = (RemoteAgentConfig) TypedConfiguration.parse(CharacterContents.newContent(xml));
		RemoteAgents agents = new RemoteAgents(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, List.of(config));
		return agents.get("other");
	}

	private void serve(HttpExchange exchange) throws IOException {
		try {
			_authorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst(CredentialProvider.AUTHORIZATION)));
			int status = _status.getAsInt();
			byte[] body = (status == 200 ? OK : "{\"error\":\"unauthorized\"}").getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(status, body.length);
			exchange.getResponseBody().write(body);
		} finally {
			exchange.close();
		}
	}

	/**
	 * Credentials sending a bearer token that is replaced by the next one on each invalidation.
	 */
	private static final class RotatingCredentials implements CredentialProvider {

		int _invalidations;

		@Override
		public void authenticate(RequestAuthentication request) {
			request.setHeader(CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER + " token-" + _invalidations);
		}

		@Override
		public void invalidate() {
			_invalidations++;
		}

	}

}
